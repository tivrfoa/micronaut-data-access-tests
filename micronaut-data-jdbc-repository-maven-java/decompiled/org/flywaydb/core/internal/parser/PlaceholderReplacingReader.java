package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;

public class PlaceholderReplacingReader extends FilterReader {
   private final String prefix;
   private final String suffix;
   private final PlaceholderReplacingReader.CaseInsensitiveMap placeholders = new PlaceholderReplacingReader.CaseInsensitiveMap();
   private final StringBuilder buffer = new StringBuilder();
   private String markBuffer;
   private String replacement;
   private int replacementPos;
   private String markReplacement;
   private int markReplacementPos;

   public PlaceholderReplacingReader(String prefix, String suffix, Map<String, String> placeholders, Reader in) {
      super(in);
      this.prefix = prefix;
      this.suffix = suffix;
      this.placeholders.putAll(placeholders);
   }

   public static PlaceholderReplacingReader create(Configuration configuration, ParsingContext parsingContext, Reader reader) {
      Map<String, String> placeholders = new HashMap();
      Map<String, String> configurationPlaceholders = configuration.getPlaceholders();
      Map<String, String> parsingContextPlaceholders = parsingContext.getPlaceholders();
      placeholders.putAll(configurationPlaceholders);
      placeholders.putAll(parsingContextPlaceholders);
      return new PlaceholderReplacingReader(configuration.getPlaceholderPrefix(), configuration.getPlaceholderSuffix(), placeholders, reader);
   }

   public static PlaceholderReplacingReader createForScriptMigration(Configuration configuration, ParsingContext parsingContext, Reader reader) {
      Map<String, String> placeholders = new HashMap();
      Map<String, String> configurationPlaceholders = configuration.getPlaceholders();
      Map<String, String> parsingContextPlaceholders = parsingContext.getPlaceholders();
      placeholders.putAll(configurationPlaceholders);
      placeholders.putAll(parsingContextPlaceholders);
      return new PlaceholderReplacingReader(configuration.getScriptPlaceholderPrefix(), configuration.getScriptPlaceholderSuffix(), placeholders, reader);
   }

   public int read() throws IOException {
      if (this.replacement == null) {
         if (this.buffer.length() > 0) {
            char c = this.buffer.charAt(0);
            this.buffer.deleteCharAt(0);
            return c;
         }

         do {
            int r = super.read();
            if (r == -1) {
               break;
            }

            this.buffer.append((char)r);
         } while(this.buffer.length() < this.prefix.length() && this.endsWith(this.buffer, this.prefix.substring(0, this.buffer.length())));

         if (!this.endsWith(this.buffer, this.prefix)) {
            if (this.buffer.length() > 0) {
               char c = this.buffer.charAt(0);
               this.buffer.deleteCharAt(0);
               return c;
            }

            return -1;
         }

         this.buffer.delete(0, this.buffer.length());
         StringBuilder placeholderBuilder = new StringBuilder();

         do {
            int r1 = super.read();
            if (r1 == -1) {
               break;
            }

            placeholderBuilder.append((char)r1);
         } while(!this.endsWith(placeholderBuilder, this.suffix));

         for(int i = 0; i < this.suffix.length(); ++i) {
            placeholderBuilder.deleteCharAt(placeholderBuilder.length() - 1);
         }

         String placeholder = placeholderBuilder.toString();
         if (!this.placeholders.containsKey(placeholder)) {
            String canonicalPlaceholder = this.prefix + placeholder + this.suffix;
            if (placeholder.contains("flyway:")) {
               throw new FlywayException("Failed to populate value for default placeholder: " + canonicalPlaceholder);
            }

            throw new FlywayException("No value provided for placeholder: " + canonicalPlaceholder + ".  Check your configuration!");
         }

         this.replacement = this.placeholders.get(placeholder);
         if (this.replacement == null || this.replacement.length() == 0) {
            this.replacement = null;
            return this.read();
         }
      }

      int result = this.replacement.charAt(this.replacementPos);
      ++this.replacementPos;
      if (this.replacementPos >= this.replacement.length()) {
         this.replacement = null;
         this.replacementPos = 0;
      }

      return result;
   }

   public int read(char[] cbuf, int off, int len) throws IOException {
      int count = 0;

      for(int i = 0; i < len; ++i) {
         int r = this.read();
         if (r == -1) {
            return count == 0 ? -1 : count;
         }

         cbuf[off + i] = (char)r;
         ++count;
      }

      return count;
   }

   public void mark(int readAheadLimit) throws IOException {
      this.markBuffer = this.buffer.toString();
      this.markReplacement = this.replacement;
      this.markReplacementPos = this.replacementPos;
      super.mark(readAheadLimit);
   }

   public void reset() throws IOException {
      super.reset();
      this.buffer.delete(0, this.buffer.length());
      this.buffer.append(this.markBuffer);
      this.replacement = this.markReplacement;
      this.replacementPos = this.markReplacementPos;
   }

   private boolean endsWith(StringBuilder result, String str) {
      if (result.length() < str.length()) {
         return false;
      } else {
         for(int i = 0; i < str.length(); ++i) {
            if (result.charAt(result.length() - str.length() + i) != str.charAt(i)) {
               return false;
            }
         }

         return true;
      }
   }

   private static class CaseInsensitiveMap extends HashMap<String, String> {
      private CaseInsensitiveMap() {
      }

      public void putAll(Map<? extends String, ? extends String> m) {
         for(Entry<? extends String, ? extends String> e : m.entrySet()) {
            this.put((String)e.getKey(), (String)e.getValue());
         }

      }

      public String put(String key, String value) {
         return (String)super.put(key.toLowerCase(), value);
      }

      public String get(Object key) {
         return (String)super.get(key.toString().toLowerCase());
      }

      public boolean containsKey(Object key) {
         return super.containsKey(key.toString().toLowerCase());
      }
   }
}
