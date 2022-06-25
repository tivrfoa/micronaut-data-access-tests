package com.example;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
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
final class $GenreController$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;
   private final boolean $interceptable;

   static {
      Map var0;
      Map var1;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            GenreController.class,
            new AnnotationMetadataHierarchy(
               $GenreController$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.http.annotation.Get", AnnotationUtil.mapOf("value", "/{id}")),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/{id}"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/{id}")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/{id}"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/{id}")
                  ),
                  AnnotationUtil.mapOf("io.micronaut.http.annotation.Get", AnnotationUtil.mapOf("value", "/{id}")),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.core.annotation.EntryPoint",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.Get"),
                     "io.micronaut.http.annotation.UriMapping",
                     Collections.EMPTY_LIST
                  ),
                  false,
                  true
               )
            ),
            "show",
            Argument.of(Optional.class, "java.util.Optional", null, Argument.ofTypeVariable(Genre.class, "T")),
            new Argument[]{Argument.of(Long.class, "id")},
            false,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            GenreController.class,
            new AnnotationMetadataHierarchy(
               $GenreController$Definition$Reference.$ANNOTATION_METADATA,
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
                     "io.micronaut.http.annotation.Put",
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
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
                     "io.micronaut.http.annotation.Put",
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.core.annotation.EntryPoint",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.Put")
                  ),
                  false,
                  true
               )
            ),
            "update",
            Argument.of(HttpResponse.class, "io.micronaut.http.HttpResponse", null, Argument.ofTypeVariable(Object.class, "B")),
            new Argument[]{
               Argument.of(
                  GenreUpdateCommand.class,
                  "command",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf("io.micronaut.http.annotation.Body", Collections.EMPTY_MAP, "javax.validation.Valid", Collections.EMPTY_MAP),
                     AnnotationUtil.internMapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP),
                     AnnotationUtil.internMapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP),
                     AnnotationUtil.mapOf("io.micronaut.http.annotation.Body", Collections.EMPTY_MAP, "javax.validation.Valid", Collections.EMPTY_MAP),
                     AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", AnnotationUtil.internListOf("io.micronaut.http.annotation.Body")),
                     false,
                     true
                  ),
                  null
               )
            },
            false,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            GenreController.class,
            new AnnotationMetadataHierarchy(
               $GenreController$Definition$Reference.$ANNOTATION_METADATA,
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
                              var0
                           )
                        }
                     ),
                     "io.micronaut.http.annotation.Get",
                     AnnotationUtil.mapOf("value", "/list"),
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/list"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/list")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/list"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/list")
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
                     "io.micronaut.http.annotation.Get",
                     AnnotationUtil.mapOf("value", "/list"),
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.core.annotation.EntryPoint",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.Get"),
                     "io.micronaut.http.annotation.UriMapping",
                     Collections.EMPTY_LIST
                  ),
                  false,
                  true
               )
            ),
            "list",
            Argument.of(List.class, "java.util.List", null, Argument.ofTypeVariable(Genre.class, "E")),
            new Argument[]{
               Argument.of(
                  Pageable.class,
                  "pageable",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.internMapOf("javax.validation.Valid", Collections.EMPTY_MAP),
                     Collections.EMPTY_MAP,
                     Collections.EMPTY_MAP,
                     AnnotationUtil.internMapOf("javax.validation.Valid", Collections.EMPTY_MAP),
                     Collections.EMPTY_MAP,
                     false,
                     true
                  ),
                  null
               )
            },
            false,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            GenreController.class,
            new AnnotationMetadataHierarchy(
               $GenreController$Definition$Reference.$ANNOTATION_METADATA,
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
                              var0
                           )
                        }
                     ),
                     "io.micronaut.http.annotation.Post",
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
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
                     "io.micronaut.http.annotation.Post",
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.core.annotation.EntryPoint",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.Post")
                  ),
                  false,
                  true
               )
            ),
            "save",
            Argument.of(HttpResponse.class, "io.micronaut.http.HttpResponse", null, Argument.ofTypeVariable(Genre.class, "B")),
            new Argument[]{
               Argument.of(
                  String.class,
                  "name",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "io.micronaut.http.annotation.Body",
                        AnnotationUtil.mapOf("value", "name"),
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
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.bind.annotation.Bindable",
                        AnnotationUtil.mapOf("value", "name"),
                        "javax.validation.Constraint",
                        AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.bind.annotation.Bindable",
                        AnnotationUtil.mapOf("value", "name"),
                        "javax.validation.Constraint",
                        AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.http.annotation.Body",
                        AnnotationUtil.mapOf("value", "name"),
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotBlank", Collections.EMPTY_MAP, var1)}
                        )
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.bind.annotation.Bindable",
                        AnnotationUtil.internListOf("io.micronaut.http.annotation.Body"),
                        "javax.validation.Constraint",
                        AnnotationUtil.internListOf("javax.validation.constraints.NotBlank")
                     ),
                     false,
                     true
                  ),
                  null
               )
            },
            false,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            GenreController.class,
            new AnnotationMetadataHierarchy(
               $GenreController$Definition$Reference.$ANNOTATION_METADATA,
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
                              var0
                           )
                        }
                     ),
                     "io.micronaut.http.annotation.Post",
                     AnnotationUtil.mapOf("value", "/ex"),
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/ex"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/ex")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/ex"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/ex")
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
                     "io.micronaut.http.annotation.Post",
                     AnnotationUtil.mapOf("value", "/ex"),
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
                     "io.micronaut.core.annotation.EntryPoint",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.Post"),
                     "io.micronaut.http.annotation.UriMapping",
                     Collections.EMPTY_LIST
                  ),
                  false,
                  true
               )
            ),
            "saveExceptions",
            Argument.of(HttpResponse.class, "io.micronaut.http.HttpResponse", null, Argument.ofTypeVariable(Genre.class, "B")),
            new Argument[]{
               Argument.of(
                  String.class,
                  "name",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "io.micronaut.http.annotation.Body",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotBlank", Collections.EMPTY_MAP, var1)}
                        )
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.bind.annotation.Bindable",
                        Collections.EMPTY_MAP,
                        "javax.validation.Constraint",
                        AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.bind.annotation.Bindable",
                        Collections.EMPTY_MAP,
                        "javax.validation.Constraint",
                        AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.http.annotation.Body",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotBlank", Collections.EMPTY_MAP, var1)}
                        )
                     ),
                     AnnotationUtil.mapOf(
                        "io.micronaut.core.bind.annotation.Bindable",
                        AnnotationUtil.internListOf("io.micronaut.http.annotation.Body"),
                        "javax.validation.Constraint",
                        AnnotationUtil.internListOf("javax.validation.constraints.NotBlank")
                     ),
                     false,
                     true
                  ),
                  null
               )
            },
            false,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            GenreController.class,
            new AnnotationMetadataHierarchy(
               $GenreController$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.http.annotation.Delete",
                     AnnotationUtil.mapOf("value", "/{id}"),
                     "io.micronaut.http.annotation.Status",
                     AnnotationUtil.mapOf("value", "NO_CONTENT")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/{id}"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/{id}")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.core.annotation.EntryPoint",
                     Collections.EMPTY_MAP,
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.mapOf("value", "/{id}"),
                     "io.micronaut.http.annotation.UriMapping",
                     AnnotationUtil.mapOf("value", "/{id}")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.http.annotation.Delete",
                     AnnotationUtil.mapOf("value", "/{id}"),
                     "io.micronaut.http.annotation.Status",
                     AnnotationUtil.mapOf("value", "NO_CONTENT")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping", "io.micronaut.http.annotation.Status"),
                     "io.micronaut.core.annotation.EntryPoint",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.HttpMethodMapping"),
                     "io.micronaut.http.annotation.HttpMethodMapping",
                     AnnotationUtil.internListOf("io.micronaut.http.annotation.Delete"),
                     "io.micronaut.http.annotation.UriMapping",
                     Collections.EMPTY_LIST
                  ),
                  false,
                  true
               )
            ),
            "delete",
            Argument.VOID,
            new Argument[]{Argument.of(Long.class, "id")},
            false,
            false
         )
      };
   }

   public $GenreController$Definition$Exec() {
      this(false);
   }

   public $GenreController$Definition$Exec(boolean var1) {
      super($METHODS_REFERENCES);
      this.$interceptable = var1;
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((GenreController)var2).show((Long)var3[0]);
         case 1:
            if (this.$interceptable && var2 instanceof $GenreController$Definition$Intercepted) {
               return (($GenreController$Definition$Intercepted)var2).$$access$$update((GenreUpdateCommand)var3[0]);
            }

            return ((GenreController)var2).update((GenreUpdateCommand)var3[0]);
         case 2:
            if (this.$interceptable && var2 instanceof $GenreController$Definition$Intercepted) {
               return (($GenreController$Definition$Intercepted)var2).$$access$$list((Pageable)var3[0]);
            }

            return ((GenreController)var2).list((Pageable)var3[0]);
         case 3:
            if (this.$interceptable && var2 instanceof $GenreController$Definition$Intercepted) {
               return (($GenreController$Definition$Intercepted)var2).$$access$$save((String)var3[0]);
            }

            return ((GenreController)var2).save((String)var3[0]);
         case 4:
            if (this.$interceptable && var2 instanceof $GenreController$Definition$Intercepted) {
               return (($GenreController$Definition$Intercepted)var2).$$access$$saveExceptions((String)var3[0]);
            }

            return ((GenreController)var2).saveExceptions((String)var3[0]);
         case 5:
            ((GenreController)var2).delete((Long)var3[0]);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(GenreController.class, "show", Long.class);
         case 1:
            return ReflectionUtils.getRequiredMethod(GenreController.class, "update", GenreUpdateCommand.class);
         case 2:
            return ReflectionUtils.getRequiredMethod(GenreController.class, "list", Pageable.class);
         case 3:
            return ReflectionUtils.getRequiredMethod(GenreController.class, "save", String.class);
         case 4:
            return ReflectionUtils.getRequiredMethod(GenreController.class, "saveExceptions", String.class);
         case 5:
            return ReflectionUtils.getRequiredMethod(GenreController.class, "delete", Long.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   private final ExecutableMethod getMethod(String var1, Class[] var2) {
      switch(var1.hashCode()) {
         case -1335458389:
            if (this.methodAtIndexMatches(5, var1, var2)) {
               return this.getExecutableMethodByIndex(5);
            }
            break;
         case -838846263:
            if (this.methodAtIndexMatches(1, var1, var2)) {
               return this.getExecutableMethodByIndex(1);
            }
            break;
         case 3322014:
            if (this.methodAtIndexMatches(2, var1, var2)) {
               return this.getExecutableMethodByIndex(2);
            }
            break;
         case 3522941:
            if (this.methodAtIndexMatches(3, var1, var2)) {
               return this.getExecutableMethodByIndex(3);
            }
            break;
         case 3529469:
            if (this.methodAtIndexMatches(0, var1, var2)) {
               return this.getExecutableMethodByIndex(0);
            }
            break;
         case 1222631265:
            if (this.methodAtIndexMatches(4, var1, var2)) {
               return this.getExecutableMethodByIndex(4);
            }
      }

      return null;
   }
}
