package io.micronaut.http.server.netty.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.netty.channel.ChannelPipelineListener;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.executor.ThreadSelection;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $NettyHttpServerConfiguration$Definition
   extends AbstractInitializableBeanDefinition<NettyHttpServerConfiguration>
   implements BeanFactory<NettyHttpServerConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyHttpServerConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationConfiguration.class, "applicationConfiguration"),
         Argument.of(List.class, "pipelineCustomizers", null, Argument.ofTypeVariable(ChannelPipelineListener.class, "E"))
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         NettyHttpServerConfiguration.class,
         "setFileTypeHandlerConfiguration",
         new Argument[]{
            Argument.of(
               NettyHttpServerConfiguration.FileTypeHandlerConfiguration.class,
               "fileTypeHandlerConfiguration",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            )
         },
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "netty"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.netty"),
                  "io.micronaut.context.annotation.Replaces",
                  AnnotationUtil.mapOf("bean", $micronaut_load_class_value_0(), "value", $micronaut_load_class_value_0())
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "netty"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "netty"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "netty"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.netty"),
                  "io.micronaut.context.annotation.Replaces",
                  AnnotationUtil.mapOf("bean", $micronaut_load_class_value_0(), "value", $micronaut_load_class_value_0())
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false
      )
   };
   private static final Set $INNER_CONFIGURATION_CLASSES = new HashSet(
      Arrays.asList(
         NettyHttpServerConfiguration.Http2Settings.class,
         NettyHttpServerConfiguration.AccessLogger.class,
         NettyHttpServerConfiguration.Worker.class,
         NettyHttpServerConfiguration.Parent.class,
         NettyHttpServerConfiguration.FileTypeHandlerConfiguration.class,
         NettyHttpServerConfiguration.NettyListenerConfiguration.class,
         HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties.class,
         HttpServerConfiguration.HostResolutionConfiguration.class,
         HttpServerConfiguration.CorsConfiguration.class,
         HttpServerConfiguration.MultipartConfiguration.class
      )
   );

   @Override
   public NettyHttpServerConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyHttpServerConfiguration var4 = new NettyHttpServerConfiguration(
         (ApplicationConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (List<ChannelPipelineListener>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (NettyHttpServerConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyHttpServerConfiguration var4 = (NettyHttpServerConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.http-version")) {
            var4.setHttpVersion(
               (HttpVersion)super.getPropertyValueForSetter(
                  var1, var2, "setHttpVersion", Argument.of(HttpVersion.class, "httpVersion"), "micronaut.server.http-version", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.thread-selection")) {
            var4.setThreadSelection(
               (ThreadSelection)super.getPropertyValueForSetter(
                  var1, var2, "setThreadSelection", Argument.of(ThreadSelection.class, "threadSelection"), "micronaut.server.thread-selection", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.default-charset")) {
            var4.setDefaultCharset(
               (Charset)super.getPropertyValueForSetter(
                  var1, var2, "setDefaultCharset", Argument.of(Charset.class, "defaultCharset"), "micronaut.server.default-charset", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.port")) {
            var4.setPort(super.getPropertyValueForSetter(var1, var2, "setPort", Argument.of(Integer.TYPE, "port"), "micronaut.server.port", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.host")) {
            var4.setHost((String)super.getPropertyValueForSetter(var1, var2, "setHost", Argument.of(String.class, "host"), "micronaut.server.host", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.read-timeout")) {
            var4.setReadTimeout(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setReadTimeout", Argument.of(Integer.class, "readTimeout"), "micronaut.server.read-timeout", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.server-header")) {
            var4.setServerHeader(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setServerHeader", Argument.of(String.class, "serverHeader"), "micronaut.server.server-header", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.max-request-size")) {
            var4.setMaxRequestSize(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxRequestSize",
                  Argument.of(
                     Long.TYPE,
                     "arg0",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.max-request-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.read-idle-timeout")) {
            var4.setReadIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setReadIdleTimeout", Argument.of(Duration.class, "readIdleTimeout"), "micronaut.server.read-idle-timeout", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.write-idle-timeout")) {
            var4.setWriteIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setWriteIdleTimeout", Argument.of(Duration.class, "writeIdleTimeout"), "micronaut.server.write-idle-timeout", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.idle-timeout")) {
            var4.setIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setIdleTimeout", Argument.of(Duration.class, "idleTimeout"), "micronaut.server.idle-timeout", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.multipart")) {
            var4.setMultipart(
               (HttpServerConfiguration.MultipartConfiguration)super.getBeanForSetter(
                  var1, var2, "setMultipart", Argument.of(HttpServerConfiguration.MultipartConfiguration.class, "multipart"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.cors")) {
            var4.setCors(
               (HttpServerConfiguration.CorsConfiguration)super.getBeanForSetter(
                  var1, var2, "setCors", Argument.of(HttpServerConfiguration.CorsConfiguration.class, "cors"), null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.date-header")) {
            var4.setDateHeader(
               super.getPropertyValueForSetter(var1, var2, "setDateHeader", Argument.of(Boolean.TYPE, "dateHeader"), "micronaut.server.date-header", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.log-handled-exceptions")) {
            var4.setLogHandledExceptions(
               super.getPropertyValueForSetter(
                  var1, var2, "setLogHandledExceptions", Argument.of(Boolean.TYPE, "logHandledExceptions"), "micronaut.server.log-handled-exceptions", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.host-resolution")) {
            var4.setHostResolution(
               (HttpServerConfiguration.HostResolutionConfiguration)super.getBeanForSetter(
                  var1, var2, "setHostResolution", Argument.of(HttpServerConfiguration.HostResolutionConfiguration.class, "hostResolution"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.locale-resolution")) {
            var4.setLocaleResolution(
               (HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties)super.getBeanForSetter(
                  var1,
                  var2,
                  "setLocaleResolution",
                  Argument.of(HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties.class, "localeResolution"),
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.client-address-header")) {
            var4.setClientAddressHeader(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setClientAddressHeader", Argument.of(String.class, "clientAddressHeader"), "micronaut.server.client-address-header", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.context-path")) {
            var4.setContextPath(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setContextPath", Argument.of(String.class, "contextPath"), "micronaut.server.context-path", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.dual-protocol")) {
            var4.setDualProtocol(
               super.getPropertyValueForSetter(var1, var2, "setDualProtocol", Argument.of(Boolean.TYPE, "dualProtocol"), "micronaut.server.dual-protocol", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.http-to-https-redirect")) {
            var4.setHttpToHttpsRedirect(
               super.getPropertyValueForSetter(
                  var1, var2, "setHttpToHttpsRedirect", Argument.of(Boolean.TYPE, "httpToHttpsRedirect"), "micronaut.server.http-to-https-redirect", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.access-logger")) {
            var4.setAccessLogger(
               (NettyHttpServerConfiguration.AccessLogger)super.getBeanForSetter(
                  var1, var2, "setAccessLogger", Argument.of(NettyHttpServerConfiguration.AccessLogger.class, "accessLogger"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.http2")) {
            var4.setHttp2(
               (NettyHttpServerConfiguration.Http2Settings)super.getBeanForSetter(
                  var1, var2, "setHttp2", Argument.of(NettyHttpServerConfiguration.Http2Settings.class, "http2"), null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.fallback-protocol")) {
            var4.setFallbackProtocol(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setFallbackProtocol", Argument.of(String.class, "fallbackProtocol"), "micronaut.server.netty.fallback-protocol", null
               )
            );
         }

         var4.setFileTypeHandlerConfiguration(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.child-options")) {
            var4.setChildOptions(
               (Map<ChannelOption, Object>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setChildOptions",
                  Argument.of(
                     Map.class,
                     "childOptions",
                     null,
                     Argument.ofTypeVariable(ChannelOption.class, "K", null, Argument.ofTypeVariable(Object.class, "T")),
                     Argument.ofTypeVariable(Object.class, "V")
                  ),
                  "micronaut.server.netty.child-options",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.options")) {
            var4.setOptions(
               (Map<ChannelOption, Object>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setOptions",
                  Argument.of(
                     Map.class,
                     "options",
                     null,
                     Argument.ofTypeVariable(ChannelOption.class, "K", null, Argument.ofTypeVariable(Object.class, "T")),
                     Argument.ofTypeVariable(Object.class, "V")
                  ),
                  "micronaut.server.netty.options",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.worker")) {
            var4.setWorker(
               (NettyHttpServerConfiguration.Worker)super.getBeanForSetter(
                  var1, var2, "setWorker", Argument.of(NettyHttpServerConfiguration.Worker.class, "worker"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.parent")) {
            var4.setParent(
               (NettyHttpServerConfiguration.Parent)super.getBeanForSetter(
                  var1, var2, "setParent", Argument.of(NettyHttpServerConfiguration.Parent.class, "parent"), null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.max-initial-line-length")) {
            var4.setMaxInitialLineLength(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxInitialLineLength",
                  Argument.of(
                     Integer.TYPE,
                     "maxInitialLineLength",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.netty.max-initial-line-length",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.max-header-size")) {
            var4.setMaxHeaderSize(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxHeaderSize",
                  Argument.of(
                     Integer.TYPE,
                     "maxHeaderSize",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.netty.max-header-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.max-chunk-size")) {
            var4.setMaxChunkSize(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxChunkSize",
                  Argument.of(
                     Integer.TYPE,
                     "maxChunkSize",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.netty.max-chunk-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.max-h2c-upgrade-request-size")) {
            var4.setMaxH2cUpgradeRequestSize(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxH2cUpgradeRequestSize",
                  Argument.of(Integer.TYPE, "maxH2cUpgradeRequestSize"),
                  "micronaut.server.netty.max-h2c-upgrade-request-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.chunked-supported")) {
            var4.setChunkedSupported(
               super.getPropertyValueForSetter(
                  var1, var2, "setChunkedSupported", Argument.of(Boolean.TYPE, "chunkedSupported"), "micronaut.server.netty.chunked-supported", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.use-native-transport")) {
            var4.setUseNativeTransport(
               super.getPropertyValueForSetter(
                  var1, var2, "setUseNativeTransport", Argument.of(Boolean.TYPE, "useNativeTransport"), "micronaut.server.netty.use-native-transport", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.validate-headers")) {
            var4.setValidateHeaders(
               super.getPropertyValueForSetter(
                  var1, var2, "setValidateHeaders", Argument.of(Boolean.TYPE, "validateHeaders"), "micronaut.server.netty.validate-headers", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.initial-buffer-size")) {
            var4.setInitialBufferSize(
               super.getPropertyValueForSetter(
                  var1, var2, "setInitialBufferSize", Argument.of(Integer.TYPE, "initialBufferSize"), "micronaut.server.netty.initial-buffer-size", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.log-level")) {
            var4.setLogLevel(
               (LogLevel)super.getPropertyValueForSetter(
                  var1, var2, "setLogLevel", Argument.of(LogLevel.class, "logLevel"), "micronaut.server.netty.log-level", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.compression-threshold")) {
            var4.setCompressionThreshold(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setCompressionThreshold",
                  Argument.of(
                     Integer.TYPE,
                     "compressionThreshold",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.netty.compression-threshold",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.compression-level")) {
            var4.setCompressionLevel(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setCompressionLevel",
                  Argument.of(
                     Integer.TYPE,
                     "compressionLevel",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.netty.compression-level",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.keep-alive-on-server-error")) {
            var4.setKeepAliveOnServerError(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setKeepAliveOnServerError",
                  Argument.of(Boolean.TYPE, "keepAliveOnServerError"),
                  "micronaut.server.netty.keep-alive-on-server-error",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.pcap-logging-path-pattern")) {
            var4.setPcapLoggingPathPattern(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setPcapLoggingPathPattern",
                  Argument.of(String.class, "pcapLoggingPathPattern"),
                  "micronaut.server.netty.pcap-logging-path-pattern",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.listeners")) {
            Argument var5 = Argument.of(
               List.class, "listeners", null, Argument.ofTypeVariable(NettyHttpServerConfiguration.NettyListenerConfiguration.class, "E")
            );
            var4.setListeners(
               (List<NettyHttpServerConfiguration.NettyListenerConfiguration>)super.getBeansOfTypeForSetter(
                  var1, var2, "setListeners", var5, var5.getTypeParameters()[0], null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NettyHttpServerConfiguration$Definition() {
      this(NettyHttpServerConfiguration.class, $CONSTRUCTOR);
   }

   protected $NettyHttpServerConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyHttpServerConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         true,
         false,
         false
      );
   }

   @Override
   protected boolean isInnerConfiguration(Class var1) {
      return $INNER_CONFIGURATION_CLASSES.contains(var1);
   }
}
