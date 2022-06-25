package io.micronaut.inject.ast;

import io.micronaut.context.annotation.Bean;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Internal
final class DefaultElementQuery<T extends Element> implements ElementQuery<T>, ElementQuery.Result<T> {
   private static final ClassElement ONLY_ACCESSIBLE_MARKER = ClassElement.of(DefaultElementQuery.class);
   private final Class<T> elementType;
   private final ClassElement onlyAccessibleType;
   private final boolean onlyDeclared;
   private final boolean onlyAbstract;
   private final boolean onlyConcrete;
   private final boolean onlyInjected;
   private final List<Predicate<String>> namePredicates;
   private final List<Predicate<AnnotationMetadata>> annotationPredicates;
   private final List<Predicate<Set<ElementModifier>>> modifiersPredicates;
   private final List<Predicate<T>> elementPredicates;
   private final List<Predicate<ClassElement>> typePredicates;
   private final boolean onlyInstance;
   private final boolean includeEnumConstants;
   private final boolean includeOverriddenMethods;
   private final boolean includeHiddenElements;

   DefaultElementQuery(Class<T> elementType) {
      this(elementType, null, false, false, false, false, false, false, false, false, null, null, null, null, null);
   }

   DefaultElementQuery(
      Class<T> elementType,
      ClassElement onlyAccessibleType,
      boolean onlyDeclared,
      boolean onlyAbstract,
      boolean onlyConcrete,
      boolean onlyInjected,
      boolean onlyInstance,
      boolean includeEnumConstants,
      boolean includeOverriddenMethods,
      boolean includeHiddenElements,
      List<Predicate<AnnotationMetadata>> annotationPredicates,
      List<Predicate<Set<ElementModifier>>> modifiersPredicates,
      List<Predicate<T>> elementPredicates,
      List<Predicate<String>> namePredicates,
      List<Predicate<ClassElement>> typePredicates
   ) {
      this.elementType = elementType;
      this.onlyAccessibleType = onlyAccessibleType;
      this.onlyDeclared = onlyDeclared;
      this.onlyAbstract = onlyAbstract;
      this.onlyConcrete = onlyConcrete;
      this.onlyInjected = onlyInjected;
      this.namePredicates = namePredicates;
      this.annotationPredicates = annotationPredicates;
      this.modifiersPredicates = modifiersPredicates;
      this.elementPredicates = elementPredicates;
      this.onlyInstance = onlyInstance;
      this.includeEnumConstants = includeEnumConstants;
      this.includeOverriddenMethods = includeOverriddenMethods;
      this.includeHiddenElements = includeHiddenElements;
      this.typePredicates = typePredicates;
   }

   @Override
   public boolean isOnlyAbstract() {
      return this.onlyAbstract;
   }

   @Override
   public boolean isOnlyInjected() {
      return this.onlyInjected;
   }

   @Override
   public boolean isOnlyConcrete() {
      return this.onlyConcrete;
   }

   @Override
   public Class<T> getElementType() {
      return this.elementType;
   }

   @Override
   public boolean isOnlyAccessible() {
      return this.onlyAccessibleType != null;
   }

   @Override
   public Optional<ClassElement> getOnlyAccessibleFromType() {
      return this.onlyAccessibleType != ONLY_ACCESSIBLE_MARKER ? Optional.ofNullable(this.onlyAccessibleType) : Optional.empty();
   }

   @Override
   public boolean isOnlyDeclared() {
      return this.onlyDeclared;
   }

   @Override
   public boolean isOnlyInstance() {
      return this.onlyInstance;
   }

   @Override
   public boolean isIncludeEnumConstants() {
      return this.includeEnumConstants;
   }

   @Override
   public boolean isIncludeOverriddenMethods() {
      return this.includeOverriddenMethods;
   }

   @Override
   public boolean isIncludeHiddenElements() {
      return this.includeHiddenElements;
   }

   @Override
   public List<Predicate<String>> getNamePredicates() {
      return this.namePredicates == null ? Collections.emptyList() : Collections.unmodifiableList(this.namePredicates);
   }

   @NonNull
   @Override
   public List<Predicate<ClassElement>> getTypePredicates() {
      return this.typePredicates == null ? Collections.emptyList() : Collections.unmodifiableList(this.typePredicates);
   }

   @Override
   public List<Predicate<AnnotationMetadata>> getAnnotationPredicates() {
      return this.annotationPredicates == null ? Collections.emptyList() : Collections.unmodifiableList(this.annotationPredicates);
   }

   @Override
   public List<Predicate<Set<ElementModifier>>> getModifierPredicates() {
      return this.modifiersPredicates == null ? Collections.emptyList() : Collections.unmodifiableList(this.modifiersPredicates);
   }

   @Override
   public List<Predicate<T>> getElementPredicates() {
      return this.elementPredicates == null ? Collections.emptyList() : Collections.unmodifiableList(this.elementPredicates);
   }

   @NonNull
   @Override
   public ElementQuery<T> onlyDeclared() {
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         true,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @Override
   public ElementQuery<T> onlyInjected() {
      List<Predicate<AnnotationMetadata>> annotationPredicates = this.annotationPredicates != null
         ? new ArrayList(this.annotationPredicates)
         : new ArrayList(1);
      annotationPredicates.add(
         (Predicate)metadata -> metadata.hasDeclaredAnnotation("javax.inject.Inject")
               || metadata.hasDeclaredStereotype("javax.inject.Qualifier") && !metadata.hasDeclaredAnnotation(Bean.class)
               || metadata.hasDeclaredAnnotation("javax.annotation.PreDestroy")
               || metadata.hasDeclaredAnnotation("javax.annotation.PostConstruct")
      );
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         true,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> onlyConcrete() {
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         true,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> onlyAbstract() {
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         true,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> onlyAccessible() {
      return new DefaultElementQuery<>(
         this.elementType,
         ONLY_ACCESSIBLE_MARKER,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> onlyAccessible(ClassElement fromType) {
      return new DefaultElementQuery<>(
         this.elementType,
         fromType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @Override
   public ElementQuery<T> onlyInstance() {
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         true,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @Override
   public ElementQuery<T> includeEnumConstants() {
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         true,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @Override
   public ElementQuery<T> includeOverriddenMethods() {
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         true,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @Override
   public ElementQuery<T> includeHiddenElements() {
      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         true,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> named(@NonNull Predicate<String> predicate) {
      Objects.requireNonNull(predicate, "Predicate cannot be null");
      List<Predicate<String>> namePredicates;
      if (this.namePredicates != null) {
         namePredicates = new ArrayList(this.namePredicates);
         namePredicates.add(predicate);
      } else {
         namePredicates = Collections.singletonList(predicate);
      }

      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> typed(@NonNull Predicate<ClassElement> predicate) {
      Objects.requireNonNull(predicate, "Predicate cannot be null");
      List<Predicate<ClassElement>> typePredicates;
      if (this.typePredicates != null) {
         typePredicates = new ArrayList(this.typePredicates);
         typePredicates.add(predicate);
      } else {
         typePredicates = Collections.singletonList(predicate);
      }

      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> annotated(@NonNull Predicate<AnnotationMetadata> predicate) {
      Objects.requireNonNull(predicate, "Predicate cannot be null");
      List<Predicate<AnnotationMetadata>> annotationPredicates;
      if (this.annotationPredicates != null) {
         annotationPredicates = new ArrayList(this.annotationPredicates);
         annotationPredicates.add(predicate);
      } else {
         annotationPredicates = Collections.singletonList(predicate);
      }

      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         annotationPredicates,
         this.modifiersPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> modifiers(@NonNull Predicate<Set<ElementModifier>> predicate) {
      Objects.requireNonNull(predicate, "Predicate cannot be null");
      List<Predicate<Set<ElementModifier>>> modifierPredicates;
      if (this.modifiersPredicates != null) {
         modifierPredicates = new ArrayList(this.modifiersPredicates);
         modifierPredicates.add(predicate);
      } else {
         modifierPredicates = Collections.singletonList(predicate);
      }

      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         modifierPredicates,
         this.elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery<T> filter(@NonNull Predicate<T> predicate) {
      Objects.requireNonNull(predicate, "Predicate cannot be null");
      List<Predicate<T>> elementPredicates;
      if (this.elementPredicates != null) {
         elementPredicates = new ArrayList(this.elementPredicates);
         elementPredicates.add(predicate);
      } else {
         elementPredicates = Collections.singletonList(predicate);
      }

      return new DefaultElementQuery<>(
         this.elementType,
         this.onlyAccessibleType,
         this.onlyDeclared,
         this.onlyAbstract,
         this.onlyConcrete,
         this.onlyInjected,
         this.onlyInstance,
         this.includeEnumConstants,
         this.includeOverriddenMethods,
         this.includeHiddenElements,
         this.annotationPredicates,
         this.modifiersPredicates,
         elementPredicates,
         this.namePredicates,
         this.typePredicates
      );
   }

   @NonNull
   @Override
   public ElementQuery.Result<T> result() {
      return this;
   }
}
