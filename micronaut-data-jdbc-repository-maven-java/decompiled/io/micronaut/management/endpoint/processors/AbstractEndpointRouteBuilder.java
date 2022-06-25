package io.micronaut.management.endpoint.processors;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.LifeCycle;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.management.endpoint.EndpointDefaultConfiguration;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Selector;
import io.micronaut.web.router.DefaultRouteBuilder;
import io.micronaut.web.router.RouteBuilder;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Internal
abstract class AbstractEndpointRouteBuilder extends DefaultRouteBuilder implements ExecutableMethodProcessor<Endpoint>, LifeCycle<AbstractEndpointRouteBuilder> {
   private static final Pattern ENDPOINT_ID_PATTERN = Pattern.compile("\\w+");
   private Map<Class, Optional<String>> endpointIds = new ConcurrentHashMap();
   private final ApplicationContext beanContext;
   private final EndpointDefaultConfiguration endpointDefaultConfiguration;

   AbstractEndpointRouteBuilder(
      ApplicationContext applicationContext,
      RouteBuilder.UriNamingStrategy uriNamingStrategy,
      ConversionService<?> conversionService,
      EndpointDefaultConfiguration endpointDefaultConfiguration
   ) {
      super(applicationContext, uriNamingStrategy, conversionService);
      this.beanContext = applicationContext;
      this.endpointDefaultConfiguration = endpointDefaultConfiguration;
   }

   protected abstract Class<? extends Annotation> getSupportedAnnotation();

   protected abstract void registerRoute(ExecutableMethod<?, ?> method, String id, @Nullable Integer port);

   @NonNull
   public AbstractEndpointRouteBuilder start() {
      return this;
   }

   @NonNull
   public AbstractEndpointRouteBuilder stop() {
      this.endpointIds.clear();
      return this;
   }

   @Override
   public boolean isRunning() {
      return true;
   }

   @Override
   public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      Class<?> declaringType = method.getDeclaringType();
      if (method.hasStereotype(this.getSupportedAnnotation())) {
         Optional<String> endPointId = this.resolveActiveEndPointId(declaringType);
         Integer port = (Integer)this.endpointDefaultConfiguration.getPort().orElse(null);
         endPointId.ifPresent(id -> this.registerRoute(method, id, port));
      }

   }

   protected Optional<String> resolveActiveEndPointId(Class<?> declaringType) {
      return (Optional<String>)this.endpointIds.computeIfAbsent(declaringType, aClass -> {
         Optional<? extends BeanDefinition<?>> opt = this.beanContext.findBeanDefinition(declaringType);
         if (opt.isPresent()) {
            BeanDefinition<?> beanDefinition = (BeanDefinition)opt.get();
            if (beanDefinition.hasStereotype(Endpoint.class)) {
               String id = (String)beanDefinition.stringValue(Endpoint.class).orElse(null);
               if (id == null || !ENDPOINT_ID_PATTERN.matcher(id).matches()) {
                  id = NameUtils.hyphenate(beanDefinition.getName());
               }

               return Optional.ofNullable(id);
            }
         }

         return Optional.empty();
      });
   }

   protected UriTemplate buildUriTemplate(ExecutableMethod<?, ?> method, String id) {
      UriTemplate template = new UriTemplate(this.resolveUriByRouteId(id));

      for(Argument argument : method.getArguments()) {
         if (this.isPathParameter(argument)) {
            template = template.nest("/{" + argument.getName() + "}");
         }
      }

      return template;
   }

   String resolveUriByRouteId(String id) {
      String path = StringUtils.prependUri(this.endpointDefaultConfiguration.getPath(), id);
      if (path.charAt(0) == '/') {
         path = path.substring(1);
      }

      return this.uriNamingStrategy.resolveUri(path);
   }

   protected boolean isPathParameter(Argument argument) {
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      return annotationMetadata.hasDeclaredAnnotation(Selector.class);
   }
}
