package io.micronaut.management.endpoint.info;

import io.micronaut.context.env.PropertySource;
import io.micronaut.core.order.Ordered;
import org.reactivestreams.Publisher;

public interface InfoSource extends Ordered {
   Publisher<PropertySource> getSource();
}
