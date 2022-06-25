package io.micronaut.http.ssl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
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
class $DefaultSslConfiguration$Definition extends AbstractInitializableBeanDefinition<DefaultSslConfiguration> implements BeanFactory<DefaultSslConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultSslConfiguration.class, "<init>", null, null, false
   );
   private static final Set $INNER_CONFIGURATION_CLASSES = new HashSet(
      Arrays.asList(
         DefaultSslConfiguration.DefaultKeyConfiguration.class,
         DefaultSslConfiguration.DefaultKeyStoreConfiguration.class,
         DefaultSslConfiguration.DefaultTrustStoreConfiguration.class
      )
   );

   @Override
   public DefaultSslConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultSslConfiguration var4 = new DefaultSslConfiguration();
      return (DefaultSslConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultSslConfiguration var4 = (DefaultSslConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.enabled")) {
            var4.setEnabled(super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.ssl.enabled", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.port")) {
            var4.setPort(super.getPropertyValueForSetter(var1, var2, "setPort", Argument.of(Integer.TYPE, "port"), "micronaut.ssl.port", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.build-self-signed")) {
            var4.setBuildSelfSigned(
               super.getPropertyValueForSetter(
                  var1, var2, "setBuildSelfSigned", Argument.of(Boolean.TYPE, "buildSelfSigned"), "micronaut.ssl.build-self-signed", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.key")) {
            var4.setKey(
               (SslConfiguration.KeyConfiguration)super.getPropertyValueForSetter(
                  var1, var2, "setKey", Argument.of(SslConfiguration.KeyConfiguration.class, "key"), "micronaut.ssl.key", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.key-store")) {
            var4.setKeyStore(
               (SslConfiguration.KeyStoreConfiguration)super.getPropertyValueForSetter(
                  var1, var2, "setKeyStore", Argument.of(SslConfiguration.KeyStoreConfiguration.class, "keyStore"), "micronaut.ssl.key-store", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.trust-store")) {
            var4.setTrustStore(
               (SslConfiguration.TrustStoreConfiguration)super.getPropertyValueForSetter(
                  var1, var2, "setTrustStore", Argument.of(SslConfiguration.TrustStoreConfiguration.class, "trustStore"), "micronaut.ssl.trust-store", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.client-authentication")) {
            var4.setClientAuthentication(
               (ClientAuthentication)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setClientAuthentication",
                  Argument.of(ClientAuthentication.class, "clientAuthentication"),
                  "micronaut.ssl.client-authentication",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.ciphers")) {
            var4.setCiphers(
               (String[])super.getPropertyValueForSetter(var1, var2, "setCiphers", Argument.of(String[].class, "ciphers"), "micronaut.ssl.ciphers", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.protocols")) {
            var4.setProtocols(
               (String[])super.getPropertyValueForSetter(var1, var2, "setProtocols", Argument.of(String[].class, "protocols"), "micronaut.ssl.protocols", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.protocol")) {
            var4.setProtocol(
               (String)super.getPropertyValueForSetter(var1, var2, "setProtocol", Argument.of(String.class, "protocol"), "micronaut.ssl.protocol", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.handshake-timeout")) {
            var4.setHandshakeTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHandshakeTimeout",
                  Argument.of(
                     Duration.class,
                     "handshakeTimeout",
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
                  "micronaut.ssl.handshake-timeout",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.ssl.key")) {
            var4.setKey(
               (DefaultSslConfiguration.DefaultKeyConfiguration)super.getBeanForSetter(
                  var1, var2, "setKey", Argument.of(DefaultSslConfiguration.DefaultKeyConfiguration.class, "keyConfiguration"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.ssl.key-store")) {
            var4.setKeyStore(
               (DefaultSslConfiguration.DefaultKeyStoreConfiguration)super.getBeanForSetter(
                  var1, var2, "setKeyStore", Argument.of(DefaultSslConfiguration.DefaultKeyStoreConfiguration.class, "keyStoreConfiguration"), null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.ssl.trust-store")) {
            var4.setTrustStore(
               (DefaultSslConfiguration.DefaultTrustStoreConfiguration)super.getBeanForSetter(
                  var1, var2, "setTrustStore", Argument.of(DefaultSslConfiguration.DefaultTrustStoreConfiguration.class, "trustStore"), null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultSslConfiguration$Definition() {
      this(DefaultSslConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultSslConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultSslConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
