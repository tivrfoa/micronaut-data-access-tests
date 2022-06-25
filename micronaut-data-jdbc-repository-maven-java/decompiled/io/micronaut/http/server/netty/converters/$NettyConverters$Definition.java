package io.micronaut.http.server.netty.converters;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.netty.channel.converters.ChannelOptionFactory;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyConverters$Definition extends AbstractInitializableBeanDefinition<NettyConverters> implements BeanFactory<NettyConverters> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyConverters.class,
      "<init>",
      new Argument[]{
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
         Argument.of(BeanProvider.class, "decoderRegistryProvider", null, Argument.ofTypeVariable(MediaTypeCodecRegistry.class, "T")),
         Argument.of(ChannelOptionFactory.class, "channelOptionFactory")
      },
      null,
      false
   );

   @Override
   public NettyConverters build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyConverters var4 = new NettyConverters(
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (BeanProvider<MediaTypeCodecRegistry>)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (ChannelOptionFactory)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (NettyConverters)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NettyConverters var4 = (NettyConverters)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NettyConverters$Definition() {
      this(NettyConverters.class, $CONSTRUCTOR);
   }

   protected $NettyConverters$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyConverters$Definition$Reference.$ANNOTATION_METADATA,
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
