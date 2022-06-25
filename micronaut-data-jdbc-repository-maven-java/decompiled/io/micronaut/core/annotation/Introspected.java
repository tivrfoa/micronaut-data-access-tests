package io.micronaut.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
public @interface Introspected {
   Class<?>[] classes() default {};

   Introspected.AccessKind[] accessKind() default {Introspected.AccessKind.METHOD};

   Introspected.Visibility[] visibility() default {Introspected.Visibility.DEFAULT};

   String[] packages() default {};

   String[] includes() default {};

   String[] excludes() default {};

   Class<? extends Annotation>[] excludedAnnotations() default {};

   Class<? extends Annotation>[] includedAnnotations() default {};

   boolean annotationMetadata() default true;

   Introspected.IndexedAnnotation[] indexed() default {};

   String withPrefix() default "with";

   public static enum AccessKind {
      FIELD,
      METHOD;
   }

   @Documented
   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
   public @interface IndexedAnnotation {
      Class<? extends Annotation> annotation();

      String member() default "";
   }

   public static enum Visibility {
      PUBLIC,
      DEFAULT;
   }
}
