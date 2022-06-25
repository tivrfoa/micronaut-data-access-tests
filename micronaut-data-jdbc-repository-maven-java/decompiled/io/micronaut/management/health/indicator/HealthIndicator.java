package io.micronaut.management.health.indicator;

import io.micronaut.core.order.Ordered;
import org.reactivestreams.Publisher;

public interface HealthIndicator extends Ordered {
   Publisher<HealthResult> getResult();
}
