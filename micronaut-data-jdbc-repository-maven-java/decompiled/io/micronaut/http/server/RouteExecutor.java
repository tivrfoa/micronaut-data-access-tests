package io.micronaut.http.server;

import io.micronaut.context.BeanContext;
import io.micronaut.context.exceptions.BeanCreationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.KotlinUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.bind.binders.ContinuationArgumentBinder;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodReference;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.inject.util.KotlinExecutableMethodUtils;
import io.micronaut.scheduling.executor.ExecutorSelector;
import io.micronaut.web.router.MethodBasedRouteMatch;
import io.micronaut.web.router.RouteInfo;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Singleton
public final class RouteExecutor {
   private static final Logger LOG = LoggerFactory.getLogger(RouteExecutor.class);
   private static final Pattern IGNORABLE_ERROR_MESSAGE = Pattern.compile("^.*(?:connection (?:reset|closed|abort|broken)|broken pipe).*$", 2);
   private final Router router;
   private final BeanContext beanContext;
   private final RequestArgumentSatisfier requestArgumentSatisfier;
   private final HttpServerConfiguration serverConfiguration;
   private final ErrorResponseProcessor<?> errorResponseProcessor;
   private final ExecutorSelector executorSelector;
   private final Optional<CoroutineHelper> coroutineHelper;

   public RouteExecutor(
      Router router,
      BeanContext beanContext,
      RequestArgumentSatisfier requestArgumentSatisfier,
      HttpServerConfiguration serverConfiguration,
      ErrorResponseProcessor<?> errorResponseProcessor,
      ExecutorSelector executorSelector
   ) {
      this.router = router;
      this.beanContext = beanContext;
      this.requestArgumentSatisfier = requestArgumentSatisfier;
      this.serverConfiguration = serverConfiguration;
      this.errorResponseProcessor = errorResponseProcessor;
      this.executorSelector = executorSelector;
      this.coroutineHelper = beanContext.findBean(CoroutineHelper.class);
   }

   @NonNull
   public Router getRouter() {
      return this.router;
   }

   @Internal
   @NonNull
   public RequestArgumentSatisfier getRequestArgumentSatisfier() {
      return this.requestArgumentSatisfier;
   }

   @NonNull
   public ErrorResponseProcessor<?> getErrorResponseProcessor() {
      return this.errorResponseProcessor;
   }

   @NonNull
   public ExecutorSelector getExecutorSelector() {
      return this.executorSelector;
   }

   public Optional<CoroutineHelper> getCoroutineHelper() {
      return this.coroutineHelper;
   }

   public Flux<MutableHttpResponse<?>> onError(Throwable t, HttpRequest<?> httpRequest) {
      Class declaringType = (Class)httpRequest.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class).map(RouteInfo::getDeclaringType).orElse(null);
      Throwable cause;
      if ((t instanceof CompletionException || t instanceof ExecutionException) && t.getCause() != null) {
         cause = t.getCause();
      } else {
         cause = t;
      }

      RouteMatch<?> errorRoute = this.findErrorRoute(cause, declaringType, httpRequest);
      if (errorRoute != null) {
         if (this.serverConfiguration.isLogHandledExceptions()) {
            this.logException(cause);
         }

         try {
            AtomicReference<HttpRequest<?>> requestReference = new AtomicReference(httpRequest);
            return this.buildRouteResponsePublisher(requestReference, Flux.just(errorRoute))
               .doOnNext(response -> response.setAttribute(HttpAttributes.EXCEPTION, cause))
               .onErrorResume(throwable -> this.createDefaultErrorResponsePublisher((HttpRequest<?>)requestReference.get(), throwable));
         } catch (Throwable var12) {
            return this.createDefaultErrorResponsePublisher(httpRequest, var12).flux();
         }
      } else {
         Optional<BeanDefinition<ExceptionHandler>> optionalDefinition = this.beanContext
            .findBeanDefinition(ExceptionHandler.class, Qualifiers.byTypeArgumentsClosest(cause.getClass(), Object.class));
         if (optionalDefinition.isPresent()) {
            final BeanDefinition<ExceptionHandler> handlerDefinition = (BeanDefinition)optionalDefinition.get();
            Optional<ExecutableMethod<ExceptionHandler, Object>> optionalMethod = handlerDefinition.findPossibleMethods("handle").findFirst();
            RouteInfo<Object> routeInfo;
            if (optionalMethod.isPresent()) {
               routeInfo = new ExecutableRouteInfo((ExecutableMethod)optionalMethod.get(), true);
            } else {
               routeInfo = new RouteInfo<Object>() {
                  @Override
                  public ReturnType<?> getReturnType() {
                     return ReturnType.of(Object.class);
                  }

                  @Override
                  public Class<?> getDeclaringType() {
                     return handlerDefinition.getBeanType();
                  }

                  @Override
                  public boolean isErrorRoute() {
                     return true;
                  }

                  @Override
                  public List<MediaType> getProduces() {
                     return (List<MediaType>)MediaType.fromType(this.getDeclaringType()).map(Collections::singletonList).orElse(Collections.emptyList());
                  }
               };
            }

            Flux<MutableHttpResponse<?>> reactiveSequence = Flux.defer(() -> {
               ExceptionHandler handler = this.beanContext.getBean(handlerDefinition);

               try {
                  if (this.serverConfiguration.isLogHandledExceptions()) {
                     this.logException(cause);
                  }

                  Object result = handler.handle(httpRequest, cause);
                  return this.createResponseForBody(httpRequest, result, routeInfo);
               } catch (Throwable var7x) {
                  return this.createDefaultErrorResponsePublisher(httpRequest, var7x);
               }
            });
            ExecutorService executor = this.findExecutor(routeInfo);
            if (executor != null) {
               reactiveSequence = this.applyExecutorToPublisher(reactiveSequence, executor);
            }

            return reactiveSequence.doOnNext(response -> response.setAttribute(HttpAttributes.EXCEPTION, cause))
               .onErrorResume(throwable -> this.createDefaultErrorResponsePublisher(httpRequest, throwable));
         } else if (this.isIgnorable(cause)) {
            this.logIgnoredException(cause);
            return Flux.empty();
         } else {
            return this.createDefaultErrorResponsePublisher(httpRequest, cause).flux();
         }
      }
   }

   public MutableHttpResponse<?> createDefaultErrorResponse(HttpRequest<?> httpRequest, Throwable cause) {
      this.logException(cause);
      MutableHttpResponse<Object> response = HttpResponse.serverError();
      response.setAttribute(HttpAttributes.EXCEPTION, cause);
      response.setAttribute(HttpAttributes.ROUTE_INFO, new RouteInfo<MutableHttpResponse>() {
         @Override
         public ReturnType<MutableHttpResponse> getReturnType() {
            return ReturnType.of(MutableHttpResponse.class, Argument.OBJECT_ARGUMENT);
         }

         @Override
         public Class<?> getDeclaringType() {
            return Object.class;
         }

         @Override
         public boolean isErrorRoute() {
            return true;
         }
      });
      MutableHttpResponse<?> mutableHttpResponse = this.errorResponseProcessor
         .processResponse(ErrorContext.builder(httpRequest).cause(cause).errorMessage("Internal Server Error: " + cause.getMessage()).build(), response);
      this.applyConfiguredHeaders(mutableHttpResponse.getHeaders());
      return !mutableHttpResponse.getContentType().isPresent() && httpRequest.getMethod() != HttpMethod.HEAD
         ? mutableHttpResponse.contentType(MediaType.APPLICATION_JSON_TYPE)
         : mutableHttpResponse;
   }

   public MediaType resolveDefaultResponseContentType(HttpRequest<?> request, RouteInfo<?> finalRoute) {
      List<MediaType> producesList = finalRoute.getProduces();
      if (request != null) {
         Iterator<MediaType> i = request.accept().iterator();
         if (i.hasNext()) {
            MediaType mt = (MediaType)i.next();
            if (producesList.contains(mt)) {
               return mt;
            }
         }
      }

      Iterator<MediaType> produces = producesList.iterator();
      MediaType defaultResponseMediaType;
      if (produces.hasNext()) {
         defaultResponseMediaType = (MediaType)produces.next();
      } else {
         defaultResponseMediaType = MediaType.APPLICATION_JSON_TYPE;
      }

      return defaultResponseMediaType;
   }

   public Flux<MutableHttpResponse<?>> executeRoute(HttpRequest<?> request, boolean executeFilters, Flux<RouteMatch<?>> routePublisher) {
      AtomicReference<HttpRequest<?>> requestReference = new AtomicReference(request);
      return this.buildResultEmitter(requestReference, executeFilters, routePublisher);
   }

   public Publisher<MutableHttpResponse<?>> filterPublisher(
      AtomicReference<HttpRequest<?>> requestReference, Publisher<MutableHttpResponse<?>> upstreamResponsePublisher
   ) {
      List<HttpFilter> httpFilters = this.router.findFilters((HttpRequest<?>)requestReference.get());
      if (httpFilters.isEmpty()) {
         return upstreamResponsePublisher;
      } else {
         final List<HttpFilter> filters = new ArrayList(httpFilters);
         final AtomicInteger integer = new AtomicInteger();
         final int len = filters.size();
         final Function<MutableHttpResponse<?>, Publisher<MutableHttpResponse<?>>> handleStatusException = response -> this.handleStatusException(
               (HttpRequest<?>)requestReference.get(), response
            );
         final Function<Throwable, Publisher<MutableHttpResponse<?>>> onError = t -> this.onError(t, (HttpRequest<?>)requestReference.get());
         ServerFilterChain filterChain = new ServerFilterChain() {
            @Override
            public Publisher<MutableHttpResponse<?>> proceed(HttpRequest<?> request) {
               int pos = integer.incrementAndGet();
               if (pos > len) {
                  throw new IllegalStateException(
                     "The FilterChain.proceed(..) method should be invoked exactly once per filter execution. The method has instead been invoked multiple times by an erroneous filter definition."
                  );
               } else if (pos == len) {
                  return upstreamResponsePublisher;
               } else {
                  HttpFilter httpFilter = (HttpFilter)filters.get(pos);
                  requestReference.set(request);

                  try {
                     return Flux.from(httpFilter.doFilter(request, this)).<MutableHttpResponse<?>>flatMap(handleStatusException).onErrorResume(onError);
                  } catch (Throwable var5) {
                     return (Publisher<MutableHttpResponse<?>>)onError.apply(var5);
                  }
               }
            }
         };
         HttpFilter httpFilter = (HttpFilter)filters.get(0);
         HttpRequest<?> request = (HttpRequest)requestReference.get();

         try {
            return Flux.from(httpFilter.doFilter(request, filterChain)).<MutableHttpResponse<?>>flatMap(handleStatusException).onErrorResume(onError);
         } catch (Throwable var13) {
            return (Publisher<MutableHttpResponse<?>>)onError.apply(var13);
         }
      }
   }

   private Mono<MutableHttpResponse<?>> createDefaultErrorResponsePublisher(HttpRequest<?> httpRequest, Throwable cause) {
      return Mono.fromCallable(() -> this.createDefaultErrorResponse(httpRequest, cause));
   }

   private MutableHttpResponse<?> newNotFoundError(HttpRequest<?> request) {
      MutableHttpResponse<?> response = this.errorResponseProcessor
         .processResponse(ErrorContext.builder(request).errorMessage("Page Not Found").build(), HttpResponse.notFound());
      return !response.getContentType().isPresent() && request.getMethod() != HttpMethod.HEAD
         ? response.contentType(MediaType.APPLICATION_JSON_TYPE)
         : response;
   }

   private Mono<MutableHttpResponse<?>> createNotFoundErrorResponsePublisher(HttpRequest<?> httpRequest) {
      return Mono.fromCallable(() -> this.newNotFoundError(httpRequest));
   }

   private void logException(Throwable cause) {
      if (this.isIgnorable(cause)) {
         this.logIgnoredException(cause);
      } else if (LOG.isErrorEnabled()) {
         LOG.error("Unexpected error occurred: " + cause.getMessage(), cause);
      }

   }

   private boolean isIgnorable(Throwable cause) {
      String message = cause.getMessage();
      return cause instanceof IOException && message != null && IGNORABLE_ERROR_MESSAGE.matcher(message).matches();
   }

   private void logIgnoredException(Throwable cause) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("Swallowed an IOException caused by client connectivity: " + cause.getMessage(), cause);
      }

   }

   private RouteMatch<?> findErrorRoute(Throwable cause, Class<?> declaringType, HttpRequest<?> httpRequest) {
      RouteMatch<?> errorRoute = null;
      if (cause instanceof BeanCreationException && declaringType != null) {
         Optional<Class> rootBeanType = ((BeanCreationException)cause).getRootBeanType().map(BeanType::getBeanType);
         if (rootBeanType.isPresent() && declaringType == rootBeanType.get()) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Failed to instantiate [{}]. Skipping lookup of a local error route", declaringType.getName());
            }

            declaringType = null;
         }
      }

      if (declaringType != null) {
         errorRoute = (RouteMatch)this.router.findErrorRoute(declaringType, cause, httpRequest).orElse(null);
      }

      if (errorRoute == null) {
         errorRoute = (RouteMatch)this.router.findErrorRoute(cause, httpRequest).orElse(null);
      }

      if (errorRoute == null) {
         HttpStatus errorStatus = null;
         if (cause instanceof UnsatisfiedRouteException) {
            errorStatus = HttpStatus.BAD_REQUEST;
         } else if (cause instanceof HttpStatusException) {
            errorStatus = ((HttpStatusException)cause).getStatus();
         }

         if (errorStatus != null) {
            if (declaringType != null) {
               errorRoute = (RouteMatch)this.router.findStatusRoute(declaringType, errorStatus, httpRequest).orElse(null);
            }

            if (errorRoute == null) {
               errorRoute = (RouteMatch)this.router.findStatusRoute(errorStatus, httpRequest).orElse(null);
            }
         }
      }

      if (errorRoute != null) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Found matching exception handler for exception [{}]: {}", cause.getMessage(), errorRoute);
         }

         errorRoute = this.requestArgumentSatisfier.fulfillArgumentRequirements(errorRoute, httpRequest, false);
      }

      return errorRoute;
   }

   private Publisher<MutableHttpResponse<?>> handleStatusException(HttpRequest<?> request, MutableHttpResponse<?> response) {
      HttpStatus status = response.status();
      RouteInfo<?> routeInfo = (RouteInfo)response.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class).orElse(null);
      if (status.getCode() >= 400 && routeInfo != null && !routeInfo.isErrorRoute()) {
         RouteMatch<Object> statusRoute = this.findStatusRoute(request, status, routeInfo);
         if (statusRoute != null) {
            return this.executeRoute(request, false, Flux.just(statusRoute));
         }
      }

      return Flux.just(response);
   }

   private RouteMatch<Object> findStatusRoute(HttpRequest<?> incomingRequest, HttpStatus status, RouteInfo<?> finalRoute) {
      Class<?> declaringType = finalRoute.getDeclaringType();
      RouteMatch<Object> statusRoute = null;
      if (declaringType != null) {
         statusRoute = (RouteMatch)this.router
            .findStatusRoute(declaringType, status, incomingRequest)
            .orElseGet(() -> (RouteMatch)this.router.findStatusRoute(status, incomingRequest).orElse(null));
      }

      return statusRoute;
   }

   private ExecutorService findExecutor(RouteInfo<?> routeMatch) {
      ExecutorService executor;
      if (routeMatch instanceof MethodReference) {
         executor = (ExecutorService)this.executorSelector.select((MethodReference)routeMatch, this.serverConfiguration.getThreadSelection()).orElse(null);
      } else {
         executor = null;
      }

      return executor;
   }

   private <T> Flux<T> applyExecutorToPublisher(Publisher<T> publisher, @Nullable ExecutorService executor) {
      if (executor != null) {
         Scheduler scheduler = Schedulers.fromExecutorService(executor);
         return Flux.from(publisher).subscribeOn(scheduler).publishOn(scheduler);
      } else {
         return Flux.from(publisher);
      }
   }

   private boolean isSingle(RouteInfo<?> finalRoute, Class<?> bodyClass) {
      return finalRoute.isSpecifiedSingle()
         || finalRoute.isSingleResult() && (finalRoute.isAsync() || finalRoute.isSuspended() || Publishers.isSingle(bodyClass));
   }

   private MutableHttpResponse<?> toMutableResponse(HttpResponse<?> message) {
      MutableHttpResponse<?> mutableHttpResponse;
      if (message instanceof MutableHttpResponse) {
         mutableHttpResponse = (MutableHttpResponse)message;
      } else {
         HttpStatus httpStatus = message.status();
         mutableHttpResponse = HttpResponse.status(httpStatus, httpStatus.getReason());
         mutableHttpResponse.body(message.body());
         message.getHeaders().forEach((name, value) -> {
            for(String val : value) {
               mutableHttpResponse.header(name, val);
            }

         });
         mutableHttpResponse.getAttributes().putAll(message.getAttributes());
      }

      return mutableHttpResponse;
   }

   private Mono<MutableHttpResponse<?>> toMutableResponse(HttpRequest<?> request, RouteInfo<?> routeInfo, HttpStatus defaultHttpStatus, Object body) {
      if (body instanceof HttpResponse) {
         MutableHttpResponse<?> outgoingResponse = this.toMutableResponse((HttpResponse<?>)body);
         Argument<?> bodyArgument = (Argument)routeInfo.getReturnType().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
         return bodyArgument.isAsyncOrReactive() ? this.processPublisherBody(request, outgoingResponse, routeInfo) : Mono.just(outgoingResponse);
      } else {
         return Mono.just(this.forStatus(routeInfo, defaultHttpStatus).body(body));
      }
   }

   private Flux<MutableHttpResponse<?>> buildRouteResponsePublisher(AtomicReference<HttpRequest<?>> requestReference, Flux<RouteMatch<?>> routeMatchPublisher) {
      return routeMatchPublisher.flatMap(route -> {
         ExecutorService executor = this.findExecutor(route);
         Flux<MutableHttpResponse<?>> reactiveSequence = this.executeRoute(requestReference, route);
         if (executor != null) {
            reactiveSequence = this.applyExecutorToPublisher(reactiveSequence, executor);
         }

         return reactiveSequence;
      });
   }

   private Flux<MutableHttpResponse<?>> buildResultEmitter(
      AtomicReference<HttpRequest<?>> requestReference, boolean executeFilters, Flux<RouteMatch<?>> routeMatchPublisher
   ) {
      Publisher<MutableHttpResponse<?>> executeRoutePublisher = this.buildRouteResponsePublisher(requestReference, routeMatchPublisher)
         .<MutableHttpResponse<?>>flatMap(response -> this.handleStatusException((HttpRequest<?>)requestReference.get(), response))
         .onErrorResume(t -> this.onError(t, (HttpRequest<?>)requestReference.get()));
      if (executeFilters) {
         executeRoutePublisher = this.filterPublisher(requestReference, executeRoutePublisher);
      }

      return Flux.from(executeRoutePublisher);
   }

   private Flux<MutableHttpResponse<?>> executeRoute(AtomicReference<HttpRequest<?>> requestReference, RouteMatch<?> routeMatch) {
      return Flux.deferContextual(contextView -> {
         try {
            HttpRequest<?> httpRequest = (HttpRequest)requestReference.get();
            RouteMatch<?> finalRoute;
            if (!routeMatch.isExecutable()) {
               finalRoute = this.requestArgumentSatisfier.fulfillArgumentRequirements(routeMatch, httpRequest, true);
            } else {
               finalRoute = routeMatch;
            }

            if (finalRoute.isSuspended() && this.coroutineHelper.isPresent()) {
               ((CoroutineHelper)this.coroutineHelper.get()).setupCoroutineContext(httpRequest, contextView);
            }

            Object body = ServerRequestContext.with(httpRequest, finalRoute::execute);
            if (body instanceof Optional) {
               body = ((Optional)body).orElse(null);
            }

            return this.createResponseForBody(httpRequest, body, finalRoute);
         } catch (Throwable var7) {
            return Flux.error(var7);
         }
      });
   }

   private Flux<MutableHttpResponse<?>> createResponseForBody(HttpRequest<?> request, Object body, RouteInfo<?> routeInfo) {
      return Flux.<MutableHttpResponse<?>>defer(
            () -> {
               Mono<MutableHttpResponse<?>> outgoingResponse;
               if (body == null) {
                  if (routeInfo.isVoid()) {
                     MutableHttpResponse<Object> data = this.forStatus(routeInfo);
                     if (HttpMethod.permitsRequestBody(request.getMethod())) {
                        data.header("Content-Length", "0");
                     }
      
                     outgoingResponse = Mono.just(data);
                  } else {
                     outgoingResponse = Mono.just(this.newNotFoundError(request));
                  }
               } else {
                  HttpStatus defaultHttpStatus = routeInfo.isErrorRoute() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
                  boolean isReactive = routeInfo.isAsyncOrReactive() || Publishers.isConvertibleToPublisher(body);
                  if (isReactive) {
                     Class<?> bodyClass = body.getClass();
                     boolean isSingle = this.isSingle(routeInfo, bodyClass);
                     boolean isCompletable = !isSingle && routeInfo.isVoid() && Publishers.isCompletable(bodyClass);
                     if (!isSingle && !isCompletable) {
                        Argument<?> typeArgument = (Argument)routeInfo.getReturnType().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
                        if (HttpResponse.class.isAssignableFrom(typeArgument.getType())) {
                           Publisher<HttpResponse<?>> bodyPublisher = Publishers.convertPublisher(body, Publisher.class);
                           Flux<MutableHttpResponse<?>> response = Flux.from(bodyPublisher).map(this::toMutableResponse);
                           Argument<?> bodyArgument = (Argument)typeArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
                           if (bodyArgument.isAsyncOrReactive()) {
                              return response.flatMap(resp -> this.processPublisherBody(request, resp, routeInfo));
                           }
      
                           return response;
                        }
      
                        MutableHttpResponse<?> response = this.forStatus(routeInfo, defaultHttpStatus).body(body);
                        return this.processPublisherBody(request, response, routeInfo);
                     }
      
                     Publisher<Object> publisher = Publishers.convertPublisher(body, Publisher.class);
                     Supplier<MutableHttpResponse<?>> emptyResponse = () -> {
                        MutableHttpResponse<?> singleResponse;
                        if (!isCompletable && !routeInfo.isVoid()) {
                           singleResponse = this.newNotFoundError(request);
                        } else {
                           singleResponse = this.forStatus(routeInfo, HttpStatus.OK).header("Content-Length", "0");
                        }
      
                        return singleResponse;
                     };
                     return Flux.from(publisher)
                        .flatMap(
                           o -> {
                              if (o instanceof Optional) {
                                 Optional bodyArgumentx = (Optional)o;
                                 if (!bodyArgumentx.isPresent()) {
                                    return Flux.just(emptyResponse.get());
                                 }
            
                                 o = ((Optional)o).get();
                              }
            
                              MutableHttpResponse<?> singleResponse;
                              if (o instanceof HttpResponse) {
                                 singleResponse = this.toMutableResponse((HttpResponse<?>)o);
                                 Argument<?> var8x = (Argument)((Argument)routeInfo.getReturnType().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT))
                                    .getFirstTypeVariable()
                                    .orElse(Argument.OBJECT_ARGUMENT);
                                 if (var8x.isAsyncOrReactive()) {
                                    return this.processPublisherBody(request, singleResponse, routeInfo);
                                 }
                              } else if (o instanceof HttpStatus) {
                                 singleResponse = this.forStatus(routeInfo, (HttpStatus)o);
                              } else {
                                 singleResponse = this.forStatus(routeInfo, defaultHttpStatus).body(o);
                              }
            
                              return Flux.just(singleResponse);
                           }
                        )
                        .switchIfEmpty(Mono.fromSupplier(emptyResponse));
                  }
      
                  if (body instanceof HttpStatus) {
                     outgoingResponse = Mono.just(HttpResponse.status((HttpStatus)body));
                  } else if (routeInfo.isSuspended()) {
                     boolean isKotlinFunctionReturnTypeUnit = routeInfo instanceof MethodBasedRouteMatch
                        && KotlinExecutableMethodUtils.isKotlinFunctionReturnTypeUnit(((MethodBasedRouteMatch)routeInfo).getExecutableMethod());
                     Supplier<CompletableFuture<?>> supplier = ContinuationArgumentBinder.extractContinuationCompletableFutureSupplier(request);
                     if (KotlinUtils.isKotlinCoroutineSuspended(body)) {
                        return Mono.fromCompletionStage(supplier).<MutableHttpResponse<?>>flatMap(obj -> {
                           MutableHttpResponse responsex;
                           if (obj instanceof HttpResponse) {
                              responsex = this.toMutableResponse((HttpResponse<?>)obj);
                              Argument<?> bodyArgumentx = (Argument)routeInfo.getReturnType().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
                              if (bodyArgumentx.isAsyncOrReactive()) {
                                 return this.processPublisherBody(request, responsex, routeInfo);
                              }
                           } else {
                              responsex = this.forStatus(routeInfo, defaultHttpStatus);
                              if (!isKotlinFunctionReturnTypeUnit) {
                                 responsex = responsex.body(obj);
                              }
                           }
      
                           return Mono.just(responsex);
                        }).switchIfEmpty(this.createNotFoundErrorResponsePublisher(request));
                     }
      
                     Object suspendedBody;
                     if (isKotlinFunctionReturnTypeUnit) {
                        suspendedBody = Mono.empty();
                     } else {
                        suspendedBody = body;
                     }
      
                     outgoingResponse = this.toMutableResponse(request, routeInfo, defaultHttpStatus, suspendedBody);
                  } else {
                     outgoingResponse = this.toMutableResponse(request, routeInfo, defaultHttpStatus, body);
                  }
               }
      
               if (request != null && request.getMethod().equals(HttpMethod.HEAD)) {
                  outgoingResponse = outgoingResponse.map(r -> {
                     Object o = r.getBody().orElse(null);
                     if (o instanceof ReferenceCounted) {
                        ((ReferenceCounted)o).release();
                     }
      
                     r.body(null);
                     return r;
                  });
               }
      
               return outgoingResponse;
            }
         )
         .doOnNext(response -> {
            this.applyConfiguredHeaders(response.getHeaders());
            if (routeInfo instanceof RouteMatch) {
               response.setAttribute(HttpAttributes.ROUTE_MATCH, routeInfo);
            }
   
            response.setAttribute(HttpAttributes.ROUTE_INFO, routeInfo);
         });
   }

   private Mono<MutableHttpResponse<?>> processPublisherBody(HttpRequest<?> request, MutableHttpResponse<?> response, RouteInfo<?> routeInfo) {
      Object body = response.body();
      if (body == null) {
         return Mono.just(response);
      } else if (Publishers.isSingle(body.getClass())) {
         return Mono.from(Publishers.convertPublisher(body, Publisher.class)).map(b -> {
            response.body(b);
            return response;
         });
      } else {
         MediaType mediaType = (MediaType)response.getContentType().orElseGet(() -> this.resolveDefaultResponseContentType(request, routeInfo));
         Flux<Object> bodyPublisher = this.applyExecutorToPublisher(Publishers.convertPublisher(body, Publisher.class), this.findExecutor(routeInfo));
         return Mono.just(response.header("Transfer-Encoding", "chunked").header("Content-Type", mediaType).body(bodyPublisher));
      }
   }

   private void applyConfiguredHeaders(MutableHttpHeaders headers) {
      if (this.serverConfiguration.isDateHeader() && !headers.contains("Date")) {
         headers.date(LocalDateTime.now());
      }

      if (!headers.contains("Server")) {
         this.serverConfiguration.getServerHeader().ifPresent(header -> headers.add("Server", header));
      }

   }

   private MutableHttpResponse<Object> forStatus(RouteInfo<?> routeMatch) {
      return this.forStatus(routeMatch, HttpStatus.OK);
   }

   private MutableHttpResponse<Object> forStatus(RouteInfo<?> routeMatch, HttpStatus defaultStatus) {
      HttpStatus status = routeMatch.findStatus(defaultStatus);
      return HttpResponse.status(status);
   }
}
