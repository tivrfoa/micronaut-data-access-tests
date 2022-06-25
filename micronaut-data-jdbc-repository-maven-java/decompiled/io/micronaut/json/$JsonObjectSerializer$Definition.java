package io.micronaut.json;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JsonObjectSerializer$Definition extends AbstractInitializableBeanDefinition<JsonObjectSerializer> implements BeanFactory<JsonObjectSerializer> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonObjectSerializer.class, "<init>", new Argument[]{Argument.of(JsonMapper.class, "jsonMapper")}, null, false
   );

   @Override
   public JsonObjectSerializer build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonObjectSerializer var4 = new JsonObjectSerializer((JsonMapper)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (JsonObjectSerializer)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonObjectSerializer var4 = (JsonObjectSerializer)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonObjectSerializer$Definition() {
      this(JsonObjectSerializer.class, $CONSTRUCTOR);
   }

   protected $JsonObjectSerializer$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonObjectSerializer$Definition$Reference.$ANNOTATION_METADATA,
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
