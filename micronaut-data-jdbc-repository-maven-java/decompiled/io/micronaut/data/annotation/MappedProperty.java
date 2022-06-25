package io.micronaut.data.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.data.model.DataType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface MappedProperty {
   String EMBEDDED_PROPERTIES = "embeddedProperties";

   String value() default "";

   @AliasFor(
      annotation = TypeDef.class,
      member = "type"
   )
   DataType type() default DataType.OBJECT;

   @AliasFor(
      annotation = TypeDef.class,
      member = "converter"
   )
   Class<?> converter() default Object.class;

   Class<?> converterPersistedType() default Object.class;

   String definition() default "";
}
