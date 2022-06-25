package io.micronaut.data.jdbc.operations;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.jdbc.config.DataJdbcConfiguration;
import io.micronaut.data.jdbc.convert.JdbcConversionContext;
import io.micronaut.data.jdbc.mapper.ColumnIndexResultSetReader;
import io.micronaut.data.jdbc.mapper.ColumnNameResultSetReader;
import io.micronaut.data.jdbc.mapper.JdbcQueryStatement;
import io.micronaut.data.jdbc.mapper.SqlResultConsumer;
import io.micronaut.data.jdbc.runtime.ConnectionCallback;
import io.micronaut.data.jdbc.runtime.PreparedStatementCallback;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.UpdateBatchOperation;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.async.AsyncCapableRepository;
import io.micronaut.data.operations.reactive.ReactiveCapableRepository;
import io.micronaut.data.operations.reactive.ReactiveRepositoryOperations;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.convert.RuntimePersistentPropertyConversionContext;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.data.runtime.mapper.DTOMapper;
import io.micronaut.data.runtime.mapper.ResultConsumer;
import io.micronaut.data.runtime.mapper.ResultReader;
import io.micronaut.data.runtime.mapper.TypeMapper;
import io.micronaut.data.runtime.mapper.sql.SqlDTOMapper;
import io.micronaut.data.runtime.mapper.sql.SqlResultEntityTypeMapper;
import io.micronaut.data.runtime.mapper.sql.SqlTypeMapper;
import io.micronaut.data.runtime.operations.ExecutorAsyncOperations;
import io.micronaut.data.runtime.operations.ExecutorReactiveOperations;
import io.micronaut.data.runtime.operations.internal.AbstractSqlRepositoryOperations;
import io.micronaut.data.runtime.operations.internal.AbstractSyncEntitiesOperations;
import io.micronaut.data.runtime.operations.internal.AbstractSyncEntityOperations;
import io.micronaut.data.runtime.operations.internal.DBOperation;
import io.micronaut.data.runtime.operations.internal.OpContext;
import io.micronaut.data.runtime.operations.internal.OperationContext;
import io.micronaut.data.runtime.operations.internal.StoredQuerySqlOperation;
import io.micronaut.data.runtime.operations.internal.StoredSqlOperation;
import io.micronaut.data.runtime.operations.internal.SyncCascadeOperations;
import io.micronaut.data.runtime.support.AbstractConversionContext;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.transaction.TransactionOperations;
import io.micronaut.transaction.jdbc.DataSourceUtils;
import io.micronaut.transaction.jdbc.DelegatingDataSource;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EachBean(DataSource.class)
@Internal
public final class DefaultJdbcRepositoryOperations
   extends AbstractSqlRepositoryOperations<Connection, ResultSet, PreparedStatement, SQLException>
   implements JdbcRepositoryOperations,
   AsyncCapableRepository,
   ReactiveCapableRepository,
   AutoCloseable,
   SyncCascadeOperations.SyncCascadeOperationsHelper<DefaultJdbcRepositoryOperations.JdbcOperationContext> {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultJdbcRepositoryOperations.class);
   private final TransactionOperations<Connection> transactionOperations;
   private final DataSource dataSource;
   private final DataSource unwrapedDataSource;
   private ExecutorAsyncOperations asyncOperations;
   private ExecutorService executorService;
   private final SyncCascadeOperations<DefaultJdbcRepositoryOperations.JdbcOperationContext> cascadeOperations;
   private final DataJdbcConfiguration jdbcConfiguration;

   @Internal
   protected DefaultJdbcRepositoryOperations(
      @Parameter String dataSourceName,
      @Parameter DataJdbcConfiguration jdbcConfiguration,
      DataSource dataSource,
      @Parameter TransactionOperations<Connection> transactionOperations,
      @Named("io") @Nullable ExecutorService executorService,
      BeanContext beanContext,
      List<MediaTypeCodec> codecs,
      @NonNull DateTimeProvider dateTimeProvider,
      RuntimeEntityRegistry entityRegistry,
      DataConversionService<?> conversionService,
      AttributeConverterRegistry attributeConverterRegistry
   ) {
      super(
         dataSourceName,
         new ColumnNameResultSetReader(conversionService),
         new ColumnIndexResultSetReader(conversionService),
         new JdbcQueryStatement(conversionService),
         codecs,
         dateTimeProvider,
         entityRegistry,
         beanContext,
         conversionService,
         attributeConverterRegistry
      );
      ArgumentUtils.requireNonNull("dataSource", dataSource);
      ArgumentUtils.requireNonNull("transactionOperations", transactionOperations);
      this.dataSource = dataSource;
      this.unwrapedDataSource = DelegatingDataSource.unwrapDataSource(dataSource);
      this.transactionOperations = transactionOperations;
      this.executorService = executorService;
      this.cascadeOperations = new SyncCascadeOperations<>(conversionService, this);
      this.jdbcConfiguration = jdbcConfiguration;
   }

   @NonNull
   private ExecutorService newLocalThreadPool() {
      this.executorService = Executors.newCachedThreadPool();
      return this.executorService;
   }

   public <T> T persistOne(DefaultJdbcRepositoryOperations.JdbcOperationContext ctx, T value, RuntimePersistentEntity<T> persistentEntity) {
      DBOperation childSqlPersistOperation = this.resolveEntityInsert(ctx.annotationMetadata, ctx.repositoryType, value.getClass(), persistentEntity);
      DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> persistOneOp = new DefaultJdbcRepositoryOperations.JdbcEntityOperations<>(
         ctx, childSqlPersistOperation, persistentEntity, value, true
      );
      persistOneOp.persist();
      return persistOneOp.getEntity();
   }

   public <T> List<T> persistBatch(
      DefaultJdbcRepositoryOperations.JdbcOperationContext ctx, Iterable<T> values, RuntimePersistentEntity<T> childPersistentEntity, Predicate<T> predicate
   ) {
      DBOperation childSqlPersistOperation = this.resolveEntityInsert(
         ctx.annotationMetadata, ctx.repositoryType, childPersistentEntity.getIntrospection().getBeanType(), childPersistentEntity
      );
      DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<T> persistBatchOp = new DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<>(
         ctx, childPersistentEntity, values, childSqlPersistOperation, true
      );
      persistBatchOp.veto(predicate);
      persistBatchOp.persist();
      return persistBatchOp.getEntities();
   }

   public <T> T updateOne(DefaultJdbcRepositoryOperations.JdbcOperationContext ctx, T value, RuntimePersistentEntity<T> persistentEntity) {
      DBOperation childSqlUpdateOperation = this.resolveEntityUpdate(ctx.annotationMetadata, ctx.repositoryType, value.getClass(), persistentEntity);
      DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> op = new DefaultJdbcRepositoryOperations.JdbcEntityOperations<>(
         ctx, persistentEntity, value, childSqlUpdateOperation
      );
      op.update();
      return op.getEntity();
   }

   public void persistManyAssociation(
      DefaultJdbcRepositoryOperations.JdbcOperationContext ctx,
      RuntimeAssociation runtimeAssociation,
      Object value,
      RuntimePersistentEntity<Object> persistentEntity,
      Object child,
      RuntimePersistentEntity<Object> childPersistentEntity
   ) {
      DBOperation dbInsertOperation = this.resolveSqlInsertAssociation(ctx.repositoryType, ctx.dialect, runtimeAssociation, persistentEntity, value);

      try {
         new DefaultJdbcRepositoryOperations.JdbcEntityOperations(ctx, childPersistentEntity, child, dbInsertOperation).execute();
      } catch (Exception var9) {
         throw new DataAccessException("SQL error executing INSERT: " + var9.getMessage(), var9);
      }
   }

   public void persistManyAssociationBatch(
      DefaultJdbcRepositoryOperations.JdbcOperationContext ctx,
      RuntimeAssociation runtimeAssociation,
      Object value,
      RuntimePersistentEntity<Object> persistentEntity,
      Iterable<Object> child,
      RuntimePersistentEntity<Object> childPersistentEntity
   ) {
      DBOperation dbInsertOperation = this.resolveSqlInsertAssociation(ctx.repositoryType, ctx.dialect, runtimeAssociation, persistentEntity, value);

      try {
         DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<Object> assocOp = new DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<>(
            ctx, childPersistentEntity, child, dbInsertOperation
         );
         assocOp.veto(ctx.persisted::contains);
         assocOp.execute();
      } catch (Exception var9) {
         throw new DataAccessException("SQL error executing INSERT: " + var9.getMessage(), var9);
      }
   }

   protected ConversionContext createTypeConversionContext(Connection connection, RuntimePersistentProperty<?> property, Argument<?> argument) {
      Objects.requireNonNull(connection);
      if (property != null) {
         return new DefaultJdbcRepositoryOperations.RuntimePersistentPropertyJdbcCC(connection, property);
      } else {
         return (ConversionContext)(argument != null
            ? new DefaultJdbcRepositoryOperations.ArgumentJdbcCC(connection, argument)
            : new DefaultJdbcRepositoryOperations.JdbcConversionContextImpl(connection));
      }
   }

   @NonNull
   public ExecutorAsyncOperations async() {
      ExecutorAsyncOperations asyncOperations = this.asyncOperations;
      if (asyncOperations == null) {
         synchronized(this) {
            asyncOperations = this.asyncOperations;
            if (asyncOperations == null) {
               asyncOperations = new ExecutorAsyncOperations(this, this.executorService != null ? this.executorService : this.newLocalThreadPool());
               this.asyncOperations = asyncOperations;
            }
         }
      }

      return asyncOperations;
   }

   @NonNull
   @Override
   public ReactiveRepositoryOperations reactive() {
      return new ExecutorReactiveOperations(this.async(), this.conversionService);
   }

   @Nullable
   @Override
   public <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.executeRead(
         connection -> {
            RuntimePersistentEntity<T> persistentEntity = this.getEntity(preparedQuery.getRootEntity());
   
            try {
               PreparedStatement ps = this.prepareStatement(connection, connection::prepareStatement, preparedQuery, false, true);
               Throwable var5 = null;
   
               Object var14;
               try {
                  ResultSet rs = ps.executeQuery();
                  Throwable var7 = null;
   
                  try {
                     Class<R> resultType = preparedQuery.getResultType();
                     if (preparedQuery.getResultDataType() != DataType.ENTITY) {
                        if (!rs.next()) {
                           return null;
                        }
   
                        if (preparedQuery.isDtoProjection()) {
                           TypeMapper<ResultSet, R> introspectedDataMapper = new DTOMapper<>(
                              persistentEntity, this.columnNameResultSetReader, this.jsonCodec, this.conversionService
                           );
                           Object joinFetchPaths = introspectedDataMapper.map(rs, resultType);
                           return joinFetchPaths;
                        }
   
                        Object v = this.columnIndexResultSetReader.readDynamic(rs, 1, preparedQuery.getResultDataType());
                        if (v == null) {
                           Object joinFetchPaths = null;
                           return joinFetchPaths;
                        }
   
                        if (resultType.isInstance(v)) {
                           return v;
                        }
   
                        Object joinFetchPaths = this.columnIndexResultSetReader.convertRequired(v, resultType);
                        return joinFetchPaths;
                     }
   
                     RuntimePersistentEntity<R> resultPersistentEntity = this.getEntity(resultType);
                     Set<JoinPath> joinFetchPaths = preparedQuery.getJoinFetchPaths();
                     SqlResultEntityTypeMapper<ResultSet, R> mapper = new SqlResultEntityTypeMapper<>(
                        resultPersistentEntity,
                        this.columnNameResultSetReader,
                        joinFetchPaths,
                        this.jsonCodec,
                        (loadedEntity, o) -> loadedEntity.hasPostLoadEventListeners()
                              ? this.triggerPostLoad(o, loadedEntity, preparedQuery.getAnnotationMetadata())
                              : o,
                        this.conversionService
                     );
                     SqlResultEntityTypeMapper.PushingMapper<ResultSet, R> oneMapper = mapper.readOneWithJoins();
                     if (rs.next()) {
                        oneMapper.processRow(rs);
                     }
   
                     while(!joinFetchPaths.isEmpty() && rs.next()) {
                        oneMapper.processRow(rs);
                     }
   
                     R result = oneMapper.getResult();
                     if (preparedQuery.hasResultConsumer()) {
                        preparedQuery.getParameterInRole("sqlMappingFunction", SqlResultConsumer.class)
                           .ifPresent(consumer -> consumer.accept(result, this.newMappingContext(rs)));
                     }
   
                     var14 = result;
                  } catch (Throwable var54) {
                     var7 = var54;
                     throw var54;
                  } finally {
                     if (rs != null) {
                        if (var7 != null) {
                           try {
                              rs.close();
                           } catch (Throwable var53) {
                              var7.addSuppressed(var53);
                           }
                        } else {
                           rs.close();
                        }
                     }
   
                  }
               } catch (Throwable var56) {
                  var5 = var56;
                  throw var56;
               } finally {
                  if (ps != null) {
                     if (var5 != null) {
                        try {
                           ps.close();
                        } catch (Throwable var52) {
                           var5.addSuppressed(var52);
                        }
                     } else {
                        ps.close();
                     }
                  }
   
               }
   
               return var14;
            } catch (SQLException var58) {
               throw new DataAccessException("Error executing SQL Query: " + var58.getMessage(), var58);
            }
         }
      );
   }

   @Override
   public <T> boolean exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
      return this.executeRead(connection -> {
         try {
            PreparedStatement ps = this.prepareStatement(connection, connection::prepareStatement, preparedQuery, false, true);
            Throwable var4 = null;

            Object var7;
            try {
               ResultSet rs = ps.executeQuery();
               Throwable var6 = null;

               try {
                  var7 = rs.next();
               } catch (Throwable var32) {
                  var7 = var32;
                  var6 = var32;
                  throw var32;
               } finally {
                  if (rs != null) {
                     if (var6 != null) {
                        try {
                           rs.close();
                        } catch (Throwable var31) {
                           var6.addSuppressed(var31);
                        }
                     } else {
                        rs.close();
                     }
                  }

               }
            } catch (Throwable var34) {
               var4 = var34;
               throw var34;
            } finally {
               if (ps != null) {
                  if (var4 != null) {
                     try {
                        ps.close();
                     } catch (Throwable var30) {
                        var4.addSuppressed(var30);
                     }
                  } else {
                     ps.close();
                  }
               }

            }

            return (Boolean)var7;
         } catch (SQLException var36) {
            throw new DataAccessException("Error executing SQL query: " + var36.getMessage(), var36);
         }
      });
   }

   @NonNull
   @Override
   public <T, R> Stream<R> findStream(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.findStream(preparedQuery, this.getConnection());
   }

   private <T, R> Stream<R> findStream(@NonNull PreparedQuery<T, R> preparedQuery, Connection connection) {
      final Class<R> resultType = preparedQuery.getResultType();
      final AtomicBoolean finished = new AtomicBoolean();

      final PreparedStatement ps;
      try {
         ps = this.prepareStatement(connection, connection::prepareStatement, preparedQuery, false, false);
      } catch (Exception var23) {
         throw new DataAccessException("SQL Error preparing Query: " + var23.getMessage(), var23);
      }

      final ResultSet openedRs = null;

      try {
         openedRs = ps.executeQuery();
         ResultSet rs = openedRs;
         boolean dtoProjection = preparedQuery.isDtoProjection();
         boolean isEntity = preparedQuery.getResultDataType() == DataType.ENTITY;
         Spliterator<R> spliterator;
         if (!isEntity && !dtoProjection) {
            spliterator = new AbstractSpliterator<R>(Long.MAX_VALUE, 1040) {
               public boolean tryAdvance(Consumer<? super R> action) {
                  if (finished.get()) {
                     return false;
                  } else {
                     try {
                        boolean hasNext = openedRs.next();
                        if (hasNext) {
                           Object v = DefaultJdbcRepositoryOperations.this.columnIndexResultSetReader
                              .readDynamic(openedRs, 1, preparedQuery.getResultDataType());
                           if (resultType.isInstance(v)) {
                              action.accept(v);
                           } else if (v != null) {
                              Object r = DefaultJdbcRepositoryOperations.this.columnIndexResultSetReader.convertRequired(v, resultType);
                              if (r != null) {
                                 action.accept(r);
                              }
                           }
                        } else {
                           DefaultJdbcRepositoryOperations.this.closeResultSet(ps, openedRs, finished);
                        }

                        return hasNext;
                     } catch (SQLException var5) {
                        throw new DataAccessException("Error retrieving next JDBC result: " + var5.getMessage(), var5);
                     }
                  }
               }
            };
         } else {
            final SqlResultConsumer sqlMappingConsumer = preparedQuery.hasResultConsumer()
               ? (SqlResultConsumer)preparedQuery.getParameterInRole("sqlMappingFunction", SqlResultConsumer.class).orElse(null)
               : null;
            RuntimePersistentEntity<T> persistentEntity = this.getEntity(preparedQuery.getRootEntity());
            final SqlTypeMapper<ResultSet, R> mapper;
            if (dtoProjection) {
               boolean isRawQuery = preparedQuery.getAnnotationMetadata().stringValue(Query.class, "rawQuery").isPresent();
               mapper = new SqlDTOMapper<>(
                  persistentEntity,
                  isRawQuery ? this.getEntity(preparedQuery.getResultType()) : persistentEntity,
                  this.columnNameResultSetReader,
                  this.jsonCodec,
                  this.conversionService
               );
            } else {
               Set<JoinPath> joinFetchPaths = preparedQuery.getJoinFetchPaths();
               SqlResultEntityTypeMapper<ResultSet, R> entityTypeMapper = new SqlResultEntityTypeMapper<>(
                  this.getEntity(resultType),
                  this.columnNameResultSetReader,
                  joinFetchPaths,
                  this.jsonCodec,
                  (loadedEntity, o) -> loadedEntity.hasPostLoadEventListeners()
                        ? this.triggerPostLoad(o, loadedEntity, preparedQuery.getAnnotationMetadata())
                        : o,
                  this.conversionService
               );
               boolean onlySingleEndedJoins = this.isOnlySingleEndedJoins(persistentEntity, joinFetchPaths);
               if (!onlySingleEndedJoins) {
                  Stream var18;
                  try {
                     SqlResultEntityTypeMapper.PushingMapper<ResultSet, List<R>> manyMapper = entityTypeMapper.readAllWithJoins();

                     while(rs.next()) {
                        manyMapper.processRow(rs);
                     }

                     var18 = ((List)manyMapper.getResult()).stream();
                  } finally {
                     this.closeResultSet(ps, rs, finished);
                  }

                  return var18;
               }

               mapper = entityTypeMapper;
            }

            spliterator = new AbstractSpliterator<R>(Long.MAX_VALUE, 1040) {
               public boolean tryAdvance(Consumer<? super R> action) {
                  if (finished.get()) {
                     return false;
                  } else {
                     boolean hasNext = mapper.hasNext(openedRs);
                     if (hasNext) {
                        R o = mapper.map(openedRs, resultType);
                        if (sqlMappingConsumer != null) {
                           sqlMappingConsumer.accept(openedRs, o);
                        }

                        action.accept(o);
                     } else {
                        DefaultJdbcRepositoryOperations.this.closeResultSet(ps, openedRs, finished);
                     }

                     return hasNext;
                  }
               }
            };
         }

         return (Stream<R>)StreamSupport.stream(spliterator, false).onClose(() -> this.closeResultSet(ps, openedRs, finished));
      } catch (Exception var25) {
         this.closeResultSet(ps, openedRs, finished);
         throw new DataAccessException("SQL Error executing Query: " + var25.getMessage(), var25);
      }
   }

   private void closeResultSet(PreparedStatement ps, ResultSet rs, AtomicBoolean finished) {
      if (finished.compareAndSet(false, true)) {
         try {
            if (rs != null) {
               rs.close();
            }

            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var5) {
            throw new DataAccessException("Error closing JDBC result stream: " + var5.getMessage(), var5);
         }
      }

   }

   @NonNull
   @Override
   public <T, R> Iterable<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.executeRead(connection -> {
         Stream<R> stream = this.findStream(preparedQuery, connection);
         Throwable var4 = null;

         List var5;
         try {
            var5 = (List)stream.collect(Collectors.toList());
         } catch (Throwable var14) {
            var4 = var14;
            throw var14;
         } finally {
            if (stream != null) {
               if (var4 != null) {
                  try {
                     stream.close();
                  } catch (Throwable var13) {
                     var4.addSuppressed(var13);
                  }
               } else {
                  stream.close();
               }
            }

         }

         return var5;
      });
   }

   @NonNull
   @Override
   public Optional<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.executeWrite(connection -> {
         try {
            PreparedStatement ps = this.prepareStatement(connection, connection::prepareStatement, preparedQuery, true, false);
            Throwable var4 = null;

            Optional var6;
            try {
               int result = ps.executeUpdate();
               if (QUERY_LOG.isTraceEnabled()) {
                  QUERY_LOG.trace("Update operation updated {} records", result);
               }

               if (preparedQuery.isOptimisticLock()) {
                  this.checkOptimisticLocking(1, Integer.valueOf(result));
               }

               var6 = Optional.of(result);
            } catch (Throwable var16) {
               var4 = var16;
               throw var16;
            } finally {
               if (ps != null) {
                  if (var4 != null) {
                     try {
                        ps.close();
                     } catch (Throwable var15) {
                        var4.addSuppressed(var15);
                     }
                  } else {
                     ps.close();
                  }
               }

            }

            return var6;
         } catch (SQLException var18) {
            throw new DataAccessException("Error executing SQL UPDATE: " + var18.getMessage(), var18);
         }
      });
   }

   private Integer sum(Stream<Integer> stream) {
      return stream.mapToInt(i -> i).sum();
   }

   @Override
   public <T> Optional<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation) {
      return Optional.ofNullable(
         this.executeWrite(
            connection -> {
               SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(operation.getRepositoryType(), DEFAULT_SQL_BUILDER);
               DefaultJdbcRepositoryOperations.JdbcOperationContext ctx = new DefaultJdbcRepositoryOperations.JdbcOperationContext(
                  operation.getAnnotationMetadata(), operation.getRepositoryType(), queryBuilder.dialect(), connection
               );
               Dialect dialect = queryBuilder.dialect();
               RuntimePersistentEntity<T> persistentEntity = this.getEntity(operation.getRootEntity());
               if (this.isSupportsBatchDelete(persistentEntity, dialect)) {
                  DBOperation dbOperation = new StoredQuerySqlOperation(queryBuilder, operation.getStoredQuery());
                  DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<T> op = new DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<>(
                     ctx, this.getEntity(operation.getRootEntity()), operation, dbOperation
                  );
                  op.delete();
                  return op.rowsUpdated;
               } else {
                  return this.sum(
                     operation.split()
                        .stream()
                        .map(
                           deleteOp -> {
                              DBOperation dbOperationx = new StoredQuerySqlOperation(queryBuilder, operation.getStoredQuery());
                              DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> opx = new DefaultJdbcRepositoryOperations.JdbcEntityOperations(
                                 ctx, this.getEntity(deleteOp.getRootEntity()), deleteOp.getEntity(), dbOperationx
                              );
                              opx.delete();
                              return opx.rowsUpdated;
                           }
                        )
                  );
               }
            }
         )
      );
   }

   @Override
   public <T> int delete(@NonNull DeleteOperation<T> operation) {
      SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(operation.getRepositoryType(), DEFAULT_SQL_BUILDER);
      return this.executeWrite(
            connection -> {
               DefaultJdbcRepositoryOperations.JdbcOperationContext ctx = new DefaultJdbcRepositoryOperations.JdbcOperationContext(
                  operation.getAnnotationMetadata(), operation.getRepositoryType(), queryBuilder.dialect(), connection
               );
               DBOperation dbOperation = new StoredQuerySqlOperation(queryBuilder, operation.getStoredQuery());
               DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> op = new DefaultJdbcRepositoryOperations.JdbcEntityOperations<>(
                  ctx, this.getEntity(operation.getRootEntity()), operation.getEntity(), dbOperation
               );
               op.delete();
               return op;
            }
         )
         .rowsUpdated;
   }

   @NonNull
   @Override
   public <T> T update(@NonNull UpdateOperation<T> operation) {
      AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
      Class<?> repositoryType = operation.getRepositoryType();
      SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(repositoryType, DEFAULT_SQL_BUILDER);
      DBOperation dbOperation = new StoredQuerySqlOperation(queryBuilder, operation.getStoredQuery());
      return this.executeWrite(
         connection -> {
            DefaultJdbcRepositoryOperations.JdbcOperationContext ctx = new DefaultJdbcRepositoryOperations.JdbcOperationContext(
               annotationMetadata, repositoryType, queryBuilder.dialect(), connection
            );
            DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> op = new DefaultJdbcRepositoryOperations.JdbcEntityOperations<>(
               ctx, this.getEntity(operation.getRootEntity()), operation.getEntity(), dbOperation
            );
            op.update();
            return op.getEntity();
         }
      );
   }

   @NonNull
   @Override
   public <T> Iterable<T> updateAll(@NonNull UpdateBatchOperation<T> operation) {
      return this.executeWrite(
         connection -> {
            AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
            Class<?> repositoryType = operation.getRepositoryType();
            SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(repositoryType, DEFAULT_SQL_BUILDER);
            RuntimePersistentEntity<T> persistentEntity = this.getEntity(operation.getRootEntity());
            DBOperation dbOperation = new StoredQuerySqlOperation(queryBuilder, operation.getStoredQuery());
            DefaultJdbcRepositoryOperations.JdbcOperationContext ctx = new DefaultJdbcRepositoryOperations.JdbcOperationContext(
               annotationMetadata, repositoryType, queryBuilder.dialect(), connection
            );
            if (!this.isSupportsBatchUpdate(persistentEntity, queryBuilder.dialect())) {
               return (List)operation.split()
                  .stream()
                  .map(
                     updateOp -> {
                        DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> opx = new DefaultJdbcRepositoryOperations.JdbcEntityOperations(
                           ctx, persistentEntity, updateOp.getEntity(), dbOperation
                        );
                        opx.update();
                        return opx.getEntity();
                     }
                  )
                  .collect(Collectors.toList());
            } else {
               DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<T> op = new DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<>(
                  ctx, persistentEntity, operation, dbOperation
               );
               op.update();
               return op.getEntities();
            }
         }
      );
   }

   @NonNull
   @Override
   public <T> T persist(@NonNull InsertOperation<T> operation) {
      AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
      Class<?> repositoryType = operation.getRepositoryType();
      SqlQueryBuilder queryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(repositoryType, DEFAULT_SQL_BUILDER);
      return this.<DefaultJdbcRepositoryOperations.JdbcEntityOperations<T>>executeWrite(
            connection -> {
               DefaultJdbcRepositoryOperations.JdbcOperationContext ctx = new DefaultJdbcRepositoryOperations.JdbcOperationContext(
                  annotationMetadata, repositoryType, queryBuilder.dialect(), connection
               );
               DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> op = new DefaultJdbcRepositoryOperations.JdbcEntityOperations<>(
                  ctx,
                  new StoredQuerySqlOperation(queryBuilder, operation.getStoredQuery()),
                  this.getEntity(operation.getRootEntity()),
                  operation.getEntity(),
                  true
               );
               op.persist();
               return op;
            }
         )
         .getEntity();
   }

   @Nullable
   @Override
   public <T> T findOne(@NonNull Class<T> type, @NonNull Serializable id) {
      throw new UnsupportedOperationException("The findOne method by ID is not supported. Execute the SQL query directly");
   }

   @NonNull
   @Override
   public <T> Iterable<T> findAll(@NonNull PagedQuery<T> query) {
      throw new UnsupportedOperationException("The findAll method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
   }

   @Override
   public <T> long count(PagedQuery<T> pagedQuery) {
      throw new UnsupportedOperationException("The count method without an explicit query is not supported. Use findAll(PreparedQuery) instead");
   }

   @NonNull
   @Override
   public <T> Stream<T> findStream(@NonNull PagedQuery<T> query) {
      throw new UnsupportedOperationException("The findStream method without an explicit query is not supported. Use findStream(PreparedQuery) instead");
   }

   @Override
   public <R> Page<R> findPage(@NonNull PagedQuery<R> query) {
      throw new UnsupportedOperationException("The findPage method without an explicit query is not supported. Use findPage(PreparedQuery) instead");
   }

   @NonNull
   @Override
   public <T> Iterable<T> persistAll(@NonNull InsertBatchOperation<T> operation) {
      return this.executeWrite(
         connection -> {
            AnnotationMetadata annotationMetadata = operation.getAnnotationMetadata();
            Class<?> repositoryType = operation.getRepositoryType();
            SqlQueryBuilder sqlQueryBuilder = (SqlQueryBuilder)this.queryBuilders.getOrDefault(repositoryType, DEFAULT_SQL_BUILDER);
            DBOperation dbOperation = new StoredQuerySqlOperation(sqlQueryBuilder, operation.getStoredQuery());
            RuntimePersistentEntity<T> persistentEntity = this.getEntity(operation.getRootEntity());
            DefaultJdbcRepositoryOperations.JdbcOperationContext ctx = new DefaultJdbcRepositoryOperations.JdbcOperationContext(
               annotationMetadata, repositoryType, sqlQueryBuilder.dialect(), connection
            );
            if (!this.isSupportsBatchInsert(persistentEntity, sqlQueryBuilder.dialect())) {
               return (List)operation.split()
                  .stream()
                  .map(
                     persistOp -> {
                        DefaultJdbcRepositoryOperations.JdbcEntityOperations<T> opx = new DefaultJdbcRepositoryOperations.JdbcEntityOperations(
                           ctx, dbOperation, persistentEntity, persistOp.getEntity(), true
                        );
                        opx.persist();
                        return opx.getEntity();
                     }
                  )
                  .collect(Collectors.toList());
            } else {
               DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<T> op = new DefaultJdbcRepositoryOperations.JdbcEntitiesOperations<>(
                  ctx, persistentEntity, operation, dbOperation, true
               );
               op.persist();
               return op.getEntities();
            }
         }
      );
   }

   private <I> I executeRead(Function<Connection, I> fn) {
      if (this.jdbcConfiguration.isTransactionPerOperation()) {
         return this.transactionOperations.executeRead(status -> fn.apply(status.getConnection()));
      } else if (this.jdbcConfiguration.isAllowConnectionPerOperation() && !this.transactionOperations.hasConnection()) {
         try {
            Connection connection = this.unwrapedDataSource.getConnection();
            Throwable var3 = null;

            Object var4;
            try {
               var4 = fn.apply(connection);
            } catch (Throwable var14) {
               var3 = var14;
               throw var14;
            } finally {
               if (connection != null) {
                  if (var3 != null) {
                     try {
                        connection.close();
                     } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                     }
                  } else {
                     connection.close();
                  }
               }

            }

            return (I)var4;
         } catch (SQLException var16) {
            throw new DataAccessException("Cannot get connection: " + var16.getMessage(), var16);
         }
      } else {
         return (I)fn.apply(this.transactionOperations.getConnection());
      }
   }

   private <I> I executeWrite(Function<Connection, I> fn) {
      if (this.jdbcConfiguration.isTransactionPerOperation()) {
         return this.transactionOperations.executeWrite(status -> fn.apply(status.getConnection()));
      } else if (this.jdbcConfiguration.isAllowConnectionPerOperation() && !this.transactionOperations.hasConnection()) {
         try {
            Connection connection = this.unwrapedDataSource.getConnection();
            Throwable var3 = null;

            Object var4;
            try {
               var4 = fn.apply(connection);
            } catch (Throwable var14) {
               var3 = var14;
               throw var14;
            } finally {
               if (connection != null) {
                  if (var3 != null) {
                     try {
                        connection.close();
                     } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                     }
                  } else {
                     connection.close();
                  }
               }

            }

            return (I)var4;
         } catch (SQLException var16) {
            throw new DataAccessException("Cannot get connection: " + var16.getMessage(), var16);
         }
      } else {
         return (I)fn.apply(this.transactionOperations.getConnection());
      }
   }

   @PreDestroy
   public void close() {
      if (this.executorService != null) {
         this.executorService.shutdown();
      }

   }

   @NonNull
   @Override
   public DataSource getDataSource() {
      return this.dataSource;
   }

   @NonNull
   @Override
   public Connection getConnection() {
      return !this.jdbcConfiguration.isTransactionPerOperation()
            && this.jdbcConfiguration.isAllowConnectionPerOperation()
            && !this.transactionOperations.hasConnection()
         ? DataSourceUtils.getConnection(this.dataSource, true)
         : (Connection)this.transactionOperations.getConnection();
   }

   @NonNull
   @Override
   public <R> R execute(@NonNull ConnectionCallback<R> callback) {
      try {
         return callback.call(this.getConnection());
      } catch (SQLException var3) {
         throw new DataAccessException("Error executing SQL Callback: " + var3.getMessage(), var3);
      }
   }

   @NonNull
   @Override
   public <R> R prepareStatement(@NonNull String sql, @NonNull PreparedStatementCallback<R> callback) {
      ArgumentUtils.requireNonNull("sql", sql);
      ArgumentUtils.requireNonNull("callback", callback);
      if (QUERY_LOG.isDebugEnabled()) {
         QUERY_LOG.debug("Executing Query: {}", sql);
      }

      try {
         R result = null;
         PreparedStatement ps = this.getConnection().prepareStatement(sql);

         Object var5;
         try {
            result = callback.call(ps);
            var5 = result;
         } finally {
            if (!(result instanceof AutoCloseable)) {
               ps.close();
            }

         }

         return (R)var5;
      } catch (SQLException var10) {
         throw new DataAccessException("Error preparing SQL statement: " + var10.getMessage(), var10);
      }
   }

   @NonNull
   @Override
   public <T> Stream<T> entityStream(@NonNull ResultSet resultSet, @NonNull Class<T> rootEntity) {
      return this.entityStream(resultSet, null, rootEntity);
   }

   @NonNull
   @Override
   public <E> E readEntity(@NonNull String prefix, @NonNull ResultSet resultSet, @NonNull Class<E> type) throws DataAccessException {
      return (E)new SqlResultEntityTypeMapper(prefix, this.getEntity(type), this.columnNameResultSetReader, this.jsonCodec, this.conversionService)
         .map(resultSet, type);
   }

   @NonNull
   @Override
   public <E, D> D readDTO(@NonNull String prefix, @NonNull ResultSet resultSet, @NonNull Class<E> rootEntity, @NonNull Class<D> dtoType) throws DataAccessException {
      return (D)new DTOMapper(this.getEntity(rootEntity), this.columnNameResultSetReader, this.jsonCodec, this.conversionService).map((D)resultSet, dtoType);
   }

   @NonNull
   @Override
   public <T> Stream<T> entityStream(@NonNull ResultSet resultSet, @Nullable String prefix, @NonNull Class<T> rootEntity) {
      ArgumentUtils.requireNonNull("resultSet", (T)resultSet);
      ArgumentUtils.requireNonNull("rootEntity", (T)rootEntity);
      TypeMapper<ResultSet, T> mapper = new SqlResultEntityTypeMapper<>(
         prefix, this.getEntity(rootEntity), this.columnNameResultSetReader, this.jsonCodec, this.conversionService
      );
      Iterable<T> iterable = () -> new Iterator<T>() {
            boolean fetched = false;
            boolean end = false;

            public boolean hasNext() {
               if (this.fetched) {
                  return true;
               } else if (this.end) {
                  return false;
               } else {
                  try {
                     if (resultSet.next()) {
                        this.fetched = true;
                     } else {
                        this.end = true;
                     }
                  } catch (SQLException var2) {
                     throw new DataAccessException("Error retrieving next JDBC result: " + var2.getMessage(), var2);
                  }

                  return !this.end;
               }
            }

            public T next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  this.fetched = false;
                  return (T)mapper.map(resultSet, rootEntity);
               }
            }
         };
      return StreamSupport.stream(iterable.spliterator(), false);
   }

   @NonNull
   private ResultConsumer.Context<ResultSet> newMappingContext(ResultSet rs) {
      return new ResultConsumer.Context<ResultSet>() {
         public ResultSet getResultSet() {
            return rs;
         }

         @Override
         public ResultReader<ResultSet, String> getResultReader() {
            return DefaultJdbcRepositoryOperations.this.columnNameResultSetReader;
         }

         @NonNull
         @Override
         public <E> E readEntity(String prefix, Class<E> type) throws DataAccessException {
            RuntimePersistentEntity<E> entity = DefaultJdbcRepositoryOperations.this.getEntity(type);
            SqlResultEntityTypeMapper<ResultSet, E> mapper = new SqlResultEntityTypeMapper<>(
               prefix,
               entity,
               DefaultJdbcRepositoryOperations.this.columnNameResultSetReader,
               DefaultJdbcRepositoryOperations.this.jsonCodec,
               DefaultJdbcRepositoryOperations.this.conversionService
            );
            return mapper.map(rs, type);
         }

         @NonNull
         @Override
         public <E, D> D readDTO(@NonNull String prefix, @NonNull Class<E> rootEntity, @NonNull Class<D> dtoType) throws DataAccessException {
            RuntimePersistentEntity<E> entity = DefaultJdbcRepositoryOperations.this.getEntity(rootEntity);
            TypeMapper<ResultSet, D> introspectedDataMapper = new DTOMapper<>(
               entity,
               DefaultJdbcRepositoryOperations.this.columnNameResultSetReader,
               DefaultJdbcRepositoryOperations.this.jsonCodec,
               DefaultJdbcRepositoryOperations.this.conversionService
            );
            return introspectedDataMapper.map((D)rs, dtoType);
         }
      };
   }

   public boolean isSupportsBatchInsert(DefaultJdbcRepositoryOperations.JdbcOperationContext jdbcOperationContext, RuntimePersistentEntity<?> persistentEntity) {
      return this.isSupportsBatchInsert(persistentEntity, jdbcOperationContext.dialect);
   }

   private static final class ArgumentJdbcCC extends DefaultJdbcRepositoryOperations.JdbcConversionContextImpl implements ArgumentConversionContext<Object> {
      private final Argument argument;

      public ArgumentJdbcCC(Connection connection, Argument argument) {
         super(ConversionContext.of(argument), connection);
         this.argument = argument;
      }

      @Override
      public Argument<Object> getArgument() {
         return this.argument;
      }
   }

   private static class JdbcConversionContextImpl extends AbstractConversionContext implements JdbcConversionContext {
      private final Connection connection;

      public JdbcConversionContextImpl(Connection connection) {
         this(ConversionContext.DEFAULT, connection);
      }

      public JdbcConversionContextImpl(ConversionContext conversionContext, Connection connection) {
         super(conversionContext);
         this.connection = connection;
      }

      @Override
      public Connection getConnection() {
         return this.connection;
      }
   }

   private final class JdbcEntitiesOperations<T> extends AbstractSyncEntitiesOperations<DefaultJdbcRepositoryOperations.JdbcOperationContext, T, SQLException> {
      private final DBOperation dbOperation;
      private int rowsUpdated;

      private JdbcEntitiesOperations(
         DefaultJdbcRepositoryOperations.JdbcOperationContext ctx, RuntimePersistentEntity<T> persistentEntity, Iterable<T> entities, DBOperation dbOperation
      ) {
         this(ctx, persistentEntity, entities, dbOperation, false);
      }

      private JdbcEntitiesOperations(
         DefaultJdbcRepositoryOperations.JdbcOperationContext ctx,
         RuntimePersistentEntity<T> persistentEntity,
         Iterable<T> entities,
         DBOperation dbOperation,
         boolean insert
      ) {
         super(
            ctx,
            DefaultJdbcRepositoryOperations.this.cascadeOperations,
            DefaultJdbcRepositoryOperations.this.conversionService,
            DefaultJdbcRepositoryOperations.this.entityEventRegistry,
            persistentEntity,
            entities,
            insert
         );
         this.dbOperation = dbOperation;
      }

      @Override
      protected void collectAutoPopulatedPreviousValues() {
         for(AbstractSyncEntitiesOperations<DefaultJdbcRepositoryOperations.JdbcOperationContext, T, SQLException>.Data d : this.entities) {
            if (!d.vetoed) {
               d.previousValues = this.dbOperation.collectAutoPopulatedPreviousValues(this.persistentEntity, d.entity);
            }
         }

      }

      private PreparedStatement prepare(Connection connection) throws SQLException {
         if (!this.insert) {
            return connection.prepareStatement(this.dbOperation.getQuery());
         } else {
            Dialect dialect = this.dbOperation.getDialect();
            return !this.hasGeneratedId || dialect != Dialect.ORACLE && dialect != Dialect.SQL_SERVER
               ? connection.prepareStatement(this.dbOperation.getQuery(), this.hasGeneratedId ? 1 : 2)
               : connection.prepareStatement(this.dbOperation.getQuery(), new String[]{this.persistentEntity.getIdentity().getPersistedName()});
         }
      }

      private void setParameters(OpContext<Connection, PreparedStatement> context, Connection connection, PreparedStatement stmt, DBOperation sqlOperation) throws SQLException {
         for(AbstractSyncEntitiesOperations<DefaultJdbcRepositoryOperations.JdbcOperationContext, T, SQLException>.Data d : this.entities) {
            if (!d.vetoed) {
               sqlOperation.setParameters(context, connection, stmt, this.persistentEntity, d.entity, d.previousValues);
               stmt.addBatch();
            }
         }

      }

      @Override
      protected void execute() throws SQLException {
         if (DefaultJdbcRepositoryOperations.QUERY_LOG.isDebugEnabled()) {
            DefaultJdbcRepositoryOperations.QUERY_LOG.debug("Executing SQL query: {}", this.dbOperation.getQuery());
         }

         PreparedStatement ps = this.prepare(this.ctx.connection);
         Throwable var2 = null;

         try {
            this.setParameters(DefaultJdbcRepositoryOperations.this, this.ctx.connection, ps, this.dbOperation);
            this.rowsUpdated = Arrays.stream(ps.executeBatch()).sum();
            if (this.hasGeneratedId) {
               RuntimePersistentProperty<T> identity = this.persistentEntity.getIdentity();
               List<Object> ids = new ArrayList();
               ResultSet generatedKeys = ps.getGeneratedKeys();
               Throwable var6 = null;

               try {
                  while(generatedKeys.next()) {
                     ids.add(DefaultJdbcRepositoryOperations.this.columnIndexResultSetReader.readDynamic(generatedKeys, 1, identity.getDataType()));
                  }
               } catch (Throwable var29) {
                  var6 = var29;
                  throw var29;
               } finally {
                  if (generatedKeys != null) {
                     if (var6 != null) {
                        try {
                           generatedKeys.close();
                        } catch (Throwable var28) {
                           var6.addSuppressed(var28);
                        }
                     } else {
                        generatedKeys.close();
                     }
                  }

               }

               Iterator<Object> iterator = ids.iterator();

               for(AbstractSyncEntitiesOperations<DefaultJdbcRepositoryOperations.JdbcOperationContext, T, SQLException>.Data d : this.entities) {
                  if (!d.vetoed) {
                     if (!iterator.hasNext()) {
                        throw new DataAccessException("Failed to generate ID for entity: " + d.entity);
                     }

                     Object id = iterator.next();
                     d.entity = this.updateEntityId(identity.getProperty(), d.entity, id);
                  }
               }
            }

            if (this.dbOperation.isOptimisticLock()) {
               int expected = (int)this.entities.stream().filter(dx -> !dx.vetoed).count();
               this.checkOptimisticLocking((long)expected, (long)this.rowsUpdated);
            }
         } catch (Throwable var31) {
            var2 = var31;
            throw var31;
         } finally {
            if (ps != null) {
               if (var2 != null) {
                  try {
                     ps.close();
                  } catch (Throwable var27) {
                     var2.addSuppressed(var27);
                  }
               } else {
                  ps.close();
               }
            }

         }

      }
   }

   private final class JdbcEntityOperations<T> extends AbstractSyncEntityOperations<DefaultJdbcRepositoryOperations.JdbcOperationContext, T, SQLException> {
      private final DBOperation dbOperation;
      private Integer rowsUpdated;
      private Map<QueryParameterBinding, Object> previousValues;

      private JdbcEntityOperations(
         DefaultJdbcRepositoryOperations.JdbcOperationContext ctx, RuntimePersistentEntity<T> persistentEntity, T entity, DBOperation dbOperation
      ) {
         this(ctx, dbOperation, persistentEntity, entity, false);
      }

      private JdbcEntityOperations(
         DefaultJdbcRepositoryOperations.JdbcOperationContext ctx,
         DBOperation dbOperation,
         RuntimePersistentEntity<T> persistentEntity,
         T entity,
         boolean insert
      ) {
         super(
            ctx,
            DefaultJdbcRepositoryOperations.this.cascadeOperations,
            DefaultJdbcRepositoryOperations.this.entityEventRegistry,
            persistentEntity,
            DefaultJdbcRepositoryOperations.this.conversionService,
            entity,
            insert
         );
         this.dbOperation = dbOperation;
      }

      @Override
      protected void collectAutoPopulatedPreviousValues() {
         this.previousValues = this.dbOperation.collectAutoPopulatedPreviousValues(this.persistentEntity, this.entity);
      }

      private PreparedStatement prepare(Connection connection, DBOperation dbOperation) throws SQLException {
         if (StoredSqlOperation.class.isInstance(dbOperation)) {
            ((StoredSqlOperation)dbOperation).checkForParameterToBeExpanded(this.persistentEntity, this.entity);
         }

         if (!this.insert) {
            return connection.prepareStatement(dbOperation.getQuery());
         } else {
            StoredSqlOperation sqlOperation = (StoredSqlOperation)dbOperation;
            Dialect dialect = sqlOperation.getDialect();
            return !this.hasGeneratedId || dialect != Dialect.ORACLE && dialect != Dialect.SQL_SERVER
               ? connection.prepareStatement(dbOperation.getQuery(), this.hasGeneratedId ? 1 : 2)
               : connection.prepareStatement(dbOperation.getQuery(), new String[]{this.persistentEntity.getIdentity().getPersistedName()});
         }
      }

      @Override
      protected void execute() throws SQLException {
         if (DefaultJdbcRepositoryOperations.QUERY_LOG.isDebugEnabled()) {
            DefaultJdbcRepositoryOperations.QUERY_LOG.debug("Executing SQL query: {}", this.dbOperation.getQuery());
         }

         PreparedStatement ps = this.prepare(this.ctx.connection, this.dbOperation);
         Throwable var2 = null;

         try {
            this.dbOperation
               .setParameters(DefaultJdbcRepositoryOperations.this, this.ctx.connection, ps, this.persistentEntity, this.entity, this.previousValues);
            this.rowsUpdated = ps.executeUpdate();
            if (this.hasGeneratedId) {
               ResultSet generatedKeys = ps.getGeneratedKeys();
               Throwable var4 = null;

               try {
                  if (!generatedKeys.next()) {
                     throw new DataAccessException("Failed to generate ID for entity: " + this.entity);
                  }

                  RuntimePersistentProperty<T> identity = this.persistentEntity.getIdentity();
                  Object id = DefaultJdbcRepositoryOperations.this.columnIndexResultSetReader.readDynamic(generatedKeys, 1, identity.getDataType());
                  BeanProperty<T, Object> property = identity.getProperty();
                  this.entity = this.updateEntityId(property, this.entity, id);
               } catch (Throwable var29) {
                  var4 = var29;
                  throw var29;
               } finally {
                  if (generatedKeys != null) {
                     if (var4 != null) {
                        try {
                           generatedKeys.close();
                        } catch (Throwable var28) {
                           var4.addSuppressed(var28);
                        }
                     } else {
                        generatedKeys.close();
                     }
                  }

               }
            }

            if (this.dbOperation.isOptimisticLock()) {
               this.checkOptimisticLocking(1L, (long)this.rowsUpdated.intValue());
            }
         } catch (Throwable var31) {
            var2 = var31;
            throw var31;
         } finally {
            if (ps != null) {
               if (var2 != null) {
                  try {
                     ps.close();
                  } catch (Throwable var27) {
                     var2.addSuppressed(var27);
                  }
               } else {
                  ps.close();
               }
            }

         }

      }
   }

   protected static class JdbcOperationContext extends OperationContext {
      public final Connection connection;
      public final Dialect dialect;

      public JdbcOperationContext(AnnotationMetadata annotationMetadata, Class<?> repositoryType, Dialect dialect, Connection connection) {
         super(annotationMetadata, repositoryType);
         this.dialect = dialect;
         this.connection = connection;
      }
   }

   private static final class RuntimePersistentPropertyJdbcCC
      extends DefaultJdbcRepositoryOperations.JdbcConversionContextImpl
      implements RuntimePersistentPropertyConversionContext {
      private final RuntimePersistentProperty<?> property;

      public RuntimePersistentPropertyJdbcCC(Connection connection, RuntimePersistentProperty<?> property) {
         super(ConversionContext.of(property.getArgument()), connection);
         this.property = property;
      }

      @Override
      public RuntimePersistentProperty<?> getRuntimePersistentProperty() {
         return this.property;
      }
   }
}
