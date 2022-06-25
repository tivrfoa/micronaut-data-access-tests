package io.micronaut.http.server;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.io.File;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpServerConfiguration$MultipartConfiguration$Definition
   extends AbstractInitializableBeanDefinition<HttpServerConfiguration.MultipartConfiguration>
   implements BeanFactory<HttpServerConfiguration.MultipartConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpServerConfiguration.MultipartConfiguration.class, "<init>", null, null, false
   );

   @Override
   public HttpServerConfiguration.MultipartConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpServerConfiguration.MultipartConfiguration var4 = new HttpServerConfiguration.MultipartConfiguration();
      return (HttpServerConfiguration.MultipartConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HttpServerConfiguration.MultipartConfiguration var4 = (HttpServerConfiguration.MultipartConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.multipart.location")) {
            var4.setLocation(
               (File)super.getPropertyValueForSetter(
                  var1, var2, "setLocation", Argument.of(File.class, "location"), "micronaut.server.multipart.location", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.multipart.max-file-size")) {
            var4.setMaxFileSize(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxFileSize",
                  Argument.of(
                     Long.TYPE,
                     "maxFileSize",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.multipart.max-file-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.multipart.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.server.multipart.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.multipart.disk")) {
            var4.setDisk(super.getPropertyValueForSetter(var1, var2, "setDisk", Argument.of(Boolean.TYPE, "disk"), "micronaut.server.multipart.disk", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.multipart.mixed")) {
            var4.setMixed(super.getPropertyValueForSetter(var1, var2, "setMixed", Argument.of(Boolean.TYPE, "mixed"), "micronaut.server.multipart.mixed", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.multipart.threshold")) {
            var4.setThreshold(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setThreshold",
                  Argument.of(
                     Long.TYPE,
                     "threshold",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.multipart.threshold",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $HttpServerConfiguration$MultipartConfiguration$Definition() {
      this(HttpServerConfiguration.MultipartConfiguration.class, $CONSTRUCTOR);
   }

   protected $HttpServerConfiguration$MultipartConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpServerConfiguration$MultipartConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
