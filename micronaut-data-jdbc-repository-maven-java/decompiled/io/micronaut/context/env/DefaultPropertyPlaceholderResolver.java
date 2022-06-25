package io.micronaut.context.env;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.PropertyResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultPropertyPlaceholderResolver implements PropertyPlaceholderResolver, AutoCloseable {
   public static final String PREFIX = "${";
   public static final String SUFFIX = "}";
   private static final Pattern ESCAPE_SEQUENCE = Pattern.compile("(.+)?:`([^`]+?)`");
   private static final char COLON = ':';
   private final PropertyResolver environment;
   private final ConversionService<?> conversionService;
   private final String prefix;
   private Collection<PropertyExpressionResolver> expressionResolvers;

   public DefaultPropertyPlaceholderResolver(PropertyResolver environment, ConversionService conversionService) {
      this.environment = environment;
      this.conversionService = conversionService;
      this.prefix = "${";
   }

   private Collection<PropertyExpressionResolver> getExpressionResolvers() {
      Collection<PropertyExpressionResolver> exResolvers = this.expressionResolvers;
      if (exResolvers == null) {
         synchronized(this) {
            exResolvers = this.expressionResolvers;
            if (exResolvers == null) {
               this.expressionResolvers = new ArrayList();
               exResolvers = new ArrayList();
               ClassLoader classLoader = this.environment instanceof Environment
                  ? ((Environment)this.environment).getClassLoader()
                  : this.environment.getClass().getClassLoader();
               SoftServiceLoader.load(PropertyExpressionResolver.class, classLoader).collectAll(exResolvers);
               this.expressionResolvers = exResolvers;
            }
         }
      }

      return exResolvers;
   }

   @Override
   public String getPrefix() {
      return this.prefix;
   }

   @Override
   public Optional<String> resolvePlaceholders(String str) {
      try {
         return Optional.of(this.resolveRequiredPlaceholders(str));
      } catch (ConfigurationException var3) {
         return Optional.empty();
      }
   }

   @Override
   public String resolveRequiredPlaceholders(String str) throws ConfigurationException {
      List<DefaultPropertyPlaceholderResolver.Segment> segments = this.buildSegments(str);
      StringBuilder value = new StringBuilder();

      for(DefaultPropertyPlaceholderResolver.Segment segment : segments) {
         value.append(segment.getValue(String.class));
      }

      return value.toString();
   }

   @Override
   public <T> T resolveRequiredPlaceholder(String str, Class<T> type) throws ConfigurationException {
      List<DefaultPropertyPlaceholderResolver.Segment> segments = this.buildSegments(str);
      if (segments.size() == 1) {
         return ((DefaultPropertyPlaceholderResolver.Segment)segments.get(0)).getValue(type);
      } else {
         throw new ConfigurationException("Cannot convert a multi segment placeholder to a specified type");
      }
   }

   public List<DefaultPropertyPlaceholderResolver.Segment> buildSegments(String str) {
      List<DefaultPropertyPlaceholderResolver.Segment> segments = new ArrayList();
      String value = str;

      for(int i = str.indexOf("${"); i > -1; i = value.indexOf("${")) {
         if (i > 0) {
            String rawSegment = value.substring(0, i);
            segments.add(new DefaultPropertyPlaceholderResolver.RawSegment(rawSegment));
         }

         value = value.substring(i + "${".length());
         int suffixIdx = value.indexOf("}");
         if (suffixIdx <= -1) {
            throw new ConfigurationException("Incomplete placeholder definitions detected: " + str);
         }

         String expr = value.substring(0, suffixIdx).trim();
         segments.add(new DefaultPropertyPlaceholderResolver.PlaceholderSegment(expr));
         if (value.length() > suffixIdx) {
            value = value.substring(suffixIdx + "}".length());
         }
      }

      if (value.length() > 0) {
         segments.add(new DefaultPropertyPlaceholderResolver.RawSegment(value));
      }

      return segments;
   }

   @Nullable
   protected <T> T resolveExpression(String context, String expression, Class<T> type) {
      for(PropertyExpressionResolver expressionResolver : this.getExpressionResolvers()) {
         Optional<T> value = expressionResolver.resolve(this.environment, this.conversionService, expression, type);
         if (value.isPresent()) {
            return (T)value.get();
         }
      }

      if (this.environment.containsProperty(expression)) {
         return (T)this.environment
            .getProperty(expression, type)
            .orElseThrow(() -> new ConfigurationException("Could not resolve expression: [" + expression + "] in placeholder ${" + context + "}"));
      } else {
         if (NameUtils.isEnvironmentName(expression)) {
            String envVar = CachedEnvironment.getenv(expression);
            if (StringUtils.isNotEmpty(envVar)) {
               return (T)this.conversionService
                  .convert(envVar, type)
                  .orElseThrow(() -> new ConfigurationException("Could not resolve expression: [" + expression + "] in placeholder ${" + context + "}"));
            }
         }

         return null;
      }
   }

   public void close() throws Exception {
      if (this.expressionResolvers != null) {
         for(PropertyExpressionResolver expressionResolver : this.expressionResolvers) {
            if (expressionResolver instanceof AutoCloseable) {
               ((AutoCloseable)expressionResolver).close();
            }
         }
      }

   }

   public class PlaceholderSegment implements DefaultPropertyPlaceholderResolver.Segment {
      private final String placeholder;
      private final List<String> expressions = new ArrayList();
      private String defaultValue;

      PlaceholderSegment(String placeholder) {
         this.placeholder = placeholder;
         this.findExpressions(placeholder);
      }

      public List<String> getExpressions() {
         return Collections.unmodifiableList(this.expressions);
      }

      @Override
      public <T> T getValue(Class<T> type) throws ConfigurationException {
         for(String expression : this.expressions) {
            T value = DefaultPropertyPlaceholderResolver.this.resolveExpression(this.placeholder, expression, type);
            if (value != null) {
               return value;
            }
         }

         if (this.defaultValue != null) {
            return (T)DefaultPropertyPlaceholderResolver.this.conversionService
               .convert(this.defaultValue, type)
               .orElseThrow(
                  () -> new ConfigurationException(
                        String.format("Could not convert default value [%s] in placeholder ${%s}", this.defaultValue, this.placeholder)
                     )
               );
         } else {
            throw new ConfigurationException("Could not resolve placeholder ${" + this.placeholder + "}");
         }
      }

      private void findExpressions(String placeholder) {
         String defaultValue = null;
         Matcher matcher = DefaultPropertyPlaceholderResolver.ESCAPE_SEQUENCE.matcher(placeholder);
         boolean escaped = false;
         String expression;
         if (matcher.find()) {
            defaultValue = matcher.group(2);
            expression = matcher.group(1);
            escaped = true;
         } else {
            int j = placeholder.indexOf(58);
            if (j > -1) {
               defaultValue = placeholder.substring(j + 1);
               expression = placeholder.substring(0, j);
            } else {
               expression = placeholder;
            }
         }

         this.expressions.add(expression);
         if (defaultValue != null) {
            if (escaped || !DefaultPropertyPlaceholderResolver.ESCAPE_SEQUENCE.matcher(defaultValue).find() && defaultValue.indexOf(58) <= -1) {
               this.defaultValue = defaultValue;
            } else {
               this.findExpressions(defaultValue);
            }
         }

      }
   }

   public class RawSegment implements DefaultPropertyPlaceholderResolver.Segment {
      private final String text;

      RawSegment(String text) {
         this.text = text;
      }

      @Override
      public <T> T getValue(Class<T> type) throws ConfigurationException {
         return (T)(type.isInstance(this.text)
            ? this.text
            : DefaultPropertyPlaceholderResolver.this.conversionService
               .convert(this.text, type)
               .orElseThrow(() -> new ConfigurationException("Could not convert: [" + this.text + "] to the required type: [" + type.getName() + "]")));
      }
   }

   public interface Segment {
      <T> T getValue(Class<T> type) throws ConfigurationException;
   }
}
