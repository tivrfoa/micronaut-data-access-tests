package reactor.core.publisher;

final class BlockingMonoSubscriber<T> extends BlockingSingleSubscriber<T> {
   @Override
   public void onNext(T t) {
      if (this.value == null) {
         this.value = t;
         this.countDown();
      }

   }

   @Override
   public void onError(Throwable t) {
      if (this.value == null) {
         this.error = t;
      }

      this.countDown();
   }

   @Override
   public String stepName() {
      return "block";
   }
}
