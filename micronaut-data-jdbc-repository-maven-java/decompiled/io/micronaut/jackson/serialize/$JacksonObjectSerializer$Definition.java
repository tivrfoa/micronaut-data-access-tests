package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $JacksonObjectSerializer$Definition extends AbstractInitializableBeanDefinition<JacksonObjectSerializer> implements BeanFactory<JacksonObjectSerializer> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JacksonObjectSerializer.class, "<init>", new Argument[]{Argument.of(ObjectMapper.class, "objectMapper")}, null, false
   );
   private static final Set $EXPOSED_TYPES = Collections.singleton(JacksonObjectSerializer.class);

   @Override
   public JacksonObjectSerializer build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JacksonObjectSerializer var4 = new JacksonObjectSerializer((ObjectMapper)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (JacksonObjectSerializer)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JacksonObjectSerializer var4 = (JacksonObjectSerializer)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JacksonObjectSerializer$Definition() {
      this(JacksonObjectSerializer.class, $CONSTRUCTOR);
   }

   protected $JacksonObjectSerializer$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JacksonObjectSerializer$Definition$Reference.$ANNOTATION_METADATA,
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

   @Override
   public Set getExposedTypes() {
      return $EXPOSED_TYPES;
   }
}
