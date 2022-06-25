package io.micronaut.json;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;
import org.reactivestreams.Processor;

public interface JsonMapper {
   <T> T readValueFromTree(@NonNull JsonNode tree, @NonNull Argument<T> type) throws IOException;

   default <T> T readValueFromTree(@NonNull JsonNode tree, @NonNull Class<T> type) throws IOException {
      return this.readValueFromTree(tree, Argument.of(type));
   }

   <T> T readValue(@NonNull InputStream inputStream, @NonNull Argument<T> type) throws IOException;

   <T> T readValue(@NonNull byte[] byteArray, @NonNull Argument<T> type) throws IOException;

   default <T> T readValue(@NonNull String string, @NonNull Argument<T> type) throws IOException {
      return this.readValue(string.getBytes(StandardCharsets.UTF_8), type);
   }

   @NonNull
   Processor<byte[], JsonNode> createReactiveParser(@NonNull Consumer<Processor<byte[], JsonNode>> onSubscribe, boolean streamArray);

   @NonNull
   JsonNode writeValueToTree(@Nullable Object value) throws IOException;

   @NonNull
   <T> JsonNode writeValueToTree(@NonNull Argument<T> type, @Nullable T value) throws IOException;

   void writeValue(@NonNull OutputStream outputStream, @Nullable Object object) throws IOException;

   <T> void writeValue(@NonNull OutputStream outputStream, @NonNull Argument<T> type, @Nullable T object) throws IOException;

   byte[] writeValueAsBytes(@Nullable Object object) throws IOException;

   <T> byte[] writeValueAsBytes(@NonNull Argument<T> type, @Nullable T object) throws IOException;

   default void updateValueFromTree(Object value, @NonNull JsonNode tree) throws IOException {
      throw new UnsupportedOperationException();
   }

   @NonNull
   default JsonMapper cloneWithFeatures(@NonNull JsonFeatures features) {
      throw new UnsupportedOperationException();
   }

   @NonNull
   default Optional<JsonFeatures> detectFeatures(@NonNull AnnotationMetadata annotations) {
      return Optional.empty();
   }

   @NonNull
   default JsonMapper cloneWithViewClass(@NonNull Class<?> viewClass) {
      throw new UnsupportedOperationException();
   }

   @NonNull
   JsonStreamConfig getStreamConfig();
}
