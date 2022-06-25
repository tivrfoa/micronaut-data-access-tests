package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.ArrayUtils;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface Argument<T> extends TypeInformation<T>, AnnotatedElement, Type {
   Argument<String> STRING = of(String.class);
   Argument<Integer> INT = of(Integer.TYPE);
   Argument<Long> LONG = of(Long.TYPE);
   Argument<Float> FLOAT = of(Float.TYPE);
   Argument<Double> DOUBLE = of(Double.TYPE);
   Argument<Void> VOID = of(Void.TYPE);
   Argument<Byte> BYTE = of(Byte.TYPE);
   Argument<Boolean> BOOLEAN = of(Boolean.TYPE);
   Argument<Character> CHAR = of(Character.TYPE);
   Argument<Short> SHORT = of(Short.TYPE);
   Argument[] ZERO_ARGUMENTS = new Argument[0];
   Argument<Object> OBJECT_ARGUMENT = of(Object.class);
   Argument<List<String>> LIST_OF_STRING = listOf(String.class);
   Argument<Void> VOID_OBJECT = of(Void.class);

   @NonNull
   @Override
   String getName();

   boolean equalsType(@Nullable Argument<?> other);

   int typeHashCode();

   default boolean isTypeVariable() {
      return false;
   }

   default boolean isInstance(@Nullable Object o) {
      return o == null ? false : this.getType().isInstance(o);
   }

   default boolean isAssignableFrom(@NonNull Class<?> candidateType) {
      return this.getType().isAssignableFrom((Class)Objects.requireNonNull(candidateType, "Candidate type cannot be null"));
   }

   default boolean isAssignableFrom(@NonNull Argument<?> candidateArgument) {
      Objects.requireNonNull(candidateArgument, "Candidate type cannot be null");
      if (!this.isAssignableFrom(candidateArgument.getType())) {
         return false;
      } else {
         Argument[] typeParameters = this.getTypeParameters();
         Argument[] candidateArgumentTypeParameters = candidateArgument.getTypeParameters();
         if (typeParameters.length == 0) {
            return candidateArgumentTypeParameters.length >= 0;
         } else if (candidateArgumentTypeParameters.length == 0) {
            for(Argument typeParameter : typeParameters) {
               if (typeParameter.getType() != Object.class) {
                  return false;
               }
            }

            return true;
         } else {
            for(int i = 0; i < typeParameters.length; ++i) {
               Argument typeParameter = typeParameters[i];
               Argument candidateArgumentTypeParameter = candidateArgumentTypeParameters[i];
               if (!typeParameter.isAssignableFrom(candidateArgumentTypeParameter)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @NonNull
   static Class<?>[] toClassArray(@Nullable Argument<?>... arguments) {
      if (ArrayUtils.isEmpty(arguments)) {
         return ReflectionUtils.EMPTY_CLASS_ARRAY;
      } else {
         Class<?>[] types = new Class[arguments.length];

         for(int i = 0; i < arguments.length; ++i) {
            Argument<?> argument = arguments[i];
            types[i] = argument.getType();
         }

         return types;
      }
   }

   @NonNull
   static String toString(@Nullable Argument<?>... arguments) {
      if (ArrayUtils.isNotEmpty(arguments)) {
         StringBuilder baseString = new StringBuilder();

         for(int i = 0; i < arguments.length; ++i) {
            Argument<?> argument = arguments[i];
            baseString.append(argument.toString());
            if (i != arguments.length - 1) {
               baseString.append(',');
            }
         }

         return baseString.toString();
      } else {
         return "";
      }
   }

   @NonNull
   static <T> Argument<T> of(@NonNull Class<T> type, @Nullable String name, @Nullable Argument<?>... typeParameters) {
      return new DefaultArgument<>(type, name, AnnotationMetadata.EMPTY_METADATA, typeParameters);
   }

   @NonNull
   static <T> Argument<T> ofTypeVariable(
      @NonNull Class<T> type, @Nullable String name, @Nullable AnnotationMetadata annotationMetadata, @Nullable Argument<?>... typeParameters
   ) {
      return new DefaultGenericPlaceholder<>(type, name, annotationMetadata, typeParameters);
   }

   @NonNull
   static <T> Argument<T> ofTypeVariable(
      @NonNull Class<T> type,
      @Nullable String argumentName,
      @NonNull String variableName,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Argument<?>... typeParameters
   ) {
      Objects.requireNonNull(variableName, "Variable name cannot be null");
      return new DefaultGenericPlaceholder<>(type, argumentName, variableName, annotationMetadata, typeParameters);
   }

   @NonNull
   static <T> Argument<T> ofTypeVariable(@NonNull Class<T> type, @Nullable String name) {
      return new DefaultGenericPlaceholder<>(type, name, AnnotationMetadata.EMPTY_METADATA);
   }

   @NonNull
   static <T> Argument<T> ofTypeVariable(@NonNull Class<T> type, @Nullable String argumentName, @NonNull String variableName) {
      return new DefaultGenericPlaceholder<>(type, argumentName, variableName, AnnotationMetadata.EMPTY_METADATA);
   }

   @NonNull
   static <T> Argument<T> of(
      @NonNull Class<T> type, @Nullable String name, @Nullable AnnotationMetadata annotationMetadata, @Nullable Argument<?>... typeParameters
   ) {
      return new DefaultArgument<>(type, name, annotationMetadata, typeParameters);
   }

   @NonNull
   static <T> Argument<T> of(@NonNull Class<T> type, @Nullable AnnotationMetadata annotationMetadata, @Nullable Argument<?>... typeParameters) {
      return new DefaultArgument<>(type, annotationMetadata, typeParameters);
   }

   @NonNull
   static <T> Argument<T> of(@NonNull Class<T> type, @Nullable String name) {
      return new DefaultArgument<>(type, name, AnnotationMetadata.EMPTY_METADATA, ZERO_ARGUMENTS);
   }

   @NonNull
   static <T> Argument<T> of(@NonNull Class<T> type, @Nullable Argument<?>... typeParameters) {
      return (Argument<T>)(ArrayUtils.isEmpty(typeParameters)
         ? of(type)
         : new DefaultArgument<>(type, NameUtils.decapitalize(type.getSimpleName()), AnnotationMetadata.EMPTY_METADATA, typeParameters));
   }

   @NonNull
   static Argument<?> of(@NonNull Type type) {
      Objects.requireNonNull(type, "Type cannot be null");
      if (type instanceof Class) {
         return of((Class<T>)type);
      } else if (type instanceof ParameterizedType) {
         ParameterizedType pt = (ParameterizedType)type;
         Type rawType = pt.getRawType();
         if (rawType instanceof Class) {
            Class<?> rawClass = (Class)rawType;
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            if (ArrayUtils.isNotEmpty(actualTypeArguments)) {
               Argument<?>[] typeArguments = new Argument[actualTypeArguments.length];

               for(int i = 0; i < actualTypeArguments.length; ++i) {
                  Type typeArgument = actualTypeArguments[i];
                  if (!(typeArgument instanceof Class) && !(typeArgument instanceof ParameterizedType)) {
                     return of(rawClass);
                  }

                  typeArguments[i] = of(typeArgument);
               }

               return of(rawClass, typeArguments);
            } else {
               return of(rawClass);
            }
         } else {
            throw new IllegalArgumentException("A ParameterizedType that has a raw type that is not a class cannot be converted to an argument");
         }
      } else {
         throw new IllegalArgumentException("Type [" + type + "] must be a Class or ParameterizedType");
      }
   }

   @NonNull
   static <T> Argument<T> of(@NonNull Class<T> type) {
      return new DefaultArgument<>(type, null, AnnotationMetadata.EMPTY_METADATA, Collections.emptyMap(), ZERO_ARGUMENTS);
   }

   @NonNull
   static <T> Argument<T> of(@NonNull Class<T> type, @Nullable Class<?>... typeParameters) {
      return of(type, AnnotationMetadata.EMPTY_METADATA, typeParameters);
   }

   @NonNull
   static <T> Argument<T> of(@NonNull Class<T> type, @Nullable AnnotationMetadata annotationMetadata, @Nullable Class<?>[] typeParameters) {
      if (ArrayUtils.isEmpty(typeParameters)) {
         return of(type, annotationMetadata);
      } else {
         TypeVariable<Class<T>>[] parameters = type.getTypeParameters();
         int len = typeParameters.length;
         if (parameters.length != len) {
            throw new IllegalArgumentException("Type parameter length does not match. Required: " + parameters.length + ", Specified: " + len);
         } else {
            Argument<?>[] typeArguments = new Argument[len];

            for(int i = 0; i < parameters.length; ++i) {
               TypeVariable<Class<T>> parameter = parameters[i];
               typeArguments[i] = of(typeParameters[i], parameter.getName());
            }

            return new DefaultArgument<>(type, annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA, typeArguments);
         }
      }
   }

   @NonNull
   static <T> Argument<List<T>> listOf(@NonNull Class<T> type) {
      return of(List.class, type);
   }

   @NonNull
   static <T> Argument<List<T>> listOf(@NonNull Argument<T> type) {
      return of(List.class, type);
   }

   @NonNull
   static <T> Argument<Set<T>> setOf(@NonNull Class<T> type) {
      return of(Set.class, type);
   }

   @NonNull
   static <T> Argument<Set<T>> setOf(@NonNull Argument<T> type) {
      return of(Set.class, type);
   }

   @NonNull
   static <K, V> Argument<Map<K, V>> mapOf(@NonNull Class<K> keyType, @NonNull Class<V> valueType) {
      return of(Map.class, keyType, valueType);
   }

   @NonNull
   static <K, V> Argument<Map<K, V>> mapOf(@NonNull Argument<K> keyType, @NonNull Argument<V> valueType) {
      return of(Map.class, keyType, valueType);
   }
}
