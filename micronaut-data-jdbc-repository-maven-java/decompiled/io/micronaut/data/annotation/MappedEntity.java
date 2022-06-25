package io.micronaut.data.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.model.naming.NamingStrategies;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Documented
@Introspected(
   excludedAnnotations = {Transient.class},
   indexed = {@Introspected.IndexedAnnotation(
   annotation = Id.class
), @Introspected.IndexedAnnotation(
   annotation = Version.class
), @Introspected.IndexedAnnotation(
   annotation = DateCreated.class
), @Introspected.IndexedAnnotation(
   annotation = DateUpdated.class
), @Introspected.IndexedAnnotation(
   annotation = MappedProperty.class,
   member = "value"
), @Introspected.IndexedAnnotation(
   annotation = Index.class,
   member = "value"
)}
)
public @interface MappedEntity {
   String value() default "";

   @AliasFor(
      annotation = NamingStrategy.class,
      member = "value"
   )
   Class<? extends io.micronaut.data.model.naming.NamingStrategy> namingStrategy() default NamingStrategies.UnderScoreSeparatedLowerCase.class;

   boolean escape() default true;

   String alias() default "";
}
