package io.micronaut.http.exceptions;

import java.net.URISyntaxException;

public class UriSyntaxException extends HttpException {
   public UriSyntaxException(URISyntaxException e) {
      super(e.getMessage());
   }
}
