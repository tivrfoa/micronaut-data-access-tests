package io.micronaut.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Relation {
   Relation.Kind value();

   String mappedBy() default "";

   Relation.Cascade[] cascade() default {Relation.Cascade.NONE};

   public static enum Cascade {
      ALL,
      PERSIST,
      UPDATE,
      NONE;
   }

   public static enum Kind {
      ONE_TO_MANY(false),
      ONE_TO_ONE(true),
      MANY_TO_MANY(false),
      EMBEDDED(true),
      MANY_TO_ONE(true);

      private final boolean singleEnded;

      private Kind(boolean singleEnded) {
         this.singleEnded = singleEnded;
      }

      public boolean isSingleEnded() {
         return this.singleEnded;
      }
   }
}
