package io.micronaut.aop.internal.intercepted;

import io.micronaut.aop.Around;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.InterceptorBinding;
import io.micronaut.aop.InterceptorKind;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.ReturnType;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;

@Internal
public final class InterceptedMethodUtil {
   private InterceptedMethodUtil() {
   }

   public static InterceptedMethod of(MethodInvocationContext<?, ?> context) {
      if (context.isSuspend()) {
         KotlinInterceptedMethod kotlinInterceptedMethod = KotlinInterceptedMethod.of(context);
         return (InterceptedMethod)(kotlinInterceptedMethod != null ? kotlinInterceptedMethod : new SynchronousInterceptedMethod(context));
      } else {
         ReturnType<?> returnType = context.getReturnType();
         Class<?> returnTypeClass = returnType.getType();
         if (returnTypeClass == Void.TYPE || returnTypeClass == String.class) {
            return new SynchronousInterceptedMethod(context);
         } else if (CompletionStage.class.isAssignableFrom(returnTypeClass) || Future.class.isAssignableFrom(returnTypeClass)) {
            return new CompletionStageInterceptedMethod(context);
         } else {
            return (InterceptedMethod)(PublisherInterceptedMethod.isConvertibleToPublisher(returnTypeClass)
               ? new PublisherInterceptedMethod(context)
               : new SynchronousInterceptedMethod(context));
         }
      }
   }

   public static AnnotationValue<?>[] resolveInterceptorBinding(AnnotationMetadata annotationMetadata, InterceptorKind interceptorKind) {
      List<AnnotationValue<InterceptorBinding>> interceptorBindings = annotationMetadata.getAnnotationValuesByType(InterceptorBinding.class);
      return !interceptorBindings.isEmpty() ? (AnnotationValue[])interceptorBindings.stream().filter(av -> {
         InterceptorKind kind = (InterceptorKind)av.enumValue("kind", InterceptorKind.class).orElse(InterceptorKind.AROUND);
         return kind == interceptorKind;
      }).toArray(x$0 -> new AnnotationValue[x$0]) : AnnotationUtil.ZERO_ANNOTATION_VALUES;
   }

   public static boolean hasAroundStereotype(@Nullable AnnotationMetadata annotationMetadata) {
      return hasAround(
         annotationMetadata,
         annMetadata -> annMetadata.hasStereotype(Around.class),
         annMetdata -> annMetdata.getAnnotationValuesByType(InterceptorBinding.class)
      );
   }

   public static boolean hasDeclaredAroundAdvice(@Nullable AnnotationMetadata annotationMetadata) {
      return hasAround(
         annotationMetadata,
         annMetadata -> annMetadata.hasDeclaredStereotype(Around.class),
         annMetdata -> annMetdata.getDeclaredAnnotationValuesByType(InterceptorBinding.class)
      );
   }

   private static boolean hasAround(
      @Nullable AnnotationMetadata annotationMetadata,
      @NonNull Predicate<AnnotationMetadata> hasFunction,
      @NonNull Function<AnnotationMetadata, List<AnnotationValue<InterceptorBinding>>> interceptorBindingsFunction
   ) {
      if (annotationMetadata == null) {
         return false;
      } else if (hasFunction.test(annotationMetadata)) {
         return true;
      } else {
         return annotationMetadata.hasDeclaredStereotype("io.micronaut.aop.InterceptorBindingDefinitions")
            ? ((List)interceptorBindingsFunction.apply(annotationMetadata))
               .stream()
               .anyMatch(av -> av.enumValue("kind", InterceptorKind.class).orElse(InterceptorKind.AROUND) == InterceptorKind.AROUND)
            : false;
      }
   }
}
