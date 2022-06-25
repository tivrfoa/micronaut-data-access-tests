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
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
final class $GenreDao$Intercepted$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;
   private final boolean $interceptable;

   static {
      Map var0;
      Map var1;
      Map var2;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            GenreDao.class,
            new AnnotationMetadataHierarchy(
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.InterceptorBindingDefinitions",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                                 "kind",
                                 "AROUND",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                              ),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.aop.InterceptorBinding")
                           )
                        }
                     ),
                     "io.micronaut.transaction.annotation.TransactionalAdvice",
                     Collections.EMPTY_MAP,
                     "javax.transaction.Transactional",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.Internal",
                     Collections.EMPTY_MAP,
                     "javax.interceptor.InterceptorBinding",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.Internal",
                     Collections.EMPTY_MAP,
                     "javax.interceptor.InterceptorBinding",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.InterceptorBindingDefinitions",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                                 "kind",
                                 "AROUND",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                              ),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.transaction.annotation.TransactionalAdvice",
                     Collections.EMPTY_MAP,
                     "javax.transaction.Transactional",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     AnnotationUtil.internListOf("io.micronaut.transaction.annotation.TransactionalAdvice"),
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.internListOf("io.micronaut.transaction.annotation.TransactionalAdvice"),
                     "io.micronaut.core.annotation.Internal",
                     AnnotationUtil.internListOf("io.micronaut.transaction.annotation.TransactionalAdvice"),
                     "javax.interceptor.InterceptorBinding",
                     AnnotationUtil.internListOf("javax.transaction.Transactional")
                  ),
                  false,
                  true
               )
            ),
            "listGenres",
            Argument.of(List.class, "java.util.List", null, Argument.ofTypeVariable(Genre.class, "E")),
            null,
            false,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `genre` (`name`,`value`,`country`) VALUES (?,?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("property", "name"),
                              var1 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.data.intercept.annotation.DataMethodQueryParameter")
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "value"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "country"), var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `genre` (`name`,`value`,`country`) VALUES (?,?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "value"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "country"), var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
            Argument.ofTypeVariable(Genre.class, "com.example.Genre", "S", null, null),
            new Argument[]{
               Argument.ofTypeVariable(
                  Genre.class,
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
                                 var2 = AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.NotNull")
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `genre` SET `name`=?,`value`=?,`country`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_4()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "value"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "country"), var1
                           ),
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var1)
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `genre` SET `name`=?,`value`=?,`country`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entity",
                        "entity",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_4()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "value"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "country"), var1
                           ),
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var1)
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "update",
            Argument.ofTypeVariable(Genre.class, "com.example.Genre", "S", null, null),
            new Argument[]{
               Argument.ofTypeVariable(
                  Genre.class,
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `genre` SET `name`=?,`value`=?,`country`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"UPDATE `genre` SET `name`=", ",`value`=", ",`country`=", " WHERE (`id` = ", ")"},
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_5()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "name"),
                              var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "value"),
                              var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "country"),
                              var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "id"),
                              var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "UPDATE `genre` SET `name`=?,`value`=?,`country`=? WHERE (`id` = ?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"UPDATE `genre` SET `name`=", ",`value`=", ",`country`=", " WHERE (`id` = ", ")"},
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_5()},
                        "opType",
                        DataMethod.OperationType.UPDATE,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "name"),
                              var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "value"),
                              var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "country"),
                              var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("expandable", true, "property", "id"),
                              var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
            Argument.of(Iterable.class, "java.lang.Iterable", null, Argument.ofTypeVariable(Genre.class, "T")),
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  Argument.ofTypeVariable(Genre.class, "T")
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `genre` (`name`,`value`,`country`) VALUES (?,?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_6()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "value"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "country"), var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "INSERT INTO `genre` (`name`,`value`,`country`) VALUES (?,?,?)"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_6()},
                        "opType",
                        DataMethod.OperationType.INSERT,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "name"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "value"), var1
                           ),
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "country"), var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
            Argument.of(Iterable.class, "java.lang.Iterable", null, Argument.ofTypeVariable(Genre.class, "T")),
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  Argument.ofTypeVariable(Genre.class, "T")
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf(
                        "value", "SELECT genre_.`id`,genre_.`name`,genre_.`value`,genre_.`country` FROM `genre` genre_ WHERE (genre_.`id` = ?)"
                     ),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_7()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.LONG, "parameterIndex", 0, "property", "id"),
                              var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf(
                        "value", "SELECT genre_.`id`,genre_.`name`,genre_.`value`,genre_.`country` FROM `genre` genre_ WHERE (genre_.`id` = ?)"
                     ),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_7()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "parameters",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.data.intercept.annotation.DataMethodQueryParameter",
                              AnnotationUtil.mapOf("dataType", DataType.LONG, "parameterIndex", 0, "property", "id"),
                              var1
                           )
                        },
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
            Argument.of(Optional.class, "java.util.Optional", null, Argument.ofTypeVariable(Genre.class, "T")),
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT TRUE FROM `genre` genre_ WHERE (genre_.`id` = ?)"),
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
                              AnnotationUtil.mapOf("dataType", DataType.LONG, "parameterIndex", 0, "property", "id"),
                              var1
                           )
                        },
                        "resultDataType",
                        DataType.BOOLEAN,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_9()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT TRUE FROM `genre` genre_ WHERE (genre_.`id` = ?)"),
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
                              AnnotationUtil.mapOf("dataType", DataType.LONG, "parameterIndex", 0, "property", "id"),
                              var1
                           )
                        },
                        "resultDataType",
                        DataType.BOOLEAN,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_9()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT genre_.`id`,genre_.`name`,genre_.`value`,genre_.`country` FROM `genre` genre_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_10()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     ),
                     "javax.annotation.Nonnull",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT genre_.`id`,genre_.`name`,genre_.`value`,genre_.`country` FROM `genre` genre_"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "idType",
                        "java.lang.Long",
                        "interceptor",
                        new AnnotationClassValue[]{$micronaut_load_class_value_10()},
                        "opType",
                        DataMethod.OperationType.QUERY,
                        "resultDataType",
                        DataType.ENTITY,
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
            Argument.of(Iterable.class, "java.lang.Iterable", null, Argument.ofTypeVariable(Genre.class, "T")),
            null,
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT COUNT(*) FROM `genre` genre_"),
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
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "SELECT COUNT(*) FROM `genre` genre_"),
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
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre`  WHERE (`id` = ?)"),
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
                              AnnotationUtil.mapOf("dataType", DataType.LONG, "parameterIndex", 0, "property", "id"),
                              var1
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre`  WHERE (`id` = ?)"),
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
                              AnnotationUtil.mapOf("dataType", DataType.LONG, "parameterIndex", 0, "property", "id"),
                              var1
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre`  WHERE (`id` = ?)"),
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
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var1)
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre`  WHERE (`id` = ?)"),
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
                           new AnnotationValue("io.micronaut.data.intercept.annotation.DataMethodQueryParameter", AnnotationUtil.mapOf("property", "id"), var1)
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
                  Genre.class,
                  "entity",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
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
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre`  WHERE (`id` IN (?))"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"DELETE  FROM `genre`  WHERE (`id` IN (", "))"},
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
                              var1
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre`  WHERE (`id` IN (?))"),
                     "io.micronaut.data.intercept.annotation.DataMethod",
                     AnnotationUtil.mapOf(
                        "entities",
                        "entities",
                        "expandableQuery",
                        new String[]{"DELETE  FROM `genre`  WHERE (`id` IN (", "))"},
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
                              var1
                           )
                        },
                        "resultType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_14()},
                        "rootEntity",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.annotation.Nonnull",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var2)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
                     false,
                     true
                  ),
                  Argument.ofTypeVariable(Genre.class, "T")
               )
            },
            true,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            CrudRepository.class,
            new AnnotationMetadataHierarchy(
               $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre` "),
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
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.data.annotation.Query",
                     AnnotationUtil.mapOf("value", "DELETE  FROM `genre` "),
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
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()}
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

   public $GenreDao$Intercepted$Definition$Exec() {
      this(false);
   }

   public $GenreDao$Intercepted$Definition$Exec(boolean var1) {
      super($METHODS_REFERENCES);
      this.$interceptable = var1;
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            if (this.$interceptable && var2 instanceof GenreDao$Intercepted) {
               return ((GenreDao$Intercepted)var2).$$access$$listGenres();
            }

            return ((GenreDao)var2).listGenres();
         case 1:
            return ((CrudRepository)var2).save(var3[0]);
         case 2:
            return ((CrudRepository)var2).update(var3[0]);
         case 3:
            return ((CrudRepository)var2).updateAll((Iterable)var3[0]);
         case 4:
            return ((CrudRepository)var2).saveAll((Iterable)var3[0]);
         case 5:
            return ((CrudRepository)var2).findById(var3[0]);
         case 6:
            return ((CrudRepository)var2).existsById(var3[0]);
         case 7:
            return ((CrudRepository)var2).findAll();
         case 8:
            return ((CrudRepository)var2).count();
         case 9:
            ((CrudRepository)var2).deleteById(var3[0]);
            return null;
         case 10:
            ((CrudRepository)var2).delete(var3[0]);
            return null;
         case 11:
            ((CrudRepository)var2).deleteAll((Iterable)var3[0]);
            return null;
         case 12:
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
            return ReflectionUtils.getRequiredMethod(GenreDao.class, "listGenres", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 1:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "save", Object.class);
         case 2:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "update", Object.class);
         case 3:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "updateAll", Iterable.class);
         case 4:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "saveAll", Iterable.class);
         case 5:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "findById", Object.class);
         case 6:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "existsById", Object.class);
         case 7:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "findAll", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 8:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "count", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 9:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "deleteById", Object.class);
         case 10:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "delete", Object.class);
         case 11:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "deleteAll", Iterable.class);
         case 12:
            return ReflectionUtils.getRequiredMethod(CrudRepository.class, "deleteAll", ReflectionUtils.EMPTY_CLASS_ARRAY);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   private final ExecutableMethod getMethod(String var1, Class[] var2) {
      switch(var1.hashCode()) {
         case -1949226984:
            if (this.methodAtIndexMatches(3, var1, var2)) {
               return this.getExecutableMethodByIndex(3);
            }
            break;
         case -1335458389:
            if (this.methodAtIndexMatches(10, var1, var2)) {
               return this.getExecutableMethodByIndex(10);
            }
            break;
         case -853211864:
            if (this.methodAtIndexMatches(7, var1, var2)) {
               return this.getExecutableMethodByIndex(7);
            }
            break;
         case -838846263:
            if (this.methodAtIndexMatches(2, var1, var2)) {
               return this.getExecutableMethodByIndex(2);
            }
            break;
         case -679722709:
            if (this.methodAtIndexMatches(5, var1, var2)) {
               return this.getExecutableMethodByIndex(5);
            }
            break;
         case -358737930:
            if (this.methodAtIndexMatches(11, var1, var2)) {
               return this.getExecutableMethodByIndex(11);
            }

            if (this.methodAtIndexMatches(12, var1, var2)) {
               return this.getExecutableMethodByIndex(12);
            }
            break;
         case 3522941:
            if (this.methodAtIndexMatches(1, var1, var2)) {
               return this.getExecutableMethodByIndex(1);
            }
            break;
         case 7493006:
            if (this.methodAtIndexMatches(0, var1, var2)) {
               return this.getExecutableMethodByIndex(0);
            }
            break;
         case 94851343:
            if (this.methodAtIndexMatches(8, var1, var2)) {
               return this.getExecutableMethodByIndex(8);
            }
            break;
         case 205272654:
            if (this.methodAtIndexMatches(6, var1, var2)) {
               return this.getExecutableMethodByIndex(6);
            }
            break;
         case 1764067357:
            if (this.methodAtIndexMatches(9, var1, var2)) {
               return this.getExecutableMethodByIndex(9);
            }
            break;
         case 1872786148:
            if (this.methodAtIndexMatches(4, var1, var2)) {
               return this.getExecutableMethodByIndex(4);
            }
      }

      return null;
   }
}
