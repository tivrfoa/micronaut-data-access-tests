package reactor.core;

import java.util.Collection;
import java.util.function.Supplier;
import reactor.util.annotation.Nullable;

@FunctionalInterface
public interface Disposable {
   void dispose();

   default boolean isDisposed() {
      return false;
   }

   public interface Composite extends Disposable {
      boolean add(Disposable var1);

      default boolean addAll(Collection<? extends Disposable> ds) {
         boolean abort = this.isDisposed();

         for(Disposable d : ds) {
            if (abort) {
               d.dispose();
            } else {
               abort = !this.add(d);
            }
         }

         return !abort;
      }

      @Override
      void dispose();

      @Override
      boolean isDisposed();

      boolean remove(Disposable var1);

      int size();
   }

   public interface Swap extends Disposable, Supplier<Disposable> {
      boolean update(@Nullable Disposable var1);

      boolean replace(@Nullable Disposable var1);
   }
}
