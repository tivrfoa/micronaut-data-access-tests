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
class $ServiceHttpClientConfiguration$ServiceConnectionPoolConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration>
   implements BeanFactory<ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration.class, "<init>", null, null, false
   );

   @Override
   public ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration var4 = new ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration();
      return (ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration var4 = (ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.pool.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.http.services.*.pool.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.pool.max-connections")) {
            var4.setMaxConnections(
               super.getPropertyValueForSetter(
                  var1, var2, "setMaxConnections", Argument.of(Integer.TYPE, "maxConnections"), "micronaut.http.services.*.pool.max-connections", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.pool.max-pending-acquires")) {
            var4.setMaxPendingAcquires(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxPendingAcquires",
                  Argument.of(Integer.TYPE, "maxPendingAcquires"),
                  "micronaut.http.services.*.pool.max-pending-acquires",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.pool.acquire-timeout")) {
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
                  "micronaut.http.services.*.pool.acquire-timeout",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ServiceHttpClientConfiguration$ServiceConnectionPoolConfiguration$Definition() {
      this(ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration.class, $CONSTRUCTOR);
   }

   protected $ServiceHttpClientConfiguration$ServiceConnectionPoolConfiguration$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $ServiceHttpClientConfiguration$ServiceConnectionPoolConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
