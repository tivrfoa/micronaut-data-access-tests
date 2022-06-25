package io.micronaut.http.server.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHttpCompressionStrategy$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpCompressionStrategy>
   implements BeanFactory<DefaultHttpCompressionStrategy> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpCompressionStrategy.class,
      "<init>",
      new Argument[]{Argument.of(NettyHttpServerConfiguration.class, "serverConfiguration")},
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
   public DefaultHttpCompressionStrategy build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpCompressionStrategy var4 = new DefaultHttpCompressionStrategy(
         (NettyHttpServerConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null)
      );
      return (DefaultHttpCompressionStrategy)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHttpCompressionStrategy var4 = (DefaultHttpCompressionStrategy)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpCompressionStrategy$Definition() {
      this(DefaultHttpCompressionStrategy.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpCompressionStrategy$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHttpCompressionStrategy$Definition$Reference.$ANNOTATION_METADATA,
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
