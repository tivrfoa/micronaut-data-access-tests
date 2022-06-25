package io.micronaut.core.bind.exceptions;

import io.micronaut.core.type.Argument;

public class UnsatisfiedArgumentException extends RuntimeException {
   private final Argument<?> argument;

   public UnsatisfiedArgumentException(Argument<?> argument) {
      super(buildMessage(argument));
      this.argument = argument;
   }

   public UnsatisfiedArgumentException(Argument<?> argument, String message) {
      super("Argument [" + argument + "] not satisfied: " + message);
      this.argument = argument;
   }

   public Argument<?> getArgument() {
      return this.argument;
   }

   private static String buildMessage(Argument<?> argument) {
      return "Required argument [" + argument + "] not specified";
   }
}
