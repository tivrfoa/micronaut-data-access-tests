package io.micronaut.discovery.config;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.naming.Described;
import org.reactivestreams.Publisher;

public interface ConfigurationClient extends Described {
   String CONFIGURATION_PREFIX = "micronaut.config-client";
   String ENABLED = "micronaut.config-client.enabled";
   String READ_TIMEOUT = "micronaut.config-client.read-timeout";

   Publisher<PropertySource> getPropertySources(Environment environment);
}
