package io.micronaut.http.client;

import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.ReadableBytes;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.ssl.ClientSslConfiguration;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.logging.LogLevel;
import io.micronaut.runtime.ApplicationConfiguration;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.Proxy.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ThreadFactory;

public abstract class HttpClientConfiguration {
   public static final long DEFAULT_READ_TIMEOUT_SECONDS = 10L;
   public static final long DEFAULT_READ_IDLE_TIMEOUT_MINUTES = 5L;
   public static final long DEFAULT_CONNECTION_POOL_IDLE_TIMEOUT_SECONDS = 0L;
   public static final long DEFAULT_SHUTDOWN_QUIET_PERIOD_MILLISECONDS = 1L;
   public static final long DEFAULT_SHUTDOWN_TIMEOUT_MILLISECONDS = 100L;
   public static final int DEFAULT_MAX_CONTENT_LENGTH = 10485760;
   public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;
   public static final boolean DEFAULT_EXCEPTION_ON_ERROR_STATUS = true;
   private Map<String, Object> channelOptions = Collections.emptyMap();
   private Integer numOfThreads = null;
   private Class<? extends ThreadFactory> threadFactory;
   private Duration connectTimeout;
   private Duration connectTtl;
   private Duration readTimeout = Duration.ofSeconds(10L);
   private Duration readIdleTimeout = Duration.of(5L, ChronoUnit.MINUTES);
   private Duration connectionPoolIdleTimeout = Duration.ofSeconds(0L);
   private Duration shutdownQuietPeriod = Duration.ofMillis(1L);
   private Duration shutdownTimeout = Duration.ofMillis(100L);
   private int maxContentLength = 10485760;
   private Type proxyType = Type.DIRECT;
   private SocketAddress proxyAddress;
   private String proxyUsername;
   private String proxyPassword;
   private ProxySelector proxySelector;
   private Charset defaultCharset = StandardCharsets.UTF_8;
   private boolean followRedirects = true;
   private boolean exceptionOnErrorStatus = true;
   private SslConfiguration sslConfiguration = new ClientSslConfiguration();
   private String loggerName;
   private String eventLoopGroup = "default";
   private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
   private LogLevel logLevel;

   public HttpClientConfiguration() {
   }

   public HttpClientConfiguration(ApplicationConfiguration applicationConfiguration) {
      if (applicationConfiguration != null) {
         this.defaultCharset = applicationConfiguration.getDefaultCharset();
      }

   }

   public HttpClientConfiguration(HttpClientConfiguration copy) {
      if (copy != null) {
         this.channelOptions = copy.channelOptions;
         this.numOfThreads = copy.numOfThreads;
         this.connectTimeout = copy.connectTimeout;
         this.connectTtl = copy.connectTtl;
         this.defaultCharset = copy.defaultCharset;
         this.exceptionOnErrorStatus = copy.exceptionOnErrorStatus;
         this.eventLoopGroup = copy.eventLoopGroup;
         this.followRedirects = copy.followRedirects;
         this.logLevel = copy.logLevel;
         this.loggerName = copy.loggerName;
         this.maxContentLength = copy.maxContentLength;
         this.proxyAddress = copy.proxyAddress;
         this.proxyPassword = copy.proxyPassword;
         this.proxySelector = copy.proxySelector;
         this.proxyType = copy.proxyType;
         this.proxyUsername = copy.proxyUsername;
         this.readIdleTimeout = copy.readIdleTimeout;
         this.connectionPoolIdleTimeout = copy.connectionPoolIdleTimeout;
         this.readTimeout = copy.readTimeout;
         this.shutdownTimeout = copy.shutdownTimeout;
         this.shutdownQuietPeriod = copy.shutdownQuietPeriod;
         this.sslConfiguration = copy.sslConfiguration;
         this.threadFactory = copy.threadFactory;
         this.httpVersion = copy.httpVersion;
      }

   }

   public HttpVersion getHttpVersion() {
      return this.httpVersion;
   }

   public void setHttpVersion(HttpVersion httpVersion) {
      if (httpVersion != null) {
         this.httpVersion = httpVersion;
      }

   }

   public Optional<LogLevel> getLogLevel() {
      return Optional.ofNullable(this.logLevel);
   }

   public void setLogLevel(@Nullable LogLevel logLevel) {
      this.logLevel = logLevel;
   }

   public String getEventLoopGroup() {
      return this.eventLoopGroup;
   }

   public void setEventLoopGroup(@NonNull String eventLoopGroup) {
      ArgumentUtils.requireNonNull("eventLoopGroup", eventLoopGroup);
      this.eventLoopGroup = eventLoopGroup;
   }

   public abstract HttpClientConfiguration.ConnectionPoolConfiguration getConnectionPoolConfiguration();

   public SslConfiguration getSslConfiguration() {
      return this.sslConfiguration;
   }

   public void setSslConfiguration(SslConfiguration sslConfiguration) {
      this.sslConfiguration = sslConfiguration;
   }

   public boolean isFollowRedirects() {
      return this.followRedirects;
   }

   public boolean isExceptionOnErrorStatus() {
      return this.exceptionOnErrorStatus;
   }

   public void setExceptionOnErrorStatus(boolean exceptionOnErrorStatus) {
      this.exceptionOnErrorStatus = exceptionOnErrorStatus;
   }

   public Optional<String> getLoggerName() {
      return Optional.ofNullable(this.loggerName);
   }

   public void setLoggerName(@Nullable String loggerName) {
      this.loggerName = loggerName;
   }

   public void setFollowRedirects(boolean followRedirects) {
      this.followRedirects = followRedirects;
   }

   public Charset getDefaultCharset() {
      return this.defaultCharset;
   }

   public void setDefaultCharset(Charset defaultCharset) {
      this.defaultCharset = defaultCharset;
   }

   public Map<String, Object> getChannelOptions() {
      return this.channelOptions;
   }

   public void setChannelOptions(Map<String, Object> channelOptions) {
      this.channelOptions = channelOptions;
   }

   public Optional<Duration> getReadTimeout() {
      return Optional.ofNullable(this.readTimeout);
   }

   public Optional<Duration> getReadIdleTimeout() {
      return Optional.ofNullable(this.readIdleTimeout);
   }

   public Optional<Duration> getConnectionPoolIdleTimeout() {
      return Optional.ofNullable(this.connectionPoolIdleTimeout);
   }

   public Optional<Duration> getConnectTimeout() {
      return Optional.ofNullable(this.connectTimeout);
   }

   public Optional<Duration> getConnectTtl() {
      return Optional.ofNullable(this.connectTtl);
   }

   public Optional<Duration> getShutdownQuietPeriod() {
      return Optional.ofNullable(this.shutdownQuietPeriod);
   }

   public Optional<Duration> getShutdownTimeout() {
      return Optional.ofNullable(this.shutdownTimeout);
   }

   public void setShutdownQuietPeriod(@Nullable Duration shutdownQuietPeriod) {
      this.shutdownQuietPeriod = shutdownQuietPeriod;
   }

   public void setShutdownTimeout(@Nullable Duration shutdownTimeout) {
      this.shutdownTimeout = shutdownTimeout;
   }

   public void setReadTimeout(@Nullable Duration readTimeout) {
      this.readTimeout = readTimeout;
   }

   public void setReadIdleTimeout(@Nullable Duration readIdleTimeout) {
      this.readIdleTimeout = readIdleTimeout;
   }

   public void setConnectionPoolIdleTimeout(@Nullable Duration connectionPoolIdleTimeout) {
      this.connectionPoolIdleTimeout = connectionPoolIdleTimeout;
   }

   public void setConnectTimeout(@Nullable Duration connectTimeout) {
      this.connectTimeout = connectTimeout;
   }

   public void setConnectTtl(@Nullable Duration connectTtl) {
      this.connectTtl = connectTtl;
   }

   public OptionalInt getNumOfThreads() {
      return this.numOfThreads != null ? OptionalInt.of(this.numOfThreads) : OptionalInt.empty();
   }

   public void setNumOfThreads(@Nullable Integer numOfThreads) {
      this.numOfThreads = numOfThreads;
   }

   public Optional<Class<? extends ThreadFactory>> getThreadFactory() {
      return Optional.ofNullable(this.threadFactory);
   }

   public void setThreadFactory(Class<? extends ThreadFactory> threadFactory) {
      this.threadFactory = threadFactory;
   }

   public int getMaxContentLength() {
      return this.maxContentLength;
   }

   public void setMaxContentLength(@ReadableBytes int maxContentLength) {
      this.maxContentLength = maxContentLength;
   }

   public Type getProxyType() {
      return this.proxyType;
   }

   public void setProxyType(Type proxyType) {
      this.proxyType = proxyType;
   }

   public Optional<SocketAddress> getProxyAddress() {
      return Optional.ofNullable(this.proxyAddress);
   }

   public void setProxyAddress(SocketAddress proxyAddress) {
      this.proxyAddress = proxyAddress;
   }

   public Optional<String> getProxyUsername() {
      String type = this.proxyType.name().toLowerCase();
      return this.proxyUsername != null ? Optional.of(this.proxyUsername) : Optional.ofNullable(CachedEnvironment.getProperty(type + ".proxyUser"));
   }

   public void setProxyUsername(String proxyUsername) {
      this.proxyUsername = proxyUsername;
   }

   public Optional<String> getProxyPassword() {
      String type = this.proxyType.name().toLowerCase();
      return this.proxyPassword != null ? Optional.of(this.proxyPassword) : Optional.ofNullable(CachedEnvironment.getProperty(type + ".proxyPassword"));
   }

   public void setProxyPassword(String proxyPassword) {
      this.proxyPassword = proxyPassword;
   }

   public void setProxySelector(ProxySelector proxySelector) {
      this.proxySelector = proxySelector;
   }

   public Optional<ProxySelector> getProxySelector() {
      return Optional.ofNullable(this.proxySelector);
   }

   public Proxy resolveProxy(boolean isSsl, String host, int port) {
      try {
         if (this.proxySelector != null) {
            URI uri = new URI(isSsl ? "https" : "http", null, host, port, null, null, null);
            return (Proxy)this.getProxySelector().flatMap(selector -> selector.select(uri).stream().findFirst()).orElse(Proxy.NO_PROXY);
         } else {
            return this.proxyAddress != null ? new Proxy(this.getProxyType(), this.proxyAddress) : Proxy.NO_PROXY;
         }
      } catch (URISyntaxException var5) {
         throw new RuntimeException(var5);
      }
   }

   public static class ConnectionPoolConfiguration implements Toggleable {
      public static final String PREFIX = "pool";
      public static final boolean DEFAULT_ENABLED = false;
      public static final int DEFAULT_MAXCONNECTIONS = -1;
      private int maxConnections = -1;
      private int maxPendingAcquires = Integer.MAX_VALUE;
      private Duration acquireTimeout;
      private boolean enabled = false;

      @Override
      public boolean isEnabled() {
         return this.enabled;
      }

      public void setEnabled(boolean enabled) {
         this.enabled = enabled;
      }

      public int getMaxConnections() {
         return this.maxConnections;
      }

      public void setMaxConnections(int maxConnections) {
         this.maxConnections = maxConnections;
      }

      public int getMaxPendingAcquires() {
         return this.maxPendingAcquires;
      }

      public void setMaxPendingAcquires(int maxPendingAcquires) {
         this.maxPendingAcquires = maxPendingAcquires;
      }

      public Optional<Duration> getAcquireTimeout() {
         return Optional.ofNullable(this.acquireTimeout);
      }

      public void setAcquireTimeout(@Nullable Duration acquireTimeout) {
         this.acquireTimeout = acquireTimeout;
      }
   }
}
