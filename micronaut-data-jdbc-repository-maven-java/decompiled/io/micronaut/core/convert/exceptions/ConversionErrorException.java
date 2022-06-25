package io.micronaut.core.convert.exceptions;

import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.type.Argument;

public class ConversionErrorException extends RuntimeException {
   private final Argument argument;
   private final ConversionError conversionError;

   public ConversionErrorException(Argument argument, ConversionError conversionError) {
      super(buildMessage(argument, conversionError), conversionError.getCause());
      this.argument = argument;
      this.conversionError = conversionError;
   }

   public ConversionErrorException(Argument argument, Exception cause) {
      super(cause.getMessage(), cause);
      this.argument = argument;
      this.conversionError = () -> cause;
   }

   public Argument getArgument() {
      return this.argument;
   }

   public ConversionError getConversionError() {
      return this.conversionError;
   }

   private static String buildMessage(Argument argument, ConversionError conversionError) {
      return String.format(
         "Failed to convert argument [%s] for value [%s] due to: %s",
         argument.getName(),
         conversionError.getOriginalValue().orElse(null),
         conversionError.getCause().getMessage()
      );
   }
}
