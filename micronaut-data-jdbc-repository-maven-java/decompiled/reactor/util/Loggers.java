package reactor.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

public abstract class Loggers {
   public static final String FALLBACK_PROPERTY = "reactor.logging.fallback";
   private static Function<String, ? extends Logger> LOGGER_FACTORY;

   public static void resetLoggerFactory() {
      try {
         useSl4jLoggers();
      } catch (Throwable var1) {
         if (isFallbackToJdk()) {
            useJdkLoggers();
         } else {
            useConsoleLoggers();
         }
      }

   }

   static boolean isFallbackToJdk() {
      return "JDK".equalsIgnoreCase(System.getProperty("reactor.logging.fallback"));
   }

   public static void useConsoleLoggers() {
      String name = Loggers.class.getName();
      Function<String, Logger> loggerFactory = new Loggers.ConsoleLoggerFactory(false);
      LOGGER_FACTORY = loggerFactory;
      ((Logger)loggerFactory.apply(name)).debug("Using Console logging");
   }

   public static void useVerboseConsoleLoggers() {
      String name = Loggers.class.getName();
      Function<String, Logger> loggerFactory = new Loggers.ConsoleLoggerFactory(true);
      LOGGER_FACTORY = loggerFactory;
      ((Logger)loggerFactory.apply(name)).debug("Using Verbose Console logging");
   }

   public static void useCustomLoggers(Function<String, ? extends Logger> loggerFactory) {
      String name = Loggers.class.getName();
      LOGGER_FACTORY = loggerFactory;
      ((Logger)loggerFactory.apply(name)).debug("Using custom logging");
   }

   public static void useJdkLoggers() {
      String name = Loggers.class.getName();
      Function<String, Logger> loggerFactory = new Loggers.JdkLoggerFactory();
      LOGGER_FACTORY = loggerFactory;
      ((Logger)loggerFactory.apply(name)).debug("Using JDK logging framework");
   }

   public static void useSl4jLoggers() {
      String name = Loggers.class.getName();
      Function<String, Logger> loggerFactory = new Loggers.Slf4JLoggerFactory();
      LOGGER_FACTORY = loggerFactory;
      ((Logger)loggerFactory.apply(name)).debug("Using Slf4j logging framework");
   }

   public static Logger getLogger(String name) {
      return (Logger)LOGGER_FACTORY.apply(name);
   }

   public static Logger getLogger(Class<?> cls) {
      return (Logger)LOGGER_FACTORY.apply(cls.getName());
   }

   Loggers() {
   }

   static {
      resetLoggerFactory();
   }

   static final class ConsoleLogger implements Logger {
      private final String name;
      private final PrintStream err;
      private final PrintStream log;
      private final boolean verbose;

      ConsoleLogger(String name, PrintStream log, PrintStream err, boolean verbose) {
         this.name = name;
         this.log = log;
         this.err = err;
         this.verbose = verbose;
      }

      ConsoleLogger(String name, boolean verbose) {
         this(name, System.out, System.err, verbose);
      }

      @Override
      public String getName() {
         return this.name;
      }

      @Nullable
      final String format(@Nullable String from, @Nullable Object... arguments) {
         if (from == null) {
            return null;
         } else {
            String computed = from;
            if (arguments != null && arguments.length != 0) {
               for(Object argument : arguments) {
                  computed = computed.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(argument)));
               }
            }

            return computed;
         }
      }

      @Override
      public boolean isTraceEnabled() {
         return this.verbose;
      }

      @Override
      public synchronized void trace(String msg) {
         if (this.verbose) {
            this.log.format("[TRACE] (%s) %s\n", Thread.currentThread().getName(), msg);
         }
      }

      @Override
      public synchronized void trace(String format, Object... arguments) {
         if (this.verbose) {
            this.log.format("[TRACE] (%s) %s\n", Thread.currentThread().getName(), this.format(format, arguments));
         }
      }

      @Override
      public synchronized void trace(String msg, Throwable t) {
         if (this.verbose) {
            this.log.format("[TRACE] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
            t.printStackTrace(this.log);
         }
      }

      @Override
      public boolean isDebugEnabled() {
         return this.verbose;
      }

      @Override
      public synchronized void debug(String msg) {
         if (this.verbose) {
            this.log.format("[DEBUG] (%s) %s\n", Thread.currentThread().getName(), msg);
         }
      }

      @Override
      public synchronized void debug(String format, Object... arguments) {
         if (this.verbose) {
            this.log.format("[DEBUG] (%s) %s\n", Thread.currentThread().getName(), this.format(format, arguments));
         }
      }

      @Override
      public synchronized void debug(String msg, Throwable t) {
         if (this.verbose) {
            this.log.format("[DEBUG] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
            t.printStackTrace(this.log);
         }
      }

      @Override
      public boolean isInfoEnabled() {
         return true;
      }

      @Override
      public synchronized void info(String msg) {
         this.log.format("[ INFO] (%s) %s\n", Thread.currentThread().getName(), msg);
      }

      @Override
      public synchronized void info(String format, Object... arguments) {
         this.log.format("[ INFO] (%s) %s\n", Thread.currentThread().getName(), this.format(format, arguments));
      }

      @Override
      public synchronized void info(String msg, Throwable t) {
         this.log.format("[ INFO] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
         t.printStackTrace(this.log);
      }

      @Override
      public boolean isWarnEnabled() {
         return true;
      }

      @Override
      public synchronized void warn(String msg) {
         this.err.format("[ WARN] (%s) %s\n", Thread.currentThread().getName(), msg);
      }

      @Override
      public synchronized void warn(String format, Object... arguments) {
         this.err.format("[ WARN] (%s) %s\n", Thread.currentThread().getName(), this.format(format, arguments));
      }

      @Override
      public synchronized void warn(String msg, Throwable t) {
         this.err.format("[ WARN] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
         t.printStackTrace(this.err);
      }

      @Override
      public boolean isErrorEnabled() {
         return true;
      }

      @Override
      public synchronized void error(String msg) {
         this.err.format("[ERROR] (%s) %s\n", Thread.currentThread().getName(), msg);
      }

      @Override
      public synchronized void error(String format, Object... arguments) {
         this.err.format("[ERROR] (%s) %s\n", Thread.currentThread().getName(), this.format(format, arguments));
      }

      @Override
      public synchronized void error(String msg, Throwable t) {
         this.err.format("[ERROR] (%s) %s - %s\n", Thread.currentThread().getName(), msg, t);
         t.printStackTrace(this.err);
      }
   }

   private static final class ConsoleLoggerFactory implements Function<String, Logger> {
      private static final HashMap<String, Logger> consoleLoggers = new HashMap();
      final boolean verbose;

      private ConsoleLoggerFactory(boolean verbose) {
         this.verbose = verbose;
      }

      public Logger apply(String name) {
         return (Logger)consoleLoggers.computeIfAbsent(name, n -> new Loggers.ConsoleLogger(n, this.verbose));
      }
   }

   static final class JdkLogger implements Logger {
      private final java.util.logging.Logger logger;

      public JdkLogger(java.util.logging.Logger logger) {
         this.logger = logger;
      }

      @Override
      public String getName() {
         return this.logger.getName();
      }

      @Override
      public boolean isTraceEnabled() {
         return this.logger.isLoggable(Level.FINEST);
      }

      @Override
      public void trace(String msg) {
         this.logger.log(Level.FINEST, msg);
      }

      @Override
      public void trace(String format, Object... arguments) {
         this.logger.log(Level.FINEST, this.format(format, arguments));
      }

      @Override
      public void trace(String msg, Throwable t) {
         this.logger.log(Level.FINEST, msg, t);
      }

      @Override
      public boolean isDebugEnabled() {
         return this.logger.isLoggable(Level.FINE);
      }

      @Override
      public void debug(String msg) {
         this.logger.log(Level.FINE, msg);
      }

      @Override
      public void debug(String format, Object... arguments) {
         this.logger.log(Level.FINE, this.format(format, arguments));
      }

      @Override
      public void debug(String msg, Throwable t) {
         this.logger.log(Level.FINE, msg, t);
      }

      @Override
      public boolean isInfoEnabled() {
         return this.logger.isLoggable(Level.INFO);
      }

      @Override
      public void info(String msg) {
         this.logger.log(Level.INFO, msg);
      }

      @Override
      public void info(String format, Object... arguments) {
         this.logger.log(Level.INFO, this.format(format, arguments));
      }

      @Override
      public void info(String msg, Throwable t) {
         this.logger.log(Level.INFO, msg, t);
      }

      @Override
      public boolean isWarnEnabled() {
         return this.logger.isLoggable(Level.WARNING);
      }

      @Override
      public void warn(String msg) {
         this.logger.log(Level.WARNING, msg);
      }

      @Override
      public void warn(String format, Object... arguments) {
         this.logger.log(Level.WARNING, this.format(format, arguments));
      }

      @Override
      public void warn(String msg, Throwable t) {
         this.logger.log(Level.WARNING, msg, t);
      }

      @Override
      public boolean isErrorEnabled() {
         return this.logger.isLoggable(Level.SEVERE);
      }

      @Override
      public void error(String msg) {
         this.logger.log(Level.SEVERE, msg);
      }

      @Override
      public void error(String format, Object... arguments) {
         this.logger.log(Level.SEVERE, this.format(format, arguments));
      }

      @Override
      public void error(String msg, Throwable t) {
         this.logger.log(Level.SEVERE, msg, t);
      }

      @Nullable
      final String format(@Nullable String from, @Nullable Object... arguments) {
         if (from == null) {
            return null;
         } else {
            String computed = from;
            if (arguments != null && arguments.length != 0) {
               for(Object argument : arguments) {
                  computed = computed.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(argument)));
               }
            }

            return computed;
         }
      }
   }

   private static class JdkLoggerFactory implements Function<String, Logger> {
      private JdkLoggerFactory() {
      }

      public Logger apply(String name) {
         return new Loggers.JdkLogger(java.util.logging.Logger.getLogger(name));
      }
   }

   private static class Slf4JLogger implements Logger {
      private final org.slf4j.Logger logger;

      public Slf4JLogger(org.slf4j.Logger logger) {
         this.logger = logger;
      }

      @Override
      public String getName() {
         return this.logger.getName();
      }

      @Override
      public boolean isTraceEnabled() {
         return this.logger.isTraceEnabled();
      }

      @Override
      public void trace(String msg) {
         this.logger.trace(msg);
      }

      @Override
      public void trace(String format, Object... arguments) {
         this.logger.trace(format, arguments);
      }

      @Override
      public void trace(String msg, Throwable t) {
         this.logger.trace(msg, t);
      }

      @Override
      public boolean isDebugEnabled() {
         return this.logger.isDebugEnabled();
      }

      @Override
      public void debug(String msg) {
         this.logger.debug(msg);
      }

      @Override
      public void debug(String format, Object... arguments) {
         this.logger.debug(format, arguments);
      }

      @Override
      public void debug(String msg, Throwable t) {
         this.logger.debug(msg, t);
      }

      @Override
      public boolean isInfoEnabled() {
         return this.logger.isInfoEnabled();
      }

      @Override
      public void info(String msg) {
         this.logger.info(msg);
      }

      @Override
      public void info(String format, Object... arguments) {
         this.logger.info(format, arguments);
      }

      @Override
      public void info(String msg, Throwable t) {
         this.logger.info(msg, t);
      }

      @Override
      public boolean isWarnEnabled() {
         return this.logger.isWarnEnabled();
      }

      @Override
      public void warn(String msg) {
         this.logger.warn(msg);
      }

      @Override
      public void warn(String format, Object... arguments) {
         this.logger.warn(format, arguments);
      }

      @Override
      public void warn(String msg, Throwable t) {
         this.logger.warn(msg, t);
      }

      @Override
      public boolean isErrorEnabled() {
         return this.logger.isErrorEnabled();
      }

      @Override
      public void error(String msg) {
         this.logger.error(msg);
      }

      @Override
      public void error(String format, Object... arguments) {
         this.logger.error(format, arguments);
      }

      @Override
      public void error(String msg, Throwable t) {
         this.logger.error(msg, t);
      }
   }

   private static class Slf4JLoggerFactory implements Function<String, Logger> {
      private Slf4JLoggerFactory() {
      }

      public Logger apply(String name) {
         return new Loggers.Slf4JLogger(LoggerFactory.getLogger(name));
      }
   }
}
