package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface ElementQuery<T extends Element> {
   ElementQuery<ClassElement> ALL_INNER_CLASSES = of(ClassElement.class);
   ElementQuery<FieldElement> ALL_FIELDS = of(FieldElement.class);
   ElementQuery<MethodElement> ALL_METHODS = of(MethodElement.class);
   ElementQuery<ConstructorElement> CONSTRUCTORS = of(ConstructorElement.class).onlyDeclared();

   @NonNull
   ElementQuery<T> onlyDeclared();

   ElementQuery<T> onlyInjected();

   @NonNull
   ElementQuery<T> onlyConcrete();

   @NonNull
   ElementQuery<T> onlyAbstract();

   @NonNull
   ElementQuery<T> onlyAccessible();

   @NonNull
   ElementQuery<T> onlyAccessible(ClassElement fromType);

   ElementQuery<T> onlyInstance();

   ElementQuery<T> includeEnumConstants();

   ElementQuery<T> includeOverriddenMethods();

   ElementQuery<T> includeHiddenElements();

   @NonNull
   ElementQuery<T> named(@NonNull Predicate<String> predicate);

   @NonNull
   default ElementQuery<T> named(@NonNull String name) {
      return this.named((Predicate<String>)(n -> n.equals(name)));
   }

   @NonNull
   ElementQuery<T> typed(@NonNull Predicate<ClassElement> predicate);

   @NonNull
   ElementQuery<T> annotated(@NonNull Predicate<AnnotationMetadata> predicate);

   @NonNull
   ElementQuery<T> modifiers(@NonNull Predicate<Set<ElementModifier>> predicate);

   @NonNull
   ElementQuery<T> filter(@NonNull Predicate<T> predicate);

   @NonNull
   ElementQuery.Result<T> result();

   @NonNull
   static <T1 extends Element> ElementQuery<T1> of(@NonNull Class<T1> elementType) {
      return new DefaultElementQuery<>((Class<T1>)Objects.requireNonNull(elementType, "Element type cannot be null"));
   }

   public interface Result<T extends Element> {
      boolean isOnlyAbstract();

      boolean isOnlyInjected();

      boolean isOnlyConcrete();

      @NonNull
      Class<T> getElementType();

      boolean isOnlyAccessible();

      Optional<ClassElement> getOnlyAccessibleFromType();

      boolean isOnlyDeclared();

      boolean isOnlyInstance();

      boolean isIncludeEnumConstants();

      boolean isIncludeOverriddenMethods();

      boolean isIncludeHiddenElements();

      @NonNull
      List<Predicate<String>> getNamePredicates();

      @NonNull
      List<Predicate<ClassElement>> getTypePredicates();

      @NonNull
      List<Predicate<AnnotationMetadata>> getAnnotationPredicates();

      @NonNull
      List<Predicate<Set<ElementModifier>>> getModifierPredicates();

      @NonNull
      List<Predicate<T>> getElementPredicates();
   }
}
