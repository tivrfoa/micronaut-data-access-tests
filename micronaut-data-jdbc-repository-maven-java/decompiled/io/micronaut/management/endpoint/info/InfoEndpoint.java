package io.micronaut.management.endpoint.info;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Endpoint("info")
public class InfoEndpoint {
   public static final String NAME = "info";
   public static final String PREFIX = "endpoints.info";
   private InfoAggregator infoAggregator;
   private InfoSource[] infoSources;

   public InfoEndpoint(InfoAggregator infoAggregator, InfoSource[] infoSources) {
      this.infoAggregator = infoAggregator;
      this.infoSources = infoSources;
   }

   @Read
   @SingleResult
   Publisher getInfo() {
      return Mono.from(this.infoAggregator.aggregate(this.infoSources));
   }
}
