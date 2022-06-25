package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.ast.beans.BeanElementBuilder;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface ClassElement extends TypedElement {
   ClassElement[] ZERO_CLASS_ELEMENTS = new ClassElement[0];

   boolean isAssignable(String type);

   default boolean isTypeVariable() {
      return false;
   }

   default boolean isGenericPlaceholder() {
      return this instanceof GenericPlaceholderElement;
   }

   default boolean isWildcard() {
      return this instanceof WildcardElement;
   }

   default boolean isAssignable(ClassElement type) {
      return this.isAssignable(type.getName());
   }

   default boolean isOptional() {
      return this.isAssignable(Optional.class);
   }

   default String getCanonicalName() {
      return this.isOptional() ? (String)this.getFirstTypeArgument().map(Element::getName).orElse(Object.class.getName()) : this.getName();
   }

   default boolean isRecord() {
      return false;
   }

   default boolean isInner() {
      return false;
   }

   default boolean isEnum() {
      return this instanceof EnumElement;
   }

   default boolean isProxy() {
      return this.getSimpleName().endsWith("$Intercepted");
   }

   @NonNull
   default Optional<MethodElement> getPrimaryConstructor() {
      return Optional.empty();
   }

   @NonNull
   default Optional<MethodElement> getDefaultConstructor() {
      return Optional.empty();
   }

   default Optional<ClassElement> getSuperType() {
      return Optional.empty();
   }

   default Collection<ClassElement> getInterfaces() {
      return Collections.emptyList();
   }

   @NonNull
   @Override
   default ClassElement getType() {
      return this;
   }

   @Override
   default String getSimpleName() {
      return NameUtils.getSimpleName(this.getName());
   }

   default String getPackageName() {
      return NameUtils.getPackageName(this.getName());
   }

   default PackageElement getPackage() {
      return PackageElement.of(this.getPackageName());
   }

   default List<PropertyElement> getBeanProperties() {
      return Collections.emptyList();
   }

   default List<FieldElement> getFields() {
      return this.getEnclosedElements(ElementQuery.ALL_FIELDS);
   }

   @Deprecated
   default List<FieldElement> getFields(@NonNull Predicate<Set<ElementModifier>> modifierFilter) {
      Objects.requireNonNull(modifierFilter, "The modifier filter cannot be null");
      return this.getEnclosedElements(ElementQuery.ALL_FIELDS.modifiers(modifierFilter));
   }

   default <T extends Element> List<T> getEnclosedElements(@NonNull ElementQuery<T> query) {
      return Collections.emptyList();
   }

   default Optional<ClassElement> getEnclosingType() {
      return Optional.empty();
   }

   default <T extends Element> Optional<T> getEnclosedElement(@NonNull ElementQuery<T> query) {
      List<T> enclosedElements = this.getEnclosedElements(query);
      return !enclosedElements.isEmpty() ? Optional.of(enclosedElements.iterator().next()) : Optional.empty();
   }

   default boolean isInterface() {
      return false;
   }

   default boolean isIterable() {
      return this.isArray() || this.isAssignable(Iterable.class);
   }

   @NonNull
   default List<? extends ClassElement> getBoundGenericTypes() {
      return new ArrayList(this.getTypeArguments().values());
   }

   @NonNull
   default List<? extends GenericPlaceholderElement> getDeclaredGenericPlaceholders() {
      return Collections.emptyList();
   }

   @NonNull
   default ClassElement getRawClassElement() {
      return this.withBoundGenericTypes(Collections.emptyList());
   }

   @NonNull
   default ClassElement withBoundGenericTypes(@NonNull List<? extends ClassElement> typeArguments) {
      return this;
   }

   default ClassElement foldBoundGenericTypes(@NonNull Function<ClassElement, ClassElement> fold) {
      List<ClassElement> typeArgs = (List)this.getBoundGenericTypes().stream().map(arg -> arg.foldBoundGenericTypes(fold)).collect(Collectors.toList());
      if (typeArgs.contains(null)) {
         typeArgs = Collections.emptyList();
      }

      return (ClassElement)fold.apply(this.withBoundGenericTypes(typeArgs));
   }

   @NonNull
   default Map<String, ClassElement> getTypeArguments(@NonNull String type) {
      return Collections.emptyMap();
   }

   @NonNull
   default Map<String, ClassElement> getTypeArguments(@NonNull Class<?> type) {
      ArgumentUtils.requireNonNull("type", type);
      return this.getTypeArguments(type.getName());
   }

   @NonNull
   default Map<String, ClassElement> getTypeArguments() {
      return Collections.emptyMap();
   }

   @NonNull
   default Map<String, Map<String, ClassElement>> getAllTypeArguments() {
      return Collections.emptyMap();
   }

   default Optional<ClassElement> getFirstTypeArgument() {
      return this.getTypeArguments().values().stream().findFirst();
   }

   default boolean isAssignable(Class<?> type) {
      return this.isAssignable(type.getName());
   }

   @NonNull
   ClassElement toArray();

   @NonNull
   ClassElement fromArray();

   @NonNull
   default BeanElementBuilder addAssociatedBean(@NonNull ClassElement type) {
      throw new UnsupportedOperationException("Element of type [" + this.getClass() + "] does not support adding associated beans at compilation time");
   }

   @NonNull
   static ClassElement of(@NonNull Class<?> type) {
      return new ReflectClassElement((Class<?>)Objects.requireNonNull(type, "Type cannot be null"));
   }

   @NonNull
   static ClassElement of(@NonNull Type type) {
      Objects.requireNonNull(type, "Type cannot be null");
      if (type instanceof Class) {
         return new ReflectClassElement((Class<?>)type);
      } else if (type instanceof TypeVariable) {
         return new ReflectGenericPlaceholderElement((TypeVariable<?>)type, 0);
      } else if (type instanceof WildcardType) {
         return new ReflectWildcardElement((WildcardType)type);
      } else if (type instanceof ParameterizedType) {
         final ParameterizedType pType = (ParameterizedType)type;
         if (pType.getOwnerType() != null) {
            throw new UnsupportedOperationException("Owner types are not supported");
         } else {
            return new ReflectClassElement(ReflectTypeElement.getErasure(type)) {
               @NonNull
               @Override
               public List<? extends ClassElement> getBoundGenericTypes() {
                  return (List<? extends ClassElement>)Arrays.stream(pType.getActualTypeArguments()).map(ClassElement::of).collect(Collectors.toList());
               }
            };
         }
      } else if (type instanceof GenericArrayType) {
         return of(((GenericArrayType)type).getGenericComponentType()).toArray();
      } else {
         throw new IllegalArgumentException("Bad type: " + type.getClass().getName());
      }
   }

   @NonNull
   static ClassElement of(@NonNull Class<?> type, @NonNull AnnotationMetadata annotationMetadata, @NonNull Map<String, ClassElement> typeArguments) {
      Objects.requireNonNull(annotationMetadata, "Annotation metadata cannot be null");
      Objects.requireNonNull(typeArguments, "Type arguments cannot be null");
      return new ReflectClassElement((Class)Objects.requireNonNull(type, "Type cannot be null")) {
         @Override
         public AnnotationMetadata getAnnotationMetadata() {
            return annotationMetadata;
         }

         @Override
         public Map<String, ClassElement> getTypeArguments() {
            return Collections.unmodifiableMap(typeArguments);
         }

         @NonNull
         @Override
         public List<? extends ClassElement> getBoundGenericTypes() {
            return (List<? extends ClassElement>)this.getDeclaredGenericPlaceholders()
               .stream()
               .map(tv -> (ClassElement)typeArguments.get(tv.getVariableName()))
               .collect(Collectors.toList());
         }
      };
   }

   @Internal
   @NonNull
   static ClassElement of(@NonNull String typeName) {
      return new SimpleClassElement(typeName);
   }

   @Internal
   @NonNull
   static ClassElement of(@NonNull String typeName, boolean isInterface, @Nullable AnnotationMetadata annotationMetadata) {
      return new SimpleClassElement(typeName, isInterface, annotationMetadata);
   }

   @Internal
   @NonNull
   static ClassElement of(
      @NonNull String typeName, boolean isInterface, @Nullable AnnotationMetadata annotationMetadata, Map<String, ClassElement> typeArguments
   ) {
      return new SimpleClassElement(typeName, isInterface, annotationMetadata);
   }
}
