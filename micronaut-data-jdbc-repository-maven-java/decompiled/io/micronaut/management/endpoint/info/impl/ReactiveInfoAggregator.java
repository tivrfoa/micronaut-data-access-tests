package io.micronaut.management.endpoint.info.impl;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.EmptyPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySourcePropertyResolver;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.management.endpoint.info.InfoAggregator;
import io.micronaut.management.endpoint.info.InfoEndpoint;
import io.micronaut.management.endpoint.info.InfoSource;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
@Requires(
   beans = {InfoEndpoint.class}
)
public class ReactiveInfoAggregator implements InfoAggregator<Map<String, Object>> {
   @Override
   public Publisher<Map<String, Object>> aggregate(InfoSource[] sources) {
      return this.aggregateResults(sources)
         .collectList()
         .map(
            list -> {
               PropertySourcePropertyResolver resolver = new PropertySourcePropertyResolver();
               list.stream()
                  .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey()))
                  .forEach(entry -> resolver.addPropertySource((PropertySource)entry.getValue()));
               return resolver.getAllProperties(StringConvention.RAW, MapFormat.MapTransformation.NESTED);
            }
         )
         .flux();
   }

   protected Flux<Entry<Integer, PropertySource>> aggregateResults(InfoSource[] sources) {
      List<Publisher<Entry<Integer, PropertySource>>> publishers = new ArrayList(sources.length);

      for(int i = 0; i < sources.length; ++i) {
         int index = i;
         Mono<Entry<Integer, PropertySource>> single = Mono.from(sources[i].getSource())
            .defaultIfEmpty(new EmptyPropertySource())
            .map(source -> new SimpleEntry(index, source));
         publishers.add(single.flux());
      }

      return Flux.merge(publishers);
   }
}
