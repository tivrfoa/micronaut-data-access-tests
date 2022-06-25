package io.micronaut.core.io.socket;

import io.micronaut.core.util.ArgumentUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

public class SocketUtils {
   public static final String LOCALHOST = "localhost";
   public static final int MIN_PORT_RANGE = 1024;
   public static final int MAX_PORT_RANGE = 65535;
   private static final Random random = new Random(System.currentTimeMillis());

   public static int findAvailableTcpPort() {
      return findAvailableTcpPort(1025, 65535);
   }

   public static int findAvailableTcpPort(int minPortRange, int maxPortRange) {
      ArgumentUtils.check(() -> minPortRange > 1024).orElseFail("Port minimum value must be greater than 1024");
      ArgumentUtils.check(() -> maxPortRange >= minPortRange).orElseFail("Max port range must be greater than minimum port range");
      ArgumentUtils.check(() -> maxPortRange <= 65535).orElseFail("Port maximum value must be less than 65535");
      int currentPort = nextPort(minPortRange, maxPortRange);

      while(!isTcpPortAvailable(currentPort)) {
         currentPort = nextPort(minPortRange, maxPortRange);
      }

      return currentPort;
   }

   public static boolean isTcpPortAvailable(int currentPort) {
      try {
         Socket socket = new Socket();
         Throwable var2 = null;

         boolean var3;
         try {
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), currentPort), 20);
            var3 = false;
         } catch (Throwable var13) {
            var2 = var13;
            throw var13;
         } finally {
            if (socket != null) {
               if (var2 != null) {
                  try {
                     socket.close();
                  } catch (Throwable var12) {
                     var2.addSuppressed(var12);
                  }
               } else {
                  socket.close();
               }
            }

         }

         return var3;
      } catch (Throwable var15) {
         return true;
      }
   }

   private static int nextPort(int minPortRange, int maxPortRange) {
      int seed = maxPortRange - minPortRange;
      return random.nextInt(seed) + minPortRange;
   }
}
