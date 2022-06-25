package io.micronaut.http.server;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.ReadableBytes;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.http.server.cors.CorsOriginConfiguration;
import io.micronaut.http.server.util.locale.HttpLocaleResolutionConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.executor.ThreadSelection;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@ConfigurationProperties(
   value = "micronaut.server",
   cliPrefix = {""}
)
public class HttpServerConfiguration implements ServerContextPathProvider {
   public static final int DEFAULT_PORT = 8080;
   public static final String PREFIX = "micronaut.server";
   public static final int DEFAULT_RANDOM_PORT = -1;
   public static final long DEFAULT_MAX_REQUEST_SIZE = 10485760L;
   public static final long DEFAULT_READ_IDLE_TIME_MINUTES = 5L;
   public static final long DEFAULT_WRITE_IDLE_TIME_MINUTES = 5L;
   public static final boolean DEFAULT_DATEHEADER = true;
   public static final long DEFAULT_IDLE_TIME_MINUTES = 5L;
   public static final boolean DEFAULT_LOG_HANDLED_EXCEPTIONS = false;
   public static final boolean DEFAULT_DUAL_PROTOCOL = false;
   public static final boolean DEFAULT_HTTP_TO_HTTPS_REDIRECT = false;
   private Integer port;
   private String host;
   private Integer readTimeout;
   private long maxRequestSize = 10485760L;
   private Duration readIdleTimeout = null;
   private Duration writeIdleTimeout = null;
   private Duration idleTimeout = Duration.ofMinutes(5L);
   private HttpServerConfiguration.MultipartConfiguration multipart = new HttpServerConfiguration.MultipartConfiguration();
   private HttpServerConfiguration.CorsConfiguration cors = new HttpServerConfiguration.CorsConfiguration();
   private String serverHeader;
   private boolean dateHeader = true;
   private boolean logHandledExceptions = false;
   private HttpServerConfiguration.HostResolutionConfiguration hostResolution;
   private HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties localeResolution;
   private String clientAddressHeader;
   private String contextPath;
   private boolean dualProtocol = false;
   private boolean httpToHttpsRedirect = false;
   private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
   private final ApplicationConfiguration applicationConfiguration;
   private Charset defaultCharset;
   private ThreadSelection threadSelection = ThreadSelection.MANUAL;

   public HttpServerConfiguration() {
      this.applicationConfiguration = new ApplicationConfiguration();
   }

   @Inject
   public HttpServerConfiguration(ApplicationConfiguration applicationConfiguration) {
      if (applicationConfiguration != null) {
         this.defaultCharset = applicationConfiguration.getDefaultCharset();
      }

      this.applicationConfiguration = applicationConfiguration != null ? applicationConfiguration : new ApplicationConfiguration();
   }

   public HttpVersion getHttpVersion() {
      return this.httpVersion;
   }

   public void setHttpVersion(HttpVersion httpVersion) {
      if (httpVersion != null) {
         this.httpVersion = httpVersion;
      }

   }

   @NonNull
   public ThreadSelection getThreadSelection() {
      return this.threadSelection;
   }

   public void setThreadSelection(ThreadSelection threadSelection) {
      if (threadSelection != null) {
         this.threadSelection = threadSelection;
      }

   }

   public ApplicationConfiguration getApplicationConfiguration() {
      return this.applicationConfiguration;
   }

   public Charset getDefaultCharset() {
      return this.defaultCharset;
   }

   public Optional<Integer> getPort() {
      return Optional.ofNullable(this.port);
   }

   public Optional<String> getHost() {
      return Optional.ofNullable(this.host);
   }

   public Optional<Integer> getReadTimeout() {
      return Optional.ofNullable(this.readTimeout);
   }

   public HttpServerConfiguration.MultipartConfiguration getMultipart() {
      return this.multipart;
   }

   public HttpServerConfiguration.CorsConfiguration getCors() {
      return this.cors;
   }

   public long getMaxRequestSize() {
      return this.maxRequestSize;
   }

   public Duration getReadIdleTimeout() {
      return (Duration)Optional.ofNullable(this.readIdleTimeout).orElse(Duration.ofMinutes(5L));
   }

   public Duration getWriteIdleTimeout() {
      return (Duration)Optional.ofNullable(this.writeIdleTimeout).orElse(Duration.ofMinutes(5L));
   }

   public Duration getIdleTimeout() {
      return this.idleTimeout;
   }

   public Optional<String> getServerHeader() {
      return Optional.ofNullable(this.serverHeader);
   }

   public boolean isDateHeader() {
      return this.dateHeader;
   }

   public boolean isLogHandledExceptions() {
      return this.logHandledExceptions;
   }

   @Nullable
   public HttpServerConfiguration.HostResolutionConfiguration getHostResolution() {
      return this.hostResolution;
   }

   @Nullable
   public HttpLocaleResolutionConfiguration getLocaleResolution() {
      return this.localeResolution;
   }

   public String getClientAddressHeader() {
      return this.clientAddressHeader;
   }

   @Override
   public String getContextPath() {
      return this.contextPath;
   }

   public boolean isDualProtocol() {
      return this.dualProtocol;
   }

   public boolean isHttpToHttpsRedirect() {
      return this.httpToHttpsRedirect;
   }

   public void setDefaultCharset(Charset defaultCharset) {
      this.defaultCharset = defaultCharset;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public void setHost(String host) {
      if (StringUtils.isNotEmpty(host)) {
         this.host = host;
      }

   }

   public void setReadTimeout(Integer readTimeout) {
      this.readTimeout = readTimeout;
   }

   public void setServerHeader(String serverHeader) {
      this.serverHeader = serverHeader;
   }

   public void setMaxRequestSize(@ReadableBytes long maxRequestSize) {
      this.maxRequestSize = maxRequestSize;
   }

   public void setReadIdleTimeout(Duration readIdleTimeout) {
      this.readIdleTimeout = readIdleTimeout;
   }

   public void setWriteIdleTimeout(Duration writeIdleTimeout) {
      this.writeIdleTimeout = writeIdleTimeout;
   }

   public void setIdleTimeout(Duration idleTimeout) {
      if (idleTimeout != null) {
         this.idleTimeout = idleTimeout;
      }

   }

   public void setMultipart(HttpServerConfiguration.MultipartConfiguration multipart) {
      this.multipart = multipart;
   }

   public void setCors(HttpServerConfiguration.CorsConfiguration cors) {
      this.cors = cors;
   }

   public void setDateHeader(boolean dateHeader) {
      this.dateHeader = dateHeader;
   }

   public void setLogHandledExceptions(boolean logHandledExceptions) {
      this.logHandledExceptions = logHandledExceptions;
   }

   public void setHostResolution(HttpServerConfiguration.HostResolutionConfiguration hostResolution) {
      this.hostResolution = hostResolution;
   }

   public void setLocaleResolution(HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties localeResolution) {
      this.localeResolution = localeResolution;
   }

   public void setClientAddressHeader(String clientAddressHeader) {
      this.clientAddressHeader = clientAddressHeader;
   }

   public void setContextPath(String contextPath) {
      this.contextPath = contextPath;
   }

   public void setDualProtocol(boolean dualProtocol) {
      this.dualProtocol = dualProtocol;
   }

   public void setHttpToHttpsRedirect(boolean httpToHttpsRedirect) {
      this.httpToHttpsRedirect = httpToHttpsRedirect;
   }

   @ConfigurationProperties("cors")
   public static class CorsConfiguration implements Toggleable {
      public static final boolean DEFAULT_ENABLED = false;
      public static final boolean DEFAULT_SINGLE_HEADER = false;
      private boolean enabled = false;
      private boolean singleHeader = false;
      private Map<String, CorsOriginConfiguration> configurations = Collections.emptyMap();
      private Map<String, CorsOriginConfiguration> defaultConfiguration = new LinkedHashMap(1);

      @Override
      public boolean isEnabled() {
         return this.enabled;
      }

      public Map<String, CorsOriginConfiguration> getConfigurations() {
         if (this.enabled && this.configurations.isEmpty()) {
            if (this.defaultConfiguration.isEmpty()) {
               this.defaultConfiguration.put("default", new CorsOriginConfiguration());
            }

            return this.defaultConfiguration;
         } else {
            return this.configurations;
         }
      }

      public boolean isSingleHeader() {
         return this.singleHeader;
      }

      public void setEnabled(boolean enabled) {
         this.enabled = enabled;
      }

      public void setConfigurations(Map<String, CorsOriginConfiguration> configurations) {
         this.configurations = configurations;
      }

      public void setSingleHeader(boolean singleHeader) {
         this.singleHeader = singleHeader;
      }
   }

   @ConfigurationProperties("host-resolution")
   public static class HostResolutionConfiguration {
      private static final Boolean DEFAULT_PORT_IN_HOST = false;
      private String hostHeader;
      private String protocolHeader;
      private String portHeader;
      private boolean portInHost = DEFAULT_PORT_IN_HOST;
      private List<Pattern> allowedHosts = Collections.emptyList();

      public String getHostHeader() {
         return this.hostHeader;
      }

      public void setHostHeader(String hostHeader) {
         this.hostHeader = hostHeader;
      }

      public String getProtocolHeader() {
         return this.protocolHeader;
      }

      public void setProtocolHeader(String protocolHeader) {
         this.protocolHeader = protocolHeader;
      }

      public String getPortHeader() {
         return this.portHeader;
      }

      public void setPortHeader(String portHeader) {
         this.portHeader = portHeader;
      }

      public boolean isPortInHost() {
         return this.portInHost;
      }

      public void setPortInHost(boolean portInHost) {
         this.portInHost = portInHost;
      }

      public List<Pattern> getAllowedHosts() {
         return this.allowedHosts;
      }

      public void setAllowedHosts(List<String> allowedHosts) {
         this.allowedHosts = new ArrayList(allowedHosts.size());

         for(String s : allowedHosts) {
            this.allowedHosts.add(Pattern.compile(s));
         }

      }

      public boolean headersConfigured() {
         return this.hostHeader != null || this.protocolHeader != null || this.portHeader != null;
      }
   }

   @ConfigurationProperties("locale-resolution")
   public static class HttpLocaleResolutionConfigurationProperties implements HttpLocaleResolutionConfiguration {
      public static final String PREFIX = "micronaut.server.locale-resolution";
      private static final boolean DEFAULT_HEADER_RESOLUTION = true;
      private Locale fixed;
      private String sessionAttribute;
      private String cookieName;
      private boolean header = true;
      private Locale defaultLocale = Locale.getDefault();

      @NonNull
      @Override
      public Optional<Locale> getFixed() {
         return Optional.ofNullable(this.fixed);
      }

      public void setFixed(@Nullable Locale fixed) {
         this.fixed = fixed;
      }

      @NonNull
      @Override
      public Optional<String> getSessionAttribute() {
         return Optional.ofNullable(this.sessionAttribute);
      }

      public void setSessionAttribute(@Nullable String sessionAttribute) {
         this.sessionAttribute = sessionAttribute;
      }

      @NonNull
      @Override
      public Locale getDefaultLocale() {
         return this.defaultLocale;
      }

      public void setDefaultLocale(@NonNull Locale defaultLocale) {
         this.defaultLocale = defaultLocale;
      }

      @NonNull
      @Override
      public Optional<String> getCookieName() {
         return Optional.ofNullable(this.cookieName);
      }

      public void setCookieName(@Nullable String cookieName) {
         this.cookieName = cookieName;
      }

      @Override
      public boolean isHeader() {
         return this.header;
      }

      public void setHeader(boolean header) {
         this.header = header;
      }
   }

   @ConfigurationProperties("multipart")
   public static class MultipartConfiguration implements Toggleable {
      public static final boolean DEFAULT_ENABLED = false;
      public static final long DEFAULT_MAX_FILE_SIZE = 1048576L;
      public static final boolean DEFAULT_DISK = false;
      public static final boolean DEFAULT_MIXED = false;
      public static final long DEFAULT_THRESHOLD = 10485760L;
      private File location;
      private long maxFileSize = 1048576L;
      private Boolean enabled;
      private boolean disk = false;
      private boolean mixed = false;
      private long threshold = 10485760L;

      public Optional<File> getLocation() {
         return Optional.ofNullable(this.location);
      }

      public long getMaxFileSize() {
         return this.maxFileSize;
      }

      @Override
      public boolean isEnabled() {
         return this.enabled == null ? false : this.enabled;
      }

      @Internal
      public Optional<Boolean> getEnabled() {
         return Optional.ofNullable(this.enabled);
      }

      public boolean isDisk() {
         return this.disk;
      }

      public boolean isMixed() {
         return this.mixed;
      }

      public long getThreshold() {
         return this.threshold;
      }

      public void setLocation(File location) {
         this.location = location;
      }

      public void setMaxFileSize(@ReadableBytes long maxFileSize) {
         this.maxFileSize = maxFileSize;
      }

      public void setEnabled(boolean enabled) {
         this.enabled = enabled;
      }

      public void setDisk(boolean disk) {
         this.disk = disk;
      }

      public void setMixed(boolean mixed) {
         this.mixed = mixed;
      }

      public void setThreshold(@ReadableBytes long threshold) {
         this.threshold = threshold;
      }
   }
}
