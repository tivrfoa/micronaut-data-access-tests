package io.micronaut.http.resource;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ResourceLoaderFactory$FileSystemResourceLoader1$Definition
   extends AbstractInitializableBeanDefinition<FileSystemResourceLoader>
   implements BeanFactory<FileSystemResourceLoader> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ResourceLoaderFactory.class,
      "fileSystemResourceLoader",
      null,
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "javax.annotation.Nonnull",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Indexes",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Indexed",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            ),
            "javax.annotation.Nonnull",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
         false,
         true
      ),
      false
   );

   @Override
   public FileSystemResourceLoader build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, ResourceLoaderFactory.class, null);
      var1.markDependentAsFactory();
      FileSystemResourceLoader var5 = ((ResourceLoaderFactory)var4).fileSystemResourceLoader();
      return (FileSystemResourceLoader)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      FileSystemResourceLoader var4 = (FileSystemResourceLoader)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ResourceLoaderFactory$FileSystemResourceLoader1$Definition() {
      this(FileSystemResourceLoader.class, $CONSTRUCTOR);
   }

   protected $ResourceLoaderFactory$FileSystemResourceLoader1$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ResourceLoaderFactory$FileSystemResourceLoader1$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false
      );
   }
}
