package io.micronaut.http.server.netty.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$Definition
   extends AbstractInitializableBeanDefinition<NettyHttpServerConfiguration.FileTypeHandlerConfiguration>
   implements BeanFactory<NettyHttpServerConfiguration.FileTypeHandlerConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;
   private static final Set $INNER_CONFIGURATION_CLASSES = Collections.singleton(
      NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration.class
   );

   @Override
   public NettyHttpServerConfiguration.FileTypeHandlerConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyHttpServerConfiguration.FileTypeHandlerConfiguration var4 = new NettyHttpServerConfiguration.FileTypeHandlerConfiguration(
         (Integer)super.getPropertyValueForConstructorArgument(var1, var2, 0, "netty.responses.file.cache-seconds", null),
         (Boolean)super.getPropertyValueForConstructorArgument(var1, var2, 1, "netty.responses.file.cache-control.public", null)
      );
      return (NettyHttpServerConfiguration.FileTypeHandlerConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyHttpServerConfiguration.FileTypeHandlerConfiguration var4 = (NettyHttpServerConfiguration.FileTypeHandlerConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.responses.file.cache-seconds")) {
            var4.setCacheSeconds(
               super.getPropertyValueForSetter(
                  var1, var2, "setCacheSeconds", Argument.of(Integer.TYPE, "cacheSeconds"), "micronaut.server.netty.responses.file.cache-seconds", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.responses.file.cache-control")) {
            var4.setCacheControl(
               (NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration)super.getBeanForSetter(
                  var1,
                  var2,
                  "setCacheControl",
                  Argument.of(NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration.class, "cacheControl"),
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         NettyHttpServerConfiguration.FileTypeHandlerConfiguration.class,
         "<init>",
         new Argument[]{
            Argument.of(
               Integer.class,
               "cacheSeconds",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property",
                              AnnotationUtil.mapOf("name", "netty.responses.file.cache-seconds"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Property")
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "netty.responses.file.cache-seconds"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               Boolean.class,
               "isPublic",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "netty.responses.file.cache-control.public"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.PropertySource",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "netty.responses.file.cache-control.public"), var0
                           )
                        }
                     ),
                     "javax.annotation.Nullable",
                     Collections.EMPTY_MAP
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            )
         },
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("java.lang.Deprecated", Collections.EMPTY_MAP, "javax.inject.Inject", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.mapOf("java.lang.Deprecated", Collections.EMPTY_MAP, "javax.inject.Inject", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         false
      );
   }

   public $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$Definition() {
      this(NettyHttpServerConfiguration.FileTypeHandlerConfiguration.class, $CONSTRUCTOR);
   }

   protected $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
