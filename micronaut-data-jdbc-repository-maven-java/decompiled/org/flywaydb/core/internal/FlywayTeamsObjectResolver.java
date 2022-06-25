package org.flywaydb.core.internal;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.ClassUtils;

public class FlywayTeamsObjectResolver {
   private static final Log LOG = LogFactory.getLog(FlywayTeamsObjectResolver.class);

   public static <T> T resolve(Class<T> clazz, Object... params) {
      String packageName = clazz.getPackage().getName();
      String className = clazz.getSimpleName();
      ClassLoader classLoader = new FlywayTeamsObjectResolver().getClass().getClassLoader();
      if (VersionPrinter.EDITION == Edition.COMMUNITY) {
         return loadCommunityClass(packageName + "." + className, classLoader, params);
      } else if (VersionPrinter.EDITION != Edition.PRO && VersionPrinter.EDITION != Edition.ENTERPRISE) {
         String pathOfTheClass = packageName + "." + VersionPrinter.EDITION.name().toLowerCase() + "." + className;
         return loadClass(pathOfTheClass, packageName, className, classLoader, params);
      } else {
         String pathOfTheClass = packageName + ".teams." + className;
         return loadClass(pathOfTheClass, packageName, className, classLoader, params);
      }
   }

   private static <T> T loadClass(String pathOfTheClass, String packageName, String className, ClassLoader classLoader, Object... params) {
      try {
         return ClassUtils.instantiate(pathOfTheClass, classLoader, params);
      } catch (FlywayException var6) {
         LOG.debug(var6.getMessage() + ". Defaulting to Community Edition for " + className);
         return loadCommunityClass(packageName + "." + className, classLoader, params);
      }
   }

   private static <T> T loadCommunityClass(String pathOfTheClass, ClassLoader classLoader, Object... params) {
      return ClassUtils.instantiate(pathOfTheClass, classLoader, params);
   }

   private FlywayTeamsObjectResolver() {
   }
}
