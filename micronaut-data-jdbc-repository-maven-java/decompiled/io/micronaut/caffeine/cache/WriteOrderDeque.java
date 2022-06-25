package io.micronaut.caffeine.cache;

import org.checkerframework.checker.nullness.qual.Nullable;

final class WriteOrderDeque<E extends WriteOrderDeque.WriteOrder<E>> extends AbstractLinkedDeque<E> {
   @Override
   public boolean contains(Object o) {
      return o instanceof WriteOrderDeque.WriteOrder && this.contains((WriteOrderDeque.WriteOrder<?>)o);
   }

   boolean contains(WriteOrderDeque.WriteOrder<?> e) {
      return e.getPreviousInWriteOrder() != null || e.getNextInWriteOrder() != null || e == this.first;
   }

   public boolean remove(Object o) {
      return o instanceof WriteOrderDeque.WriteOrder && this.remove((E)o);
   }

   public boolean remove(E e) {
      if (this.contains(e)) {
         this.unlink(e);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public E getPrevious(E e) {
      return e.getPreviousInWriteOrder();
   }

   public void setPrevious(E e, @Nullable E prev) {
      e.setPreviousInWriteOrder(prev);
   }

   @Nullable
   public E getNext(E e) {
      return e.getNextInWriteOrder();
   }

   public void setNext(E e, @Nullable E next) {
      e.setNextInWriteOrder(next);
   }

   interface WriteOrder<T extends WriteOrderDeque.WriteOrder<T>> {
      @Nullable
      T getPreviousInWriteOrder();

      void setPreviousInWriteOrder(@Nullable T var1);

      @Nullable
      T getNextInWriteOrder();

      void setNextInWriteOrder(@Nullable T var1);
   }
}
