package io.micronaut.http.codec;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $CodecConfiguration$Definition extends AbstractInitializableBeanDefinition<CodecConfiguration> implements BeanFactory<CodecConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CodecConfiguration.class, "<init>", null, null, false
   );

   @Override
   public CodecConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CodecConfiguration var4 = new CodecConfiguration();
      return (CodecConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         CodecConfiguration var4 = (CodecConfiguration)var3;
         if (this.containsPropertiesValue(var1, var2, "micronaut.codec.*.additional-types")) {
            var4.setAdditionalTypes(
               (List<MediaType>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setAdditionalTypes",
                  Argument.of(List.class, "additionalTypes", null, Argument.ofTypeVariable(MediaType.class, "E")),
                  "micronaut.codec.*.additional-types",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $CodecConfiguration$Definition() {
      this(CodecConfiguration.class, $CONSTRUCTOR);
   }

   protected $CodecConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CodecConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
