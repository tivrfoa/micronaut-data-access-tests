package io.micronaut.http.server;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.scheduling.executor.ExecutorSelector;
import io.micronaut.web.router.Router;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $RouteExecutor$Definition extends AbstractInitializableBeanDefinition<RouteExecutor> implements BeanFactory<RouteExecutor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RouteExecutor.class,
      "<init>",
      new Argument[]{
         Argument.of(Router.class, "router"),
         Argument.of(BeanContext.class, "beanContext"),
         Argument.of(RequestArgumentSatisfier.class, "requestArgumentSatisfier"),
         Argument.of(HttpServerConfiguration.class, "serverConfiguration"),
         Argument.of(ErrorResponseProcessor.class, "errorResponseProcessor", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(ExecutorSelector.class, "executorSelector")
      },
      null,
      false
   );

   @Override
   public RouteExecutor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RouteExecutor var4 = new RouteExecutor(
         (Router)super.getBeanForConstructorArgument(var1, var2, 0, null),
         var2,
         (RequestArgumentSatisfier)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (HttpServerConfiguration)super.getBeanForConstructorArgument(var1, var2, 3, null),
         (ErrorResponseProcessor<?>)super.getBeanForConstructorArgument(var1, var2, 4, null),
         (ExecutorSelector)super.getBeanForConstructorArgument(var1, var2, 5, null)
      );
      return (RouteExecutor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      RouteExecutor var4 = (RouteExecutor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $RouteExecutor$Definition() {
      this(RouteExecutor.class, $CONSTRUCTOR);
   }

   protected $RouteExecutor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RouteExecutor$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false
      );
   }
}
