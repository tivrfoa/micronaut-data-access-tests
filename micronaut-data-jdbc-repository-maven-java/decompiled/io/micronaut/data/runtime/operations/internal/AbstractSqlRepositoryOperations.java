package io.micronaut.data.runtime.operations.internal;

import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.QueryParameter;
import io.micronaut.data.model.query.builder.QueryResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.repository.GenericRepository;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.data.runtime.mapper.QueryStatement;
import io.micronaut.data.runtime.mapper.ResultReader;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

@Internal
public abstract class AbstractSqlRepositoryOperations<Cnt, RS, PS, Exc extends Exception>
   extends AbstractRepositoryOperations<Cnt, PS>
   implements ApplicationContextProvider,
   OpContext<Cnt, PS> {
   protected static final Logger QUERY_LOG = DataSettings.QUERY_LOG;
   protected static final SqlQueryBuilder DEFAULT_SQL_BUILDER = new SqlQueryBuilder();
   protected final ResultReader<RS, String> columnNameResultSetReader;
   protected final ResultReader<RS, Integer> columnIndexResultSetReader;
   protected final QueryStatement<PS, Integer> preparedStatementWriter;
   protected final Map<Class, SqlQueryBuilder> queryBuilders = new HashMap(10);
   private final Map<AbstractSqlRepositoryOperations<Cnt, RS, PS, Exc>.QueryKey, DBOperation> entityInserts = new ConcurrentHashMap(10);
   private final Map<AbstractSqlRepositoryOperations<Cnt, RS, PS, Exc>.QueryKey, DBOperation> entityUpdates = new ConcurrentHashMap(10);
   private final Map<Association, String> associationInserts = new ConcurrentHashMap(10);

   protected AbstractSqlRepositoryOperations(
      String dataSourceName,
      ResultReader<RS, String> columnNameResultSetReader,
      ResultReader<RS, Integer> columnIndexResultSetReader,
      QueryStatement<PS, Integer> preparedStatementWriter,
      List<MediaTypeCodec> codecs,
      DateTimeProvider<Object> dateTimeProvider,
      RuntimeEntityRegistry runtimeEntityRegistry,
      BeanContext beanContext,
      DataConversionService<?> conversionService,
      AttributeConverterRegistry attributeConverterRegistry
   ) {
      super(codecs, dateTimeProvider, runtimeEntityRegistry, conversionService, attributeConverterRegistry);
      this.columnNameResultSetReader = columnNameResultSetReader;
      this.columnIndexResultSetReader = columnIndexResultSetReader;
      this.preparedStatementWriter = preparedStatementWriter;

      for(BeanDefinition<GenericRepository> beanDefinition : beanContext.getBeanDefinitions(GenericRepository.class, Qualifiers.byStereotype(Repository.class))) {
         String targetDs = (String)beanDefinition.stringValue(Repository.class).orElse("default");
         if (targetDs.equalsIgnoreCase(dataSourceName)) {
            Class<GenericRepository> beanType = beanDefinition.getBeanType();
            SqlQueryBuilder queryBuilder = new SqlQueryBuilder(beanDefinition.getAnnotationMetadata());
            this.queryBuilders.put(beanType, queryBuilder);
         }
      }

   }

   protected <T, R> PS prepareStatement(
      Cnt connection,
      AbstractSqlRepositoryOperations.StatementSupplier<PS> statementFunction,
      @NonNull PreparedQuery<T, R> preparedQuery,
      boolean isUpdate,
      boolean isSingleResult
   ) throws Exc {
      SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(preparedQuery.getRepositoryType(), DEFAULT_SQL_BUILDER);
      RuntimePersistentEntity<T> persistentEntity = this.getEntity(preparedQuery.getRootEntity());
      PreparedQueryDBOperation pqSqlOperation = new PreparedQueryDBOperation(preparedQuery, queryBuilder);
      pqSqlOperation.checkForParameterToBeExpanded(persistentEntity, (T)null);
      if (!isUpdate) {
         pqSqlOperation.attachPageable(preparedQuery.getPageable(), isSingleResult, persistentEntity, queryBuilder);
      }

      String query = pqSqlOperation.getQuery();
      if (QUERY_LOG.isDebugEnabled()) {
         QUERY_LOG.debug("Executing Query: {}", query);
      }

      PS ps;
      try {
         ps = statementFunction.create(query);
      } catch (Exception var12) {
         throw new DataAccessException("Unable to prepare query [" + query + "]: " + var12.getMessage(), var12);
      }

      pqSqlOperation.setParameters(this, connection, ps, persistentEntity, (T)null, null);
      return ps;
   }

   @Override
   public void setStatementParameter(PS preparedStatement, int index, DataType dataType, Object value, Dialect dialect) {
      switch(dataType) {
         case UUID:
            if (value != null && dialect.requiresStringUUID(dataType)) {
               value = value.toString();
            }
            break;
         case JSON:
            if (value != null && this.jsonCodec != null && !value.getClass().equals(String.class)) {
               value = new String(this.jsonCodec.encode(value), StandardCharsets.UTF_8);
            }
            break;
         case ENTITY:
            if (value != null) {
               RuntimePersistentProperty<Object> idReader = this.getIdReader(value);
               Object id = idReader.getProperty().get(value);
               if (id == null) {
                  throw new DataAccessException("Supplied entity is a transient instance: " + value);
               }

               this.setStatementParameter(preparedStatement, index, idReader.getDataType(), id, dialect);
               return;
            }
      }

      dataType = dialect.getDataType(dataType);
      if (QUERY_LOG.isTraceEnabled()) {
         QUERY_LOG.trace("Binding parameter at position {} to value {} with data type: {}", index, value, dataType);
      }

      this.preparedStatementWriter.setDynamic(preparedStatement, index, dataType, value);
   }

   @NonNull
   protected DBOperation resolveEntityInsert(
      AnnotationMetadata annotationMetadata, Class<?> repositoryType, @NonNull Class<?> rootEntity, @NonNull RuntimePersistentEntity<?> persistentEntity
   ) {
      return (DBOperation)this.entityInserts.computeIfAbsent(new AbstractSqlRepositoryOperations.QueryKey(repositoryType, rootEntity), queryKey -> {
         SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(repositoryType, DEFAULT_SQL_BUILDER);
         QueryResult queryResult = queryBuilder.buildInsert(annotationMetadata, persistentEntity);
         return new QueryResultSqlOperation(queryBuilder, queryResult);
      });
   }

   protected <T> String resolveAssociationInsert(Class repositoryType, RuntimePersistentEntity<T> persistentEntity, RuntimeAssociation<T> association) {
      return (String)this.associationInserts.computeIfAbsent(association, association1 -> {
         SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(repositoryType, DEFAULT_SQL_BUILDER);
         return queryBuilder.buildJoinTableInsert(persistentEntity, association1);
      });
   }

   @NonNull
   protected DBOperation resolveEntityUpdate(
      AnnotationMetadata annotationMetadata, Class<?> repositoryType, @NonNull Class<?> rootEntity, @NonNull RuntimePersistentEntity<?> persistentEntity
   ) {
      AbstractSqlRepositoryOperations<Cnt, RS, PS, Exc>.QueryKey key = new AbstractSqlRepositoryOperations.QueryKey(repositoryType, rootEntity);
      return (DBOperation)this.entityUpdates
         .computeIfAbsent(
            key,
            queryKey -> {
               SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(repositoryType, DEFAULT_SQL_BUILDER);
               PersistentProperty identity = persistentEntity.getIdentity();
               String idName;
               if (identity != null) {
                  idName = identity.getName();
               } else {
                  idName = "id";
               }
      
               QueryModel queryModel = QueryModel.from(persistentEntity).idEq(new QueryParameter(idName));
               List<String> updateProperties = (List)persistentEntity.getPersistentProperties()
                  .stream()
                  .filter(
                     p -> (!(p instanceof Association) || !((Association)p).isForeignKey())
                           && p.getAnnotationMetadata().booleanValue(AutoPopulated.class, "updateable").orElse(true)
                  )
                  .map(PersistentProperty::getName)
                  .collect(Collectors.toList());
               QueryResult queryResult = queryBuilder.buildUpdate(annotationMetadata, queryModel, updateProperties);
               return new QueryResultSqlOperation(queryBuilder, queryResult);
            }
         );
   }

   protected <T> DBOperation resolveSqlInsertAssociation(
      Class<?> repositoryType, Dialect dialect, RuntimeAssociation<T> association, RuntimePersistentEntity<T> persistentEntity, T entity
   ) {
      String sqlInsert = this.resolveAssociationInsert(repositoryType, persistentEntity, association);
      return new DBOperation(sqlInsert, dialect) {
         @Override
         public <T, Cnt, PS> void setParameters(
            OpContext<Cnt, PS> context, Cnt connection, PS ps, RuntimePersistentEntity<T> pe, T e, Map<QueryParameterBinding, Object> previousValues
         ) {
            int i = 0;

            for(Entry<PersistentProperty, Object> property : (List)AbstractSqlRepositoryOperations.this.idPropertiesWithValues(
                  persistentEntity.getIdentity(), entity
               )
               .collect(Collectors.toList())) {
               Object value = context.convert(connection, property.getValue(), (RuntimePersistentProperty<?>)property.getKey());
               context.setStatementParameter(
                  ps, AbstractSqlRepositoryOperations.this.shiftIndex(i++), ((PersistentProperty)property.getKey()).getDataType(), value, this.dialect
               );
            }

            for(Entry<PersistentProperty, Object> property : (List)AbstractSqlRepositoryOperations.this.idPropertiesWithValues(pe.getIdentity(), e)
               .collect(Collectors.toList())) {
               Object value = context.convert(connection, property.getValue(), (RuntimePersistentProperty<?>)property.getKey());
               context.setStatementParameter(
                  ps, AbstractSqlRepositoryOperations.this.shiftIndex(i++), ((PersistentProperty)property.getKey()).getDataType(), value, this.dialect
               );
            }

         }
      };
   }

   private Stream<Entry<PersistentProperty, Object>> idPropertiesWithValues(PersistentProperty property, Object value) {
      Object propertyValue = ((RuntimePersistentProperty)property).getProperty().get(value);
      if (property instanceof Embedded) {
         Embedded embedded = (Embedded)property;
         PersistentEntity embeddedEntity = embedded.getAssociatedEntity();
         return embeddedEntity.getPersistentProperties().stream().flatMap(prop -> this.idPropertiesWithValues(prop, propertyValue));
      } else if (property instanceof Association) {
         Association association = (Association)property;
         if (association.isForeignKey()) {
            return Stream.empty();
         } else {
            PersistentEntity associatedEntity = association.getAssociatedEntity();
            PersistentProperty identity = associatedEntity.getIdentity();
            if (identity == null) {
               throw new IllegalStateException("Identity cannot be missing for: " + associatedEntity);
            } else {
               return this.idPropertiesWithValues(identity, propertyValue);
            }
         }
      } else {
         return Stream.of(new SimpleEntry(property, propertyValue));
      }
   }

   protected boolean isSupportsBatchInsert(PersistentEntity persistentEntity, Dialect dialect) {
      switch(dialect) {
         case SQL_SERVER:
            return false;
         case MYSQL:
         case ORACLE:
            if (persistentEntity.getIdentity() != null) {
               return !persistentEntity.getIdentity().isGenerated();
            }

            return false;
         default:
            return true;
      }
   }

   protected boolean isSupportsBatchUpdate(PersistentEntity persistentEntity, Dialect dialect) {
      return true;
   }

   protected boolean isSupportsBatchDelete(PersistentEntity persistentEntity, Dialect dialect) {
      return true;
   }

   private class QueryKey {
      final Class repositoryType;
      final Class entityType;

      QueryKey(Class repositoryType, Class entityType) {
         this.repositoryType = repositoryType;
         this.entityType = entityType;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractSqlRepositoryOperations<Cnt, RS, PS, Exc>.QueryKey queryKey = (AbstractSqlRepositoryOperations.QueryKey)o;
            return this.repositoryType.equals(queryKey.repositoryType) && this.entityType.equals(queryKey.entityType);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.repositoryType, this.entityType});
      }
   }

   @FunctionalInterface
   protected interface StatementSupplier<PS> {
      PS create(String ps) throws Exception;
   }
}
