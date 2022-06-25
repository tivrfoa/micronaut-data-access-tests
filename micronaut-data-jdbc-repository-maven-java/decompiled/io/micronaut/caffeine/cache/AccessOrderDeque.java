package io.micronaut.caffeine.cache;

import org.checkerframework.checker.nullness.qual.Nullable;

final class AccessOrderDeque<E extends AccessOrderDeque.AccessOrder<E>> extends AbstractLinkedDeque<E> {
   @Override
   public boolean contains(Object o) {
      return o instanceof AccessOrderDeque.AccessOrder && this.contains((AccessOrderDeque.AccessOrder<?>)o);
   }

   boolean contains(AccessOrderDeque.AccessOrder<?> e) {
      return e.getPreviousInAccessOrder() != null || e.getNextInAccessOrder() != null || e == this.first;
   }

   public boolean remove(Object o) {
      return o instanceof AccessOrderDeque.AccessOrder && this.remove((E)o);
   }

   boolean remove(E e) {
      if (this.contains(e)) {
         this.unlink(e);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public E getPrevious(E e) {
      return e.getPreviousInAccessOrder();
   }

   public void setPrevious(E e, @Nullable E prev) {
      e.setPreviousInAccessOrder(prev);
   }

   @Nullable
   public E getNext(E e) {
      return e.getNextInAccessOrder();
   }

   public void setNext(E e, @Nullable E next) {
      e.setNextInAccessOrder(next);
   }

   interface AccessOrder<T extends AccessOrderDeque.AccessOrder<T>> {
      @Nullable
      T getPreviousInAccessOrder();

      void setPreviousInAccessOrder(@Nullable T var1);

      @Nullable
      T getNextInAccessOrder();

      void setNextInAccessOrder(@Nullable T var1);
   }
}
