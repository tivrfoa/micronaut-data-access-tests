package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.Named;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.query.JoinPath;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface StoredQuery<E, R> extends Named, StoredDataOperation<R> {
   @NonNull
   Class<E> getRootEntity();

   @Deprecated
   default boolean hasInExpression() {
      return false;
   }

   boolean hasPageable();

   @NonNull
   String getQuery();

   @NonNull
   String[] getExpandableQueryParts();

   List<QueryParameterBinding> getQueryBindings();

   @NonNull
   Class<R> getResultType();

   @NonNull
   @Override
   Argument<R> getResultArgument();

   @NonNull
   DataType getResultDataType();

   default boolean isNative() {
      return false;
   }

   boolean useNumericPlaceholders();

   default boolean isDtoProjection() {
      return false;
   }

   default Optional<Class<?>> getEntityIdentifierType() {
      return Optional.empty();
   }

   @NonNull
   default Class<?>[] getArgumentTypes() {
      return ReflectionUtils.EMPTY_CLASS_ARRAY;
   }

   @NonNull
   @Deprecated
   default Map<String, String> getParameterBinding() {
      return Collections.emptyMap();
   }

   boolean isCount();

   @Deprecated
   @NonNull
   default DataType[] getIndexedParameterTypes() {
      return DataType.EMPTY_DATA_TYPE_ARRAY;
   }

   @NonNull
   @Deprecated
   default int[] getIndexedParameterBinding() {
      return new int[0];
   }

   @Deprecated
   default String[] getParameterNames() {
      return StringUtils.EMPTY_STRING_ARRAY;
   }

   @Deprecated
   default String[] getIndexedParameterPaths() {
      return StringUtils.EMPTY_STRING_ARRAY;
   }

   @NonNull
   default Map<String, Object> getQueryHints() {
      return Collections.emptyMap();
   }

   @Deprecated
   @Nullable
   default String getLastUpdatedProperty() {
      return null;
   }

   @Deprecated
   default String[] getIndexedParameterAutoPopulatedPropertyPaths() {
      return StringUtils.EMPTY_STRING_ARRAY;
   }

   @Deprecated
   default String[] getIndexedParameterAutoPopulatedPreviousPropertyPaths() {
      return StringUtils.EMPTY_STRING_ARRAY;
   }

   @Deprecated
   default int[] getIndexedParameterAutoPopulatedPreviousPropertyIndexes() {
      return new int[0];
   }

   @NonNull
   default Set<JoinPath> getJoinFetchPaths() {
      return Collections.emptySet();
   }

   boolean isSingleResult();

   boolean hasResultConsumer();

   default boolean isOptimisticLock() {
      return false;
   }
}
