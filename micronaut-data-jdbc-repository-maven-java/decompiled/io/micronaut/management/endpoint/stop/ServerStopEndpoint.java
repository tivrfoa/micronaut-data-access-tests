package io.micronaut.management.endpoint.stop;

import io.micronaut.context.ApplicationContext;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Write;
import java.util.LinkedHashMap;
import java.util.Map;

@Endpoint(
   id = "stop",
   defaultEnabled = false
)
public class ServerStopEndpoint {
   private static final long WAIT_BEFORE_STOP = 500L;
   private final ApplicationContext context;
   private final Map<String, String> message;

   ServerStopEndpoint(ApplicationContext context) {
      this.context = context;
      this.message = new LinkedHashMap(1);
      this.message.put("message", "Server shutdown started");
   }

   @Write(
      consumes = {}
   )
   public Object stop() {
      Map var1;
      try {
         var1 = this.message;
      } finally {
         Thread thread = new Thread(this::stopServer);
         thread.setContextClassLoader(this.getClass().getClassLoader());
         thread.start();
      }

      return var1;
   }

   private void stopServer() {
      try {
         Thread.sleep(500L);
      } catch (InterruptedException var2) {
         Thread.currentThread().interrupt();
      }

      this.context.stop();
   }
}
