package io.micronaut.data.runtime.query.internal;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.StoredQuery;
import io.micronaut.transaction.TransactionDefinition;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DelegateStoredQuery<E, R> extends StoredQuery<E, R> {
   StoredQuery<E, R> getStoredQueryDelegate();

   @Override
   default AnnotationMetadata getAnnotationMetadata() {
      return this.getStoredQueryDelegate().getAnnotationMetadata();
   }

   @Override
   default Class<E> getRootEntity() {
      return this.getStoredQueryDelegate().getRootEntity();
   }

   @Override
   default boolean hasPageable() {
      return this.getStoredQueryDelegate().hasPageable();
   }

   @Override
   default String getQuery() {
      return this.getStoredQueryDelegate().getQuery();
   }

   @Override
   default String[] getExpandableQueryParts() {
      return this.getStoredQueryDelegate().getExpandableQueryParts();
   }

   @Override
   default List<QueryParameterBinding> getQueryBindings() {
      return this.getStoredQueryDelegate().getQueryBindings();
   }

   @Override
   default Class<R> getResultType() {
      return this.getStoredQueryDelegate().getResultType();
   }

   @Override
   default Optional<TransactionDefinition> getTransactionDefinition() {
      return this.getStoredQueryDelegate().getTransactionDefinition();
   }

   @Override
   default Argument<R> getResultArgument() {
      return this.getStoredQueryDelegate().getResultArgument();
   }

   @Override
   default DataType getResultDataType() {
      return this.getStoredQueryDelegate().getResultDataType();
   }

   @Override
   default boolean isNative() {
      return this.getStoredQueryDelegate().isNative();
   }

   @Override
   default boolean useNumericPlaceholders() {
      return this.getStoredQueryDelegate().useNumericPlaceholders();
   }

   @Override
   default boolean isDtoProjection() {
      return this.getStoredQueryDelegate().isDtoProjection();
   }

   @Override
   default Optional<Class<?>> getEntityIdentifierType() {
      return this.getStoredQueryDelegate().getEntityIdentifierType();
   }

   @Override
   default Class<?>[] getArgumentTypes() {
      return this.getStoredQueryDelegate().getArgumentTypes();
   }

   @Override
   default boolean isCount() {
      return this.getStoredQueryDelegate().isCount();
   }

   @Override
   default Map<String, Object> getQueryHints() {
      return this.getStoredQueryDelegate().getQueryHints();
   }

   @Override
   default Set<JoinPath> getJoinFetchPaths() {
      return this.getStoredQueryDelegate().getJoinFetchPaths();
   }

   @Override
   default boolean isSingleResult() {
      return this.getStoredQueryDelegate().isSingleResult();
   }

   @Override
   default boolean hasResultConsumer() {
      return this.getStoredQueryDelegate().hasResultConsumer();
   }

   @Override
   default boolean isOptimisticLock() {
      return this.getStoredQueryDelegate().isOptimisticLock();
   }

   @Override
   default String getName() {
      return this.getStoredQueryDelegate().getName();
   }

   @Nullable
   @Override
   default String[] getIndexedParameterAutoPopulatedPropertyPaths() {
      return this.getStoredQueryDelegate().getIndexedParameterAutoPopulatedPropertyPaths();
   }

   @Override
   default String[] getIndexedParameterAutoPopulatedPreviousPropertyPaths() {
      return this.getStoredQueryDelegate().getIndexedParameterAutoPopulatedPreviousPropertyPaths();
   }

   @Override
   default int[] getIndexedParameterAutoPopulatedPreviousPropertyIndexes() {
      return this.getStoredQueryDelegate().getIndexedParameterAutoPopulatedPreviousPropertyIndexes();
   }

   @Deprecated
   @Override
   default boolean hasInExpression() {
      return this.getStoredQueryDelegate().hasInExpression();
   }
}
