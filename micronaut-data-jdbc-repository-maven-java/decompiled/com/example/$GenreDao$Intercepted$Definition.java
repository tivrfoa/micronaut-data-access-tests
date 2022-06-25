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
import io.micronaut.data.jdbc.runtime.JdbcOperations;
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
class $GenreDao$Intercepted$Definition
   extends AbstractInitializableBeanDefinition<GenreDao$Intercepted>
   implements AdvisedBeanType<GenreDao>,
   BeanFactory<GenreDao$Intercepted> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.data.repository.CrudRepository",
      new Argument[]{Argument.of(Genre.class, "E"), Argument.of(Long.class, "ID")},
      "io.micronaut.data.repository.GenericRepository",
      new Argument[]{Argument.of(Genre.class, "E"), Argument.of(Long.class, "ID")}
   );

   @Override
   public GenreDao$Intercepted build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      GenreDao$Intercepted var4 = new GenreDao$Intercepted(
         (JdbcOperations)super.getBeanForConstructorArgument(var1, var2, 0, null),
         var1,
         var2,
         (Qualifier)super.getBeanForConstructorArgument(var1, var2, 3, null),
         super.getBeanRegistrationsForConstructorArgument(
            var1,
            var2,
            4,
            ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[4].getTypeParameters()[0].getTypeParameters()[0],
            Qualifiers.byInterceptorBinding(((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[4].getAnnotationMetadata())
         )
      );
      return (GenreDao$Intercepted)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      GenreDao$Intercepted var4 = (GenreDao$Intercepted)var3;
      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         GenreDao$Intercepted.class,
         "<init>",
         new Argument[]{
            Argument.of(JdbcOperations.class, "jdbcOperations"),
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
                           ),
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_4()},
                                 "kind",
                                 "AROUND",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_5()}
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
                           ),
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_4()},
                                 "kind",
                                 "AROUND",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_5()}
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
         null,
         false
      );
   }

   public $GenreDao$Intercepted$Definition() {
      this(GenreDao$Intercepted.class, $CONSTRUCTOR);
   }

   protected $GenreDao$Intercepted$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $GenreDao$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $GenreDao$Intercepted$Definition$Exec(),
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
      return GenreDao.class;
   }
}
