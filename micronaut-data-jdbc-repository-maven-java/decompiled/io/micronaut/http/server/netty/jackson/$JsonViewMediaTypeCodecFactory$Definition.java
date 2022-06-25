package io.micronaut.http.server.netty.jackson;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.json.codec.JsonMediaTypeCodec;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JsonViewMediaTypeCodecFactory$Definition
   extends AbstractInitializableBeanDefinition<JsonViewMediaTypeCodecFactory>
   implements BeanFactory<JsonViewMediaTypeCodecFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonViewMediaTypeCodecFactory.class, "<init>", new Argument[]{Argument.of(JsonMediaTypeCodec.class, "jsonCodec")}, null, false
   );

   @Override
   public JsonViewMediaTypeCodecFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonViewMediaTypeCodecFactory var4 = new JsonViewMediaTypeCodecFactory((JsonMediaTypeCodec)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (JsonViewMediaTypeCodecFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonViewMediaTypeCodecFactory var4 = (JsonViewMediaTypeCodecFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonViewMediaTypeCodecFactory$Definition() {
      this(JsonViewMediaTypeCodecFactory.class, $CONSTRUCTOR);
   }

   protected $JsonViewMediaTypeCodecFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonViewMediaTypeCodecFactory$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }
}
