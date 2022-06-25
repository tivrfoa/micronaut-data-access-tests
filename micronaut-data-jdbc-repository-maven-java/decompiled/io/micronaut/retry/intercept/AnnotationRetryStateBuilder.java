package io.micronaut.retry.intercept;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.retry.RetryState;
import io.micronaut.retry.RetryStateBuilder;
import io.micronaut.retry.annotation.DefaultRetryPredicate;
import io.micronaut.retry.annotation.RetryPredicate;
import io.micronaut.retry.annotation.Retryable;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class AnnotationRetryStateBuilder implements RetryStateBuilder {
   private static final String ATTEMPTS = "attempts";
   private static final String MULTIPLIER = "multiplier";
   private static final String DELAY = "delay";
   private static final String MAX_DELAY = "maxDelay";
   private static final String INCLUDES = "includes";
   private static final String EXCLUDES = "excludes";
   private static final String PREDICATE = "predicate";
   private static final String CAPTUREDEXCEPTION = "capturedException";
   private static final int DEFAULT_RETRY_ATTEMPTS = 3;
   private final AnnotationMetadata annotationMetadata;

   AnnotationRetryStateBuilder(AnnotationMetadata annotationMetadata) {
      this.annotationMetadata = annotationMetadata;
   }

   @Override
   public RetryState build() {
      AnnotationValue<Retryable> retry = (AnnotationValue)this.annotationMetadata
         .findAnnotation(Retryable.class)
         .orElseThrow(() -> new IllegalStateException("Missing @Retryable annotation"));
      int attempts = retry.get("attempts", Integer.class).orElse(3);
      Duration delay = (Duration)retry.get("delay", Duration.class).orElse(Duration.ofSeconds(1L));
      Class<? extends RetryPredicate> predicateClass = (Class)retry.get("predicate", Class.class).orElse(DefaultRetryPredicate.class);
      RetryPredicate predicate = createPredicate(predicateClass, retry);
      Class<? extends Throwable> capturedException = (Class)retry.get("capturedException", Class.class).orElse(RuntimeException.class);
      return new SimpleRetry(
         attempts,
         retry.get("multiplier", Double.class).orElse(0.0),
         delay,
         (Duration)retry.get("maxDelay", Duration.class).orElse(null),
         predicate,
         capturedException
      );
   }

   private static RetryPredicate createPredicate(Class<? extends RetryPredicate> predicateClass, AnnotationValue<Retryable> retry) {
      if (predicateClass.equals(DefaultRetryPredicate.class)) {
         List<Class<? extends Throwable>> includes = resolveIncludes(retry, "includes");
         List<Class<? extends Throwable>> excludes = resolveIncludes(retry, "excludes");
         return new DefaultRetryPredicate(includes, excludes);
      } else {
         return InstantiationUtils.instantiate(predicateClass);
      }
   }

   private static List<Class<? extends Throwable>> resolveIncludes(AnnotationValue<Retryable> retry, String includes) {
      Class<?>[] values = retry.classValues(includes);
      return Collections.unmodifiableList(Arrays.asList(values));
   }
}
