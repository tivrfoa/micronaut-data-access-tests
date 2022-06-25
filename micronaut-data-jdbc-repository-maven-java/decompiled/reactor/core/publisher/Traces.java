package reactor.core.publisher;

import java.lang.StackWalker.StackFrame;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

final class Traces {
   static final boolean full = Boolean.parseBoolean(System.getProperty("reactor.trace.assembly.fullstacktrace", "false"));
   static final String CALL_SITE_GLUE = " ⇢ ";
   static Supplier<Supplier<String>> callSiteSupplierFactory;

   static boolean shouldSanitize(String stackTraceRow) {
      return stackTraceRow.startsWith("java.util.function")
         || stackTraceRow.startsWith("reactor.core.publisher.Mono.onAssembly")
         || stackTraceRow.equals("reactor.core.publisher.Mono.onAssembly")
         || stackTraceRow.equals("reactor.core.publisher.Flux.onAssembly")
         || stackTraceRow.equals("reactor.core.publisher.ParallelFlux.onAssembly")
         || stackTraceRow.startsWith("reactor.core.publisher.SignalLogger")
         || stackTraceRow.startsWith("reactor.core.publisher.FluxOnAssembly")
         || stackTraceRow.startsWith("reactor.core.publisher.MonoOnAssembly.")
         || stackTraceRow.startsWith("reactor.core.publisher.MonoCallableOnAssembly.")
         || stackTraceRow.startsWith("reactor.core.publisher.FluxCallableOnAssembly.")
         || stackTraceRow.startsWith("reactor.core.publisher.Hooks")
         || stackTraceRow.startsWith("sun.reflect")
         || stackTraceRow.startsWith("java.util.concurrent.ThreadPoolExecutor")
         || stackTraceRow.startsWith("java.lang.reflect");
   }

   static String extractOperatorAssemblyInformation(String source) {
      String[] parts = extractOperatorAssemblyInformationParts(source);
      switch(parts.length) {
         case 0:
            return "[no operator assembly information]";
         default:
            return String.join(" ⇢ ", parts);
      }
   }

   static boolean isUserCode(String line) {
      return !line.startsWith("reactor.core.publisher") || line.contains("Test");
   }

   static String[] extractOperatorAssemblyInformationParts(String source) {
      String[] uncleanTraces = source.split("\n");
      List<String> traces = (List)Stream.of(uncleanTraces).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
      if (traces.isEmpty()) {
         return new String[0];
      } else {
         int i = 0;

         while(i < traces.size() && !isUserCode((String)traces.get(i))) {
            ++i;
         }

         String apiLine;
         String userCodeLine;
         if (i == 0) {
            apiLine = "";
            userCodeLine = (String)traces.get(0);
         } else if (i == traces.size()) {
            apiLine = "";
            userCodeLine = ((String)traces.get(i - 1)).replaceFirst("reactor.core.publisher.", "");
         } else {
            apiLine = (String)traces.get(i - 1);
            userCodeLine = (String)traces.get(i);
         }

         if (apiLine.isEmpty()) {
            return new String[]{userCodeLine};
         } else {
            int linePartIndex = apiLine.indexOf(40);
            if (linePartIndex > 0) {
               apiLine = apiLine.substring(0, linePartIndex);
            }

            apiLine = apiLine.replaceFirst("reactor.core.publisher.", "");
            return new String[]{apiLine, "at " + userCodeLine};
         }
      }
   }

   static {
      String[] strategyClasses = new String[]{
         Traces.class.getName() + "$StackWalkerCallSiteSupplierFactory",
         Traces.class.getName() + "$SharedSecretsCallSiteSupplierFactory",
         Traces.class.getName() + "$ExceptionCallSiteSupplierFactory"
      };
      callSiteSupplierFactory = (Supplier)Stream.of(strategyClasses).flatMap(className -> {
         try {
            Class<?> clazz = Class.forName(className);
            Supplier<Supplier<String>> function = (Supplier)clazz.getDeclaredConstructor().newInstance();
            return Stream.of(function);
         } catch (LinkageError var3) {
            return Stream.empty();
         } catch (Throwable var4) {
            return Stream.empty();
         }
      }).findFirst().orElseThrow(() -> new IllegalStateException("Valid strategy not found"));
   }

   static class ExceptionCallSiteSupplierFactory implements Supplier<Supplier<String>> {
      public Supplier<String> get() {
         return new Traces.ExceptionCallSiteSupplierFactory.TracingException();
      }

      static class TracingException extends Throwable implements Supplier<String> {
         public String get() {
            StackTraceElement previousElement = null;
            StackTraceElement[] stackTrace = this.getStackTrace();

            for(int i = 2; i < stackTrace.length; ++i) {
               StackTraceElement e = stackTrace[i];
               String className = e.getClassName();
               if (Traces.isUserCode(className)) {
                  StringBuilder sb = new StringBuilder();
                  if (previousElement != null) {
                     sb.append("\t").append(previousElement.toString()).append("\n");
                  }

                  sb.append("\t").append(e.toString()).append("\n");
                  return sb.toString();
               }

               if (Traces.full || e.getLineNumber() > 1) {
                  String classAndMethod = className + "." + e.getMethodName();
                  if (Traces.full || !Traces.shouldSanitize(classAndMethod)) {
                     previousElement = e;
                  }
               }
            }

            return "";
         }
      }
   }

   static class SharedSecretsCallSiteSupplierFactory implements Supplier<Supplier<String>> {
      public Supplier<String> get() {
         return new Traces.SharedSecretsCallSiteSupplierFactory.TracingException();
      }

      static class TracingException extends Throwable implements Supplier<String> {
         static final JavaLangAccess javaLangAccess = SharedSecrets.getJavaLangAccess();

         public String get() {
            int stackTraceDepth = javaLangAccess.getStackTraceDepth(this);
            StackTraceElement previousElement = null;

            for(int i = 2; i < stackTraceDepth; ++i) {
               StackTraceElement e = javaLangAccess.getStackTraceElement(this, i);
               String className = e.getClassName();
               if (Traces.isUserCode(className)) {
                  StringBuilder sb = new StringBuilder();
                  if (previousElement != null) {
                     sb.append("\t").append(previousElement.toString()).append("\n");
                  }

                  sb.append("\t").append(e.toString()).append("\n");
                  return sb.toString();
               }

               if (Traces.full || e.getLineNumber() > 1) {
                  String classAndMethod = className + "." + e.getMethodName();
                  if (Traces.full || !Traces.shouldSanitize(classAndMethod)) {
                     previousElement = e;
                  }
               }
            }

            return "";
         }
      }
   }

   static final class StackWalkerCallSiteSupplierFactory implements Supplier<Supplier<String>> {
      public Supplier<String> get() {
         StackFrame[] stack = (StackFrame[])StackWalker.getInstance().walk(s -> {
            StackFrame[] result = new StackFrame[10];
            Iterator<StackFrame> iterator = s.iterator();
            iterator.next();
            int i = 0;

            while(iterator.hasNext()) {
               StackFrame frame = (StackFrame)iterator.next();
               if (i >= result.length) {
                  return new StackFrame[0];
               }

               result[i++] = frame;
               if (Traces.isUserCode(frame.getClassName())) {
                  break;
               }
            }

            StackFrame[] copy = new StackFrame[i];
            System.arraycopy(result, 0, copy, 0, i);
            return copy;
         });
         if (stack.length == 0) {
            return () -> "";
         } else {
            return stack.length == 1 ? () -> "\t" + stack[0].toString() + "\n" : () -> {
               StringBuilder sb = new StringBuilder();
               int j = stack.length - 2;

               while(j > 0) {
                  StackFrame previous;
                  previous = stack[j];
                  label21:
                  if (!Traces.full) {
                     if (!previous.isNativeMethod()) {
                        String previousRow = previous.getClassName() + "." + previous.getMethodName();
                        if (!Traces.shouldSanitize(previousRow)) {
                           break label21;
                        }
                     }

                     --j;
                     continue;
                  }

                  sb.append("\t").append(previous.toString()).append("\n");
                  break;
               }

               sb.append("\t").append(stack[stack.length - 1].toString()).append("\n");
               return sb.toString();
            };
         }
      }

      static {
         StackWalker.getInstance();
      }
   }
}
