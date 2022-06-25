package io.micronaut.transaction.support;

import java.io.Flushable;

public interface SmartTransactionObject extends Flushable {
   boolean isRollbackOnly();

   void flush();
}
