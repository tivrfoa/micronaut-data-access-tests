package io.micronaut.context.exceptions;

import io.micronaut.inject.BeanDefinition;
import java.util.Iterator;

public class NonUniqueBeanException extends NoSuchBeanException {
   private final Class targetType;
   private final Iterator possibleCandidates;

   public <T> NonUniqueBeanException(Class targetType, Iterator<BeanDefinition<T>> candidates) {
      super(buildMessage(candidates));
      this.targetType = targetType;
      this.possibleCandidates = candidates;
   }

   public <T> Iterator<BeanDefinition<T>> getPossibleCandidates() {
      return this.possibleCandidates;
   }

   public <T> Class<T> getBeanType() {
      return this.targetType;
   }

   private static <T> String buildMessage(Iterator<BeanDefinition<T>> possibleCandidates) {
      StringBuilder message = new StringBuilder("Multiple possible bean candidates found: [");

      while(possibleCandidates.hasNext()) {
         Class next = ((BeanDefinition)possibleCandidates.next()).getBeanType();
         message.append(next.getName());
         if (possibleCandidates.hasNext()) {
            message.append(", ");
         }
      }

      message.append("]");
      return message.toString();
   }
}
