package io.micronaut.data.annotation;

import io.micronaut.data.annotation.repeatable.JoinSpecifications;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Repeatable(JoinSpecifications.class)
public @interface Join {
   String value();

   Join.Type type() default Join.Type.FETCH;

   String alias() default "";

   public static enum Type {
      DEFAULT,
      LEFT,
      LEFT_FETCH,
      RIGHT,
      RIGHT_FETCH,
      FETCH,
      INNER,
      OUTER;
   }
}
