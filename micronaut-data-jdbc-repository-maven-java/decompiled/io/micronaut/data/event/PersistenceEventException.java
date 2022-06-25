package io.micronaut.data.event;

import io.micronaut.data.exceptions.DataAccessException;

public class PersistenceEventException extends DataAccessException {
   public PersistenceEventException(String message) {
      super(message);
   }

   public PersistenceEventException(String message, Throwable cause) {
      super(message, cause);
   }
}
