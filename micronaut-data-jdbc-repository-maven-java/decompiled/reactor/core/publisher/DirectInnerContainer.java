package reactor.core.publisher;

interface DirectInnerContainer<T> {
   boolean add(SinkManyBestEffort.DirectInner<T> var1);

   void remove(SinkManyBestEffort.DirectInner<T> var1);
}
