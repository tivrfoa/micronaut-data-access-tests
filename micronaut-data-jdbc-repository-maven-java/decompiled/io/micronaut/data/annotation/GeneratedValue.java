package io.micronaut.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface GeneratedValue {
   GeneratedValue.Type value() default GeneratedValue.Type.AUTO;

   String definition() default "";

   String ref() default "";

   public static enum Type {
      AUTO,
      SEQUENCE,
      IDENTITY,
      UUID;
   }
}
