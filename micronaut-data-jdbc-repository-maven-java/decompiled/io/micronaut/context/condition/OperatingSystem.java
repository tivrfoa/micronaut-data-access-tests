package io.micronaut.context.condition;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.CachedEnvironment;
import java.util.Locale;

public final class OperatingSystem {
   private static volatile OperatingSystem instance;
   private final Requires.Family family;

   private OperatingSystem(Requires.Family family) {
      this.family = family;
   }

   public static OperatingSystem getCurrent() {
      if (instance == null) {
         synchronized(OperatingSystem.class) {
            if (instance == null) {
               String osName = CachedEnvironment.getProperty("os.name").toLowerCase(Locale.ENGLISH);
               Requires.Family osFamily;
               if (osName.contains("linux")) {
                  osFamily = Requires.Family.LINUX;
               } else if (osName.startsWith("mac") || osName.startsWith("darwin")) {
                  osFamily = Requires.Family.MAC_OS;
               } else if (osName.contains("windows")) {
                  osFamily = Requires.Family.WINDOWS;
               } else if (osName.contains("sunos")) {
                  osFamily = Requires.Family.SOLARIS;
               } else {
                  osFamily = Requires.Family.OTHER;
               }

               instance = new OperatingSystem(osFamily);
            }
         }
      }

      return instance;
   }

   public boolean isLinux() {
      return this.family == Requires.Family.LINUX;
   }

   public boolean isWindows() {
      return this.family == Requires.Family.WINDOWS;
   }

   public boolean isMacOs() {
      return this.family == Requires.Family.MAC_OS;
   }

   public boolean isSolaris() {
      return this.family == Requires.Family.SOLARIS;
   }

   public Requires.Family getFamily() {
      return this.family;
   }
}
