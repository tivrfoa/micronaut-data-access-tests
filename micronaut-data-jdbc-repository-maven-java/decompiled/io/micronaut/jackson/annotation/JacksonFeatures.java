package io.micronaut.jackson.annotation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface JacksonFeatures {
   SerializationFeature[] enabledSerializationFeatures() default {};

   SerializationFeature[] disabledSerializationFeatures() default {};

   DeserializationFeature[] enabledDeserializationFeatures() default {};

   DeserializationFeature[] disabledDeserializationFeatures() default {};

   Class<? extends Module>[] additionalModules() default {};
}
