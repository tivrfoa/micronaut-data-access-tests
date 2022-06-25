package io.micronaut.data.annotation;

import io.micronaut.data.annotation.repeatable.TypeDefinitions;
import io.micronaut.data.model.DataType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Documented
@Repeatable(TypeDefinitions.class)
@Inherited
public @interface TypeDef {
   DataType type();

   Class<?> converter() default Object.class;

   Class[] classes() default {};

   String[] names() default {};
}
