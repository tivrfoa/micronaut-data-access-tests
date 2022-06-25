package javax.validation;

import java.util.Set;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

public interface Validator {
   <T> Set<ConstraintViolation<T>> validate(T var1, Class<?>... var2);

   <T> Set<ConstraintViolation<T>> validateProperty(T var1, String var2, Class<?>... var3);

   <T> Set<ConstraintViolation<T>> validateValue(Class<T> var1, String var2, Object var3, Class<?>... var4);

   BeanDescriptor getConstraintsForClass(Class<?> var1);

   <T> T unwrap(Class<T> var1);

   ExecutableValidator forExecutables();
}
