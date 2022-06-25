package io.micronaut.jackson.databind;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import io.micronaut.jackson.ObjectMapperFactory;
import io.micronaut.jackson.codec.JacksonFeatures;
import io.micronaut.jackson.core.parser.JacksonCoreProcessor;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.jackson.core.tree.TreeGenerator;
import io.micronaut.json.JsonFeatures;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.JsonStreamConfig;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;

@Internal
@Singleton
@BootstrapContextCompatible
public final class JacksonDatabindMapper implements JsonMapper {
   private final ObjectMapper objectMapper;
   private final JsonStreamConfig config;
   private final JsonNodeTreeCodec treeCodec;

   @Inject
   @Internal
   public JacksonDatabindMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      this.config = JsonStreamConfig.DEFAULT
         .withUseBigDecimalForFloats(objectMapper.getDeserializationConfig().isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS))
         .withUseBigIntegerForInts(objectMapper.getDeserializationConfig().isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS));
      this.treeCodec = JsonNodeTreeCodec.getInstance().withConfig(this.config);
   }

   @Internal
   public JacksonDatabindMapper() {
      this(new ObjectMapperFactory().objectMapper(null, null));
   }

   @Internal
   public ObjectMapper getObjectMapper() {
      return this.objectMapper;
   }

   @Override
   public <T> T readValueFromTree(@NonNull JsonNode tree, @NonNull Argument<T> type) throws IOException {
      JsonParser tokens = this.treeAsTokens(tree);
      JavaType javaType = JacksonConfiguration.constructType(type, this.objectMapper.getTypeFactory());
      Optional<Class> view = type.getAnnotationMetadata().classValue(JsonView.class);
      return (T)(view.isPresent()
         ? this.objectMapper.readerWithView((Class<?>)view.get()).readValue(tokens, javaType)
         : this.objectMapper.readValue(tokens, javaType));
   }

   @NonNull
   @Override
   public JsonNode writeValueToTree(@Nullable Object value) throws IOException {
      TreeGenerator treeGenerator = this.treeCodec.createTreeGenerator();
      treeGenerator.setCodec(this.objectMapper);
      this.objectMapper.writeValue(treeGenerator, value);
      return treeGenerator.getCompletedValue();
   }

   @NonNull
   @Override
   public <T> JsonNode writeValueToTree(@NonNull Argument<T> type, T value) throws IOException {
      return this.writeValueToTree(value);
   }

   @Override
   public <T> T readValue(@NonNull InputStream inputStream, @NonNull Argument<T> type) throws IOException {
      return this.objectMapper.readValue(inputStream, JacksonConfiguration.constructType(type, this.objectMapper.getTypeFactory()));
   }

   @Override
   public <T> T readValue(@NonNull byte[] byteArray, @NonNull Argument<T> type) throws IOException {
      return this.objectMapper.readValue(byteArray, JacksonConfiguration.constructType(type, this.objectMapper.getTypeFactory()));
   }

   @Override
   public void writeValue(@NonNull OutputStream outputStream, @Nullable Object object) throws IOException {
      this.objectMapper.writeValue(outputStream, object);
   }

   @Override
   public <T> void writeValue(@NonNull OutputStream outputStream, @NonNull Argument<T> type, T object) throws IOException {
      this.writeValue(outputStream, object);
   }

   @Override
   public byte[] writeValueAsBytes(@Nullable Object object) throws IOException {
      return this.objectMapper.writeValueAsBytes(object);
   }

   @Override
   public <T> byte[] writeValueAsBytes(@NonNull Argument<T> type, T object) throws IOException {
      return this.writeValueAsBytes(object);
   }

   @Override
   public void updateValueFromTree(Object value, @NonNull JsonNode tree) throws IOException {
      this.objectMapper.readerForUpdating(value).readValue(this.treeAsTokens(tree));
   }

   @NonNull
   @Override
   public JsonMapper cloneWithFeatures(@NonNull JsonFeatures features) {
      JacksonFeatures jacksonFeatures = (JacksonFeatures)features;
      ObjectMapper objectMapper = this.objectMapper.copy();
      jacksonFeatures.getDeserializationFeatures().forEach(objectMapper::configure);
      jacksonFeatures.getSerializationFeatures().forEach(objectMapper::configure);

      for(Class<? extends Module> moduleClass : jacksonFeatures.getAdditionalModules()) {
         objectMapper.registerModule(InstantiationUtils.instantiate(moduleClass));
      }

      return new JacksonDatabindMapper(objectMapper);
   }

   @NonNull
   @Override
   public JsonMapper cloneWithViewClass(@NonNull Class<?> viewClass) {
      ObjectMapper objectMapper = this.objectMapper.copy();
      objectMapper.setConfig(objectMapper.getSerializationConfig().withView(viewClass));
      objectMapper.setConfig(objectMapper.getDeserializationConfig().withView(viewClass));
      return new JacksonDatabindMapper(objectMapper);
   }

   @NonNull
   @Override
   public JsonStreamConfig getStreamConfig() {
      return this.config;
   }

   @NonNull
   @Override
   public Processor<byte[], JsonNode> createReactiveParser(@NonNull Consumer<Processor<byte[], JsonNode>> onSubscribe, boolean streamArray) {
      return new JacksonCoreProcessor(streamArray, this.objectMapper.getFactory(), this.config) {
         @Override
         public void subscribe(Subscriber<? super JsonNode> downstreamSubscriber) {
            onSubscribe.accept(this);
            super.subscribe(downstreamSubscriber);
         }
      };
   }

   @NonNull
   @Override
   public Optional<JsonFeatures> detectFeatures(@NonNull AnnotationMetadata annotations) {
      return Optional.ofNullable(annotations.getAnnotation(io.micronaut.jackson.annotation.JacksonFeatures.class)).map(JacksonFeatures::fromAnnotation);
   }

   private JsonParser treeAsTokens(@NonNull JsonNode tree) {
      JsonParser parser = this.treeCodec.treeAsTokens(tree);
      parser.setCodec(this.objectMapper);
      return parser;
   }
}
