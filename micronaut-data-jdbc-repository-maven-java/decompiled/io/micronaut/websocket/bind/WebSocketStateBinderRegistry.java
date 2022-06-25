package io.micronaut.websocket.bind;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.annotation.AnnotatedArgumentBinder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.bind.binders.QueryValueArgumentBinder;
import io.micronaut.websocket.WebSocketSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Internal
public class WebSocketStateBinderRegistry implements ArgumentBinderRegistry<WebSocketState> {
   private final ArgumentBinderRegistry<HttpRequest<?>> requestBinderRegistry;
   private final Map<Class, ArgumentBinder<?, WebSocketState>> byType = new HashMap(5);
   private final ArgumentBinder<Object, HttpRequest<?>> queryValueArgumentBinder;

   public WebSocketStateBinderRegistry(RequestBinderRegistry requestBinderRegistry) {
      this.requestBinderRegistry = requestBinderRegistry;
      ArgumentBinder<Object, WebSocketState> sessionBinder = (context, source) -> () -> Optional.of(source.getSession());
      this.byType.put(WebSocketSession.class, sessionBinder);
      this.queryValueArgumentBinder = new QueryValueArgumentBinder<>(ConversionService.SHARED);
   }

   @Override
   public <T, ST> void addRequestArgumentBinder(ArgumentBinder<T, ST> binder) {
      this.requestBinderRegistry.addRequestArgumentBinder(binder);
   }

   public <T> Optional<ArgumentBinder<T, WebSocketState>> findArgumentBinder(Argument<T> argument, WebSocketState source) {
      Optional<ArgumentBinder<T, HttpRequest<?>>> argumentBinder = this.requestBinderRegistry.findArgumentBinder(argument, source.getOriginatingRequest());
      if (argumentBinder.isPresent()) {
         ArgumentBinder<T, HttpRequest<?>> adapted = (ArgumentBinder)argumentBinder.get();
         boolean isParameterBinder = adapted instanceof AnnotatedArgumentBinder && ((AnnotatedArgumentBinder)adapted).getAnnotationType() == QueryValue.class;
         if (!isParameterBinder) {
            return Optional.of((ArgumentBinder<, WebSocketState>)(context, source1) -> adapted.bind(context, source.getOriginatingRequest()));
         }
      }

      ArgumentBinder binder = (ArgumentBinder)this.byType.get(argument.getType());
      if (binder != null) {
         return Optional.of(binder);
      } else {
         ConvertibleValues<Object> uriVariables = source.getSession().getUriVariables();
         return uriVariables.contains(argument.getName())
            ? Optional.of((ArgumentBinder<, WebSocketState>)(context, s) -> () -> uriVariables.get(argument.getName(), argument))
            : Optional.of((ArgumentBinder<, WebSocketState>)(context, s) -> this.queryValueArgumentBinder.bind(context, s.getOriginatingRequest()));
      }
   }
}
