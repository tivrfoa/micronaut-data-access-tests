package io.micronaut.data.model.runtime;

import java.util.List;

public interface DeleteBatchOperation<E> extends BatchOperation<E> {
   List<DeleteOperation<E>> split();
}
