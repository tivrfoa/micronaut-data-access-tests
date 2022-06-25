package io.micronaut.data.annotation;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.jpa.JpaQueryBuilder;
import io.micronaut.data.operations.RepositoryOperations;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
public @interface RepositoryConfiguration {
   Class<? extends QueryBuilder> queryBuilder() default JpaQueryBuilder.class;

   Class<? extends RepositoryOperations> operations() default RepositoryOperations.class;

   TypeRole[] typeRoles() default {@TypeRole(
   role = "pageable",
   type = Pageable.class
), @TypeRole(
   role = "sort",
   type = Sort.class
), @TypeRole(
   role = "slice",
   type = Slice.class
), @TypeRole(
   role = "page",
   type = Page.class
)};

   boolean implicitQueries() default true;

   boolean namedParameters() default true;
}
