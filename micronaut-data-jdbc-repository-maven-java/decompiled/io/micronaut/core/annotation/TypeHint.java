package io.micronaut.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeHint {
   Class[] value() default {};

   TypeHint.AccessType[] accessType() default {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS};

   String[] typeNames() default {};

   public static enum AccessType {
      ALL_PUBLIC,
      ALL_DECLARED_CONSTRUCTORS,
      ALL_PUBLIC_CONSTRUCTORS,
      ALL_DECLARED_METHODS,
      ALL_DECLARED_FIELDS,
      ALL_PUBLIC_METHODS,
      ALL_PUBLIC_FIELDS;
   }
}
