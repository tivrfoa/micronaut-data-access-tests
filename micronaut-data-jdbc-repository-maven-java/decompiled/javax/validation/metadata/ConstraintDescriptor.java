package javax.validation.metadata;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;

public interface ConstraintDescriptor<T extends Annotation> {
   T getAnnotation();

   String getMessageTemplate();

   Set<Class<?>> getGroups();

   Set<Class<? extends Payload>> getPayload();

   ConstraintTarget getValidationAppliesTo();

   List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses();

   Map<String, Object> getAttributes();

   Set<ConstraintDescriptor<?>> getComposingConstraints();

   boolean isReportAsSingleViolation();

   ValidateUnwrappedValue getValueUnwrapping();

   <U> U unwrap(Class<U> var1);
}
