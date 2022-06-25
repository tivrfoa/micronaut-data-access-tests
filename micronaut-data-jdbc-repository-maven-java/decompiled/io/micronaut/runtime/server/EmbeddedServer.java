package io.micronaut.runtime.server;

import io.micronaut.runtime.EmbeddedApplication;
import java.net.URI;
import java.net.URL;

public interface EmbeddedServer extends EmbeddedApplication<EmbeddedServer> {
   int getPort();

   String getHost();

   String getScheme();

   URL getURL();

   URI getURI();

   @Override
   default boolean isServer() {
      return true;
   }

   default boolean isKeepAlive() {
      return true;
   }
}
