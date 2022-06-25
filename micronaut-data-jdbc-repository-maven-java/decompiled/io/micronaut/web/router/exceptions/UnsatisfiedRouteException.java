package io.micronaut.web.router.exceptions;

import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.Argument;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.CookieValue;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.RequestAttribute;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class UnsatisfiedRouteException extends RoutingException {
   private final Argument<?> argument;

   UnsatisfiedRouteException(String message, Argument<?> argument) {
      super(message);
      this.argument = argument;
   }

   public static UnsatisfiedRouteException create(Argument<?> argument) {
      Optional<Class<? extends Annotation>> classOptional = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(Bindable.class);
      if (classOptional.isPresent()) {
         Class<? extends Annotation> clazz = (Class)classOptional.get();
         String name = (String)argument.getAnnotationMetadata().stringValue(clazz).orElse(argument.getName());
         if (clazz == Body.class) {
            throw new UnsatisfiedBodyRouteException(name, argument);
         } else if (clazz == QueryValue.class) {
            throw new UnsatisfiedQueryValueRouteException(name, argument);
         } else if (clazz == PathVariable.class) {
            throw new UnsatisfiedPathVariableRouteException(name, argument);
         } else if (clazz == Header.class) {
            throw new UnsatisfiedHeaderRouteException(name, argument);
         } else if (clazz == Part.class) {
            throw new UnsatisfiedPartRouteException(name, argument);
         } else if (clazz == RequestAttribute.class) {
            throw new UnsatisfiedRequestAttributeRouteException(name, argument);
         } else if (clazz == CookieValue.class) {
            throw new UnsatisfiedCookieValueRouteException(name, argument);
         } else {
            throw new UnsatisfiedRouteException("Required " + clazz.getSimpleName() + " [" + name + "] not specified", argument);
         }
      } else {
         throw new UnsatisfiedRouteException("Required argument [" + argument + "] not specified", argument);
      }
   }

   public Argument<?> getArgument() {
      return this.argument;
   }
}
