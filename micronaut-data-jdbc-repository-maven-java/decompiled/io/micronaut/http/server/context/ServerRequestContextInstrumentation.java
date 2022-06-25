package io.micronaut.http.server.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.scheduling.instrument.InvocationInstrumenter;
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory;
import io.micronaut.scheduling.instrument.ReactiveInvocationInstrumenterFactory;
import jakarta.inject.Singleton;

@Singleton
@Internal
final class ServerRequestContextInstrumentation implements InvocationInstrumenterFactory, ReactiveInvocationInstrumenterFactory {
   @Override
   public InvocationInstrumenter newInvocationInstrumenter() {
      return (InvocationInstrumenter)ServerRequestContext.currentRequest().map(ServerRequestContextInvocationInstrumenter::new).orElse(null);
   }

   @Override
   public InvocationInstrumenter newReactiveInvocationInstrumenter() {
      return this.newInvocationInstrumenter();
   }
}
