package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StaticMessageSource extends AbstractMessageSource {
   private final Map<AbstractMessageSource.MessageKey, String> messageMap = new ConcurrentHashMap(40);

   @NonNull
   public StaticMessageSource addMessage(@NonNull String code, @NonNull String message) {
      if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(message)) {
         this.messageMap.put(new AbstractMessageSource.MessageKey(Locale.getDefault(), code), message);
      }

      return this;
   }

   @NonNull
   public StaticMessageSource addMessage(@NonNull Locale locale, @NonNull String code, @NonNull String message) {
      ArgumentUtils.requireNonNull("locale", locale);
      if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(message)) {
         this.messageMap.put(new AbstractMessageSource.MessageKey(locale, code), message);
      }

      return this;
   }

   @NonNull
   @Override
   public Optional<String> getRawMessage(@NonNull String code, @NonNull MessageSource.MessageContext context) {
      ArgumentUtils.requireNonNull("code", code);
      ArgumentUtils.requireNonNull("context", context);
      String msg = (String)this.messageMap.get(new AbstractMessageSource.MessageKey(context.getLocale(), code));
      return msg != null ? Optional.of(msg) : Optional.ofNullable(this.messageMap.get(new AbstractMessageSource.MessageKey(Locale.getDefault(), code)));
   }
}
