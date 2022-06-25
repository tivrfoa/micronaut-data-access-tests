package io.micronaut.http.context.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;

public class HttpRequestTerminatedEvent extends ApplicationEvent {
   public HttpRequestTerminatedEvent(@NonNull HttpRequest<?> request) {
      super(request);
   }

   @NonNull
   public HttpRequest<?> getSource() {
      return (HttpRequest<?>)super.getSource();
   }
}
