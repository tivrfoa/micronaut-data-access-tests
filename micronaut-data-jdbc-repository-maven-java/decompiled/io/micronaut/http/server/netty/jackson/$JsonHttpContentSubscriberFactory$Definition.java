package io.micronaut.http.server.netty.jackson;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.json.JsonMapper;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JsonHttpContentSubscriberFactory$Definition
   extends AbstractInitializableBeanDefinition<JsonHttpContentSubscriberFactory>
   implements BeanFactory<JsonHttpContentSubscriberFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonHttpContentSubscriberFactory.class,
      "<init>",
      new Argument[]{Argument.of(JsonMapper.class, "jsonMapper"), Argument.of(HttpServerConfiguration.class, "httpServerConfiguration")},
      null,
      false
   );

   @Override
   public JsonHttpContentSubscriberFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonHttpContentSubscriberFactory var4 = new JsonHttpContentSubscriberFactory(
         (JsonMapper)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (HttpServerConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (JsonHttpContentSubscriberFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonHttpContentSubscriberFactory var4 = (JsonHttpContentSubscriberFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonHttpContentSubscriberFactory$Definition() {
      this(JsonHttpContentSubscriberFactory.class, $CONSTRUCTOR);
   }

   protected $JsonHttpContentSubscriberFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonHttpContentSubscriberFactory$Definition$Reference.$ANNOTATION_METADATA,
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
