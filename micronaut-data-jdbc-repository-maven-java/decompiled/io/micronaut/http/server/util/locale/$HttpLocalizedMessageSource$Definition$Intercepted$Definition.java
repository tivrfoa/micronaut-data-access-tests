package io.micronaut.http.server.util.locale;

import io.micronaut.aop.Interceptor;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.MessageSource;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.LocaleResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ProxyBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// $FF: synthetic class
@Generated
class $HttpLocalizedMessageSource$Definition$Intercepted$Definition
   extends $HttpLocalizedMessageSource$Definition
   implements AdvisedBeanType<HttpLocalizedMessageSource>,
   BeanFactory<$HttpLocalizedMessageSource$Definition$Intercepted>,
   ProxyBeanDefinition<HttpLocalizedMessageSource> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;

   @Override
   public $HttpLocalizedMessageSource$Definition$Intercepted build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      $HttpLocalizedMessageSource$Definition$Intercepted var4 = new $HttpLocalizedMessageSource$Definition$Intercepted(
         (LocaleResolver)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (MessageSource)super.getBeanForConstructorArgument(var1, var2, 1, null),
         var1,
         var2,
         (Qualifier)super.getBeanForConstructorArgument(var1, var2, 4, null),
         super.getBeanRegistrationsForConstructorArgument(
            var1,
            var2,
            5,
            ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[5].getTypeParameters()[0].getTypeParameters()[0],
            Qualifiers.byInterceptorBinding(((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[5].getAnnotationMetadata())
         )
      );
      return ($HttpLocalizedMessageSource$Definition$Intercepted)this.injectBean(var1, var2, var4);
   }

   @Override
   public Class getTargetDefinitionType() {
      return $HttpLocalizedMessageSource$Definition.class;
   }

   @Override
   public Class getTargetType() {
      return HttpLocalizedMessageSource.class;
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         $HttpLocalizedMessageSource$Definition$Intercepted.class,
         "<init>",
         new Argument[]{
            Argument.of(
               LocaleResolver.class, "localeResolver", null, Argument.ofTypeVariable(HttpRequest.class, "T", null, Argument.ofTypeVariable(Object.class, "B"))
            ),
            Argument.of(MessageSource.class, "messageSource"),
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
                              AnnotationUtil.mapOf("kind", "AROUND", "value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
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
                              AnnotationUtil.mapOf("kind", "AROUND", "value", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
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

   public $HttpLocalizedMessageSource$Definition$Intercepted$Definition() {
      super($HttpLocalizedMessageSource$Definition$Intercepted.class, $CONSTRUCTOR);
   }

   @Override
   public Class getInterceptedType() {
      return HttpLocalizedMessageSource.class;
   }
}
