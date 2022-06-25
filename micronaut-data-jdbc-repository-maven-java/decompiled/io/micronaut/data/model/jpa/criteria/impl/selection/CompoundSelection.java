package io.micronaut.data.model.jpa.criteria.impl.selection;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.ISelection;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitable;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import jakarta.persistence.criteria.Selection;
import java.util.Collections;
import java.util.List;

@Internal
public final class CompoundSelection<T> implements ISelection<T>, SelectionVisitable {
   private final List<Selection<?>> selections;

   public CompoundSelection(List<Selection<?>> selections) {
      this.selections = selections;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   @Override
   public boolean isCompoundSelection() {
      return true;
   }

   @Override
   public List<Selection<?>> getCompoundSelectionItems() {
      return Collections.unmodifiableList(this.selections);
   }

   public Class<? extends T> getJavaType() {
      throw new IllegalStateException("Unknown");
   }

   @Override
   public Selection<T> alias(String name) {
      throw new IllegalStateException("Compound selection cannot have alias!");
   }
}
