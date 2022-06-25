package io.micronaut.data.runtime.query;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.StoredQuery;
import io.micronaut.inject.ExecutableMethod;
import java.util.List;

public interface StoredQueryResolver {
   <E, R> StoredQuery<E, R> resolveQuery(MethodInvocationContext<?, ?> context, Class<E> entityClass, Class<R> resultType);

   <E, R> StoredQuery<E, R> resolveCountQuery(MethodInvocationContext<?, ?> context, Class<E> entityClass, Class<R> resultType);

   <E, QR> StoredQuery<E, QR> createStoredQuery(
      ExecutableMethod<?, ?> executableMethod,
      DataMethod.OperationType operationType,
      String name,
      AnnotationMetadata annotationMetadata,
      Class<Object> rootEntity,
      String query,
      String update,
      String[] queryParts,
      List<QueryParameterBinding> queryParameters,
      boolean hasPageable,
      boolean isSingleResult
   );

   StoredQuery<Object, Long> createCountStoredQuery(
      ExecutableMethod<?, ?> executableMethod,
      DataMethod.OperationType operationType,
      String name,
      AnnotationMetadata annotationMetadata,
      Class<Object> rootEntity,
      String query,
      String[] queryParts,
      List<QueryParameterBinding> queryParameters
   );
}
