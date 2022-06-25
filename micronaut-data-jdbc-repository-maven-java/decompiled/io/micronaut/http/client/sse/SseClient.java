package io.micronaut.http.client.sse;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.sse.Event;
import java.net.URL;
import org.reactivestreams.Publisher;

public interface SseClient {
   <I> Publisher<Event<ByteBuffer<?>>> eventStream(@NonNull HttpRequest<I> request);

   <I, B> Publisher<Event<B>> eventStream(@NonNull HttpRequest<I> request, @NonNull Argument<B> eventType);

   <I, B> Publisher<Event<B>> eventStream(@NonNull HttpRequest<I> request, @NonNull Argument<B> eventType, @NonNull Argument<?> errorType);

   default <I, B> Publisher<Event<B>> eventStream(@NonNull HttpRequest<I> request, @NonNull Class<B> eventType) {
      return this.eventStream(request, Argument.of(eventType));
   }

   default <B> Publisher<Event<B>> eventStream(@NonNull String uri, @NonNull Class<B> eventType) {
      return this.eventStream(HttpRequest.GET(uri), Argument.of(eventType));
   }

   default <B> Publisher<Event<B>> eventStream(@NonNull String uri, @NonNull Argument<B> eventType) {
      return this.eventStream(HttpRequest.GET(uri), eventType);
   }

   static SseClient create(@Nullable URL url) {
      return SseClientFactoryResolver.getFactory().createSseClient(url);
   }

   static SseClient create(@Nullable URL url, @NonNull HttpClientConfiguration configuration) {
      return SseClientFactoryResolver.getFactory().createSseClient(url, configuration);
   }
}
