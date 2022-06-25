package io.micronaut.http.netty.channel;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultEventLoopGroupRegistry$Definition
   extends AbstractInitializableBeanDefinition<DefaultEventLoopGroupRegistry>
   implements BeanFactory<DefaultEventLoopGroupRegistry>,
   DisposableBeanDefinition<DefaultEventLoopGroupRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultEventLoopGroupRegistry.class,
      "<init>",
      new Argument[]{Argument.of(EventLoopGroupFactory.class, "eventLoopGroupFactory"), Argument.of(BeanLocator.class, "beanLocator")},
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultEventLoopGroupRegistry.class,
         "shutdown",
         null,
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.BootstrapContextCompatible",
                  Collections.EMPTY_MAP,
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.BootstrapContextCompatible",
                  Collections.EMPTY_MAP,
                  "io.micronaut.context.annotation.Factory",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false,
         false,
         true
      )
   };

   @Override
   public DefaultEventLoopGroupRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultEventLoopGroupRegistry var4 = new DefaultEventLoopGroupRegistry(
         (EventLoopGroupFactory)super.getBeanForConstructorArgument(var1, var2, 0, null), (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (DefaultEventLoopGroupRegistry)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultEventLoopGroupRegistry var4 = (DefaultEventLoopGroupRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public DefaultEventLoopGroupRegistry dispose(BeanResolutionContext var1, BeanContext var2, DefaultEventLoopGroupRegistry var3) {
      DefaultEventLoopGroupRegistry var4 = (DefaultEventLoopGroupRegistry)var3;
      super.preDestroy(var1, var2, var3);
      var4.shutdown();
      return var4;
   }

   public $DefaultEventLoopGroupRegistry$Definition() {
      this(DefaultEventLoopGroupRegistry.class, $CONSTRUCTOR);
   }

   protected $DefaultEventLoopGroupRegistry$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultEventLoopGroupRegistry$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.empty(),
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
