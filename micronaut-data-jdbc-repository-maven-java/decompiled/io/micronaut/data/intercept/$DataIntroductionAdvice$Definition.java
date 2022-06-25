package io.micronaut.data.intercept;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.transaction.interceptor.CoroutineTxHelper;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DataIntroductionAdvice$Definition extends AbstractInitializableBeanDefinition<DataIntroductionAdvice> implements BeanFactory<DataIntroductionAdvice> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataIntroductionAdvice.class,
      "<init>",
      new Argument[]{
         Argument.of(
            BeanLocator.class,
            "beanLocator",
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
         ),
         Argument.of(
            CoroutineTxHelper.class,
            "coroutineTxHelper",
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
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public DataIntroductionAdvice build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DataIntroductionAdvice var4 = new DataIntroductionAdvice(
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 0, null), (CoroutineTxHelper)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (DataIntroductionAdvice)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DataIntroductionAdvice var4 = (DataIntroductionAdvice)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DataIntroductionAdvice$Definition() {
      this(DataIntroductionAdvice.class, $CONSTRUCTOR);
   }

   protected $DataIntroductionAdvice$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataIntroductionAdvice$Definition$Reference.$ANNOTATION_METADATA,
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
