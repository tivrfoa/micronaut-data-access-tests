package io.micronaut.data.model.runtime;

import java.util.List;

public interface UpdateBatchOperation<E> extends BatchOperation<E> {
   List<UpdateOperation<E>> split();
}
