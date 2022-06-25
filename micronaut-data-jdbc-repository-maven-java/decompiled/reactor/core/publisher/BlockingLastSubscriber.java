package reactor.core.publisher;

final class BlockingLastSubscriber<T> extends BlockingSingleSubscriber<T> {
   @Override
   public void onNext(T t) {
      this.value = t;
   }

   @Override
   public void onError(Throwable t) {
      this.value = null;
      this.error = t;
      this.countDown();
   }

   @Override
   public String stepName() {
      return "blockLast";
   }
}
