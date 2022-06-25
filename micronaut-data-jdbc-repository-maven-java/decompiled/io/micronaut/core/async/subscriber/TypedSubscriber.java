package io.micronaut.core.async.subscriber;

import io.micronaut.core.type.Argument;

public abstract class TypedSubscriber<T> extends CompletionAwareSubscriber<T> {
   private final Argument<T> typeArgument;

   public TypedSubscriber(Argument<T> typeArgument) {
      this.typeArgument = typeArgument;
   }

   public Argument<T> getTypeArgument() {
      return this.typeArgument;
   }
}
