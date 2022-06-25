package io.micronaut.transaction.test;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.spring.tx.test.SpringTransactionTestExecutionListener;
import io.micronaut.test.annotation.TransactionMode;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.support.DefaultTransactionDefinition;
import java.util.concurrent.atomic.AtomicInteger;

@EachBean(SynchronousTransactionManager.class)
@Requirements({@Requires(
   classes = {TestExecutionListener.class}
), @Requires(
   property = "micronaut.test.transactional",
   value = "true",
   defaultValue = "true"
)})
@Replaces(SpringTransactionTestExecutionListener.class)
@Internal
public class DefaultTestTransactionExecutionListener implements TestExecutionListener {
   private final SynchronousTransactionManager<Object> transactionManager;
   private final TransactionMode transactionMode;
   private TransactionStatus<Object> tx;
   private final AtomicInteger counter = new AtomicInteger();
   private final AtomicInteger setupCounter = new AtomicInteger();
   private final boolean rollback;

   protected DefaultTestTransactionExecutionListener(
      SynchronousTransactionManager<Object> transactionManager,
      @Property(name = "micronaut.test.rollback",defaultValue = "true") boolean rollback,
      @Property(name = "micronaut.test.transaction-mode",defaultValue = "SEPARATE_TRANSACTIONS") TransactionMode transactionMode
   ) {
      this.transactionManager = transactionManager;
      this.rollback = rollback;
      this.transactionMode = transactionMode;
   }

   public void beforeSetupTest(TestContext testContext) {
      this.beforeTestExecution(testContext);
   }

   public void afterSetupTest(TestContext testContext) {
      if (this.transactionMode.equals(TransactionMode.SINGLE_TRANSACTION)) {
         this.setupCounter.getAndIncrement();
      } else {
         this.afterTestExecution(false);
      }

   }

   public void beforeCleanupTest(TestContext testContext) {
      this.beforeTestExecution(testContext);
   }

   public void afterCleanupTest(TestContext testContext) {
      this.afterTestExecution(false);
   }

   public void afterTestExecution(TestContext testContext) {
      if (this.transactionMode.equals(TransactionMode.SINGLE_TRANSACTION)) {
         this.counter.addAndGet(-this.setupCounter.getAndSet(0));
      }

      this.afterTestExecution(this.rollback);
   }

   public void beforeTestExecution(TestContext testContext) {
      if (this.counter.getAndIncrement() == 0) {
         this.tx = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
      }

   }

   private void afterTestExecution(boolean rollback) {
      if (this.counter.decrementAndGet() == 0) {
         if (rollback) {
            this.transactionManager.rollback(this.tx);
         } else {
            this.transactionManager.commit(this.tx);
         }
      }

   }
}
