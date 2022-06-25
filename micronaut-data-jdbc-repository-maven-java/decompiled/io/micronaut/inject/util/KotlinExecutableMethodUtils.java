package io.micronaut.inject.util;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.KotlinUtils;
import io.micronaut.inject.ExecutableMethod;
import kotlin.Unit;

@Internal
public class KotlinExecutableMethodUtils {
   public static boolean isKotlinFunctionReturnTypeUnit(@NonNull ExecutableMethod method) {
      if (KotlinUtils.KOTLIN_COROUTINES_SUPPORTED) {
         Argument[] arguments = method.getArguments();
         int argumentsLength = arguments.length;
         if (argumentsLength > 0) {
            Argument[] params = arguments[argumentsLength - 1].getTypeParameters();
            return params.length == 1 && params[0].getType() == Unit.class;
         }
      }

      return false;
   }
}
