package io.micronaut.http.exceptions;

import io.micronaut.http.HttpStatus;
import java.util.Optional;

public class HttpStatusException extends HttpException {
   private HttpStatus status;
   private Object body;

   public HttpStatusException(HttpStatus status, String message) {
      super(message);
      this.status = status;
   }

   public HttpStatusException(HttpStatus status, Object body) {
      this.status = status;
      this.body = body;
   }

   public HttpStatus getStatus() {
      return this.status;
   }

   public Optional<Object> getBody() {
      return Optional.ofNullable(this.body);
   }
}
