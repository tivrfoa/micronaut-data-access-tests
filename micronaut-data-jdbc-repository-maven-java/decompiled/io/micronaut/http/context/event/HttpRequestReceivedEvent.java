package io.micronaut.http.context.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;

public class HttpRequestReceivedEvent extends ApplicationEvent {
   public HttpRequestReceivedEvent(@NonNull HttpRequest<?> request) {
      super(request);
   }

   @NonNull
   public HttpRequest<?> getSource() {
      return (HttpRequest<?>)super.getSource();
   }
}
