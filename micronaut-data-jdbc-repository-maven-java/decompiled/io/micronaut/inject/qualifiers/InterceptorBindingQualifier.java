package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanType;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public final class InterceptorBindingQualifier<T> implements Qualifier<T> {
   public static final String META_MEMBER_MEMBERS = "bindMembers";
   private static final String META_MEMBER_INTERCEPTOR_TYPE = "interceptorType";
   private final Map<String, List<AnnotationValue<?>>> supportedAnnotationNames;
   private final Set<Class<?>> supportedInterceptorTypes;

   InterceptorBindingQualifier(AnnotationMetadata annotationMetadata) {
      List<AnnotationValue<Annotation>> annotationValues = (List)annotationMetadata.findAnnotation("io.micronaut.inject.qualifiers.InterceptorBindingQualifier")
         .map(av -> av.getAnnotations("value"))
         .orElse(Collections.emptyList());
      this.supportedAnnotationNames = new HashMap(annotationValues.size());

      for(AnnotationValue<Annotation> annotationValue : annotationValues) {
         String name = (String)annotationValue.stringValue().orElse(null);
         if (name != null) {
            AnnotationValue<Annotation> members = (AnnotationValue)annotationValue.getAnnotation("bindMembers").orElse(null);
            if (members != null) {
               List<AnnotationValue<?>> existing = (List)this.supportedAnnotationNames.computeIfAbsent(name, k -> new ArrayList(5));
               existing.add(members);
            } else {
               this.supportedAnnotationNames.put(name, null);
            }
         }
      }

      this.supportedInterceptorTypes = (Set)annotationValues.stream()
         .flatMap(av -> (Stream)av.classValue("interceptorType").map(Stream::of).orElse(Stream.empty()))
         .collect(Collectors.toSet());
   }

   InterceptorBindingQualifier(Collection<AnnotationValue<?>> bindingAnnotations) {
      if (CollectionUtils.isNotEmpty(bindingAnnotations)) {
         this.supportedAnnotationNames = new HashMap(bindingAnnotations.size());

         for(AnnotationValue<?> bindingAnnotation : bindingAnnotations) {
            String name = (String)bindingAnnotation.stringValue().orElse(null);
            if (name != null) {
               AnnotationValue<Annotation> members = (AnnotationValue)bindingAnnotation.getAnnotation("bindMembers").orElse(null);
               if (members != null) {
                  List<AnnotationValue<?>> existing = (List)this.supportedAnnotationNames.computeIfAbsent(name, k -> new ArrayList(5));
                  existing.add(members);
               } else {
                  this.supportedAnnotationNames.putIfAbsent(name, null);
               }
            }
         }
      } else {
         this.supportedAnnotationNames = Collections.emptyMap();
      }

      this.supportedInterceptorTypes = Collections.emptySet();
   }

   @Deprecated
   InterceptorBindingQualifier(String[] bindingAnnotations) {
      if (ArrayUtils.isNotEmpty(bindingAnnotations)) {
         this.supportedAnnotationNames = new HashMap(bindingAnnotations.length);

         for(String bindingAnnotation : bindingAnnotations) {
            this.supportedAnnotationNames.put(bindingAnnotation, null);
         }
      } else {
         this.supportedAnnotationNames = Collections.emptyMap();
      }

      this.supportedInterceptorTypes = Collections.emptySet();
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> {
         if (this.supportedInterceptorTypes.contains(candidate.getBeanType())) {
            return true;
         } else {
            AnnotationMetadata annotationMetadata = candidate.getAnnotationMetadata();
            Collection<AnnotationValue<?>> interceptorValues = resolveInterceptorAnnotationValues(annotationMetadata, null);
            if (interceptorValues.isEmpty()) {
               return false;
            } else if (interceptorValues.size() == 1) {
               AnnotationValue<?> interceptorBinding = (AnnotationValue)interceptorValues.iterator().next();
               String annotationName = (String)interceptorBinding.stringValue().orElse(null);
               if (annotationName == null) {
                  return false;
               } else {
                  List<AnnotationValue<?>> bindingList = (List)this.supportedAnnotationNames.get(annotationName);
                  if (bindingList == null) {
                     return this.supportedAnnotationNames.containsKey(annotationName);
                  } else {
                     AnnotationValue<Annotation> otherBinding = (AnnotationValue)interceptorBinding.getAnnotation("bindMembers").orElse(null);
                     boolean matched = true;

                     for(AnnotationValue<?> binding : bindingList) {
                        matched = matched && (!binding.isPresent("bindMembers") || binding.equals(otherBinding));
                     }

                     return matched;
                  }
               }
            } else {
               boolean matched = false;

               for(AnnotationValue<?> annotation : interceptorValues) {
                  String annotationName = (String)annotation.stringValue().orElse(null);
                  if (annotationName != null) {
                     List<AnnotationValue<?>> bindingList = (List)this.supportedAnnotationNames.get(annotationName);
                     if (bindingList != null) {
                        AnnotationValue<Annotation> otherBinding = (AnnotationValue)annotation.getAnnotation("bindMembers").orElse(null);

                        for(AnnotationValue<?> binding : bindingList) {
                           matched = !binding.isPresent("bindMembers") || binding.equals(otherBinding);
                           if (matched) {
                              break;
                           }
                        }
                     } else {
                        matched = this.supportedAnnotationNames.containsKey(annotationName);
                     }

                     if (matched) {
                        break;
                     }
                  }
               }

               return matched;
            }
         }
      });
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         InterceptorBindingQualifier<?> that = (InterceptorBindingQualifier)o;
         return this.supportedAnnotationNames.equals(that.supportedAnnotationNames) && this.supportedInterceptorTypes.equals(that.supportedInterceptorTypes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.supportedAnnotationNames, this.supportedInterceptorTypes});
   }

   public String toString() {
      return CollectionUtils.isEmpty(this.supportedAnnotationNames) && CollectionUtils.isEmpty(this.supportedInterceptorTypes)
         ? "@InterceptorBinding(NONE)"
         : (String)this.supportedAnnotationNames.keySet().stream().map(name -> "@InterceptorBinding(" + name + ")").collect(Collectors.joining(" "))
            + (String)this.supportedInterceptorTypes
               .stream()
               .map(name -> "@InterceptorBinding(interceptorType = " + name + ")")
               .collect(Collectors.joining(" "));
   }

   @NonNull
   private static Collection<AnnotationValue<?>> resolveInterceptorAnnotationValues(@NonNull AnnotationMetadata annotationMetadata, @Nullable String kind) {
      List<AnnotationValue<Annotation>> bindings = annotationMetadata.getAnnotationValuesByName("io.micronaut.aop.InterceptorBinding");
      return (Collection<AnnotationValue<?>>)(CollectionUtils.isNotEmpty(bindings) ? (Collection)bindings.stream().filter(av -> {
         if (!av.stringValue().isPresent()) {
            return false;
         } else if (kind == null) {
            return true;
         } else {
            String specifiedkind = (String)av.stringValue("kind").orElse(null);
            return specifiedkind == null || specifiedkind.equals(kind);
         }
      }).collect(Collectors.toList()) : Collections.emptyList());
   }
}
