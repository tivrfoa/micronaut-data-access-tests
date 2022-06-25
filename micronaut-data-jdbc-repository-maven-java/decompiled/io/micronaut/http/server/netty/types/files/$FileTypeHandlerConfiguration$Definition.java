package io.micronaut.http.server.netty.types.files;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $FileTypeHandlerConfiguration$Definition
   extends AbstractInitializableBeanDefinition<FileTypeHandlerConfiguration>
   implements BeanFactory<FileTypeHandlerConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      FileTypeHandlerConfiguration.class, "<init>", null, null, false
   );
   private static final Set $INNER_CONFIGURATION_CLASSES = Collections.singleton(FileTypeHandlerConfiguration.CacheControlConfiguration.class);

   @Override
   public FileTypeHandlerConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      FileTypeHandlerConfiguration var4 = new FileTypeHandlerConfiguration();
      return (FileTypeHandlerConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         FileTypeHandlerConfiguration var4 = (FileTypeHandlerConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "netty.responses.file.cache-seconds")) {
            var4.setCacheSeconds(
               super.getPropertyValueForSetter(
                  var1, var2, "setCacheSeconds", Argument.of(Integer.TYPE, "cacheSeconds"), "netty.responses.file.cache-seconds", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "netty.responses.file.cache-control")) {
            var4.setCacheControl(
               (FileTypeHandlerConfiguration.CacheControlConfiguration)super.getBeanForSetter(
                  var1, var2, "setCacheControl", Argument.of(FileTypeHandlerConfiguration.CacheControlConfiguration.class, "cacheControl"), null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $FileTypeHandlerConfiguration$Definition() {
      this(FileTypeHandlerConfiguration.class, $CONSTRUCTOR);
   }

   protected $FileTypeHandlerConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $FileTypeHandlerConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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

   @Override
   protected boolean isInnerConfiguration(Class var1) {
      return $INNER_CONFIGURATION_CLASSES.contains(var1);
   }
}
