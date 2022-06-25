package io.micronaut.data.runtime.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DataSettings {
   String PREFIX = "micronaut.data";
   Logger QUERY_LOG = LoggerFactory.getLogger("io.micronaut.data.query");
}
