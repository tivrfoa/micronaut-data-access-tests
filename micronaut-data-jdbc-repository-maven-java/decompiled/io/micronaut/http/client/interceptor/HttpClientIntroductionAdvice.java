package io.micronaut.http.client.interceptor;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.core.beans.BeanMap;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.convert.format.Format;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.version.annotation.Version;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.CustomHttpMethod;
import io.micronaut.http.annotation.HttpMethodMapping;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientRegistry;
import io.micronaut.http.client.ReactiveClientResultTransformer;
import io.micronaut.http.client.StreamingHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.bind.ClientArgumentRequestBinder;
import io.micronaut.http.client.bind.ClientRequestUriContext;
import io.micronaut.http.client.bind.HttpClientBinderRegistry;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.sse.Event;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.json.codec.JsonMediaTypeCodec;
import jakarta.inject.Singleton;
import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Internal
@BootstrapContextCompatible
public class HttpClientIntroductionAdvice implements MethodInterceptor<Object, Object> {
   private static final Logger LOG = LoggerFactory.getLogger(HttpClientIntroductionAdvice.class);
   private static final MediaType[] DEFAULT_ACCEPT_TYPES = new MediaType[]{MediaType.APPLICATION_JSON_TYPE};
   private final List<ReactiveClientResultTransformer> transformers;
   private final HttpClientBinderRegistry binderRegistry;
   private final JsonMediaTypeCodec jsonMediaTypeCodec;
   private final HttpClientRegistry<?> clientFactory;
   private final ConversionService<?> conversionService;

   public HttpClientIntroductionAdvice(
      HttpClientRegistry<?> clientFactory,
      JsonMediaTypeCodec jsonMediaTypeCodec,
      List<ReactiveClientResultTransformer> transformers,
      HttpClientBinderRegistry binderRegistry,
      ConversionService<?> conversionService
   ) {
      this.clientFactory = clientFactory;
      this.jsonMediaTypeCodec = jsonMediaTypeCodec;
      this.transformers = transformers != null ? transformers : Collections.emptyList();
      this.binderRegistry = binderRegistry;
      this.conversionService = conversionService;
   }

   @Nullable
   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      if (!context.hasStereotype(Client.class)) {
         throw new IllegalStateException("Client advice called from type that is not annotated with @Client: " + context);
      } else {
         AnnotationMetadata annotationMetadata = context.getAnnotationMetadata();
         final Class<?> declaringType = context.getDeclaringType();
         if (Closeable.class != declaringType && AutoCloseable.class != declaringType) {
            Optional<Class<? extends Annotation>> httpMethodMapping = context.getAnnotationTypeByStereotype(HttpMethodMapping.class);
            HttpClient httpClient = this.clientFactory.getClient(annotationMetadata);
            if (context.hasStereotype(HttpMethodMapping.class) && httpClient != null) {
               AnnotationValue<HttpMethodMapping> mapping = context.getAnnotation(HttpMethodMapping.class);
               String uri = mapping.getRequiredValue(String.class);
               if (StringUtils.isEmpty(uri)) {
                  uri = "/" + context.getMethodName();
               }

               Class<? extends Annotation> annotationType = (Class)httpMethodMapping.get();
               HttpMethod httpMethod = HttpMethod.parse(annotationType.getSimpleName().toUpperCase(Locale.ENGLISH));
               String httpMethodName = (String)context.stringValue(CustomHttpMethod.class, "method").orElse(httpMethod.name());
               MutableHttpRequest<?> request = HttpRequest.create(httpMethod, "", httpMethodName);
               UriMatchTemplate uriTemplate = UriMatchTemplate.of("");
               if (uri.length() != 1 || uri.charAt(0) != '/') {
                  uriTemplate = uriTemplate.nest(uri);
               }

               Map<String, Object> pathParams = new HashMap();
               Map<String, List<String>> queryParams = new LinkedHashMap();
               ClientRequestUriContext uriContext = new ClientRequestUriContext(uriTemplate, pathParams, queryParams);
               List<Argument> bodyArguments = new ArrayList();
               List<String> uriVariables = uriTemplate.getVariableNames();
               Map<String, MutableArgumentValue<?>> parameters = context.getParameters();
               ClientArgumentRequestBinder<Object> defaultBinder = (ctx, uriCtx, value, req) -> {
                  Argument<?> argument = ctx.getArgument();
                  if (uriCtx.getUriTemplate().getVariableNames().contains(argument.getName())) {
                     String name = (String)argument.getAnnotationMetadata().stringValue(Bindable.class).orElse(argument.getName());
                     if (argument.getAnnotationMetadata().hasStereotype(Format.class)) {
                        ConversionService.SHARED
                           .convert(value, ConversionContext.STRING.with(argument.getAnnotationMetadata()))
                           .ifPresent(v -> pathParams.put(name, v));
                     } else {
                        pathParams.put(name, value);
                     }
                  } else {
                     bodyArguments.add(ctx.getArgument());
                  }

               };
               List<Class<? extends Annotation>> methodBinderTypes = context.getAnnotationTypesByStereotype(Bindable.class);
               methodBinderTypes.addAll(context.getAnnotationTypesByStereotype(Version.class));
               if (!CollectionUtils.isEmpty(methodBinderTypes)) {
                  for(Class<? extends Annotation> binderType : methodBinderTypes) {
                     this.binderRegistry.findAnnotatedBinder(binderType).ifPresent(b -> b.bind(context, uriContext, request));
                  }
               }

               InterceptedMethod interceptedMethod = InterceptedMethod.of(context);
               Argument[] arguments = context.getArguments();
               if (arguments.length > 0) {
                  for(Argument argument : arguments) {
                     Object definedValue = this.getValue(argument, context, parameters);
                     if (definedValue != null) {
                        ClientArgumentRequestBinder<Object> binder = (ClientArgumentRequestBinder)this.binderRegistry
                           .findArgumentBinder(argument)
                           .orElse(defaultBinder);
                        ArgumentConversionContext conversionContext = ConversionContext.of(argument);
                        binder.bind(conversionContext, uriContext, definedValue, request);
                        if (conversionContext.hasErrors()) {
                           return interceptedMethod.handleException(
                              new ConversionErrorException(argument, (ConversionError)conversionContext.getLastError().get())
                           );
                        }
                     }
                  }
               }

               Object body = request.getBody().orElse(null);
               if (body == null && !bodyArguments.isEmpty()) {
                  Map<String, Object> bodyMap = new LinkedHashMap();

                  for(Argument bodyArgument : bodyArguments) {
                     String argumentName = bodyArgument.getName();
                     MutableArgumentValue<?> value = (MutableArgumentValue)parameters.get(argumentName);
                     bodyMap.put(argumentName, value.getValue());
                  }

                  body = bodyMap;
                  request.body(bodyMap);
               }

               boolean variableSatisfied = uriVariables.isEmpty() || pathParams.keySet().containsAll(uriVariables);
               if (body != null && !variableSatisfied) {
                  if (body instanceof Map) {
                     for(Entry<Object, Object> entry : ((Map)body).entrySet()) {
                        String k = entry.getKey().toString();
                        Object v = entry.getValue();
                        if (v != null) {
                           pathParams.putIfAbsent(k, v);
                        }
                     }
                  } else {
                     BeanMap<Object> beanMap = BeanMap.of(body);

                     for(Entry<String, Object> entry : beanMap.entrySet()) {
                        String k = (String)entry.getKey();
                        Object v = entry.getValue();
                        if (v != null) {
                           pathParams.putIfAbsent(k, v);
                        }
                     }
                  }
               }

               if (!HttpMethod.permitsRequestBody(httpMethod)) {
                  request.body(null);
                  body = null;
               }

               uri = uriTemplate.expand(pathParams);
               uriVariables.forEach(pathParams::remove);
               this.addParametersToQuery(pathParams, uriContext);
               request.uri(URI.create(this.appendQuery(uri, uriContext.getQueryParameters())));
               if (body != null && !request.getContentType().isPresent()) {
                  MediaType[] contentTypes = MediaType.of((CharSequence[])context.stringValues(Produces.class));
                  if (ArrayUtils.isEmpty(contentTypes)) {
                     contentTypes = DEFAULT_ACCEPT_TYPES;
                  }

                  if (ArrayUtils.isNotEmpty(contentTypes)) {
                     request.contentType(contentTypes[0]);
                  }
               }

               request.setAttribute(HttpAttributes.INVOCATION_CONTEXT, context);
               request.setAttribute(HttpAttributes.URI_TEMPLATE, this.resolveTemplate(annotationMetadata, uriTemplate.toString()));
               String serviceId = this.getClientId(annotationMetadata);
               Argument<?> errorType = (Argument)annotationMetadata.classValue(Client.class, "errorType")
                  .map(Argument::of)
                  .orElse(HttpClient.DEFAULT_ERROR_TYPE);
               request.setAttribute(HttpAttributes.SERVICE_ID, serviceId);
               Collection<MediaType> accept = request.accept();
               MediaType[] acceptTypes;
               if (accept.isEmpty()) {
                  String[] consumesMediaType = context.stringValues(Consumes.class);
                  if (ArrayUtils.isEmpty(consumesMediaType)) {
                     acceptTypes = DEFAULT_ACCEPT_TYPES;
                  } else {
                     acceptTypes = MediaType.of((CharSequence[])consumesMediaType);
                  }

                  request.accept(acceptTypes);
               } else {
                  acceptTypes = (MediaType[])accept.toArray(MediaType.EMPTY_ARRAY);
               }

               ReturnType<?> returnType = context.getReturnType();

               try {
                  Argument<?> valueType = interceptedMethod.returnTypeValue();
                  final Class<?> reactiveValueType = valueType.getType();
                  switch(interceptedMethod.resultType()) {
                     case PUBLISHER:
                        boolean isSingle = returnType.isSingleResult()
                           || returnType.isCompletable()
                           || HttpResponse.class.isAssignableFrom(reactiveValueType)
                           || HttpStatus.class == reactiveValueType;
                        Publisher<?> publisher;
                        if (!isSingle && httpClient instanceof StreamingHttpClient) {
                           publisher = this.httpClientResponseStreamingPublisher((StreamingHttpClient)httpClient, acceptTypes, request, errorType, valueType);
                        } else {
                           publisher = this.httpClientResponsePublisher(httpClient, request, returnType, errorType, valueType);
                        }

                        Object finalPublisher = interceptedMethod.handleResult(publisher);

                        for(ReactiveClientResultTransformer transformer : this.transformers) {
                           finalPublisher = transformer.transform(finalPublisher);
                        }

                        return finalPublisher;
                     case COMPLETION_STAGE:
                        Publisher<?> csPublisher = this.httpClientResponsePublisher(httpClient, request, returnType, errorType, valueType);
                        final CompletableFuture<Object> future = new CompletableFuture();
                        csPublisher.subscribe(
                           new CompletionAwareSubscriber<Object>() {
                              AtomicReference<Object> reference = new AtomicReference();
   
                              @Override
                              protected void doOnSubscribe(Subscription subscription) {
                                 subscription.request(1L);
                              }
   
                              @Override
                              protected void doOnNext(Object message) {
                                 if (Void.class != reactiveValueType) {
                                    this.reference.set(message);
                                 }
   
                              }
   
                              @Override
                              protected void doOnError(Throwable t) {
                                 if (t instanceof HttpClientResponseException) {
                                    HttpClientResponseException e = (HttpClientResponseException)t;
                                    if (e.getStatus() == HttpStatus.NOT_FOUND) {
                                       if (reactiveValueType == Optional.class) {
                                          future.complete(Optional.empty());
                                       } else if (HttpResponse.class.isAssignableFrom(reactiveValueType)) {
                                          future.complete(e.getResponse());
                                       } else {
                                          future.complete(null);
                                       }
   
                                       return;
                                    }
                                 }
   
                                 if (HttpClientIntroductionAdvice.LOG.isErrorEnabled()) {
                                    HttpClientIntroductionAdvice.LOG
                                       .error("Client [" + declaringType.getName() + "] received HTTP error response: " + t.getMessage(), t);
                                 }
   
                                 future.completeExceptionally(t);
                              }
   
                              @Override
                              protected void doOnComplete() {
                                 future.complete(this.reference.get());
                              }
                           }
                        );
                        return interceptedMethod.handleResult(future);
                     case SYNCHRONOUS:
                        Class<?> javaReturnType = returnType.getType();
                        BlockingHttpClient blockingHttpClient = httpClient.toBlocking();
                        if (Void.TYPE == javaReturnType || httpMethod == HttpMethod.HEAD) {
                           request.getHeaders().remove("Accept");
                        }

                        if (HttpResponse.class.isAssignableFrom(javaReturnType)) {
                           return this.handleBlockingCall(
                              javaReturnType,
                              () -> blockingHttpClient.exchange(
                                    request, (Argument)returnType.asArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT), errorType
                                 )
                           );
                        } else {
                           if (Void.TYPE == javaReturnType) {
                              return this.handleBlockingCall(javaReturnType, () -> blockingHttpClient.exchange(request, null, errorType));
                           }

                           return this.handleBlockingCall(javaReturnType, () -> blockingHttpClient.retrieve(request, returnType.asArgument(), errorType));
                        }
                     default:
                        return interceptedMethod.unsupported();
                  }
               } catch (Exception var39) {
                  return interceptedMethod.handleException(var39);
               }
            } else {
               return context.proceed();
            }
         } else {
            this.clientFactory.disposeClient(annotationMetadata);
            return null;
         }
      }
   }

   private Publisher httpClientResponsePublisher(
      HttpClient httpClient, MutableHttpRequest<?> request, ReturnType<?> returnType, Argument<?> errorType, Argument<?> reactiveValueArgument
   ) {
      Class<?> argumentType = reactiveValueArgument.getType();
      if (Void.class == argumentType || returnType.isVoid()) {
         request.getHeaders().remove("Accept");
         return httpClient.exchange(request, Argument.VOID, errorType);
      } else {
         return HttpResponse.class.isAssignableFrom(argumentType)
            ? httpClient.exchange(request, reactiveValueArgument, errorType)
            : httpClient.retrieve(request, reactiveValueArgument, errorType);
      }
   }

   private Publisher httpClientResponseStreamingPublisher(
      StreamingHttpClient streamingHttpClient, MediaType[] acceptTypes, MutableHttpRequest<?> request, Argument<?> errorType, Argument<?> reactiveValueArgument
   ) {
      Class<?> reactiveValueType = reactiveValueArgument.getType();
      if (Void.class == reactiveValueType) {
         request.getHeaders().remove("Accept");
      }

      if (streamingHttpClient instanceof SseClient && Arrays.asList(acceptTypes).contains(MediaType.TEXT_EVENT_STREAM_TYPE)) {
         SseClient sseClient = (SseClient)streamingHttpClient;
         return reactiveValueArgument.getType() == Event.class
            ? sseClient.eventStream(request, (Argument)reactiveValueArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT), errorType)
            : Publishers.map(sseClient.eventStream(request, reactiveValueArgument, errorType), Event::getData);
      } else if (this.isJsonParsedMediaType(acceptTypes)) {
         return streamingHttpClient.jsonStream(request, reactiveValueArgument, errorType);
      } else {
         Publisher<ByteBuffer<?>> byteBufferPublisher = streamingHttpClient.dataStream(request, errorType);
         if (reactiveValueType == ByteBuffer.class) {
            return byteBufferPublisher;
         } else if (ConversionService.SHARED.canConvert(ByteBuffer.class, reactiveValueType)) {
            return Publishers.map(byteBufferPublisher, value -> ConversionService.SHARED.convert(value, reactiveValueType).get());
         } else {
            throw new ConfigurationException(
               "Cannot create the generated HTTP client's required return type, since no TypeConverter from ByteBuffer to "
                  + reactiveValueType
                  + " is registered"
            );
         }
      }
   }

   private Object getValue(Argument argument, MethodInvocationContext<?, ?> context, Map<String, MutableArgumentValue<?>> parameters) {
      String argumentName = argument.getName();
      MutableArgumentValue<?> value = (MutableArgumentValue)parameters.get(argumentName);
      Object definedValue = value.getValue();
      if (definedValue == null) {
         definedValue = argument.getAnnotationMetadata().stringValue(Bindable.class, "defaultValue").orElse(null);
      }

      if (definedValue == null && !argument.isNullable()) {
         throw new IllegalArgumentException(
            String.format(
               "Argument [%s] is null. Null values are not allowed to be passed to client methods (%s). Add a supported Nullable annotation type if that is the desired behaviour",
               argument.getName(),
               context.getExecutableMethod().toString()
            )
         );
      } else {
         return definedValue instanceof Optional ? ((Optional)definedValue).orElse(null) : definedValue;
      }
   }

   private Object handleBlockingCall(Class returnType, Supplier<Object> supplier) {
      try {
         if (Void.TYPE == returnType) {
            supplier.get();
            return null;
         } else {
            return supplier.get();
         }
      } catch (RuntimeException var4) {
         if (var4 instanceof HttpClientResponseException && ((HttpClientResponseException)var4).getStatus() == HttpStatus.NOT_FOUND) {
            if (returnType == Optional.class) {
               return Optional.empty();
            } else {
               return HttpResponse.class.isAssignableFrom(returnType) ? ((HttpClientResponseException)var4).getResponse() : null;
            }
         } else {
            throw var4;
         }
      }
   }

   private boolean isJsonParsedMediaType(MediaType[] acceptTypes) {
      return Arrays.stream(acceptTypes)
         .anyMatch(
            mediaType -> mediaType.equals(MediaType.APPLICATION_JSON_STREAM_TYPE)
                  || mediaType.getExtension().equals("json")
                  || this.jsonMediaTypeCodec.getMediaTypes().contains(mediaType)
         );
   }

   private String resolveTemplate(AnnotationMetadata annotationMetadata, String templateString) {
      String path = (String)annotationMetadata.stringValue(Client.class, "path").orElse(null);
      if (StringUtils.isNotEmpty(path)) {
         return path + templateString;
      } else {
         String value = this.getClientId(annotationMetadata);
         return StringUtils.isNotEmpty(value) && value.startsWith("/") ? value + templateString : templateString;
      }
   }

   private String getClientId(AnnotationMetadata clientAnn) {
      return (String)clientAnn.stringValue(Client.class).orElse(null);
   }

   private void addParametersToQuery(Map<String, Object> parameters, ClientRequestUriContext uriContext) {
      for(Entry<String, Object> entry : parameters.entrySet()) {
         this.conversionService
            .convert(entry.getValue(), ConversionContext.STRING)
            .ifPresent(v -> this.conversionService.convert(entry.getKey(), ConversionContext.STRING).ifPresent(k -> uriContext.addQueryParameter(k, v)));
      }

   }

   private String appendQuery(String uri, Map<String, List<String>> queryParams) {
      if (queryParams.isEmpty()) {
         return uri;
      } else {
         UriBuilder builder = UriBuilder.of(uri);

         for(Entry<String, List<String>> entry : queryParams.entrySet()) {
            builder.queryParam((String)entry.getKey(), ((List)entry.getValue()).toArray());
         }

         return builder.toString();
      }
   }
}
