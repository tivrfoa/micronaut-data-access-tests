package io.micronaut.data.model.query.builder.sql;

public @interface SqlQueryConfiguration {
   SqlQueryConfiguration.DialectConfiguration[] value() default {};

   public @interface DialectConfiguration {
      Dialect dialect();

      String positionalParameterFormat() default "?";

      boolean escapeQueries() default true;
   }
}
