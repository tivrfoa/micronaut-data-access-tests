package io.micronaut.validation;

import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.validation.validator.ExecutableMethodValidator;
import io.micronaut.validation.validator.ReactiveValidator;
import io.micronaut.validation.validator.Validator;
import jakarta.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

@Singleton
public class ValidatingInterceptor implements MethodInterceptor<Object, Object> {
   public static final int POSITION = InterceptPhase.VALIDATE.getPosition();
   @Nullable
   private final ExecutableValidator executableValidator;
   @Nullable
   private final ExecutableMethodValidator micronautValidator;

   public ValidatingInterceptor(@Nullable Validator micronautValidator, @Nullable ValidatorFactory validatorFactory) {
      if (validatorFactory != null) {
         javax.validation.Validator validator = validatorFactory.getValidator();
         if (validator instanceof Validator) {
            this.micronautValidator = (ExecutableMethodValidator)validator;
            this.executableValidator = null;
         } else {
            this.micronautValidator = null;
            this.executableValidator = validator.forExecutables();
         }
      } else if (micronautValidator != null) {
         this.micronautValidator = micronautValidator.forExecutables();
         this.executableValidator = null;
      } else {
         this.micronautValidator = null;
         this.executableValidator = null;
      }

   }

   @Override
   public int getOrder() {
      return POSITION;
   }

   @Nullable
   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      if (this.executableValidator != null) {
         Method targetMethod = context.getTargetMethod();
         if (targetMethod.getParameterTypes().length != 0) {
            Set<ConstraintViolation<Object>> constraintViolations = this.executableValidator
               .validateParameters(context.getTarget(), targetMethod, context.getParameterValues(), this.getValidationGroups(context));
            if (!constraintViolations.isEmpty()) {
               throw new ConstraintViolationException(constraintViolations);
            }
         }

         return this.validateReturnExecutableValidator(context, targetMethod);
      } else if (this.micronautValidator != null) {
         ExecutableMethod<Object, Object> executableMethod = context.getExecutableMethod();
         if (executableMethod.getArguments().length != 0) {
            Set<ConstraintViolation<Object>> constraintViolations = this.micronautValidator
               .validateParameters(context.getTarget(), executableMethod, context.getParameterValues(), this.getValidationGroups(context));
            if (!constraintViolations.isEmpty()) {
               throw new ConstraintViolationException(constraintViolations);
            }
         }

         if (this.hasValidationAnnotation(context)) {
            if (this.micronautValidator instanceof ReactiveValidator) {
               InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

               try {
                  switch(interceptedMethod.resultType()) {
                     case PUBLISHER:
                        return interceptedMethod.handleResult(
                           ((ReactiveValidator)this.micronautValidator)
                              .validatePublisher(interceptedMethod.interceptResultAsPublisher(), this.getValidationGroups(context))
                        );
                     case COMPLETION_STAGE:
                        return interceptedMethod.handleResult(
                           ((ReactiveValidator)this.micronautValidator)
                              .validateCompletionStage(interceptedMethod.interceptResultAsCompletionStage(), this.getValidationGroups(context))
                        );
                     case SYNCHRONOUS:
                        return this.validateReturnMicronautValidator(context, executableMethod);
                     default:
                        return interceptedMethod.unsupported();
                  }
               } catch (Exception var5) {
                  return interceptedMethod.handleException(var5);
               }
            } else {
               return this.validateReturnMicronautValidator(context, executableMethod);
            }
         } else {
            return context.proceed();
         }
      } else {
         return context.proceed();
      }
   }

   private Object validateReturnMicronautValidator(MethodInvocationContext<Object, Object> context, ExecutableMethod<Object, Object> executableMethod) {
      Object result = context.proceed();
      Set<ConstraintViolation<Object>> constraintViolations = this.micronautValidator
         .validateReturnValue(context.getTarget(), executableMethod, result, this.getValidationGroups(context));
      if (!constraintViolations.isEmpty()) {
         throw new ConstraintViolationException(constraintViolations);
      } else {
         return result;
      }
   }

   private Object validateReturnExecutableValidator(MethodInvocationContext<Object, Object> context, Method targetMethod) {
      Object result = context.proceed();
      if (this.hasValidationAnnotation(context)) {
         Set<ConstraintViolation<Object>> constraintViolations = this.executableValidator
            .validateReturnValue(context.getTarget(), targetMethod, result, this.getValidationGroups(context));
         if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
         }
      }

      return result;
   }

   private boolean hasValidationAnnotation(MethodInvocationContext<Object, Object> context) {
      return context.hasStereotype(Validator.ANN_VALID) || context.hasStereotype(Validator.ANN_CONSTRAINT);
   }

   private Class<?>[] getValidationGroups(MethodInvocationContext<Object, Object> context) {
      return context.classValues(Validated.class, "groups");
   }
}
