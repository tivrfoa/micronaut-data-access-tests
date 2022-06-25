package io.micronaut.context;

import io.micronaut.context.exceptions.NoSuchMessageException;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.ArgumentUtils;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
@Indexed(MessageSource.class)
public interface MessageSource extends Ordered {
   MessageSource EMPTY = new MessageSource() {
      @NonNull
      @Override
      public Optional<String> getRawMessage(@NonNull String code, @NonNull MessageSource.MessageContext context) {
         return Optional.empty();
      }

      @NonNull
      @Override
      public String interpolate(@NonNull String template, @NonNull MessageSource.MessageContext context) {
         return template;
      }
   };

   @NonNull
   default Optional<String> getMessage(@NonNull String code, @NonNull Locale locale) {
      return this.getMessage(code, MessageSource.MessageContext.of(locale));
   }

   @NonNull
   default Optional<String> getMessage(@NonNull String code, @NonNull Locale locale, @NonNull Object... variables) {
      return this.getMessage(code, locale, MessageSourceUtils.variables(variables));
   }

   @NonNull
   default Optional<String> getMessage(@NonNull String code, @NonNull Locale locale, @NonNull Map<String, Object> variables) {
      return this.getMessage(code, MessageSource.MessageContext.of(locale, variables));
   }

   @NonNull
   default Optional<String> getMessage(@NonNull String code, @NonNull MessageSource.MessageContext context) {
      Optional<String> rawMessage = this.getRawMessage(code, context);
      return rawMessage.map(message -> this.interpolate(message, context));
   }

   @NonNull
   default String getMessage(@NonNull String code, @NonNull String defaultMessage, @NonNull Locale locale) {
      return this.getMessage(code, MessageSource.MessageContext.of(locale), defaultMessage);
   }

   @NonNull
   default String getMessage(@NonNull String code, @NonNull String defaultMessage, @NonNull Locale locale, @NonNull Map<String, Object> variables) {
      return this.getMessage(code, MessageSource.MessageContext.of(locale, variables), defaultMessage);
   }

   @NonNull
   default String getMessage(@NonNull String code, @NonNull String defaultMessage, @NonNull Locale locale, @NonNull Object... variables) {
      return this.getMessage(code, defaultMessage, locale, MessageSourceUtils.variables(variables));
   }

   @NonNull
   default String getMessage(@NonNull String code, @NonNull MessageSource.MessageContext context, @NonNull String defaultMessage) {
      ArgumentUtils.requireNonNull("defaultMessage", defaultMessage);
      String rawMessage = this.getRawMessage(code, context, defaultMessage);
      return this.interpolate(rawMessage, context);
   }

   @NonNull
   Optional<String> getRawMessage(@NonNull String code, @NonNull MessageSource.MessageContext context);

   @NonNull
   default String getRawMessage(@NonNull String code, @NonNull MessageSource.MessageContext context, @NonNull String defaultMessage) {
      ArgumentUtils.requireNonNull("defaultMessage", defaultMessage);
      return (String)this.getRawMessage(code, context).orElse(defaultMessage);
   }

   @NonNull
   String interpolate(@NonNull String template, @NonNull MessageSource.MessageContext context);

   @NonNull
   default String getRequiredMessage(@NonNull String code, @NonNull MessageSource.MessageContext context) {
      return (String)this.getMessage(code, context).orElseThrow(() -> new NoSuchMessageException(code));
   }

   @NonNull
   default String getRequiredRawMessage(@NonNull String code, @NonNull MessageSource.MessageContext context) {
      return (String)this.getRawMessage(code, context).orElseThrow(() -> new NoSuchMessageException(code));
   }

   public interface MessageContext {
      MessageSource.MessageContext DEFAULT = new MessageSource.MessageContext() {
      };

      @NonNull
      default Locale getLocale() {
         return Locale.getDefault();
      }

      @NonNull
      default Locale getLocale(@Nullable Locale defaultLocale) {
         return defaultLocale != null ? defaultLocale : this.getLocale();
      }

      @NonNull
      default Map<String, Object> getVariables() {
         return Collections.emptyMap();
      }

      @NonNull
      static MessageSource.MessageContext of(@Nullable Locale locale) {
         return new DefaultMessageContext(locale, null);
      }

      @NonNull
      static MessageSource.MessageContext of(@Nullable Map<String, Object> variables) {
         return new DefaultMessageContext(null, variables);
      }

      @NonNull
      static MessageSource.MessageContext of(@Nullable Locale locale, @Nullable Map<String, Object> variables) {
         return new DefaultMessageContext(locale, variables);
      }
   }
}
