package io.micronaut.management.endpoint.threads;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ThreadDumpEndpoint$Definition extends AbstractInitializableBeanDefinition<ThreadDumpEndpoint> implements BeanFactory<ThreadDumpEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ThreadDumpEndpoint.class,
      "<init>",
      new Argument[]{Argument.of(ThreadInfoMapper.class, "threadInfoMapper", null, Argument.ofTypeVariable(Object.class, "T"))},
      null,
      false
   );

   @Override
   public ThreadDumpEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ThreadDumpEndpoint var4 = new ThreadDumpEndpoint((ThreadInfoMapper<?>)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (ThreadDumpEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ThreadDumpEndpoint var4 = (ThreadDumpEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ThreadDumpEndpoint$Definition() {
      this(ThreadDumpEndpoint.class, $CONSTRUCTOR);
   }

   protected $ThreadDumpEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ThreadDumpEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $ThreadDumpEndpoint$Definition$Exec(),
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
