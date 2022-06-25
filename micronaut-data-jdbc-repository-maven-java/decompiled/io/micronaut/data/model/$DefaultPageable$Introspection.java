package io.micronaut.data.model;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.List;

// $FF: synthetic class
@Generated
final class $DefaultPageable$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AnnotationMetadata $FIELD_CONSTRUCTOR_ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      Collections.EMPTY_MAP,
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      false,
      true
   );
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(Integer.TYPE, "page"),
      Argument.of(Integer.TYPE, "size"),
      Argument.of(
         Sort.class,
         "sort",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         null
      )
   };
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Integer.TYPE, "size"), 0, -1, 1, true, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Integer.TYPE, "number"), 2, -1, 3, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Sort.class,
            "sort",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         4,
         -1,
         5,
         true,
         true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Long.TYPE, "offset"), 6, -1, 7, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Boolean.TYPE, "unpaged"), 8, -1, 9, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Boolean.TYPE, "sorted"), 10, -1, 11, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            List.class,
            "orderBy",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonIgnore", Collections.EMPTY_MAP, "javax.annotation.Nonnull", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonIgnore", Collections.EMPTY_MAP, "javax.annotation.Nonnull", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonIgnore")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(Sort.Order.class, "E")
         ),
         12,
         -1,
         13,
         true,
         false
      )
   };

   public $DefaultPageable$Introspection() {
      super(
         DefaultPageable.class,
         $DefaultPageable$IntrospectionRef.$ANNOTATION_METADATA,
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
            return ((DefaultPageable)var2).getSize();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot create copy of type [io.micronaut.data.model.DefaultPageable]. Constructor contains argument [page] that is not a readable property"
            );
         case 2:
            return ((DefaultPageable)var2).getNumber();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [number] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPageable"
            );
         case 4:
            return ((DefaultPageable)var2).getSort();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot create copy of type [io.micronaut.data.model.DefaultPageable]. Constructor contains argument [page] that is not a readable property"
            );
         case 6:
            return ((DefaultPageable)var2).getOffset();
         case 7:
            throw new UnsupportedOperationException(
               "Cannot mutate property [offset] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPageable"
            );
         case 8:
            return ((DefaultPageable)var2).isUnpaged();
         case 9:
            throw new UnsupportedOperationException(
               "Cannot mutate property [unpaged] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPageable"
            );
         case 10:
            return ((DefaultPageable)var2).isSorted();
         case 11:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sorted] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPageable"
            );
         case 12:
            return ((DefaultPageable)var2).getOrderBy();
         case 13:
            throw new UnsupportedOperationException(
               "Cannot mutate property [orderBy] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPageable"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -1207110587:
            if (var1.equals("orderBy")) {
               return 6;
            }
            break;
         case -1034364087:
            if (var1.equals("number")) {
               return 1;
            }
            break;
         case -1019779949:
            if (var1.equals("offset")) {
               return 3;
            }
            break;
         case -896593219:
            if (var1.equals("sorted")) {
               return 5;
            }
            break;
         case -280618820:
            if (var1.equals("unpaged")) {
               return 4;
            }
            break;
         case 3530753:
            if (var1.equals("size")) {
               return 0;
            }
            break;
         case 3536286:
            if (var1.equals("sort")) {
               return 2;
            }
      }

      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultPageable(var1[0], var1[1], (Sort)var1[2]);
   }
}
