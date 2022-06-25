package io.micronaut.management.endpoint.processors;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.management.endpoint.EndpointDefaultConfiguration;
import io.micronaut.management.endpoint.annotation.Delete;
import io.micronaut.web.router.RouteBuilder;
import io.micronaut.web.router.UriRoute;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;

@Singleton
public class DeleteEndpointRouteBuilder extends AbstractEndpointRouteBuilder {
   public DeleteEndpointRouteBuilder(
      ApplicationContext beanContext,
      RouteBuilder.UriNamingStrategy uriNamingStrategy,
      ConversionService<?> conversionService,
      EndpointDefaultConfiguration endpointDefaultConfiguration
   ) {
      super(beanContext, uriNamingStrategy, conversionService, endpointDefaultConfiguration);
   }

   @Override
   protected Class<? extends Annotation> getSupportedAnnotation() {
      return Delete.class;
   }

   @Override
   protected void registerRoute(ExecutableMethod<?, ?> method, String id, Integer port) {
      Class<?> declaringType = method.getDeclaringType();
      UriTemplate template = this.buildUriTemplate(method, id);
      UriRoute uriRoute = this.DELETE(template.toString(), declaringType, method.getMethodName(), method.getArgumentTypes());
      if (port != null) {
         uriRoute = uriRoute.exposedPort(port);
      }

      if (LOG.isDebugEnabled()) {
         LOG.debug("Created Route to @Endpoint {}: {}", method.getDeclaringType().getName(), uriRoute);
      }

   }
}
