package io.micronaut.http.server.util;

import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class DefaultHttpHostResolver implements HttpHostResolver {
   private static final String DEFAULT_HOST = "http://localhost";
   private final BeanProvider<EmbeddedServer> embeddedServer;
   private final HttpServerConfiguration serverConfiguration;

   @Inject
   public DefaultHttpHostResolver(HttpServerConfiguration serverConfiguration, @Nullable BeanProvider<EmbeddedServer> embeddedServer) {
      this.serverConfiguration = serverConfiguration;
      this.embeddedServer = embeddedServer;
   }

   @NonNull
   @Override
   public String resolve(@Nullable HttpRequest request) {
      String host;
      if (request != null) {
         HttpServerConfiguration.HostResolutionConfiguration configuration = this.serverConfiguration.getHostResolution();
         if (configuration != null && configuration.headersConfigured()) {
            host = this.getConfiguredHost(request, configuration);
         } else {
            host = this.getDefaultHost(request);
         }
      } else {
         host = this.getEmbeddedHost();
      }

      return this.validateHost(host);
   }

   @NonNull
   protected String validateHost(@NonNull String host) {
      if (!host.equals("http://localhost")) {
         HttpServerConfiguration.HostResolutionConfiguration configuration = this.serverConfiguration.getHostResolution();
         if (configuration != null) {
            List<Pattern> allowedHosts = configuration.getAllowedHosts();
            if (!allowedHosts.isEmpty() && allowedHosts.stream().map(pattern -> pattern.matcher(host)).noneMatch(Matcher::matches)) {
               return "http://localhost";
            }
         }
      }

      return host;
   }

   protected String getEmbeddedHost() {
      if (this.embeddedServer != null) {
         EmbeddedServer server = this.embeddedServer.get();
         return this.createHost(server.getScheme(), server.getHost(), server.getPort());
      } else {
         return "http://localhost";
      }
   }

   protected String getDefaultHost(HttpRequest request) {
      ProxyHeaderParser proxyHeaderParser = new ProxyHeaderParser(request);
      if (proxyHeaderParser.getHost() != null) {
         return this.createHost(proxyHeaderParser.getScheme(), proxyHeaderParser.getHost(), proxyHeaderParser.getPort());
      } else {
         String hostHeader = request.getHeaders().get("Host");
         if (hostHeader != null) {
            return this.getConfiguredHost(request, null, "Host", null, true);
         } else {
            URI uri = request.getUri();
            if (uri.getHost() != null) {
               Integer port = uri.getPort();
               if (port < 0) {
                  port = null;
               }

               return this.createHost(uri.getScheme(), uri.getHost(), port);
            } else {
               return this.getEmbeddedHost();
            }
         }
      }
   }

   protected String getConfiguredHost(HttpRequest request, HttpServerConfiguration.HostResolutionConfiguration configuration) {
      return this.getConfiguredHost(
         request, configuration.getProtocolHeader(), configuration.getHostHeader(), configuration.getPortHeader(), configuration.isPortInHost()
      );
   }

   protected String getConfiguredHost(HttpRequest request, String schemeHeader, String hostHeader, String portHeader, boolean isPortInHost) {
      HttpHeaders headers = request.getHeaders();
      String scheme = null;
      if (schemeHeader != null) {
         scheme = headers.get(schemeHeader);
      }

      if (scheme == null) {
         scheme = request.getUri().getScheme();
      }

      if (scheme == null && this.embeddedServer != null) {
         scheme = this.embeddedServer.get().getScheme();
      }

      String host = null;
      if (hostHeader != null) {
         host = headers.get(hostHeader);
      }

      if (host == null) {
         host = request.getUri().getHost();
      }

      if (host == null && this.embeddedServer != null) {
         host = this.embeddedServer.get().getHost();
      }

      Integer port;
      if (isPortInHost && host != null && host.contains(":")) {
         String[] parts = host.split(":");
         host = parts[0].trim();
         port = Integer.valueOf(parts[1].trim());
      } else if (portHeader != null) {
         port = (Integer)headers.get(portHeader, Integer.class).orElse(null);
      } else {
         port = request.getUri().getPort();
         if (port < 0) {
            port = null;
         }
      }

      return this.createHost(scheme, host, port);
   }

   private String createHost(@Nullable String scheme, @Nullable String host, @Nullable Integer port) {
      scheme = scheme == null ? "http" : scheme;
      host = host == null ? "localhost" : host;
      return port != null && port != 80 && port != 443 ? scheme + "://" + host + ":" + port : scheme + "://" + host;
   }
}
