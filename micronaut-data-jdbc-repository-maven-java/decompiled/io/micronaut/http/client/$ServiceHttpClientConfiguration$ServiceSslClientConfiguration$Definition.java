package io.micronaut.http.client;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.ssl.ClientAuthentication;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ServiceHttpClientConfiguration.ServiceSslClientConfiguration>
   implements BeanFactory<ServiceHttpClientConfiguration.ServiceSslClientConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServiceHttpClientConfiguration.ServiceSslClientConfiguration.class, "<init>", null, null, false
   );
   private static final Set $INNER_CONFIGURATION_CLASSES = new HashSet(
      Arrays.asList(
         ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyConfiguration.class,
         ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration.class,
         ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultTrustStoreConfiguration.class
      )
   );

   @Override
   public ServiceHttpClientConfiguration.ServiceSslClientConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServiceHttpClientConfiguration.ServiceSslClientConfiguration var4 = new ServiceHttpClientConfiguration.ServiceSslClientConfiguration();
      return (ServiceHttpClientConfiguration.ServiceSslClientConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ServiceHttpClientConfiguration.ServiceSslClientConfiguration var4 = (ServiceHttpClientConfiguration.ServiceSslClientConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.http.services.*.ssl.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.port")) {
            var4.setPort(super.getPropertyValueForSetter(var1, var2, "setPort", Argument.of(Integer.TYPE, "port"), "micronaut.http.services.*.ssl.port", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.build-self-signed")) {
            var4.setBuildSelfSigned(
               super.getPropertyValueForSetter(
                  var1, var2, "setBuildSelfSigned", Argument.of(Boolean.TYPE, "buildSelfSigned"), "micronaut.http.services.*.ssl.build-self-signed", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.key")) {
            var4.setKey(
               (SslConfiguration.KeyConfiguration)super.getPropertyValueForSetter(
                  var1, var2, "setKey", Argument.of(SslConfiguration.KeyConfiguration.class, "key"), "micronaut.http.services.*.ssl.key", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.key-store")) {
            var4.setKeyStore(
               (SslConfiguration.KeyStoreConfiguration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setKeyStore",
                  Argument.of(SslConfiguration.KeyStoreConfiguration.class, "keyStore"),
                  "micronaut.http.services.*.ssl.key-store",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.trust-store")) {
            var4.setTrustStore(
               (SslConfiguration.TrustStoreConfiguration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setTrustStore",
                  Argument.of(SslConfiguration.TrustStoreConfiguration.class, "trustStore"),
                  "micronaut.http.services.*.ssl.trust-store",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.client-authentication")) {
            var4.setClientAuthentication(
               (ClientAuthentication)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setClientAuthentication",
                  Argument.of(ClientAuthentication.class, "clientAuthentication"),
                  "micronaut.http.services.*.ssl.client-authentication",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.ciphers")) {
            var4.setCiphers(
               (String[])super.getPropertyValueForSetter(
                  var1, var2, "setCiphers", Argument.of(String[].class, "ciphers"), "micronaut.http.services.*.ssl.ciphers", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.protocols")) {
            var4.setProtocols(
               (String[])super.getPropertyValueForSetter(
                  var1, var2, "setProtocols", Argument.of(String[].class, "protocols"), "micronaut.http.services.*.ssl.protocols", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.protocol")) {
            var4.setProtocol(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProtocol", Argument.of(String.class, "protocol"), "micronaut.http.services.*.ssl.protocol", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.handshake-timeout")) {
            var4.setHandshakeTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHandshakeTimeout",
                  Argument.of(
                     Duration.class,
                     "arg0",
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
                  "micronaut.http.services.*.ssl.handshake-timeout",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.insecure-trust-all-certificates")) {
            var4.setInsecureTrustAllCertificates(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setInsecureTrustAllCertificates",
                  Argument.of(Boolean.TYPE, "insecureTrustAllCertificates"),
                  "micronaut.http.services.*.ssl.insecure-trust-all-certificates",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.services.*.ssl.key")) {
            var4.setKey(
               (ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyConfiguration)super.getBeanForSetter(
                  var1,
                  var2,
                  "setKey",
                  Argument.of(
                     ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyConfiguration.class,
                     "keyConfiguration",
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
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.services.*.ssl.key-store")) {
            var4.setKeyStore(
               (ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration)super.getBeanForSetter(
                  var1,
                  var2,
                  "setKeyStore",
                  Argument.of(
                     ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration.class,
                     "keyStoreConfiguration",
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
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.services.*.ssl.trust-store")) {
            var4.setTrustStore(
               (ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultTrustStoreConfiguration)super.getBeanForSetter(
                  var1,
                  var2,
                  "setTrustStore",
                  Argument.of(
                     ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultTrustStoreConfiguration.class,
                     "trustStore",
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
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$Definition() {
      this(ServiceHttpClientConfiguration.ServiceSslClientConfiguration.class, $CONSTRUCTOR);
   }

   protected $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
