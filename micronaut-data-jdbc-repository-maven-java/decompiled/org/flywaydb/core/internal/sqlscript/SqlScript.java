package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.resource.LoadableResource;

public interface SqlScript extends Comparable<SqlScript> {
   SqlStatementIterator getSqlStatements();

   int getSqlStatementCount();

   LoadableResource getResource();

   boolean executeInTransaction();

   boolean shouldExecute();

   void validate();
}
