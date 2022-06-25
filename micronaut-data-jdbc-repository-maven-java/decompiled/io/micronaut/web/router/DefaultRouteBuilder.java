package io.micronaut.web.router;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataResolver;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.uri.UriMatchInfo;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.web.router.exceptions.RoutingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultRouteBuilder implements RouteBuilder {
   public static final RouteBuilder.UriNamingStrategy CAMEL_CASE_NAMING_STRATEGY = new RouteBuilder.UriNamingStrategy() {
   };
   protected static final Logger LOG = LoggerFactory.getLogger(DefaultRouteBuilder.class);
   static final Object NO_VALUE = new Object();
   protected final ExecutionHandleLocator executionHandleLocator;
   protected final RouteBuilder.UriNamingStrategy uriNamingStrategy;
   protected final ConversionService<?> conversionService;
   protected final Charset defaultCharset;
   private DefaultRouteBuilder.DefaultUriRoute currentParentRoute = null;
   private List<UriRoute> uriRoutes = new ArrayList();
   private List<StatusRoute> statusRoutes = new ArrayList();
   private List<ErrorRoute> errorRoutes = new ArrayList();
   private List<FilterRoute> filterRoutes = new ArrayList();
   private Set<Integer> exposedPorts = new HashSet(5);

   public DefaultRouteBuilder(ExecutionHandleLocator executionHandleLocator) {
      this(executionHandleLocator, CAMEL_CASE_NAMING_STRATEGY);
   }

   public DefaultRouteBuilder(ExecutionHandleLocator executionHandleLocator, RouteBuilder.UriNamingStrategy uriNamingStrategy) {
      this(executionHandleLocator, uriNamingStrategy, ConversionService.SHARED);
   }

   public DefaultRouteBuilder(
      ExecutionHandleLocator executionHandleLocator, RouteBuilder.UriNamingStrategy uriNamingStrategy, ConversionService<?> conversionService
   ) {
      this.executionHandleLocator = executionHandleLocator;
      this.uriNamingStrategy = uriNamingStrategy;
      this.conversionService = conversionService;
      if (executionHandleLocator instanceof ApplicationContext) {
         ApplicationContext applicationContext = (ApplicationContext)executionHandleLocator;
         Environment environment = applicationContext.getEnvironment();
         this.defaultCharset = environment.get("micronaut.application.default-charset", Charset.class, StandardCharsets.UTF_8);
      } else {
         this.defaultCharset = StandardCharsets.UTF_8;
      }

   }

   @Override
   public Set<Integer> getExposedPorts() {
      return this.exposedPorts;
   }

   @Override
   public List<FilterRoute> getFilterRoutes() {
      return this.filterRoutes;
   }

   @Override
   public FilterRoute addFilter(String pathPattern, Supplier<HttpFilter> filter) {
      DefaultFilterRoute route = new DefaultFilterRoute(pathPattern, filter, (AnnotationMetadataResolver)this.executionHandleLocator);
      this.filterRoutes.add(route);
      return route;
   }

   @Override
   public FilterRoute addFilter(String pathPattern, BeanLocator beanLocator, BeanDefinition<? extends HttpFilter> beanDefinition) {
      DefaultFilterRoute route = new BeanDefinitionFilterRoute(pathPattern, beanLocator, beanDefinition);
      this.filterRoutes.add(route);
      return route;
   }

   @Override
   public List<StatusRoute> getStatusRoutes() {
      return Collections.unmodifiableList(this.statusRoutes);
   }

   @Override
   public List<ErrorRoute> getErrorRoutes() {
      return Collections.unmodifiableList(this.errorRoutes);
   }

   @Override
   public List<UriRoute> getUriRoutes() {
      return Collections.unmodifiableList(this.uriRoutes);
   }

   @Override
   public RouteBuilder.UriNamingStrategy getUriNamingStrategy() {
      return this.uriNamingStrategy;
   }

   @Override
   public ResourceRoute resources(Class cls) {
      return new DefaultRouteBuilder.DefaultResourceRoute(cls);
   }

   @Override
   public ResourceRoute single(Class cls) {
      return new DefaultRouteBuilder.DefaultSingleRoute(cls);
   }

   @Override
   public StatusRoute status(Class originatingClass, HttpStatus status, Class type, String method, Class[] parameterTypes) {
      Optional<MethodExecutionHandle<?, Object>> executionHandle = this.executionHandleLocator.findExecutionHandle(type, method, parameterTypes);
      MethodExecutionHandle<?, Object> executableHandle = (MethodExecutionHandle)executionHandle.orElseThrow(
         () -> new RoutingException("No such route: " + type.getName() + "." + method)
      );
      DefaultRouteBuilder.DefaultStatusRoute statusRoute = new DefaultRouteBuilder.DefaultStatusRoute(
         originatingClass, status, executableHandle, this.conversionService
      );
      this.statusRoutes.add(statusRoute);
      return statusRoute;
   }

   @Override
   public StatusRoute status(HttpStatus status, Class type, String method, Class[] parameterTypes) {
      Optional<MethodExecutionHandle<?, Object>> executionHandle = this.executionHandleLocator.findExecutionHandle(type, method, parameterTypes);
      MethodExecutionHandle<?, Object> executableHandle = (MethodExecutionHandle)executionHandle.orElseThrow(
         () -> new RoutingException("No such route: " + type.getName() + "." + method)
      );
      DefaultRouteBuilder.DefaultStatusRoute statusRoute = new DefaultRouteBuilder.DefaultStatusRoute(status, executableHandle, this.conversionService);
      this.statusRoutes.add(statusRoute);
      return statusRoute;
   }

   @Override
   public ErrorRoute error(Class originatingClass, Class<? extends Throwable> error, Class type, String method, Class[] parameterTypes) {
      Optional<MethodExecutionHandle<?, Object>> executionHandle = this.executionHandleLocator.findExecutionHandle(type, method, parameterTypes);
      MethodExecutionHandle<?, Object> executableHandle = (MethodExecutionHandle)executionHandle.orElseThrow(
         () -> new RoutingException("No such route: " + type.getName() + "." + method)
      );
      DefaultRouteBuilder.DefaultErrorRoute errorRoute = new DefaultRouteBuilder.DefaultErrorRoute(
         originatingClass, error, executableHandle, this.conversionService
      );
      this.errorRoutes.add(errorRoute);
      return errorRoute;
   }

   @Override
   public ErrorRoute error(Class<? extends Throwable> error, Class type, String method, Class[] parameterTypes) {
      Optional<MethodExecutionHandle<?, Object>> executionHandle = this.executionHandleLocator.findExecutionHandle(type, method, parameterTypes);
      MethodExecutionHandle<?, Object> executableHandle = (MethodExecutionHandle)executionHandle.orElseThrow(
         () -> new RoutingException("No such route: " + type.getName() + "." + method)
      );
      DefaultRouteBuilder.DefaultErrorRoute errorRoute = new DefaultRouteBuilder.DefaultErrorRoute(error, executableHandle, this.conversionService);
      this.errorRoutes.add(errorRoute);
      return errorRoute;
   }

   @Override
   public UriRoute GET(String uri, Object target, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.GET, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute GET(String uri, Class<?> type, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.GET, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute POST(String uri, Object target, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.POST, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute POST(String uri, Class type, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.POST, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute PUT(String uri, Object target, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.PUT, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute PUT(String uri, Class type, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.PUT, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute PATCH(String uri, Object target, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.PATCH, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute PATCH(String uri, Class type, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.PATCH, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute DELETE(String uri, Object target, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.DELETE, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute DELETE(String uri, Class type, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.DELETE, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute OPTIONS(String uri, Object target, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.OPTIONS, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute OPTIONS(String uri, Class type, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.OPTIONS, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute HEAD(String uri, Object target, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.HEAD, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute HEAD(String uri, Class type, String method, Class... parameterTypes) {
      return this.buildRoute(HttpMethod.HEAD, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute TRACE(String uri, Object target, String method, Class[] parameterTypes) {
      return this.buildRoute(HttpMethod.TRACE, uri, target.getClass(), method, parameterTypes);
   }

   @Override
   public UriRoute TRACE(String uri, Class type, String method, Class[] parameterTypes) {
      return this.buildRoute(HttpMethod.TRACE, uri, type, method, parameterTypes);
   }

   @Override
   public UriRoute GET(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.GET, uri, beanDefinition, method);
   }

   @Override
   public UriRoute POST(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.POST, uri, beanDefinition, method);
   }

   @Override
   public UriRoute PUT(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.PUT, uri, beanDefinition, method);
   }

   @Override
   public UriRoute PATCH(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.PATCH, uri, beanDefinition, method);
   }

   @Override
   public UriRoute DELETE(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.DELETE, uri, beanDefinition, method);
   }

   @Override
   public UriRoute OPTIONS(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.OPTIONS, uri, beanDefinition, method);
   }

   @Override
   public UriRoute HEAD(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.HEAD, uri, beanDefinition, method);
   }

   @Override
   public UriRoute TRACE(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(HttpMethod.TRACE, uri, beanDefinition, method);
   }

   protected UriRoute buildRoute(HttpMethod httpMethod, String uri, Class<?> type, String method, Class... parameterTypes) {
      Optional<? extends MethodExecutionHandle<?, Object>> executionHandle = this.executionHandleLocator.findExecutionHandle(type, method, parameterTypes);
      MethodExecutionHandle<?, Object> executableHandle = (MethodExecutionHandle)executionHandle.orElseThrow(
         () -> new RoutingException("No such route: " + type.getName() + "." + method)
      );
      return this.buildRoute(httpMethod, uri, executableHandle);
   }

   protected UriRoute buildRoute(HttpMethod httpMethod, String uri, MethodExecutionHandle<?, Object> executableHandle) {
      return this.buildRoute(httpMethod.name(), httpMethod, uri, executableHandle);
   }

   private UriRoute buildRoute(String httpMethodName, HttpMethod httpMethod, String uri, MethodExecutionHandle<?, Object> executableHandle) {
      UriRoute route;
      if (this.currentParentRoute != null) {
         route = new DefaultRouteBuilder.DefaultUriRoute(httpMethod, this.currentParentRoute.uriMatchTemplate.nest(uri), executableHandle, httpMethodName);
         this.currentParentRoute.nestedRoutes.add((DefaultRouteBuilder.DefaultUriRoute)route);
      } else {
         route = new DefaultRouteBuilder.DefaultUriRoute(httpMethod, uri, executableHandle, httpMethodName);
      }

      this.uriRoutes.add(route);
      return route;
   }

   private UriRoute buildBeanRoute(HttpMethod httpMethod, String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.buildBeanRoute(httpMethod.name(), httpMethod, uri, beanDefinition, method);
   }

   protected UriRoute buildBeanRoute(String httpMethodName, HttpMethod httpMethod, String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      MethodExecutionHandle<?, Object> executionHandle = this.executionHandleLocator.createExecutionHandle(beanDefinition, method);
      return this.buildRoute(httpMethodName, httpMethod, uri, executionHandle);
   }

   abstract class AbstractRoute implements MethodBasedRoute, RouteInfo<Object> {
      protected final List<Predicate<HttpRequest<?>>> conditions = new ArrayList();
      protected final MethodExecutionHandle<?, ?> targetMethod;
      protected final ConversionService<?> conversionService;
      protected List<MediaType> consumesMediaTypes;
      protected List<MediaType> producesMediaTypes;
      protected String bodyArgumentName;
      protected Argument<?> bodyArgument;
      protected final Map<String, Argument> requiredInputs;
      protected final Class<?> declaringType;
      protected boolean consumesMediaTypesContainsAll;
      protected boolean producesMediaTypesContainsAll;
      protected final HttpStatus definedStatus;
      protected final boolean isWebSocketRoute;
      private final boolean isVoid;
      private final boolean suspended;
      private final boolean reactive;
      private final boolean single;
      private final boolean async;
      private final boolean specifiedSingle;
      private final boolean isAsyncOrReactive;

      AbstractRoute(MethodExecutionHandle targetMethod, ConversionService<?> conversionService, List<MediaType> mediaTypes) {
         this.targetMethod = targetMethod;
         this.conversionService = conversionService;
         this.consumesMediaTypes = mediaTypes;
         this.declaringType = targetMethod.getDeclaringType();
         this.producesMediaTypes = RouteInfo.super.getProduces();
         this.consumesMediaTypes = RouteInfo.super.getConsumes();
         this.suspended = targetMethod.getExecutableMethod().isSuspend();
         this.reactive = RouteInfo.super.isReactive();
         this.async = RouteInfo.super.isAsync();
         this.single = RouteInfo.super.isSingleResult();
         this.isVoid = RouteInfo.super.isVoid();
         this.specifiedSingle = RouteInfo.super.isSpecifiedSingle();
         this.isAsyncOrReactive = RouteInfo.super.isAsyncOrReactive();

         for(Argument argument : targetMethod.getArguments()) {
            if (argument.getAnnotationMetadata().hasAnnotation(Body.class)) {
               this.bodyArgument = argument;
            }
         }

         Argument[] requiredArguments = targetMethod.getArguments();
         if (requiredArguments.length > 0) {
            Map<String, Argument> requiredInputs = new LinkedHashMap(requiredArguments.length);

            for(Argument requiredArgument : requiredArguments) {
               String inputName = this.resolveInputName(requiredArgument);
               requiredInputs.put(inputName, requiredArgument);
            }

            this.requiredInputs = Collections.unmodifiableMap(requiredInputs);
         } else {
            this.requiredInputs = Collections.emptyMap();
         }

         this.setConsumesMediaTypesContainsAll();
         this.setProducesMediaTypesContainsAll();
         this.definedStatus = (HttpStatus)targetMethod.enumValue(Status.class, HttpStatus.class).orElse(null);
         this.isWebSocketRoute = targetMethod.hasAnnotation("io.micronaut.websocket.annotation.OnMessage");
      }

      @Override
      public Class<?> getDeclaringType() {
         return this.declaringType;
      }

      private void setConsumesMediaTypesContainsAll() {
         this.consumesMediaTypesContainsAll = this.consumesMediaTypes == null
            || this.consumesMediaTypes.isEmpty()
            || this.consumesMediaTypes.contains(MediaType.ALL_TYPE);
      }

      private void setProducesMediaTypesContainsAll() {
         this.producesMediaTypesContainsAll = this.producesMediaTypes == null
            || this.producesMediaTypes.isEmpty()
            || this.producesMediaTypes.contains(MediaType.ALL_TYPE);
      }

      @NonNull
      protected String resolveInputName(@NonNull Argument argument) {
         String inputName = (String)argument.getAnnotationMetadata().stringValue(Bindable.NAME).orElse(null);
         if (StringUtils.isEmpty(inputName)) {
            inputName = argument.getName();
         }

         return inputName;
      }

      @NonNull
      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return this.targetMethod.getAnnotationMetadata();
      }

      @Override
      public ReturnType<?> getReturnType() {
         return this.targetMethod.getReturnType();
      }

      @Override
      public boolean isSuspended() {
         return this.suspended;
      }

      @Override
      public boolean isReactive() {
         return this.reactive;
      }

      @Override
      public boolean isSingleResult() {
         return this.single;
      }

      @Override
      public boolean isSpecifiedSingle() {
         return this.specifiedSingle;
      }

      @Override
      public boolean isAsync() {
         return this.async;
      }

      @Override
      public boolean isAsyncOrReactive() {
         return this.isAsyncOrReactive;
      }

      @Override
      public boolean isVoid() {
         return this.isVoid;
      }

      @Override
      public Route consumes(MediaType... mediaTypes) {
         if (mediaTypes != null) {
            this.consumesMediaTypes = Collections.unmodifiableList(Arrays.asList(mediaTypes));
            this.setConsumesMediaTypesContainsAll();
         }

         return this;
      }

      @Override
      public List<MediaType> getConsumes() {
         return this.consumesMediaTypes;
      }

      @Override
      public Route consumesAll() {
         this.consumesMediaTypes = Collections.emptyList();
         this.setConsumesMediaTypesContainsAll();
         return this;
      }

      @Override
      public Route where(Predicate<HttpRequest<?>> condition) {
         if (condition != null) {
            this.conditions.add(condition);
         }

         return this;
      }

      @Override
      public Route body(String argument) {
         this.bodyArgumentName = argument;
         return this;
      }

      @Override
      public Route body(Argument<?> argument) {
         this.bodyArgument = argument;
         return this;
      }

      @Override
      public Route produces(MediaType... mediaType) {
         if (mediaType != null) {
            this.producesMediaTypes = Collections.unmodifiableList(Arrays.asList(mediaType));
            this.setProducesMediaTypesContainsAll();
         }

         return this;
      }

      @Override
      public List<MediaType> getProduces() {
         return this.producesMediaTypes;
      }

      @Override
      public MethodExecutionHandle getTargetMethod() {
         return this.targetMethod;
      }

      protected boolean permitsRequestBody() {
         return true;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof DefaultRouteBuilder.AbstractRoute)) {
            return false;
         } else {
            DefaultRouteBuilder.AbstractRoute that = (DefaultRouteBuilder.AbstractRoute)o;
            return Objects.equals(this.consumesMediaTypes, that.consumesMediaTypes) && Objects.equals(this.producesMediaTypes, that.producesMediaTypes);
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.consumesMediaTypes, this.producesMediaTypes});
      }
   }

   class DefaultErrorRoute extends DefaultRouteBuilder.AbstractRoute implements ErrorRoute {
      private final Class<? extends Throwable> error;
      private final Class originatingClass;

      public DefaultErrorRoute(Class<? extends Throwable> error, MethodExecutionHandle targetMethod, ConversionService<?> conversionService) {
         this(null, error, targetMethod, conversionService);
      }

      public DefaultErrorRoute(
         Class originatingClass, Class<? extends Throwable> error, MethodExecutionHandle targetMethod, ConversionService<?> conversionService
      ) {
         super(targetMethod, conversionService, Collections.emptyList());
         this.originatingClass = originatingClass;
         this.error = error;
      }

      @Nullable
      @Override
      public Class<?> originatingType() {
         return this.originatingClass;
      }

      @Override
      public Class<? extends Throwable> exceptionType() {
         return this.error;
      }

      @Override
      public <T> Optional<RouteMatch<T>> match(Class originatingClass, Throwable exception) {
         return originatingClass == this.originatingClass && this.error.isInstance(exception)
            ? Optional.of(new ErrorRouteMatch(exception, this, this.conversionService))
            : Optional.empty();
      }

      @Override
      public <T> Optional<RouteMatch<T>> match(Throwable exception) {
         return this.originatingClass == null && this.error.isInstance(exception)
            ? Optional.of(new ErrorRouteMatch(exception, this, this.conversionService))
            : Optional.empty();
      }

      @Override
      public ErrorRoute consumes(MediaType... mediaType) {
         return (ErrorRoute)super.consumes(mediaType);
      }

      @Override
      public ErrorRoute produces(MediaType... mediaType) {
         return (ErrorRoute)super.produces(mediaType);
      }

      @Override
      public Route consumesAll() {
         super.consumesAll();
         return this;
      }

      @Override
      public ErrorRoute nest(Runnable nested) {
         return this;
      }

      @Override
      public ErrorRoute where(Predicate<HttpRequest<?>> condition) {
         return (ErrorRoute)super.where(condition);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o == null || this.getClass() != o.getClass()) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            DefaultRouteBuilder.DefaultErrorRoute that = (DefaultRouteBuilder.DefaultErrorRoute)o;
            return this.error.equals(that.error) && Objects.equals(this.originatingClass, that.originatingClass);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(new Object[]{super.hashCode(), this.error, this.originatingClass});
      }

      public String toString() {
         StringBuilder builder = new StringBuilder();
         return builder.append(' ')
            .append(this.error.getSimpleName())
            .append(" -> ")
            .append(this.targetMethod.getDeclaringType().getSimpleName())
            .append('#')
            .append(this.targetMethod)
            .toString();
      }
   }

   class DefaultResourceRoute implements ResourceRoute {
      private final Map<HttpMethod, Route> resourceRoutes;
      private final DefaultRouteBuilder.DefaultUriRoute getRoute;

      DefaultResourceRoute(Map<HttpMethod, Route> resourceRoutes, DefaultRouteBuilder.DefaultUriRoute getRoute) {
         this.resourceRoutes = resourceRoutes;
         this.getRoute = getRoute;
      }

      DefaultResourceRoute(Class type) {
         this.resourceRoutes = new LinkedHashMap();
         Map<HttpMethod, Route> routeMap = this.resourceRoutes;
         this.getRoute = this.buildGetRoute(type, routeMap);
         this.buildRemainingRoutes(type, routeMap);
      }

      @Override
      public ResourceRoute consumes(MediaType... mediaTypes) {
         if (mediaTypes != null) {
            for(Route route : this.resourceRoutes.values()) {
               route.produces(mediaTypes);
            }
         }

         return this;
      }

      @Override
      public Route consumesAll() {
         return this.consumes(MediaType.EMPTY_ARRAY);
      }

      @Override
      public ResourceRoute nest(Runnable nested) {
         DefaultRouteBuilder.DefaultUriRoute previous = DefaultRouteBuilder.this.currentParentRoute;
         DefaultRouteBuilder.this.currentParentRoute = this.getRoute;

         try {
            nested.run();
         } finally {
            DefaultRouteBuilder.this.currentParentRoute = previous;
         }

         return this;
      }

      @Override
      public ResourceRoute where(Predicate<HttpRequest<?>> condition) {
         for(Route route : this.resourceRoutes.values()) {
            route.where(condition);
         }

         return this;
      }

      @Override
      public ResourceRoute produces(MediaType... mediaType) {
         if (mediaType != null) {
            for(Route route : this.resourceRoutes.values()) {
               route.produces(mediaType);
            }
         }

         return this;
      }

      public ResourceRoute body(String argument) {
         return this;
      }

      @Override
      public Route body(Argument<?> argument) {
         return this;
      }

      @Override
      public ResourceRoute readOnly(boolean readOnly) {
         List<HttpMethod> excluded = Arrays.asList(HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.POST, HttpMethod.PUT);
         return this.handleExclude(excluded);
      }

      @Override
      public ResourceRoute exclude(HttpMethod... methods) {
         return this.handleExclude(Arrays.asList(methods));
      }

      protected ResourceRoute newResourceRoute(Map<HttpMethod, Route> newMap, DefaultRouteBuilder.DefaultUriRoute getRoute) {
         return DefaultRouteBuilder.this.new DefaultResourceRoute(newMap, getRoute);
      }

      protected DefaultRouteBuilder.DefaultUriRoute buildGetRoute(Class type, Map<HttpMethod, Route> routeMap) {
         DefaultRouteBuilder.DefaultUriRoute getRoute = (DefaultRouteBuilder.DefaultUriRoute)DefaultRouteBuilder.this.GET(type, RouteBuilder.ID);
         routeMap.put(HttpMethod.GET, getRoute);
         return getRoute;
      }

      protected void buildRemainingRoutes(Class type, Map<HttpMethod, Route> routeMap) {
         routeMap.put(HttpMethod.GET, DefaultRouteBuilder.this.GET(type));
         routeMap.put(HttpMethod.POST, DefaultRouteBuilder.this.POST(type));
         routeMap.put(HttpMethod.DELETE, DefaultRouteBuilder.this.DELETE(type, RouteBuilder.ID));
         routeMap.put(HttpMethod.PATCH, DefaultRouteBuilder.this.PATCH(type, RouteBuilder.ID));
         routeMap.put(HttpMethod.PUT, DefaultRouteBuilder.this.PUT(type, RouteBuilder.ID));
      }

      private ResourceRoute handleExclude(List<HttpMethod> excluded) {
         Map<HttpMethod, Route> newMap = new LinkedHashMap();
         this.resourceRoutes.forEach((key, value) -> {
            if (excluded.contains(key)) {
               DefaultRouteBuilder.this.uriRoutes.remove(value);
            } else {
               newMap.put(key, value);
            }

         });
         return this.newResourceRoute(newMap, this.getRoute);
      }
   }

   class DefaultSingleRoute extends DefaultRouteBuilder.DefaultResourceRoute {
      DefaultSingleRoute(Map<HttpMethod, Route> resourceRoutes, DefaultRouteBuilder.DefaultUriRoute getRoute) {
         super(resourceRoutes, getRoute);
      }

      DefaultSingleRoute(Class type) {
         super(type);
      }

      @Override
      protected ResourceRoute newResourceRoute(Map<HttpMethod, Route> newMap, DefaultRouteBuilder.DefaultUriRoute getRoute) {
         return DefaultRouteBuilder.this.new DefaultSingleRoute(newMap, getRoute);
      }

      @Override
      protected DefaultRouteBuilder.DefaultUriRoute buildGetRoute(Class type, Map<HttpMethod, Route> routeMap) {
         DefaultRouteBuilder.DefaultUriRoute getRoute = (DefaultRouteBuilder.DefaultUriRoute)DefaultRouteBuilder.this.GET(type);
         routeMap.put(HttpMethod.GET, getRoute);
         return getRoute;
      }

      @Override
      protected void buildRemainingRoutes(Class type, Map<HttpMethod, Route> routeMap) {
         routeMap.put(HttpMethod.POST, DefaultRouteBuilder.this.POST(type));
         routeMap.put(HttpMethod.DELETE, DefaultRouteBuilder.this.DELETE(type));
         routeMap.put(HttpMethod.PATCH, DefaultRouteBuilder.this.PATCH(type));
         routeMap.put(HttpMethod.PUT, DefaultRouteBuilder.this.PUT(type));
      }
   }

   class DefaultStatusRoute extends DefaultRouteBuilder.AbstractRoute implements StatusRoute {
      private final HttpStatus status;
      private final Class originatingClass;

      public DefaultStatusRoute(HttpStatus status, MethodExecutionHandle targetMethod, ConversionService<?> conversionService) {
         this(null, status, targetMethod, conversionService);
      }

      public DefaultStatusRoute(Class originatingClass, HttpStatus status, MethodExecutionHandle targetMethod, ConversionService<?> conversionService) {
         super(targetMethod, conversionService, Collections.emptyList());
         this.originatingClass = originatingClass;
         this.status = status;
      }

      @Nullable
      @Override
      public Class<?> originatingType() {
         return this.originatingClass;
      }

      @Override
      public HttpStatus status() {
         return this.status;
      }

      @Override
      public <T> Optional<RouteMatch<T>> match(Class originatingClass, HttpStatus status) {
         return originatingClass == this.originatingClass && this.status == status
            ? Optional.of(new StatusRouteMatch(status, this, this.conversionService))
            : Optional.empty();
      }

      @Override
      public <T> Optional<RouteMatch<T>> match(HttpStatus status) {
         return this.originatingClass == null && this.status == status
            ? Optional.of(new StatusRouteMatch(status, this, this.conversionService))
            : Optional.empty();
      }

      @Override
      public StatusRoute consumes(MediaType... mediaType) {
         return this;
      }

      @Override
      public Route consumesAll() {
         return this;
      }

      @Override
      public StatusRoute nest(Runnable nested) {
         return this;
      }

      @Override
      public StatusRoute where(Predicate<HttpRequest<?>> condition) {
         return (StatusRoute)super.where(condition);
      }

      public HttpStatus getStatus() {
         return this.status;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof DefaultRouteBuilder.DefaultStatusRoute)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            DefaultRouteBuilder.DefaultStatusRoute that = (DefaultRouteBuilder.DefaultStatusRoute)o;
            return this.status == that.status && Objects.equals(this.originatingClass, that.originatingClass);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(new Object[]{super.hashCode(), this.status, this.originatingClass});
      }
   }

   class DefaultUriRoute extends DefaultRouteBuilder.AbstractRoute implements UriRoute {
      final String httpMethodName;
      final HttpMethod httpMethod;
      final UriMatchTemplate uriMatchTemplate;
      final List<DefaultRouteBuilder.DefaultUriRoute> nestedRoutes = new ArrayList(2);
      private Integer port;

      DefaultUriRoute(HttpMethod httpMethod, CharSequence uriTemplate, MethodExecutionHandle targetMethod) {
         this(httpMethod, uriTemplate, targetMethod, httpMethod.name());
      }

      DefaultUriRoute(HttpMethod httpMethod, CharSequence uriTemplate, MethodExecutionHandle targetMethod, String httpMethodName) {
         this(httpMethod, uriTemplate, MediaType.APPLICATION_JSON_TYPE, targetMethod, httpMethodName);
      }

      DefaultUriRoute(HttpMethod httpMethod, CharSequence uriTemplate, MediaType mediaType, MethodExecutionHandle targetMethod) {
         this(httpMethod, uriTemplate, mediaType, targetMethod, httpMethod.name());
      }

      DefaultUriRoute(HttpMethod httpMethod, CharSequence uriTemplate, MediaType mediaType, MethodExecutionHandle targetMethod, String httpMethodName) {
         this(httpMethod, new UriMatchTemplate(uriTemplate), Collections.singletonList(mediaType), targetMethod, httpMethodName);
      }

      DefaultUriRoute(HttpMethod httpMethod, UriMatchTemplate uriTemplate, MethodExecutionHandle targetMethod) {
         this(httpMethod, uriTemplate, targetMethod, httpMethod.name());
      }

      DefaultUriRoute(HttpMethod httpMethod, UriMatchTemplate uriTemplate, MethodExecutionHandle targetMethod, String httpMethodName) {
         this(httpMethod, uriTemplate, Collections.singletonList(MediaType.APPLICATION_JSON_TYPE), targetMethod, httpMethodName);
      }

      DefaultUriRoute(HttpMethod httpMethod, UriMatchTemplate uriTemplate, List<MediaType> mediaTypes, MethodExecutionHandle targetMethod) {
         this(httpMethod, uriTemplate, mediaTypes, targetMethod, httpMethod.name());
      }

      DefaultUriRoute(
         HttpMethod httpMethod, UriMatchTemplate uriTemplate, List<MediaType> mediaTypes, MethodExecutionHandle targetMethod, String httpMethodName
      ) {
         super(targetMethod, ConversionService.SHARED, mediaTypes);
         this.httpMethod = httpMethod;
         this.uriMatchTemplate = uriTemplate;
         this.httpMethodName = httpMethodName;
      }

      @Override
      public String getHttpMethodName() {
         return this.httpMethodName;
      }

      public String toString() {
         StringBuilder builder = new StringBuilder(this.getHttpMethodName());
         return builder.append(' ')
            .append(this.uriMatchTemplate)
            .append(" -> ")
            .append(this.targetMethod.getDeclaringType().getSimpleName())
            .append('#')
            .append(this.targetMethod.getName())
            .append(" (")
            .append(String.join(",", this.consumesMediaTypes))
            .append(")")
            .toString();
      }

      @Override
      public HttpMethod getHttpMethod() {
         return this.httpMethod;
      }

      @Override
      public UriRoute body(String argument) {
         return (UriRoute)super.body(argument);
      }

      @Override
      public UriRoute exposedPort(int port) {
         this.port = port;
         this.where(httpRequest -> httpRequest.getServerAddress().getPort() == port);
         DefaultRouteBuilder.this.exposedPorts.add(port);
         return this;
      }

      @Override
      public Integer getPort() {
         return this.port;
      }

      @Override
      public UriRoute consumes(MediaType... mediaTypes) {
         return (UriRoute)super.consumes(mediaTypes);
      }

      @Override
      public UriRoute produces(MediaType... mediaType) {
         return (UriRoute)super.produces(mediaType);
      }

      @Override
      public UriRoute consumesAll() {
         return (UriRoute)super.consumesAll();
      }

      @Override
      public UriRoute nest(Runnable nested) {
         DefaultRouteBuilder.DefaultUriRoute previous = DefaultRouteBuilder.this.currentParentRoute;
         DefaultRouteBuilder.this.currentParentRoute = this;

         try {
            nested.run();
         } finally {
            DefaultRouteBuilder.this.currentParentRoute = previous;
         }

         return this;
      }

      @Override
      public UriRoute where(Predicate<HttpRequest<?>> condition) {
         return (UriRoute)super.where(condition);
      }

      @Override
      public Optional<UriRouteMatch> match(String uri) {
         Optional<UriMatchInfo> matchInfo = this.uriMatchTemplate.match(uri);
         return matchInfo.map(info -> new DefaultUriRouteMatch(info, this, DefaultRouteBuilder.this.defaultCharset, this.conversionService));
      }

      @Override
      public UriMatchTemplate getUriMatchTemplate() {
         return this.uriMatchTemplate;
      }

      public int compareTo(UriRoute o) {
         return this.uriMatchTemplate.compareTo(o.getUriMatchTemplate());
      }

      @Override
      protected boolean permitsRequestBody() {
         return HttpMethod.permitsRequestBody(this.httpMethod);
      }
   }
}
