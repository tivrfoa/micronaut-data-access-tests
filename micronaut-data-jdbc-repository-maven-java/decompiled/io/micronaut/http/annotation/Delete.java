package io.micronaut.http.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.core.async.annotation.SingleResult;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@HttpMethodMapping
@Inherited
public @interface Delete {
   @Aliases({@AliasFor(
   annotation = HttpMethodMapping.class,
   member = "value"
), @AliasFor(
   annotation = UriMapping.class,
   member = "value"
)})
   String value() default "/";

   @Aliases({@AliasFor(
   annotation = HttpMethodMapping.class,
   member = "value"
), @AliasFor(
   annotation = UriMapping.class,
   member = "value"
)})
   String uri() default "/";

   @Aliases({@AliasFor(
   annotation = HttpMethodMapping.class,
   member = "uris"
), @AliasFor(
   annotation = UriMapping.class,
   member = "uris"
)})
   String[] uris() default {"/"};

   @AliasFor(
      annotation = Consumes.class,
      member = "value"
   )
   String[] consumes() default {};

   @AliasFor(
      annotation = Produces.class,
      member = "value"
   )
   String[] produces() default {};

   @Aliases({@AliasFor(
   annotation = Produces.class,
   member = "value"
), @AliasFor(
   annotation = Consumes.class,
   member = "value"
)})
   String[] processes() default {};

   @Aliases({@AliasFor(
   annotation = Produces.class,
   member = "single"
), @AliasFor(
   annotation = Consumes.class,
   member = "single"
), @AliasFor(
   annotation = SingleResult.class,
   member = "value"
)})
   boolean single() default false;
}
