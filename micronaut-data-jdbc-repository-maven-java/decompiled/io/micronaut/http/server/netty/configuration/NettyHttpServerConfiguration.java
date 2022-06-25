package io.micronaut.http.server.netty.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.ReadableBytes;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.netty.channel.ChannelPipelineListener;
import io.micronaut.http.netty.channel.EventLoopGroupConfiguration;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ConfigurationProperties("netty")
@Replaces(HttpServerConfiguration.class)
public class NettyHttpServerConfiguration extends HttpServerConfiguration {
   public static final boolean DEFAULT_USE_NATIVE_TRANSPORT = false;
   public static final int DEFAULT_MAXINITIALLINELENGTH = 4096;
   public static final int DEFAULT_MAXHEADERSIZE = 8192;
   public static final int DEFAULT_MAXCHUNKSIZE = 8192;
   public static final boolean DEFAULT_CHUNKSUPPORTED = true;
   public static final boolean DEFAULT_VALIDATEHEADERS = true;
   public static final int DEFAULT_INITIALBUFFERSIZE = 128;
   public static final int DEFAULT_COMPRESSIONTHRESHOLD = 1024;
   public static final int DEFAULT_COMPRESSIONLEVEL = 6;
   public static final boolean DEFAULT_KEEP_ALIVE_ON_SERVER_ERROR = false;
   private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServerConfiguration.class);
   private final List<ChannelPipelineListener> pipelineCustomizers;
   private Map<ChannelOption, Object> childOptions = Collections.emptyMap();
   private Map<ChannelOption, Object> options = Collections.emptyMap();
   private NettyHttpServerConfiguration.Worker worker;
   private NettyHttpServerConfiguration.Parent parent;
   private NettyHttpServerConfiguration.FileTypeHandlerConfiguration fileTypeHandlerConfiguration = new NettyHttpServerConfiguration.FileTypeHandlerConfiguration(
      
   );
   private int maxInitialLineLength = 4096;
   private int maxHeaderSize = 8192;
   private int maxChunkSize = 8192;
   private int maxH2cUpgradeRequestSize = 8192;
   private boolean chunkedSupported = true;
   private boolean validateHeaders = true;
   private int initialBufferSize = 128;
   private LogLevel logLevel;
   private int compressionThreshold = 1024;
   private int compressionLevel = 6;
   private boolean useNativeTransport = false;
   private String fallbackProtocol = "http/1.1";
   private NettyHttpServerConfiguration.AccessLogger accessLogger;
   private NettyHttpServerConfiguration.Http2Settings http2Settings = new NettyHttpServerConfiguration.Http2Settings();
   private boolean keepAliveOnServerError = false;
   private String pcapLoggingPathPattern = null;
   private List<NettyHttpServerConfiguration.NettyListenerConfiguration> listeners = null;

   public NettyHttpServerConfiguration() {
      this(null, Collections.emptyList());
   }

   public NettyHttpServerConfiguration(ApplicationConfiguration applicationConfiguration) {
      this(applicationConfiguration, Collections.emptyList());
   }

   @Inject
   public NettyHttpServerConfiguration(ApplicationConfiguration applicationConfiguration, List<ChannelPipelineListener> pipelineCustomizers) {
      super(applicationConfiguration);
      this.pipelineCustomizers = pipelineCustomizers;
   }

   public NettyHttpServerConfiguration.AccessLogger getAccessLogger() {
      return this.accessLogger;
   }

   public void setAccessLogger(NettyHttpServerConfiguration.AccessLogger accessLogger) {
      this.accessLogger = accessLogger;
   }

   public NettyHttpServerConfiguration.Http2Settings getHttp2() {
      return this.http2Settings;
   }

   public void setHttp2(NettyHttpServerConfiguration.Http2Settings http2) {
      if (http2 != null) {
         this.http2Settings = http2;
      }

   }

   public List<ChannelPipelineListener> getPipelineCustomizers() {
      return this.pipelineCustomizers;
   }

   public String getFallbackProtocol() {
      return this.fallbackProtocol;
   }

   public void setFallbackProtocol(String fallbackProtocol) {
      if (fallbackProtocol != null) {
         this.fallbackProtocol = fallbackProtocol;
      }

   }

   public Optional<LogLevel> getLogLevel() {
      return Optional.ofNullable(this.logLevel);
   }

   public int getMaxInitialLineLength() {
      return this.maxInitialLineLength;
   }

   public int getMaxHeaderSize() {
      return this.maxHeaderSize;
   }

   public int getMaxChunkSize() {
      return this.maxChunkSize;
   }

   public int getMaxH2cUpgradeRequestSize() {
      return this.maxH2cUpgradeRequestSize;
   }

   public boolean isChunkedSupported() {
      return this.chunkedSupported;
   }

   public boolean isUseNativeTransport() {
      return this.useNativeTransport;
   }

   public boolean isValidateHeaders() {
      return this.validateHeaders;
   }

   public int getInitialBufferSize() {
      return this.initialBufferSize;
   }

   public int getCompressionThreshold() {
      return this.compressionThreshold;
   }

   public int getCompressionLevel() {
      return this.compressionLevel;
   }

   public Map<ChannelOption, Object> getChildOptions() {
      return this.childOptions;
   }

   public Map<ChannelOption, Object> getOptions() {
      return this.options;
   }

   public NettyHttpServerConfiguration.Worker getWorker() {
      return this.worker;
   }

   @NonNull
   public NettyHttpServerConfiguration.FileTypeHandlerConfiguration getFileTypeHandlerConfiguration() {
      return this.fileTypeHandlerConfiguration;
   }

   @Inject
   public void setFileTypeHandlerConfiguration(@NonNull NettyHttpServerConfiguration.FileTypeHandlerConfiguration fileTypeHandlerConfiguration) {
      if (fileTypeHandlerConfiguration != null) {
         this.fileTypeHandlerConfiguration = fileTypeHandlerConfiguration;
      }

   }

   public NettyHttpServerConfiguration.Parent getParent() {
      return this.parent;
   }

   public boolean isKeepAliveOnServerError() {
      return this.keepAliveOnServerError;
   }

   public void setChildOptions(Map<ChannelOption, Object> childOptions) {
      this.childOptions = childOptions;
   }

   public void setOptions(Map<ChannelOption, Object> options) {
      this.options = options;
   }

   public void setWorker(NettyHttpServerConfiguration.Worker worker) {
      this.worker = worker;
   }

   public void setParent(NettyHttpServerConfiguration.Parent parent) {
      this.parent = parent;
   }

   public void setMaxInitialLineLength(@ReadableBytes int maxInitialLineLength) {
      this.maxInitialLineLength = maxInitialLineLength;
   }

   public void setMaxHeaderSize(@ReadableBytes int maxHeaderSize) {
      this.maxHeaderSize = maxHeaderSize;
   }

   public void setMaxChunkSize(@ReadableBytes int maxChunkSize) {
      this.maxChunkSize = maxChunkSize;
   }

   public void setMaxH2cUpgradeRequestSize(int maxH2cUpgradeRequestSize) {
      this.maxH2cUpgradeRequestSize = maxH2cUpgradeRequestSize;
   }

   public void setChunkedSupported(boolean chunkedSupported) {
      this.chunkedSupported = chunkedSupported;
   }

   public void setUseNativeTransport(boolean useNativeTransport) {
      this.useNativeTransport = useNativeTransport;
   }

   public void setValidateHeaders(boolean validateHeaders) {
      this.validateHeaders = validateHeaders;
   }

   public void setInitialBufferSize(int initialBufferSize) {
      this.initialBufferSize = initialBufferSize;
   }

   public void setLogLevel(LogLevel logLevel) {
      this.logLevel = logLevel;
   }

   public void setCompressionThreshold(@ReadableBytes int compressionThreshold) {
      this.compressionThreshold = compressionThreshold;
   }

   public void setCompressionLevel(@ReadableBytes int compressionLevel) {
      this.compressionLevel = compressionLevel;
   }

   public void setKeepAliveOnServerError(boolean keepAliveOnServerError) {
      this.keepAliveOnServerError = keepAliveOnServerError;
   }

   @Internal
   public String getPcapLoggingPathPattern() {
      return this.pcapLoggingPathPattern;
   }

   @Internal
   public void setPcapLoggingPathPattern(String pcapLoggingPathPattern) {
      this.pcapLoggingPathPattern = pcapLoggingPathPattern;
   }

   public List<NettyHttpServerConfiguration.NettyListenerConfiguration> getListeners() {
      return this.listeners;
   }

   public void setListeners(List<NettyHttpServerConfiguration.NettyListenerConfiguration> listeners) {
      this.listeners = listeners;
   }

   @ConfigurationProperties("access-logger")
   public static class AccessLogger {
      private boolean enabled;
      private String loggerName;
      private String logFormat;
      private List<String> exclusions;

      public boolean isEnabled() {
         return this.enabled;
      }

      public void setEnabled(boolean enabled) {
         this.enabled = enabled;
      }

      public String getLoggerName() {
         return this.loggerName;
      }

      public void setLoggerName(String loggerName) {
         this.loggerName = loggerName;
      }

      public String getLogFormat() {
         return this.logFormat;
      }

      public void setLogFormat(String logFormat) {
         this.logFormat = logFormat;
      }

      public List<String> getExclusions() {
         return this.exclusions;
      }

      public void setExclusions(List<String> exclusions) {
         this.exclusions = exclusions;
      }
   }

   public abstract static class EventLoopConfig implements EventLoopGroupConfiguration {
      private int threads;
      private Integer ioRatio;
      private String executor;
      private boolean preferNativeTransport = false;
      private Duration shutdownQuietPeriod = Duration.ofSeconds(2L);
      private Duration shutdownTimeout = Duration.ofSeconds(15L);
      private String name;

      EventLoopConfig(String name) {
         this.name = name;
      }

      @NonNull
      @Override
      public String getName() {
         return this.name;
      }

      public void setEventLoopGroup(String name) {
         if (StringUtils.isNotEmpty(name)) {
            this.name = name;
         }

      }

      public void setThreads(int threads) {
         this.threads = threads;
      }

      public void setIoRatio(Integer ioRatio) {
         this.ioRatio = ioRatio;
      }

      public void setExecutor(String executor) {
         this.executor = executor;
      }

      public void setPreferNativeTransport(boolean preferNativeTransport) {
         this.preferNativeTransport = preferNativeTransport;
      }

      public void setShutdownQuietPeriod(Duration shutdownQuietPeriod) {
         if (shutdownQuietPeriod != null) {
            this.shutdownQuietPeriod = shutdownQuietPeriod;
         }

      }

      public void setShutdownTimeout(Duration shutdownTimeout) {
         if (shutdownTimeout != null) {
            this.shutdownTimeout = shutdownTimeout;
         }

      }

      public int getNumOfThreads() {
         return this.threads;
      }

      @Override
      public Optional<Integer> getIoRatio() {
         return this.ioRatio != null ? Optional.of(this.ioRatio) : Optional.empty();
      }

      @Override
      public Optional<String> getExecutorName() {
         return this.executor != null ? Optional.of(this.executor) : Optional.empty();
      }

      @Override
      public int getNumThreads() {
         return this.threads;
      }

      @Override
      public boolean isPreferNativeTransport() {
         return this.preferNativeTransport;
      }

      @Override
      public Duration getShutdownQuietPeriod() {
         return this.shutdownQuietPeriod;
      }

      @Override
      public Duration getShutdownTimeout() {
         return this.shutdownTimeout;
      }
   }

   @ConfigurationProperties("responses.file")
   public static class FileTypeHandlerConfiguration {
      public static final int DEFAULT_CACHESECONDS = 60;
      private int cacheSeconds = 60;
      private NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration cacheControl = new NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration(
         
      );

      public FileTypeHandlerConfiguration() {
      }

      @Deprecated
      @Inject
      public FileTypeHandlerConfiguration(
         @Nullable @Property(name = "netty.responses.file.cache-seconds") Integer cacheSeconds,
         @Nullable @Property(name = "netty.responses.file.cache-control.public") Boolean isPublic
      ) {
         if (cacheSeconds != null) {
            this.cacheSeconds = cacheSeconds;
            NettyHttpServerConfiguration.LOG
               .warn(
                  "The configuration `netty.responses.file.cache-seconds` is deprecated and will be removed in a future release. Use `micronaut.server.netty.responses.file.cache-seconds` instead."
               );
         }

         if (isPublic != null) {
            this.cacheControl.setPublic(isPublic);
            NettyHttpServerConfiguration.LOG
               .warn(
                  "The configuration `netty.responses.file.cache-control.public` is deprecated and will be removed in a future release. Use `micronaut.server.netty.responses.file.cache-control.public` instead."
               );
         }

      }

      public int getCacheSeconds() {
         return this.cacheSeconds;
      }

      public void setCacheSeconds(int cacheSeconds) {
         this.cacheSeconds = cacheSeconds;
      }

      public NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration getCacheControl() {
         return this.cacheControl;
      }

      public void setCacheControl(NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration cacheControl) {
         this.cacheControl = cacheControl;
      }

      @ConfigurationProperties("cache-control")
      public static class CacheControlConfiguration {
         private static final boolean DEFAULT_PUBLIC_CACHE = false;
         private boolean publicCache = false;

         public void setPublic(boolean publicCache) {
            this.publicCache = publicCache;
         }

         @NonNull
         public boolean getPublic() {
            return this.publicCache;
         }
      }
   }

   @ConfigurationProperties("http2")
   public static class Http2Settings {
      private final io.netty.handler.codec.http2.Http2Settings settings = io.netty.handler.codec.http2.Http2Settings.defaultSettings();

      public io.netty.handler.codec.http2.Http2Settings http2Settings() {
         return this.settings;
      }

      public Long getHeaderTableSize() {
         return this.settings.headerTableSize();
      }

      public void setHeaderTableSize(Long value) {
         if (value != null) {
            this.settings.headerTableSize(value);
         }

      }

      public Boolean getPushEnabled() {
         return this.settings.pushEnabled();
      }

      public void setPushEnabled(Boolean enabled) {
         if (enabled != null) {
            this.settings.pushEnabled(enabled);
         }

      }

      public Long getMaxConcurrentStreams() {
         return this.settings.maxConcurrentStreams();
      }

      public void setMaxConcurrentStreams(Long value) {
         if (value != null) {
            this.settings.maxConcurrentStreams(value);
         }

      }

      public Integer getInitialWindowSize() {
         return this.settings.initialWindowSize();
      }

      public void setInitialWindowSize(Integer value) {
         if (value != null) {
            this.settings.initialWindowSize(value);
         }

      }

      public Integer getMaxFrameSize() {
         return this.settings.maxFrameSize();
      }

      public void setMaxFrameSize(Integer value) {
         if (value != null) {
            this.settings.maxFrameSize(value);
         }

      }

      public Long getMaxHeaderListSize() {
         return this.settings.maxHeaderListSize();
      }

      public void setMaxHeaderListSize(Long value) {
         if (value != null) {
            this.settings.maxHeaderListSize(value);
         }

      }
   }

   @EachProperty("listeners")
   public static final class NettyListenerConfiguration {
      private NettyHttpServerConfiguration.NettyListenerConfiguration.Family family = NettyHttpServerConfiguration.NettyListenerConfiguration.Family.TCP;
      private boolean ssl;
      @Nullable
      private String host;
      private int port;
      private String path;
      private boolean exposeDefaultRoutes = true;

      @Internal
      public static NettyHttpServerConfiguration.NettyListenerConfiguration createTcp(@Nullable String host, int port, boolean ssl) {
         NettyHttpServerConfiguration.NettyListenerConfiguration configuration = new NettyHttpServerConfiguration.NettyListenerConfiguration();
         configuration.setFamily(NettyHttpServerConfiguration.NettyListenerConfiguration.Family.TCP);
         configuration.setHost(host);
         configuration.setPort(port);
         configuration.setSsl(ssl);
         return configuration;
      }

      public NettyHttpServerConfiguration.NettyListenerConfiguration.Family getFamily() {
         return this.family;
      }

      public void setFamily(@NonNull NettyHttpServerConfiguration.NettyListenerConfiguration.Family family) {
         Objects.requireNonNull(family, "family");
         this.family = family;
      }

      public boolean isSsl() {
         return this.ssl;
      }

      public void setSsl(boolean ssl) {
         this.ssl = ssl;
      }

      @Nullable
      public String getHost() {
         return this.host;
      }

      public void setHost(@Nullable String host) {
         this.host = host;
      }

      public int getPort() {
         return this.port;
      }

      public void setPort(int port) {
         this.port = port;
      }

      public String getPath() {
         return this.path;
      }

      public void setPath(String path) {
         this.path = path;
      }

      @Internal
      public boolean isExposeDefaultRoutes() {
         return this.exposeDefaultRoutes;
      }

      @Internal
      public void setExposeDefaultRoutes(boolean exposeDefaultRoutes) {
         this.exposeDefaultRoutes = exposeDefaultRoutes;
      }

      public static enum Family {
         TCP,
         UNIX;
      }
   }

   @ConfigurationProperties("parent")
   @Requires(
      missingProperty = "micronaut.netty.event-loops.parent"
   )
   public static class Parent extends NettyHttpServerConfiguration.EventLoopConfig {
      public static final String NAME = "parent";

      Parent() {
         super("parent");
      }
   }

   @ConfigurationProperties("worker")
   public static class Worker extends NettyHttpServerConfiguration.EventLoopConfig {
      Worker() {
         super("default");
      }
   }
}
