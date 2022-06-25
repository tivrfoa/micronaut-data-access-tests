package io.micronaut.scheduling.executor;

import io.micronaut.inject.MethodReference;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public interface ExecutorSelector {
   Optional<ExecutorService> select(MethodReference method, ThreadSelection threadSelection);

   Optional<ExecutorService> select(String name);
}
