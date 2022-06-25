package io.micronaut.flyway.endpoint;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.MigrationInfo;

// $FF: synthetic class
@Generated
final class $FlywayReport$Introspection extends AbstractInitializableBeanIntrospection {
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
      Argument.of(String.class, "name"), Argument.of(List.class, "changeSets", null, Argument.ofTypeVariable(MigrationInfo.class, "E"))
   };
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "name"), 0, -1, 1, true, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            List.class,
            "migrations",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.databind.annotation.JsonSerialize", AnnotationUtil.mapOf("contentAs", $micronaut_load_class_value_0())
               ),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.databind.annotation.JsonSerialize", AnnotationUtil.mapOf("contentAs", $micronaut_load_class_value_0())
               ),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.databind.annotation.JsonSerialize")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(MigrationInfo.class, "E")
         ),
         2,
         -1,
         3,
         true,
         false
      )
   };

   public $FlywayReport$Introspection() {
      super(
         FlywayReport.class,
         $FlywayReport$IntrospectionRef.$ANNOTATION_METADATA,
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
            return ((FlywayReport)var2).getName();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot create copy of type [io.micronaut.flyway.endpoint.FlywayReport]. Constructor contains argument [changeSets] that is not a readable property"
            );
         case 2:
            return ((FlywayReport)var2).getMigrations();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [migrations] that is not mutable via a setter method or constructor argument for type: io.micronaut.flyway.endpoint.FlywayReport"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -644856219:
            if (var1.equals("migrations")) {
               return 1;
            }
            break;
         case 3373707:
            if (var1.equals("name")) {
               return 0;
            }
      }

      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new FlywayReport((String)var1[0], (List<MigrationInfo>)var1[1]);
   }
}
