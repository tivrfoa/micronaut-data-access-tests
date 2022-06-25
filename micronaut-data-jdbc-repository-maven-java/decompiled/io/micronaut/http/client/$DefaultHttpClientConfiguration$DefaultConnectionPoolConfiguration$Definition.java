package io.micronaut.http.client;

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
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHttpClientConfiguration$DefaultConnectionPoolConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration>
   implements BeanFactory<DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration var4 = new DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration();
      return (DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration var4 = (DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.pool.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.http.client.pool.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.pool.max-connections")) {
            var4.setMaxConnections(
               super.getPropertyValueForSetter(
                  var1, var2, "setMaxConnections", Argument.of(Integer.TYPE, "maxConnections"), "micronaut.http.client.pool.max-connections", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.pool.max-pending-acquires")) {
            var4.setMaxPendingAcquires(
               super.getPropertyValueForSetter(
                  var1, var2, "setMaxPendingAcquires", Argument.of(Integer.TYPE, "maxPendingAcquires"), "micronaut.http.client.pool.max-pending-acquires", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.pool.acquire-timeout")) {
            var4.setAcquireTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setAcquireTimeout",
                  Argument.of(
                     Duration.class,
                     "acquireTimeout",
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
                  "micronaut.http.client.pool.acquire-timeout",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpClientConfiguration$DefaultConnectionPoolConfiguration$Definition() {
      this(DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpClientConfiguration$DefaultConnectionPoolConfiguration$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $DefaultHttpClientConfiguration$DefaultConnectionPoolConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
}
