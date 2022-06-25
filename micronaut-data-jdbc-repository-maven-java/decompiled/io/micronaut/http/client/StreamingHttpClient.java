package io.micronaut.http.client;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import java.net.URL;
import java.util.Map;
import org.reactivestreams.Publisher;

public interface StreamingHttpClient extends HttpClient {
   <I> Publisher<ByteBuffer<?>> dataStream(@NonNull HttpRequest<I> request);

   <I> Publisher<ByteBuffer<?>> dataStream(@NonNull HttpRequest<I> request, @NonNull Argument<?> errorType);

   <I> Publisher<HttpResponse<ByteBuffer<?>>> exchangeStream(@NonNull HttpRequest<I> request);

   <I> Publisher<HttpResponse<ByteBuffer<?>>> exchangeStream(@NonNull HttpRequest<I> request, @NonNull Argument<?> errorType);

   <I> Publisher<Map<String, Object>> jsonStream(@NonNull HttpRequest<I> request);

   <I, O> Publisher<O> jsonStream(@NonNull HttpRequest<I> request, @NonNull Argument<O> type);

   <I, O> Publisher<O> jsonStream(@NonNull HttpRequest<I> request, @NonNull Argument<O> type, @NonNull Argument<?> errorType);

   default <I, O> Publisher<O> jsonStream(@NonNull HttpRequest<I> request, @NonNull Class<O> type) {
      return this.jsonStream(request, Argument.of(type));
   }

   static StreamingHttpClient create(@Nullable URL url) {
      return StreamingHttpClientFactoryResolver.getFactory().createStreamingClient(url);
   }

   static StreamingHttpClient create(@Nullable URL url, @NonNull HttpClientConfiguration configuration) {
      return StreamingHttpClientFactoryResolver.getFactory().createStreamingClient(url, configuration);
   }
}
