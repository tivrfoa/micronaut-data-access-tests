package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.jpa.criteria.impl.selection.AliasedSelection;
import jakarta.persistence.criteria.Selection;
import java.util.Collections;
import java.util.List;

public interface ISelection<T> extends Selection<T> {
   @NonNull
   default Selection<T> alias(@NonNull String name) {
      return new AliasedSelection<>(this, name);
   }

   @Nullable
   default String getAlias() {
      return null;
   }

   default boolean isCompoundSelection() {
      return false;
   }

   @NonNull
   default List<Selection<?>> getCompoundSelectionItems() {
      return Collections.emptyList();
   }
}
