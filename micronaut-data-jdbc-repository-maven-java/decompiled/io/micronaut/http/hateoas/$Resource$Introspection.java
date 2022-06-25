package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.OptionalMultiValues;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $Resource$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AnnotationMetadata $FIELD_CONSTRUCTOR_ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "com.fasterxml.jackson.annotation.JsonCreator",
         AnnotationUtil.mapOf("mode", "DELEGATING"),
         "io.micronaut.core.annotation.Creator",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "com.fasterxml.jackson.annotation.JsonCreator",
         AnnotationUtil.mapOf("mode", "DELEGATING"),
         "io.micronaut.core.annotation.Creator",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonCreator")),
      false,
      true
   );
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{Argument.of(GenericResource.class, "genericResource")};
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            OptionalMultiValues.class,
            "links",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "_links")),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "_links")),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(Link.class, "V")
         ),
         0,
         -1,
         1,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            OptionalMultiValues.class,
            "embedded",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "_embedded")),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "_embedded")),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(
               Resource.class,
               "V",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("io.micronaut.core.annotation.Introspected", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               Argument.ZERO_ARGUMENTS
            )
         ),
         2,
         -1,
         3,
         true,
         false
      )
   };

   public $Resource$Introspection() {
      super(
         Resource.class,
         $Resource$IntrospectionRef.$ANNOTATION_METADATA,
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
            return ((Resource)var2).getLinks();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [links] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.Resource"
            );
         case 2:
            return ((Resource)var2).getEmbedded();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [embedded] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.Resource"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case 102977465:
            if (var1.equals("links")) {
               return 0;
            }
            break;
         case 785848970:
            if (var1.equals("embedded")) {
               return 1;
            }
      }

      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return Resource.deserialize((GenericResource)var1[0]);
   }
}
