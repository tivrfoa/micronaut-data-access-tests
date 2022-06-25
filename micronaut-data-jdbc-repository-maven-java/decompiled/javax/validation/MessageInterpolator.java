package javax.validation;

import java.util.Locale;
import javax.validation.metadata.ConstraintDescriptor;

public interface MessageInterpolator {
   String interpolate(String var1, MessageInterpolator.Context var2);

   String interpolate(String var1, MessageInterpolator.Context var2, Locale var3);

   public interface Context {
      ConstraintDescriptor<?> getConstraintDescriptor();

      Object getValidatedValue();

      <T> T unwrap(Class<T> var1);
   }
}
