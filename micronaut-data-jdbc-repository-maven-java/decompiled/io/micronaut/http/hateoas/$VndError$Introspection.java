package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.OptionalMultiValues;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
final class $VndError$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{Argument.of(String.class, "message")};
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "message"), 0, 1, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Optional.class,
            "logref",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "logref")),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "logref")),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(String.class, "T")
         ),
         2,
         -1,
         3,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Optional.class,
            "path",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "path")),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "path")),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(String.class, "T")
         ),
         4,
         -1,
         5,
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
         6,
         -1,
         7,
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
         8,
         -1,
         9,
         true,
         false
      )
   };

   public $VndError$Introspection() {
      super(VndError.class, $VndError$IntrospectionRef.$ANNOTATION_METADATA, null, $CONSTRUCTOR_ARGUMENTS, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((VndError)var2).getMessage();
         case 1:
            ((VndError)var2).setMessage((String)var3);
            return null;
         case 2:
            return ((VndError)var2).getLogref();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [logref] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.VndError"
            );
         case 4:
            return ((VndError)var2).getPath();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot mutate property [path] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.VndError"
            );
         case 6:
            return ((VndError)var2).getLinks();
         case 7:
            throw new UnsupportedOperationException(
               "Cannot mutate property [links] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.VndError"
            );
         case 8:
            return ((VndError)var2).getEmbedded();
         case 9:
            throw new UnsupportedOperationException(
               "Cannot mutate property [embedded] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.VndError"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -1097326897:
            if (var1.equals("logref")) {
               return 1;
            }
            break;
         case 3433509:
            if (var1.equals("path")) {
               return 2;
            }
            break;
         case 102977465:
            if (var1.equals("links")) {
               return 3;
            }
            break;
         case 785848970:
            if (var1.equals("embedded")) {
               return 4;
            }
            break;
         case 954925063:
            if (var1.equals("message")) {
               return 0;
            }
      }

      return -1;
   }

   @Override
   public Object instantiate() {
      return new VndError();
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new VndError((String)var1[0]);
   }
}
