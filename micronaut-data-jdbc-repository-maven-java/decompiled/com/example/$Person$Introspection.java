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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $Person$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES;
   private static final int[] INDEX_1 = new int[]{0};
   private static final int[] INDEX_2 = new int[]{0, 1, 2, 3, 4};

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
                     "io.micronaut.data.annotation.GeneratedValue",
                     AnnotationUtil.mapOf("value", "AUTO"),
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
               "name",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.STRING, "value", "name")),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.STRING, "value", "name")),
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
               Timestamp.class,
               "bornTimestamp",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.TIMESTAMP)),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("type", DataType.TIMESTAMP, "value", "born_timestamp")
                  ),
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
               List.class,
               "phones",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("value", "person_phone")),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.MappedProperty",
                     AnnotationUtil.mapOf("value", "person_phone"),
                     "io.micronaut.data.annotation.Relation",
                     AnnotationUtil.mapOf("mappedBy", "person", "value", "ONE_TO_MANY")
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               Argument.ofTypeVariable(
                  Phone.class,
                  "E",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "com.github.tivrfoa.mapresultset.api.Table",
                        Collections.EMPTY_MAP,
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
                        AnnotationUtil.mapOf("value", "Phone")
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
                        "com.github.tivrfoa.mapresultset.api.Table",
                        Collections.EMPTY_MAP,
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
                        AnnotationUtil.mapOf("value", "Phone")
                     ),
                     AnnotationUtil.mapOf("io.micronaut.core.annotation.Introspected", AnnotationUtil.internListOf("io.micronaut.data.annotation.MappedEntity")),
                     false,
                     true
                  )
               )
            ),
            6,
            7,
            -1,
            false,
            true
         ),
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(
            Argument.of(
               List.class,
               "addresses",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.data.annotation.MappedProperty", AnnotationUtil.mapOf("value", "person_address")),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "com.github.tivrfoa.mapresultset.api.ManyToMany",
                     Collections.EMPTY_MAP,
                     "io.micronaut.data.annotation.MappedProperty",
                     AnnotationUtil.mapOf("value", "person_address"),
                     "io.micronaut.data.annotation.Relation",
                     AnnotationUtil.mapOf("mappedBy", "listPerson", "value", "MANY_TO_MANY")
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               Argument.ofTypeVariable(
                  Address.class,
                  "E",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "com.github.tivrfoa.mapresultset.api.Table",
                        AnnotationUtil.mapOf("name", "address"),
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
                        AnnotationUtil.mapOf("value", "address")
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
                        "com.github.tivrfoa.mapresultset.api.Table",
                        AnnotationUtil.mapOf("name", "address"),
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
                        AnnotationUtil.mapOf("value", "address")
                     ),
                     AnnotationUtil.mapOf("io.micronaut.core.annotation.Introspected", AnnotationUtil.internListOf("io.micronaut.data.annotation.MappedEntity")),
                     false,
                     true
                  )
               )
            ),
            8,
            9,
            -1,
            false,
            true
         )
      };
   }

   public $Person$Introspection() {
      super(Person.class, $Person$IntrospectionRef.$ANNOTATION_METADATA, null, null, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((Person)var2).getId();
         case 1:
            ((Person)var2).setId((Integer)var3);
            return null;
         case 2:
            return ((Person)var2).getName();
         case 3:
            ((Person)var2).setName((String)var3);
            return null;
         case 4:
            return ((Person)var2).getBornTimestamp();
         case 5:
            ((Person)var2).setBornTimestamp((Timestamp)var3);
            return null;
         case 6:
            return ((Person)var2).getPhones();
         case 7:
            ((Person)var2).setPhones((List<Phone>)var3);
            return null;
         case 8:
            return ((Person)var2).getAddresses();
         case 9:
            ((Person)var2).setAddresses((List<Address>)var3);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -989040443:
            if (var1.equals("phones")) {
               return 3;
            }
            break;
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
         case 874544034:
            if (var1.equals("addresses")) {
               return 4;
            }
            break;
         case 1683009485:
            if (var1.equals("bornTimestamp")) {
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
                  case -311646204:
                     if (var2.equals("person_phone")) {
                        return this.getPropertyByIndex(3);
                     }
                     break;
                  case -207513696:
                     if (var2.equals("born_timestamp")) {
                        return this.getPropertyByIndex(2);
                     }
                     break;
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
                  case 603502858:
                     if (var2.equals("person_address")) {
                        return this.getPropertyByIndex(4);
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
      return new Person();
   }
}
