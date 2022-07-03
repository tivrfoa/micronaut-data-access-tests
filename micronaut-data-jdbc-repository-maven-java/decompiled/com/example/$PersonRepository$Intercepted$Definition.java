package com.example;

import io.micronaut.aop.Interceptor;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $PersonRepository$Intercepted$Definition
   extends AbstractInitializableBeanDefinition<PersonRepository$Intercepted>
   implements AdvisedBeanType<PersonRepository>,
   BeanFactory<PersonRepository$Intercepted> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.data.repository.CrudRepository",
      new Argument[]{Argument.of(Person.class, "E"), Argument.of(Long.class, "ID")},
      "io.micronaut.data.repository.GenericRepository",
      new Argument[]{Argument.of(Person.class, "E"), Argument.of(Long.class, "ID")},
      "io.micronaut.data.repository.PageableRepository",
      new Argument[]{Argument.of(Person.class, "E"), Argument.of(Long.class, "ID")}
   );

   @Override
   public PersonRepository$Intercepted build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      PersonRepository$Intercepted var4 = new PersonRepository$Intercepted(
         var1,
         var2,
         (Qualifier)super.getBeanForConstructorArgument(var1, var2, 2, null),
         super.getBeanRegistrationsForConstructorArgument(
            var1,
            var2,
            3,
            ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[3].getTypeParameters()[0].getTypeParameters()[0],
            Qualifiers.byInterceptorBinding(((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[3].getAnnotationMetadata())
         )
      );
      return (PersonRepository$Intercepted)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      PersonRepository$Intercepted var4 = (PersonRepository$Intercepted)var3;
      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      Map var1;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         PersonRepository$Intercepted.class,
         "<init>",
         new Argument[]{
            Argument.of(BeanResolutionContext.class, "$beanResolutionContext"),
            Argument.of(BeanContext.class, "$beanContext"),
            Argument.of(
               Qualifier.class,
               "$qualifier",
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
            ),
            Argument.of(
               List.class,
               "$interceptors",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.inject.qualifiers.InterceptorBindingQualifier",
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
                           ),
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                                 "kind",
                                 "INTRODUCTION",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                              ),
                              var0
                           )
                        }
                     )
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.inject.qualifiers.InterceptorBindingQualifier",
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
                           ),
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                                 "kind",
                                 "INTRODUCTION",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                              ),
                              var0
                           )
                        }
                     )
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               Argument.of(BeanRegistration.class, "E", null, Argument.of(Interceptor.class, "T"))
            )
         },
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
                           new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                           "kind",
                           "INTRODUCTION",
                           "value",
                           new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                        ),
                        var0
                     )
                  }
               ),
               "io.micronaut.data.jdbc.annotation.JdbcRepository",
               AnnotationUtil.mapOf("dialect", "MYSQL")
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Introduction",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Type",
               AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_2()}),
               "io.micronaut.data.annotation.Repository",
               AnnotationUtil.mapOf("dialect", "MYSQL"),
               "io.micronaut.data.annotation.RepositoryConfiguration",
               AnnotationUtil.mapOf(
                  "implicitQueries",
                  false,
                  "namedParameters",
                  false,
                  "operations",
                  $micronaut_load_class_value_4(),
                  "queryBuilder",
                  $micronaut_load_class_value_5(),
                  "typeRoles",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.data.annotation.TypeRole",
                        AnnotationUtil.mapOf("role", "sqlMappingFunction", "type", $micronaut_load_class_value_6()),
                        var1 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.data.annotation.TypeRole")
                     )
                  }
               ),
               "javax.inject.Scope",
               Collections.EMPTY_MAP,
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Around",
               Collections.EMPTY_MAP,
               "io.micronaut.aop.Introduction",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Type",
               AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_2()}),
               "io.micronaut.data.annotation.Repository",
               AnnotationUtil.mapOf("dialect", "MYSQL"),
               "io.micronaut.data.annotation.RepositoryConfiguration",
               AnnotationUtil.mapOf(
                  "implicitQueries",
                  false,
                  "namedParameters",
                  false,
                  "operations",
                  $micronaut_load_class_value_4(),
                  "queryBuilder",
                  $micronaut_load_class_value_5(),
                  "typeRoles",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.data.annotation.TypeRole",
                        AnnotationUtil.mapOf("role", "sqlMappingFunction", "type", $micronaut_load_class_value_6()),
                        var1
                     )
                  }
               ),
               "javax.inject.Scope",
               Collections.EMPTY_MAP,
               "javax.inject.Singleton",
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
                           new AnnotationClassValue[]{$micronaut_load_class_value_2()},
                           "kind",
                           "INTRODUCTION",
                           "value",
                           new AnnotationClassValue[]{$micronaut_load_class_value_3()}
                        ),
                        var0
                     ),
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
               "io.micronaut.core.annotation.Blocking",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.core.annotation.Indexed",
                        AnnotationUtil.mapOf("value", $micronaut_load_class_value_7()),
                        AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                     )
                  }
               ),
               "io.micronaut.data.jdbc.annotation.JdbcRepository",
               AnnotationUtil.mapOf("dialect", "MYSQL"),
               "io.micronaut.validation.Validated",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Around",
               AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
               "io.micronaut.aop.Introduction",
               AnnotationUtil.internListOf("io.micronaut.data.annotation.Repository"),
               "io.micronaut.context.annotation.Type",
               AnnotationUtil.internListOf("io.micronaut.data.annotation.Repository", "io.micronaut.validation.Validated"),
               "io.micronaut.data.annotation.Repository",
               AnnotationUtil.internListOf("io.micronaut.data.jdbc.annotation.JdbcRepository"),
               "io.micronaut.data.annotation.RepositoryConfiguration",
               AnnotationUtil.internListOf("io.micronaut.data.jdbc.annotation.JdbcRepository"),
               "javax.inject.Scope",
               AnnotationUtil.internListOf("javax.inject.Singleton"),
               "javax.inject.Singleton",
               AnnotationUtil.internListOf("io.micronaut.data.annotation.Repository")
            ),
            false,
            true
         ),
         false
      );
   }

   public $PersonRepository$Intercepted$Definition() {
      this(PersonRepository$Intercepted.class, $CONSTRUCTOR);
   }

   protected $PersonRepository$Intercepted$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $PersonRepository$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $PersonRepository$Intercepted$Definition$Exec(),
         $TYPE_ARGUMENTS,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         false
      );
   }

   @Override
   public Class getInterceptedType() {
      return PersonRepository.class;
   }
}
