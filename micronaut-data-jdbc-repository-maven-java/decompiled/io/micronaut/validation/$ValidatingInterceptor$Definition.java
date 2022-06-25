package io.micronaut.validation;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.validation.validator.Validator;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.validation.ValidatorFactory;

// $FF: synthetic class
@Generated
class $ValidatingInterceptor$Definition extends AbstractInitializableBeanDefinition<ValidatingInterceptor> implements BeanFactory<ValidatingInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ValidatingInterceptor.class,
      "<init>",
      new Argument[]{
         Argument.of(
            Validator.class,
            "micronautValidator",
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
            ValidatorFactory.class,
            "validatorFactory",
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
         )
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
   public ValidatingInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ValidatingInterceptor var4 = new ValidatingInterceptor(
         (Validator)super.getBeanForConstructorArgument(var1, var2, 0, null), (ValidatorFactory)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (ValidatingInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ValidatingInterceptor var4 = (ValidatingInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ValidatingInterceptor$Definition() {
      this(ValidatingInterceptor.class, $CONSTRUCTOR);
   }

   protected $ValidatingInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ValidatingInterceptor$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
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
}
