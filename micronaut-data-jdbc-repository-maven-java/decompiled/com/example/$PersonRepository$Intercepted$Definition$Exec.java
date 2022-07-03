package com.example;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.PageableRepository;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
final class $PersonRepository$Intercepted$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;

   static {
      Map var0;
      Map var1;
      Map var2;
      Map var3;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            PersonRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `person` (`name`,`born_timestamp`) VALUES (?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("property", "name"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.data.intercept.annotation.DataMethodQueryParameter")
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `person` (`name`,`born_timestamp`) VALUES (?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "save",
            Argument.of(Person.class, "com.example.Person"),
            new Argument[]{
               Argument.of(
                  String.class,
                  "name",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "javax.validation.constraints.NotBlank",
                                 Collections.EMPTY_MAP,
                                 var1 = AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.NotBlank")
                              )
                           }
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotBlank", Collections.EMPTY_MAP, var1)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotBlank")),
                     false,
                     true
                  ),
                  null
               ),
               Argument.of(Timestamp.class, "bornTimestamp")
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            PersonRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `person` SET `name`=?,`born_timestamp`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "person",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           ),
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var0)
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `person` SET `name`=?,`born_timestamp`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "person",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           ),
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var0)
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "update",
            Argument.of(Person.class, "com.example.Person"),
            new Argument[]{Argument.of(Person.class, "person")},
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            PersonRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf(
                        "value",
                        "SELECT person_.`id`,person_.`name`,person_.`born_timestamp`,person_phones_.`id` AS phones_id,person_phones_.`number` AS phones_number,person_phones_.`person_id` AS phones_person_id,person_addresses_.`id` AS addresses_id,person_addresses_.`street` AS addresses_street FROM `person` person_ INNER JOIN `Phone` person_phones_ ON person_.`id`=person_phones_.`person_id` INNER JOIN `person_address` person_addresses_person_address_ ON person_.`id`=person_addresses_person_address_.`person_id`  INNER JOIN `address` person_addresses_ ON person_addresses_person_address_.`address_id`=person_addresses_.`id`"
                     ),
                     "io.micronaut.data.annotation.repeatable.JoinSpecifications",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.annotation.Join",
                              AnnotationUtil.mapOf("type", "FETCH", "value", "addresses"),
                              var2 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.data.annotation.Join")
                           ),
                           new AnnotationValue("io.micronaut.data.annotation.Join", AnnotationUtil.mapOf("type", "FETCH", "value", "phones"), var2)
                        }
                     ),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf(
                        "value",
                        "SELECT person_.`id`,person_.`name`,person_.`born_timestamp`,person_phones_.`id` AS phones_id,person_phones_.`number` AS phones_number,person_phones_.`person_id` AS phones_person_id,person_addresses_.`id` AS addresses_id,person_addresses_.`street` AS addresses_street FROM `person` person_ INNER JOIN `Phone` person_phones_ ON person_.`id`=person_phones_.`person_id` INNER JOIN `person_address` person_addresses_person_address_ ON person_.`id`=person_addresses_person_address_.`person_id`  INNER JOIN `address` person_addresses_ ON person_addresses_person_address_.`address_id`=person_addresses_.`id`"
                     ),
                     "io.micronaut.data.annotation.repeatable.JoinSpecifications",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.data.annotation.Join", AnnotationUtil.mapOf("type", "FETCH", "value", "addresses"), var2),
                           new AnnotationValue("io.micronaut.data.annotation.Join", AnnotationUtil.mapOf("type", "FETCH", "value", "phones"), var2)
                        }
                     ),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "list",
            Argument.of(List.class, "java.util.List", null, Argument.ofTypeVariable(Person.class, "E")),
            null,
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            PageableRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "sort",
                        "sort"
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "sort",
                        "sort"
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "findAll",
            Argument.of(Iterable.class, "java.lang.Iterable", null, Argument.ofTypeVariable(Person.class, "T")),
            new Argument[]{
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
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            PageableRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf(
                        "countQuery",
                        "SELECT COUNT(*) FROM `person` person_",
                        "value",
                        "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_"
                     ),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_4()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "pageable",
                        "pageable",
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "sort",
                        "pageable"
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf(
                        "countQuery",
                        "SELECT COUNT(*) FROM `person` person_",
                        "value",
                        "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_"
                     ),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_4()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "pageable",
                        "pageable",
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "sort",
                        "pageable"
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "findAll",
            Argument.of(Page.class, "io.micronaut.data.model.Page", null, Argument.ofTypeVariable(Person.class, "T")),
            new Argument[]{
               Argument.of(
                  Pageable.class,
                  "pageable",
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
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `person` (`name`,`born_timestamp`) VALUES (?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_5()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `person` (`name`,`born_timestamp`) VALUES (?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_5()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "save",
            Argument.ofTypeVariable(Person.class, "com.example.Person", "S", null, null),
            new Argument[]{
               Argument.ofTypeVariable(
                  Person.class,
                  "entity",
                  "S",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.Valid",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "javax.validation.constraints.NotNull",
                                 Collections.EMPTY_MAP,
                                 var3 = AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.NotNull")
                              )
                           }
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.Valid",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  null
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `person` SET `name`=?,`born_timestamp`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"UPDATE `person` SET `name`=", ",`born_timestamp`=", " WHERE (`id` = ", ")"},
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_6()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "name"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "bornTimestamp"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "id"),
                              var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `person` SET `name`=?,`born_timestamp`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"UPDATE `person` SET `name`=", ",`born_timestamp`=", " WHERE (`id` = ", ")"},
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_6()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "name"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "bornTimestamp"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "id"),
                              var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "updateAll",
            Argument.of(Iterable.class, "java.lang.Iterable", null, Argument.ofTypeVariable(Person.class, "T")),
            new Argument[]{
               Argument.of(
                  Iterable.class,
                  "entities",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.Valid",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.Valid",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  Argument.ofTypeVariable(Person.class, "T")
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `person` (`name`,`born_timestamp`) VALUES (?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_7()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `person` (`name`,`born_timestamp`) VALUES (?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_7()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "bornTimestamp"), var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "saveAll",
            Argument.of(Iterable.class, "java.lang.Iterable", null, Argument.ofTypeVariable(Person.class, "T")),
            new Argument[]{
               Argument.of(
                  Iterable.class,
                  "entities",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.Valid",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.Valid",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  Argument.ofTypeVariable(Person.class, "T")
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_ WHERE (person_.`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_8()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.INTEGER, "parameterIndex", 0, "property", "id"),
                              var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_ WHERE (person_.`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_8()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.INTEGER, "parameterIndex", 0, "property", "id"),
                              var0
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "findById",
            Argument.of(Optional.class, "java.util.Optional", null, Argument.ofTypeVariable(Person.class, "T")),
            new Argument[]{
               Argument.ofTypeVariable(
                  Long.class,
                  "id",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  null
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT TRUE FROM `person` person_ WHERE (person_.`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_9()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.INTEGER, "parameterIndex", 0, "property", "id"),
                              var0
                           )
                        },
                        "resultDataType",
                        DataType.BOOLEAN,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_10()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT TRUE FROM `person` person_ WHERE (person_.`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_9()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.INTEGER, "parameterIndex", 0, "property", "id"),
                              var0
                           )
                        },
                        "resultDataType",
                        DataType.BOOLEAN,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_10()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "existsById",
            Argument.BOOLEAN,
            new Argument[]{
               Argument.ofTypeVariable(
                  Long.class,
                  "id",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  null
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT person_.`id`,person_.`name`,person_.`born_timestamp` FROM `person` person_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "findAll",
            Argument.of(Iterable.class, "java.lang.Iterable", null, Argument.ofTypeVariable(Person.class, "T")),
            null,
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT COUNT(*) FROM `person` person_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_11()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.LONG,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_12()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT COUNT(*) FROM `person` person_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_11()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.LONG,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_12()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "count",
            Argument.LONG,
            null,
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person`  WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_13()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.INTEGER, "parameterIndex", 0, "property", "id"),
                              var0
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person`  WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_13()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.INTEGER, "parameterIndex", 0, "property", "id"),
                              var0
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "deleteById",
            Argument.VOID,
            new Argument[]{
               Argument.ofTypeVariable(
                  Long.class,
                  "id",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  null
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person`  WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_15()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var0)
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person`  WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_15()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var0)
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "delete",
            Argument.VOID,
            new Argument[]{
               Argument.ofTypeVariable(
                  Person.class,
                  "entity",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  null
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person`  WHERE (`id` IN (?))"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"DELETE  FROM `person`  WHERE (`id` IN (", "))"},
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_13()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "id"),
                              var0
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person`  WHERE (`id` IN (?))"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"DELETE  FROM `person`  WHERE (`id` IN (", "))"},
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_13()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "id"),
                              var0
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "deleteAll",
            Argument.VOID,
            new Argument[]{
               Argument.of(
                  Iterable.class,
                  "entities",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var3)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  Argument.ofTypeVariable(Person.class, "T")
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person` "),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_13()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `person` "),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_13()},
                        "opType",
                        DataMethod.OperationType.DELETE,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "deleteAll",
            Argument.VOID,
            null,
            true,
            false
         )
      };
   }

   public $PersonRepository$Intercepted$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((PersonRepository)var2).save((String)var3[0], (Timestamp)var3[1]);
         case 1:
            return ((PersonRepository)var2).update((Person)var3[0]);
         case 2:
            return ((PersonRepository)var2).list();
         case 3:
            return ((PageableRepository)var2).findAll((Sort)var3[0]);
         case 4:
            return ((PageableRepository)var2).findAll((Pageable)var3[0]);
         case 5:
            return ((CrudRepository)var2).save(var3[0]);
         case 6:
            return ((CrudRepository)var2).updateAll((Iterable)var3[0]);
         case 7:
            return ((CrudRepository)var2).saveAll((Iterable)var3[0]);
         case 8:
            return ((CrudRepository)var2).findById(var3[0]);
         case 9:
            return ((CrudRepository)var2).existsById(var3[0]);
         case 10:
            return ((CrudRepository)var2).findAll();
         case 11:
            return ((CrudRepository)var2).count();
         case 12:
            ((CrudRepository)var2).deleteById(var3[0]);
            return null;
         case 13:
            ((CrudRepository)var2).delete(var3[0]);
            return null;
         case 14:
            ((CrudRepository)var2).deleteAll((Iterable)var3[0]);
            return null;
         case 15:
            ((CrudRepository)var2).deleteAll();
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(PersonRepository.class, "save", String.class, Timestamp.class);
         case 1:
            return ReflectionUtils.getRequiredMethod(PersonRepository.class, "update", Person.class);
         case 2:
            return ReflectionUtils.getRequiredMethod(PersonRepository.class, "list", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 3:
            return ReflectionUtils.getRequiredMethod(PageableRepository.class, "findAll", Sort.class);
         case 4:
            return ReflectionUtils.getRequiredMethod(PageableRepository.class, "findAll", Pageable.class);
         case 5:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "save", Object.class);
         case 6:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "updateAll", Iterable.class);
         case 7:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "saveAll", Iterable.class);
         case 8:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "findById", Object.class);
         case 9:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "existsById", Object.class);
         case 10:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "findAll", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 11:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "count", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 12:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "deleteById", Object.class);
         case 13:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "delete", Object.class);
         case 14:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "deleteAll", Iterable.class);
         case 15:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "deleteAll", ReflectionUtils.EMPTY_CLASS_ARRAY);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   private final ExecutableMethod getMethod(String var1, Class[] var2) {
      switch(var1.hashCode()) {
         case -1949226984:
            if (this.methodAtIndexMatches(6, var1, var2)) {
               return this.getExecutableMethodByIndex(6);
            }
            break;
         case -1335458389:
            if (this.methodAtIndexMatches(13, var1, var2)) {
               return this.getExecutableMethodByIndex(13);
            }
            break;
         case -853211864:
            if (this.methodAtIndexMatches(3, var1, var2)) {
               return this.getExecutableMethodByIndex(3);
            }

            if (this.methodAtIndexMatches(4, var1, var2)) {
               return this.getExecutableMethodByIndex(4);
            }

            if (this.methodAtIndexMatches(10, var1, var2)) {
               return this.getExecutableMethodByIndex(10);
            }
            break;
         case -838846263:
            if (this.methodAtIndexMatches(1, var1, var2)) {
               return this.getExecutableMethodByIndex(1);
            }
            break;
         case -679722709:
            if (this.methodAtIndexMatches(8, var1, var2)) {
               return this.getExecutableMethodByIndex(8);
            }
            break;
         case -358737930:
            if (this.methodAtIndexMatches(14, var1, var2)) {
               return this.getExecutableMethodByIndex(14);
            }

            if (this.methodAtIndexMatches(15, var1, var2)) {
               return this.getExecutableMethodByIndex(15);
            }
            break;
         case 3322014:
            if (this.methodAtIndexMatches(2, var1, var2)) {
               return this.getExecutableMethodByIndex(2);
            }
            break;
         case 3522941:
            if (this.methodAtIndexMatches(0, var1, var2)) {
               return this.getExecutableMethodByIndex(0);
            }

            if (this.methodAtIndexMatches(5, var1, var2)) {
               return this.getExecutableMethodByIndex(5);
            }
            break;
         case 94851343:
            if (this.methodAtIndexMatches(11, var1, var2)) {
               return this.getExecutableMethodByIndex(11);
            }
            break;
         case 205272654:
            if (this.methodAtIndexMatches(9, var1, var2)) {
               return this.getExecutableMethodByIndex(9);
            }
            break;
         case 1764067357:
            if (this.methodAtIndexMatches(12, var1, var2)) {
               return this.getExecutableMethodByIndex(12);
            }
            break;
         case 1872786148:
            if (this.methodAtIndexMatches(7, var1, var2)) {
               return this.getExecutableMethodByIndex(7);
            }
      }

      return null;
   }
}
