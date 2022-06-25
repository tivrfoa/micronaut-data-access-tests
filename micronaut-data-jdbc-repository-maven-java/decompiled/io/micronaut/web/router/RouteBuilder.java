package io.micronaut.web.router;

import io.micronaut.context.BeanLocator;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.naming.conventions.MethodConvention;
import io.micronaut.core.naming.conventions.PropertyConvention;
import io.micronaut.core.naming.conventions.TypeConvention;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.UriMapping;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ProxyBeanDefinition;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Indexed(RouteBuilder.class)
public interface RouteBuilder {
   PropertyConvention ID = PropertyConvention.ID;

   Set<Integer> getExposedPorts();

   List<FilterRoute> getFilterRoutes();

   List<UriRoute> getUriRoutes();

   List<StatusRoute> getStatusRoutes();

   List<ErrorRoute> getErrorRoutes();

   RouteBuilder.UriNamingStrategy getUriNamingStrategy();

   FilterRoute addFilter(String pathPattern, Supplier<HttpFilter> filter);

   FilterRoute addFilter(String pathPattern, BeanLocator beanLocator, BeanDefinition<? extends HttpFilter> beanDefinition);

   ResourceRoute resources(Class cls);

   default ResourceRoute resources(Object instance) {
      return this.resources(instance.getClass());
   }

   ResourceRoute single(Class cls);

   default ResourceRoute single(Object instance) {
      return this.single(instance.getClass());
   }

   default StatusRoute status(HttpStatus status, Object instance, String method) {
      return this.status(status, instance.getClass(), method, ReflectionUtils.EMPTY_CLASS_ARRAY);
   }

   StatusRoute status(HttpStatus status, Class type, String method, Class... parameterTypes);

   StatusRoute status(Class originatingClass, HttpStatus status, Class type, String method, Class... parameterTypes);

   ErrorRoute error(Class<? extends Throwable> error, Class type, String method, Class... parameterTypes);

   ErrorRoute error(Class originatingClass, Class<? extends Throwable> error, Class type, String method, Class... parameterTypes);

   default ErrorRoute error(Class<? extends Throwable> error, Class type) {
      return this.error(
         error, type, NameUtils.decapitalize(NameUtils.trimSuffix(type.getSimpleName(), "Exception", "Error")), ReflectionUtils.EMPTY_CLASS_ARRAY
      );
   }

   default ErrorRoute error(Class<? extends Throwable> error, Object instance) {
      return this.error(error, instance.getClass(), NameUtils.decapitalize(NameUtils.trimSuffix(error.getSimpleName(), "Exception", "Error")), error);
   }

   default ErrorRoute error(Class<? extends Throwable> error, Object instance, String method) {
      return this.error(error, instance.getClass(), method, error);
   }

   default ErrorRoute error(Class<? extends Throwable> error, Object instance, String method, Class... parameterTypes) {
      return this.error(error, instance.getClass(), method, parameterTypes);
   }

   default UriRoute GET(String uri, Object target) {
      return this.GET(uri, target, MethodConvention.INDEX.methodName());
   }

   default UriRoute GET(Object target) {
      Class<?> type = target.getClass();
      return this.GET(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute GET(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.GET(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.SHOW.methodName(), Object.class);
   }

   default UriRoute GET(Class type) {
      return this.GET(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.INDEX.methodName());
   }

   default UriRoute GET(Class type, PropertyConvention id) {
      return this.GET(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.SHOW.methodName(), Object.class);
   }

   default UriRoute GET(String uri, ExecutableMethod<?, ?> method) {
      return this.GET(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute GET(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.GET(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute GET(String uri, Object target, String method, Class... parameterTypes);

   UriRoute GET(String uri, Class<?> type, String method, Class... parameterTypes);

   default UriRoute POST(String uri, Object target, Class... parameterTypes) {
      return this.POST(uri, target, MethodConvention.SAVE.methodName(), parameterTypes);
   }

   default UriRoute POST(Object target) {
      Class<?> type = target.getClass();
      return this.POST(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute POST(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.POST(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.UPDATE.methodName());
   }

   default UriRoute POST(Class type) {
      return this.POST(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.SAVE.methodName());
   }

   default UriRoute POST(Class type, PropertyConvention id) {
      return this.POST(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.UPDATE.methodName());
   }

   default UriRoute POST(String uri, ExecutableMethod<?, ?> method) {
      return this.POST(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute POST(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.POST(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute POST(String uri, Object target, String method, Class... parameterTypes);

   UriRoute POST(String uri, Class type, String method, Class... parameterTypes);

   default UriRoute PUT(String uri, Object target) {
      return this.PUT(uri, target, MethodConvention.UPDATE.methodName());
   }

   default UriRoute PUT(Object target) {
      Class<?> type = target.getClass();
      return this.PUT(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute PUT(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.PUT(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.UPDATE.methodName(), Object.class);
   }

   default UriRoute PUT(Class type) {
      return this.PUT(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.UPDATE.methodName(), Object.class);
   }

   default UriRoute PUT(Class type, PropertyConvention id) {
      return this.PUT(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.UPDATE.methodName(), Object.class);
   }

   default UriRoute PUT(String uri, ExecutableMethod<?, ?> method) {
      return this.PUT(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute PUT(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.PUT(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute PUT(String uri, Object target, String method, Class... parameterTypes);

   UriRoute PUT(String uri, Class type, String method, Class... parameterTypes);

   default UriRoute PATCH(String uri, Object target) {
      return this.PATCH(uri, target, MethodConvention.UPDATE.methodName());
   }

   default UriRoute PATCH(Object target) {
      Class<?> type = target.getClass();
      return this.PATCH(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute PATCH(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.PATCH(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.UPDATE.methodName(), Object.class);
   }

   default UriRoute PATCH(Class type) {
      return this.PATCH(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.UPDATE.methodName(), Object.class);
   }

   default UriRoute PATCH(Class type, PropertyConvention id) {
      return this.PATCH(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.UPDATE.methodName(), Object.class);
   }

   default UriRoute PATCH(String uri, ExecutableMethod<?, ?> method) {
      return this.PATCH(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute PATCH(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.PATCH(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute PATCH(String uri, Object target, String method, Class... parameterTypes);

   UriRoute PATCH(String uri, Class type, String method, Class... parameterTypes);

   default UriRoute DELETE(String uri, Object target) {
      return this.DELETE(uri, target, MethodConvention.DELETE.methodName(), Object.class);
   }

   default UriRoute DELETE(Object target) {
      Class<?> type = target.getClass();
      return this.DELETE(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute DELETE(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.DELETE(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.DELETE.methodName(), Object.class);
   }

   default UriRoute DELETE(Class type) {
      return this.DELETE(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.DELETE.methodName(), Object.class);
   }

   default UriRoute DELETE(Class type, PropertyConvention id) {
      return this.DELETE(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.DELETE.methodName(), Object.class);
   }

   default UriRoute DELETE(String uri, ExecutableMethod<?, ?> method) {
      return this.DELETE(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute DELETE(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.DELETE(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute DELETE(String uri, Object target, String method, Class... parameterTypes);

   UriRoute DELETE(String uri, Class type, String method, Class... parameterTypes);

   default UriRoute OPTIONS(String uri, Object target) {
      return this.OPTIONS(uri, target, MethodConvention.OPTIONS.methodName());
   }

   default UriRoute OPTIONS(Object target) {
      Class<?> type = target.getClass();
      return this.OPTIONS(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute OPTIONS(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.OPTIONS(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.OPTIONS.methodName());
   }

   default UriRoute OPTIONS(Class type) {
      return this.OPTIONS(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.OPTIONS.methodName());
   }

   default UriRoute OPTIONS(Class type, PropertyConvention id) {
      return this.OPTIONS(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.OPTIONS.methodName());
   }

   default UriRoute OPTIONS(String uri, ExecutableMethod<?, ?> method) {
      return this.OPTIONS(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute OPTIONS(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.OPTIONS(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute OPTIONS(String uri, Object target, String method, Class... parameterTypes);

   UriRoute OPTIONS(String uri, Class type, String method, Class... parameterTypes);

   default UriRoute HEAD(String uri, Object target) {
      return this.HEAD(uri, target, MethodConvention.HEAD.methodName());
   }

   default UriRoute HEAD(Object target) {
      Class<?> type = target.getClass();
      return this.HEAD(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute HEAD(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.HEAD(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.HEAD.methodName());
   }

   default UriRoute HEAD(Class type) {
      return this.HEAD(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.HEAD.methodName());
   }

   default UriRoute HEAD(Class type, PropertyConvention id) {
      return this.HEAD(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.HEAD.methodName());
   }

   default UriRoute HEAD(String uri, ExecutableMethod<?, ?> method) {
      return this.HEAD(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute HEAD(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.HEAD(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute HEAD(String uri, Object target, String method, Class... parameterTypes);

   UriRoute HEAD(String uri, Class type, String method, Class... parameterTypes);

   default UriRoute TRACE(String uri, Object target) {
      return this.TRACE(uri, target, MethodConvention.TRACE.methodName());
   }

   default UriRoute TRACE(Object target) {
      Class<?> type = target.getClass();
      return this.TRACE(this.getUriNamingStrategy().resolveUri(type), target);
   }

   default UriRoute TRACE(Object target, PropertyConvention id) {
      Class<?> type = target.getClass();
      return this.TRACE(this.getUriNamingStrategy().resolveUri(type, id), target, MethodConvention.TRACE.methodName());
   }

   default UriRoute TRACE(Class type) {
      return this.TRACE(this.getUriNamingStrategy().resolveUri(type), type, MethodConvention.TRACE.methodName());
   }

   default UriRoute TRACE(Class type, PropertyConvention id) {
      return this.HEAD(this.getUriNamingStrategy().resolveUri(type, id), type, MethodConvention.TRACE.methodName());
   }

   default UriRoute TRACE(String uri, ExecutableMethod<?, ?> method) {
      return this.TRACE(uri, method.getDeclaringType(), method.getMethodName(), method.getArgumentTypes());
   }

   default UriRoute TRACE(String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      return this.TRACE(uri, beanDefinition.getBeanType(), method.getMethodName(), method.getArgumentTypes());
   }

   UriRoute TRACE(String uri, Object target, String method, Class... parameterTypes);

   UriRoute TRACE(String uri, Class type, String method, Class... parameterTypes);

   public interface UriNamingStrategy {
      default String resolveUri(Class<?> type) {
         Controller annotation = (Controller)type.getAnnotation(Controller.class);
         String uri = this.normalizeUri(annotation != null ? annotation.value() : null);
         return uri != null ? uri : '/' + TypeConvention.CONTROLLER.asPropertyName(type);
      }

      @NonNull
      default String resolveUri(BeanDefinition<?> beanDefinition) {
         String uri = (String)beanDefinition.stringValue(UriMapping.class).orElseGet(() -> (String)beanDefinition.stringValue(Controller.class).orElse("/"));
         uri = this.normalizeUri(uri);
         if (uri != null) {
            return uri;
         } else {
            Class<?> beanType;
            if (beanDefinition instanceof ProxyBeanDefinition) {
               ProxyBeanDefinition pbd = (ProxyBeanDefinition)beanDefinition;
               beanType = pbd.getTargetType();
            } else {
               beanType = beanDefinition.getBeanType();
            }

            return '/' + TypeConvention.CONTROLLER.asPropertyName(beanType);
         }
      }

      @NonNull
      default String resolveUri(String property) {
         if (StringUtils.isEmpty(property)) {
            return "/";
         } else {
            return property.charAt(0) != '/' ? '/' + NameUtils.decapitalize(property) : property;
         }
      }

      @NonNull
      default String resolveUri(Class type, PropertyConvention id) {
         return this.resolveUri(type) + "/{" + id.lowerCaseName() + "}";
      }

      default String normalizeUri(@Nullable String uri) {
         if (uri != null) {
            int len = uri.length();
            if (len > 0 && uri.charAt(0) != '/') {
               uri = '/' + uri;
            }

            if (len > 1 && uri.charAt(uri.length() - 1) == '/') {
               uri = uri.substring(0, uri.length() - 1);
            }

            if (len > 0) {
               return uri;
            }
         }

         return null;
      }
   }
}
