package reactor.core.scheduler;

import java.util.Collection;
import reactor.core.Disposable;

final class EmptyCompositeDisposable implements Disposable.Composite {
   @Override
   public boolean add(Disposable d) {
      return false;
   }

   @Override
   public boolean addAll(Collection<? extends Disposable> ds) {
      return false;
   }

   @Override
   public boolean remove(Disposable d) {
      return false;
   }

   @Override
   public int size() {
      return 0;
   }

   @Override
   public void dispose() {
   }

   @Override
   public boolean isDisposed() {
      return false;
   }
}
