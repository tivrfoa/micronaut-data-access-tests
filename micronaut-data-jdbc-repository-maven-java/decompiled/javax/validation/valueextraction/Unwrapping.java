package javax.validation.valueextraction;

import javax.validation.Payload;

public interface Unwrapping {
   public interface Skip extends Payload {
   }

   public interface Unwrap extends Payload {
   }
}
