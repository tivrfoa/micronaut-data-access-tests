package org.reactivestreams;

public interface Subscription {
   void request(long var1);

   void cancel();
}
