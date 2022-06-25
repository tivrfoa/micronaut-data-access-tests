package io.micronaut.aop.chain;

import io.micronaut.aop.Interceptor;
import io.micronaut.aop.InterceptorBinding;
import io.micronaut.aop.InterceptorKind;
import io.micronaut.aop.InvocationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
abstract class AbstractInterceptorChain<B, R> implements InvocationContext<B, R> {
   protected static final Logger LOG = LoggerFactory.getLogger(InterceptorChain.class);
   protected final Interceptor<B, R>[] interceptors;
   protected final Object[] originalParameters;
   protected final int interceptorCount;
   protected volatile MutableConvertibleValues<Object> attributes;
   protected int index = 0;
   protected volatile Map<String, MutableArgumentValue<?>> parameters;

   AbstractInterceptorChain(Interceptor<B, R>[] interceptors, Object... originalParameters) {
      this.interceptors = interceptors;
      this.interceptorCount = interceptors.length;
      this.originalParameters = originalParameters;
   }

   @NonNull
   @Override
   public Object[] getParameterValues() {
      return this.originalParameters;
   }

   @NonNull
   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      MutableConvertibleValues<Object> localAttributes = this.attributes;
      if (localAttributes == null) {
         synchronized(this) {
            localAttributes = this.attributes;
            if (localAttributes == null) {
               localAttributes = MutableConvertibleValues.of(new ConcurrentHashMap(5));
               this.attributes = localAttributes;
            }
         }
      }

      return localAttributes;
   }

   @NonNull
   @Override
   public Map<String, MutableArgumentValue<?>> getParameters() {
      Map<String, MutableArgumentValue<?>> localParameters = this.parameters;
      if (localParameters == null) {
         synchronized(this) {
            localParameters = this.parameters;
            if (localParameters == null) {
               Argument[] arguments = this.getArguments();
               localParameters = new LinkedHashMap(arguments.length);

               for(final int i = 0; i < arguments.length; ++i) {
                  final Argument argument = arguments[i];
                  localParameters.put(argument.getName(), new MutableArgumentValue<Object>() {
                     @Override
                     public AnnotationMetadata getAnnotationMetadata() {
                        return argument.getAnnotationMetadata();
                     }

                     @Override
                     public Optional<Argument<?>> getFirstTypeVariable() {
                        return argument.getFirstTypeVariable();
                     }

                     @Override
                     public Argument[] getTypeParameters() {
                        return argument.getTypeParameters();
                     }

                     @Override
                     public Map<String, Argument<?>> getTypeVariables() {
                        return argument.getTypeVariables();
                     }

                     @NonNull
                     @Override
                     public String getName() {
                        return argument.getName();
                     }

                     @NonNull
                     @Override
                     public Class<Object> getType() {
                        return argument.getType();
                     }

                     @Override
                     public boolean equalsType(@Nullable Argument<?> other) {
                        return argument.equalsType(other);
                     }

                     @Override
                     public int typeHashCode() {
                        return argument.typeHashCode();
                     }

                     @Override
                     public Object getValue() {
                        return AbstractInterceptorChain.this.originalParameters[i];
                     }

                     @Override
                     public void setValue(Object value) {
                        AbstractInterceptorChain.this.originalParameters[i] = value;
                     }
                  });
               }

               localParameters = Collections.unmodifiableMap(localParameters);
               this.parameters = localParameters;
            }
         }
      }

      return localParameters;
   }

   @Override
   public R proceed(@NonNull Interceptor from) throws RuntimeException {
      for(int i = 0; i < this.interceptors.length; ++i) {
         Interceptor<B, R> interceptor = this.interceptors[i];
         if (interceptor == from) {
            this.index = i + 1;
            return this.proceed();
         }
      }

      throw new IllegalArgumentException("Argument [" + from + "] is not within the interceptor chain");
   }

   @NonNull
   protected static Collection<AnnotationValue<?>> resolveInterceptorValues(@NonNull AnnotationMetadata annotationMetadata, @NonNull InterceptorKind kind) {
      if (!(annotationMetadata instanceof AnnotationMetadataHierarchy)) {
         List<AnnotationValue<InterceptorBinding>> bindings = annotationMetadata.getAnnotationValuesByType(InterceptorBinding.class);
         return (Collection<AnnotationValue<?>>)(CollectionUtils.isNotEmpty(bindings) ? (Collection)bindings.stream().filter(av -> {
            if (!av.stringValue().isPresent()) {
               return false;
            } else {
               InterceptorKind specifiedkindx = (InterceptorKind)av.enumValue("kind", InterceptorKind.class).orElse(null);
               return specifiedkindx == null || specifiedkindx.equals(kind);
            }
         }).collect(Collectors.toSet()) : Collections.emptyList());
      } else {
         List<AnnotationValue<InterceptorBinding>> declaredValues = annotationMetadata.getDeclaredMetadata()
            .getAnnotationValuesByType(InterceptorBinding.class);
         List<AnnotationValue<InterceptorBinding>> parentValues = ((AnnotationMetadataHierarchy)annotationMetadata)
            .getRootMetadata()
            .getAnnotationValuesByType(InterceptorBinding.class);
         if (!CollectionUtils.isNotEmpty(declaredValues) && !CollectionUtils.isNotEmpty(parentValues)) {
            return Collections.emptyList();
         } else {
            Set<AnnotationValue<?>> resolved = new HashSet(declaredValues.size() + parentValues.size());
            Set<String> declared = new HashSet(declaredValues.size());

            for(AnnotationValue<InterceptorBinding> declaredValue : declaredValues) {
               String annotationName = (String)declaredValue.stringValue().orElse(null);
               if (annotationName != null) {
                  InterceptorKind specifiedkind = (InterceptorKind)declaredValue.enumValue("kind", InterceptorKind.class).orElse(null);
                  if (specifiedkind == null || specifiedkind.equals(kind)) {
                     if (!annotationMetadata.isRepeatableAnnotation(annotationName)) {
                        declared.add(annotationName);
                     }

                     resolved.add(declaredValue);
                  }
               }
            }

            for(AnnotationValue<InterceptorBinding> parentValue : parentValues) {
               String annotationName = (String)parentValue.stringValue().orElse(null);
               if (annotationName != null && !declared.contains(annotationName)) {
                  InterceptorKind specifiedkind = (InterceptorKind)parentValue.enumValue("kind", InterceptorKind.class).orElse(null);
                  if (specifiedkind == null || specifiedkind.equals(kind)) {
                     resolved.add(parentValue);
                  }
               }
            }

            return resolved;
         }
      }
   }
}
