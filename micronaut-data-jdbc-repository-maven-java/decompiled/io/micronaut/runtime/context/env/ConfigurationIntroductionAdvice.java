package io.micronaut.runtime.context.env;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.value.PropertyNotFoundException;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Optional;

@Prototype
@Internal
@BootstrapContextCompatible
public class ConfigurationIntroductionAdvice implements MethodInterceptor<Object, Object> {
   private static final String MEMBER_BEAN = "bean";
   private static final String MEMBER_NAME = "name";
   private final Environment environment;
   private final BeanContext beanContext;
   private final String name;

   ConfigurationIntroductionAdvice(Qualifier<?> qualifier, Environment environment, BeanContext beanContext) {
      this.environment = environment;
      this.beanContext = beanContext;
      this.name = qualifier instanceof Named ? ((Named)qualifier).getName() : null;
   }

   @Nullable
   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      ReturnType<Object> rt = context.getReturnType();
      Class<Object> returnType = rt.getType();
      if (context.isTrue(ConfigurationAdvice.class, "bean")) {
         Qualifier<Object> qualifier = this.name != null ? Qualifiers.byName(this.name) : null;
         if (context.isNullable()) {
            Object v = this.beanContext.findBean(returnType, qualifier).orElse(null);
            return v != null ? this.environment.convertRequired(v, returnType) : v;
         } else {
            return this.environment.convertRequired(this.beanContext.getBean(returnType, qualifier), returnType);
         }
      } else {
         String property = (String)context.stringValue(Property.class, "name").orElse(null);
         if (property == null) {
            throw new IllegalStateException("No property name available to resolve");
         } else {
            boolean iterable = property.indexOf(42) > -1;
            if (iterable && this.name != null) {
               property = property.replace("*", this.name);
            }

            String defaultValue = (String)context.stringValue(Bindable.class, "defaultValue").orElse(null);
            Argument<Object> argument = rt.asArgument();
            Optional<Object> value = this.environment.getProperty(property, argument);
            if (defaultValue != null) {
               return value.orElseGet(() -> this.environment.convertRequired(defaultValue, argument));
            } else if (rt.isOptional()) {
               return value.orElse(Optional.empty());
            } else if (context.isNullable()) {
               return value.orElse(null);
            } else {
               String finalProperty = property;
               return value.orElseThrow(() -> new PropertyNotFoundException(finalProperty, argument.getType()));
            }
         }
      }
   }
}
