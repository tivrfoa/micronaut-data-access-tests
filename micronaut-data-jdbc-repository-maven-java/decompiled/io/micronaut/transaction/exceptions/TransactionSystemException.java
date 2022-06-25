package io.micronaut.transaction.exceptions;

import io.micronaut.core.annotation.Nullable;
import java.util.Objects;

public class TransactionSystemException extends TransactionException {
   @Nullable
   private Throwable applicationException;

   public TransactionSystemException(String msg) {
      super(msg);
   }

   public TransactionSystemException(String msg, Throwable cause) {
      super(msg, cause);
   }

   public void initApplicationException(Throwable ex) {
      Objects.requireNonNull(ex, "Application exception must not be null");
      if (this.applicationException != null) {
         throw new IllegalStateException("Already holding an application exception: " + this.applicationException);
      } else {
         this.applicationException = ex;
      }
   }

   @Nullable
   public final Throwable getApplicationException() {
      return this.applicationException;
   }

   @Nullable
   public Throwable getOriginalException() {
      return this.applicationException != null ? this.applicationException : this.getCause();
   }
}
