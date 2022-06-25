package io.micronaut.management.endpoint.info.source;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $GitInfoSource$Definition extends AbstractInitializableBeanDefinition<GitInfoSource> implements BeanFactory<GitInfoSource> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      GitInfoSource.class,
      "<init>",
      new Argument[]{
         Argument.of(ResourceResolver.class, "resourceResolver"),
         Argument.of(
            String.class,
            "gitPropertiesPath",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Value", AnnotationUtil.mapOf("value", "${endpoints.info.git.location:git.properties}")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Value", AnnotationUtil.mapOf("value", "${endpoints.info.git.location:git.properties}")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("io.micronaut.context.annotation.Value")),
               true,
               true
            ),
            null
         )
      },
      null,
      false
   );

   @Override
   public GitInfoSource build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      GitInfoSource var4 = new GitInfoSource(
         (ResourceResolver)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (String)super.getPropertyPlaceholderValueForConstructorArgument(var1, var2, 1, "${endpoints.info.git.location:git.properties}")
      );
      return (GitInfoSource)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      GitInfoSource var4 = (GitInfoSource)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $GitInfoSource$Definition() {
      this(GitInfoSource.class, $CONSTRUCTOR);
   }

   protected $GitInfoSource$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $GitInfoSource$Definition$Reference.$ANNOTATION_METADATA,
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
