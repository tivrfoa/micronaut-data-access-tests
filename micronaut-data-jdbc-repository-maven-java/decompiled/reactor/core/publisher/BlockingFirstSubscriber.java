package reactor.core.publisher;

final class BlockingFirstSubscriber<T> extends BlockingSingleSubscriber<T> {
   @Override
   public void onNext(T t) {
      if (this.value == null) {
         this.value = t;
         this.dispose();
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
      return "blockFirst";
   }
}
