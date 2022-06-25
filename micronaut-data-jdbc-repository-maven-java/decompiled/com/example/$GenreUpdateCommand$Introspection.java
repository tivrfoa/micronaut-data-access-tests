package com.example;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $GenreUpdateCommand$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{Argument.of(Long.class, "id"), Argument.of(String.class, "name")};
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Long.class,
            "id",
            new DefaultAnnotationMetadata(
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
               AnnotationUtil.mapOf(
                  "javax.validation.constraints.NotNull$List",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "javax.validation.constraints.NotNull",
                           Collections.EMPTY_MAP,
                           AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.NotNull")
                        )
                     }
                  )
               ),
               AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
               false,
               true
            ),
            null
         ),
         0,
         -1,
         1,
         true,
         true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            String.class,
            "name",
            new DefaultAnnotationMetadata(
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
               AnnotationUtil.mapOf(
                  "javax.validation.constraints.NotBlank$List",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "javax.validation.constraints.NotBlank",
                           Collections.EMPTY_MAP,
                           AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.NotBlank")
                        )
                     }
                  )
               ),
               AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotBlank")),
               false,
               true
            ),
            null
         ),
         2,
         -1,
         3,
         true,
         true
      )
   };
   private static final int[] INDEX_1 = new int[]{0, 1};

   public $GenreUpdateCommand$Introspection() {
      super(GenreUpdateCommand.class, $GenreUpdateCommand$IntrospectionRef.$ANNOTATION_METADATA, null, $CONSTRUCTOR_ARGUMENTS, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((GenreUpdateCommand)var2).getId();
         case 1:
            GenreUpdateCommand var4 = (GenreUpdateCommand)var2;
            return new GenreUpdateCommand((Long)var3, var4.getName());
         case 2:
            return ((GenreUpdateCommand)var2).getName();
         case 3:
            GenreUpdateCommand var5 = (GenreUpdateCommand)var2;
            return new GenreUpdateCommand(var5.getId(), (String)var3);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case 3355:
            if (var1.equals("id")) {
               return 0;
            }
            break;
         case 3373707:
            if (var1.equals("name")) {
               return 1;
            }
      }

      return -1;
   }

   @Override
   protected final BeanProperty findIndexedProperty(Class var1, String var2) {
      String var3 = var1.getName();
      return var3.equals("javax.validation.Constraint") && var2 == null ? this.getPropertyByIndex(1) : null;
   }

   @Override
   public final Collection getIndexedProperties(Class var1) {
      String var2 = var1.getName();
      return (Collection)(var2.equals("javax.validation.Constraint") ? this.getBeanPropertiesIndexedSubset(INDEX_1) : Collections.emptyList());
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new GenreUpdateCommand((Long)var1[0], (String)var1[1]);
   }
}
