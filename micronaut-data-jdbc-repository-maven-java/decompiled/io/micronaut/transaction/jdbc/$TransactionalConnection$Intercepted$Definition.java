package io.micronaut.transaction.jdbc;

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
class $TransactionalConnection$Intercepted$Definition
   extends AbstractInitializableBeanDefinition<TransactionalConnection$Intercepted>
   implements AdvisedBeanType<TransactionalConnection>,
   BeanFactory<TransactionalConnection$Intercepted> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;

   @Override
   public TransactionalConnection$Intercepted build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      TransactionalConnection$Intercepted var4 = new TransactionalConnection$Intercepted(
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
      return (TransactionalConnection$Intercepted)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         TransactionalConnection$Intercepted var4 = (TransactionalConnection$Intercepted)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         TransactionalConnection$Intercepted.class,
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
                                 "INTRODUCTION",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                              ),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.aop.InterceptorBinding")
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
                                 "INTRODUCTION",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_1()}
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
                           new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                           "kind",
                           "INTRODUCTION",
                           "value",
                           new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                        ),
                        var0
                     )
                  }
               ),
               "io.micronaut.context.annotation.EachBean",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
               "io.micronaut.transaction.jdbc.TransactionalConnectionAdvice",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Introduction",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Type",
               AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
               "javax.inject.Scope",
               Collections.EMPTY_MAP,
               "javax.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Introduction",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Type",
               AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
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
                           new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                           "kind",
                           "INTRODUCTION",
                           "value",
                           new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                        ),
                        var0
                     )
                  }
               ),
               "io.micronaut.context.annotation.EachBean",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
               "io.micronaut.transaction.jdbc.TransactionalConnectionAdvice",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Introduction",
               AnnotationUtil.internListOf("io.micronaut.transaction.jdbc.TransactionalConnectionAdvice"),
               "io.micronaut.context.annotation.Type",
               AnnotationUtil.internListOf("io.micronaut.transaction.jdbc.TransactionalConnectionAdvice"),
               "io.micronaut.core.annotation.Internal",
               AnnotationUtil.internListOf("io.micronaut.transaction.jdbc.TransactionalConnectionAdvice"),
               "javax.inject.Scope",
               AnnotationUtil.internListOf("javax.inject.Singleton"),
               "javax.inject.Singleton",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
            ),
            false,
            true
         ),
         false
      );
   }

   public $TransactionalConnection$Intercepted$Definition() {
      this(TransactionalConnection$Intercepted.class, $CONSTRUCTOR);
   }

   protected $TransactionalConnection$Intercepted$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $TransactionalConnection$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $TransactionalConnection$Intercepted$Definition$Exec(),
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }

   @Override
   public Class getInterceptedType() {
      return TransactionalConnection.class;
   }
}
