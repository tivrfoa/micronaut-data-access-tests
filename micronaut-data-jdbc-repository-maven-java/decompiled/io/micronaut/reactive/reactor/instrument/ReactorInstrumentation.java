package io.micronaut.reactive.reactor.instrument;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.scheduling.instrument.Instrumentation;
import io.micronaut.scheduling.instrument.InvocationInstrumenter;
import io.micronaut.scheduling.instrument.ReactiveInvocationInstrumenterFactory;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;

@Requirements({@Requires(
   sdk = Requires.Sdk.MICRONAUT,
   version = "2.0.0"
), @Requires(
   classes = {Flux.class, Schedulers.Factory.class}
)})
@Context
@Internal
class ReactorInstrumentation {
   @PostConstruct
   void init(ReactorInstrumentation.ReactorInstrumenterFactory instrumenterFactory) {
      if (instrumenterFactory.hasInstrumenters()) {
         Schedulers.onScheduleHook("micronaut", runnable -> {
            InvocationInstrumenter instrumenter = instrumenterFactory.create();
            return instrumenter != null ? () -> {
               try (Instrumentation ignored = instrumenter.newInstrumentation()) {
                  runnable.run();
               }

            } : runnable;
         });
         Hooks.onEachOperator("micronaut", Operators.lift((scannable, coreSubscriber) -> {
            if (coreSubscriber instanceof ReactorSubscriber) {
               return coreSubscriber;
            } else {
               InvocationInstrumenter instrumenter = instrumenterFactory.create();
               return (CoreSubscriber)(instrumenter != null ? new ReactorSubscriber(instrumenter, coreSubscriber) : coreSubscriber);
            }
         }));
      }

   }

   @PreDestroy
   void removeInstrumentation() {
      Schedulers.removeExecutorServiceDecorator("micronaut");
      Hooks.resetOnEachOperator("micronaut");
   }

   @Context
   @Requires(
      classes = {Flux.class}
   )
   @Internal
   static final class ReactorInstrumenterFactory {
      private final List<ReactiveInvocationInstrumenterFactory> reactiveInvocationInstrumenterFactories;

      ReactorInstrumenterFactory(List<ReactiveInvocationInstrumenterFactory> reactiveInvocationInstrumenterFactories) {
         this.reactiveInvocationInstrumenterFactories = reactiveInvocationInstrumenterFactories;
      }

      public boolean hasInstrumenters() {
         return !this.reactiveInvocationInstrumenterFactories.isEmpty();
      }

      @Nullable
      public InvocationInstrumenter create() {
         List<InvocationInstrumenter> invocationInstrumenter = this.getReactiveInvocationInstrumenters();
         return CollectionUtils.isNotEmpty(invocationInstrumenter) ? InvocationInstrumenter.combine(invocationInstrumenter) : null;
      }

      private List<InvocationInstrumenter> getReactiveInvocationInstrumenters() {
         List<InvocationInstrumenter> instrumenters = new ArrayList(this.reactiveInvocationInstrumenterFactories.size());

         for(ReactiveInvocationInstrumenterFactory instrumenterFactory : this.reactiveInvocationInstrumenterFactories) {
            InvocationInstrumenter instrumenter = instrumenterFactory.newReactiveInvocationInstrumenter();
            if (instrumenter != null) {
               instrumenters.add(instrumenter);
            }
         }

         return instrumenters;
      }
   }
}
