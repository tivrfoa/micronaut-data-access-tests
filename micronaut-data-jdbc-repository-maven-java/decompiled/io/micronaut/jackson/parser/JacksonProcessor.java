package io.micronaut.jackson.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.json.async.NonBlockingJsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.processor.SingleThreadedBufferingProcessor;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JacksonProcessor extends SingleThreadedBufferingProcessor<byte[], JsonNode> {
   private static final Logger LOG = LoggerFactory.getLogger(JacksonProcessor.class);
   private NonBlockingJsonParser currentNonBlockingJsonParser;
   private final ConcurrentLinkedDeque<JsonNode> nodeStack = new ConcurrentLinkedDeque();
   private final JsonFactory jsonFactory;
   @Nullable
   private final DeserializationConfig deserializationConfig;
   private final JsonNodeFactory nodeFactory;
   private String currentFieldName;
   private boolean streamArray;
   private boolean rootIsArray;
   private boolean jsonStream;

   public JacksonProcessor(JsonFactory jsonFactory, boolean streamArray, @Nullable DeserializationConfig deserializationConfig) {
      this.jsonFactory = jsonFactory;
      this.deserializationConfig = deserializationConfig;
      this.streamArray = streamArray;
      this.jsonStream = true;
      this.nodeFactory = deserializationConfig == null ? JsonNodeFactory.instance : deserializationConfig.getNodeFactory();

      try {
         this.currentNonBlockingJsonParser = (NonBlockingJsonParser)jsonFactory.createNonBlockingByteArrayParser();
      } catch (IOException var5) {
         throw new IllegalStateException("Failed to create non-blocking JSON parser: " + var5.getMessage(), var5);
      }
   }

   public JacksonProcessor(JsonFactory jsonFactory, boolean streamArray) {
      this(jsonFactory, streamArray, null);
   }

   public JacksonProcessor(JsonFactory jsonFactory, DeserializationConfig deserializationConfig) {
      this(jsonFactory, false, deserializationConfig);
   }

   public JacksonProcessor(JsonFactory jsonFactory) {
      this(jsonFactory, false, null);
   }

   public JacksonProcessor(DeserializationConfig deserializationConfig) {
      this(new JsonFactory(), deserializationConfig);
   }

   public JacksonProcessor() {
      this(new JsonFactory(), null);
   }

   public boolean needMoreInput() {
      return this.currentNonBlockingJsonParser.getNonBlockingInputFeeder().needMoreInput();
   }

   @Override
   protected void doOnComplete() {
      if (this.jsonStream && this.nodeStack.isEmpty()) {
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
         JsonToken event = this.currentNonBlockingJsonParser.nextToken();
         this.checkForStreaming(event);

         for(; event != JsonToken.NOT_AVAILABLE; event = this.currentNonBlockingJsonParser.nextToken()) {
            JsonNode root = this.asJsonNode(event);
            if (root != null) {
               boolean isLast = this.nodeStack.isEmpty() && !this.jsonStream;
               if (isLast) {
                  byteFeeder.endOfInput();
                  if (this.streamArray && root.isArray()) {
                     break;
                  }
               }

               this.publishNode(root);
               if (isLast) {
                  break;
               }
            }
         }

         if (this.jsonStream) {
            if (this.nodeStack.isEmpty()) {
               byteFeeder.endOfInput();
            }

            this.requestMoreInput();
         } else if (this.needMoreInput()) {
            this.requestMoreInput();
         }
      } catch (IOException var6) {
         this.onError(var6);
      }

   }

   private void checkForStreaming(JsonToken event) {
      if (event == JsonToken.START_ARRAY && this.nodeStack.peekFirst() == null) {
         this.rootIsArray = true;
         this.jsonStream = false;
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

   private JsonNode asJsonNode(JsonToken event) throws IOException {
      switch(event) {
         case START_OBJECT:
            this.nodeStack.push(this.node((JsonNode)this.nodeStack.peekFirst()));
            break;
         case START_ARRAY:
            JsonNode node = (JsonNode)this.nodeStack.peekFirst();
            if (node == null) {
               this.rootIsArray = true;
            }

            this.nodeStack.push(this.array(node));
            break;
         case END_OBJECT:
         case END_ARRAY:
            this.checkEmptyNodeStack(event);
            JsonNode current = (JsonNode)this.nodeStack.pop();
            if (this.nodeStack.isEmpty()) {
               return current;
            }

            if (this.streamArray && this.nodeStack.size() == 1) {
               return ((JsonNode)this.nodeStack.peekFirst()).isArray() ? current : null;
            }

            return null;
         case FIELD_NAME:
            this.checkEmptyNodeStack(event);
            this.currentFieldName = this.currentNonBlockingJsonParser.getCurrentName();
            break;
         case VALUE_NUMBER_INT:
            this.checkEmptyNodeStack(event);
            this.addIntegerNumber((JsonNode)this.nodeStack.peekFirst());
            break;
         case VALUE_STRING:
            this.checkEmptyNodeStack(event);
            this.addJsonNode((JsonNode)this.nodeStack.peekFirst(), this.nodeFactory.textNode(this.currentNonBlockingJsonParser.getValueAsString()));
            break;
         case VALUE_NUMBER_FLOAT:
            this.checkEmptyNodeStack(event);
            this.addFloatNumber((JsonNode)this.nodeStack.peekFirst());
            break;
         case VALUE_NULL:
            this.checkEmptyNodeStack(event);
            this.addJsonNode((JsonNode)this.nodeStack.peekFirst(), this.nodeFactory.nullNode());
            break;
         case VALUE_TRUE:
         case VALUE_FALSE:
            this.checkEmptyNodeStack(event);
            this.addJsonNode((JsonNode)this.nodeStack.peekFirst(), this.nodeFactory.booleanNode(this.currentNonBlockingJsonParser.getBooleanValue()));
            break;
         default:
            throw new IllegalStateException("Unsupported JSON event: " + event);
      }

      if (this.rootIsArray && this.streamArray && this.nodeStack.size() == 1) {
         ArrayNode arrayNode = (ArrayNode)this.nodeStack.peekFirst();
         if (arrayNode.size() > 0) {
            return arrayNode.remove(arrayNode.size() - 1);
         }
      }

      return null;
   }

   private static String tokenType(JsonToken token) {
      switch(token) {
         case END_OBJECT:
         case END_ARRAY:
            return "container end";
         case FIELD_NAME:
            return "field";
         case VALUE_NUMBER_INT:
            return "integer";
         case VALUE_STRING:
            return "string";
         case VALUE_NUMBER_FLOAT:
            return "float";
         case VALUE_NULL:
            return "null";
         case VALUE_TRUE:
         case VALUE_FALSE:
            return "boolean";
         default:
            return "";
      }
   }

   private void addIntegerNumber(final JsonNode integerNode) throws IOException {
      if (this.useBigIntegerForInts()) {
         this.addJsonNode(integerNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getBigIntegerValue()));
      } else {
         JsonParser.NumberType numberIntType = this.currentNonBlockingJsonParser.getNumberType();
         switch(numberIntType) {
            case BIG_INTEGER:
               this.addJsonNode(integerNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getBigIntegerValue()));
               break;
            case LONG:
               this.addJsonNode(integerNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getLongValue()));
               break;
            case INT:
               this.addJsonNode(integerNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getIntValue()));
               break;
            default:
               throw new IllegalStateException("Unsupported number type: " + numberIntType);
         }
      }

   }

   private void addFloatNumber(final JsonNode decimalNode) throws IOException {
      if (this.useBigDecimalForFloats()) {
         this.addJsonNode(decimalNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getDecimalValue()));
      } else {
         JsonParser.NumberType numberDecimalType = this.currentNonBlockingJsonParser.getNumberType();
         switch(numberDecimalType) {
            case FLOAT:
               this.addJsonNode(decimalNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getFloatValue()));
               break;
            case DOUBLE:
               this.addJsonNode(decimalNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getDoubleValue()));
               break;
            case BIG_DECIMAL:
               this.addJsonNode(decimalNode, this.nodeFactory.numberNode(this.currentNonBlockingJsonParser.getDecimalValue()));
               break;
            default:
               throw new IllegalStateException("Unsupported number type: " + numberDecimalType);
         }
      }

   }

   private void checkEmptyNodeStack(JsonToken token) throws JsonParseException {
      if (this.nodeStack.isEmpty()) {
         throw new JsonParseException(this.currentNonBlockingJsonParser, "Unexpected " + tokenType(token) + " literal");
      }
   }

   private boolean useBigDecimalForFloats() {
      return this.deserializationConfig != null && this.deserializationConfig.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
   }

   private boolean useBigIntegerForInts() {
      return this.deserializationConfig != null && this.deserializationConfig.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
   }

   private void addJsonNode(JsonNode node, JsonNode value) {
      if (node.isObject()) {
         ((ObjectNode)node).set(this.currentFieldName, value);
      } else {
         ((ArrayNode)node).add(value);
      }

   }

   private JsonNode array(JsonNode node) {
      if (node == null) {
         return this.nodeFactory.arrayNode();
      } else {
         return node.isObject() ? ((ObjectNode)node).putArray(this.currentFieldName) : ((ArrayNode)node).addArray();
      }
   }

   private JsonNode node(JsonNode node) {
      if (node == null) {
         return this.nodeFactory.objectNode();
      } else if (node.isObject()) {
         return ((ObjectNode)node).putObject(this.currentFieldName);
      } else {
         return !node.isArray() || this.streamArray && this.nodeStack.size() == 1 ? this.nodeFactory.objectNode() : ((ArrayNode)node).addObject();
      }
   }
}
