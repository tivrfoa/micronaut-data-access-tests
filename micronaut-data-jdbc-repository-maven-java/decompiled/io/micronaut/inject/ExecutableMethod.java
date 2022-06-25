package io.micronaut.inject;

import io.micronaut.core.naming.Described;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import java.util.Arrays;
import java.util.stream.Collectors;

public interface ExecutableMethod<T, R> extends Executable<T, R>, MethodReference<T, R>, Described {
   default boolean isAbstract() {
      return false;
   }

   default boolean isSuspend() {
      return false;
   }

   @Override
   default String getDescription(boolean simple) {
      Argument<R> argument = this.getReturnType().asArgument();
      String typeString = argument.getTypeString(simple);
      String args = (String)Arrays.stream(this.getArguments()).map(arg -> arg.getTypeString(simple) + " " + arg.getName()).collect(Collectors.joining(","));
      return typeString + " " + this.getName() + "(" + args + ")";
   }

   @Override
   default String getDescription() {
      return this.getDescription(true);
   }
}
