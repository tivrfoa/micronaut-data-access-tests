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
final class $DefaultPage$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AnnotationMetadata $FIELD_CONSTRUCTOR_ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "com.fasterxml.jackson.annotation.JsonCreator",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Creator",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.ReflectiveAccess",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "com.fasterxml.jackson.annotation.JsonCreator",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Creator",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.ReflectiveAccess",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonCreator")),
      false,
      true
   );
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(
         List.class,
         "content",
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "content")),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "content")),
            AnnotationUtil.mapOf(
               "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
            ),
            false,
            true
         ),
         Argument.ofTypeVariable(Object.class, "E", "T")
      ),
      Argument.of(
         Pageable.class,
         "pageable",
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "pageable")),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "pageable")),
            AnnotationUtil.mapOf(
               "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
            ),
            false,
            true
         ),
         null
      ),
      Argument.of(
         Long.TYPE,
         "totalSize",
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "totalSize")),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JsonProperty", AnnotationUtil.mapOf("value", "totalSize")),
            AnnotationUtil.mapOf(
               "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonProperty")
            ),
            false,
            true
         ),
         null
      )
   };
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Long.TYPE,
            "totalSize",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
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
            List.class,
            "content",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP, "javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP, "javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(Object.class, "E", "T")
         ),
         2,
         -1,
         3,
         true,
         true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Pageable.class,
            "pageable",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP, "javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("io.micronaut.core.annotation.ReflectiveAccess", Collections.EMPTY_MAP, "javax.annotation.Nonnull", Collections.EMPTY_MAP),
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
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Integer.TYPE, "pageNumber"), 6, -1, 7, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Long.TYPE, "offset"), 8, -1, 9, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Integer.TYPE, "size"), 10, -1, 11, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Boolean.TYPE, "empty"), 12, -1, 13, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Sort.class,
            "sort",
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
            null
         ),
         14,
         -1,
         15,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Integer.TYPE, "numberOfElements"), 16, -1, 17, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Integer.TYPE, "totalPages"), 18, -1, 19, true, false)
   };

   public $DefaultPage$Introspection() {
      super(
         DefaultPage.class,
         $DefaultPage$IntrospectionRef.$ANNOTATION_METADATA,
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
            return ((DefaultPage)var2).getTotalSize();
         case 1:
            DefaultPage var4 = (DefaultPage)var2;
            return new DefaultPage(var4.getContent(), var4.getPageable(), (Long)var3);
         case 2:
            return ((DefaultPage)var2).getContent();
         case 3:
            DefaultPage var5 = (DefaultPage)var2;
            return new DefaultPage((List)var3, var5.getPageable(), var5.getTotalSize());
         case 4:
            return ((DefaultPage)var2).getPageable();
         case 5:
            DefaultPage var6 = (DefaultPage)var2;
            return new DefaultPage(var6.getContent(), (Pageable)var3, var6.getTotalSize());
         case 6:
            return ((DefaultPage)var2).getPageNumber();
         case 7:
            throw new UnsupportedOperationException(
               "Cannot mutate property [pageNumber] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPage"
            );
         case 8:
            return ((DefaultPage)var2).getOffset();
         case 9:
            throw new UnsupportedOperationException(
               "Cannot mutate property [offset] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPage"
            );
         case 10:
            return ((DefaultPage)var2).getSize();
         case 11:
            throw new UnsupportedOperationException(
               "Cannot mutate property [size] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPage"
            );
         case 12:
            return ((DefaultPage)var2).isEmpty();
         case 13:
            throw new UnsupportedOperationException(
               "Cannot mutate property [empty] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPage"
            );
         case 14:
            return ((DefaultPage)var2).getSort();
         case 15:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sort] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPage"
            );
         case 16:
            return ((DefaultPage)var2).getNumberOfElements();
         case 17:
            throw new UnsupportedOperationException(
               "Cannot mutate property [numberOfElements] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPage"
            );
         case 18:
            return ((DefaultPage)var2).getTotalPages();
         case 19:
            throw new UnsupportedOperationException(
               "Cannot mutate property [totalPages] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.DefaultPage"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -1019779949:
            if (var1.equals("offset")) {
               return 4;
            }
            break;
         case -719810848:
            if (var1.equals("totalPages")) {
               return 9;
            }
            break;
         case -577311387:
            if (var1.equals("totalSize")) {
               return 0;
            }
            break;
         case 3530753:
            if (var1.equals("size")) {
               return 5;
            }
            break;
         case 3536286:
            if (var1.equals("sort")) {
               return 7;
            }
            break;
         case 96634189:
            if (var1.equals("empty")) {
               return 6;
            }
            break;
         case 859838569:
            if (var1.equals("pageable")) {
               return 2;
            }
            break;
         case 951530617:
            if (var1.equals("content")) {
               return 1;
            }
            break;
         case 1144767160:
            if (var1.equals("pageNumber")) {
               return 3;
            }
            break;
         case 1203985431:
            if (var1.equals("numberOfElements")) {
               return 8;
            }
      }

      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultPage((List)var1[0], (Pageable)var1[1], var1[2]);
   }
}
