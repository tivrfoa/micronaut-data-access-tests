package io.micronaut.reactive.flow.converters;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import jakarta.inject.Singleton;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.reactive.ReactiveFlowKt;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Requires(
   classes = {Flux.class, ReactiveFlowKt.class}
)
public class FlowConverterRegistrar implements TypeConverterRegistrar {
   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(Flow.class, Flux.class, flow -> Flux.from(ReactiveFlowKt.asPublisher(flow)));
      conversionService.addConverter(Flow.class, Publisher.class, ReactiveFlowKt::asPublisher);
      conversionService.addConverter(Publisher.class, Flow.class, ReactiveFlowKt::asFlow);
   }
}
