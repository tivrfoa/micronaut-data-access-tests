package io.micronaut.data.runtime.intercept.criteria;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaDelete;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaQuery;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaUpdate;
import io.micronaut.data.model.jpa.criteria.impl.QueryResultPersistentEntityCriteriaQuery;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryParameterBinding;
import io.micronaut.data.model.query.builder.QueryResult;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.StoredQuery;
import io.micronaut.data.operations.HintsCapableRepository;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.repository.jpa.criteria.DeleteSpecification;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.data.repository.jpa.criteria.UpdateSpecification;
import io.micronaut.data.runtime.criteria.RuntimeCriteriaBuilder;
import io.micronaut.data.runtime.intercept.AbstractQueryInterceptor;
import io.micronaut.data.runtime.query.DefaultStoredQueryResolver;
import io.micronaut.data.runtime.query.StoredQueryResolver;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSpecificationInterceptor<T, R> extends AbstractQueryInterceptor<T, R> {
   private final Map<RepositoryMethodKey, QueryBuilder> sqlQueryBuilderForRepositories = new ConcurrentHashMap();
   private final RuntimeCriteriaBuilder criteriaBuilder;
   private final StoredQueryResolver storedQueryResolver;

   protected AbstractSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
      RuntimeEntityRegistry runtimeEntityRegistry = operations.getApplicationContext().getBean(RuntimeEntityRegistry.class);
      this.criteriaBuilder = new RuntimeCriteriaBuilder(runtimeEntityRegistry);
      this.storedQueryResolver = (StoredQueryResolver)(operations instanceof StoredQueryResolver
         ? (StoredQueryResolver)operations
         : new DefaultStoredQueryResolver() {
            @Override
            protected HintsCapableRepository getHintsCapableRepository() {
               return operations;
            }
         });
   }

   protected final <E, QR> PreparedQuery<E, QR> preparedQueryForCriteria(
      RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context, AbstractSpecificationInterceptor.Type type
   ) {
      Class<Object> rootEntity = this.getRequiredRootEntity(context);
      Pageable pageable = Pageable.UNPAGED;

      for(Object param : context.getParameterValues()) {
         if (param instanceof Pageable) {
            pageable = (Pageable)param;
            break;
         }
      }

      QueryBuilder sqlQueryBuilder = (QueryBuilder)this.sqlQueryBuilderForRepositories
         .computeIfAbsent(
            methodKey,
            repositoryMethodKey -> {
               Class<QueryBuilder> builder = (Class)context.getAnnotationMetadata()
                  .classValue(RepositoryConfiguration.class, "queryBuilder")
                  .orElseThrow(() -> new IllegalStateException("Cannot determine QueryBuilder"));
               BeanIntrospection<QueryBuilder> introspection = BeanIntrospection.getIntrospection(builder);
               return introspection.getConstructorArguments().length == 1 && introspection.getConstructorArguments()[0].getType() == AnnotationMetadata.class
                  ? introspection.instantiate(context.getAnnotationMetadata())
                  : introspection.instantiate();
            }
         );
      QueryResult queryResult;
      if (type == AbstractSpecificationInterceptor.Type.COUNT
         || type == AbstractSpecificationInterceptor.Type.FIND_ALL
         || type == AbstractSpecificationInterceptor.Type.FIND_ONE
         || type == AbstractSpecificationInterceptor.Type.FIND_PAGE) {
         QuerySpecification<Object> specification = this.getQuerySpecification(context);
         PersistentEntityCriteriaQuery<Object> criteriaQuery = this.criteriaBuilder.createQuery();
         Root<Object> root = criteriaQuery.from(rootEntity);
         if (specification != null) {
            Predicate predicate = specification.toPredicate(root, criteriaQuery, this.criteriaBuilder);
            if (predicate != null) {
               criteriaQuery.where((Expression<Boolean>)predicate);
            }
         }

         if (type == AbstractSpecificationInterceptor.Type.FIND_ALL) {
            for(Object param : context.getParameterValues()) {
               if (param instanceof Sort && param != pageable) {
                  Sort sort = (Sort)param;
                  if (sort.isSorted()) {
                     criteriaQuery.orderBy(this.getOrders(sort, root, this.criteriaBuilder));
                     break;
                  }
               }
            }
         } else if (type == AbstractSpecificationInterceptor.Type.COUNT) {
            criteriaQuery.select(this.criteriaBuilder.count(root));
         }

         queryResult = ((QueryResultPersistentEntityCriteriaQuery)criteriaQuery).buildQuery(sqlQueryBuilder);
      } else if (type == AbstractSpecificationInterceptor.Type.DELETE_ALL) {
         DeleteSpecification<Object> specification = this.getDeleteSpecification(context);
         PersistentEntityCriteriaDelete<Object> criteriaDelete = this.criteriaBuilder.createCriteriaDelete(rootEntity);
         Root<Object> root = criteriaDelete.from(rootEntity);
         if (specification != null) {
            Predicate predicate = specification.toPredicate(root, criteriaDelete, this.criteriaBuilder);
            if (predicate != null) {
               criteriaDelete.where((Expression<Boolean>)predicate);
            }
         }

         queryResult = ((QueryResultPersistentEntityCriteriaQuery)criteriaDelete).buildQuery(sqlQueryBuilder);
      } else {
         if (type != AbstractSpecificationInterceptor.Type.UPDATE_ALL) {
            throw new IllegalStateException("Unknown criteria type: " + type);
         }

         UpdateSpecification<Object> specification = this.getUpdateSpecification(context);
         PersistentEntityCriteriaUpdate<Object> criteriaUpdate = this.criteriaBuilder.createCriteriaUpdate(rootEntity);
         Root<Object> root = criteriaUpdate.from(rootEntity);
         if (specification != null) {
            Predicate predicate = specification.toPredicate(root, criteriaUpdate, this.criteriaBuilder);
            if (predicate != null) {
               criteriaUpdate.where((Expression<Boolean>)predicate);
            }
         }

         queryResult = ((QueryResultPersistentEntityCriteriaQuery)criteriaUpdate).buildQuery(sqlQueryBuilder);
      }

      String query = queryResult.getQuery();
      String update = queryResult.getUpdate();
      List<QueryParameterBinding> parameterBindings = queryResult.getParameterBindings();
      List<io.micronaut.data.model.runtime.QueryParameterBinding> queryParameters = new ArrayList(parameterBindings.size());

      for(QueryParameterBinding p : parameterBindings) {
         queryParameters.add(new AbstractSpecificationInterceptor.QueryResultParameterBinding(p, queryParameters));
      }

      String[] queryParts = queryParameters.stream().anyMatch(io.micronaut.data.model.runtime.QueryParameterBinding::isExpandable)
         ? (String[])queryResult.getQueryParts().toArray(new String[0])
         : null;
      StoredQuery<E, QR> storedQuery;
      if (type == AbstractSpecificationInterceptor.Type.COUNT) {
         storedQuery = this.storedQueryResolver
            .createCountStoredQuery(
               context.getExecutableMethod(),
               DataMethod.OperationType.COUNT,
               context.getName(),
               context.getAnnotationMetadata(),
               rootEntity,
               query,
               queryParts,
               queryParameters
            );
      } else if (type == AbstractSpecificationInterceptor.Type.FIND_ALL) {
         storedQuery = this.storedQueryResolver
            .createStoredQuery(
               context.getExecutableMethod(),
               DataMethod.OperationType.QUERY,
               context.getName(),
               context.getAnnotationMetadata(),
               rootEntity,
               query,
               null,
               queryParts,
               queryParameters,
               !pageable.isUnpaged(),
               false
            );
      } else {
         DataMethod.OperationType operationType;
         switch(type) {
            case COUNT:
               operationType = DataMethod.OperationType.COUNT;
               break;
            case DELETE_ALL:
               operationType = DataMethod.OperationType.DELETE;
               break;
            case UPDATE_ALL:
               operationType = DataMethod.OperationType.UPDATE;
               break;
            case FIND_ALL:
            case FIND_ONE:
            case FIND_PAGE:
               operationType = DataMethod.OperationType.QUERY;
               break;
            default:
               throw new IllegalStateException("Unknown value: " + type);
         }

         storedQuery = this.storedQueryResolver
            .createStoredQuery(
               context.getExecutableMethod(),
               operationType,
               context.getName(),
               context.getAnnotationMetadata(),
               rootEntity,
               query,
               update,
               queryParts,
               queryParameters,
               false,
               true
            );
      }

      return this.preparedQueryResolver.resolveQuery(context, storedQuery, pageable);
   }

   @Nullable
   protected QuerySpecification<Object> getQuerySpecification(MethodInvocationContext<?, ?> context) {
      Object parameterValue = context.getParameterValues()[0];
      if (parameterValue instanceof QuerySpecification) {
         return (QuerySpecification<Object>)parameterValue;
      } else if (parameterValue instanceof PredicateSpecification) {
         return QuerySpecification.where((PredicateSpecification<Object>)parameterValue);
      } else {
         Argument<?> parameterArgument = context.getArguments()[0];
         if (!parameterArgument.isAssignableFrom(QuerySpecification.class) && !parameterArgument.isAssignableFrom(PredicateSpecification.class)) {
            throw new IllegalArgumentException("Argument must be an instance of: " + QuerySpecification.class + " or " + PredicateSpecification.class);
         } else {
            return null;
         }
      }
   }

   @Nullable
   protected DeleteSpecification<Object> getDeleteSpecification(MethodInvocationContext<?, ?> context) {
      Object parameterValue = context.getParameterValues()[0];
      if (parameterValue instanceof DeleteSpecification) {
         return (DeleteSpecification<Object>)parameterValue;
      } else if (parameterValue instanceof PredicateSpecification) {
         return DeleteSpecification.where((PredicateSpecification<Object>)parameterValue);
      } else {
         Argument<?> parameterArgument = context.getArguments()[0];
         if (!parameterArgument.isAssignableFrom(DeleteSpecification.class) && !parameterArgument.isAssignableFrom(PredicateSpecification.class)) {
            throw new IllegalArgumentException("Argument must be an instance of: " + DeleteSpecification.class + " or " + PredicateSpecification.class);
         } else {
            return null;
         }
      }
   }

   @Nullable
   protected UpdateSpecification<Object> getUpdateSpecification(MethodInvocationContext<?, ?> context) {
      Object parameterValue = context.getParameterValues()[0];
      if (parameterValue instanceof UpdateSpecification) {
         return (UpdateSpecification<Object>)parameterValue;
      } else {
         Argument<?> parameterArgument = context.getArguments()[0];
         if (!parameterArgument.isAssignableFrom(UpdateSpecification.class) && !parameterArgument.isAssignableFrom(PredicateSpecification.class)) {
            throw new IllegalArgumentException("Argument must be an instance of: " + UpdateSpecification.class);
         } else {
            return null;
         }
      }
   }

   private List<Order> getOrders(Sort sort, Root<?> root, CriteriaBuilder cb) {
      List<Order> orders = new ArrayList();

      for(Sort.Order order : sort.getOrderBy()) {
         Path<Object> propertyPath = root.get(order.getProperty());
         orders.add(order.isAscending() ? cb.asc(propertyPath) : cb.desc(propertyPath));
      }

      return orders;
   }

   private static class QueryResultParameterBinding implements io.micronaut.data.model.runtime.QueryParameterBinding {
      private final QueryParameterBinding p;
      private final List<io.micronaut.data.model.runtime.QueryParameterBinding> all;
      private boolean previousInitialized;
      private io.micronaut.data.model.runtime.QueryParameterBinding previousPopulatedValueParameter;

      public QueryResultParameterBinding(QueryParameterBinding p, List<io.micronaut.data.model.runtime.QueryParameterBinding> all) {
         this.p = p;
         this.all = all;
      }

      @Override
      public String getName() {
         return this.p.getKey();
      }

      @Override
      public DataType getDataType() {
         return this.p.getDataType();
      }

      @Override
      public Class<?> getParameterConverterClass() {
         return (Class<?>)ClassUtils.forName(this.p.getConverterClassName(), null).orElseThrow(IllegalStateException::new);
      }

      @Override
      public int getParameterIndex() {
         return this.p.getParameterIndex();
      }

      @Override
      public String[] getParameterBindingPath() {
         return this.p.getParameterBindingPath();
      }

      @Override
      public String[] getPropertyPath() {
         return this.p.getPropertyPath();
      }

      @Override
      public boolean isAutoPopulated() {
         return this.p.isAutoPopulated();
      }

      @Override
      public boolean isRequiresPreviousPopulatedValue() {
         return this.p.isRequiresPreviousPopulatedValue();
      }

      @Override
      public io.micronaut.data.model.runtime.QueryParameterBinding getPreviousPopulatedValueParameter() {
         if (!this.previousInitialized) {
            for(io.micronaut.data.model.runtime.QueryParameterBinding it : this.all) {
               if (it != this && it.getParameterIndex() != -1 && Arrays.equals(this.getPropertyPath(), it.getPropertyPath())) {
                  this.previousPopulatedValueParameter = it;
                  break;
               }
            }

            this.previousInitialized = true;
         }

         return this.previousPopulatedValueParameter;
      }

      @Override
      public boolean isExpandable() {
         return this.p.isExpandable();
      }
   }

   protected static enum Type {
      COUNT,
      FIND_ONE,
      FIND_PAGE,
      FIND_ALL,
      DELETE_ALL,
      UPDATE_ALL;
   }
}
