package io.micronaut.data.model.query.builder;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospectionReference;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.builder.jpa.JpaQueryBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Introspected
public interface QueryBuilder {
   Pattern VARIABLE_PATTERN = Pattern.compile("([^:])(:([a-zA-Z0-9]+))");
   @Deprecated
   Pattern IN_VARIABLES_PATTERN = Pattern.compile("(?<singleGroup>:[a-zA-Z0-9]+)|(?<inGroup>IN\\((:[a-zA-Z0-9]+)\\))");

   @Nullable
   QueryResult buildInsert(AnnotationMetadata repositoryMetadata, PersistentEntity entity);

   @NonNull
   default QueryResult buildQuery(@NonNull QueryModel query) {
      return this.buildQuery(AnnotationMetadata.EMPTY_METADATA, query);
   }

   QueryResult buildQuery(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query);

   @NonNull
   default QueryResult buildUpdate(@NonNull QueryModel query, @NonNull List<String> propertiesToUpdate) {
      return this.buildUpdate(AnnotationMetadata.EMPTY_METADATA, query, propertiesToUpdate);
   }

   @NonNull
   default QueryResult buildUpdate(@NonNull QueryModel query, @NonNull Map<String, Object> propertiesToUpdate) {
      return this.buildUpdate(AnnotationMetadata.EMPTY_METADATA, query, propertiesToUpdate);
   }

   QueryResult buildUpdate(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query, @NonNull List<String> propertiesToUpdate);

   QueryResult buildUpdate(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query, @NonNull Map<String, Object> propertiesToUpdate);

   @NonNull
   default QueryResult buildDelete(@NonNull QueryModel query) {
      return this.buildDelete(AnnotationMetadata.EMPTY_METADATA, query);
   }

   QueryResult buildDelete(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query);

   @NonNull
   QueryResult buildOrderBy(@NonNull PersistentEntity entity, @NonNull Sort sort);

   @NonNull
   QueryResult buildPagination(@NonNull Pageable pageable);

   @NonNull
   static QueryBuilder newQueryBuilder(@NonNull AnnotationMetadata annotationMetadata) {
      return (QueryBuilder)annotationMetadata.stringValue(RepositoryConfiguration.class, "queryBuilder")
         .flatMap(
            type -> BeanIntrospector.SHARED
                  .findIntrospections((Predicate<? super BeanIntrospectionReference<?>>)(ref -> ref.isPresent() && ref.getBeanType().getName().equals(type)))
                  .stream()
                  .findFirst()
                  .map(introspection -> {
                     try {
                        Argument<?>[] constructorArguments = introspection.getConstructorArguments();
                        if (constructorArguments.length == 0) {
                           return (QueryBuilder)introspection.instantiate();
                        }
         
                        if (constructorArguments.length == 1 && constructorArguments[0].getType() == AnnotationMetadata.class) {
                           return (QueryBuilder)introspection.instantiate(annotationMetadata);
                        }
                     } catch (InstantiationException var3) {
                        return new JpaQueryBuilder();
                     }
         
                     return new JpaQueryBuilder();
                  })
         )
         .orElse(new JpaQueryBuilder());
   }

   default boolean shouldAliasProjections() {
      return true;
   }

   default boolean supportsForUpdate() {
      return false;
   }
}
