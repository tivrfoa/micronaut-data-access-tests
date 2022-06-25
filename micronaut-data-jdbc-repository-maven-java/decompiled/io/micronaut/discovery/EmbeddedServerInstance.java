package io.micronaut.discovery;

import io.micronaut.runtime.server.EmbeddedServer;

public interface EmbeddedServerInstance extends ServiceInstance {
   EmbeddedServer getEmbeddedServer();
}
