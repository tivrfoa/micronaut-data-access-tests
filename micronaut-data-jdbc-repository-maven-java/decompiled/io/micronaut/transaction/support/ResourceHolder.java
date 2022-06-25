package io.micronaut.transaction.support;

public interface ResourceHolder {
   void reset();

   void unbound();

   boolean isVoid();
}
