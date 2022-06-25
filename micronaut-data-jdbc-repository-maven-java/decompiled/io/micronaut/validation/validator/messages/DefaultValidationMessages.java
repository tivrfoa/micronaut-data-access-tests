package io.micronaut.validation.validator.messages;

import io.micronaut.context.StaticMessageSource;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Singleton
public class DefaultValidationMessages extends StaticMessageSource {
   private static final String MESSAGE_SUFFIX = ".message";

   public DefaultValidationMessages() {
      this.addMessage(AssertTrue.class.getName() + ".message", "must be true");
      this.addMessage(AssertFalse.class.getName() + ".message", "must be false");
      this.addMessage(DecimalMax.class.getName() + ".message", "must be less than or equal to {value}");
      this.addMessage(DecimalMin.class.getName() + ".message", "must be greater than or equal to {value}");
      this.addMessage(Digits.class.getName() + ".message", "numeric value out of bounds (<{integer} digits>.<{fraction} digits> expected)");
      this.addMessage(Email.class.getName() + ".message", "must be a well-formed email address");
      this.addMessage(Future.class.getName() + ".message", "must be a future date");
      this.addMessage(FutureOrPresent.class.getName() + ".message", "must be a date in the present or in the future");
      this.addMessage(Max.class.getName() + ".message", "must be less than or equal to {value}");
      this.addMessage(Min.class.getName() + ".message", "must be greater than or equal to {value}");
      this.addMessage(Negative.class.getName() + ".message", "must be less than 0");
      this.addMessage(NegativeOrZero.class.getName() + ".message", "must be less than or equal to 0");
      this.addMessage(NotBlank.class.getName() + ".message", "must not be blank");
      this.addMessage(NotEmpty.class.getName() + ".message", "must not be empty");
      this.addMessage(NotNull.class.getName() + ".message", "must not be null");
      this.addMessage(Null.class.getName() + ".message", "must be null");
      this.addMessage(Past.class.getName() + ".message", "must be a past date");
      this.addMessage(PastOrPresent.class.getName() + ".message", "must be a date in the past or in the present");
      this.addMessage(Pattern.class.getName() + ".message", "must match \"{regexp}\"");
      this.addMessage(Positive.class.getName() + ".message", "must be greater than 0");
      this.addMessage(PositiveOrZero.class.getName() + ".message", "must be greater than or equal to 0");
      this.addMessage(Size.class.getName() + ".message", "size must be between {min} and {max}");
      this.addMessage(
         Introspected.class.getName() + ".message",
         "Cannot validate {type}. No bean introspection present. Please add @Introspected to the class and ensure Micronaut annotation processing is enabled"
      );
   }
}
