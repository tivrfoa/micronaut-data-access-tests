package io.micronaut.management.health.aggregator;

import io.micronaut.management.endpoint.health.HealthLevelOfDetail;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import org.reactivestreams.Publisher;

public interface HealthAggregator<T extends HealthResult> {
   Publisher<T> aggregate(HealthIndicator[] indicators, HealthLevelOfDetail healthLevelOfDetail);

   Publisher<HealthResult> aggregate(String name, Publisher<HealthResult> results);
}
