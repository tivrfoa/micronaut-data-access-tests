package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Internal
class DefaultMessageContext implements MessageSource.MessageContext {
   @Nullable
   private final Locale locale;
   @Nullable
   private final Map<String, Object> variables;

   DefaultMessageContext(@Nullable Locale locale, @Nullable Map<String, Object> variables) {
      this.locale = locale;
      this.variables = variables;
   }

   @NonNull
   @Override
   public Map<String, Object> getVariables() {
      return this.variables != null ? Collections.unmodifiableMap(this.variables) : Collections.emptyMap();
   }

   @NonNull
   @Override
   public Locale getLocale() {
      return this.getLocale(Locale.getDefault());
   }

   @NonNull
   @Override
   public Locale getLocale(@Nullable Locale defaultLocale) {
      if (this.locale != null) {
         return this.locale;
      } else {
         return defaultLocale != null ? defaultLocale : Locale.getDefault();
      }
   }
}
