package io.micronaut.inject;

import io.micronaut.core.annotation.Internal;
import java.util.Objects;

@Internal
class DefaultBeanIdentifier implements BeanIdentifier {
   private final String id;

   DefaultBeanIdentifier(String id) {
      this.id = id;
   }

   public String toString() {
      return this.id;
   }

   @Override
   public String getName() {
      return this.id;
   }

   public int length() {
      return this.id.length();
   }

   public char charAt(int index) {
      return this.id.charAt(index);
   }

   public CharSequence subSequence(int start, int end) {
      return this.id.subSequence(start, end);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultBeanIdentifier that = (DefaultBeanIdentifier)o;
         return Objects.equals(this.id, that.id);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id});
   }
}
