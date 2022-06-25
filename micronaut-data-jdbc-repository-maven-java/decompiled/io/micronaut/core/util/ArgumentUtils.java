package io.micronaut.core.util;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Described;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;

public class ArgumentUtils {
   @NonNull
   public static Number requirePositive(String name, Number value) {
      requireNonNull(name, value);
      requirePositive(name, value.intValue());
      return value;
   }

   public static <T> T requireNonNull(String name, T value) {
      if (value == null) {
         throw new NullPointerException("Argument [" + name + "] cannot be null");
      } else {
         return value;
      }
   }

   public static int requirePositive(String name, int value) {
      if (value < 0) {
         throw new IllegalArgumentException("Argument [" + name + "] cannot be negative");
      } else {
         return value;
      }
   }

   public static ArgumentUtils.ArgumentCheck check(ArgumentUtils.Check check) {
      return new ArgumentUtils.ArgumentCheck(check);
   }

   public static <T> ArgumentUtils.ArgumentCheck check(String name, T value) {
      return new ArgumentUtils.ArgumentCheck(name, value);
   }

   public static void validateArguments(@NonNull Described described, @NonNull Argument<?>[] arguments, @NonNull Object[] values) {
      int requiredCount = arguments.length;
      int actualCount = values == null ? 0 : values.length;
      if (requiredCount != actualCount) {
         throw new IllegalArgumentException(
            "Wrong number of arguments to " + (described instanceof Executable ? "method" : "constructor") + ": " + described.getDescription()
         );
      } else {
         if (requiredCount > 0) {
            for(int i = 0; i < arguments.length; ++i) {
               Argument<?> argument = arguments[i];
               Class<?> type = argument.getWrapperType();
               Object value = values[i];
               if (value != null && !type.isInstance(value)) {
                  throw new IllegalArgumentException(
                     "Invalid type ["
                        + values[i].getClass().getName()
                        + "] for argument ["
                        + argument
                        + "] of "
                        + (described instanceof Executable ? "method" : "constructor")
                        + ": "
                        + described.getDescription()
                  );
               }
            }
         }

      }
   }

   public static class ArgumentCheck<T> {
      private final ArgumentUtils.Check check;
      private final String name;
      private final T value;

      public ArgumentCheck(ArgumentUtils.Check check) {
         this.check = check;
         this.name = null;
         this.value = null;
      }

      public ArgumentCheck(String name, T value) {
         this.check = null;
         this.name = name;
         this.value = value;
      }

      public void orElseFail(String message) {
         if (this.check != null && !this.check.condition()) {
            throw new IllegalArgumentException(message);
         }
      }

      public void notNull() {
         if (this.value == null) {
            throw new NullPointerException("Argument [" + this.name + "] cannot be null");
         }
      }
   }

   @FunctionalInterface
   public interface Check {
      boolean condition();
   }
}
