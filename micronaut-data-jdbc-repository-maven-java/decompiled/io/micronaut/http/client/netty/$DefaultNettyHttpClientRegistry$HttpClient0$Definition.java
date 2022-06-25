package io.micronaut.http.client.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultNettyHttpClientRegistry$HttpClient0$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpClient>
   implements BeanFactory<DefaultHttpClient>,
   ParametrizedBeanFactory<DefaultHttpClient> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultNettyHttpClientRegistry.class,
      "httpClient",
      new Argument[]{
         Argument.of(
            InjectionPoint.class,
            "injectionPoint",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(Object.class, "T")
         ),
         Argument.of(
            LoadBalancer.class,
            "loadBalancer",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         ),
         Argument.of(
            HttpClientConfiguration.class,
            "configuration",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         ),
         Argument.of(BeanContext.class, "beanContext")
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.BootstrapContextCompatible",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Primary",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary")),
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf("io.micronaut.context.LifeCycle", new Argument[]{Argument.of(HttpClient.class, "T")});

   @Override
   public DefaultHttpClient doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      Object var5 = ((DefaultBeanContext)var2).getBean(var1, DefaultNettyHttpClientRegistry.class, null);
      var1.markDependentAsFactory();
      DefaultHttpClient var6 = ((DefaultNettyHttpClientRegistry)var5)
         .httpClient(
            (InjectionPoint<?>)super.getBeanForConstructorArgument(var1, var2, 0, null),
            (LoadBalancer)var4.get("loadBalancer"),
            (HttpClientConfiguration)var4.get("configuration"),
            var2
         );
      return (DefaultHttpClient)this.injectBean(var1, var2, var6);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHttpClient var4 = (DefaultHttpClient)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultNettyHttpClientRegistry$HttpClient0$Definition() {
      this(DefaultHttpClient.class, $CONSTRUCTOR);
   }

   protected $DefaultNettyHttpClientRegistry$HttpClient0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultNettyHttpClientRegistry$HttpClient0$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.empty(),
         false,
         false,
         false,
         false,
         true,
         false,
         false,
         false
      );
   }
}
