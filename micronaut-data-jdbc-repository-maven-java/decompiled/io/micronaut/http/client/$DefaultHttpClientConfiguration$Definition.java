package io.micronaut.http.client;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.ssl.ClientSslConfiguration;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.logging.LogLevel;
import io.micronaut.runtime.ApplicationConfiguration;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.Proxy.Type;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $DefaultHttpClientConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpClientConfiguration>
   implements BeanFactory<DefaultHttpClientConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpClientConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration.class, "connectionPoolConfiguration"),
         Argument.of(ApplicationConfiguration.class, "applicationConfiguration")
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
         DefaultHttpClientConfiguration.class,
         "setClientSslConfiguration",
         new Argument[]{
            Argument.of(
               ClientSslConfiguration.class,
               "sslConfiguration",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
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
                  "io.micronaut.context.annotation.BootstrapContextCompatible",
                  Collections.EMPTY_MAP,
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.http.client"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.http.client"),
                  "io.micronaut.context.annotation.Primary",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.http.client"),
                  "javax.inject.Qualifier",
                  Collections.EMPTY_MAP,
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.http.client"),
                  "javax.inject.Qualifier",
                  Collections.EMPTY_MAP,
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.BootstrapContextCompatible",
                  Collections.EMPTY_MAP,
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.http.client"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.http.client"),
                  "io.micronaut.context.annotation.Primary",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationProperties"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
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
   private static final Set $INNER_CONFIGURATION_CLASSES = Collections.singleton(DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration.class);

   @Override
   public DefaultHttpClientConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpClientConfiguration var4 = new DefaultHttpClientConfiguration(
         (DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ApplicationConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (DefaultHttpClientConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultHttpClientConfiguration var4 = (DefaultHttpClientConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.http-version")) {
            var4.setHttpVersion(
               (HttpVersion)super.getPropertyValueForSetter(
                  var1, var2, "setHttpVersion", Argument.of(HttpVersion.class, "httpVersion"), "micronaut.http.client.http-version", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.log-level")) {
            var4.setLogLevel(
               (LogLevel)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setLogLevel",
                  Argument.of(
                     LogLevel.class,
                     "logLevel",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.log-level",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.event-loop-group")) {
            var4.setEventLoopGroup(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setEventLoopGroup",
                  Argument.of(
                     String.class,
                     "eventLoopGroup",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.event-loop-group",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.ssl-configuration")) {
            var4.setSslConfiguration(
               (SslConfiguration)super.getPropertyValueForSetter(
                  var1, var2, "setSslConfiguration", Argument.of(SslConfiguration.class, "sslConfiguration"), "micronaut.http.client.ssl-configuration", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.exception-on-error-status")) {
            var4.setExceptionOnErrorStatus(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setExceptionOnErrorStatus",
                  Argument.of(Boolean.TYPE, "exceptionOnErrorStatus"),
                  "micronaut.http.client.exception-on-error-status",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.logger-name")) {
            var4.setLoggerName(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setLoggerName",
                  Argument.of(
                     String.class,
                     "loggerName",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.logger-name",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.follow-redirects")) {
            var4.setFollowRedirects(
               super.getPropertyValueForSetter(
                  var1, var2, "setFollowRedirects", Argument.of(Boolean.TYPE, "followRedirects"), "micronaut.http.client.follow-redirects", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.default-charset")) {
            var4.setDefaultCharset(
               (Charset)super.getPropertyValueForSetter(
                  var1, var2, "setDefaultCharset", Argument.of(Charset.class, "defaultCharset"), "micronaut.http.client.default-charset", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.client.channel-options")) {
            var4.setChannelOptions(
               (Map<String, Object>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setChannelOptions",
                  Argument.of(Map.class, "channelOptions", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V")),
                  "micronaut.http.client.channel-options",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.shutdown-quiet-period")) {
            var4.setShutdownQuietPeriod(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setShutdownQuietPeriod",
                  Argument.of(
                     Duration.class,
                     "shutdownQuietPeriod",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.shutdown-quiet-period",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.shutdown-timeout")) {
            var4.setShutdownTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setShutdownTimeout",
                  Argument.of(
                     Duration.class,
                     "shutdownTimeout",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.shutdown-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.read-timeout")) {
            var4.setReadTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setReadTimeout",
                  Argument.of(
                     Duration.class,
                     "readTimeout",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.read-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.read-idle-timeout")) {
            var4.setReadIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setReadIdleTimeout",
                  Argument.of(
                     Duration.class,
                     "readIdleTimeout",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.read-idle-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.connection-pool-idle-timeout")) {
            var4.setConnectionPoolIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setConnectionPoolIdleTimeout",
                  Argument.of(
                     Duration.class,
                     "connectionPoolIdleTimeout",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.connection-pool-idle-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.connect-timeout")) {
            var4.setConnectTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setConnectTimeout",
                  Argument.of(
                     Duration.class,
                     "connectTimeout",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.connect-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.connect-ttl")) {
            var4.setConnectTtl(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setConnectTtl",
                  Argument.of(
                     Duration.class,
                     "connectTtl",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.connect-ttl",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.num-of-threads")) {
            var4.setNumOfThreads(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setNumOfThreads",
                  Argument.of(
                     Integer.class,
                     "numOfThreads",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.http.client.num-of-threads",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.thread-factory")) {
            var4.setThreadFactory(
               (Class<? extends ThreadFactory>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setThreadFactory",
                  Argument.of(Class.class, "threadFactory", null, Argument.ofTypeVariable(ThreadFactory.class, "T")),
                  "micronaut.http.client.thread-factory",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.max-content-length")) {
            var4.setMaxContentLength(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxContentLength",
                  Argument.of(
                     Integer.TYPE,
                     "maxContentLength",
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
                  "micronaut.http.client.max-content-length",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.proxy-type")) {
            var4.setProxyType(
               (Type)super.getPropertyValueForSetter(var1, var2, "setProxyType", Argument.of(Type.class, "proxyType"), "micronaut.http.client.proxy-type", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.proxy-address")) {
            var4.setProxyAddress(
               (SocketAddress)super.getPropertyValueForSetter(
                  var1, var2, "setProxyAddress", Argument.of(SocketAddress.class, "proxyAddress"), "micronaut.http.client.proxy-address", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.proxy-username")) {
            var4.setProxyUsername(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProxyUsername", Argument.of(String.class, "proxyUsername"), "micronaut.http.client.proxy-username", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.proxy-password")) {
            var4.setProxyPassword(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProxyPassword", Argument.of(String.class, "proxyPassword"), "micronaut.http.client.proxy-password", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.proxy-selector")) {
            var4.setProxySelector(
               (ProxySelector)super.getPropertyValueForSetter(
                  var1, var2, "setProxySelector", Argument.of(ProxySelector.class, "proxySelector"), "micronaut.http.client.proxy-selector", null
               )
            );
         }

         var4.setClientSslConfiguration(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpClientConfiguration$Definition() {
      this(DefaultHttpClientConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpClientConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHttpClientConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
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
