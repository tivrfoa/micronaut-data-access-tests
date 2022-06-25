package io.micronaut.runtime.context.env;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ConfigurationIntroductionAdvice$Definition
   extends AbstractInitializableBeanDefinition<ConfigurationIntroductionAdvice>
   implements BeanFactory<ConfigurationIntroductionAdvice> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ConfigurationIntroductionAdvice.class,
      "<init>",
      new Argument[]{
         Argument.of(Qualifier.class, "qualifier", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(Environment.class, "environment"),
         Argument.of(BeanContext.class, "beanContext")
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public ConfigurationIntroductionAdvice build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ConfigurationIntroductionAdvice var4 = new ConfigurationIntroductionAdvice(
         (Qualifier<?>)super.getBeanForConstructorArgument(var1, var2, 0, null), (Environment)super.getBeanForConstructorArgument(var1, var2, 1, null), var2
      );
      return (ConfigurationIntroductionAdvice)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ConfigurationIntroductionAdvice var4 = (ConfigurationIntroductionAdvice)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ConfigurationIntroductionAdvice$Definition() {
      this(ConfigurationIntroductionAdvice.class, $CONSTRUCTOR);
   }

   protected $ConfigurationIntroductionAdvice$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ConfigurationIntroductionAdvice$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("io.micronaut.context.annotation.Prototype"),
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }
}
