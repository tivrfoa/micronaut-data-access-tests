package io.micronaut.management.endpoint.processors;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.management.endpoint.EndpointDefaultConfiguration;
import io.micronaut.management.endpoint.annotation.Selector;
import io.micronaut.management.endpoint.annotation.Write;
import io.micronaut.web.router.RouteBuilder;
import io.micronaut.web.router.UriRoute;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;

@Singleton
public class WriteEndpointRouteBuilder extends AbstractEndpointRouteBuilder {
   public WriteEndpointRouteBuilder(
      ApplicationContext beanContext,
      RouteBuilder.UriNamingStrategy uriNamingStrategy,
      ConversionService<?> conversionService,
      EndpointDefaultConfiguration endpointDefaultConfiguration
   ) {
      super(beanContext, uriNamingStrategy, conversionService, endpointDefaultConfiguration);
   }

   @Override
   protected Class<? extends Annotation> getSupportedAnnotation() {
      return Write.class;
   }

   @Override
   protected void registerRoute(ExecutableMethod<?, ?> method, String id, Integer port) {
      Class<?> declaringType = method.getDeclaringType();
      UriTemplate template = this.buildUriTemplate(method, id);
      String[] consumes = method.stringValues(Write.class, "consumes");
      UriRoute uriRoute = this.POST(template.toString(), declaringType, method.getMethodName(), method.getArgumentTypes())
         .consumes(MediaType.of((CharSequence[])consumes));
      if (port != null) {
         uriRoute = uriRoute.exposedPort(port);
      }

      if (LOG.isDebugEnabled()) {
         LOG.debug("Created Route to @Endpoint {}: {}", method.getDeclaringType().getName(), uriRoute);
      }

   }

   @Override
   protected boolean isPathParameter(Argument argument) {
      return argument.isDeclaredAnnotationPresent(Selector.class);
   }
}
