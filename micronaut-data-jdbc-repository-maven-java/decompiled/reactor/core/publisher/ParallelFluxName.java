package reactor.core.publisher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

final class ParallelFluxName<T> extends ParallelFlux<T> implements Scannable {
   final ParallelFlux<T> source;
   final String name;
   final Set<Tuple2<String, String>> tags;

   static <T> ParallelFlux<T> createOrAppend(ParallelFlux<T> source, String name) {
      Objects.requireNonNull(name, "name");
      if (source instanceof ParallelFluxName) {
         ParallelFluxName<T> s = (ParallelFluxName)source;
         return new ParallelFluxName<>(s.source, name, s.tags);
      } else {
         return new ParallelFluxName<>(source, name, null);
      }
   }

   static <T> ParallelFlux<T> createOrAppend(ParallelFlux<T> source, String tagName, String tagValue) {
      Objects.requireNonNull(tagName, "tagName");
      Objects.requireNonNull(tagValue, "tagValue");
      Set<Tuple2<String, String>> tags = Collections.singleton(Tuples.of(tagName, tagValue));
      if (source instanceof ParallelFluxName) {
         ParallelFluxName<T> s = (ParallelFluxName)source;
         if (s.tags != null) {
            tags = new HashSet(tags);
            tags.addAll(s.tags);
         }

         return new ParallelFluxName<>(s.source, s.name, tags);
      } else {
         return new ParallelFluxName<>(source, null, tags);
      }
   }

   ParallelFluxName(ParallelFlux<T> source, @Nullable String name, @Nullable Set<Tuple2<String, String>> tags) {
      this.source = source;
      this.name = name;
      this.tags = tags;
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.NAME) {
         return this.name;
      } else if (key == Scannable.Attr.TAGS && this.tags != null) {
         return this.tags.stream();
      } else if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      this.source.subscribe(subscribers);
   }
}
