package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.ArrayUtils;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public interface TypeInformation<T> extends TypeVariableResolver, AnnotationMetadataProvider, Type {
   @NonNull
   Class<T> getType();

   default boolean isPrimitive() {
      return this.getType().isPrimitive();
   }

   default Class<?> getWrapperType() {
      return this.isPrimitive() ? ReflectionUtils.getWrapperType(this.getType()) : this.getType();
   }

   @NonNull
   default String getTypeName() {
      Argument<?>[] typeParameters = this.getTypeParameters();
      if (ArrayUtils.isNotEmpty(typeParameters)) {
         String typeName = this.getType().getTypeName();
         return typeName + "<" + (String)Arrays.stream(typeParameters).map(TypeInformation::getTypeName).collect(Collectors.joining(",")) + ">";
      } else {
         return this.getType().getTypeName();
      }
   }

   default boolean isReactive() {
      return RuntimeTypeInformation.isReactive(this.getType());
   }

   default boolean isWrapperType() {
      return RuntimeTypeInformation.isWrapperType(this.getType());
   }

   default Argument<?> getWrappedType() {
      return RuntimeTypeInformation.getWrappedType(this);
   }

   default boolean isCompletable() {
      return RuntimeTypeInformation.isCompletable(this.getType());
   }

   default boolean isAsync() {
      Class<T> type = this.getType();
      return CompletionStage.class.isAssignableFrom(type);
   }

   default boolean isAsyncOrReactive() {
      return this.isAsync() || this.isReactive();
   }

   default boolean isContainerType() {
      Class<T> type = this.getType();
      return Map.class == type || DefaultArgument.CONTAINER_TYPES.contains(type);
   }

   default boolean hasTypeVariables() {
      return !this.getTypeVariables().isEmpty();
   }

   default String getTypeString(boolean simple) {
      Class<T> type = this.getType();
      StringBuilder returnType = new StringBuilder(simple ? type.getSimpleName() : type.getName());
      Map<String, Argument<?>> generics = this.getTypeVariables();
      if (!generics.isEmpty()) {
         returnType.append("<").append((String)generics.values().stream().map(arg -> arg.getTypeString(simple)).collect(Collectors.joining(", "))).append(">");
      }

      return returnType.toString();
   }

   default boolean isVoid() {
      Class<T> javaReturnType = this.getType();
      if (javaReturnType == Void.TYPE) {
         return true;
      } else if (this.isCompletable()) {
         return true;
      } else {
         return !this.isReactive() && !this.isAsync() ? false : this.getFirstTypeVariable().filter(arg -> arg.getType() == Void.class).isPresent();
      }
   }

   default boolean isOptional() {
      Class<T> type = this.getType();
      return type == Optional.class;
   }

   default boolean isSpecifiedSingle() {
      return RuntimeTypeInformation.isSpecifiedSingle(this);
   }

   @NonNull
   default Type asType() {
      return (Type)(this.getTypeParameters().length == 0 ? this.getType() : this.asParameterizedType());
   }

   @NonNull
   default ParameterizedType asParameterizedType() {
      return new ParameterizedType() {
         public Type[] getActualTypeArguments() {
            return (Type[])Arrays.stream(TypeInformation.this.getTypeParameters()).map(TypeInformation::asType).toArray(x$0 -> new Type[x$0]);
         }

         public Type getRawType() {
            return TypeInformation.this.getType();
         }

         public Type getOwnerType() {
            return null;
         }

         public String getTypeName() {
            return TypeInformation.this.getTypeName();
         }

         public String toString() {
            return this.getTypeName();
         }

         public int hashCode() {
            return Arrays.hashCode(this.getActualTypeArguments()) ^ Objects.hashCode(this.getOwnerType()) ^ Objects.hashCode(this.getRawType());
         }

         public boolean equals(Object o) {
            if (o instanceof ParameterizedType) {
               ParameterizedType that = (ParameterizedType)o;
               if (this == that) {
                  return true;
               } else {
                  return Objects.equals(this.getOwnerType(), that.getOwnerType())
                     && Objects.equals(this.getRawType(), that.getRawType())
                     && Arrays.equals(this.getActualTypeArguments(), that.getActualTypeArguments());
               }
            } else {
               return false;
            }
         }
      };
   }

   default boolean isArray() {
      return this.getType().isArray();
   }

   @NonNull
   default String getSimpleName() {
      return this.getType().getSimpleName();
   }

   default boolean isProvider() {
      for(String type : DefaultArgument.PROVIDER_TYPES) {
         if (this.getType().getName().equals(type)) {
            return true;
         }
      }

      return false;
   }
}
