package io.micronaut.http.ssl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $ServerSslConfiguration$Definition extends AbstractInitializableBeanDefinition<ServerSslConfiguration> implements BeanFactory<ServerSslConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServerSslConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(DefaultSslConfiguration.class, "defaultSslConfiguration"),
         Argument.of(DefaultSslConfiguration.DefaultKeyConfiguration.class, "defaultKeyConfiguration"),
         Argument.of(DefaultSslConfiguration.DefaultKeyStoreConfiguration.class, "defaultKeyStoreConfiguration"),
         Argument.of(SslConfiguration.TrustStoreConfiguration.class, "defaultTrustStoreConfiguration")
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
         ServerSslConfiguration.class,
         "setKey",
         new Argument[]{
            Argument.of(
               ServerSslConfiguration.DefaultKeyConfiguration.class,
               "keyConfiguration",
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
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.ssl")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.ssl")
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
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         ServerSslConfiguration.class,
         "setKeyStore",
         new Argument[]{
            Argument.of(
               ServerSslConfiguration.DefaultKeyStoreConfiguration.class,
               "keyStoreConfiguration",
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
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.ssl")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.ssl")
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
      ),
      new AbstractInitializableBeanDefinition.MethodReference(
         ServerSslConfiguration.class,
         "setTrustStore",
         new Argument[]{
            Argument.of(
               ServerSslConfiguration.DefaultTrustStoreConfiguration.class,
               "trustStore",
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
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.ssl")
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "javax.inject.Scope",
                  Collections.EMPTY_MAP,
                  "javax.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.ConfigurationProperties",
                  AnnotationUtil.mapOf("value", "micronaut.server.ssl"),
                  "io.micronaut.context.annotation.ConfigurationReader",
                  AnnotationUtil.mapOf("prefix", "micronaut.server.ssl")
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
         ServerSslConfiguration.DefaultKeyConfiguration.class,
         ServerSslConfiguration.DefaultKeyStoreConfiguration.class,
         ServerSslConfiguration.DefaultTrustStoreConfiguration.class
      )
   );

   @Override
   public ServerSslConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServerSslConfiguration var4 = new ServerSslConfiguration(
         (DefaultSslConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (DefaultSslConfiguration.DefaultKeyConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (DefaultSslConfiguration.DefaultKeyStoreConfiguration)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (SslConfiguration.TrustStoreConfiguration)super.getBeanForConstructorArgument(var1, var2, 3, null)
      );
      return (ServerSslConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ServerSslConfiguration var4 = (ServerSslConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.server.ssl.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.key")) {
            var4.setKey(
               (SslConfiguration.KeyConfiguration)super.getPropertyValueForSetter(
                  var1, var2, "setKey", Argument.of(SslConfiguration.KeyConfiguration.class, "key"), "micronaut.server.ssl.key", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.key-store")) {
            var4.setKeyStore(
               (SslConfiguration.KeyStoreConfiguration)super.getPropertyValueForSetter(
                  var1, var2, "setKeyStore", Argument.of(SslConfiguration.KeyStoreConfiguration.class, "keyStore"), "micronaut.server.ssl.key-store", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.trust-store")) {
            var4.setTrustStore(
               (SslConfiguration.TrustStoreConfiguration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setTrustStore",
                  Argument.of(SslConfiguration.TrustStoreConfiguration.class, "trustStore"),
                  "micronaut.server.ssl.trust-store",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.client-authentication")) {
            var4.setClientAuthentication(
               (ClientAuthentication)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setClientAuthentication",
                  Argument.of(ClientAuthentication.class, "clientAuthentication"),
                  "micronaut.server.ssl.client-authentication",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.ciphers")) {
            var4.setCiphers(
               (String[])super.getPropertyValueForSetter(var1, var2, "setCiphers", Argument.of(String[].class, "ciphers"), "micronaut.server.ssl.ciphers", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.protocols")) {
            var4.setProtocols(
               (String[])super.getPropertyValueForSetter(
                  var1, var2, "setProtocols", Argument.of(String[].class, "protocols"), "micronaut.server.ssl.protocols", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.protocol")) {
            var4.setProtocol(
               (String)super.getPropertyValueForSetter(var1, var2, "setProtocol", Argument.of(String.class, "protocol"), "micronaut.server.ssl.protocol", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.handshake-timeout")) {
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
                  "micronaut.server.ssl.handshake-timeout",
                  null
               )
            );
         }

         var4.setKey(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
         var4.setKeyStore(super.getBeanForMethodArgument(var1, var2, 1, 0, null));
         var4.setTrustStore(super.getBeanForMethodArgument(var1, var2, 2, 0, null));
         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.port")) {
            var4.setPort(super.getPropertyValueForSetter(var1, var2, "setPort", Argument.of(Integer.TYPE, "port"), "micronaut.server.ssl.port", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.build-self-signed")) {
            var4.setBuildSelfSigned(
               super.getPropertyValueForSetter(
                  var1, var2, "setBuildSelfSigned", Argument.of(Boolean.TYPE, "buildSelfSigned"), "micronaut.server.ssl.build-self-signed", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ServerSslConfiguration$Definition() {
      this(ServerSslConfiguration.class, $CONSTRUCTOR);
   }

   protected $ServerSslConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServerSslConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
