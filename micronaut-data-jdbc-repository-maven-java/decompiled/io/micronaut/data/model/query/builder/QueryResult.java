package io.micronaut.data.model.query.builder;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.model.DataType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface QueryResult {
   @NonNull
   String getQuery();

   @Nullable
   default String getUpdate() {
      return null;
   }

   @Nullable
   default String getAggregate() {
      return null;
   }

   @NonNull
   List<String> getQueryParts();

   @NonNull
   default Map<String, String> getParameters() {
      return (Map<String, String>)this.getParameterBindings()
         .stream()
         .collect(Collectors.toMap(QueryParameterBinding::getKey, p -> String.join(".", p.getPropertyPath())));
   }

   @NonNull
   default Map<String, DataType> getParameterTypes() {
      return (Map<String, DataType>)this.getParameterBindings()
         .stream()
         .collect(Collectors.toMap(p -> String.join(".", p.getPropertyPath()), QueryParameterBinding::getDataType, (d1, d2) -> d1));
   }

   List<QueryParameterBinding> getParameterBindings();

   Map<String, String> getAdditionalRequiredParameters();

   default int getMax() {
      return -1;
   }

   default long getOffset() {
      return 0L;
   }

   @NonNull
   static QueryResult of(
      @NonNull String query,
      @NonNull List<String> queryParts,
      @NonNull List<QueryParameterBinding> parameterBindings,
      @NonNull Map<String, String> additionalRequiredParameters
   ) {
      ArgumentUtils.requireNonNull("query", query);
      ArgumentUtils.requireNonNull("parameterBindings", parameterBindings);
      ArgumentUtils.requireNonNull("additionalRequiredParameters", additionalRequiredParameters);
      return new QueryResult() {
         @NonNull
         @Override
         public String getQuery() {
            return query;
         }

         @Override
         public List<String> getQueryParts() {
            return queryParts;
         }

         @Override
         public List<QueryParameterBinding> getParameterBindings() {
            return parameterBindings;
         }

         @Override
         public Map<String, String> getAdditionalRequiredParameters() {
            return additionalRequiredParameters;
         }
      };
   }

   @NonNull
   static QueryResult of(
      @NonNull String query,
      @NonNull List<String> queryParts,
      @NonNull List<QueryParameterBinding> parameterBindings,
      @NonNull Map<String, String> additionalRequiredParameters,
      int max,
      long offset
   ) {
      ArgumentUtils.requireNonNull("query", query);
      ArgumentUtils.requireNonNull("parameterBindings", parameterBindings);
      ArgumentUtils.requireNonNull("additionalRequiredParameters", additionalRequiredParameters);
      return new QueryResult() {
         @Override
         public int getMax() {
            return max;
         }

         @Override
         public long getOffset() {
            return offset;
         }

         @NonNull
         @Override
         public String getQuery() {
            return query;
         }

         @Override
         public List<String> getQueryParts() {
            return queryParts;
         }

         @Override
         public List<QueryParameterBinding> getParameterBindings() {
            return parameterBindings;
         }

         @Override
         public Map<String, String> getAdditionalRequiredParameters() {
            return additionalRequiredParameters;
         }
      };
   }
}
