package io.micronaut.http.server.util;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.server.HttpServerConfiguration;
import jakarta.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.List;

@Singleton
public class DefaultHttpClientAddressResolver implements HttpClientAddressResolver {
   private final HttpServerConfiguration serverConfiguration;

   public DefaultHttpClientAddressResolver(HttpServerConfiguration serverConfiguration) {
      this.serverConfiguration = serverConfiguration;
   }

   @Nullable
   @Override
   public String resolve(@NonNull HttpRequest request) {
      String configuredHeader = this.serverConfiguration.getClientAddressHeader();
      if (configuredHeader != null) {
         return request.getHeaders().get(configuredHeader);
      } else {
         ProxyHeaderParser proxyHeaderParser = new ProxyHeaderParser(request);
         List<String> addresses = proxyHeaderParser.getFor();
         if (addresses.isEmpty()) {
            InetSocketAddress address = request.getRemoteAddress();
            return address != null ? address.getHostString() : null;
         } else {
            return (String)addresses.get(0);
         }
      }
   }
}
