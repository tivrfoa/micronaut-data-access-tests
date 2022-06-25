package io.micronaut.transaction.interceptor;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.transaction.support.DefaultTransactionDefinition;
import java.util.Set;

public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {
   private String qualifier;
   private Set<Class<? extends Throwable>> noRollbackFor;

   public void setQualifier(String qualifier) {
      this.qualifier = qualifier;
   }

   public void setNoRollbackFor(Class<? extends Throwable>... noRollbackFor) {
      if (ArrayUtils.isNotEmpty(noRollbackFor)) {
         this.noRollbackFor = CollectionUtils.setOf(noRollbackFor);
      }

   }

   @Nullable
   @Override
   public String getQualifier() {
      return this.qualifier;
   }

   @Override
   public boolean rollbackOn(Throwable ex) {
      return this.noRollbackFor == null ? true : this.noRollbackFor.stream().noneMatch(t -> t.isInstance(ex));
   }
}
