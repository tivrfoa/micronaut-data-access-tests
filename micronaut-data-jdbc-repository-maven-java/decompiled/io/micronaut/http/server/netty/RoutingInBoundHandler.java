package io.micronaut.http.server.netty;

import io.micronaut.buffer.netty.NettyByteBufferFactory;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.Writable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.context.event.HttpRequestTerminatedEvent;
import io.micronaut.http.multipart.PartData;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.http.netty.AbstractNettyHttpRequest;
import io.micronaut.http.netty.NettyHttpResponseBuilder;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.micronaut.http.netty.stream.JsonSubscriber;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.exceptions.InternalServerException;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.server.netty.multipart.NettyCompletedFileUpload;
import io.micronaut.http.server.netty.multipart.NettyPartData;
import io.micronaut.http.server.netty.multipart.NettyStreamingFileUpload;
import io.micronaut.http.server.netty.types.NettyCustomizableResponseTypeHandler;
import io.micronaut.http.server.netty.types.NettyCustomizableResponseTypeHandlerRegistry;
import io.micronaut.http.server.netty.types.files.NettyStreamedFileCustomizableResponseType;
import io.micronaut.http.server.netty.types.files.NettySystemFileCustomizableResponseType;
import io.micronaut.http.server.types.files.FileCustomizableResponseType;
import io.micronaut.runtime.http.codec.TextPlainCodec;
import io.micronaut.web.router.MethodBasedRouteMatch;
import io.micronaut.web.router.RouteInfo;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRouteMatch;
import io.micronaut.web.router.exceptions.DuplicateRouteException;
import io.micronaut.web.router.resource.StaticResourceResolver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Sinks;

@Internal
@ChannelHandler.Sharable
class RoutingInBoundHandler extends SimpleChannelInboundHandler<HttpRequest<?>> {
   private static final Logger LOG = LoggerFactory.getLogger(RoutingInBoundHandler.class);
   private static final Pattern IGNORABLE_ERROR_MESSAGE = Pattern.compile("^.*(?:connection (?:reset|closed|abort|broken)|broken pipe).*$", 2);
   private static final Argument ARGUMENT_PART_DATA = Argument.of(PartData.class);
   private final Router router;
   private final StaticResourceResolver staticResourceResolver;
   private final NettyHttpServerConfiguration serverConfiguration;
   private final HttpContentProcessorResolver httpContentProcessorResolver;
   private final ErrorResponseProcessor<?> errorResponseProcessor;
   private final RequestArgumentSatisfier requestArgumentSatisfier;
   private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
   private final NettyCustomizableResponseTypeHandlerRegistry customizableResponseTypeHandlerRegistry;
   private final Supplier<ExecutorService> ioExecutorSupplier;
   private final boolean multipartEnabled;
   private ExecutorService ioExecutor;
   private final ApplicationEventPublisher<HttpRequestTerminatedEvent> terminateEventPublisher;
   private final RouteExecutor routeExecutor;

   RoutingInBoundHandler(
      NettyHttpServerConfiguration serverConfiguration,
      NettyCustomizableResponseTypeHandlerRegistry customizableResponseTypeHandlerRegistry,
      NettyEmbeddedServices embeddedServerContext,
      Supplier<ExecutorService> ioExecutor,
      HttpContentProcessorResolver httpContentProcessorResolver,
      ApplicationEventPublisher<HttpRequestTerminatedEvent> terminateEventPublisher
   ) {
      this.mediaTypeCodecRegistry = embeddedServerContext.getMediaTypeCodecRegistry();
      this.customizableResponseTypeHandlerRegistry = customizableResponseTypeHandlerRegistry;
      this.staticResourceResolver = embeddedServerContext.getStaticResourceResolver();
      this.ioExecutorSupplier = ioExecutor;
      this.router = embeddedServerContext.getRouter();
      this.requestArgumentSatisfier = embeddedServerContext.getRequestArgumentSatisfier();
      this.serverConfiguration = serverConfiguration;
      this.httpContentProcessorResolver = httpContentProcessorResolver;
      this.errorResponseProcessor = embeddedServerContext.getRouteExecutor().getErrorResponseProcessor();
      this.terminateEventPublisher = terminateEventPublisher;
      Optional<Boolean> multipartEnabled = serverConfiguration.getMultipart().getEnabled();
      this.multipartEnabled = !multipartEnabled.isPresent() || multipartEnabled.get();
      this.routeExecutor = embeddedServerContext.getRouteExecutor();
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      super.handlerRemoved(ctx);
      this.cleanupIfNecessary(ctx);
   }

   @Override
   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      super.channelInactive(ctx);
      if (ctx.channel().isWritable()) {
         ctx.flush();
      }

      this.cleanupIfNecessary(ctx);
   }

   private void cleanupIfNecessary(ChannelHandlerContext ctx) {
      NettyHttpRequest.remove(ctx);
   }

   private void cleanupRequest(ChannelHandlerContext ctx, NettyHttpRequest request) {
      try {
         request.release();
      } finally {
         if (this.terminateEventPublisher != ApplicationEventPublisher.NO_OP) {
            ctx.executor().execute(() -> {
               try {
                  this.terminateEventPublisher.publishEvent(new HttpRequestTerminatedEvent(request));
               } catch (Exception var3) {
                  if (LOG.isErrorEnabled()) {
                     LOG.error("Error publishing request terminated event: " + var3.getMessage(), var3);
                  }
               }

            });
         }

      }

   }

   @Override
   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      try {
         if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent)evt;
            IdleState state = idleStateEvent.state();
            if (state == IdleState.ALL_IDLE) {
               ctx.close();
            }
         }
      } finally {
         super.userEventTriggered(ctx, evt);
      }

   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      if (this.isIgnorable(cause)) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Swallowed an IOException caused by client connectivity: " + cause.getMessage(), cause);
         }

      } else {
         NettyHttpRequest<?> nettyHttpRequest = NettyHttpRequest.remove(ctx);
         if (nettyHttpRequest != null) {
            ServerRequestContext.set(nettyHttpRequest);
            this.filterAndEncodeResponse(ctx, nettyHttpRequest, this.routeExecutor.onError(cause, nettyHttpRequest));
         } else {
            if (!(cause instanceof SSLException) && !(cause.getCause() instanceof SSLException)) {
               if (LOG.isErrorEnabled()) {
                  LOG.error("Micronaut Server Error - No request state present. Cause: " + cause.getMessage(), cause);
               }
            } else if (LOG.isDebugEnabled()) {
               LOG.debug("Micronaut Server Error - No request state present. Cause: " + cause.getMessage(), cause);
            }

            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
         }
      }
   }

   protected void channelRead0(ChannelHandlerContext ctx, HttpRequest<?> request) {
      ctx.channel().config().setAutoRead(false);
      HttpMethod httpMethod = request.getMethod();
      String requestPath = request.getUri().getPath();
      ServerRequestContext.set(request);
      if (LOG.isDebugEnabled()) {
         LOG.debug("Request {} {}", httpMethod, request.getUri());
      }

      NettyHttpRequest nettyHttpRequest = (NettyHttpRequest)request;
      io.netty.handler.codec.http.HttpRequest nativeRequest = nettyHttpRequest.getNativeRequest();
      DecoderResult decoderResult = nativeRequest.decoderResult();
      if (decoderResult.isFailure()) {
         Throwable cause = decoderResult.cause();
         HttpStatus status = cause instanceof TooLongFrameException ? HttpStatus.REQUEST_ENTITY_TOO_LARGE : HttpStatus.BAD_REQUEST;
         this.handleStatusError(ctx, nettyHttpRequest, HttpResponse.status(status), status.getReason());
      } else {
         MediaType contentType = (MediaType)request.getContentType().orElse(null);
         String requestMethodName = request.getMethodName();
         if (!this.multipartEnabled && contentType != null && contentType.equals(MediaType.MULTIPART_FORM_DATA_TYPE)) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(
                  "Multipart uploads have been disabled via configuration. Rejected request for URI {}, method {}, and content type {}",
                  request.getUri(),
                  requestMethodName,
                  contentType
               );
            }

            this.handleStatusError(
               ctx, nettyHttpRequest, HttpResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE), "Content Type [" + contentType + "] not allowed"
            );
         } else {
            UriRouteMatch<Object, Object> routeMatch = null;
            List<UriRouteMatch<Object, Object>> uriRoutes = this.router.findAllClosest(request);
            if (uriRoutes.size() > 1) {
               throw new DuplicateRouteException(requestPath, uriRoutes);
            } else {
               if (uriRoutes.size() == 1) {
                  routeMatch = (UriRouteMatch)uriRoutes.get(0);
                  this.setRouteAttributes(request, routeMatch);
               }

               if (routeMatch == null && request.getMethod().equals(HttpMethod.OPTIONS)) {
                  List<UriRouteMatch<Object, Object>> anyUriRoutes = (List)this.router
                     .findAny(request.getUri().toString(), request)
                     .collect(Collectors.toList());
                  if (!anyUriRoutes.isEmpty()) {
                     this.setRouteAttributes(request, (UriRouteMatch<Object, Object>)anyUriRoutes.get(0));
                     request.setAttribute(
                        HttpAttributes.AVAILABLE_HTTP_METHODS, anyUriRoutes.stream().map(UriRouteMatch::getHttpMethod).collect(Collectors.toList())
                     );
                  }
               }

               if (routeMatch == null) {
                  Optional<? extends FileCustomizableResponseType> optionalFile = this.matchFile(requestPath);
                  if (optionalFile.isPresent()) {
                     this.filterAndEncodeResponse(ctx, nettyHttpRequest, Flux.just(HttpResponse.ok(optionalFile.get())));
                  } else {
                     if (LOG.isDebugEnabled()) {
                        LOG.debug("No matching route: {} {}", httpMethod, request.getUri());
                     }

                     List<UriRouteMatch<?, ?>> anyMatchingRoutes = (List)this.router.findAny(request.getUri().toString(), request).collect(Collectors.toList());
                     Collection<MediaType> acceptedTypes = request.accept();
                     boolean hasAcceptHeader = CollectionUtils.isNotEmpty(acceptedTypes);
                     Set<MediaType> acceptableContentTypes = contentType != null ? new HashSet(5) : null;
                     Set<String> allowedMethods = new HashSet(5);
                     Set<MediaType> produceableContentTypes = hasAcceptHeader ? new HashSet(5) : null;

                     for(UriRouteMatch<?, ?> anyRoute : anyMatchingRoutes) {
                        String routeMethod = anyRoute.getRoute().getHttpMethodName();
                        if (!requestMethodName.equals(routeMethod)) {
                           allowedMethods.add(routeMethod);
                        }

                        if (contentType != null && !anyRoute.doesConsume(contentType)) {
                           acceptableContentTypes.addAll(anyRoute.getRoute().getConsumes());
                        }

                        if (hasAcceptHeader && !anyRoute.doesProduce(acceptedTypes)) {
                           produceableContentTypes.addAll(anyRoute.getRoute().getProduces());
                        }
                     }

                     if (CollectionUtils.isNotEmpty(acceptableContentTypes)) {
                        if (LOG.isDebugEnabled()) {
                           LOG.debug("Content type not allowed for URI {}, method {}, and content type {}", request.getUri(), requestMethodName, contentType);
                        }

                        this.handleStatusError(
                           ctx,
                           nettyHttpRequest,
                           HttpResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
                           "Content Type [" + contentType + "] not allowed. Allowed types: " + acceptableContentTypes
                        );
                     } else if (CollectionUtils.isNotEmpty(produceableContentTypes)) {
                        if (LOG.isDebugEnabled()) {
                           LOG.debug("Content type not allowed for URI {}, method {}, and content type {}", request.getUri(), requestMethodName, contentType);
                        }

                        this.handleStatusError(
                           ctx,
                           nettyHttpRequest,
                           HttpResponse.status(HttpStatus.NOT_ACCEPTABLE),
                           "Specified Accept Types " + acceptedTypes + " not supported. Supported types: " + produceableContentTypes
                        );
                     } else if (!allowedMethods.isEmpty()) {
                        if (LOG.isDebugEnabled()) {
                           LOG.debug("Method not allowed for URI {} and method {}", request.getUri(), requestMethodName);
                        }

                        this.handleStatusError(
                           ctx,
                           nettyHttpRequest,
                           HttpResponse.notAllowedGeneric(allowedMethods),
                           "Method [" + requestMethodName + "] not allowed for URI [" + request.getUri() + "]. Allowed methods: " + allowedMethods
                        );
                     } else {
                        this.handleStatusError(ctx, nettyHttpRequest, HttpResponse.status(HttpStatus.NOT_FOUND), "Page Not Found");
                     }
                  }
               } else {
                  if (LOG.isTraceEnabled()) {
                     if (routeMatch instanceof MethodBasedRouteMatch) {
                        LOG.trace("Matched route {} - {} to controller {}", requestMethodName, requestPath, routeMatch.getDeclaringType());
                     } else {
                        LOG.trace("Matched route {} - {}", requestMethodName, requestPath);
                     }
                  }

                  if (routeMatch.isWebSocketRoute()) {
                     this.handleStatusError(ctx, nettyHttpRequest, HttpResponse.status(HttpStatus.BAD_REQUEST), "Not a WebSocket request");
                  } else {
                     this.handleRouteMatch(routeMatch, nettyHttpRequest, ctx);
                  }

               }
            }
         }
      }
   }

   private void setRouteAttributes(HttpRequest<?> request, UriRouteMatch<Object, Object> route) {
      request.setAttribute(HttpAttributes.ROUTE, route.getRoute());
      request.setAttribute(HttpAttributes.ROUTE_MATCH, route);
      request.setAttribute(HttpAttributes.ROUTE_INFO, route);
      request.setAttribute(HttpAttributes.URI_TEMPLATE, route.getRoute().getUriMatchTemplate().toString());
   }

   private void handleStatusError(ChannelHandlerContext ctx, NettyHttpRequest<?> nettyHttpRequest, MutableHttpResponse<?> defaultResponse, String message) {
      Optional<RouteMatch<Object>> statusRoute = this.router.findStatusRoute(defaultResponse.status(), nettyHttpRequest);
      if (statusRoute.isPresent()) {
         RouteMatch<Object> routeMatch = (RouteMatch)statusRoute.get();
         this.handleRouteMatch(routeMatch, nettyHttpRequest, ctx);
      } else {
         if (nettyHttpRequest.getMethod() != HttpMethod.HEAD) {
            defaultResponse = this.errorResponseProcessor
               .processResponse(ErrorContext.builder(nettyHttpRequest).errorMessage(message).build(), defaultResponse);
            if (!defaultResponse.getContentType().isPresent()) {
               defaultResponse = defaultResponse.contentType(MediaType.APPLICATION_JSON_TYPE);
            }
         }

         this.filterAndEncodeResponse(ctx, nettyHttpRequest, Publishers.just(defaultResponse));
      }

   }

   private void filterAndEncodeResponse(ChannelHandlerContext channelContext, NettyHttpRequest<?> request, Publisher<MutableHttpResponse<?>> responsePublisher) {
      AtomicReference<HttpRequest<?>> requestReference = new AtomicReference(request);
      Flux.from(this.routeExecutor.filterPublisher(requestReference, responsePublisher))
         .contextWrite(ctx -> ctx.put("micronaut.http.server.request", request))
         .subscribe(new Subscriber<MutableHttpResponse<?>>() {
            Subscription subscription;
            AtomicBoolean empty = new AtomicBoolean();
   
            @Override
            public void onSubscribe(Subscription s) {
               this.subscription = s;
               s.request(1L);
            }
   
            public void onNext(MutableHttpResponse<?> response) {
               this.empty.set(false);
               RoutingInBoundHandler.this.encodeHttpResponse(channelContext, request, response, null, response.body());
               this.subscription.request(1L);
            }
   
            @Override
            public void onError(Throwable t) {
               this.empty.set(false);
               MutableHttpResponse<?> response = RoutingInBoundHandler.this.routeExecutor.createDefaultErrorResponse(request, t);
               RoutingInBoundHandler.this.encodeHttpResponse(channelContext, request, response, null, response.body());
            }
   
            @Override
            public void onComplete() {
               if (this.empty.get()) {
                  channelContext.read();
               }
   
            }
         });
   }

   private Optional<? extends FileCustomizableResponseType> matchFile(String path) {
      Optional<URL> optionalUrl = this.staticResourceResolver.resolve(path);
      if (optionalUrl.isPresent()) {
         try {
            URL url = (URL)optionalUrl.get();
            if (url.getProtocol().equals("file")) {
               File file = Paths.get(url.toURI()).toFile();
               if (file.exists() && !file.isDirectory() && file.canRead()) {
                  return Optional.of(new NettySystemFileCustomizableResponseType(file));
               }
            }

            return Optional.of(new NettyStreamedFileCustomizableResponseType(url));
         } catch (URISyntaxException var5) {
         }
      }

      return Optional.empty();
   }

   private void handleRouteMatch(RouteMatch<?> originalRoute, NettyHttpRequest<?> request, ChannelHandlerContext context) {
      final RouteMatch<?> route = this.requestArgumentSatisfier.fulfillArgumentRequirements(originalRoute, request, false);
      Optional<Argument<?>> bodyArgument = route.getBodyArgument().filter(argument -> argument.getAnnotationMetadata().hasAnnotation(Body.class));
      io.netty.handler.codec.http.HttpRequest nativeRequest = request.getNativeRequest();
      Flux<RouteMatch<?>> routeMatchPublisher;
      if (route.isExecutable()
         || !HttpMethod.permitsRequestBody(request.getMethod())
         || !(nativeRequest instanceof StreamedHttpRequest)
         || bodyArgument.isPresent() && route.isSatisfied(((Argument)bodyArgument.get()).getName())) {
         context.read();
         routeMatchPublisher = Flux.just(route);
      } else {
         routeMatchPublisher = Mono.<RouteMatch<?>>create(
               emitter -> this.httpContentProcessorResolver.resolve(request, route).subscribe(this.buildSubscriber(request, route, emitter))
            )
            .flux();
      }

      Flux<MutableHttpResponse<?>> routeResponse = this.routeExecutor.executeRoute(request, true, routeMatchPublisher);
      routeResponse.contextWrite(ctx -> ctx.put("micronaut.http.server.request", request))
         .subscribe(
            new CompletionAwareSubscriber<HttpResponse<?>>() {
               @Override
               protected void doOnSubscribe(Subscription subscription) {
                  subscription.request(1L);
               }
      
               protected void doOnNext(HttpResponse<?> message) {
                  RoutingInBoundHandler.this.encodeHttpResponse(
                     context, request, RoutingInBoundHandler.this.toMutableResponse(message), route.getBodyType(), message.body()
                  );
                  this.subscription.request(1L);
               }
      
               @Override
               protected void doOnError(Throwable throwable) {
                  MutableHttpResponse<?> defaultErrorResponse = RoutingInBoundHandler.this.routeExecutor.createDefaultErrorResponse(request, throwable);
                  RoutingInBoundHandler.this.encodeHttpResponse(context, request, defaultErrorResponse, route.getBodyType(), defaultErrorResponse.body());
               }
      
               @Override
               protected void doOnComplete() {
               }
            }
         );
   }

   private Subscriber<Object> buildSubscriber(NettyHttpRequest<?> request, RouteMatch<?> finalRoute, MonoSink<RouteMatch<?>> emitter) {
      boolean isFormData = request.isFormOrMultipartData();
      return isFormData
         ? new CompletionAwareSubscriber<Object>() {
            final boolean alwaysAddContent = request.isFormData();
            RouteMatch<?> routeMatch = finalRoute;
            final AtomicBoolean executed = new AtomicBoolean(false);
            final AtomicLong pressureRequested = new AtomicLong(0L);
            final ConcurrentHashMap<String, Sinks.Many<Object>> subjectsByDataName = new ConcurrentHashMap();
            final Collection<Sinks.Many<Object>> downstreamSubscribers = Collections.synchronizedList(new ArrayList());
            final ConcurrentHashMap<IdentityWrapper, HttpDataReference> dataReferences = new ConcurrentHashMap();
            final ConversionService conversionService = ConversionService.SHARED;
            Subscription s;
            final LongConsumer onRequest = num -> this.pressureRequested.updateAndGet(p -> {
                  long newVal = p - num;
                  if (newVal < 0L) {
                     this.s.request(num - p);
                     return 0L;
                  } else {
                     return newVal;
                  }
               });
   
            Flux processFlowable(Sinks.Many<Object> many, HttpDataReference dataReference, boolean controlsFlow) {
               Flux flux = many.asFlux();
               if (controlsFlow) {
                  flux = flux.doOnRequest(this.onRequest);
               }
   
               return flux.doAfterTerminate(() -> {
                  if (controlsFlow) {
                     dataReference.destroy();
                  }
   
               });
            }
   
            @Override
            protected void doOnSubscribe(Subscription subscription) {
               this.s = subscription;
               subscription.request(1L);
            }
   
            @Override
            protected void doOnNext(Object message) {
               try {
                  this.doOnNext0(message);
               } finally {
                  ReferenceCountUtil.release(message);
               }
   
            }
   
            private void doOnNext0(Object message) {
               if (!request.destroyed) {
                  boolean executed = this.executed.get();
                  if (!(message instanceof ByteBufHolder)) {
                     request.setBody(message);
                     this.s.request(1L);
                  } else if (message instanceof HttpData) {
                     HttpData data = (HttpData)message;
                     if (RoutingInBoundHandler.LOG.isTraceEnabled()) {
                        RoutingInBoundHandler.LOG.trace("Received HTTP Data for request [{}]: {}", request, message);
                     }
   
                     String name = data.getName();
                     Optional<Argument<?>> requiredInput = this.routeMatch.getRequiredInput(name);
                     if (!requiredInput.isPresent()) {
                        request.addContent(data);
                        this.s.request(1L);
                     } else {
                        Argument<?> argument = (Argument)requiredInput.get();
                        boolean isPublisher = Publishers.isConvertibleToPublisher(argument.getType());
                        boolean chunkedProcessing = false;
                        Supplier<Object> value;
                        if (isPublisher) {
                           HttpDataReference dataReference = (HttpDataReference)this.dataReferences
                              .computeIfAbsent(new IdentityWrapper(data), key -> new HttpDataReference(data));
                           Argument typeVariable;
                           if (StreamingFileUpload.class.isAssignableFrom(argument.getType())) {
                              typeVariable = RoutingInBoundHandler.ARGUMENT_PART_DATA;
                           } else {
                              typeVariable = (Argument)argument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
                           }
   
                           Class typeVariableType = typeVariable.getType();
                           Sinks.Many<Object> namedSubject = (Sinks.Many)this.subjectsByDataName
                              .computeIfAbsent(name, key -> this.makeDownstreamUnicastProcessor());
                           chunkedProcessing = PartData.class.equals(typeVariableType)
                              || Publishers.isConvertibleToPublisher(typeVariableType)
                              || ClassUtils.isJavaLangType(typeVariableType);
                           if (Publishers.isConvertibleToPublisher(typeVariableType)) {
                              boolean streamingFileUpload = StreamingFileUpload.class.isAssignableFrom(typeVariableType);
                              if (streamingFileUpload) {
                                 typeVariable = RoutingInBoundHandler.ARGUMENT_PART_DATA;
                              } else {
                                 typeVariable = (Argument)typeVariable.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
                              }
   
                              dataReference.subject
                                 .getAndUpdate(
                                    subjectx -> {
                                       if (subjectx != null) {
                                          return subjectx;
                                       } else {
                                          Sinks.Many<Object> childSubject = this.makeDownstreamUnicastProcessor();
                                          Flux flowable = this.processFlowable(childSubject, dataReference, true);
                                          if (streamingFileUpload && data instanceof FileUpload) {
                                             namedSubject.tryEmitNext(
                                                new NettyStreamingFileUpload(
                                                   (FileUpload)data,
                                                   RoutingInBoundHandler.this.serverConfiguration.getMultipart(),
                                                   RoutingInBoundHandler.this.getIoExecutor(),
                                                   flowable
                                                )
                                             );
                                          } else {
                                             namedSubject.tryEmitNext(flowable);
                                          }
         
                                          return childSubject;
                                       }
                                    }
                                 );
                           }
   
                           Sinks.Many<Object> ds = (Sinks.Many)dataReference.subject.get();
                           Sinks.Many<Object> subject;
                           if (ds != null) {
                              subject = ds;
                           } else {
                              subject = namedSubject;
                           }
   
                           Object part = data;
                           if (chunkedProcessing) {
                              HttpDataReference.Component component;
                              try {
                                 component = dataReference.addComponent();
                                 if (component == null) {
                                    this.s.request(1L);
                                    return;
                                 }
                              } catch (IOException var19) {
                                 subject.tryEmitError(var19);
                                 this.s.cancel();
                                 return;
                              }
   
                              part = new NettyPartData(dataReference, component);
                           }
   
                           if (data instanceof FileUpload && StreamingFileUpload.class.isAssignableFrom(argument.getType())) {
                              dataReference.upload
                                 .getAndUpdate(
                                    upload -> (StreamingFileUpload)(upload == null
                                          ? new NettyStreamingFileUpload(
                                             (FileUpload)data,
                                             RoutingInBoundHandler.this.serverConfiguration.getMultipart(),
                                             RoutingInBoundHandler.this.getIoExecutor(),
                                             this.processFlowable(subject, dataReference, true)
                                          )
                                          : upload)
                                 );
                           }
   
                           Optional<?> converted = this.conversionService.convert(part, typeVariable);
                           converted.ifPresent(subject::tryEmitNext);
                           if (data.isCompleted() && chunkedProcessing) {
                              subject.tryEmitComplete();
                           }
   
                           value = () -> {
                              StreamingFileUpload upload = (StreamingFileUpload)dataReference.upload.get();
                              return upload != null ? upload : this.processFlowable(namedSubject, dataReference, dataReference.subject.get() == null);
                           };
                        } else {
                           if (data instanceof Attribute && !data.isCompleted()) {
                              request.addContent(data);
                              this.s.request(1L);
                              return;
                           }
   
                           value = () -> data.refCnt() > 0 ? data : null;
                        }
   
                        if (!executed) {
                           String argumentName = argument.getName();
                           if (!this.routeMatch.isSatisfied(argumentName)) {
                              Object fulfillParamter = value.get();
                              this.routeMatch = this.routeMatch.fulfill(Collections.singletonMap(argumentName, fulfillParamter));
                              if (!this.alwaysAddContent && fulfillParamter instanceof ByteBufHolder) {
                                 request.addContent((ByteBufHolder)fulfillParamter);
                              }
                           }
   
                           if (isPublisher && chunkedProcessing) {
                              this.pressureRequested.incrementAndGet();
                           }
   
                           if (this.routeMatch.isExecutable() || message instanceof LastHttpContent) {
                              this.executeRoute();
                              executed = true;
                           }
                        }
   
                        if (this.alwaysAddContent && !request.destroyed) {
                           request.addContent(data);
                        }
   
                        if (!executed || !chunkedProcessing) {
                           this.s.request(1L);
                        }
                     }
                  } else {
                     request.addContent((ByteBufHolder)message);
                     this.s.request(1L);
                  }
   
               }
            }
   
            @Override
            protected void doOnError(Throwable t) {
               this.s.cancel();
   
               for(Object toDiscard : this.routeMatch.getVariableValues().values()) {
                  if (toDiscard instanceof ReferenceCounted) {
                     ((ReferenceCounted)toDiscard).release();
                  }
   
                  if (toDiscard instanceof io.netty.util.ReferenceCounted) {
                     ((io.netty.util.ReferenceCounted)toDiscard).release();
                  }
   
                  if (toDiscard instanceof NettyCompletedFileUpload) {
                     ((NettyCompletedFileUpload)toDiscard).discard();
                  }
               }
   
               for(Sinks.Many<Object> subject : this.downstreamSubscribers) {
                  subject.tryEmitError(t);
               }
   
               emitter.error(t);
            }
   
            @Override
            protected void doOnComplete() {
               for(Sinks.Many<Object> subject : this.downstreamSubscribers) {
                  subject.tryEmitComplete();
               }
   
               this.executeRoute();
            }
   
            private Sinks.Many<Object> makeDownstreamUnicastProcessor() {
               Sinks.Many<Object> processor = Sinks.many().unicast().onBackpressureBuffer();
               this.downstreamSubscribers.add(processor);
               return processor;
            }
   
            private void executeRoute() {
               if (this.executed.compareAndSet(false, true)) {
                  emitter.success(this.routeMatch);
               }
   
            }
         }
         : new CompletionAwareSubscriber<Object>() {
            private Subscription s;
            private RouteMatch<?> routeMatch = finalRoute;
            private AtomicBoolean executed = new AtomicBoolean(false);
   
            @Override
            protected void doOnSubscribe(Subscription subscription) {
               this.s = subscription;
               subscription.request(1L);
            }
   
            @Override
            protected void doOnNext(Object message) {
               if (message instanceof ByteBufHolder) {
                  request.addContent((ByteBufHolder)message);
                  this.s.request(1L);
               } else {
                  request.setBody(message);
                  this.s.request(1L);
               }
   
               ReferenceCountUtil.release(message);
            }
   
            @Override
            protected void doOnError(Throwable t) {
               this.s.cancel();
               emitter.error(t);
            }
   
            @Override
            protected void doOnComplete() {
               if (this.executed.compareAndSet(false, true)) {
                  emitter.success(this.routeMatch);
               }
   
            }
         };
   }

   private ExecutorService getIoExecutor() {
      ExecutorService executor = this.ioExecutor;
      if (executor == null) {
         synchronized(this) {
            executor = this.ioExecutor;
            if (executor == null) {
               executor = (ExecutorService)this.ioExecutorSupplier.get();
               this.ioExecutor = executor;
            }
         }
      }

      return executor;
   }

   private void encodeHttpResponse(
      ChannelHandlerContext context, NettyHttpRequest<?> nettyRequest, MutableHttpResponse<?> response, @Nullable Argument<Object> bodyType, Object body
   ) {
      boolean isNotHead = nettyRequest.getMethod() != HttpMethod.HEAD;
      if (isNotHead) {
         if (body instanceof Writable) {
            this.getIoExecutor()
               .execute(
                  () -> {
                     ByteBuf byteBuf = context.alloc().ioBuffer(128);
                     ByteBufOutputStream outputStream = new ByteBufOutputStream(byteBuf);
      
                     try {
                        Writable writable = (Writable)body;
                        writable.writeTo(outputStream, nettyRequest.getCharacterEncoding());
                        response.body(byteBuf);
                        if (!response.getContentType().isPresent()) {
                           response.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class)
                              .ifPresent(routeInfo -> response.contentType(this.routeExecutor.resolveDefaultResponseContentType(nettyRequest, routeInfo)));
                        }
      
                        this.writeFinalNettyResponse(response, nettyRequest, context);
                     } catch (IOException var9) {
                        MutableHttpResponse<?> errorResponse = this.routeExecutor.createDefaultErrorResponse(nettyRequest, var9);
                        this.writeFinalNettyResponse(errorResponse, nettyRequest, context);
                     }
      
                  }
               );
         } else if (body instanceof Publisher) {
            response.body(null);
            DelegateStreamedHttpResponse streamedResponse = new DelegateStreamedHttpResponse(
               this.toNettyResponse(response), this.mapToHttpContent(nettyRequest, response, body, context)
            );
            nettyRequest.prepareHttp2ResponseIfNecessary(streamedResponse);
            context.writeAndFlush(streamedResponse);
            context.read();
         } else {
            this.encodeResponseBody(context, nettyRequest, response, bodyType, body);
            this.writeFinalNettyResponse(response, nettyRequest, context);
         }
      } else {
         response.body(null);
         this.writeFinalNettyResponse(response, nettyRequest, context);
      }

   }

   private Flux<HttpContent> mapToHttpContent(NettyHttpRequest<?> request, MutableHttpResponse<?> response, Object body, ChannelHandlerContext context) {
      RouteInfo<?> routeInfo = (RouteInfo)response.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class).orElse(null);
      boolean hasRouteInfo = routeInfo != null;
      MediaType mediaType = (MediaType)response.getContentType().orElse(null);
      if (mediaType == null && hasRouteInfo) {
         mediaType = this.routeExecutor.resolveDefaultResponseContentType(request, routeInfo);
      }

      boolean isJson = mediaType != null && mediaType.getExtension().equals("json") && this.isJsonFormattable(hasRouteInfo ? routeInfo.getBodyType() : null);
      NettyByteBufferFactory byteBufferFactory = new NettyByteBufferFactory(context.alloc());
      Flux<Object> bodyPublisher = Flux.from(Publishers.convertPublisher(body, Publisher.class));
      MediaType finalMediaType = mediaType;
      Flux<HttpContent> httpContentPublisher = bodyPublisher.map(
         message -> {
            HttpContent httpContent;
            if (message instanceof ByteBuf) {
               httpContent = new DefaultHttpContent((ByteBuf)message);
            } else if (message instanceof ByteBuffer) {
               ByteBuffer<?> byteBuffer = (ByteBuffer)message;
               Object nativeBuffer = byteBuffer.asNativeBuffer();
               if (nativeBuffer instanceof ByteBuf) {
                  httpContent = new DefaultHttpContent((ByteBuf)nativeBuffer);
               } else {
                  httpContent = new DefaultHttpContent(Unpooled.copiedBuffer(byteBuffer.asNioBuffer()));
               }
            } else if (message instanceof byte[]) {
               httpContent = new DefaultHttpContent(Unpooled.copiedBuffer((byte[])message));
            } else if (message instanceof HttpContent) {
               httpContent = (HttpContent)message;
            } else {
               MediaTypeCodec var10x = (MediaTypeCodec)this.mediaTypeCodecRegistry
                  .findCodec(finalMediaType, message.getClass())
                  .orElse(new TextPlainCodec(this.serverConfiguration.getDefaultCharset()));
               if (LOG.isTraceEnabled()) {
                  LOG.trace("Encoding emitted response object [{}] using codec: {}", message, var10x);
               }
   
               ByteBuffer var11x;
               if (hasRouteInfo) {
                  Argument<Object> bodyType = routeInfo.getBodyType();
                  if (bodyType.isInstance(message)) {
                     var11x = var10x.encode(bodyType, message, byteBufferFactory);
                  } else {
                     var11x = var10x.encode(message, byteBufferFactory);
                  }
               } else {
                  var11x = var10x.encode(message, byteBufferFactory);
               }
   
               httpContent = new DefaultHttpContent((ByteBuf)var11x.asNativeBuffer());
            }
   
            return httpContent;
         }
      );
      if (isJson) {
         httpContentPublisher = JsonSubscriber.lift(httpContentPublisher);
      }

      return httpContentPublisher.contextWrite(reactorContext -> reactorContext.put("micronaut.http.server.request", request))
         .doOnNext(httpContent -> context.read())
         .doAfterTerminate(() -> this.cleanupRequest(context, request));
   }

   private boolean isJsonFormattable(Argument<?> argument) {
      if (argument == null) {
         return false;
      } else {
         Class<?> javaType = argument.getType();
         if (Publishers.isConvertibleToPublisher(javaType)) {
            javaType = ((Argument)argument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT)).getType();
         }

         return javaType != byte[].class && !ByteBuffer.class.isAssignableFrom(javaType) && !ByteBuf.class.isAssignableFrom(javaType);
      }
   }

   private void encodeResponseBody(
      ChannelHandlerContext context, HttpRequest<?> request, MutableHttpResponse<?> message, @Nullable Argument<Object> bodyType, Object body
   ) {
      if (body != null) {
         Optional<NettyCustomizableResponseTypeHandler> typeHandler = this.customizableResponseTypeHandlerRegistry.findTypeHandler(body.getClass());
         if (typeHandler.isPresent()) {
            NettyCustomizableResponseTypeHandler th = (NettyCustomizableResponseTypeHandler)typeHandler.get();
            this.setBodyContent(message, new RoutingInBoundHandler.NettyCustomizableResponseTypeHandlerInvoker(th, body));
         } else {
            MediaType mediaType = (MediaType)message.getContentType().orElse(null);
            if (mediaType == null) {
               mediaType = (MediaType)message.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class)
                  .map(routeInfo -> this.routeExecutor.resolveDefaultResponseContentType(request, routeInfo))
                  .orElse(MediaType.APPLICATION_JSON_TYPE);
               message.contentType(mediaType);
            }

            if (body instanceof CharSequence) {
               ByteBuf byteBuf = Unpooled.wrappedBuffer(body.toString().getBytes(message.getCharacterEncoding()));
               this.setResponseBody(message, byteBuf);
            } else if (body instanceof byte[]) {
               ByteBuf byteBuf = Unpooled.wrappedBuffer((byte[])body);
               this.setResponseBody(message, byteBuf);
            } else if (body instanceof ByteBuffer) {
               ByteBuffer<?> byteBuffer = (ByteBuffer)body;
               Object nativeBuffer = byteBuffer.asNativeBuffer();
               if (nativeBuffer instanceof ByteBuf) {
                  this.setResponseBody(message, (ByteBuf)nativeBuffer);
               } else if (nativeBuffer instanceof java.nio.ByteBuffer) {
                  ByteBuf byteBuf = Unpooled.wrappedBuffer((java.nio.ByteBuffer)nativeBuffer);
                  this.setResponseBody(message, byteBuf);
               }
            } else if (body instanceof ByteBuf) {
               this.setResponseBody(message, (ByteBuf)body);
            } else {
               Optional<MediaTypeCodec> registeredCodec = this.mediaTypeCodecRegistry.findCodec(mediaType, body.getClass());
               if (registeredCodec.isPresent()) {
                  MediaTypeCodec codec = (MediaTypeCodec)registeredCodec.get();
                  this.encodeBodyWithCodec(message, bodyType, body, codec, context, request);
               } else {
                  MediaTypeCodec defaultCodec = new TextPlainCodec(this.serverConfiguration.getDefaultCharset());
                  this.encodeBodyWithCodec(message, bodyType, body, defaultCodec, context, request);
               }
            }
         }

      }
   }

   private void writeFinalNettyResponse(MutableHttpResponse<?> message, HttpRequest<?> request, ChannelHandlerContext context) {
      HttpStatus httpStatus = message.status();
      io.micronaut.http.HttpVersion httpVersion = request.getHttpVersion();
      boolean isHttp2 = httpVersion == io.micronaut.http.HttpVersion.HTTP_2_0;
      boolean decodeError = request instanceof NettyHttpRequest && ((NettyHttpRequest)request).getNativeRequest().decoderResult().isFailure();
      Object body = message.body();
      if (body instanceof RoutingInBoundHandler.NettyCustomizableResponseTypeHandlerInvoker) {
         if (!isHttp2 && !message.getHeaders().contains("Connection")) {
            if (decodeError || httpStatus.getCode() >= 500 && !this.serverConfiguration.isKeepAliveOnServerError()) {
               message.getHeaders().set("Connection", HttpHeaderValues.CLOSE);
            } else {
               message.getHeaders().set("Connection", HttpHeaderValues.KEEP_ALIVE);
            }
         }

         RoutingInBoundHandler.NettyCustomizableResponseTypeHandlerInvoker handler = (RoutingInBoundHandler.NettyCustomizableResponseTypeHandlerInvoker)body;
         message.body(null);
         handler.invoke(request, message, context);
      } else {
         final io.netty.handler.codec.http.HttpResponse nettyResponse = NettyHttpResponseBuilder.toHttpResponse(message);
         HttpHeaders nettyHeaders = nettyResponse.headers();
         if (!isHttp2 && !nettyHeaders.contains(HttpHeaderNames.CONNECTION)) {
            boolean expectKeepAlive = nettyResponse.protocolVersion().isKeepAliveDefault() || request.getHeaders().isKeepAlive();
            if (decodeError || !expectKeepAlive && httpStatus.getCode() >= 500 && !this.serverConfiguration.isKeepAliveOnServerError()) {
               nettyHeaders.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            } else {
               nettyHeaders.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
         }

         if (!nettyHeaders.contains(HttpHeaderNames.CONTENT_LENGTH) && !nettyHeaders.contains(HttpHeaderNames.TRANSFER_ENCODING)) {
            nettyHeaders.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
         }

         NettyHttpRequest<?> nettyHttpRequest = (NettyHttpRequest)request;
         if (isHttp2) {
            this.addHttp2StreamHeader(request, nettyResponse);
         }

         io.netty.handler.codec.http.HttpRequest nativeRequest = nettyHttpRequest.getNativeRequest();
         final GenericFutureListener<Future<? super Void>> requestCompletor = future -> {
            try {
               if (!future.isSuccess()) {
                  Throwable throwable = future.cause();
                  if (!(throwable instanceof ClosedChannelException)) {
                     if (throwable instanceof Http2Exception.StreamException) {
                        Http2Exception.StreamException se = (Http2Exception.StreamException)throwable;
                        if (se.error() == Http2Error.STREAM_CLOSED) {
                           return;
                        }
                     }

                     if (LOG.isErrorEnabled()) {
                        LOG.error("Error writing final response: " + throwable.getMessage(), throwable);
                     }

                  }
               }
            } finally {
               this.cleanupRequest(context, nettyHttpRequest);
               context.read();
            }
         };
         if (nativeRequest instanceof StreamedHttpRequest && !((StreamedHttpRequest)nativeRequest).isConsumed()) {
            StreamedHttpRequest streamedHttpRequest = (StreamedHttpRequest)nativeRequest;
            streamedHttpRequest.subscribe(new Subscriber<HttpContent>() {
               private Subscription streamSub;

               @Override
               public void onSubscribe(Subscription s) {
                  this.streamSub = s;
                  s.request(1L);
               }

               public void onNext(HttpContent httpContent) {
                  httpContent.release();
                  this.streamSub.request(1L);
               }

               @Override
               public void onError(Throwable t) {
                  RoutingInBoundHandler.this.syncWriteAndFlushNettyResponse(context, request, nettyResponse, requestCompletor);
               }

               @Override
               public void onComplete() {
                  RoutingInBoundHandler.this.syncWriteAndFlushNettyResponse(context, request, nettyResponse, requestCompletor);
               }
            });
         } else {
            this.syncWriteAndFlushNettyResponse(context, request, nettyResponse, requestCompletor);
         }
      }

   }

   private void syncWriteAndFlushNettyResponse(
      ChannelHandlerContext context,
      HttpRequest<?> request,
      io.netty.handler.codec.http.HttpResponse nettyResponse,
      GenericFutureListener<Future<? super Void>> requestCompletor
   ) {
      context.writeAndFlush(nettyResponse).addListener(requestCompletor);
      if (LOG.isDebugEnabled()) {
         LOG.debug("Response {} - {} {}", nettyResponse.status().code(), request.getMethodName(), request.getUri());
      }

   }

   private void addHttp2StreamHeader(HttpRequest<?> request, io.netty.handler.codec.http.HttpResponse nettyResponse) {
      String streamId = request.getHeaders().get(AbstractNettyHttpRequest.STREAM_ID);
      if (streamId != null) {
         nettyResponse.headers().set(AbstractNettyHttpRequest.STREAM_ID, streamId);
      }

   }

   @NonNull
   private io.netty.handler.codec.http.HttpResponse toNettyResponse(HttpResponse<?> message) {
      return message instanceof NettyHttpResponseBuilder
         ? ((NettyHttpResponseBuilder)message).toHttpResponse()
         : this.createNettyResponse(message).toHttpResponse();
   }

   @NonNull
   private MutableHttpResponse<?> toMutableResponse(HttpResponse<?> message) {
      return (MutableHttpResponse<?>)(message instanceof MutableHttpResponse ? (MutableHttpResponse)message : this.createNettyResponse(message));
   }

   @NonNull
   private NettyMutableHttpResponse<?> createNettyResponse(HttpResponse<?> message) {
      HttpStatus httpStatus = message.status();
      Object body = message.body();
      HttpHeaders nettyHeaders = new DefaultHttpHeaders(this.serverConfiguration.isValidateHeaders());
      message.getHeaders().forEach(nettyHeaders::set);
      return new NettyMutableHttpResponse(
         HttpVersion.HTTP_1_1,
         HttpResponseStatus.valueOf(httpStatus.getCode(), httpStatus.getReason()),
         body instanceof ByteBuf ? body : null,
         ConversionService.SHARED
      );
   }

   private MutableHttpResponse<?> encodeBodyWithCodec(
      MutableHttpResponse<?> response,
      @Nullable Argument<Object> bodyType,
      Object body,
      MediaTypeCodec codec,
      ChannelHandlerContext context,
      HttpRequest<?> request
   ) {
      try {
         ByteBuf byteBuf = this.encodeBodyAsByteBuf(bodyType, body, codec, context, request);
         this.setResponseBody(response, byteBuf);
         return response;
      } catch (LinkageError var9) {
         throw new InternalServerException("Fatal error encoding bytebuf: " + var9.getMessage(), var9);
      }
   }

   private void setResponseBody(MutableHttpResponse<?> response, ByteBuf byteBuf) {
      int len = byteBuf.readableBytes();
      MutableHttpHeaders headers = response.getHeaders();
      headers.set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(len));
      this.setBodyContent(response, byteBuf);
   }

   private MutableHttpResponse<?> setBodyContent(MutableHttpResponse<?> response, Object bodyContent) {
      return response.body(bodyContent);
   }

   private ByteBuf encodeBodyAsByteBuf(
      @Nullable Argument<Object> bodyType, Object body, MediaTypeCodec codec, ChannelHandlerContext context, HttpRequest<?> request
   ) {
      ByteBuf byteBuf;
      if (body instanceof ByteBuf) {
         byteBuf = (ByteBuf)body;
      } else if (body instanceof ByteBuffer) {
         ByteBuffer byteBuffer = (ByteBuffer)body;
         Object nativeBuffer = byteBuffer.asNativeBuffer();
         if (nativeBuffer instanceof ByteBuf) {
            byteBuf = (ByteBuf)nativeBuffer;
         } else {
            byteBuf = Unpooled.wrappedBuffer(byteBuffer.asNioBuffer());
         }
      } else if (body instanceof byte[]) {
         byteBuf = Unpooled.wrappedBuffer((byte[])body);
      } else if (body instanceof Writable) {
         byteBuf = context.alloc().ioBuffer(128);
         ByteBufOutputStream outputStream = new ByteBufOutputStream(byteBuf);
         Writable writable = (Writable)body;

         try {
            writable.writeTo(outputStream, request.getCharacterEncoding());
         } catch (IOException var10) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var10.getMessage());
            }
         }
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Encoding emitted response object [{}] using codec: {}", body, codec);
         }

         ByteBuffer<ByteBuf> wrapped;
         if (bodyType != null && bodyType.isInstance(body)) {
            wrapped = codec.encode(bodyType, body, new NettyByteBufferFactory(context.alloc()));
         } else {
            wrapped = codec.encode(body, new NettyByteBufferFactory(context.alloc()));
         }

         byteBuf = wrapped.asNativeBuffer().retain();
         if (wrapped instanceof ReferenceCounted) {
            ((ReferenceCounted)wrapped).release();
         }
      }

      return byteBuf;
   }

   protected boolean isIgnorable(Throwable cause) {
      if (!(cause instanceof ClosedChannelException) && !(cause.getCause() instanceof ClosedChannelException)) {
         String message = cause.getMessage();
         return cause instanceof IOException && message != null && IGNORABLE_ERROR_MESSAGE.matcher(message).matches();
      } else {
         return true;
      }
   }

   private static class NettyCustomizableResponseTypeHandlerInvoker {
      final NettyCustomizableResponseTypeHandler handler;
      final Object body;

      NettyCustomizableResponseTypeHandlerInvoker(NettyCustomizableResponseTypeHandler handler, Object body) {
         this.handler = handler;
         this.body = body;
      }

      void invoke(HttpRequest<?> request, MutableHttpResponse response, ChannelHandlerContext channelHandlerContext) {
         this.handler.handle(this.body, request, response, channelHandlerContext);
      }
   }
}
