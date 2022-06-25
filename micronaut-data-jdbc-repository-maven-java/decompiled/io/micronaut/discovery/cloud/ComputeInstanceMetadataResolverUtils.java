package io.micronaut.discovery.cloud;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

@Internal
public class ComputeInstanceMetadataResolverUtils {
   public static com.fasterxml.jackson.databind.JsonNode readMetadataUrl(
      URL url, int connectionTimeoutMs, int readTimeoutMs, ObjectMapper objectMapper, Map<String, String> requestProperties
   ) throws IOException {
      InputStream in = openMetadataUrl(url, connectionTimeoutMs, readTimeoutMs, requestProperties);
      Throwable var6 = null;

      com.fasterxml.jackson.databind.JsonNode var7;
      try {
         var7 = objectMapper.readTree(in);
      } catch (Throwable var16) {
         var6 = var16;
         throw var16;
      } finally {
         if (in != null) {
            if (var6 != null) {
               try {
                  in.close();
               } catch (Throwable var15) {
                  var6.addSuppressed(var15);
               }
            } else {
               in.close();
            }
         }

      }

      return var7;
   }

   public static JsonNode readMetadataUrl(
      URL url, int connectionTimeoutMs, int readTimeoutMs, JsonNodeTreeCodec treeCodec, JsonFactory jsonFactory, Map<String, String> requestProperties
   ) throws IOException {
      InputStream in = openMetadataUrl(url, connectionTimeoutMs, readTimeoutMs, requestProperties);
      Throwable var7 = null;

      Object var10;
      try {
         JsonParser parser = jsonFactory.createParser(in);
         Throwable var9 = null;

         try {
            var10 = treeCodec.readTree(parser);
         } catch (Throwable var33) {
            var10 = var33;
            var9 = var33;
            throw var33;
         } finally {
            if (parser != null) {
               if (var9 != null) {
                  try {
                     parser.close();
                  } catch (Throwable var32) {
                     var9.addSuppressed(var32);
                  }
               } else {
                  parser.close();
               }
            }

         }
      } catch (Throwable var35) {
         var7 = var35;
         throw var35;
      } finally {
         if (in != null) {
            if (var7 != null) {
               try {
                  in.close();
               } catch (Throwable var31) {
                  var7.addSuppressed(var31);
               }
            } else {
               in.close();
            }
         }

      }

      return (JsonNode)var10;
   }

   public static JsonNode readMetadataUrl(URL url, int connectionTimeoutMs, int readTimeoutMs, JsonMapper jsonMapper, Map<String, String> requestProperties) throws IOException {
      InputStream in = openMetadataUrl(url, connectionTimeoutMs, readTimeoutMs, requestProperties);
      Throwable var6 = null;

      JsonNode var7;
      try {
         var7 = jsonMapper.readValue(in, Argument.of(JsonNode.class));
      } catch (Throwable var16) {
         var6 = var16;
         throw var16;
      } finally {
         if (in != null) {
            if (var6 != null) {
               try {
                  in.close();
               } catch (Throwable var15) {
                  var6.addSuppressed(var15);
               }
            } else {
               in.close();
            }
         }

      }

      return var7;
   }

   private static InputStream openMetadataUrl(URL url, int connectionTimeoutMs, int readTimeoutMs, Map<String, String> requestProperties) throws IOException {
      URLConnection urlConnection = url.openConnection();
      if (url.getProtocol().equalsIgnoreCase("file")) {
         urlConnection.connect();
         return urlConnection.getInputStream();
      } else {
         HttpURLConnection uc = (HttpURLConnection)urlConnection;
         uc.setConnectTimeout(connectionTimeoutMs);
         requestProperties.forEach(uc::setRequestProperty);
         uc.setReadTimeout(readTimeoutMs);
         uc.setRequestMethod(HttpMethod.GET.name());
         uc.setDoOutput(true);
         return uc.getInputStream();
      }
   }

   public static void populateMetadata(AbstractComputeInstanceMetadata instanceMetadata, JsonNode metadata) {
      if (metadata != null) {
         Map<String, String> finalMetadata = new HashMap(metadata.size());

         for(Entry<String, JsonNode> entry : metadata.entries()) {
            JsonNode value = (JsonNode)entry.getValue();
            if (value.isString()) {
               finalMetadata.put(entry.getKey(), value.getStringValue());
            }
         }

         instanceMetadata.setMetadata(finalMetadata);
      }

   }

   public static Optional<String> stringValue(com.fasterxml.jackson.databind.JsonNode json, String key) {
      return Optional.ofNullable(json.findValue(key)).map(com.fasterxml.jackson.databind.JsonNode::asText);
   }

   public static void populateMetadata(AbstractComputeInstanceMetadata instanceMetadata, Map<?, ?> metadata) {
      if (metadata != null) {
         Map<String, String> finalMetadata = new HashMap(metadata.size());

         for(Entry<?, ?> entry : metadata.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
               finalMetadata.put(key.toString(), value.toString());
            }
         }

         instanceMetadata.setMetadata(finalMetadata);
      }

   }
}
