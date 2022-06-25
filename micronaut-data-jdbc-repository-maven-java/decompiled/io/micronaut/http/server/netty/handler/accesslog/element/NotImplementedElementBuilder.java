package io.micronaut.http.server.netty.handler.accesslog.element;

public class NotImplementedElementBuilder implements LogElementBuilder {
   private static final String[] NOT_IMPLEMENTED = new String[]{"l", "u"};

   @Override
   public int getOrder() {
      return Integer.MAX_VALUE;
   }

   @Override
   public LogElement build(String token, String param) {
      for(String element : NOT_IMPLEMENTED) {
         if (token.equals(element)) {
            return ConstantElement.UNKNOWN;
         }
      }

      return null;
   }
}
