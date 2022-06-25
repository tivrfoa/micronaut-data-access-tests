package io.micronaut.data.runtime.convert;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DataConversionServiceFactory$Definition
   extends AbstractInitializableBeanDefinition<DataConversionServiceFactory>
   implements BeanFactory<DataConversionServiceFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataConversionServiceFactory.class, "<init>", null, null, false
   );

   @Override
   public DataConversionServiceFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DataConversionServiceFactory var4 = new DataConversionServiceFactory();
      return (DataConversionServiceFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DataConversionServiceFactory var4 = (DataConversionServiceFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DataConversionServiceFactory$Definition() {
      this(DataConversionServiceFactory.class, $CONSTRUCTOR);
   }

   protected $DataConversionServiceFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataConversionServiceFactory$Definition$Reference.$ANNOTATION_METADATA,
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
