package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class SignalLogger<IN> implements SignalPeek<IN> {
   static final int CONTEXT_PARENT = 256;
   static final int SUBSCRIBE = 128;
   static final int ON_SUBSCRIBE = 64;
   static final int ON_NEXT = 32;
   static final int ON_ERROR = 16;
   static final int ON_COMPLETE = 8;
   static final int REQUEST = 4;
   static final int CANCEL = 2;
   static final int AFTER_TERMINATE = 1;
   static final int ALL = 510;
   static final AtomicLong IDS = new AtomicLong(1L);
   final CorePublisher<IN> source;
   final Logger log;
   final boolean fuseable;
   final int options;
   final Level level;
   final String operatorLine;
   final long id;
   static final String LOG_TEMPLATE = "{}({})";
   static final String LOG_TEMPLATE_FUSEABLE = "| {}({})";

   SignalLogger(CorePublisher<IN> source, @Nullable String category, Level level, boolean correlateStack, SignalType... options) {
      this(source, category, level, correlateStack, Loggers::getLogger, options);
   }

   SignalLogger(
      CorePublisher<IN> source,
      @Nullable String category,
      Level level,
      boolean correlateStack,
      Function<String, Logger> loggerSupplier,
      @Nullable SignalType... options
   ) {
      this.source = (CorePublisher)Objects.requireNonNull(source, "source");
      this.id = IDS.getAndIncrement();
      this.fuseable = source instanceof Fuseable;
      if (correlateStack) {
         this.operatorLine = Traces.extractOperatorAssemblyInformation((String)((Supplier)Traces.callSiteSupplierFactory.get()).get());
      } else {
         this.operatorLine = null;
      }

      boolean generated = category == null || category.isEmpty() || category.endsWith(".");
      category = generated && category == null ? "reactor." : category;
      if (generated) {
         if (source instanceof Mono) {
            category = category + "Mono." + source.getClass().getSimpleName().replace("Mono", "");
         } else if (source instanceof ParallelFlux) {
            category = category + "Parallel." + source.getClass().getSimpleName().replace("Parallel", "");
         } else {
            category = category + "Flux." + source.getClass().getSimpleName().replace("Flux", "");
         }

         category = category + "." + this.id;
      }

      this.log = (Logger)loggerSupplier.apply(category);
      this.level = level;
      if (options != null && options.length != 0) {
         int opts = 0;

         for(SignalType option : options) {
            if (option == SignalType.CANCEL) {
               opts |= 2;
            } else if (option == SignalType.CURRENT_CONTEXT) {
               opts |= 256;
            } else if (option == SignalType.ON_SUBSCRIBE) {
               opts |= 64;
            } else if (option == SignalType.REQUEST) {
               opts |= 4;
            } else if (option == SignalType.ON_NEXT) {
               opts |= 32;
            } else if (option == SignalType.ON_ERROR) {
               opts |= 16;
            } else if (option == SignalType.ON_COMPLETE) {
               opts |= 8;
            } else if (option == SignalType.SUBSCRIBE) {
               opts |= 128;
            } else if (option == SignalType.AFTER_TERMINATE) {
               opts |= 1;
            }
         }

         this.options = opts;
      } else {
         this.options = 510;
      }

   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.PARENT ? this.source : null;
   }

   void log(SignalType signalType, Object signalValue) {
      String line = this.fuseable ? "| {}({})" : "{}({})";
      if (this.operatorLine != null) {
         line = line + " " + this.operatorLine;
      }

      if (this.level == Level.FINEST) {
         this.log.trace(line, signalType, signalValue);
      } else if (this.level == Level.FINE) {
         this.log.debug(line, signalType, signalValue);
      } else if (this.level == Level.INFO) {
         this.log.info(line, signalType, signalValue);
      } else if (this.level == Level.WARNING) {
         this.log.warn(line, signalType, signalValue);
      } else if (this.level == Level.SEVERE) {
         this.log.error(line, signalType, signalValue);
      }

   }

   void safeLog(SignalType signalType, Object signalValue) {
      if (signalValue instanceof Fuseable.QueueSubscription) {
         signalValue = String.valueOf(signalValue);
         if (this.log.isDebugEnabled()) {
            this.log
               .debug(
                  "A Fuseable Subscription has been passed to the logging framework, this is generally a sign of a misplaced log(), eg. 'window(2).log()' instead of 'window(2).flatMap(w -> w.log())'"
               );
         }
      }

      try {
         this.log(signalType, signalValue);
      } catch (UnsupportedOperationException var4) {
         this.log(signalType, String.valueOf(signalValue));
         if (this.log.isDebugEnabled()) {
            this.log
               .debug(
                  "UnsupportedOperationException has been raised by the logging framework, does your log() placement make sense? eg. 'window(2).log()' instead of 'window(2).flatMap(w -> w.log())'",
                  var4
               );
         }
      }

   }

   static String subscriptionAsString(@Nullable Subscription s) {
      if (s == null) {
         return "null subscription";
      } else {
         StringBuilder asString = new StringBuilder();
         if (s instanceof Fuseable.SynchronousSubscription) {
            asString.append("[Synchronous Fuseable] ");
         } else if (s instanceof Fuseable.QueueSubscription) {
            asString.append("[Fuseable] ");
         }

         Class<? extends Subscription> clazz = s.getClass();
         String name = clazz.getCanonicalName();
         if (name == null) {
            name = clazz.getName();
         }

         name = name.replaceFirst(clazz.getPackage().getName() + ".", "");
         asString.append(name);
         return asString.toString();
      }
   }

   @Nullable
   @Override
   public Consumer<? super Subscription> onSubscribeCall() {
      return (this.options & 64) != 64 || this.level == Level.INFO && !this.log.isInfoEnabled()
         ? null
         : s -> this.log(SignalType.ON_SUBSCRIBE, subscriptionAsString(s));
   }

   @Nullable
   @Override
   public Consumer<? super Context> onCurrentContextCall() {
      return (this.options & 256) != 256
            || (this.level != Level.FINE || !this.log.isDebugEnabled()) && (this.level != Level.FINEST || !this.log.isTraceEnabled())
         ? null
         : c -> this.log(SignalType.CURRENT_CONTEXT, c);
   }

   @Nullable
   @Override
   public Consumer<? super IN> onNextCall() {
      return (this.options & 32) != 32 || this.level == Level.INFO && !this.log.isInfoEnabled() ? null : d -> this.safeLog(SignalType.ON_NEXT, d);
   }

   @Nullable
   @Override
   public Consumer<? super Throwable> onErrorCall() {
      boolean shouldLogAsDebug = this.level == Level.FINE && this.log.isDebugEnabled();
      boolean shouldLogAsTrace = this.level == Level.FINEST && this.log.isTraceEnabled();
      boolean shouldLogAsError = this.level != Level.FINE && this.level != Level.FINEST && this.log.isErrorEnabled();
      if ((this.options & 16) == 16 && (shouldLogAsError || shouldLogAsDebug || shouldLogAsTrace)) {
         String line = this.fuseable ? "| {}({})" : "{}({})";
         if (this.operatorLine != null) {
            line = line + " " + this.operatorLine;
         }

         String s = line;
         if (shouldLogAsTrace) {
            return e -> {
               this.log.trace(s, SignalType.ON_ERROR, e, this.source);
               this.log.trace("", e);
            };
         } else {
            return shouldLogAsDebug ? e -> {
               this.log.debug(s, SignalType.ON_ERROR, e, this.source);
               this.log.debug("", e);
            } : e -> {
               this.log.error(s, SignalType.ON_ERROR, e, this.source);
               this.log.error("", e);
            };
         }
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Runnable onCompleteCall() {
      return (this.options & 8) != 8 || this.level == Level.INFO && !this.log.isInfoEnabled() ? null : () -> this.log(SignalType.ON_COMPLETE, "");
   }

   @Nullable
   @Override
   public Runnable onAfterTerminateCall() {
      return (this.options & 1) != 1 || this.level == Level.INFO && !this.log.isInfoEnabled() ? null : () -> this.log(SignalType.AFTER_TERMINATE, "");
   }

   @Nullable
   @Override
   public LongConsumer onRequestCall() {
      return (this.options & 4) != 4 || this.level == Level.INFO && !this.log.isInfoEnabled()
         ? null
         : n -> this.log(SignalType.REQUEST, Long.MAX_VALUE == n ? "unbounded" : n);
   }

   @Nullable
   @Override
   public Runnable onCancelCall() {
      return (this.options & 2) != 2 || this.level == Level.INFO && !this.log.isInfoEnabled() ? null : () -> this.log(SignalType.CANCEL, "");
   }

   public String toString() {
      return "/loggers/" + this.log.getName() + "/" + this.id;
   }
}
