package io.micronaut.http.server.netty.types.files;

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
class $FileTypeHandlerConfiguration$CacheControlConfiguration$Definition
   extends AbstractInitializableBeanDefinition<FileTypeHandlerConfiguration.CacheControlConfiguration>
   implements BeanFactory<FileTypeHandlerConfiguration.CacheControlConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      FileTypeHandlerConfiguration.CacheControlConfiguration.class, "<init>", null, null, false
   );

   @Override
   public FileTypeHandlerConfiguration.CacheControlConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      FileTypeHandlerConfiguration.CacheControlConfiguration var4 = new FileTypeHandlerConfiguration.CacheControlConfiguration();
      return (FileTypeHandlerConfiguration.CacheControlConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         FileTypeHandlerConfiguration.CacheControlConfiguration var4 = (FileTypeHandlerConfiguration.CacheControlConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "netty.responses.file.cache-control.public")) {
            var4.setPublic(
               super.getPropertyValueForSetter(
                  var1, var2, "setPublic", Argument.of(Boolean.TYPE, "publicCache"), "netty.responses.file.cache-control.public", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $FileTypeHandlerConfiguration$CacheControlConfiguration$Definition() {
      this(FileTypeHandlerConfiguration.CacheControlConfiguration.class, $CONSTRUCTOR);
   }

   protected $FileTypeHandlerConfiguration$CacheControlConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $FileTypeHandlerConfiguration$CacheControlConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
