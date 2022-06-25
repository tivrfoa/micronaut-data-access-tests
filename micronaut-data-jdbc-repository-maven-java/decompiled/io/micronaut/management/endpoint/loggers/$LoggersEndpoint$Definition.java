package io.micronaut.management.endpoint.loggers;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $LoggersEndpoint$Definition extends AbstractInitializableBeanDefinition<LoggersEndpoint> implements BeanFactory<LoggersEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      LoggersEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(ManagedLoggingSystem.class, "loggingSystem"),
         Argument.of(
            LoggersManager.class,
            "loggersManager",
            null,
            Argument.ofTypeVariable(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
         )
      },
      null,
      false
   );

   @Override
   public LoggersEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      LoggersEndpoint var4 = new LoggersEndpoint(
         (ManagedLoggingSystem)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (LoggersManager<Map<String, Object>>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (LoggersEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         LoggersEndpoint var4 = (LoggersEndpoint)var3;
         if (this.containsPropertyValue(var1, var2, "endpoints.loggers.write-sensitive")) {
            var4.setWriteSensitive(
               super.getPropertyValueForSetter(
                  var1, var2, "setWriteSensitive", Argument.of(Boolean.TYPE, "writeSensitive"), "endpoints.loggers.write-sensitive", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $LoggersEndpoint$Definition() {
      this(LoggersEndpoint.class, $CONSTRUCTOR);
   }

   protected $LoggersEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $LoggersEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $LoggersEndpoint$Definition$Exec(),
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         true,
         false,
         false
      );
   }
}
