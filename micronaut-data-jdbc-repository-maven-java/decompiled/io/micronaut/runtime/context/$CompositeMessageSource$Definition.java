package io.micronaut.runtime.context;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.MessageSource;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $CompositeMessageSource$Definition extends AbstractInitializableBeanDefinition<CompositeMessageSource> implements BeanFactory<CompositeMessageSource> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CompositeMessageSource.class,
      "<init>",
      new Argument[]{
         Argument.of(
            Collection.class,
            "messageSources",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(MessageSource.class, "E")
         )
      },
      null,
      false
   );

   @Override
   public CompositeMessageSource build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CompositeMessageSource var4 = new CompositeMessageSource(
         super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      return (CompositeMessageSource)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CompositeMessageSource var4 = (CompositeMessageSource)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CompositeMessageSource$Definition() {
      this(CompositeMessageSource.class, $CONSTRUCTOR);
   }

   protected $CompositeMessageSource$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CompositeMessageSource$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.empty(),
         false,
         false,
         false,
         false,
         true,
         false,
         false,
         false
      );
   }
}
