package reactor.core.publisher;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

public abstract class Hooks {
   static Function<Publisher, Publisher> onEachOperatorHook;
   static volatile Function<Publisher, Publisher> onLastOperatorHook;
   static volatile BiFunction<? super Throwable, Object, ? extends Throwable> onOperatorErrorHook;
   static volatile Consumer<? super Throwable> onErrorDroppedHook;
   static volatile Consumer<Object> onNextDroppedHook;
   static volatile OnNextFailureStrategy onNextErrorHook;
   private static final LinkedHashMap<String, Function<? super Publisher<Object>, ? extends Publisher<Object>>> onEachOperatorHooks = new LinkedHashMap(1);
   private static final LinkedHashMap<String, Function<? super Publisher<Object>, ? extends Publisher<Object>>> onLastOperatorHooks = new LinkedHashMap(1);
   private static final LinkedHashMap<String, BiFunction<? super Throwable, Object, ? extends Throwable>> onOperatorErrorHooks = new LinkedHashMap(1);
   private static final LinkedHashMap<String, Function<Queue<?>, Queue<?>>> QUEUE_WRAPPERS = new LinkedHashMap(1);
   private static Function<Queue<?>, Queue<?>> QUEUE_WRAPPER = Function.identity();
   static final Logger log = Loggers.getLogger(Hooks.class);
   static final String KEY_ON_ERROR_DROPPED = "reactor.onErrorDropped.local";
   static final String KEY_ON_NEXT_DROPPED = "reactor.onNextDropped.local";
   static final String KEY_ON_OPERATOR_ERROR = "reactor.onOperatorError.local";
   static final String KEY_ON_DISCARD = "reactor.onDiscard.local";
   static final String KEY_ON_REJECTED_EXECUTION = "reactor.onRejectedExecution.local";
   static boolean GLOBAL_TRACE = initStaticGlobalTrace();
   static boolean DETECT_CONTEXT_LOSS = false;

   public static <T> Flux<T> convertToFluxBypassingHooks(Publisher<T> publisher) {
      return Flux.wrap(publisher);
   }

   public static <T> Mono<T> convertToMonoBypassingHooks(Publisher<T> publisher, boolean enforceMonoContract) {
      return Mono.wrap(publisher, enforceMonoContract);
   }

   public static void onEachOperator(Function<? super Publisher<Object>, ? extends Publisher<Object>> onEachOperator) {
      onEachOperator(onEachOperator.toString(), onEachOperator);
   }

   public static void onEachOperator(String key, Function<? super Publisher<Object>, ? extends Publisher<Object>> onEachOperator) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(onEachOperator, "onEachOperator");
      log.debug("Hooking onEachOperator: {}", key);
      synchronized(log) {
         onEachOperatorHooks.put(key, onEachOperator);
         onEachOperatorHook = createOrUpdateOpHook(onEachOperatorHooks.values());
      }
   }

   public static void resetOnEachOperator(String key) {
      Objects.requireNonNull(key, "key");
      log.debug("Reset onEachOperator: {}", key);
      synchronized(log) {
         onEachOperatorHooks.remove(key);
         onEachOperatorHook = createOrUpdateOpHook(onEachOperatorHooks.values());
      }
   }

   public static void resetOnEachOperator() {
      log.debug("Reset to factory defaults : onEachOperator");
      synchronized(log) {
         onEachOperatorHooks.clear();
         onEachOperatorHook = null;
      }
   }

   public static void onErrorDropped(Consumer<? super Throwable> c) {
      Objects.requireNonNull(c, "onErrorDroppedHook");
      log.debug("Hooking new default : onErrorDropped");
      synchronized(log) {
         if (onErrorDroppedHook != null) {
            Consumer<Throwable> _c = onErrorDroppedHook.andThen(c);
            onErrorDroppedHook = _c;
         } else {
            onErrorDroppedHook = c;
         }

      }
   }

   public static void onLastOperator(Function<? super Publisher<Object>, ? extends Publisher<Object>> onLastOperator) {
      onLastOperator(onLastOperator.toString(), onLastOperator);
   }

   public static void onLastOperator(String key, Function<? super Publisher<Object>, ? extends Publisher<Object>> onLastOperator) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(onLastOperator, "onLastOperator");
      log.debug("Hooking onLastOperator: {}", key);
      synchronized(log) {
         onLastOperatorHooks.put(key, onLastOperator);
         onLastOperatorHook = createOrUpdateOpHook(onLastOperatorHooks.values());
      }
   }

   public static void resetOnLastOperator(String key) {
      Objects.requireNonNull(key, "key");
      log.debug("Reset onLastOperator: {}", key);
      synchronized(log) {
         onLastOperatorHooks.remove(key);
         onLastOperatorHook = createOrUpdateOpHook(onLastOperatorHooks.values());
      }
   }

   public static void resetOnLastOperator() {
      log.debug("Reset to factory defaults : onLastOperator");
      synchronized(log) {
         onLastOperatorHooks.clear();
         onLastOperatorHook = null;
      }
   }

   public static void onNextDropped(Consumer<Object> c) {
      Objects.requireNonNull(c, "onNextDroppedHook");
      log.debug("Hooking new default : onNextDropped");
      synchronized(log) {
         if (onNextDroppedHook != null) {
            onNextDroppedHook = onNextDroppedHook.andThen(c);
         } else {
            onNextDroppedHook = c;
         }

      }
   }

   public static void onNextDroppedFail() {
      log.debug("Enabling failure mode for onNextDropped");
      synchronized(log) {
         onNextDroppedHook = n -> {
            throw Exceptions.failWithCancel();
         };
      }
   }

   public static void onOperatorDebug() {
      log.debug("Enabling stacktrace debugging via onOperatorDebug");
      GLOBAL_TRACE = true;
   }

   public static void resetOnOperatorDebug() {
      GLOBAL_TRACE = false;
   }

   public static void onNextError(BiFunction<? super Throwable, Object, ? extends Throwable> onNextError) {
      Objects.requireNonNull(onNextError, "onNextError");
      log.debug("Hooking new default : onNextError");
      if (onNextError instanceof OnNextFailureStrategy) {
         synchronized(log) {
            onNextErrorHook = (OnNextFailureStrategy)onNextError;
         }
      } else {
         synchronized(log) {
            onNextErrorHook = new OnNextFailureStrategy.LambdaOnNextErrorStrategy(onNextError);
         }
      }

   }

   public static void onOperatorError(BiFunction<? super Throwable, Object, ? extends Throwable> onOperatorError) {
      onOperatorError(onOperatorError.toString(), onOperatorError);
   }

   public static void onOperatorError(String key, BiFunction<? super Throwable, Object, ? extends Throwable> onOperatorError) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(onOperatorError, "onOperatorError");
      log.debug("Hooking onOperatorError: {}", key);
      synchronized(log) {
         onOperatorErrorHooks.put(key, onOperatorError);
         onOperatorErrorHook = createOrUpdateOpErrorHook(onOperatorErrorHooks.values());
      }
   }

   public static void resetOnOperatorError(String key) {
      Objects.requireNonNull(key, "key");
      log.debug("Reset onOperatorError: {}", key);
      synchronized(log) {
         onOperatorErrorHooks.remove(key);
         onOperatorErrorHook = createOrUpdateOpErrorHook(onOperatorErrorHooks.values());
      }
   }

   public static void resetOnOperatorError() {
      log.debug("Reset to factory defaults : onOperatorError");
      synchronized(log) {
         onOperatorErrorHooks.clear();
         onOperatorErrorHook = null;
      }
   }

   public static void resetOnErrorDropped() {
      log.debug("Reset to factory defaults : onErrorDropped");
      synchronized(log) {
         onErrorDroppedHook = null;
      }
   }

   public static void resetOnNextDropped() {
      log.debug("Reset to factory defaults : onNextDropped");
      synchronized(log) {
         onNextDroppedHook = null;
      }
   }

   public static void resetOnNextError() {
      log.debug("Reset to factory defaults : onNextError");
      synchronized(log) {
         onNextErrorHook = null;
      }
   }

   public static void enableContextLossTracking() {
      DETECT_CONTEXT_LOSS = true;
   }

   public static void disableContextLossTracking() {
      DETECT_CONTEXT_LOSS = false;
   }

   @Nullable
   static Function<Publisher, Publisher> createOrUpdateOpHook(Collection<Function<? super Publisher<Object>, ? extends Publisher<Object>>> hooks) {
      Function<Publisher, Publisher> composite = null;

      for(Function<? super Publisher<Object>, ? extends Publisher<Object>> function : hooks) {
         if (composite != null) {
            composite = composite.andThen(function);
         } else {
            composite = function;
         }
      }

      return composite;
   }

   @Nullable
   static BiFunction<? super Throwable, Object, ? extends Throwable> createOrUpdateOpErrorHook(
      Collection<BiFunction<? super Throwable, Object, ? extends Throwable>> hooks
   ) {
      BiFunction<? super Throwable, Object, ? extends Throwable> composite = null;

      for(BiFunction<? super Throwable, Object, ? extends Throwable> function : hooks) {
         if (composite != null) {
            BiFunction<? super Throwable, Object, ? extends Throwable> ff = composite;
            composite = (e, data) -> (Throwable)function.apply(ff.apply(e, data), data);
         } else {
            composite = function;
         }
      }

      return composite;
   }

   static final Map<String, Function<? super Publisher<Object>, ? extends Publisher<Object>>> getOnEachOperatorHooks() {
      return Collections.unmodifiableMap(onEachOperatorHooks);
   }

   static final Map<String, Function<? super Publisher<Object>, ? extends Publisher<Object>>> getOnLastOperatorHooks() {
      return Collections.unmodifiableMap(onLastOperatorHooks);
   }

   static final Map<String, BiFunction<? super Throwable, Object, ? extends Throwable>> getOnOperatorErrorHooks() {
      return Collections.unmodifiableMap(onOperatorErrorHooks);
   }

   static boolean initStaticGlobalTrace() {
      return Boolean.parseBoolean(System.getProperty("reactor.trace.operatorStacktrace", "false"));
   }

   Hooks() {
   }

   @Nullable
   @Deprecated
   public static <T, P extends Publisher<T>> Publisher<T> addReturnInfo(@Nullable P publisher, String method) {
      return publisher == null ? null : addAssemblyInfo(publisher, new FluxOnAssembly.MethodReturnSnapshot(method));
   }

   @Nullable
   @Deprecated
   public static <T, P extends Publisher<T>> Publisher<T> addCallSiteInfo(@Nullable P publisher, String callSite) {
      return publisher == null ? null : addAssemblyInfo(publisher, new FluxOnAssembly.AssemblySnapshot(callSite));
   }

   static <T, P extends Publisher<T>> Publisher<T> addAssemblyInfo(P publisher, FluxOnAssembly.AssemblySnapshot stacktrace) {
      if (publisher instanceof Callable) {
         return (Publisher<T>)(publisher instanceof Mono
            ? new MonoCallableOnAssembly<>((Mono<? extends T>)publisher, stacktrace)
            : new FluxCallableOnAssembly<>((Flux<? extends T>)publisher, stacktrace));
      } else if (publisher instanceof Mono) {
         return new MonoOnAssembly<>((Mono<? extends T>)publisher, stacktrace);
      } else if (publisher instanceof ParallelFlux) {
         return new ParallelFluxOnAssembly<>((ParallelFlux<T>)publisher, stacktrace);
      } else {
         return (Publisher<T>)(publisher instanceof ConnectableFlux
            ? new ConnectableFluxOnAssembly<>((ConnectableFlux<T>)publisher, stacktrace)
            : new FluxOnAssembly<>((Flux<? extends T>)publisher, stacktrace));
      }
   }

   public static void addQueueWrapper(String key, Function<Queue<?>, Queue<?>> decorator) {
      synchronized(QUEUE_WRAPPERS) {
         QUEUE_WRAPPERS.put(key, decorator);
         Function<Queue<?>, Queue<?>> newHook = null;

         for(Function<Queue<?>, Queue<?>> function : QUEUE_WRAPPERS.values()) {
            if (newHook == null) {
               newHook = function;
            } else {
               newHook = newHook.andThen(function);
            }
         }

         QUEUE_WRAPPER = newHook;
      }
   }

   public static void removeQueueWrapper(String key) {
      synchronized(QUEUE_WRAPPERS) {
         QUEUE_WRAPPERS.remove(key);
         if (QUEUE_WRAPPERS.isEmpty()) {
            QUEUE_WRAPPER = Function.identity();
         } else {
            Function<Queue<?>, Queue<?>> newHook = null;

            for(Function<Queue<?>, Queue<?>> function : QUEUE_WRAPPERS.values()) {
               if (newHook == null) {
                  newHook = function;
               } else {
                  newHook = newHook.andThen(function);
               }
            }

            QUEUE_WRAPPER = newHook;
         }

      }
   }

   public static void removeQueueWrappers() {
      synchronized(QUEUE_WRAPPERS) {
         QUEUE_WRAPPERS.clear();
         QUEUE_WRAPPER = Function.identity();
      }
   }

   public static <T> Queue<T> wrapQueue(Queue<T> queue) {
      return (Queue<T>)QUEUE_WRAPPER.apply(queue);
   }
}
