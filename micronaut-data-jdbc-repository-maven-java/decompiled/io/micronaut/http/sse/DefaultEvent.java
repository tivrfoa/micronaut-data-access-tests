package io.micronaut.http.sse;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.annotation.Produces;
import java.time.Duration;

@Internal
@Produces({"text/event-stream"})
class DefaultEvent<T> implements Event<T> {
   private final T data;
   private String id;
   private String name;
   private String comment;
   private Duration retry;

   DefaultEvent(T data) {
      this.data = data;
   }

   @Override
   public T getData() {
      return this.data;
   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getComment() {
      return this.comment;
   }

   @Override
   public Duration getRetry() {
      return this.retry;
   }

   @Override
   public Event<T> retry(Duration duration) {
      this.retry = duration;
      return this;
   }

   @Override
   public Event<T> id(String id) {
      this.id = id;
      return this;
   }

   @Override
   public Event<T> name(String name) {
      this.name = name;
      return this;
   }

   @Override
   public Event<T> comment(String comment) {
      this.comment = comment;
      return this;
   }
}
