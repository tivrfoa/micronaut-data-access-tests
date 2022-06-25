package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

@Internal
class DefaultArgumentValue<V> implements ArgumentValue<V> {
   private final Argument<V> argument;
   private final V value;

   DefaultArgumentValue(Argument<V> argument, V value) {
      this.argument = argument;
      this.value = value;
   }

   @Override
   public String getName() {
      return this.argument.getName();
   }

   @Override
   public Class<V> getType() {
      return this.argument.getType();
   }

   @Override
   public Optional<Argument<?>> getFirstTypeVariable() {
      return this.argument.getFirstTypeVariable();
   }

   @Override
   public Argument[] getTypeParameters() {
      return this.argument.getTypeParameters();
   }

   @Override
   public Map<String, Argument<?>> getTypeVariables() {
      return this.argument.getTypeVariables();
   }

   @Override
   public V getValue() {
      return this.value;
   }

   @Override
   public <T extends Annotation> T synthesize(Class<T> annotationClass) {
      return this.argument.synthesize(annotationClass);
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      return this.argument.synthesize(annotationClass, sourceAnnotation);
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      return this.argument.synthesizeDeclared(annotationClass, sourceAnnotation);
   }

   @Override
   public Annotation[] synthesizeAll() {
      return this.argument.synthesizeAll();
   }

   @Override
   public Annotation[] synthesizeDeclared() {
      return this.argument.synthesizeDeclared();
   }

   @Override
   public boolean equalsType(@Nullable Argument<?> o) {
      return this.argument.equalsType(o);
   }

   @Override
   public int typeHashCode() {
      return this.argument.typeHashCode();
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.argument.getAnnotationMetadata();
   }
}
