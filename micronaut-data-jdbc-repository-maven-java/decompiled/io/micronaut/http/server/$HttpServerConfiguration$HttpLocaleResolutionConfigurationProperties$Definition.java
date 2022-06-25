package io.micronaut.http.server;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpServerConfiguration$HttpLocaleResolutionConfigurationProperties$Definition
   extends AbstractInitializableBeanDefinition<HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties>
   implements BeanFactory<HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties.class, "<init>", null, null, false
   );

   @Override
   public HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties var4 = new HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties();
      return (HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties var4 = (HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.locale-resolution.fixed")) {
            var4.setFixed(
               (Locale)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setFixed",
                  Argument.of(
                     Locale.class,
                     "fixed",
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
                  "micronaut.server.locale-resolution.fixed",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.locale-resolution.session-attribute")) {
            var4.setSessionAttribute(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setSessionAttribute",
                  Argument.of(
                     String.class,
                     "sessionAttribute",
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
                  "micronaut.server.locale-resolution.session-attribute",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.locale-resolution.default-locale")) {
            var4.setDefaultLocale(
               (Locale)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setDefaultLocale",
                  Argument.of(
                     Locale.class,
                     "defaultLocale",
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
                  "micronaut.server.locale-resolution.default-locale",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.locale-resolution.cookie-name")) {
            var4.setCookieName(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setCookieName",
                  Argument.of(
                     String.class,
                     "cookieName",
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
                  "micronaut.server.locale-resolution.cookie-name",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.locale-resolution.header")) {
            var4.setHeader(
               super.getPropertyValueForSetter(var1, var2, "setHeader", Argument.of(Boolean.TYPE, "header"), "micronaut.server.locale-resolution.header", null)
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $HttpServerConfiguration$HttpLocaleResolutionConfigurationProperties$Definition() {
      this(HttpServerConfiguration.HttpLocaleResolutionConfigurationProperties.class, $CONSTRUCTOR);
   }

   protected $HttpServerConfiguration$HttpLocaleResolutionConfigurationProperties$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $HttpServerConfiguration$HttpLocaleResolutionConfigurationProperties$Definition$Reference.$ANNOTATION_METADATA,
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
}
