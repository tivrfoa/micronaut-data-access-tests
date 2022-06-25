package io.micronaut.management.health.indicator;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.health.HealthStatus;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $DefaultHealthResult$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AnnotationMetadata $FIELD_CONSTRUCTOR_ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonCreator", Collections.EMPTY_MAP, "io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonCreator", Collections.EMPTY_MAP, "io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonCreator")),
      false,
      true
   );
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(
         String.class,
         "name",
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "name")),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "name")),
            AnnotationUtil.mapOf(
               "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
            ),
            false,
            true
         ),
         null
      ),
      Argument.of(
         String.class,
         "status",
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "status")),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "status")),
            AnnotationUtil.mapOf(
               "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
            ),
            false,
            true
         ),
         null
      ),
      Argument.of(
         Map.class,
         "details",
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "details")),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "details")),
            AnnotationUtil.mapOf(
               "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
            ),
            false,
            true
         ),
         Argument.ofTypeVariable(String.class, "K"),
         Argument.ofTypeVariable(Object.class, "V")
      )
   };
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "name"), 0, -1, 1, true, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(HealthStatus.class, "status"), 2, -1, 3, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Object.class, "details"), 4, -1, 5, true, false)
   };

   public $DefaultHealthResult$Introspection() {
      super(
         DefaultHealthResult.class,
         $DefaultHealthResult$IntrospectionRef.$ANNOTATION_METADATA,
         $FIELD_CONSTRUCTOR_ANNOTATION_METADATA,
         $CONSTRUCTOR_ARGUMENTS,
         $PROPERTIES_REFERENCES,
         null
      );
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((DefaultHealthResult)var2).getName();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot create copy of type [io.micronaut.management.health.indicator.DefaultHealthResult]. Property of type [java.lang.Object] is not assignable to constructor argument [details]"
            );
         case 2:
            return ((DefaultHealthResult)var2).getStatus();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [status] that is not mutable via a setter method or constructor argument for type: io.micronaut.management.health.indicator.DefaultHealthResult"
            );
         case 4:
            return ((DefaultHealthResult)var2).getDetails();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot mutate property [details] that is not mutable via a setter method or constructor argument for type: io.micronaut.management.health.indicator.DefaultHealthResult"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -892481550:
            if (var1.equals("status")) {
               return 1;
            }
            break;
         case 3373707:
            if (var1.equals("name")) {
               return 0;
            }
            break;
         case 1557721666:
            if (var1.equals("details")) {
               return 2;
            }
      }

      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultHealthResult((String)var1[0], (String)var1[1], (Map<String, Object>)var1[2]);
   }
}
