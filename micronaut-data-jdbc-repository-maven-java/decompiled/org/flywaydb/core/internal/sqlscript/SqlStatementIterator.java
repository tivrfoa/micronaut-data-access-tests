package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.util.CloseableIterator;

public interface SqlStatementIterator extends CloseableIterator<SqlStatement> {
   void close();
}
