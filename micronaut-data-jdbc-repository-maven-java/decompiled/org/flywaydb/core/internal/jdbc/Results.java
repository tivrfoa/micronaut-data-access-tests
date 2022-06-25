package org.flywaydb.core.internal.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Warning;

public class Results {
   public static final Results EMPTY_RESULTS = new Results();
   private final List<Result> results = new ArrayList();
   private final List<Warning> warnings = new ArrayList();
   private final List<Error> errors = new ArrayList();
   private SQLException exception = null;

   public void addResult(Result result) {
      this.results.add(result);
   }

   public void addWarning(Warning warning) {
      this.warnings.add(warning);
   }

   public void addError(Error error) {
      this.errors.add(error);
   }

   public List<Result> getResults() {
      return this.results;
   }

   public List<Warning> getWarnings() {
      return this.warnings;
   }

   public List<Error> getErrors() {
      return this.errors;
   }

   public SQLException getException() {
      return this.exception;
   }

   public void setException(SQLException exception) {
      this.exception = exception;
   }
}
