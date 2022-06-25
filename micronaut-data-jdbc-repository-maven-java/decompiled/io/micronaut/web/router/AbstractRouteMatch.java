package io.micronaut.web.router;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;

abstract class AbstractRouteMatch<T, R> implements MethodBasedRouteMatch<T, R> {
   protected final MethodExecutionHandle<T, R> executableMethod;
   protected final ConversionService<?> conversionService;
   protected final DefaultRouteBuilder.AbstractRoute abstractRoute;
   protected final List<MediaType> consumedMediaTypes;
   protected final List<MediaType> producedMediaTypes;

   protected AbstractRouteMatch(DefaultRouteBuilder.AbstractRoute abstractRoute, ConversionService<?> conversionService) {
      this.abstractRoute = abstractRoute;
      this.executableMethod = abstractRoute.targetMethod;
      this.conversionService = conversionService;
      this.consumedMediaTypes = abstractRoute.getConsumes();
      this.producedMediaTypes = abstractRoute.getProduces();
   }

   @Override
   public final boolean isSuspended() {
      return this.abstractRoute.isSuspended();
   }

   @Override
   public final boolean isReactive() {
      return this.abstractRoute.isReactive();
   }

   @Override
   public final boolean isSingleResult() {
      return this.abstractRoute.isSingleResult();
   }

   @Override
   public final boolean isSpecifiedSingle() {
      return this.abstractRoute.isSpecifiedSingle();
   }

   @Override
   public final boolean isAsync() {
      return this.abstractRoute.isAsync();
   }

   @Override
   public final boolean isVoid() {
      return this.abstractRoute.isVoid();
   }

   @Override
   public boolean isAsyncOrReactive() {
      return this.abstractRoute.isAsyncOrReactive();
   }

   @Override
   public T getTarget() {
      return this.executableMethod.getTarget();
   }

   @NonNull
   @Override
   public ExecutableMethod<?, R> getExecutableMethod() {
      return this.executableMethod.getExecutableMethod();
   }

   @Override
   public List<MediaType> getProduces() {
      return this.abstractRoute.getProduces();
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.executableMethod.getAnnotationMetadata();
   }

   @Override
   public Optional<Argument<?>> getBodyArgument() {
      Argument<?> arg = this.abstractRoute.bodyArgument;
      if (arg != null) {
         return Optional.of(arg);
      } else {
         String bodyArgument = this.abstractRoute.bodyArgumentName;
         return bodyArgument != null ? Optional.ofNullable(this.abstractRoute.requiredInputs.get(bodyArgument)) : Optional.empty();
      }
   }

   @Override
   public boolean isRequiredInput(String name) {
      return this.abstractRoute.requiredInputs.containsKey(name);
   }

   @Override
   public Optional<Argument<?>> getRequiredInput(String name) {
      return Optional.ofNullable(this.abstractRoute.requiredInputs.get(name));
   }

   @Override
   public boolean isExecutable() {
      Map<String, Object> variables = this.getVariableValues();

      for(Entry<String, Argument> entry : this.abstractRoute.requiredInputs.entrySet()) {
         Object value = variables.get(entry.getKey());
         if (value == null || value instanceof UnresolvedArgument) {
            return false;
         }
      }

      Optional<Argument<?>> bodyArgument = this.getBodyArgument();
      if (!bodyArgument.isPresent()) {
         return true;
      } else {
         Object value = variables.get(((Argument)bodyArgument.get()).getName());
         return value != null && !(value instanceof UnresolvedArgument);
      }
   }

   @Override
   public Method getTargetMethod() {
      return this.executableMethod.getTargetMethod();
   }

   @Override
   public String getMethodName() {
      return this.executableMethod.getMethodName();
   }

   @Override
   public Class getDeclaringType() {
      return this.executableMethod.getDeclaringType();
   }

   @Override
   public Argument[] getArguments() {
      return this.executableMethod.getArguments();
   }

   public boolean test(HttpRequest request) {
      for(Predicate<HttpRequest<?>> condition : this.abstractRoute.conditions) {
         if (!condition.test(request)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public ReturnType<R> getReturnType() {
      return this.executableMethod.getReturnType();
   }

   @Override
   public R invoke(Object... arguments) {
      ConversionService<?> conversionService = this.conversionService;
      Argument[] targetArguments = this.getArguments();
      if (targetArguments.length == 0) {
         return this.executableMethod.invoke(new Object[0]);
      } else {
         List<Object> argumentList = new ArrayList(arguments.length);
         Map<String, Object> variables = this.getVariableValues();
         Iterator<Object> valueIterator = variables.values().iterator();
         int i = 0;

         for(Argument<?> targetArgument : targetArguments) {
            String name = targetArgument.getName();
            Object value = variables.get(name);
            if (value != null) {
               Optional<?> result = conversionService.convert(value, targetArgument.getType());
               argumentList.add(result.orElseThrow(() -> new IllegalArgumentException("Wrong argument types to method: " + this.executableMethod)));
            } else if (valueIterator.hasNext()) {
               Optional<?> result = conversionService.convert(valueIterator.next(), targetArgument.getType());
               argumentList.add(result.orElseThrow(() -> new IllegalArgumentException("Wrong argument types to method: " + this.executableMethod)));
            } else {
               if (i >= arguments.length) {
                  throw new IllegalArgumentException("Wrong number of arguments to method: " + this.executableMethod);
               }

               Optional<?> result = conversionService.convert(arguments[i++], targetArgument.getType());
               argumentList.add(result.orElseThrow(() -> new IllegalArgumentException("Wrong argument types to method: " + this.executableMethod)));
            }
         }

         return this.executableMethod.invoke(argumentList.toArray());
      }
   }

   @Override
   public R execute(Map<String, Object> argumentValues) {
      Argument[] targetArguments = this.getArguments();
      if (targetArguments.length == 0) {
         return this.executableMethod.invoke(new Object[0]);
      } else {
         ConversionService<?> conversionService = this.conversionService;
         Map<String, Object> uriVariables = this.getVariableValues();
         List<Object> argumentList = new ArrayList(argumentValues.size());

         for(Entry<String, Argument> entry : this.abstractRoute.requiredInputs.entrySet()) {
            Argument argument = (Argument)entry.getValue();
            String name = (String)entry.getKey();
            Object value = DefaultRouteBuilder.NO_VALUE;
            if (uriVariables.containsKey(name)) {
               value = uriVariables.get(name);
            } else if (argumentValues.containsKey(name)) {
               value = argumentValues.get(name);
            }

            Class argumentType = argument.getType();
            if (value instanceof UnresolvedArgument) {
               UnresolvedArgument<?> unresolved = (UnresolvedArgument)value;
               ArgumentBinder.BindingResult<?> bindingResult = (ArgumentBinder.BindingResult)unresolved.get();
               if (bindingResult.isPresentAndSatisfied()) {
                  Object resolved = bindingResult.get();
                  if (resolved instanceof ConversionError) {
                     ConversionError conversionError = (ConversionError)resolved;
                     throw new ConversionErrorException(argument, conversionError);
                  }

                  this.convertValueAndAddToList(conversionService, argumentList, argument, resolved, argumentType);
               } else {
                  if (!argument.isNullable()) {
                     List<ConversionError> conversionErrors = bindingResult.getConversionErrors();
                     if (!conversionErrors.isEmpty()) {
                        ConversionError conversionError = (ConversionError)conversionErrors.iterator().next();
                        throw new ConversionErrorException(argument, conversionError);
                     }

                     throw UnsatisfiedRouteException.create(argument);
                  }

                  argumentList.add(null);
               }
            } else if (value instanceof NullArgument) {
               argumentList.add(null);
            } else {
               if (value instanceof ConversionError) {
                  throw new ConversionErrorException(argument, (ConversionError)value);
               }

               if (value == DefaultRouteBuilder.NO_VALUE) {
                  throw UnsatisfiedRouteException.create(argument);
               }

               this.convertValueAndAddToList(conversionService, argumentList, argument, value, argumentType);
            }
         }

         return this.executableMethod.invoke(argumentList.toArray());
      }
   }

   private void convertValueAndAddToList(ConversionService conversionService, List argumentList, Argument argument, Object value, Class argumentType) {
      if (argumentType.isInstance(value)) {
         if (argument.isContainerType()) {
            if (argument.hasTypeVariables()) {
               ConversionContext conversionContext = ConversionContext.of(argument);
               Optional<?> result = conversionService.convert(value, argumentType, conversionContext);
               argumentList.add(this.resolveValueOrError(argument, conversionContext, result));
            } else {
               argumentList.add(value);
            }
         } else {
            argumentList.add(value);
         }
      } else {
         ConversionContext conversionContext = ConversionContext.of(argument);
         Optional<?> result = conversionService.convert(value, argumentType, conversionContext);
         argumentList.add(this.resolveValueOrError(argument, conversionContext, result));
      }

   }

   @Override
   public boolean doesConsume(MediaType contentType) {
      return contentType == null || this.abstractRoute.consumesMediaTypesContainsAll || this.explicitlyConsumes(contentType);
   }

   @Override
   public boolean doesProduce(@Nullable Collection<MediaType> acceptableTypes) {
      return this.abstractRoute.producesMediaTypesContainsAll || this.anyMediaTypesMatch(this.producedMediaTypes, acceptableTypes);
   }

   @Override
   public boolean doesProduce(@Nullable MediaType acceptableType) {
      return this.abstractRoute.producesMediaTypesContainsAll
         || acceptableType == null
         || acceptableType.equals(MediaType.ALL_TYPE)
         || this.producedMediaTypes.contains(acceptableType);
   }

   private boolean anyMediaTypesMatch(List<MediaType> producedMediaTypes, Collection<MediaType> acceptableTypes) {
      if (CollectionUtils.isEmpty(acceptableTypes)) {
         return true;
      } else {
         for(MediaType acceptableType : acceptableTypes) {
            if (acceptableType.equals(MediaType.ALL_TYPE) || producedMediaTypes.contains(acceptableType)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean explicitlyConsumes(MediaType contentType) {
      return this.consumedMediaTypes.contains(contentType);
   }

   @Override
   public boolean explicitlyProduces(MediaType contentType) {
      return this.producedMediaTypes == null || this.producedMediaTypes.isEmpty() || this.producedMediaTypes.contains(contentType);
   }

   @Override
   public RouteMatch<R> fulfill(Map<String, Object> argumentValues) {
      if (CollectionUtils.isEmpty(argumentValues)) {
         return this;
      } else {
         Map<String, Object> oldVariables = this.getVariableValues();
         Map<String, Object> newVariables = new LinkedHashMap(oldVariables);
         Argument<?> bodyArgument = (Argument)this.getBodyArgument().orElse(null);
         Argument[] arguments = this.getArguments();
         Collection<Argument> requiredArguments = this.getRequiredArguments();
         boolean hasRequiredArguments = CollectionUtils.isNotEmpty(requiredArguments);

         for(Argument requiredArgument : arguments) {
            String argumentName = requiredArgument.getName();
            if (argumentValues.containsKey(argumentName)) {
               Object value = argumentValues.get(argumentName);
               if (bodyArgument != null && bodyArgument.getName().equals(argumentName)) {
                  requiredArgument = bodyArgument;
               }

               if (hasRequiredArguments) {
                  requiredArguments.remove(requiredArgument);
               }

               if (value != null) {
                  String name = this.abstractRoute.resolveInputName(requiredArgument);
                  if (!(value instanceof UnresolvedArgument) && !(value instanceof NullArgument)) {
                     Class type = requiredArgument.getType();
                     if (type.isInstance(value)) {
                        newVariables.put(name, value);
                     } else {
                        ArgumentConversionContext conversionContext = ConversionContext.of(requiredArgument);
                        Optional converted = this.conversionService.convert(value, conversionContext);
                        Object result = converted.isPresent() ? converted.get() : conversionContext.getLastError().orElse(null);
                        if (result != null) {
                           newVariables.put(name, result);
                        }
                     }
                  } else {
                     newVariables.put(name, value);
                  }
               }
            }
         }

         return this.newFulfilled(newVariables, (List<Argument>)requiredArguments);
      }
   }

   @Override
   public HttpStatus findStatus(HttpStatus defaultStatus) {
      return this.abstractRoute.definedStatus == null ? defaultStatus : this.abstractRoute.definedStatus;
   }

   @Override
   public boolean isWebSocketRoute() {
      return this.abstractRoute.isWebSocketRoute;
   }

   protected Object resolveValueOrError(Argument argument, ConversionContext conversionContext, Optional<?> result) {
      if (!result.isPresent()) {
         Optional<ConversionError> lastError = conversionContext.getLastError();
         if (!lastError.isPresent() && argument.isDeclaredNullable()) {
            return null;
         } else {
            throw (RuntimeException)lastError.map(conversionError -> new ConversionErrorException(argument, conversionError))
               .orElseGet(() -> UnsatisfiedRouteException.create(argument));
         }
      } else {
         return result.get();
      }
   }

   protected abstract RouteMatch<R> newFulfilled(Map<String, Object> newVariables, List<Argument> requiredArguments);
}
