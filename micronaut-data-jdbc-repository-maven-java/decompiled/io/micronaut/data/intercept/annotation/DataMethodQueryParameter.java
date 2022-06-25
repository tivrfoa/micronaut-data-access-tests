package io.micronaut.data.intercept.annotation;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.DataType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Internal
@Inherited
public @interface DataMethodQueryParameter {
   String META_MEMBER_NAME = "name";
   String META_MEMBER_DATA_TYPE = "dataType";
   String META_MEMBER_PARAMETER_INDEX = "parameterIndex";
   String META_MEMBER_PARAMETER_BINDING_PATH = "parameterBindingPath";
   String META_MEMBER_PROPERTY = "property";
   String META_MEMBER_PROPERTY_PATH = "propertyPath";
   String META_MEMBER_CONVERTER = "converter";
   String META_MEMBER_AUTO_POPULATED = "autoPopulated";
   String META_MEMBER_REQUIRES_PREVIOUS_POPULATED_VALUES = "requiresPreviousPopulatedValue";
   String META_MEMBER_EXPANDABLE = "expandable";

   String name() default "";

   DataType dataType() default DataType.OBJECT;

   int parameterIndex() default -1;

   String[] parameterBindingPath() default {};

   String property() default "";

   String[] propertyPath() default {};

   Class[] converter() default {};

   boolean autoPopulated() default false;

   boolean requiresPreviousPopulatedValue() default false;
}
