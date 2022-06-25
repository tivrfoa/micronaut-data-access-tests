package io.micronaut.http.server;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpVersion;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.executor.ThreadSelection;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $HttpServerConfiguration$Definition extends AbstractInitializableBeanDefinition<HttpServerConfiguration> implements BeanFactory<HttpServerConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpServerConfiguration.class,
      "<init>",
      new Argument[]{Argument.of(ApplicationConfiguration.class, "applicationConfiguration")},
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
      Arrays.asList(
         HttpServerConfiguration.MultipartConfiguration.class,
         HttpServerConfiguration.CorsConfiguration.class,
         HttpServerConfiguration.HostResolutionConfiguration.class,
         HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties.class
      )
   );

   @Override
   public HttpServerConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpServerConfiguration var4 = new HttpServerConfiguration((ApplicationConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (HttpServerConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HttpServerConfiguration var4 = (HttpServerConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.http-version") || this.containsPropertyValue(var1, var2, "httpVersion")) {
            var4.setHttpVersion(
               (HttpVersion)super.getPropertyValueForSetter(
                  var1, var2, "setHttpVersion", Argument.of(HttpVersion.class, "httpVersion"), "micronaut.server.http-version", "httpVersion"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.thread-selection") || this.containsPropertyValue(var1, var2, "threadSelection")) {
            var4.setThreadSelection(
               (ThreadSelection)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setThreadSelection",
                  Argument.of(ThreadSelection.class, "threadSelection"),
                  "micronaut.server.thread-selection",
                  "threadSelection"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.default-charset") || this.containsPropertyValue(var1, var2, "defaultCharset")) {
            var4.setDefaultCharset(
               (Charset)super.getPropertyValueForSetter(
                  var1, var2, "setDefaultCharset", Argument.of(Charset.class, "defaultCharset"), "micronaut.server.default-charset", "defaultCharset"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.port") || this.containsPropertyValue(var1, var2, "port")) {
            var4.setPort(super.getPropertyValueForSetter(var1, var2, "setPort", Argument.of(Integer.TYPE, "port"), "micronaut.server.port", "port"));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.host") || this.containsPropertyValue(var1, var2, "host")) {
            var4.setHost((String)super.getPropertyValueForSetter(var1, var2, "setHost", Argument.of(String.class, "host"), "micronaut.server.host", "host"));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.read-timeout") || this.containsPropertyValue(var1, var2, "readTimeout")) {
            var4.setReadTimeout(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setReadTimeout", Argument.of(Integer.class, "readTimeout"), "micronaut.server.read-timeout", "readTimeout"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.server-header") || this.containsPropertyValue(var1, var2, "serverHeader")) {
            var4.setServerHeader(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setServerHeader", Argument.of(String.class, "serverHeader"), "micronaut.server.server-header", "serverHeader"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.max-request-size") || this.containsPropertyValue(var1, var2, "maxRequestSize")) {
            var4.setMaxRequestSize(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxRequestSize",
                  Argument.of(
                     Long.TYPE,
                     "maxRequestSize",
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
                  "maxRequestSize"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.read-idle-timeout") || this.containsPropertyValue(var1, var2, "readIdleTimeout")) {
            var4.setReadIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setReadIdleTimeout", Argument.of(Duration.class, "readIdleTimeout"), "micronaut.server.read-idle-timeout", "readIdleTimeout"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.write-idle-timeout") || this.containsPropertyValue(var1, var2, "writeIdleTimeout")) {
            var4.setWriteIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setWriteIdleTimeout", Argument.of(Duration.class, "writeIdleTimeout"), "micronaut.server.write-idle-timeout", "writeIdleTimeout"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.idle-timeout") || this.containsPropertyValue(var1, var2, "idleTimeout")) {
            var4.setIdleTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setIdleTimeout", Argument.of(Duration.class, "idleTimeout"), "micronaut.server.idle-timeout", "idleTimeout"
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.multipart") || this.containsPropertyValue(var1, var2, "multipart")) {
            var4.setMultipart(
               (HttpServerConfiguration.MultipartConfiguration)super.getBeanForSetter(
                  var1, var2, "setMultipart", Argument.of(HttpServerConfiguration.MultipartConfiguration.class, "multipart"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.cors") || this.containsPropertyValue(var1, var2, "cors")) {
            var4.setCors(
               (HttpServerConfiguration.CorsConfiguration)super.getBeanForSetter(
                  var1, var2, "setCors", Argument.of(HttpServerConfiguration.CorsConfiguration.class, "cors"), null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.date-header") || this.containsPropertyValue(var1, var2, "dateHeader")) {
            var4.setDateHeader(
               super.getPropertyValueForSetter(
                  var1, var2, "setDateHeader", Argument.of(Boolean.TYPE, "dateHeader"), "micronaut.server.date-header", "dateHeader"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.log-handled-exceptions")
            || this.containsPropertyValue(var1, var2, "logHandledExceptions")) {
            var4.setLogHandledExceptions(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setLogHandledExceptions",
                  Argument.of(Boolean.TYPE, "logHandledExceptions"),
                  "micronaut.server.log-handled-exceptions",
                  "logHandledExceptions"
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.host-resolution") || this.containsPropertyValue(var1, var2, "hostResolution")) {
            var4.setHostResolution(
               (HttpServerConfiguration.HostResolutionConfiguration)super.getBeanForSetter(
                  var1, var2, "setHostResolution", Argument.of(HttpServerConfiguration.HostResolutionConfiguration.class, "hostResolution"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.locale-resolution") || this.containsPropertyValue(var1, var2, "localeResolution")) {
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

         if (this.containsPropertyValue(var1, var2, "micronaut.server.client-address-header") || this.containsPropertyValue(var1, var2, "clientAddressHeader")) {
            var4.setClientAddressHeader(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setClientAddressHeader",
                  Argument.of(String.class, "clientAddressHeader"),
                  "micronaut.server.client-address-header",
                  "clientAddressHeader"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.context-path") || this.containsPropertyValue(var1, var2, "contextPath")) {
            var4.setContextPath(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setContextPath", Argument.of(String.class, "contextPath"), "micronaut.server.context-path", "contextPath"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.dual-protocol") || this.containsPropertyValue(var1, var2, "dualProtocol")) {
            var4.setDualProtocol(
               super.getPropertyValueForSetter(
                  var1, var2, "setDualProtocol", Argument.of(Boolean.TYPE, "dualProtocol"), "micronaut.server.dual-protocol", "dualProtocol"
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.http-to-https-redirect") || this.containsPropertyValue(var1, var2, "httpToHttpsRedirect")
            )
          {
            var4.setHttpToHttpsRedirect(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHttpToHttpsRedirect",
                  Argument.of(Boolean.TYPE, "httpToHttpsRedirect"),
                  "micronaut.server.http-to-https-redirect",
                  "httpToHttpsRedirect"
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $HttpServerConfiguration$Definition() {
      this(HttpServerConfiguration.class, $CONSTRUCTOR);
   }

   protected $HttpServerConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpServerConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
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
