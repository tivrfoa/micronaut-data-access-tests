package com.example;

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
class $GenreController$Definition extends AbstractInitializableBeanDefinition<GenreController> implements BeanFactory<GenreController> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      GenreController.class, "<init>", new Argument[]{Argument.of(GenreRepository.class, "genreRepository")}, null, false
   );

   @Override
   public GenreController build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      GenreController var4 = new GenreController((GenreRepository)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (GenreController)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      GenreController var4 = (GenreController)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $GenreController$Definition() {
      this(GenreController.class, $CONSTRUCTOR);
   }

   protected $GenreController$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $GenreController$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $GenreController$Definition$Exec(),
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
