package io.micronaut.http.client;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.logging.LogLevel;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.Proxy.Type;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $ServiceHttpClientConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ServiceHttpClientConfiguration>
   implements BeanFactory<ServiceHttpClientConfiguration>,
   ParametrizedBeanFactory<ServiceHttpClientConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServiceHttpClientConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(
            String.class,
            "serviceId",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         ),
         Argument.of(
            ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration.class,
            "connectionPoolConfiguration",
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
         ),
         Argument.of(
            ServiceHttpClientConfiguration.ServiceSslClientConfiguration.class,
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
         ),
         Argument.of(HttpClientConfiguration.class, "defaultHttpClientConfiguration")
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
   private static final Set $INNER_CONFIGURATION_CLASSES = new HashSet(
      Arrays.asList(ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration.class, ServiceHttpClientConfiguration.ServiceSslClientConfiguration.class)
   );

   @Override
   public ServiceHttpClientConfiguration doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      ServiceHttpClientConfiguration var5 = new ServiceHttpClientConfiguration(
         (String)var4.get("serviceId"),
         (ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (ServiceHttpClientConfiguration.ServiceSslClientConfiguration)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (HttpClientConfiguration)super.getBeanForConstructorArgument(var1, var2, 3, null)
      );
      return (ServiceHttpClientConfiguration)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ServiceHttpClientConfiguration var4 = (ServiceHttpClientConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.http-version")) {
            var4.setHttpVersion(
               (HttpVersion)super.getPropertyValueForSetter(
                  var1, var2, "setHttpVersion", Argument.of(HttpVersion.class, "httpVersion"), "micronaut.http.services.*.http-version", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.log-level")) {
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
                  "micronaut.http.services.*.log-level",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.event-loop-group")) {
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
                  "micronaut.http.services.*.event-loop-group",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl-configuration")) {
            var4.setSslConfiguration(
               (SslConfiguration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setSslConfiguration",
                  Argument.of(SslConfiguration.class, "sslConfiguration"),
                  "micronaut.http.services.*.ssl-configuration",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.exception-on-error-status")) {
            var4.setExceptionOnErrorStatus(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setExceptionOnErrorStatus",
                  Argument.of(Boolean.TYPE, "exceptionOnErrorStatus"),
                  "micronaut.http.services.*.exception-on-error-status",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.logger-name")) {
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
                  "micronaut.http.services.*.logger-name",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.follow-redirects")) {
            var4.setFollowRedirects(
               super.getPropertyValueForSetter(
                  var1, var2, "setFollowRedirects", Argument.of(Boolean.TYPE, "followRedirects"), "micronaut.http.services.*.follow-redirects", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.default-charset")) {
            var4.setDefaultCharset(
               (Charset)super.getPropertyValueForSetter(
                  var1, var2, "setDefaultCharset", Argument.of(Charset.class, "defaultCharset"), "micronaut.http.services.*.default-charset", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.services.*.channel-options")) {
            var4.setChannelOptions(
               (Map<String, Object>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setChannelOptions",
                  Argument.of(Map.class, "channelOptions", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V")),
                  "micronaut.http.services.*.channel-options",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.shutdown-quiet-period")) {
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
                  "micronaut.http.services.*.shutdown-quiet-period",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.shutdown-timeout")) {
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
                  "micronaut.http.services.*.shutdown-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.read-timeout")) {
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
                  "micronaut.http.services.*.read-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.read-idle-timeout")) {
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
                  "micronaut.http.services.*.read-idle-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.connection-pool-idle-timeout")) {
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
                  "micronaut.http.services.*.connection-pool-idle-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.connect-timeout")) {
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
                  "micronaut.http.services.*.connect-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.connect-ttl")) {
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
                  "micronaut.http.services.*.connect-ttl",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.num-of-threads")) {
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
                  "micronaut.http.services.*.num-of-threads",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.thread-factory")) {
            var4.setThreadFactory(
               (Class<? extends ThreadFactory>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setThreadFactory",
                  Argument.of(Class.class, "threadFactory", null, Argument.ofTypeVariable(ThreadFactory.class, "T")),
                  "micronaut.http.services.*.thread-factory",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.max-content-length")) {
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
                  "micronaut.http.services.*.max-content-length",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.proxy-type")) {
            var4.setProxyType(
               (Type)super.getPropertyValueForSetter(
                  var1, var2, "setProxyType", Argument.of(Type.class, "proxyType"), "micronaut.http.services.*.proxy-type", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.proxy-address")) {
            var4.setProxyAddress(
               (SocketAddress)super.getPropertyValueForSetter(
                  var1, var2, "setProxyAddress", Argument.of(SocketAddress.class, "proxyAddress"), "micronaut.http.services.*.proxy-address", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.proxy-username")) {
            var4.setProxyUsername(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProxyUsername", Argument.of(String.class, "proxyUsername"), "micronaut.http.services.*.proxy-username", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.proxy-password")) {
            var4.setProxyPassword(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProxyPassword", Argument.of(String.class, "proxyPassword"), "micronaut.http.services.*.proxy-password", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.proxy-selector")) {
            var4.setProxySelector(
               (ProxySelector)super.getPropertyValueForSetter(
                  var1, var2, "setProxySelector", Argument.of(ProxySelector.class, "proxySelector"), "micronaut.http.services.*.proxy-selector", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.services.*.urls")) {
            var4.setUrls(
               (List<URI>)super.getPropertyValueForSetter(
                  var1, var2, "setUrls", Argument.of(List.class, "urls", null, Argument.ofTypeVariable(URI.class, "E")), "micronaut.http.services.*.urls", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.url")) {
            var4.setUrl((URI)super.getPropertyValueForSetter(var1, var2, "setUrl", Argument.of(URI.class, "url"), "micronaut.http.services.*.url", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.health-check-uri")) {
            var4.setHealthCheckUri(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setHealthCheckUri", Argument.of(String.class, "healthCheckUri"), "micronaut.http.services.*.health-check-uri", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.health-check")) {
            var4.setHealthCheck(
               super.getPropertyValueForSetter(
                  var1, var2, "setHealthCheck", Argument.of(Boolean.TYPE, "healthCheck"), "micronaut.http.services.*.health-check", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.path")) {
            var4.setPath(
               (String)super.getPropertyValueForSetter(var1, var2, "setPath", Argument.of(String.class, "path"), "micronaut.http.services.*.path", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.health-check-interval")) {
            var4.setHealthCheckInterval(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHealthCheckInterval",
                  Argument.of(Duration.class, "healthCheckInterval"),
                  "micronaut.http.services.*.health-check-interval",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ServiceHttpClientConfiguration$Definition() {
      this(ServiceHttpClientConfiguration.class, $CONSTRUCTOR);
   }

   protected $ServiceHttpClientConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServiceHttpClientConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
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
