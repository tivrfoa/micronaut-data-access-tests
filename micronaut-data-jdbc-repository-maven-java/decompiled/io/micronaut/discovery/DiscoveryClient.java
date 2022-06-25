package io.micronaut.discovery;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.naming.Described;
import java.io.Closeable;
import java.util.List;
import org.reactivestreams.Publisher;

@Indexed(DiscoveryClient.class)
public interface DiscoveryClient extends Closeable, AutoCloseable, Described {
   @SingleResult
   Publisher<List<ServiceInstance>> getInstances(String serviceId);

   @SingleResult
   Publisher<List<String>> getServiceIds();
}
