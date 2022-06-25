package io.micronaut.caffeine.cache;

import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.Nullable;

interface LinkedDeque<E> extends Deque<E> {
   boolean isFirst(E var1);

   boolean isLast(E var1);

   void moveToFront(E var1);

   void moveToBack(E var1);

   @Nullable
   E getPrevious(E var1);

   void setPrevious(E var1, @Nullable E var2);

   @Nullable
   E getNext(E var1);

   void setNext(E var1, @Nullable E var2);

   LinkedDeque.PeekingIterator<E> iterator();

   LinkedDeque.PeekingIterator<E> descendingIterator();

   public interface PeekingIterator<E> extends Iterator<E> {
      @Nullable
      E peek();

      static <E> LinkedDeque.PeekingIterator<E> concat(final LinkedDeque.PeekingIterator<E> first, final LinkedDeque.PeekingIterator<E> second) {
         return new LinkedDeque.PeekingIterator<E>() {
            public boolean hasNext() {
               return first.hasNext() || second.hasNext();
            }

            public E next() {
               if (first.hasNext()) {
                  return (E)first.next();
               } else if (second.hasNext()) {
                  return (E)second.next();
               } else {
                  throw new NoSuchElementException();
               }
            }

            @Nullable
            @Override
            public E peek() {
               return (E)(first.hasNext() ? first.peek() : second.peek());
            }
         };
      }

      static <E> LinkedDeque.PeekingIterator<E> comparing(
         final LinkedDeque.PeekingIterator<E> first, final LinkedDeque.PeekingIterator<E> second, final Comparator<E> comparator
      ) {
         return new LinkedDeque.PeekingIterator<E>() {
            public boolean hasNext() {
               return first.hasNext() || second.hasNext();
            }

            public E next() {
               if (!first.hasNext()) {
                  return (E)second.next();
               } else if (!second.hasNext()) {
                  return (E)first.next();
               } else {
                  E o1 = first.peek();
                  E o2 = second.peek();
                  boolean greaterOrEqual = comparator.compare(o1, o2) >= 0;
                  return (E)(greaterOrEqual ? first.next() : second.next());
               }
            }

            @Nullable
            @Override
            public E peek() {
               if (!first.hasNext()) {
                  return second.peek();
               } else if (!second.hasNext()) {
                  return first.peek();
               } else {
                  E o1 = first.peek();
                  E o2 = second.peek();
                  boolean greaterOrEqual = comparator.compare(o1, o2) >= 0;
                  return (E)(greaterOrEqual ? first.peek() : second.peek());
               }
            }
         };
      }
   }
}
