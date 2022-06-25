package io.micronaut.http.client.bind;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.KotlinUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.version.annotation.Version;
import io.micronaut.http.BasicAuth;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.CookieValue;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.annotation.RequestBean;
import io.micronaut.http.client.bind.binders.AttributeClientRequestBinder;
import io.micronaut.http.client.bind.binders.HeaderClientRequestBinder;
import io.micronaut.http.client.bind.binders.QueryValueClientArgumentRequestBinder;
import io.micronaut.http.client.bind.binders.VersionClientRequestBinder;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import kotlin.coroutines.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Internal
public class DefaultHttpClientBinderRegistry implements HttpClientBinderRegistry {
   private static final Logger LOG = LoggerFactory.getLogger(HttpClientBinderRegistry.class);
   private final Map<Class<? extends Annotation>, ClientArgumentRequestBinder<?>> byAnnotation = new LinkedHashMap();
   private final Map<Integer, ClientArgumentRequestBinder<?>> byType = new LinkedHashMap();
   private final Map<Class<? extends Annotation>, AnnotatedClientRequestBinder<?>> methodByAnnotation = new LinkedHashMap();

   protected DefaultHttpClientBinderRegistry(ConversionService<?> conversionService, List<ClientRequestBinder> binders, BeanContext beanContext) {
      this.byType
         .put(
            Argument.of(HttpHeaders.class).typeHashCode(),
            (ClientArgumentRequestBinder<HttpHeaders>)(context, uriContext, value, request) -> value.forEachValue(request::header)
         );
      this.byType
         .put(
            Argument.of(Cookies.class).typeHashCode(),
            (ClientArgumentRequestBinder<Cookies>)(context, uriContext, value, request) -> request.cookies(value.getAll())
         );
      this.byType
         .put(Argument.of(Cookie.class).typeHashCode(), (ClientArgumentRequestBinder<Cookie>)(context, uriContext, value, request) -> request.cookie(value));
      this.byType
         .put(
            Argument.of(BasicAuth.class).typeHashCode(),
            (ClientArgumentRequestBinder<BasicAuth>)(context, uriContext, value, request) -> request.basicAuth(value.getUsername(), value.getPassword())
         );
      this.byType
         .put(
            Argument.of(Locale.class).typeHashCode(),
            (ClientArgumentRequestBinder<Locale>)(context, uriContext, value, request) -> request.header("Accept-Language", value.toLanguageTag())
         );
      this.byAnnotation.put(QueryValue.class, new QueryValueClientArgumentRequestBinder(conversionService));
      this.byAnnotation
         .put(
            PathVariable.class,
            (ClientArgumentRequestBinder<Object>)(context, uriContext, value, request) -> {
               String parameterName = (String)context.getAnnotationMetadata()
                  .stringValue(PathVariable.class)
                  .filter(StringUtils::isNotEmpty)
                  .orElse(context.getArgument().getName());
               conversionService.convert(value, ConversionContext.STRING.with(context.getAnnotationMetadata()))
                  .filter(StringUtils::isNotEmpty)
                  .ifPresent(param -> uriContext.getPathParameters().put(parameterName, param));
            }
         );
      this.byAnnotation
         .put(
            CookieValue.class,
            (ClientArgumentRequestBinder<Object>)(context, uriContext, value, request) -> {
               String cookieName = (String)context.getAnnotationMetadata()
                  .stringValue(CookieValue.class)
                  .filter(StringUtils::isNotEmpty)
                  .orElse(context.getArgument().getName());
               conversionService.convert(value, String.class).ifPresent(o -> request.cookie(Cookie.of(cookieName, o)));
            }
         );
      this.byAnnotation
         .put(
            Header.class,
            (ClientArgumentRequestBinder<Object>)(context, uriContext, value, request) -> {
               AnnotationMetadata annotationMetadata = context.getAnnotationMetadata();
               String headerName = (String)annotationMetadata.stringValue(Header.class)
                  .filter(StringUtils::isNotEmpty)
                  .orElse(NameUtils.hyphenate(context.getArgument().getName()));
               conversionService.convert(value, String.class).ifPresent(header -> request.getHeaders().set(headerName, header));
            }
         );
      this.byAnnotation
         .put(
            RequestAttribute.class,
            (ClientArgumentRequestBinder<Object>)(context, uriContext, value, request) -> {
               AnnotationMetadata annotationMetadata = context.getAnnotationMetadata();
               String name = context.getArgument().getName();
               String attributeName = (String)annotationMetadata.stringValue(RequestAttribute.class)
                  .filter(StringUtils::isNotEmpty)
                  .orElse(NameUtils.hyphenate(name));
               request.getAttributes().put(attributeName, value);
               conversionService.convert(value, ConversionContext.STRING.with(context.getAnnotationMetadata()))
                  .filter(StringUtils::isNotEmpty)
                  .ifPresent(param -> {
                     if (uriContext.getUriTemplate().getVariableNames().contains(name)) {
                        uriContext.getPathParameters().put(name, param);
                     }
         
                  });
            }
         );
      this.byAnnotation.put(Body.class, (ClientArgumentRequestBinder<Object>)(context, uriContext, value, request) -> request.body(value));
      this.byAnnotation.put(RequestBean.class, (ClientArgumentRequestBinder<Object>)(context, uriContext, value, request) -> {
         BeanIntrospection<Object> introspection = BeanIntrospection.getIntrospection(context.getArgument().getType());

         for(BeanProperty<Object, Object> beanProperty : introspection.getBeanProperties()) {
            this.findArgumentBinder(beanProperty.asArgument()).ifPresent(binderx -> {
               Object propertyValue = beanProperty.get(value);
               if (propertyValue != null) {
                  binderx.bind(context.with(beanProperty.asArgument()), uriContext, propertyValue, request);
               }

            });
         }

      });
      this.methodByAnnotation.put(Header.class, new HeaderClientRequestBinder());
      this.methodByAnnotation.put(Version.class, new VersionClientRequestBinder(beanContext));
      this.methodByAnnotation.put(RequestAttribute.class, new AttributeClientRequestBinder());
      if (KotlinUtils.KOTLIN_COROUTINES_SUPPORTED) {
         this.byType.put(Argument.of(Continuation.class).typeHashCode(), (ClientArgumentRequestBinder<Object>)(context, uriContext, value, request) -> {
         });
      }

      if (CollectionUtils.isNotEmpty(binders)) {
         for(ClientRequestBinder binder : binders) {
            this.addBinder(binder);
         }
      }

   }

   @Override
   public <T> Optional<ClientArgumentRequestBinder<?>> findArgumentBinder(@NonNull Argument<T> argument) {
      Optional<Class<? extends Annotation>> opt = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(Bindable.class);
      if (opt.isPresent()) {
         Class<? extends Annotation> annotationType = (Class)opt.get();
         ClientArgumentRequestBinder<?> binder = (ClientArgumentRequestBinder)this.byAnnotation.get(annotationType);
         return Optional.ofNullable(binder);
      } else {
         Optional<ClientArgumentRequestBinder<?>> typeBinder = this.findTypeBinder(argument);
         if (typeBinder.isPresent()) {
            return typeBinder;
         } else if (argument.isOptional()) {
            Argument<?> typeArgument = (Argument)argument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            return this.findTypeBinder(typeArgument);
         } else {
            return Optional.empty();
         }
      }
   }

   @Override
   public Optional<AnnotatedClientRequestBinder<?>> findAnnotatedBinder(@NonNull Class<?> annotationType) {
      return Optional.ofNullable(this.methodByAnnotation.get(annotationType));
   }

   public <T> void addBinder(ClientRequestBinder binder) {
      if (binder instanceof AnnotatedClientRequestBinder) {
         AnnotatedClientRequestBinder<?> annotatedBinder = (AnnotatedClientRequestBinder)binder;
         this.methodByAnnotation.put(annotatedBinder.getAnnotationType(), annotatedBinder);
      } else if (binder instanceof AnnotatedClientArgumentRequestBinder) {
         AnnotatedClientArgumentRequestBinder<?> annotatedRequestArgumentBinder = (AnnotatedClientArgumentRequestBinder)binder;
         Class<? extends Annotation> annotationType = annotatedRequestArgumentBinder.getAnnotationType();
         this.byAnnotation.put(annotationType, annotatedRequestArgumentBinder);
      } else if (binder instanceof TypedClientArgumentRequestBinder) {
         TypedClientArgumentRequestBinder<?> typedRequestArgumentBinder = (TypedClientArgumentRequestBinder)binder;
         this.byType.put(typedRequestArgumentBinder.argumentType().typeHashCode(), typedRequestArgumentBinder);
         List<Class<?>> superTypes = typedRequestArgumentBinder.superTypes();
         if (CollectionUtils.isNotEmpty(superTypes)) {
            for(Class<?> superType : superTypes) {
               this.byType.put(Argument.of(superType).typeHashCode(), typedRequestArgumentBinder);
            }
         }
      } else if (LOG.isErrorEnabled()) {
         LOG.error(
            "The client request binder {} was rejected because it does not implement {}, {}, or {}",
            binder.getClass().getName(),
            TypedClientArgumentRequestBinder.class.getName(),
            AnnotatedClientArgumentRequestBinder.class.getName(),
            AnnotatedClientRequestBinder.class.getName()
         );
      }

   }

   private <T> Optional<ClientArgumentRequestBinder<?>> findTypeBinder(Argument<T> argument) {
      ClientArgumentRequestBinder<?> binder = (ClientArgumentRequestBinder)this.byType.get(argument.typeHashCode());
      return binder != null ? Optional.of(binder) : Optional.ofNullable(this.byType.get(Argument.of(argument.getType()).typeHashCode()));
   }
}
