package org.flywaydb.core.internal.jdbc;

import java.util.concurrent.Callable;

public interface ExecutionTemplate {
   <T> T execute(Callable<T> var1);
}
