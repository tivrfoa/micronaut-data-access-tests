package io.micronaut.http.client;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.scheduling.TaskScheduler;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ServiceHttpClientFactory$Definition
   extends AbstractInitializableBeanDefinition<ServiceHttpClientFactory>
   implements BeanFactory<ServiceHttpClientFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServiceHttpClientFactory.class,
      "<init>",
      new Argument[]{
         Argument.of(TaskScheduler.class, "taskScheduler"),
         Argument.of(
            BeanProvider.class,
            "clientFactory",
            null,
            Argument.ofTypeVariable(HttpClientRegistry.class, "T", null, Argument.ofTypeVariable(HttpClient.class, "T"))
         )
      },
      null,
      false
   );

   @Override
   public ServiceHttpClientFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServiceHttpClientFactory var4 = new ServiceHttpClientFactory(
         (TaskScheduler)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (BeanProvider<HttpClientRegistry<?>>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (ServiceHttpClientFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ServiceHttpClientFactory var4 = (ServiceHttpClientFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ServiceHttpClientFactory$Definition() {
      this(ServiceHttpClientFactory.class, $CONSTRUCTOR);
   }

   protected $ServiceHttpClientFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServiceHttpClientFactory$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.empty(),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         false
      );
   }
}
