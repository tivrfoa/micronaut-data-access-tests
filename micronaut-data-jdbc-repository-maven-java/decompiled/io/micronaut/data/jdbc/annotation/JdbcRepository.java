package io.micronaut.data.jdbc.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.annotation.TypeRole;
import io.micronaut.data.jdbc.mapper.SqlResultConsumer;
import io.micronaut.data.jdbc.operations.JdbcRepositoryOperations;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RepositoryConfiguration(
   queryBuilder = SqlQueryBuilder.class,
   operations = JdbcRepositoryOperations.class,
   implicitQueries = false,
   namedParameters = false,
   typeRoles = {@TypeRole(
   role = "sqlMappingFunction",
   type = SqlResultConsumer.class
)}
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Repository
public @interface JdbcRepository {
   @AliasFor(
      annotation = Repository.class,
      member = "dialect"
   )
   Dialect dialect() default Dialect.ANSI;

   @Aliases({@AliasFor(
   annotation = Repository.class,
   member = "dialect"
), @AliasFor(
   annotation = JdbcRepository.class,
   member = "dialect"
)})
   String dialectName() default "ANSI";
}
