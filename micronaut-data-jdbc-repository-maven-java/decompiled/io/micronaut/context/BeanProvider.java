package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface BeanProvider<T> extends Iterable<T> {
   @NonNull
   T get();

   default Optional<T> find(@Nullable Qualifier<T> qualifier) {
      return this.isPresent() ? Optional.of(this.get()) : Optional.empty();
   }

   @NonNull
   default BeanDefinition<T> getDefinition() {
      throw new UnsupportedOperationException("BeanDefinition information can only be obtained from dependency injected providers");
   }

   @NonNull
   default T get(@Nullable Qualifier<T> qualifier) {
      return this.get();
   }

   @NonNull
   default Iterator<T> iterator() {
      return Collections.singletonList(this.get()).iterator();
   }

   default Stream<T> stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }

   default boolean isUnique() {
      return true;
   }

   default boolean isPresent() {
      return true;
   }

   default boolean isResolvable() {
      return this.isUnique() && this.isPresent();
   }

   default void ifPresent(@NonNull Consumer<T> consumer) {
      if (this.isPresent()) {
         ((Consumer)Objects.requireNonNull(consumer, "Consumer cannot be null")).accept(this.get());
      }

   }

   default void ifResolvable(@NonNull Consumer<T> consumer) {
      if (this.isResolvable()) {
         ((Consumer)Objects.requireNonNull(consumer, "Consumer cannot be null")).accept(this.get());
      }

   }

   @Nullable
   default T orElse(@Nullable T alternative) {
      return (T)(this.isPresent() ? this.get() : alternative);
   }

   @NonNull
   static <T1> Argument<BeanProvider<T1>> argumentOf(@NonNull Class<T1> type) {
      return Argument.of(BeanProvider.class, (Class)Objects.requireNonNull(type, "Type cannot be null"));
   }
}
