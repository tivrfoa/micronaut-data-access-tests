package io.micronaut.core.async.publisher;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.async.subscriber.Completable;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.optim.StaticOptimizations;
import io.micronaut.core.reflect.ClassUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Internal
@TypeHint({Publishers.class})
public class Publishers {
   private static final List<Class<?>> REACTIVE_TYPES;
   private static final List<Class<?>> SINGLE_TYPES;
   private static final List<Class<?>> COMPLETABLE_TYPES;

   public static void registerReactiveType(Class<?> type) {
      if (type != null) {
         REACTIVE_TYPES.add(type);
      }

   }

   public static void registerReactiveSingle(Class<?> type) {
      if (type != null) {
         registerReactiveType(type);
         SINGLE_TYPES.add(type);
      }

   }

   public static void registerReactiveCompletable(Class<?> type) {
      if (type != null) {
         registerReactiveType(type);
         COMPLETABLE_TYPES.add(type);
      }

   }

   public static List<Class<?>> getKnownReactiveTypes() {
      return Collections.unmodifiableList(new ArrayList(REACTIVE_TYPES));
   }

   public static List<Class<?>> getKnownSingleTypes() {
      return Collections.unmodifiableList(new ArrayList(SINGLE_TYPES));
   }

   public static List<Class<?>> getKnownCompletableTypes() {
      return Collections.unmodifiableList(new ArrayList(COMPLETABLE_TYPES));
   }

   public static <T> Publisher<T> fromCompletableFuture(Supplier<CompletableFuture<T>> futureSupplier) {
      return new CompletableFuturePublisher<>(futureSupplier);
   }

   public static <T> Publisher<T> fromCompletableFuture(CompletableFuture<T> future) {
      return new CompletableFuturePublisher<>(() -> future);
   }

   public static <T> Publisher<T> just(T value) {
      return new Publishers.JustPublisher<>(value);
   }

   public static <T> Publisher<T> just(Throwable error) {
      return new Publishers.JustThrowPublisher<>(error);
   }

   public static <T> Publisher<T> empty() {
      return new Publishers.JustCompletePublisher<>();
   }

   public static <T, R> Publisher<R> map(Publisher<T> publisher, Function<T, R> mapper) {
      return actual -> publisher.subscribe(new CompletionAwareSubscriber<T>() {
            @Override
            protected void doOnSubscribe(Subscription subscription) {
               actual.onSubscribe(subscription);
            }

            @Override
            protected void doOnNext(T message) {
               try {
                  Object result = Objects.requireNonNull(mapper.apply(message), "The mapper returned a null value.");
                  actual.onNext(result);
               } catch (Throwable var3) {
                  this.onError(var3);
               }

            }

            @Override
            protected void doOnError(Throwable t) {
               actual.onError(t);
            }

            @Override
            protected void doOnComplete() {
               actual.onComplete();
            }
         });
   }

   public static <T, R> Publisher<R> mapOrSupplyEmpty(Publisher<T> publisher, Publishers.MapOrSupplyEmpty<T, R> mapOrSupplyEmpty) {
      return actual -> publisher.subscribe(new CompletionAwareSubscriber<T>() {
            AtomicBoolean resultPresent = new AtomicBoolean();

            @Override
            protected void doOnSubscribe(Subscription subscription) {
               actual.onSubscribe(subscription);
            }

            @Override
            protected void doOnNext(T message) {
               try {
                  Object result = Objects.requireNonNull(mapOrSupplyEmpty.map(message), "The mapper returned a null value.");
                  actual.onNext(result);
                  this.resultPresent.set(true);
               } catch (Throwable var3) {
                  this.onError(var3);
               }

            }

            @Override
            protected void doOnError(Throwable t) {
               actual.onError(t);
            }

            @Override
            protected void doOnComplete() {
               if (!this.resultPresent.get()) {
                  actual.onNext(mapOrSupplyEmpty.supplyEmpty());
               }

               actual.onComplete();
            }
         });
   }

   public static <T> Publisher<T> then(Publisher<T> publisher, Consumer<T> consumer) {
      return actual -> publisher.subscribe(new CompletionAwareSubscriber<T>() {
            @Override
            protected void doOnSubscribe(Subscription subscription) {
               actual.onSubscribe(subscription);
            }

            @Override
            protected void doOnNext(T message) {
               try {
                  consumer.accept(message);
                  actual.onNext(message);
               } catch (Throwable var3) {
                  this.onError(var3);
               }

            }

            @Override
            protected void doOnError(Throwable t) {
               actual.onError(t);
            }

            @Override
            protected void doOnComplete() {
               actual.onComplete();
            }
         });
   }

   public static <T> Publisher<T> onComplete(Publisher<T> publisher, Supplier<CompletableFuture<Void>> future) {
      return actual -> publisher.subscribe(new CompletionAwareSubscriber<T>() {
            @Override
            protected void doOnSubscribe(Subscription subscription) {
               actual.onSubscribe(subscription);
            }

            @Override
            protected void doOnNext(T message) {
               try {
                  actual.onNext(message);
               } catch (Throwable var3) {
                  this.onError(var3);
               }

            }

            @Override
            protected void doOnError(Throwable t) {
               actual.onError(t);
            }

            @Override
            protected void doOnComplete() {
               ((CompletableFuture)future.get()).whenComplete((aVoid, throwable) -> {
                  if (throwable != null) {
                     actual.onError(throwable);
                  } else {
                     actual.onComplete();
                  }

               });
            }
         });
   }

   public static boolean isConvertibleToPublisher(Class<?> type) {
      if (Publisher.class.isAssignableFrom(type)) {
         return true;
      } else if (!type.isPrimitive() && !packageOf(type).startsWith("java.")) {
         for(Class<?> reactiveType : REACTIVE_TYPES) {
            if (reactiveType.isAssignableFrom(type)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static String packageOf(Class<?> type) {
      Package pkg = type.getPackage();
      return pkg == null ? "" : pkg.getName();
   }

   public static boolean isConvertibleToPublisher(Object object) {
      if (object == null) {
         return false;
      } else {
         return object instanceof Publisher ? true : isConvertibleToPublisher(object.getClass());
      }
   }

   public static <T> T convertPublisher(Object object, Class<T> publisherType) {
      Objects.requireNonNull(object, "Argument [object] cannot be null");
      Objects.requireNonNull(publisherType, "Argument [publisherType] cannot be null");
      if (publisherType.isInstance(object)) {
         return (T)object;
      } else if (object instanceof CompletableFuture) {
         Publisher<T> futurePublisher = fromCompletableFuture((Supplier<CompletableFuture<T>>)(() -> (CompletableFuture)object));
         return (T)ConversionService.SHARED.convert(futurePublisher, publisherType).orElseThrow(() -> unconvertibleError(object, publisherType));
      } else {
         return (T)(object instanceof Publishers.MicronautPublisher && Publishers.MicronautPublisher.class.isAssignableFrom(publisherType)
            ? object
            : ConversionService.SHARED.convert(object, publisherType).orElseThrow(() -> unconvertibleError(object, publisherType)));
      }
   }

   public static boolean isSingle(Class<?> type) {
      for(Class<?> reactiveType : SINGLE_TYPES) {
         if (reactiveType.isAssignableFrom(type)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isCompletable(Class<?> type) {
      for(Class<?> reactiveType : COMPLETABLE_TYPES) {
         if (reactiveType.isAssignableFrom(type)) {
            return true;
         }
      }

      return false;
   }

   private static <T> IllegalArgumentException unconvertibleError(Object object, Class<T> publisherType) {
      return new IllegalArgumentException(
         "Cannot convert reactive type ["
            + object.getClass()
            + "] to type ["
            + publisherType
            + "]. Ensure that you have the necessary Reactive module on your classpath. For example for Reactor you should have 'micronaut-reactor'."
      );
   }

   static {
      ClassLoader classLoader = Publishers.class.getClassLoader();
      Optional<PublishersOptimizations> publishers = StaticOptimizations.get(PublishersOptimizations.class);
      List<Class<?>> reactiveTypes;
      List<Class<?>> singleTypes;
      List<Class<?>> completableTypes;
      if (publishers.isPresent()) {
         PublishersOptimizations optimizations = (PublishersOptimizations)publishers.get();
         reactiveTypes = optimizations.getReactiveTypes();
         singleTypes = optimizations.getSingleTypes();
         completableTypes = optimizations.getCompletableTypes();
      } else {
         reactiveTypes = new ArrayList(3);
         singleTypes = new ArrayList(3);
         completableTypes = new ArrayList(3);
         singleTypes.add(CompletableFuturePublisher.class);
         singleTypes.add(Publishers.JustPublisher.class);
         completableTypes.add(Completable.class);

         for(String name : Arrays.asList(
            "io.reactivex.Observable",
            "reactor.core.publisher.Flux",
            "kotlinx.coroutines.flow.Flow",
            "io.reactivex.rxjava3.core.Flowable",
            "io.reactivex.rxjava3.core.Observable"
         )) {
            Optional<Class> aClass = ClassUtils.forName(name, classLoader);
            aClass.ifPresent(reactiveTypes::add);
         }

         for(String name : Arrays.asList(
            "io.reactivex.Single", "reactor.core.publisher.Mono", "io.reactivex.Maybe", "io.reactivex.rxjava3.core.Single", "io.reactivex.rxjava3.core.Maybe"
         )) {
            Optional<Class> aClass = ClassUtils.forName(name, classLoader);
            aClass.ifPresent(aClass1 -> {
               singleTypes.add(aClass1);
               reactiveTypes.add(aClass1);
            });
         }

         for(String name : Arrays.asList("io.reactivex.Completable", "io.reactivex.rxjava3.core.Completable")) {
            Optional<Class> aClass = ClassUtils.forName(name, classLoader);
            aClass.ifPresent(aClass1 -> {
               completableTypes.add(aClass1);
               reactiveTypes.add(aClass1);
            });
         }
      }

      REACTIVE_TYPES = reactiveTypes;
      SINGLE_TYPES = singleTypes;
      COMPLETABLE_TYPES = completableTypes;
   }

   private static class JustCompletePublisher<T> implements Publishers.MicronautPublisher<T> {
      private JustCompletePublisher() {
      }

      @Override
      public void subscribe(Subscriber<? super T> subscriber) {
         subscriber.onSubscribe(new Subscription() {
            boolean done;

            @Override
            public void request(long n) {
               if (!this.done) {
                  this.done = true;
                  subscriber.onComplete();
               }
            }

            @Override
            public void cancel() {
               this.done = true;
            }
         });
      }
   }

   @Internal
   public static class JustPublisher<T> implements Publishers.MicronautPublisher<T> {
      private final T value;

      public JustPublisher(T value) {
         this.value = value;
      }

      @Override
      public void subscribe(Subscriber<? super T> subscriber) {
         subscriber.onSubscribe(new Subscription() {
            boolean done;

            @Override
            public void request(long n) {
               if (!this.done) {
                  this.done = true;
                  if (JustPublisher.this.value != null) {
                     subscriber.onNext(JustPublisher.this.value);
                  }

                  subscriber.onComplete();
               }
            }

            @Override
            public void cancel() {
               this.done = true;
            }
         });
      }
   }

   private static class JustThrowPublisher<T> implements Publishers.MicronautPublisher<T> {
      private final Throwable error;

      public JustThrowPublisher(Throwable error) {
         this.error = error;
      }

      @Override
      public void subscribe(Subscriber<? super T> subscriber) {
         subscriber.onSubscribe(new Subscription() {
            boolean done;

            @Override
            public void request(long n) {
               if (!this.done) {
                  this.done = true;
                  subscriber.onError(JustThrowPublisher.this.error);
               }
            }

            @Override
            public void cancel() {
               this.done = true;
            }
         });
      }
   }

   public interface MapOrSupplyEmpty<T, R> {
      @NonNull
      R map(@NonNull T result);

      @NonNull
      R supplyEmpty();
   }

   public interface MicronautPublisher<T> extends Publisher<T> {
   }
}
