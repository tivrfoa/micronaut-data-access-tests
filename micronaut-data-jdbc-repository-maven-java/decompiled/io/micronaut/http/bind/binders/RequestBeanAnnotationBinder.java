package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestBean;
import io.micronaut.http.bind.RequestBinderRegistry;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestBeanAnnotationBinder<T>
   extends AbstractAnnotatedArgumentBinder<RequestBean, T, HttpRequest<?>>
   implements AnnotatedRequestArgumentBinder<RequestBean, T> {
   private final RequestBinderRegistry requestBinderRegistry;

   public RequestBeanAnnotationBinder(RequestBinderRegistry requestBinderRegistry, ConversionService<?> conversionService) {
      super(conversionService);
      this.requestBinderRegistry = requestBinderRegistry;
   }

   @Override
   public Class<RequestBean> getAnnotationType() {
      return RequestBean.class;
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
      Argument<T> argument = context.getArgument();
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      boolean hasAnnotation = annotationMetadata.hasAnnotation(RequestBean.class);
      if (hasAnnotation) {
         BeanIntrospection<T> introspection = BeanIntrospection.getIntrospection(context.getArgument().getType());
         Map<String, BeanProperty<T, Object>> beanProperties = (Map)introspection.getBeanProperties()
            .stream()
            .collect(Collectors.toMap(Named::getName, p -> p));
         if (introspection.getConstructorArguments().length > 0) {
            Argument<?>[] constructorArguments = introspection.getConstructorArguments();
            Object[] argumentValues = new Object[constructorArguments.length];

            for(int i = 0; i < constructorArguments.length; ++i) {
               Argument<Object> constructorArgument = constructorArguments[i];
               BeanProperty<T, Object> bp = (BeanProperty)beanProperties.get(constructorArgument.getName());
               Argument<Object> argumentToBind;
               if (bp != null) {
                  argumentToBind = bp.asArgument();
               } else {
                  argumentToBind = constructorArgument;
               }

               Optional<Object> bindableResult = this.getBindableResult(source, argumentToBind);
               argumentValues[i] = constructorArgument.isOptional() ? bindableResult : bindableResult.orElse(null);
            }

            return () -> Optional.of(introspection.instantiate(false, argumentValues));
         } else {
            T bean = introspection.instantiate();

            for(BeanProperty<T, Object> property : beanProperties.values()) {
               Argument<Object> propertyArgument = property.asArgument();
               Optional<Object> bindableResult = this.getBindableResult(source, propertyArgument);
               property.set(bean, propertyArgument.isOptional() ? bindableResult : bindableResult.orElse(null));
            }

            return () -> Optional.of(bean);
         }
      } else {
         return ArgumentBinder.BindingResult.EMPTY;
      }
   }

   private Optional<Object> getBindableResult(HttpRequest<?> source, Argument<Object> argument) {
      ArgumentConversionContext<Object> conversionContext = ConversionContext.of(
         argument, (Locale)source.getLocale().orElse(Locale.getDefault()), source.getCharacterEncoding()
      );
      return this.getBindableResult(conversionContext, source);
   }

   private Optional<Object> getBindableResult(ArgumentConversionContext<Object> conversionContext, HttpRequest<?> source) {
      Argument<Object> argument = conversionContext.getArgument();
      Optional<ArgumentBinder<Object, HttpRequest<?>>> binder = this.requestBinderRegistry.findArgumentBinder(argument, source);
      if (!binder.isPresent()) {
         throw new UnsatisfiedArgumentException(argument);
      } else {
         ArgumentBinder.BindingResult<Object> result = ((ArgumentBinder)binder.get()).bind(conversionContext, source);
         if (!result.isSatisfied() || !result.getConversionErrors().isEmpty()) {
            List<ConversionError> errors = result.getConversionErrors();
            if (!errors.isEmpty()) {
               throw new ConversionErrorException(argument, (ConversionError)errors.iterator().next());
            }
         }

         if (!result.isPresentAndSatisfied() && !argument.isNullable() && !argument.getType().isAssignableFrom(Optional.class)) {
            throw new UnsatisfiedArgumentException(argument);
         } else {
            return result.getValue();
         }
      }
   }
}
