package com.fasterxml.jackson.datatype.jdk8;

import java.io.IOException;

public class WrappedIOException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public WrappedIOException(IOException cause) {
      super(cause);
   }

   public IOException getCause() {
      return (IOException)super.getCause();
   }
}
