package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import java.util.Map;
import java.util.Optional;

public interface LocalizedMessageSource {
   @NonNull
   Optional<String> getMessage(@NonNull String code);

   @NonNull
   Optional<String> getMessage(@NonNull String code, Object... variables);

   @NonNull
   Optional<String> getMessage(@NonNull String code, Map<String, Object> variables);

   @NonNull
   default String getMessageOrDefault(@NonNull String code, @NonNull String defaultMessage) {
      return (String)this.getMessage(code).orElse(defaultMessage);
   }

   @NonNull
   default String getMessageOrDefault(@NonNull String code, @NonNull String defaultMessage, Object... variables) {
      return (String)this.getMessage(code, variables).orElse(defaultMessage);
   }

   @NonNull
   default String getMessageOrDefault(@NonNull String code, @NonNull String defaultMessage, Map<String, Object> variables) {
      return (String)this.getMessage(code, variables).orElse(defaultMessage);
   }
}
