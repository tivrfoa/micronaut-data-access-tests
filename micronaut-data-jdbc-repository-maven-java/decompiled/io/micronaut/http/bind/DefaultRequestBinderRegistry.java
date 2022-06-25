package io.micronaut.http.bind;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.KotlinUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.clhm.ConcurrentLinkedHashMap;
import io.micronaut.http.FullHttpRequest;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.PushCapableHttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import io.micronaut.http.bind.binders.ContinuationArgumentBinder;
import io.micronaut.http.bind.binders.CookieAnnotationBinder;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.bind.binders.HeaderAnnotationBinder;
import io.micronaut.http.bind.binders.ParameterAnnotationBinder;
import io.micronaut.http.bind.binders.PathVariableAnnotationBinder;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.bind.binders.RequestAttributeAnnotationBinder;
import io.micronaut.http.bind.binders.RequestBeanAnnotationBinder;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

@Singleton
public class DefaultRequestBinderRegistry implements RequestBinderRegistry {
   private static final long CACHE_MAX_SIZE = 30L;
   private final Map<Class<? extends Annotation>, RequestArgumentBinder> byAnnotation = new LinkedHashMap();
   private final Map<DefaultRequestBinderRegistry.TypeAndAnnotation, RequestArgumentBinder> byTypeAndAnnotation = new LinkedHashMap();
   private final Map<Integer, RequestArgumentBinder> byType = new LinkedHashMap();
   private final ConversionService<?> conversionService;
   private final Map<DefaultRequestBinderRegistry.TypeAndAnnotation, Optional<RequestArgumentBinder>> argumentBinderCache = new ConcurrentLinkedHashMap.Builder(
         
      )
      .maximumWeightedCapacity(30L)
      .build();

   public DefaultRequestBinderRegistry(ConversionService conversionService, RequestArgumentBinder... binders) {
      this(conversionService, Arrays.asList(binders));
   }

   @Inject
   public DefaultRequestBinderRegistry(ConversionService conversionService, List<RequestArgumentBinder> binders) {
      this.conversionService = conversionService;
      if (CollectionUtils.isNotEmpty(binders)) {
         for(RequestArgumentBinder binder : binders) {
            this.addRequestArgumentBinder(binder);
         }
      }

      this.registerDefaultConverters(conversionService);
      this.registerDefaultAnnotationBinders(this.byAnnotation);
      this.byType.put(Argument.of(HttpHeaders.class).typeHashCode(), (RequestArgumentBinder)(argument, source) -> () -> Optional.of(source.getHeaders()));
      this.byType
         .put(
            Argument.of(HttpRequest.class).typeHashCode(),
            (RequestArgumentBinder)(argument, source) -> {
               Optional<Argument<?>> typeVariable = argument.getFirstTypeVariable()
                  .filter(arg -> arg.getType() != Object.class)
                  .filter(arg -> arg.getType() != Void.class);
               if (!typeVariable.isPresent() || !HttpMethod.permitsRequestBody(source.getMethod())) {
                  return () -> Optional.of(source);
               } else {
                  return source.getBody().isPresent()
                     ? () -> Optional.of(new FullHttpRequest(source, (Argument)typeVariable.get()))
                     : ArgumentBinder.BindingResult.UNSATISFIED;
               }
            }
         );
      this.byType
         .put(
            Argument.of(PushCapableHttpRequest.class).typeHashCode(),
            (RequestArgumentBinder)(argument, source) -> {
               if (source instanceof PushCapableHttpRequest) {
                  Optional<Argument<?>> typeVariable = argument.getFirstTypeVariable()
                     .filter(arg -> arg.getType() != Object.class)
                     .filter(arg -> arg.getType() != Void.class);
                  if (!typeVariable.isPresent() || !HttpMethod.permitsRequestBody(source.getMethod())) {
                     return () -> Optional.of((PushCapableHttpRequest)source);
                  } else {
                     return source.getBody().isPresent()
                        ? () -> Optional.of(
                              new DefaultRequestBinderRegistry.PushCapableFullHttpRequest((PushCapableHttpRequest)source, (Argument)typeVariable.get())
                           )
                        : ArgumentBinder.BindingResult.EMPTY;
                  }
               } else {
                  return ArgumentBinder.BindingResult.UNSATISFIED;
               }
            }
         );
      this.byType.put(Argument.of(HttpParameters.class).typeHashCode(), (RequestArgumentBinder)(argument, source) -> () -> Optional.of(source.getParameters()));
      this.byType.put(Argument.of(Cookies.class).typeHashCode(), (RequestArgumentBinder)(argument, source) -> () -> Optional.of(source.getCookies()));
      this.byType.put(Argument.of(Cookie.class).typeHashCode(), (RequestArgumentBinder)(context, source) -> {
         Cookies cookies = source.getCookies();
         String name = context.getArgument().getName();
         Cookie cookie = cookies.get(name);
         if (cookie == null) {
            cookie = cookies.get(NameUtils.hyphenate(name));
         }

         Cookie finalCookie = cookie;
         return () -> finalCookie != null ? Optional.of(finalCookie) : Optional.empty();
      });
   }

   @Override
   public <T, ST> void addRequestArgumentBinder(ArgumentBinder<T, ST> binder) {
      if (binder instanceof AnnotatedRequestArgumentBinder) {
         AnnotatedRequestArgumentBinder<?, ?> annotatedRequestArgumentBinder = (AnnotatedRequestArgumentBinder)binder;
         Class<? extends Annotation> annotationType = annotatedRequestArgumentBinder.getAnnotationType();
         if (binder instanceof TypedRequestArgumentBinder) {
            TypedRequestArgumentBinder<?> typedRequestArgumentBinder = (TypedRequestArgumentBinder)binder;
            Argument argumentType = typedRequestArgumentBinder.argumentType();
            this.byTypeAndAnnotation.put(new DefaultRequestBinderRegistry.TypeAndAnnotation(argumentType, annotationType), binder);
            List<Class<?>> superTypes = typedRequestArgumentBinder.superTypes();
            if (CollectionUtils.isNotEmpty(superTypes)) {
               for(Class<?> superType : superTypes) {
                  this.byTypeAndAnnotation.put(new DefaultRequestBinderRegistry.TypeAndAnnotation(Argument.of(superType), annotationType), binder);
               }
            }
         } else {
            this.byAnnotation.put(annotationType, annotatedRequestArgumentBinder);
         }
      } else if (binder instanceof TypedRequestArgumentBinder) {
         TypedRequestArgumentBinder typedRequestArgumentBinder = (TypedRequestArgumentBinder)binder;
         this.byType.put(typedRequestArgumentBinder.argumentType().typeHashCode(), typedRequestArgumentBinder);
      }

   }

   public <T> Optional<ArgumentBinder<T, HttpRequest<?>>> findArgumentBinder(Argument<T> argument, HttpRequest<?> source) {
      Optional<Class<? extends Annotation>> opt = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(Bindable.class);
      if (opt.isPresent()) {
         Class<? extends Annotation> annotationType = (Class)opt.get();
         RequestArgumentBinder<T> binder = this.findBinder(argument, annotationType);
         if (binder == null) {
            binder = (RequestArgumentBinder)this.byAnnotation.get(annotationType);
         }

         if (binder != null) {
            return Optional.of(binder);
         }
      } else {
         RequestArgumentBinder<T> binder = (RequestArgumentBinder)this.byType.get(argument.typeHashCode());
         if (binder != null) {
            return Optional.of(binder);
         }

         binder = (RequestArgumentBinder)this.byType.get(Argument.of(argument.getType()).typeHashCode());
         if (binder != null) {
            return Optional.of(binder);
         }
      }

      return Optional.of(new ParameterAnnotationBinder(this.conversionService));
   }

   protected <T> RequestArgumentBinder findBinder(Argument<T> argument, Class<? extends Annotation> annotationType) {
      DefaultRequestBinderRegistry.TypeAndAnnotation key = new DefaultRequestBinderRegistry.TypeAndAnnotation(argument, annotationType);
      return (RequestArgumentBinder)((Optional)this.argumentBinderCache
            .computeIfAbsent(
               key,
               key1 -> {
                  RequestArgumentBinder requestArgumentBinder = (RequestArgumentBinder)this.byTypeAndAnnotation.get(key1);
                  if (requestArgumentBinder == null) {
                     Class<?> javaType = key1.type.getType();
         
                     for(Entry<DefaultRequestBinderRegistry.TypeAndAnnotation, RequestArgumentBinder> entry : this.byTypeAndAnnotation.entrySet()) {
                        DefaultRequestBinderRegistry.TypeAndAnnotation typeAndAnnotation = (DefaultRequestBinderRegistry.TypeAndAnnotation)entry.getKey();
                        if (typeAndAnnotation.annotation == annotationType) {
                           Argument<?> t = typeAndAnnotation.type;
                           if (t.getType().isAssignableFrom(javaType)) {
                              requestArgumentBinder = (RequestArgumentBinder)entry.getValue();
                              if (requestArgumentBinder != null) {
                                 break;
                              }
                           }
                        }
                     }
         
                     if (requestArgumentBinder == null) {
                        requestArgumentBinder = (RequestArgumentBinder)this.byTypeAndAnnotation
                           .get(new DefaultRequestBinderRegistry.TypeAndAnnotation(Argument.of(argument.getType()), annotationType));
                     }
                  }
         
                  return Optional.ofNullable(requestArgumentBinder);
               }
            ))
         .orElse(null);
   }

   protected void registerDefaultConverters(ConversionService<?> conversionService) {
      conversionService.addConverter(CharSequence.class, MediaType.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            String str = object.toString();

            try {
               return Optional.of(MediaType.of(str));
            } catch (IllegalArgumentException var5) {
               context.reject(var5);
               return Optional.empty();
            }
         }
      }));
   }

   protected void registerDefaultAnnotationBinders(Map<Class<? extends Annotation>, RequestArgumentBinder> byAnnotation) {
      DefaultBodyAnnotationBinder bodyBinder = new DefaultBodyAnnotationBinder(this.conversionService);
      byAnnotation.put(Body.class, bodyBinder);
      CookieAnnotationBinder<Object> cookieAnnotationBinder = new CookieAnnotationBinder<>(this.conversionService);
      byAnnotation.put(cookieAnnotationBinder.getAnnotationType(), cookieAnnotationBinder);
      HeaderAnnotationBinder<Object> headerAnnotationBinder = new HeaderAnnotationBinder<>(this.conversionService);
      byAnnotation.put(headerAnnotationBinder.getAnnotationType(), headerAnnotationBinder);
      ParameterAnnotationBinder<Object> parameterAnnotationBinder = new ParameterAnnotationBinder<>(this.conversionService);
      byAnnotation.put(parameterAnnotationBinder.getAnnotationType(), parameterAnnotationBinder);
      RequestAttributeAnnotationBinder<Object> requestAttributeAnnotationBinder = new RequestAttributeAnnotationBinder<>(this.conversionService);
      byAnnotation.put(requestAttributeAnnotationBinder.getAnnotationType(), requestAttributeAnnotationBinder);
      PathVariableAnnotationBinder<Object> pathVariableAnnotationBinder = new PathVariableAnnotationBinder<>(this.conversionService);
      byAnnotation.put(pathVariableAnnotationBinder.getAnnotationType(), pathVariableAnnotationBinder);
      RequestBeanAnnotationBinder<Object> requestBeanAnnotationBinder = new RequestBeanAnnotationBinder<>(this, this.conversionService);
      byAnnotation.put(requestBeanAnnotationBinder.getAnnotationType(), requestBeanAnnotationBinder);
      if (KotlinUtils.KOTLIN_COROUTINES_SUPPORTED) {
         ContinuationArgumentBinder continuationArgumentBinder = new ContinuationArgumentBinder();
         this.byType.put(continuationArgumentBinder.argumentType().typeHashCode(), continuationArgumentBinder);
      }

   }

   private static final class PushCapableFullHttpRequest<B> extends FullHttpRequest<B> implements PushCapableHttpRequest<B> {
      public PushCapableFullHttpRequest(PushCapableHttpRequest<B> delegate, Argument<B> bodyType) {
         super(delegate, bodyType);
      }

      public PushCapableHttpRequest<B> getDelegate() {
         return (PushCapableHttpRequest<B>)super.getDelegate();
      }

      @Override
      public boolean isServerPushSupported() {
         return this.getDelegate().isServerPushSupported();
      }

      @Override
      public PushCapableHttpRequest<B> serverPush(@NonNull HttpRequest<?> request) {
         this.getDelegate().serverPush(request);
         return this;
      }
   }

   private static final class TypeAndAnnotation {
      private final Argument<?> type;
      private final Class<? extends Annotation> annotation;

      public TypeAndAnnotation(Argument<?> type, Class<? extends Annotation> annotation) {
         this.type = type;
         this.annotation = annotation;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultRequestBinderRegistry.TypeAndAnnotation that = (DefaultRequestBinderRegistry.TypeAndAnnotation)o;
            return !this.type.equalsType(that.type) ? false : this.annotation.equals(that.annotation);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.type.typeHashCode();
         return 31 * result + this.annotation.hashCode();
      }
   }
}
