package io.micronaut.context.env;

public interface ActiveEnvironment {
   String getName();

   int getPriority();

   static ActiveEnvironment of(String name, int priority) {
      return new ActiveEnvironment() {
         @Override
         public String getName() {
            return name;
         }

         @Override
         public int getPriority() {
            return priority;
         }
      };
   }
}
