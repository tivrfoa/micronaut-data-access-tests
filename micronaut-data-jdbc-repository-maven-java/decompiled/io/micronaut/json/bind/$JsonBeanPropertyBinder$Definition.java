package io.micronaut.json.bind;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.json.JsonConfiguration;
import io.micronaut.json.JsonMapper;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JsonBeanPropertyBinder$Definition extends AbstractInitializableBeanDefinition<JsonBeanPropertyBinder> implements BeanFactory<JsonBeanPropertyBinder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JsonBeanPropertyBinder.class,
      "<init>",
      new Argument[]{
         Argument.of(JsonMapper.class, "jsonMapper"),
         Argument.of(JsonConfiguration.class, "configuration"),
         Argument.of(BeanProvider.class, "exceptionHandlers", null, Argument.ofTypeVariable(JsonBeanPropertyBinderExceptionHandler.class, "T"))
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.bind.ArgumentBinder",
      new Argument[]{
         Argument.of(Object.class, "T"),
         Argument.of(Map.class, "S", null, Argument.ofTypeVariable(CharSequence.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
      }
   );

   @Override
   public JsonBeanPropertyBinder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JsonBeanPropertyBinder var4 = new JsonBeanPropertyBinder(
         (JsonMapper)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (JsonConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (BeanProvider<JsonBeanPropertyBinderExceptionHandler>)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (JsonBeanPropertyBinder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JsonBeanPropertyBinder var4 = (JsonBeanPropertyBinder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JsonBeanPropertyBinder$Definition() {
      this(JsonBeanPropertyBinder.class, $CONSTRUCTOR);
   }

   protected $JsonBeanPropertyBinder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JsonBeanPropertyBinder$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }
}
