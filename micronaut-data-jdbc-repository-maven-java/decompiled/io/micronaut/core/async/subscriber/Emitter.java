package io.micronaut.core.async.subscriber;

public interface Emitter<T> extends Completable {
   void onNext(T t);

   void onError(Throwable t);
}
