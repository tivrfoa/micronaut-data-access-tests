package io.micronaut.management.endpoint.info;

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
class $InfoEndpoint$Definition extends AbstractInitializableBeanDefinition<InfoEndpoint> implements BeanFactory<InfoEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      InfoEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(InfoAggregator.class, "infoAggregator", null, Argument.ofTypeVariable(Object.class, "T")), Argument.of(InfoSource[].class, "infoSources")
      },
      null,
      false
   );

   @Override
   public InfoEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      InfoEndpoint var4 = new InfoEndpoint(
         (InfoAggregator)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (InfoSource[])super.getBeansOfTypeForConstructorArgument(var1, var2, 1, Argument.of(InfoSource.class, null), null).toArray(new InfoSource[0])
      );
      return (InfoEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         InfoEndpoint var4 = (InfoEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $InfoEndpoint$Definition() {
      this(InfoEndpoint.class, $CONSTRUCTOR);
   }

   protected $InfoEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $InfoEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $InfoEndpoint$Definition$Exec(),
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
