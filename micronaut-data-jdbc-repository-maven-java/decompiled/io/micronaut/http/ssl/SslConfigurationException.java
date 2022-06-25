package io.micronaut.http.ssl;

public class SslConfigurationException extends RuntimeException {
   public SslConfigurationException(String message) {
      super(message);
   }

   public SslConfigurationException(String message, Throwable cause) {
      super(message, cause);
   }

   public SslConfigurationException(Throwable cause) {
      super("An error occurred configuring SSL", cause);
   }
}
