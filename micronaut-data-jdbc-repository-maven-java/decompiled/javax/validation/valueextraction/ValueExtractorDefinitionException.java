package javax.validation.valueextraction;

import javax.validation.ValidationException;

public class ValueExtractorDefinitionException extends ValidationException {
   public ValueExtractorDefinitionException() {
   }

   public ValueExtractorDefinitionException(String message) {
      super(message);
   }

   public ValueExtractorDefinitionException(Throwable cause) {
      super(cause);
   }

   public ValueExtractorDefinitionException(String message, Throwable cause) {
      super(message, cause);
   }
}
