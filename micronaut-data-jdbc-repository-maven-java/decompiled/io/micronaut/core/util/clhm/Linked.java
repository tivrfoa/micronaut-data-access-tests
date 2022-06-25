package io.micronaut.core.util.clhm;

interface Linked<T extends Linked<T>> {
   T getPrevious();

   void setPrevious(T prev);

   T getNext();

   void setNext(T next);
}
