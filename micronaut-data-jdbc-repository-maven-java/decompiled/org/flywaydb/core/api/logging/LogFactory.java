package org.flywaydb.core.api.logging;

import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.logging.EvolvingLog;
import org.flywaydb.core.internal.logging.apachecommons.ApacheCommonsLogCreator;
import org.flywaydb.core.internal.logging.buffered.BufferedLogCreator;
import org.flywaydb.core.internal.logging.javautil.JavaUtilLogCreator;
import org.flywaydb.core.internal.logging.log4j2.Log4j2LogCreator;
import org.flywaydb.core.internal.logging.multi.MultiLogCreator;
import org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;

public class LogFactory {
   private static final Object $LOCK = new Object[0];
   private static volatile LogCreator logCreator;
   private static LogCreator fallbackLogCreator;
   private static Configuration configuration;

   public static void setConfiguration(Configuration configuration) {
      synchronized($LOCK) {
         LogFactory.configuration = configuration;
         logCreator = null;
      }
   }

   public static Log getLog(Class<?> clazz) {
      synchronized($LOCK) {
         if (logCreator == null) {
            logCreator = getLogCreator(LogFactory.class.getClassLoader(), fallbackLogCreator);
         }

         return new EvolvingLog(logCreator.createLogger(clazz), clazz);
      }
   }

   private static LogCreator getLogCreator(ClassLoader classLoader, LogCreator fallbackLogCreator) {
      synchronized($LOCK) {
         if (configuration == null) {
            return new BufferedLogCreator();
         } else {
            String[] loggers = configuration.getLoggers();
            List<LogCreator> logCreators = new ArrayList();

            for(String logger : loggers) {
               String var9 = logger.toLowerCase();
               switch(var9) {
                  case "auto":
                     logCreators.add(autoDetectLogCreator(classLoader, fallbackLogCreator));
                     break;
                  case "maven":
                  case "console":
                     logCreators.add(fallbackLogCreator);
                     break;
                  case "slf4j":
                     logCreators.add((LogCreator)ClassUtils.instantiate(Slf4jLogCreator.class.getName(), classLoader));
                     break;
                  case "log4j2":
                     logCreators.add((LogCreator)ClassUtils.instantiate(Log4j2LogCreator.class.getName(), classLoader));
                     break;
                  case "apache-commons":
                     logCreators.add((LogCreator)ClassUtils.instantiate(ApacheCommonsLogCreator.class.getName(), classLoader));
                     break;
                  default:
                     logCreators.add((LogCreator)ClassUtils.instantiate(logger, classLoader));
               }
            }

            return new MultiLogCreator(logCreators);
         }
      }
   }

   private static LogCreator autoDetectLogCreator(ClassLoader classLoader, LogCreator fallbackLogCreator) {
      synchronized($LOCK) {
         FeatureDetector featureDetector = new FeatureDetector(classLoader);
         if (featureDetector.isSlf4jAvailable()) {
            return ClassUtils.instantiate(Slf4jLogCreator.class.getName(), classLoader);
         } else if (featureDetector.isLog4J2Available()) {
            return ClassUtils.instantiate(Log4j2LogCreator.class.getName(), classLoader);
         } else if (featureDetector.isApacheCommonsLoggingAvailable()) {
            return ClassUtils.instantiate(ApacheCommonsLogCreator.class.getName(), classLoader);
         } else {
            return (LogCreator)(fallbackLogCreator == null ? new JavaUtilLogCreator() : fallbackLogCreator);
         }
      }
   }

   private LogFactory() {
   }

   public static void setLogCreator(LogCreator logCreator) {
      synchronized($LOCK) {
         LogFactory.logCreator = logCreator;
      }
   }

   public static void setFallbackLogCreator(LogCreator fallbackLogCreator) {
      synchronized($LOCK) {
         LogFactory.fallbackLogCreator = fallbackLogCreator;
      }
   }
}
