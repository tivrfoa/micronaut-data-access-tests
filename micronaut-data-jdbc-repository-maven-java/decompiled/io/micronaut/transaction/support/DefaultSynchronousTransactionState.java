package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.transaction.TransactionDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public final class DefaultSynchronousTransactionState implements SynchronousTransactionState {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultSynchronousTransactionState.class);
   private Set<TransactionSynchronization> synchronizations;
   private String transactionName;
   private boolean readOnly;
   private TransactionDefinition.Isolation isolation;
   private boolean active;

   @Override
   public boolean isSynchronizationActive() {
      return this.synchronizations != null;
   }

   @Override
   public void initSynchronization() throws IllegalStateException {
      if (this.isSynchronizationActive()) {
         throw new IllegalStateException("Cannot activate transaction synchronization - already active");
      } else {
         LOG.trace("Initializing transaction synchronization");
         this.synchronizations = new LinkedHashSet();
      }
   }

   @Override
   public void registerSynchronization(TransactionSynchronization synchronization) {
      Objects.requireNonNull(synchronization, "TransactionSynchronization must not be null");
      if (this.synchronizations == null) {
         throw new IllegalStateException("Transaction synchronization is not active");
      } else {
         this.synchronizations.add(synchronization);
      }
   }

   @Override
   public List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
      if (this.synchronizations == null) {
         throw new IllegalStateException("Transaction synchronization is not active");
      } else if (this.synchronizations.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<TransactionSynchronization> sortedSynchs = new ArrayList(this.synchronizations);
         OrderUtil.sort(sortedSynchs);
         return Collections.unmodifiableList(sortedSynchs);
      }
   }

   @Override
   public void clearSynchronization() throws IllegalStateException {
      if (!this.isSynchronizationActive()) {
         throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
      } else {
         LOG.trace("Clearing transaction synchronization");
         this.synchronizations = null;
      }
   }

   @Override
   public void setTransactionName(String name) {
      this.transactionName = name;
   }

   @Override
   public String getTransactionName() {
      return this.transactionName;
   }

   @Override
   public void setTransactionReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
   }

   @Override
   public boolean isTransactionReadOnly() {
      return this.readOnly;
   }

   @Override
   public void setTransactionIsolationLevel(TransactionDefinition.Isolation isolationLevel) {
      this.isolation = isolationLevel;
   }

   @Override
   public TransactionDefinition.Isolation getTransactionIsolationLevel() {
      return this.isolation;
   }

   @Override
   public void setActualTransactionActive(boolean active) {
      this.active = active;
   }

   @Override
   public boolean isActualTransactionActive() {
      return this.active;
   }

   @Override
   public void clear() {
      this.synchronizations = null;
      this.transactionName = null;
      this.isolation = null;
   }
}
