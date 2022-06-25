package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

@Internal
public class DefaultArgument<T> implements Argument<T>, ArgumentCoercible<T> {
   public static final Set<Class<?>> CONTAINER_TYPES = CollectionUtils.setOf(
      (T[])(List.class, Set.class, Collection.class, Queue.class, SortedSet.class, Deque.class, Vector.class, ArrayList.class)
   );
   public static final Set<String> PROVIDER_TYPES = CollectionUtils.setOf(
      (T[])("io.micronaut.context.BeanProvider", "javax.inject.Provider", "jakarta.inject.Provider")
   );
   private final Class<T> type;
   private final String name;
   private final Map<String, Argument<?>> typeParameters;
   private final Argument<?>[] typeParameterArray;
   private final AnnotationMetadata annotationMetadata;
   private final boolean isTypeVar;

   public DefaultArgument(Class<T> type, String name, AnnotationMetadata annotationMetadata, Argument<?>... genericTypes) {
      this(type, name, annotationMetadata, ArrayUtils.isNotEmpty(genericTypes) ? initializeTypeParameters(genericTypes) : Collections.emptyMap(), genericTypes);
   }

   public DefaultArgument(Class<T> type, AnnotationMetadata annotationMetadata, Argument<?>... genericTypes) {
      this(type, null, annotationMetadata, ArrayUtils.isNotEmpty(genericTypes) ? initializeTypeParameters(genericTypes) : Collections.emptyMap(), genericTypes);
   }

   public DefaultArgument(
      Class<T> type, String name, AnnotationMetadata annotationMetadata, Map<String, Argument<?>> typeParameters, Argument<?>[] typeParameterArray
   ) {
      this(type, name, annotationMetadata, typeParameters, typeParameterArray, false);
   }

   public DefaultArgument(Class<T> type, String name, AnnotationMetadata annotationMetadata, boolean isTypeVariable, Argument<?>... genericTypes) {
      this(
         type,
         name,
         annotationMetadata,
         ArrayUtils.isNotEmpty(genericTypes) ? initializeTypeParameters(genericTypes) : Collections.emptyMap(),
         genericTypes,
         isTypeVariable
      );
   }

   protected DefaultArgument(
      Class<T> type,
      String name,
      AnnotationMetadata annotationMetadata,
      Map<String, Argument<?>> typeParameters,
      Argument<?>[] typeParameterArray,
      boolean isTypeVariable
   ) {
      this.type = (Class)Objects.requireNonNull(type, "Type cannot be null");
      this.name = name;
      this.annotationMetadata = annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA;
      this.typeParameters = typeParameters;
      this.typeParameterArray = typeParameterArray;
      this.isTypeVar = isTypeVariable;
   }

   public DefaultArgument(Type type, String name, AnnotationMetadata annotationMetadata) {
      this.annotationMetadata = annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA;
      if (type == null) {
         type = this.getClass().getGenericSuperclass();
         if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException(type + " is not parameterized");
         }

         type = ((ParameterizedType)type).getActualTypeArguments()[0];
      }

      if (type instanceof Class) {
         this.type = (Class)type;
         this.typeParameterArray = Argument.ZERO_ARGUMENTS;
      } else {
         if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException(type.getClass().getSimpleName() + " types are not supported");
         }

         ParameterizedType parameterizedType = (ParameterizedType)type;
         this.type = (Class)parameterizedType.getRawType();
         TypeVariable<Class<T>>[] params = this.type.getTypeParameters();
         Type[] paramValues = parameterizedType.getActualTypeArguments();
         this.typeParameterArray = new Argument[params.length];

         for(int i = 0; i < params.length; ++i) {
            TypeVariable param = params[i];
            Type value = paramValues[i];
            this.typeParameterArray[i] = new DefaultArgument(value, param.getName(), AnnotationMetadata.EMPTY_METADATA);
         }
      }

      this.name = name;
      this.typeParameters = initializeTypeParameters(this.typeParameterArray);
      this.isTypeVar = false;
   }

   @Override
   public boolean isTypeVariable() {
      return this.isTypeVar;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public Optional<Argument<?>> getFirstTypeVariable() {
      return !this.typeParameters.isEmpty() ? Optional.of(this.typeParameters.values().iterator().next()) : Optional.empty();
   }

   @Override
   public Argument[] getTypeParameters() {
      return this.typeParameterArray == null ? Argument.ZERO_ARGUMENTS : this.typeParameterArray;
   }

   @Override
   public Map<String, Argument<?>> getTypeVariables() {
      return this.typeParameters;
   }

   @NonNull
   @Override
   public Class<T> getType() {
      return this.type;
   }

   @NonNull
   @Override
   public String getName() {
      return this.name == null ? this.getType().getSimpleName() : this.name;
   }

   public String toString() {
      return this.name == null ? this.getType().getSimpleName() : this.getType().getSimpleName() + " " + this.getName();
   }

   @Override
   public boolean equalsType(@Nullable Argument<?> o) {
      if (this == o) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return Objects.equals(this.type, o.getType()) && Objects.equals(this.typeParameters, o.getTypeVariables());
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DefaultArgument)) {
         return false;
      } else {
         DefaultArgument<?> that = (DefaultArgument)o;
         return Objects.equals(this.type, that.type)
            && Objects.equals(this.getName(), that.getName())
            && Objects.equals(this.typeParameters, that.typeParameters);
      }
   }

   @Override
   public int typeHashCode() {
      return Objects.hash(new Object[]{this.type, this.typeParameters});
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.getName(), this.typeParameters});
   }

   private static Map<String, Argument<?>> initializeTypeParameters(Argument<?>[] genericTypes) {
      Map<String, Argument<?>> typeParameters;
      if (genericTypes != null && genericTypes.length > 0) {
         typeParameters = new LinkedHashMap(genericTypes.length);

         for(Argument<?> genericType : genericTypes) {
            typeParameters.put(genericType.getName(), genericType);
         }
      } else {
         typeParameters = Collections.emptyMap();
      }

      return typeParameters;
   }

   @NonNull
   @Override
   public Argument<T> asArgument() {
      return this;
   }
}
