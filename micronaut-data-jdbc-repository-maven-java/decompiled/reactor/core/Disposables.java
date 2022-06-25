package reactor.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import reactor.util.annotation.Nullable;

public final class Disposables {
   static final Disposable DISPOSED = disposed();

   private Disposables() {
   }

   public static Disposable.Composite composite() {
      return new Disposables.ListCompositeDisposable();
   }

   public static Disposable.Composite composite(Disposable... disposables) {
      return new Disposables.ListCompositeDisposable(disposables);
   }

   public static Disposable.Composite composite(Iterable<? extends Disposable> disposables) {
      return new Disposables.ListCompositeDisposable(disposables);
   }

   public static Disposable disposed() {
      return new Disposables.AlwaysDisposable();
   }

   public static Disposable never() {
      return new Disposables.NeverDisposable();
   }

   public static Disposable single() {
      return new Disposables.SimpleDisposable();
   }

   public static Disposable.Swap swap() {
      return new Disposables.SwapDisposable();
   }

   static <T> boolean set(AtomicReferenceFieldUpdater<T, Disposable> updater, T holder, @Nullable Disposable newValue) {
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

   static <T> boolean replace(AtomicReferenceFieldUpdater<T, Disposable> updater, T holder, @Nullable Disposable newValue) {
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

   static <T> boolean dispose(AtomicReferenceFieldUpdater<T, Disposable> updater, T holder) {
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

   static boolean isDisposed(Disposable d) {
      return d == DISPOSED;
   }

   static final class AlwaysDisposable implements Disposable {
      @Override
      public void dispose() {
      }

      @Override
      public boolean isDisposed() {
         return true;
      }
   }

   static final class ListCompositeDisposable implements Disposable.Composite, Scannable {
      @Nullable
      List<Disposable> resources;
      volatile boolean disposed;

      ListCompositeDisposable() {
      }

      ListCompositeDisposable(Disposable... resources) {
         Objects.requireNonNull(resources, "resources is null");
         this.resources = new LinkedList();

         for(Disposable d : resources) {
            Objects.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
         }

      }

      ListCompositeDisposable(Iterable<? extends Disposable> resources) {
         Objects.requireNonNull(resources, "resources is null");
         this.resources = new LinkedList();

         for(Disposable d : resources) {
            Objects.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
         }

      }

      @Override
      public void dispose() {
         if (!this.disposed) {
            List<Disposable> set;
            synchronized(this) {
               if (this.disposed) {
                  return;
               }

               this.disposed = true;
               set = this.resources;
               this.resources = null;
            }

            this.dispose(set);
         }
      }

      @Override
      public boolean isDisposed() {
         return this.disposed;
      }

      @Override
      public boolean add(Disposable d) {
         Objects.requireNonNull(d, "d is null");
         if (!this.disposed) {
            synchronized(this) {
               if (!this.disposed) {
                  List<Disposable> set = this.resources;
                  if (set == null) {
                     set = new LinkedList();
                     this.resources = set;
                  }

                  set.add(d);
                  return true;
               }
            }
         }

         d.dispose();
         return false;
      }

      @Override
      public boolean addAll(Collection<? extends Disposable> ds) {
         Objects.requireNonNull(ds, "ds is null");
         if (!this.disposed) {
            synchronized(this) {
               if (!this.disposed) {
                  List<Disposable> set = this.resources;
                  if (set == null) {
                     set = new LinkedList();
                     this.resources = set;
                  }

                  for(Disposable d : ds) {
                     Objects.requireNonNull(d, "d is null");
                     set.add(d);
                  }

                  return true;
               }
            }
         }

         for(Disposable d : ds) {
            d.dispose();
         }

         return false;
      }

      @Override
      public boolean remove(Disposable d) {
         Objects.requireNonNull(d, "Disposable item is null");
         if (this.disposed) {
            return false;
         } else {
            synchronized(this) {
               if (this.disposed) {
                  return false;
               } else {
                  List<Disposable> set = this.resources;
                  return set != null && set.remove(d);
               }
            }
         }
      }

      @Override
      public int size() {
         List<Disposable> r = this.resources;
         return r == null ? 0 : r.size();
      }

      Stream<Disposable> asStream() {
         List<Disposable> r = this.resources;
         return r == null ? Stream.empty() : r.stream();
      }

      public void clear() {
         if (!this.disposed) {
            List<Disposable> set;
            synchronized(this) {
               if (this.disposed) {
                  return;
               }

               set = this.resources;
               this.resources = null;
            }

            this.dispose(set);
         }
      }

      void dispose(@Nullable List<Disposable> set) {
         if (set != null) {
            List<Throwable> errors = null;

            for(Disposable o : set) {
               try {
                  o.dispose();
               } catch (Throwable var6) {
                  Exceptions.throwIfFatal(var6);
                  if (errors == null) {
                     errors = new ArrayList();
                  }

                  errors.add(var6);
               }
            }

            if (errors != null) {
               if (errors.size() == 1) {
                  throw Exceptions.propagate((Throwable)errors.get(0));
               } else {
                  throw Exceptions.multiple(errors);
               }
            }
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return this.asStream().filter(Scannable.class::isInstance).map(Scannable::from);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.CANCELLED ? this.isDisposed() : null;
      }
   }

   static final class NeverDisposable implements Disposable {
      @Override
      public void dispose() {
      }

      @Override
      public boolean isDisposed() {
         return false;
      }
   }

   static final class SimpleDisposable extends AtomicBoolean implements Disposable {
      @Override
      public void dispose() {
         this.set(true);
      }

      @Override
      public boolean isDisposed() {
         return this.get();
      }
   }

   static final class SwapDisposable implements Disposable.Swap {
      volatile Disposable inner;
      static final AtomicReferenceFieldUpdater<Disposables.SwapDisposable, Disposable> INNER = AtomicReferenceFieldUpdater.newUpdater(
         Disposables.SwapDisposable.class, Disposable.class, "inner"
      );

      @Override
      public boolean update(@Nullable Disposable next) {
         return Disposables.set(INNER, this, next);
      }

      @Override
      public boolean replace(@Nullable Disposable next) {
         return Disposables.replace(INNER, this, next);
      }

      @Nullable
      public Disposable get() {
         return this.inner;
      }

      @Override
      public void dispose() {
         Disposables.dispose(INNER, this);
      }

      @Override
      public boolean isDisposed() {
         return Disposables.isDisposed((Disposable)INNER.get(this));
      }
   }
}
