package io.micronaut.http.server.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.scheduling.instrument.Instrumentation;
import io.micronaut.scheduling.instrument.InvocationInstrumenter;

class ServerRequestContextInvocationInstrumenter implements InvocationInstrumenter {
   private final HttpRequest<?> invocationRequest;

   public ServerRequestContextInvocationInstrumenter(HttpRequest<?> invocationRequest) {
      this.invocationRequest = invocationRequest;
   }

   @NonNull
   @Override
   public Instrumentation newInstrumentation() {
      HttpRequest<?> currentRequest = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      boolean isSet;
      if (this.invocationRequest != currentRequest) {
         isSet = true;
         ServerRequestContext.set(this.invocationRequest);
      } else {
         isSet = false;
      }

      return cleanup -> {
         if (cleanup) {
            ServerRequestContext.set(null);
         } else if (isSet) {
            ServerRequestContext.set(currentRequest);
         }

      };
   }
}
