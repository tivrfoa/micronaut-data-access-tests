package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.OptionalMultiValues;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $GenericResource$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Map.class,
            "additionalProperties",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JsonAnyGetter", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JsonAnyGetter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonAnyGetter")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(String.class, "K"),
            Argument.ofTypeVariable(Object.class, "V")
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
         2,
         -1,
         3,
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
               )
            )
         ),
         4,
         -1,
         5,
         true,
         false
      )
   };

   public $GenericResource$Introspection() {
      super(GenericResource.class, $GenericResource$IntrospectionRef.$ANNOTATION_METADATA, null, null, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((GenericResource)var2).getAdditionalProperties();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [additionalProperties] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.GenericResource"
            );
         case 2:
            return ((GenericResource)var2).getLinks();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [links] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.GenericResource"
            );
         case 4:
            return ((GenericResource)var2).getEmbedded();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot mutate property [embedded] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.GenericResource"
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
               return 1;
            }
            break;
         case 785848970:
            if (var1.equals("embedded")) {
               return 2;
            }
            break;
         case 1887542458:
            if (var1.equals("additionalProperties")) {
               return 0;
            }
      }

      return -1;
   }

   @Override
   public Object instantiate() {
      return new GenericResource();
   }
}
