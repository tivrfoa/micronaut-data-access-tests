package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.type.Argument;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Internal
public final class PreparedQueryDBOperation extends StoredSqlOperation {
   private final PreparedQuery<?, ?> preparedQuery;

   protected PreparedQueryDBOperation(@NonNull PreparedQuery<?, ?> preparedQuery, SqlQueryBuilder queryBuilder) {
      super(queryBuilder, preparedQuery.getQuery(), preparedQuery.getExpandableQueryParts(), preparedQuery.getQueryBindings(), false);
      this.preparedQuery = preparedQuery;
   }

   @Override
   protected <T> int getQueryParameterValueSize(QueryParameterBinding parameter, RuntimePersistentEntity<T> persistentEntity, T entity) {
      int parameterIndex = parameter.getParameterIndex();
      return parameterIndex == -1 ? 1 : this.sizeOf(this.preparedQuery.getParameterArray()[parameterIndex]);
   }

   public <K> void attachPageable(Pageable pageable, boolean isSingleResult, RuntimePersistentEntity<K> persistentEntity, SqlQueryBuilder queryBuilder) {
      if (pageable != Pageable.UNPAGED) {
         StringBuilder added = new StringBuilder();
         Sort sort = pageable.getSort();
         if (sort.isSorted()) {
            added.append(queryBuilder.buildOrderBy(persistentEntity, sort).getQuery());
         } else if (this.isSqlServerWithoutOrderBy(this.query, this.dialect)) {
            sort = this.sortById(persistentEntity);
            added.append(queryBuilder.buildOrderBy(persistentEntity, sort).getQuery());
         }

         if (isSingleResult && pageable.getOffset() > 0L) {
            pageable = Pageable.from(pageable.getNumber(), 1);
         }

         added.append(queryBuilder.buildPagination(pageable).getQuery());
         int forUpdateIndex = this.query.lastIndexOf(" FOR UPDATE");
         if (forUpdateIndex == -1) {
            forUpdateIndex = this.query.lastIndexOf(" WITH (UPDLOCK, ROWLOCK)");
         }

         if (forUpdateIndex > -1) {
            this.query = this.query.substring(0, forUpdateIndex) + added + this.query.substring(forUpdateIndex);
         } else {
            this.query = this.query + added;
         }
      }

   }

   @NonNull
   private <K> Sort sortById(RuntimePersistentEntity<K> persistentEntity) {
      RuntimePersistentProperty<K> identity = persistentEntity.getIdentity();
      if (identity == null) {
         throw new DataAccessException("Pagination requires an entity ID on SQL Server");
      } else {
         return Sort.unsorted().order(Sort.Order.asc(identity.getName()));
      }
   }

   private boolean isSqlServerWithoutOrderBy(String query, Dialect dialect) {
      return dialect == Dialect.SQL_SERVER && !query.contains(" ORDER BY ");
   }

   @Override
   public <K, Cnt, PS> void setParameters(
      OpContext<Cnt, PS> context,
      Cnt connection,
      PS stmt,
      RuntimePersistentEntity<K> persistentEntity,
      K entity,
      Map<QueryParameterBinding, Object> previousValues
   ) {
      int index = context.shiftIndex(0);
      Object[] parameterArray = this.preparedQuery.getParameterArray();
      Argument[] parameterArguments = this.preparedQuery.getArguments();

      for(QueryParameterBinding queryParameterBinding : this.preparedQuery.getQueryBindings()) {
         Class<?> parameterConverter = queryParameterBinding.getParameterConverterClass();
         Object value;
         if (queryParameterBinding.getParameterIndex() != -1) {
            value = this.resolveParameterValue(queryParameterBinding, parameterArray);
         } else {
            if (!queryParameterBinding.isAutoPopulated()) {
               throw new IllegalStateException(
                  "Invalid query [" + this.query + "]. Unable to establish parameter value for parameter at position: " + (index + 1)
               );
            }

            String[] propertyPath = queryParameterBinding.getRequiredPropertyPath();
            PersistentPropertyPath pp = persistentEntity.getPropertyPath(propertyPath);
            if (pp == null) {
               throw new IllegalStateException("Cannot find auto populated property: " + String.join(".", propertyPath));
            }

            RuntimePersistentProperty<?> persistentProperty = (RuntimePersistentProperty)pp.getProperty();
            Object previousValue = null;
            QueryParameterBinding previousPopulatedValueParameter = queryParameterBinding.getPreviousPopulatedValueParameter();
            if (previousPopulatedValueParameter != null) {
               if (previousPopulatedValueParameter.getParameterIndex() == -1) {
                  throw new IllegalStateException("Previous value parameter cannot be bind!");
               }

               previousValue = this.resolveParameterValue(previousPopulatedValueParameter, parameterArray);
            }

            value = context.getRuntimeEntityRegistry().autoPopulateRuntimeProperty(persistentProperty, previousValue);
            value = context.convert(connection, value, persistentProperty);
            parameterConverter = null;
         }

         DataType dataType = queryParameterBinding.getDataType();
         List<Object> values = queryParameterBinding.isExpandable() ? this.expandValue(value, dataType) : Collections.singletonList(value);
         if (values != null && values.isEmpty()) {
            value = null;
            values = null;
         }

         if (values == null) {
            if (parameterConverter != null) {
               int parameterIndex = queryParameterBinding.getParameterIndex();
               Argument<?> argument = parameterIndex > -1 ? parameterArguments[parameterIndex] : null;
               value = context.convert(parameterConverter, connection, value, argument);
            }

            context.setStatementParameter(stmt, index++, dataType, value, this.dialect);
         } else {
            for(Object v : values) {
               if (parameterConverter != null) {
                  int parameterIndex = queryParameterBinding.getParameterIndex();
                  Argument<?> argument = parameterIndex > -1 ? parameterArguments[parameterIndex] : null;
                  v = context.convert(parameterConverter, connection, v, argument);
               }

               context.setStatementParameter(stmt, index++, dataType, v, this.dialect);
            }
         }
      }

   }

   private Object resolveParameterValue(QueryParameterBinding queryParameterBinding, Object[] parameterArray) {
      Object value = parameterArray[queryParameterBinding.getParameterIndex()];
      String[] parameterBindingPath = queryParameterBinding.getParameterBindingPath();
      if (parameterBindingPath != null) {
         for(String prop : parameterBindingPath) {
            if (value == null) {
               break;
            }

            value = BeanWrapper.getWrapper(value).getRequiredProperty(prop, Argument.OBJECT_ARGUMENT);
         }
      }

      return value;
   }
}
