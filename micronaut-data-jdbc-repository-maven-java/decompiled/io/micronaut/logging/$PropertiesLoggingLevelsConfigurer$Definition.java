package io.micronaut.logging;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $PropertiesLoggingLevelsConfigurer$Definition
   extends AbstractInitializableBeanDefinition<PropertiesLoggingLevelsConfigurer>
   implements BeanFactory<PropertiesLoggingLevelsConfigurer> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      PropertiesLoggingLevelsConfigurer.class,
      "<init>",
      new Argument[]{
         Argument.of(Environment.class, "environment"), Argument.of(List.class, "loggingSystems", null, Argument.ofTypeVariable(LoggingSystem.class, "E"))
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.ApplicationEventListener", new Argument[]{Argument.of(RefreshEvent.class, "E")}
   );

   @Override
   public PropertiesLoggingLevelsConfigurer build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      PropertiesLoggingLevelsConfigurer var4 = new PropertiesLoggingLevelsConfigurer(
         (Environment)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (List<LoggingSystem>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (PropertiesLoggingLevelsConfigurer)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      PropertiesLoggingLevelsConfigurer var4 = (PropertiesLoggingLevelsConfigurer)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $PropertiesLoggingLevelsConfigurer$Definition() {
      this(PropertiesLoggingLevelsConfigurer.class, $CONSTRUCTOR);
   }

   protected $PropertiesLoggingLevelsConfigurer$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $PropertiesLoggingLevelsConfigurer$Definition$Reference.$ANNOTATION_METADATA,
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
