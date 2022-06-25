package io.micronaut.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class StreamUtils {
   public static <T, A, D> Collector<T, ?, D> maxAll(Comparator<? super T> comparator, Collector<? super T, A, D> downstream) {
      Supplier<A> downstreamSupplier = downstream.supplier();
      BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
      BinaryOperator<A> downstreamCombiner = downstream.combiner();

      class Container {
         A acc;
         T obj;
         boolean hasAny;

         Container(A acc) {
            this.acc = acc;
         }
      }

      Supplier<Container> supplier = () -> new Container((A)downstreamSupplier.get());
      BiConsumer<Container, T> accumulator = (acc, t) -> {
         if (!acc.hasAny) {
            downstreamAccumulator.accept(acc.acc, t);
            acc.obj = (T)t;
            acc.hasAny = true;
         } else {
            int cmp = comparator.compare(t, acc.obj);
            if (cmp > 0) {
               acc.acc = (A)downstreamSupplier.get();
               acc.obj = (T)t;
            }

            if (cmp >= 0) {
               downstreamAccumulator.accept(acc.acc, t);
            }
         }

      };
      BinaryOperator<Container> combiner = (acc1, acc2) -> {
         if (!acc2.hasAny) {
            return acc1;
         } else if (!acc1.hasAny) {
            return acc2;
         } else {
            int cmp = comparator.compare(acc1.obj, acc2.obj);
            if (cmp > 0) {
               return acc1;
            } else if (cmp < 0) {
               return acc2;
            } else {
               acc1.acc = (A)downstreamCombiner.apply(acc1.acc, acc2.acc);
               return acc1;
            }
         }
      };
      Function<Container, D> finisher = acc -> downstream.finisher().apply(acc.acc);
      return Collector.of(supplier, accumulator, combiner, finisher);
   }

   public static <T, A, D> Collector<T, ?, D> minAll(Comparator<? super T> comparator, Collector<? super T, A, D> downstream) {
      Supplier<A> downstreamSupplier = downstream.supplier();
      BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
      BinaryOperator<A> downstreamCombiner = downstream.combiner();

      class Container {
         A acc;
         T obj;
         boolean hasAny;

         Container(A acc) {
            this.acc = acc;
         }
      }

      Supplier<Container> supplier = () -> new Container((A)downstreamSupplier.get());
      BiConsumer<Container, T> accumulator = (acc, t) -> {
         if (!acc.hasAny) {
            downstreamAccumulator.accept(acc.acc, t);
            acc.obj = (T)t;
            acc.hasAny = true;
         } else {
            int cmp = comparator.compare(t, acc.obj);
            if (cmp < 0) {
               acc.acc = (A)downstreamSupplier.get();
               acc.obj = (T)t;
            }

            if (cmp <= 0) {
               downstreamAccumulator.accept(acc.acc, t);
            }
         }

      };
      BinaryOperator<Container> combiner = (acc1, acc2) -> {
         if (!acc2.hasAny) {
            return acc1;
         } else if (!acc1.hasAny) {
            return acc2;
         } else {
            int cmp = comparator.compare(acc1.obj, acc2.obj);
            if (cmp < 0) {
               return acc1;
            } else if (cmp > 0) {
               return acc2;
            } else {
               acc1.acc = (A)downstreamCombiner.apply(acc1.acc, acc2.acc);
               return acc1;
            }
         }
      };
      Function<Container, D> finisher = acc -> downstream.finisher().apply(acc.acc);
      return Collector.of(supplier, accumulator, combiner, finisher);
   }

   public static <T, A extends Collection<T>> Collector<T, A, Collection<T>> toImmutableCollection(Supplier<A> collectionFactory) {
      return Collector.of(collectionFactory, Collection::add, (left, right) -> {
         left.addAll(right);
         return left;
      }, Collections::unmodifiableCollection);
   }

   public static <T> Collector<T, Collection<T>, Collection<T>> toImmutableCollection() {
      return toImmutableCollection(ArrayList::new);
   }
}
