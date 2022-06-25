package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $DefaultExecutorSelector$Definition extends AbstractInitializableBeanDefinition<DefaultExecutorSelector> implements BeanFactory<DefaultExecutorSelector> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultExecutorSelector.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanLocator.class, "beanLocator"),
         Argument.of(
            BeanProvider.class,
            "ioExecutor",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            Argument.ofTypeVariable(ExecutorService.class, "T")
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

   @Override
   public DefaultExecutorSelector build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultExecutorSelector var4 = new DefaultExecutorSelector(
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (BeanProvider<ExecutorService>)super.getBeanForConstructorArgument(var1, var2, 1, Qualifiers.byName("io"))
      );
      return (DefaultExecutorSelector)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultExecutorSelector var4 = (DefaultExecutorSelector)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultExecutorSelector$Definition() {
      this(DefaultExecutorSelector.class, $CONSTRUCTOR);
   }

   protected $DefaultExecutorSelector$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultExecutorSelector$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
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
