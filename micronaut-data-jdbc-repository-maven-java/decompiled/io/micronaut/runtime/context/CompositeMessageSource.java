package io.micronaut.runtime.context;

import io.micronaut.context.AbstractMessageSource;
import io.micronaut.context.MessageSource;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Primary
public final class CompositeMessageSource extends AbstractMessageSource {
   private final Collection<MessageSource> messageSources;

   public CompositeMessageSource(@Nullable Collection<MessageSource> messageSources) {
      if (messageSources != null) {
         this.messageSources = (Collection)OrderUtil.sort(messageSources.stream()).collect(Collectors.toList());
      } else {
         this.messageSources = Collections.emptyList();
      }

   }

   @NonNull
   @Override
   public Optional<String> getRawMessage(@NonNull String code, @NonNull MessageSource.MessageContext context) {
      ArgumentUtils.requireNonNull("code", code);
      ArgumentUtils.requireNonNull("context", context);

      for(MessageSource messageSource : this.messageSources) {
         Optional<String> message = messageSource.getRawMessage(code, context);
         if (message.isPresent()) {
            return message;
         }
      }

      return Optional.empty();
   }
}
