package io.micronaut.management.endpoint.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.endpoint.EndpointEnabledCondition;
import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Singleton
@ConfigurationReader(
   prefix = "endpoints"
)
@Requires(
   condition = EndpointEnabledCondition.class
)
public @interface Endpoint {
   boolean ENABLED = true;
   boolean SENSITIVE = true;
   String DEFAULT_PREFIX = "endpoints";
   String ALL = "all";

   @Aliases({@AliasFor(
   annotation = ConfigurationReader.class,
   member = "value"
), @AliasFor(
   member = "id"
)})
   String value() default "";

   @Aliases({@AliasFor(
   member = "value"
), @AliasFor(
   annotation = ConfigurationReader.class,
   member = "value"
)})
   String id() default "";

   @AliasFor(
      annotation = ConfigurationReader.class,
      member = "prefix"
   )
   String prefix() default "endpoints";

   boolean defaultEnabled() default true;

   boolean defaultSensitive() default true;

   String defaultConfigurationId() default "all";
}
