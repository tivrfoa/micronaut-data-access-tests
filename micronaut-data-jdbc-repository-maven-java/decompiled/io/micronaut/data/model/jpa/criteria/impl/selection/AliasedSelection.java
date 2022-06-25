package io.micronaut.data.model.jpa.criteria.impl.selection;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.ISelection;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitable;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import jakarta.persistence.criteria.Selection;
import java.util.List;

@Internal
public final class AliasedSelection<T> implements ISelection<T>, SelectionVisitable {
   private final ISelection<T> selection;
   private final String alias;

   public AliasedSelection(ISelection<T> selection, String alias) {
      this.selection = selection;
      this.alias = alias;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   public ISelection<T> getSelection() {
      return this.selection;
   }

   @Override
   public Selection<T> alias(String name) {
      throw new IllegalStateException("Alias already assigned!");
   }

   @Override
   public boolean isCompoundSelection() {
      return this.selection.isCompoundSelection();
   }

   @Override
   public List<Selection<?>> getCompoundSelectionItems() {
      return this.selection.getCompoundSelectionItems();
   }

   public Class<? extends T> getJavaType() {
      return this.selection.getJavaType();
   }

   @Override
   public String getAlias() {
      return this.alias;
   }
}
