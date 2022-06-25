package io.micronaut.jackson.serialize;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.json.tree.JsonNode;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JsonNodeSerializer$Definition extends AbstractInitializableBeanDefinition<JsonNodeSerializer> implements BeanFactory<JsonNodeSerializer> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonNodeSerializer.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "com.fasterxml.jackson.databind.JsonSerializer", new Argument[]{Argument.of(JsonNode.class, "T")}
   );

   @Override
   public JsonNodeSerializer build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonNodeSerializer var4 = new JsonNodeSerializer();
      return (JsonNodeSerializer)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonNodeSerializer var4 = (JsonNodeSerializer)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonNodeSerializer$Definition() {
      this(JsonNodeSerializer.class, $CONSTRUCTOR);
   }

   protected $JsonNodeSerializer$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonNodeSerializer$Definition$Reference.$ANNOTATION_METADATA,
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
