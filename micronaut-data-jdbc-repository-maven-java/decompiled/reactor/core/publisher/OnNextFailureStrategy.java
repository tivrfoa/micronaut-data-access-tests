package reactor.core.publisher;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import reactor.core.Exceptions;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

interface OnNextFailureStrategy extends BiFunction<Throwable, Object, Throwable>, BiPredicate<Throwable, Object> {
   String KEY_ON_NEXT_ERROR_STRATEGY = "reactor.onNextError.localStrategy";
   OnNextFailureStrategy STOP = new OnNextFailureStrategy() {
      @Override
      public boolean test(Throwable error, @Nullable Object value) {
         return false;
      }

      @Override
      public Throwable process(Throwable error, @Nullable Object value, Context context) {
         Exceptions.throwIfFatal(error);
         Throwable iee = new IllegalStateException("STOP strategy cannot process errors");
         iee.addSuppressed(error);
         return iee;
      }
   };
   OnNextFailureStrategy RESUME_DROP = new OnNextFailureStrategy.ResumeDropStrategy(null);

   @Nullable
   default Throwable apply(Throwable throwable, @Nullable Object o) {
      return this.process(throwable, o, Context.empty());
   }

   boolean test(Throwable var1, @Nullable Object var2);

   @Nullable
   Throwable process(Throwable var1, @Nullable Object var2, Context var3);

   static OnNextFailureStrategy stop() {
      return STOP;
   }

   static OnNextFailureStrategy resumeDrop() {
      return RESUME_DROP;
   }

   static OnNextFailureStrategy resumeDropIf(Predicate<Throwable> causePredicate) {
      return new OnNextFailureStrategy.ResumeDropStrategy(causePredicate);
   }

   static OnNextFailureStrategy resume(BiConsumer<Throwable, Object> errorConsumer) {
      return new OnNextFailureStrategy.ResumeStrategy(null, errorConsumer);
   }

   static OnNextFailureStrategy resumeIf(Predicate<Throwable> causePredicate, BiConsumer<Throwable, Object> errorConsumer) {
      return new OnNextFailureStrategy.ResumeStrategy(causePredicate, errorConsumer);
   }

   public static final class LambdaOnNextErrorStrategy implements OnNextFailureStrategy {
      private final BiFunction<? super Throwable, Object, ? extends Throwable> delegateProcessor;
      private final BiPredicate<? super Throwable, Object> delegatePredicate;

      public LambdaOnNextErrorStrategy(BiFunction<? super Throwable, Object, ? extends Throwable> delegateProcessor) {
         this.delegateProcessor = delegateProcessor;
         if (delegateProcessor instanceof BiPredicate) {
            this.delegatePredicate = (BiPredicate)delegateProcessor;
         } else {
            this.delegatePredicate = (e, v) -> true;
         }

      }

      @Override
      public boolean test(Throwable error, @Nullable Object value) {
         return this.delegatePredicate.test(error, value);
      }

      @Nullable
      @Override
      public Throwable process(Throwable error, @Nullable Object value, Context ignored) {
         return (Throwable)this.delegateProcessor.apply(error, value);
      }
   }

   public static final class ResumeDropStrategy implements OnNextFailureStrategy {
      final Predicate<Throwable> errorPredicate;

      ResumeDropStrategy(@Nullable Predicate<Throwable> errorPredicate) {
         this.errorPredicate = errorPredicate;
      }

      @Override
      public boolean test(Throwable error, @Nullable Object value) {
         return this.errorPredicate == null || this.errorPredicate.test(error);
      }

      @Nullable
      @Override
      public Throwable process(Throwable error, @Nullable Object value, Context context) {
         if (this.errorPredicate == null) {
            Exceptions.throwIfFatal(error);
         } else if (!this.errorPredicate.test(error)) {
            Exceptions.throwIfFatal(error);
            return error;
         }

         try {
            if (value != null) {
               Operators.onNextDropped(value, context);
            }

            Operators.onErrorDropped(error, context);
            return null;
         } catch (Throwable var5) {
            return Exceptions.addSuppressed(var5, error);
         }
      }
   }

   public static final class ResumeStrategy implements OnNextFailureStrategy {
      final Predicate<Throwable> errorPredicate;
      final BiConsumer<Throwable, Object> errorConsumer;

      ResumeStrategy(@Nullable Predicate<Throwable> errorPredicate, BiConsumer<Throwable, Object> errorConsumer) {
         this.errorPredicate = errorPredicate;
         this.errorConsumer = errorConsumer;
      }

      @Override
      public boolean test(Throwable error, @Nullable Object value) {
         return this.errorPredicate == null || this.errorPredicate.test(error);
      }

      @Nullable
      @Override
      public Throwable process(Throwable error, @Nullable Object value, Context context) {
         if (this.errorPredicate == null) {
            Exceptions.throwIfFatal(error);
         } else if (!this.errorPredicate.test(error)) {
            Exceptions.throwIfFatal(error);
            return error;
         }

         try {
            this.errorConsumer.accept(error, value);
            return null;
         } catch (Throwable var5) {
            return Exceptions.addSuppressed(var5, error);
         }
      }
   }
}
