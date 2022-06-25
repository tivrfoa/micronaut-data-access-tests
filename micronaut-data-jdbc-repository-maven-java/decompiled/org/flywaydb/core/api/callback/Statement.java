package org.flywaydb.core.api.callback;

import java.util.List;

public interface Statement {
   String getSql();

   List<Warning> getWarnings();

   List<Error> getErrors();
}
