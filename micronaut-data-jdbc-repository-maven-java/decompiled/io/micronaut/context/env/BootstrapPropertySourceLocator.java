package io.micronaut.context.env;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Blocking;
import java.util.Collections;

public interface BootstrapPropertySourceLocator {
   BootstrapPropertySourceLocator EMPTY_LOCATOR = environment -> Collections.emptySet();

   @Blocking
   Iterable<PropertySource> findPropertySources(Environment environment) throws ConfigurationException;
}
