package io.micronaut.data.jdbc.convert.vendor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $OracleTypeConvertersFactory$Definition
   extends AbstractInitializableBeanDefinition<OracleTypeConvertersFactory>
   implements BeanFactory<OracleTypeConvertersFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      OracleTypeConvertersFactory.class, "<init>", null, null, false
   );

   @Override
   public OracleTypeConvertersFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      OracleTypeConvertersFactory var4 = new OracleTypeConvertersFactory();
      return (OracleTypeConvertersFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      OracleTypeConvertersFactory var4 = (OracleTypeConvertersFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $OracleTypeConvertersFactory$Definition() {
      this(OracleTypeConvertersFactory.class, $CONSTRUCTOR);
   }

   protected $OracleTypeConvertersFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $OracleTypeConvertersFactory$Definition$Reference.$ANNOTATION_METADATA,
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
