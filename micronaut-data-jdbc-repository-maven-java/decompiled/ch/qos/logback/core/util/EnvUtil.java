package ch.qos.logback.core.util;

import java.util.ArrayList;
import java.util.List;

public class EnvUtil {
   private static boolean isJDK_N_OrHigher(int n) {
      List<String> versionList = new ArrayList();

      for(int i = 0; i < 5; ++i) {
         versionList.add("1." + (n + i));
      }

      String javaVersion = System.getProperty("java.version");
      if (javaVersion == null) {
         return false;
      } else {
         for(String v : versionList) {
            if (javaVersion.startsWith(v)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isJDK5() {
      return isJDK_N_OrHigher(5);
   }

   public static boolean isJDK6OrHigher() {
      return isJDK_N_OrHigher(6);
   }

   public static boolean isJDK7OrHigher() {
      return isJDK_N_OrHigher(7);
   }

   public static boolean isJaninoAvailable() {
      ClassLoader classLoader = EnvUtil.class.getClassLoader();

      try {
         Class<?> bindingClass = classLoader.loadClass("org.codehaus.janino.ScriptEvaluator");
         return bindingClass != null;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   public static boolean isWindows() {
      String os = System.getProperty("os.name");
      return os.startsWith("Windows");
   }
}
