package io.micronaut.jackson.core.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.json.async.NonBlockingJsonParser;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.processor.SingleThreadedBufferingProcessor;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.jackson.core.tree.JsonStreamTransfer;
import io.micronaut.jackson.core.tree.TreeGenerator;
import io.micronaut.json.JsonStreamConfig;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.util.Optional;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class JacksonCoreProcessor extends SingleThreadedBufferingProcessor<byte[], JsonNode> {
   private static final Logger LOG = LoggerFactory.getLogger(JacksonCoreProcessor.class);
   private NonBlockingJsonParser currentNonBlockingJsonParser;
   private TreeGenerator currentGenerator = null;
   private final JsonFactory jsonFactory;
   private final JsonStreamConfig deserializationConfig;
   private final JsonNodeTreeCodec treeCodec;
   private final boolean streamArray;
   private boolean started;
   private boolean rootIsArray;
   private boolean jsonStream;

   public JacksonCoreProcessor(boolean streamArray, JsonFactory jsonFactory, @NonNull JsonStreamConfig deserializationConfig) {
      this.jsonFactory = jsonFactory;
      this.streamArray = streamArray;
      this.treeCodec = JsonNodeTreeCodec.getInstance().withConfig(deserializationConfig);
      this.jsonStream = true;
      this.deserializationConfig = deserializationConfig;

      try {
         this.currentNonBlockingJsonParser = (NonBlockingJsonParser)jsonFactory.createNonBlockingByteArrayParser();
      } catch (IOException var5) {
         throw new IllegalStateException("Failed to create non-blocking JSON parser: " + var5.getMessage(), var5);
      }
   }

   public boolean needMoreInput() {
      return this.currentNonBlockingJsonParser.getNonBlockingInputFeeder().needMoreInput();
   }

   @Override
   protected void doOnComplete() {
      if (this.jsonStream && this.currentGenerator == null) {
         super.doOnComplete();
      } else if (this.needMoreInput()) {
         this.doOnError(new JsonEOFException(this.currentNonBlockingJsonParser, JsonToken.NOT_AVAILABLE, "Unexpected end-of-input"));
      } else {
         super.doOnComplete();
      }

   }

   protected void onUpstreamMessage(byte[] message) {
      if (LOG.isTraceEnabled()) {
         LOG.trace("Received upstream bytes of length: " + message.length);
      }

      try {
         if (message.length == 0) {
            if (this.needMoreInput()) {
               this.requestMoreInput();
            }

            return;
         }

         ByteArrayFeeder byteFeeder = this.byteFeeder(message);

         JsonToken event;
         while((event = this.currentNonBlockingJsonParser.nextToken()) != JsonToken.NOT_AVAILABLE) {
            if (!this.started) {
               this.started = true;
               if (this.streamArray && event == JsonToken.START_ARRAY) {
                  this.rootIsArray = true;
                  this.jsonStream = false;
                  continue;
               }
            }

            if (this.currentGenerator == null) {
               if (event == JsonToken.END_ARRAY && this.rootIsArray) {
                  byteFeeder.endOfInput();
                  break;
               }

               this.currentGenerator = this.treeCodec.createTreeGenerator();
            }

            JsonStreamTransfer.transferCurrentToken(this.currentNonBlockingJsonParser, this.currentGenerator, this.deserializationConfig);
            if (this.currentGenerator.isComplete()) {
               this.publishNode(this.currentGenerator.getCompletedValue());
               this.currentGenerator = null;
            }
         }

         if (this.jsonStream) {
            if (this.currentGenerator == null) {
               byteFeeder.endOfInput();
            }

            this.requestMoreInput();
         } else if (this.needMoreInput()) {
            this.requestMoreInput();
         }
      } catch (IOException var4) {
         this.onError(var4);
      }

   }

   private void publishNode(final JsonNode root) {
      Optional<Subscriber<? super JsonNode>> opt = this.currentDownstreamSubscriber();
      if (opt.isPresent()) {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Materialized new JsonNode call onNext...");
         }

         ((Subscriber)opt.get()).onNext(root);
      }

   }

   private void requestMoreInput() {
      if (LOG.isTraceEnabled()) {
         LOG.trace("More input required to parse JSON. Demanding more.");
      }

      this.upstreamSubscription.request(1L);
      ++this.upstreamDemand;
   }

   private ByteArrayFeeder byteFeeder(byte[] message) throws IOException {
      ByteArrayFeeder byteFeeder = this.currentNonBlockingJsonParser.getNonBlockingInputFeeder();
      boolean needMoreInput = byteFeeder.needMoreInput();
      if (!needMoreInput) {
         this.currentNonBlockingJsonParser = (NonBlockingJsonParser)this.jsonFactory.createNonBlockingByteArrayParser();
         byteFeeder = this.currentNonBlockingJsonParser.getNonBlockingInputFeeder();
      }

      byteFeeder.feedInput(message, 0, message.length);
      return byteFeeder;
   }
}
