package io.micronaut.context;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ParametrizedBeanFactory;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Internal
public abstract class AbstractParametrizedBeanDefinition<T> extends AbstractBeanDefinition<T> implements ParametrizedBeanFactory<T> {
   private final Argument[] requiredArguments = this.resolveRequiredArguments();

   public AbstractParametrizedBeanDefinition(
      Class<T> producedType, Class<?> declaringType, String methodName, AnnotationMetadata methodMetadata, boolean requiresReflection, Argument... arguments
   ) {
      super(producedType, declaringType, methodName, methodMetadata, requiresReflection, arguments);
   }

   protected AbstractParametrizedBeanDefinition(Class<T> type, AnnotationMetadata annotationMetadata, boolean requiresReflection, Argument... arguments) {
      super(type, annotationMetadata, requiresReflection, arguments);
   }

   @Override
   public Argument<?>[] getRequiredArguments() {
      return this.requiredArguments;
   }

   @Override
   public final T build(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition, Map<String, Object> requiredArgumentValues) throws BeanInstantiationException {
      requiredArgumentValues = (Map<String, Object>)(requiredArgumentValues != null ? new LinkedHashMap(requiredArgumentValues) : Collections.emptyMap());
      Argument<?>[] requiredArguments = this.getRequiredArguments();
      Optional<Class> eachBeanType = definition.classValue(EachBean.class);

      for(Argument<?> requiredArgument : requiredArguments) {
         if (requiredArgument.getType() == BeanResolutionContext.class) {
            requiredArgumentValues.put(requiredArgument.getName(), resolutionContext);
         }

         BeanResolutionContext.Path path = resolutionContext.getPath();

         try {
            path.pushConstructorResolve(this, requiredArgument);
            String argumentName = requiredArgument.getName();
            if (!requiredArgumentValues.containsKey(argumentName) && !requiredArgument.isNullable()) {
               if (eachBeanType.filter(type -> type == requiredArgument.getType()).isPresent()) {
                  throw new DisabledBeanException("@EachBean parameter disabled for argument: " + requiredArgument.getName());
               }

               throw new BeanInstantiationException(resolutionContext, "Missing bean argument value: " + argumentName);
            }

            Object value = requiredArgumentValues.get(argumentName);
            boolean requiresConversion = value != null && !requiredArgument.getType().isInstance(value);
            if (requiresConversion) {
               Optional<?> converted = ConversionService.SHARED.convert(value, requiredArgument.getType(), ConversionContext.of(requiredArgument));
               Object finalValue = value;
               value = converted.orElseThrow(
                  () -> new BeanInstantiationException(resolutionContext, "Invalid value [" + finalValue + "] for argument: " + argumentName)
               );
               requiredArgumentValues.put(argumentName, value);
            }
         } finally {
            path.pop();
         }
      }

      return this.doBuild(resolutionContext, context, definition, requiredArgumentValues);
   }

   protected abstract T doBuild(
      BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition, Map<String, Object> requiredArgumentValues
   );

   private Argument[] resolveRequiredArguments() {
      return (Argument[])Arrays.stream(this.getConstructor().getArguments()).filter(arg -> {
         Optional<Class<? extends Annotation>> qualifierType = arg.getAnnotationMetadata().getAnnotationTypeByStereotype("javax.inject.Qualifier");
         return qualifierType.isPresent() && qualifierType.get() == Parameter.class;
      }).toArray(x$0 -> new Argument[x$0]);
   }
}
