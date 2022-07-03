package com.example;

import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.DataType;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $Address$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES;
   private static final int[] INDEX_1 = new int[]{0};
   private static final int[] INDEX_2 = new int[]{0, 1, 2};

   static {
      Map var0;
      $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(
            Argument.of(
               Integer.TYPE,
               "id",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.INTEGER, "value", "id")),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "com.github.tivrfoa.mapresultset.api.Id",
                     Collections.EMPTY_MAP,
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
               String.class,
               "street",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.STRING, "value", "street")),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.STRING, "value", "street")),
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
               List.class,
               "listPerson",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("value", "address_person")),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "com.github.tivrfoa.mapresultset.api.ManyToMany",
                     Collections.EMPTY_MAP,
                     "io.micronaut.data.annotation.MappedProperty",
                     AnnotationUtil.mapOf("value", "address_person"),
                     "io.micronaut.data.annotation.Relation",
                     AnnotationUtil.mapOf("mappedBy", "addresses", "value", "MANY_TO_MANY")
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               Argument.ofTypeVariable(
                  Person.class,
                  "E",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.annotation.Introspected",
                        AnnotationUtil.mapOf(
                           "excludedAnnotations",
                           $micronaut_load_class_value_0(),
                           "indexed",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_1()),
                                 var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Introspected$IndexedAnnotation")
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_2()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_6(), "member", "value"),
                                 var0
                              )
                           }
                        ),
                        "io.micronaut.data.annotation.MappedEntity",
                        AnnotationUtil.mapOf("value", "person")
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.annotation.Introspected",
                        AnnotationUtil.mapOf(
                           "excludedAnnotations",
                           new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                           "indexed",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_1()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_2()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_6(), "member", "value"),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5(), "member", "value"),
                                 var0
                              )
                           }
                        )
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.annotation.Introspected",
                        AnnotationUtil.mapOf(
                           "excludedAnnotations",
                           new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                           "indexed",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_1()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_2()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_6(), "member", "value"),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5(), "member", "value"),
                                 var0
                              )
                           }
                        )
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.annotation.Introspected",
                        AnnotationUtil.mapOf(
                           "excludedAnnotations",
                           $micronaut_load_class_value_0(),
                           "indexed",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_1()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_2()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_3()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_4()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_5()),
                                 var0
                              ),
                              new AnnotationValue(
                                 "io.micronaut.core.annotation.Introspected$IndexedAnnotation",
                                 AnnotationUtil.mapOf("annotation", $micronaut_load_class_value_6(), "member", "value"),
                                 var0
                              )
                           }
                        ),
                        "io.micronaut.data.annotation.MappedEntity",
                        AnnotationUtil.mapOf("value", "person")
                     ),
                     AnnotationUtil.mapOf("io.micronaut.core.annotation.Introspected", AnnotationUtil.internListOf("io.micronaut.data.annotation.MappedEntity")),
                     false,
                     true
                  )
               )
            ),
            4,
            5,
            -1,
            false,
            true
         )
      };
   }

   public $Address$Introspection() {
      super(Address.class, $Address$IntrospectionRef.$ANNOTATION_METADATA, null, null, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((Address)var2).getId();
         case 1:
            ((Address)var2).setId((Integer)var3);
            return null;
         case 2:
            return ((Address)var2).getStreet();
         case 3:
            ((Address)var2).setStreet((String)var3);
            return null;
         case 4:
            return ((Address)var2).getListPerson();
         case 5:
            ((Address)var2).setListPerson((List<Person>)var3);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -891990013:
            if (var1.equals("street")) {
               return 1;
            }
            break;
         case 3355:
            if (var1.equals("id")) {
               return 0;
            }
            break;
         case 265275795:
            if (var1.equals("listPerson")) {
               return 2;
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
                  case -891990013:
                     if (var2.equals("street")) {
                        return this.getPropertyByIndex(1);
                     }
                     break;
                  case 3355:
                     if (var2.equals("id")) {
                        return this.getPropertyByIndex(0);
                     }
                     break;
                  case 463888896:
                     if (var2.equals("address_person")) {
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
      return new Address();
   }
}
