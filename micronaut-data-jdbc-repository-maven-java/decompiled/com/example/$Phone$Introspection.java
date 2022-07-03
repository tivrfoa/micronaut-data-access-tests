package com.example;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.DataType;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $Phone$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Integer.TYPE,
            "id",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.INTEGER, "value", "id")),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.data.annotation.Id",
                  Collections.EMPTY_MAP,
                  "io.micronaut.data.annotation.MappedProperty",
                  AnnotationUtil.mapOf("type", DataType.INTEGER, "value", "id")
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
            Integer.TYPE,
            "number",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.INTEGER, "value", "number")),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.INTEGER, "value", "number")),
               Collections.EMPTY_MAP,
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
            Person.class,
            "person",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JsonIgnore",
                  Collections.EMPTY_MAP,
                  "io.micronaut.data.annotation.MappedProperty",
                  AnnotationUtil.mapOf("type", DataType.ENTITY, "value", "person_id")
               ),
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JsonIgnore",
                  Collections.EMPTY_MAP,
                  "io.micronaut.data.annotation.MappedProperty",
                  AnnotationUtil.mapOf("type", DataType.ENTITY, "value", "person_id"),
                  "io.micronaut.data.annotation.Relation",
                  AnnotationUtil.mapOf("value", "MANY_TO_ONE")
               ),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonIgnore")
               ),
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
      )
   };
   private static final int[] INDEX_1 = new int[]{0};
   private static final int[] INDEX_2 = new int[]{0, 1, 2};

   public $Phone$Introspection() {
      super(Phone.class, $Phone$IntrospectionRef.$ANNOTATION_METADATA, null, null, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((Phone)var2).getId();
         case 1:
            ((Phone)var2).setId((Integer)var3);
            return null;
         case 2:
            return ((Phone)var2).getNumber();
         case 3:
            ((Phone)var2).setNumber((Integer)var3);
            return null;
         case 4:
            return ((Phone)var2).getPerson();
         case 5:
            ((Phone)var2).setPerson((Person)var3);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -1034364087:
            if (var1.equals("number")) {
               return 1;
            }
            break;
         case -991716523:
            if (var1.equals("person")) {
               return 2;
            }
            break;
         case 3355:
            if (var1.equals("id")) {
               return 0;
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
         case 295147779:
            if (var3.equals("io.micronaut.data.annotation.MappedProperty") && var2 != null) {
               switch(var2.hashCode()) {
                  case -1034364087:
                     if (var2.equals("number")) {
                        return this.getPropertyByIndex(1);
                     }
                     break;
                  case 3355:
                     if (var2.equals("id")) {
                        return this.getPropertyByIndex(0);
                     }
                     break;
                  case 853187141:
                     if (var2.equals("person_id")) {
                        return this.getPropertyByIndex(2);
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
         case 295147779:
            if (var2.equals("io.micronaut.data.annotation.MappedProperty")) {
               return this.getBeanPropertiesIndexedSubset(INDEX_2);
            }
      }

      return Collections.emptyList();
   }

   @Override
   public Object instantiate() {
      return new Phone();
   }
}
