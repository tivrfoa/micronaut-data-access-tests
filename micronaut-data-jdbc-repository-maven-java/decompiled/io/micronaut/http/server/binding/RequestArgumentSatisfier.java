package io.micronaut.http.server.binding;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.bind.binders.BodyArgumentBinder;
import io.micronaut.http.bind.binders.NonBlockingBodyArgumentBinder;
import io.micronaut.http.bind.binders.RequestBeanAnnotationBinder;
import io.micronaut.web.router.NullArgument;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.UnresolvedArgument;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
@Internal
public class RequestArgumentSatisfier {
   private final RequestBinderRegistry binderRegistry;

   public RequestArgumentSatisfier(RequestBinderRegistry requestBinderRegistry) {
      this.binderRegistry = requestBinderRegistry;
   }

   public RequestBinderRegistry getBinderRegistry() {
      return this.binderRegistry;
   }

   public RouteMatch<?> fulfillArgumentRequirements(RouteMatch<?> route, HttpRequest<?> request, boolean satisfyOptionals) {
      Collection<Argument> requiredArguments = route.getRequiredArguments();
      Map<String, Object> argumentValues;
      if (requiredArguments.isEmpty()) {
         argumentValues = Collections.emptyMap();
      } else {
         argumentValues = new LinkedHashMap(requiredArguments.size());

         for(Argument argument : requiredArguments) {
            this.getValueForArgument(argument, request, satisfyOptionals).ifPresent(value -> argumentValues.put(argument.getName(), value));
         }
      }

      return route.fulfill(argumentValues);
   }

   protected Optional<Object> getValueForArgument(Argument argument, HttpRequest<?> request, boolean satisfyOptionals) {
      Object value = null;
      Optional<ArgumentBinder> registeredBinder = this.binderRegistry.findArgumentBinder(argument, request);
      if (registeredBinder.isPresent()) {
         ArgumentBinder argumentBinder = (ArgumentBinder)registeredBinder.get();
         ArgumentConversionContext conversionContext = ConversionContext.of(argument, (Locale)request.getLocale().orElse(null), request.getCharacterEncoding());
         if (argumentBinder instanceof BodyArgumentBinder) {
            if (argumentBinder instanceof NonBlockingBodyArgumentBinder) {
               ArgumentBinder.BindingResult bindingResult = argumentBinder.bind(conversionContext, request);
               if (bindingResult.isPresentAndSatisfied()) {
                  value = bindingResult.get();
               } else if (bindingResult.isSatisfied() && argument.isNullable()) {
                  value = NullArgument.INSTANCE;
               }
            } else {
               value = this.getValueForBlockingBodyArgumentBinder(request, argumentBinder, conversionContext);
            }
         } else if (argumentBinder instanceof RequestBeanAnnotationBinder) {
            value = () -> argumentBinder.bind(conversionContext, request);
         } else {
            ArgumentBinder.BindingResult bindingResult = argumentBinder.bind(conversionContext, request);
            if (argument.getType() == Optional.class) {
               if (bindingResult.isSatisfied() || satisfyOptionals) {
                  Optional optionalValue = bindingResult.getValue();
                  if (optionalValue.isPresent()) {
                     value = optionalValue.get();
                  } else {
                     value = optionalValue;
                  }
               }
            } else if (bindingResult.isPresentAndSatisfied()) {
               value = bindingResult.get();
            } else if (bindingResult.isSatisfied() && argument.isNullable()) {
               value = NullArgument.INSTANCE;
            } else if (HttpMethod.requiresRequestBody(request.getMethod()) || argument.isNullable() || conversionContext.hasErrors()) {
               value = () -> {
                  ArgumentBinder.BindingResult result = argumentBinder.bind(conversionContext, request);
                  Optional<ConversionError> lastError = conversionContext.getLastError();
                  return lastError.isPresent() ? () -> lastError : result;
               };
            }
         }
      }

      return Optional.ofNullable(value);
   }

   private Object getValueForBlockingBodyArgumentBinder(HttpRequest<?> request, ArgumentBinder argumentBinder, ArgumentConversionContext conversionContext) {
      return (UnresolvedArgument)() -> argumentBinder.bind(conversionContext, request);
   }
}
