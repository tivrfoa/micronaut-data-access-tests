package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Any;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Qualifiers {
   public static <T> Qualifier<T> any() {
      return AnyQualifier.INSTANCE;
   }

   @Nullable
   public static <T> Qualifier<T> forArgument(@NonNull Argument<?> argument) {
      AnnotationMetadata annotationMetadata = ((Argument)Objects.requireNonNull(argument, "Argument cannot be null")).getAnnotationMetadata();
      boolean hasMetadata = annotationMetadata != AnnotationMetadata.EMPTY_METADATA;
      List<String> qualifierTypes = hasMetadata ? annotationMetadata.getAnnotationNamesByStereotype("javax.inject.Qualifier") : null;
      if (!CollectionUtils.isNotEmpty(qualifierTypes)) {
         return null;
      } else if (qualifierTypes.size() == 1) {
         return byAnnotation(annotationMetadata, (String)qualifierTypes.iterator().next());
      } else {
         Qualifier[] qualifiers = new Qualifier[qualifierTypes.size()];
         int i = 0;

         for(String type : qualifierTypes) {
            qualifiers[i++] = byAnnotation(annotationMetadata, type);
         }

         return byQualifiers(qualifiers);
      }
   }

   public static <T> Qualifier<T> byQualifiers(Qualifier<T>... qualifiers) {
      return new CompositeQualifier<>(qualifiers);
   }

   public static <T> Qualifier<T> byName(String name) {
      return new NameQualifier<>(null, name);
   }

   public static <T> Qualifier<T> byAnnotation(Annotation annotation) {
      Qualifier<T> qualifier = findCustomByType(AnnotationMetadata.EMPTY_METADATA, annotation.annotationType());
      return (Qualifier<T>)(qualifier != null ? qualifier : new AnnotationQualifier<>(annotation));
   }

   public static <T> Qualifier<T> byAnnotation(AnnotationMetadata metadata, Class<? extends Annotation> type) {
      Qualifier<T> instance = findCustomByType(metadata, type);
      return (Qualifier<T>)(instance != null ? instance : AnnotationMetadataQualifier.fromType(metadata, type));
   }

   public static <T> Qualifier<T> byAnnotation(AnnotationMetadata metadata, String type) {
      Qualifier<T> qualifier = findCustomByName(metadata, type);
      return (Qualifier<T>)(qualifier != null ? qualifier : AnnotationMetadataQualifier.fromTypeName(metadata, type));
   }

   public static <T extends Annotation> Qualifier<T> byAnnotation(AnnotationMetadata metadata, AnnotationValue<T> annotationValue) {
      Qualifier<T> qualifier = findCustomByName(metadata, annotationValue.getAnnotationName());
      return (Qualifier<T>)(qualifier != null ? qualifier : AnnotationMetadataQualifier.fromValue(metadata, annotationValue));
   }

   public static <T> Qualifier<T> byRepeatableAnnotation(AnnotationMetadata metadata, String repeatableType) {
      return new RepeatableAnnotationQualifier<>(metadata, repeatableType);
   }

   @Internal
   public static <T> Qualifier<T> byAnnotationSimple(AnnotationMetadata metadata, String type) {
      Qualifier<T> qualifier = findCustomByName(metadata, type);
      return (Qualifier<T>)(qualifier != null ? qualifier : AnnotationMetadataQualifier.fromTypeName(metadata, type));
   }

   public static <T> Qualifier<T> byStereotype(Class<? extends Annotation> stereotype) {
      Qualifier<T> instance = findCustomByType(AnnotationMetadata.EMPTY_METADATA, stereotype);
      return (Qualifier<T>)(instance != null ? instance : new AnnotationStereotypeQualifier<>(stereotype));
   }

   public static <T> Qualifier<T> byStereotype(String stereotype) {
      Qualifier<T> qualifier = findCustomByName(AnnotationMetadata.EMPTY_METADATA, stereotype);
      return (Qualifier<T>)(qualifier != null ? qualifier : new NamedAnnotationStereotypeQualifier<>(stereotype));
   }

   public static <T> Qualifier<T> byTypeArguments(Class... typeArguments) {
      return new TypeArgumentQualifier<>(typeArguments);
   }

   @NonNull
   public static <T> Qualifier<T> byExactTypeArgumentName(@NonNull String typeName) {
      return new ExactTypeArgumentNameQualifier<>(typeName);
   }

   public static <T> Qualifier<T> byTypeArgumentsClosest(Class... typeArguments) {
      return new ClosestTypeArgumentQualifier<>(typeArguments);
   }

   public static <T> Qualifier<T> byType(Class... typeArguments) {
      return new TypeAnnotationQualifier<>(typeArguments);
   }

   @NonNull
   public static <T> Qualifier<T> byInterceptorBinding(@NonNull AnnotationMetadata annotationMetadata) {
      return new InterceptorBindingQualifier<>(annotationMetadata);
   }

   @Deprecated
   @NonNull
   public static <T> Qualifier<T> byInterceptorBinding(@NonNull Collection<String> bindingAnnotationNames) {
      return new InterceptorBindingQualifier<>((String[])bindingAnnotationNames.toArray(StringUtils.EMPTY_STRING_ARRAY));
   }

   @NonNull
   public static <T> Qualifier<T> byInterceptorBindingValues(@NonNull Collection<AnnotationValue<?>> binding) {
      return new InterceptorBindingQualifier<>(binding);
   }

   @Nullable
   private static <T> Qualifier<T> findCustomByType(@NonNull AnnotationMetadata metadata, @NonNull Class<? extends Annotation> type) {
      if (Any.class == type) {
         return AnyQualifier.INSTANCE;
      } else if (Primary.class == type) {
         return PrimaryQualifier.INSTANCE;
      } else {
         if (Type.class == type) {
            Optional<Class> aClass = metadata.classValue(type);
            if (aClass.isPresent()) {
               return byType((Class)aClass.get());
            }
         } else if (Named.class == type || "javax.inject.Named".equals(type.getName())) {
            Optional<String> value = metadata.stringValue(type);
            if (value.isPresent()) {
               return byName((String)value.get());
            }
         }

         return null;
      }
   }

   @Nullable
   private static <T> Qualifier<T> findCustomByName(@NonNull AnnotationMetadata metadata, @NonNull String type) {
      if (Type.NAME.equals(type)) {
         Optional<Class> aClass = metadata.classValue(type);
         if (aClass.isPresent()) {
            return byType((Class)aClass.get());
         }
      } else {
         if (Any.NAME.equals(type)) {
            return AnyQualifier.INSTANCE;
         }

         if (Qualifier.PRIMARY.equals(type)) {
            return PrimaryQualifier.INSTANCE;
         }

         if (Named.class.getName().equals(type) || "javax.inject.Named".equals(type)) {
            String n = (String)metadata.stringValue(type).orElse(null);
            if (n != null) {
               return byName(n);
            }
         }
      }

      return null;
   }
}
