package io.micronaut.data.model.runtime;

import java.util.List;

public interface InsertBatchOperation<E> extends BatchOperation<E> {
   List<InsertOperation<E>> split();
}
