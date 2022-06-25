package com.example;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.data.model.DataType;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $Genre$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Long.class,
            "id",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.LONG, "value", "id")),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.data.annotation.GeneratedValue",
                  AnnotationUtil.mapOf("value", "AUTO"),
                  "io.micronaut.data.annotation.Id",
                  Collections.EMPTY_MAP,
                  "io.micronaut.data.annotation.MappedProperty",
                  AnnotationUtil.mapOf("type", DataType.LONG, "value", "id")
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         0,
         1,
         -1,
         false,
         true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            String.class,
            "name",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.STRING, "value", "name")),
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
               AnnotationUtil.mapOf(
                  "io.micronaut.data.annotation.MappedProperty",
                  AnnotationUtil.mapOf("type", DataType.STRING, "value", "name"),
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
         2,
         3,
         -1,
         false,
         true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Double.TYPE,
            "value",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.DOUBLE, "value", "value")),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.DOUBLE, "value", "value")),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         4,
         5,
         -1,
         false,
         true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            String.class,
            "country",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.STRING, "value", "country")),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.STRING, "value", "country")),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         6,
         7,
         -1,
         false,
         true
      )
   };
   private static final int[] INDEX_1 = new int[]{0};
   private static final int[] INDEX_2 = new int[]{0, 1, 2, 3};
   private static final int[] INDEX_3 = new int[]{1};

   public $Genre$Introspection() {
      super(Genre.class, $Genre$IntrospectionRef.$ANNOTATION_METADATA, null, null, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((Genre)var2).getId();
         case 1:
            ((Genre)var2).setId((Long)var3);
            return null;
         case 2:
            return ((Genre)var2).getName();
         case 3:
            ((Genre)var2).setName((String)var3);
            return null;
         case 4:
            return ((Genre)var2).getValue();
         case 5:
            ((Genre)var2).setValue((Double)var3);
            return null;
         case 6:
            return ((Genre)var2).getCountry();
         case 7:
            ((Genre)var2).setCountry((String)var3);
            return null;
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
            break;
         case 111972721:
            if (var1.equals("value")) {
               return 2;
            }
            break;
         case 957831062:
            if (var1.equals("country")) {
               return 3;
            }
      }

      return -1;
   }

   @Override
   protected final BeanProperty findIndexedProperty(Class var1, String var2) {
      String var3 = var1.getName();
      switch(var3.hashCode()) {
         case -1213318794:
            if (var3.equals("io.micronaut.data.annotation.Id") && var2 == null) {
               return this.getPropertyByIndex(0);
            }
            break;
         case -11574982:
            if (var3.equals("javax.validation.Constraint") && var2 == null) {
               return this.getPropertyByIndex(1);
            }
            break;
         case 295147779:
            if (var3.equals("io.micronaut.data.annotation.MappedProperty") && var2 != null) {
               switch(var2.hashCode()) {
                  case 3355:
                     if (var2.equals("id")) {
                        return this.getPropertyByIndex(0);
                     }
                     break;
                  case 3373707:
                     if (var2.equals("name")) {
                        return this.getPropertyByIndex(1);
                     }
                     break;
                  case 111972721:
                     if (var2.equals("value")) {
                        return this.getPropertyByIndex(2);
                     }
                     break;
                  case 957831062:
                     if (var2.equals("country")) {
                        return this.getPropertyByIndex(3);
                     }
               }
            }
      }

      return null;
   }

   @Override
   public final Collection getIndexedProperties(Class var1) {
      String var2 = var1.getName();
      switch(var2.hashCode()) {
         case -1213318794:
            if (var2.equals("io.micronaut.data.annotation.Id")) {
               return this.getBeanPropertiesIndexedSubset(INDEX_1);
            }
            break;
         case -11574982:
            if (var2.equals("javax.validation.Constraint")) {
               return this.getBeanPropertiesIndexedSubset(INDEX_3);
            }
            break;
         case 295147779:
            if (var2.equals("io.micronaut.data.annotation.MappedProperty")) {
               return this.getBeanPropertiesIndexedSubset(INDEX_2);
            }
      }

      return Collections.emptyList();
   }

   @Override
   public Object instantiate() {
      return new Genre();
   }
}
