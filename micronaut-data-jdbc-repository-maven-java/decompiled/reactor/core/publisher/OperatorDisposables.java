package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.util.annotation.Nullable;

final class OperatorDisposables {
   static final Disposable DISPOSED = Disposables.disposed();

   public static <T> boolean set(AtomicReferenceFieldUpdater<T, Disposable> updater, T holder, @Nullable Disposable newValue) {
      Disposable current;
      do {
         current = (Disposable)updater.get(holder);
         if (current == DISPOSED) {
            if (newValue != null) {
               newValue.dispose();
            }

            return false;
         }
      } while(!updater.compareAndSet(holder, current, newValue));

      if (current != null) {
         current.dispose();
      }

      return true;
   }

   public static <T> boolean setOnce(
      AtomicReferenceFieldUpdater<T, Disposable> updater, T holder, Disposable newValue, Consumer<RuntimeException> errorCallback
   ) {
      Objects.requireNonNull(newValue, "newValue is null");
      if (!updater.compareAndSet(holder, null, newValue)) {
         newValue.dispose();
         if (updater.get(holder) != DISPOSED) {
            errorCallback.accept(new IllegalStateException("Disposable already pushed"));
         }

         return false;
      } else {
         return true;
      }
   }

   public static <T> boolean replace(AtomicReferenceFieldUpdater<T, Disposable> updater, T holder, @Nullable Disposable newValue) {
      Disposable current;
      do {
         current = (Disposable)updater.get(holder);
         if (current == DISPOSED) {
            if (newValue != null) {
               newValue.dispose();
            }

            return false;
         }
      } while(!updater.compareAndSet(holder, current, newValue));

      return true;
   }

   public static <T> boolean dispose(AtomicReferenceFieldUpdater<T, Disposable> updater, T holder) {
      Disposable current = (Disposable)updater.get(holder);
      Disposable d = DISPOSED;
      if (current != d) {
         current = (Disposable)updater.getAndSet(holder, d);
         if (current != d) {
            if (current != null) {
               current.dispose();
            }

            return true;
         }
      }

      return false;
   }

   public static boolean validate(@Nullable Disposable current, Disposable next, Consumer<RuntimeException> errorCallback) {
      if (next == null) {
         errorCallback.accept(new NullPointerException("next is null"));
         return false;
      } else if (current != null) {
         next.dispose();
         errorCallback.accept(new IllegalStateException("Disposable already pushed"));
         return false;
      } else {
         return true;
      }
   }

   public static <T> boolean trySet(AtomicReferenceFieldUpdater<T, Disposable> updater, T holder, Disposable newValue) {
      if (!updater.compareAndSet(holder, null, newValue)) {
         if (updater.get(holder) == DISPOSED) {
            newValue.dispose();
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean isDisposed(Disposable d) {
      return d == DISPOSED;
   }
}
