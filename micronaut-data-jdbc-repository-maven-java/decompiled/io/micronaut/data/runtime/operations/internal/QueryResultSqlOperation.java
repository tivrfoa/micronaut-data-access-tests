package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.query.builder.QueryParameterBinding;
import io.micronaut.data.model.query.builder.QueryResult;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import java.util.List;
import java.util.stream.Collectors;

@Internal
public class QueryResultSqlOperation extends StoredSqlOperation {
   public QueryResultSqlOperation(SqlQueryBuilder queryBuilder, QueryResult queryResult) {
      super(
         queryBuilder,
         queryResult.getQuery(),
         queryResult.getParameterBindings().stream().anyMatch(QueryParameterBinding::isExpandable)
            ? (String[])queryResult.getQueryParts().toArray(new String[0])
            : null,
         (List<io.micronaut.data.model.runtime.QueryParameterBinding>)queryResult.getParameterBindings()
            .stream()
            .map(QueryResultSqlOperation::map)
            .collect(Collectors.toList()),
         false
      );
   }

   private static io.micronaut.data.model.runtime.QueryParameterBinding map(QueryParameterBinding binding) {
      return new io.micronaut.data.model.runtime.QueryParameterBinding() {
         @Override
         public String getName() {
            return binding.getKey();
         }

         @Override
         public DataType getDataType() {
            return binding.getDataType();
         }

         @Override
         public Class<?> getParameterConverterClass() {
            return (Class<?>)ClassUtils.forName(binding.getConverterClassName(), null).orElseThrow(IllegalStateException::new);
         }

         @Override
         public int getParameterIndex() {
            return binding.getParameterIndex();
         }

         @Override
         public String[] getParameterBindingPath() {
            return binding.getParameterBindingPath();
         }

         @Override
         public String[] getPropertyPath() {
            return binding.getPropertyPath();
         }

         @Override
         public boolean isAutoPopulated() {
            return binding.isAutoPopulated();
         }

         @Override
         public boolean isRequiresPreviousPopulatedValue() {
            return binding.isRequiresPreviousPopulatedValue();
         }

         @Override
         public io.micronaut.data.model.runtime.QueryParameterBinding getPreviousPopulatedValueParameter() {
            return null;
         }
      };
   }
}
