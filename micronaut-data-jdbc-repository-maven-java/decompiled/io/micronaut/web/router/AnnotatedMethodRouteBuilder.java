package io.micronaut.web.router;

import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.CustomHttpMethod;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Head;
import io.micronaut.http.annotation.HttpMethodMapping;
import io.micronaut.http.annotation.Options;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Trace;
import io.micronaut.http.annotation.UriMapping;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class AnnotatedMethodRouteBuilder extends DefaultRouteBuilder implements ExecutableMethodProcessor<Controller> {
   private static final MediaType[] DEFAULT_MEDIA_TYPES = new MediaType[]{MediaType.APPLICATION_JSON_TYPE};
   private final Map<Class, Consumer<AnnotatedMethodRouteBuilder.RouteDefinition>> httpMethodsHandlers = new LinkedHashMap();

   public AnnotatedMethodRouteBuilder(
      ExecutionHandleLocator executionHandleLocator, RouteBuilder.UriNamingStrategy uriNamingStrategy, ConversionService<?> conversionService
   ) {
      super(executionHandleLocator, uriNamingStrategy, conversionService);
      this.httpMethodsHandlers.put(Get.class, (Consumer)definition -> {
         BeanDefinition bean = definition.beanDefinition;
         ExecutableMethod method = definition.executableMethod;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Get.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] produces = this.resolveProduces(method);
            UriRoute route = this.GET(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method).produces(produces);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }

            if (method.booleanValue(Get.class, "headRoute").orElse(true)) {
               route = this.HEAD(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method).produces(produces);
               if (definition.port > -1) {
                  route.exposedPort(definition.port);
               }

               if (LOG.isDebugEnabled()) {
                  LOG.debug("Created Route: {}", route);
               }
            }
         }

      });
      this.httpMethodsHandlers.put(Post.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Post.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] consumes = this.resolveConsumes(method);
            MediaType[] produces = this.resolveProduces(method);
            UriRoute route = this.POST(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            route = route.consumes(consumes).produces(produces);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(CustomHttpMethod.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(CustomHttpMethod.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] consumes = this.resolveConsumes(method);
            MediaType[] produces = this.resolveProduces(method);
            String methodName = (String)method.stringValue(CustomHttpMethod.class, "method").get();
            UriRoute route = this.buildBeanRoute(methodName, HttpMethod.CUSTOM, this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            route = route.consumes(consumes).produces(produces);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(Put.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Put.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] consumes = this.resolveConsumes(method);
            MediaType[] produces = this.resolveProduces(method);
            UriRoute route = this.PUT(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            route = route.consumes(consumes).produces(produces);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(Patch.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Patch.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] consumes = this.resolveConsumes(method);
            MediaType[] produces = this.resolveProduces(method);
            UriRoute route = this.PATCH(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            route = route.consumes(consumes).produces(produces);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(Delete.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Delete.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] consumes = this.resolveConsumes(method);
            MediaType[] produces = this.resolveProduces(method);
            UriRoute route = this.DELETE(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            route = route.consumes(consumes).produces(produces);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(Head.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Head.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            UriRoute route = this.HEAD(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(Options.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Options.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] consumes = this.resolveConsumes(method);
            MediaType[] produces = this.resolveProduces(method);
            UriRoute route = this.OPTIONS(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            route = route.consumes(consumes).produces(produces);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(Trace.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         Set<String> uris = CollectionUtils.setOf(method.stringValues(Trace.class, "uris"));
         uris.add(method.stringValue(HttpMethodMapping.class).orElse("/"));

         for(String uri : uris) {
            UriRoute route = this.TRACE(this.resolveUri(bean, uri, method, uriNamingStrategy), bean, method);
            if (definition.port > -1) {
               route.exposedPort(definition.port);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }

      });
      this.httpMethodsHandlers.put(Error.class, (Consumer)definition -> {
         ExecutableMethod method = definition.executableMethod;
         BeanDefinition bean = definition.beanDefinition;
         boolean isGlobal = method.isTrue(Error.class, "global");
         Class declaringType = bean.getBeanType();
         if (method.isPresent(Error.class, "status")) {
            Optional<HttpStatus> value = method.enumValue(Error.class, "status", HttpStatus.class);
            value.ifPresent(httpStatus -> {
               if (isGlobal) {
                  this.status(httpStatus, declaringType, method.getMethodName(), method.getArgumentTypes());
               } else {
                  this.status(declaringType, httpStatus, declaringType, method.getMethodName(), method.getArgumentTypes());
               }

            });
         } else {
            Class exceptionType = null;
            if (method.isPresent(Error.class, "value")) {
               Optional<Class> annotationValue = method.classValue(Error.class);
               if (annotationValue.isPresent() && Throwable.class.isAssignableFrom((Class)annotationValue.get())) {
                  exceptionType = (Class)annotationValue.get();
               }
            }

            if (exceptionType == null) {
               exceptionType = (Class)Arrays.stream(method.getArgumentTypes()).filter(Throwable.class::isAssignableFrom).findFirst().orElse(Throwable.class);
            }

            if (isGlobal) {
               this.error(exceptionType, declaringType, method.getMethodName(), method.getArgumentTypes());
            } else {
               this.error(declaringType, exceptionType, declaringType, method.getMethodName(), method.getArgumentTypes());
            }
         }

      });
   }

   private MediaType[] resolveConsumes(ExecutableMethod method) {
      MediaType[] consumes = MediaType.of((CharSequence[])method.stringValues(Consumes.class));
      if (ArrayUtils.isEmpty(consumes)) {
         consumes = DEFAULT_MEDIA_TYPES;
      }

      return consumes;
   }

   private MediaType[] resolveProduces(ExecutableMethod method) {
      MediaType[] produces = MediaType.of((CharSequence[])method.stringValues(Produces.class));
      if (ArrayUtils.isEmpty(produces)) {
         produces = DEFAULT_MEDIA_TYPES;
      }

      return produces;
   }

   @Override
   public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      Optional<Class<? extends Annotation>> actionAnn = method.getAnnotationTypeByStereotype(HttpMethodMapping.class);
      actionAnn.ifPresent(annotationClass -> {
         Consumer<AnnotatedMethodRouteBuilder.RouteDefinition> handler = (Consumer)this.httpMethodsHandlers.get(annotationClass);
         if (handler != null) {
            int port = beanDefinition.intValue(Controller.class, "port").orElse(-1);
            handler.accept(new AnnotatedMethodRouteBuilder.RouteDefinition(beanDefinition, method, port));
         }

      });
      if (!actionAnn.isPresent() && method.isDeclaredAnnotationPresent(UriMapping.class)) {
         Set<String> uris = CollectionUtils.setOf(method.stringValues(UriMapping.class, "uris"));
         uris.add(method.stringValue(UriMapping.class).orElse("/"));

         for(String uri : uris) {
            MediaType[] produces = MediaType.of((CharSequence[])method.stringValues(Produces.class));
            Route route = this.GET(
                  this.resolveUri(beanDefinition, uri, method, this.uriNamingStrategy),
                  method.getDeclaringType(),
                  method.getMethodName(),
                  method.getArgumentTypes()
               )
               .produces(produces);
            if (LOG.isDebugEnabled()) {
               LOG.debug("Created Route: {}", route);
            }
         }
      }

   }

   private String resolveUri(BeanDefinition bean, String value, ExecutableMethod method, RouteBuilder.UriNamingStrategy uriNamingStrategy) {
      UriTemplate rootUri = UriTemplate.of(uriNamingStrategy.resolveUri(bean));
      return StringUtils.isNotEmpty(value) ? rootUri.nest(value).toString() : rootUri.nest(uriNamingStrategy.resolveUri(method.getMethodName())).toString();
   }

   private final class RouteDefinition {
      private final BeanDefinition beanDefinition;
      private final ExecutableMethod executableMethod;
      private final int port;

      public RouteDefinition(BeanDefinition beanDefinition, ExecutableMethod executableMethod, int port) {
         this.beanDefinition = beanDefinition;
         this.executableMethod = executableMethod;
         this.port = port;
      }
   }
}
