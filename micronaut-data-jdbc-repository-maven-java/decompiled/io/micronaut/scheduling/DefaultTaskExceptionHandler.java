package io.micronaut.scheduling;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Primary
public class DefaultTaskExceptionHandler implements TaskExceptionHandler<Object, Throwable> {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExceptionHandler.class);

   @Override
   public void handle(@Nullable Object bean, @NonNull Throwable throwable) {
      if (LOG.isErrorEnabled()) {
         StringBuilder message = new StringBuilder("Error invoking scheduled task ");
         if (bean != null) {
            message.append("for bean [").append(bean.toString()).append("] ");
         }

         message.append(throwable.getMessage());
         LOG.error(message.toString(), throwable);
      }

   }
}
