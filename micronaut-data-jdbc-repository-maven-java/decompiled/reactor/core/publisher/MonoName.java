package reactor.core.publisher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

final class MonoName<T> extends InternalMonoOperator<T, T> {
   final String name;
   final Set<Tuple2<String, String>> tags;

   static <T> Mono<T> createOrAppend(Mono<T> source, String name) {
      Objects.requireNonNull(name, "name");
      if (source instanceof MonoName) {
         MonoName<T> s = (MonoName)source;
         return new MonoName<>(s.source, name, s.tags);
      } else if (source instanceof MonoNameFuseable) {
         MonoNameFuseable<T> s = (MonoNameFuseable)source;
         return new MonoNameFuseable<>(s.source, name, s.tags);
      } else {
         return (Mono<T>)(source instanceof Fuseable ? new MonoNameFuseable<>(source, name, null) : new MonoName<>(source, name, null));
      }
   }

   static <T> Mono<T> createOrAppend(Mono<T> source, String tagName, String tagValue) {
      Objects.requireNonNull(tagName, "tagName");
      Objects.requireNonNull(tagValue, "tagValue");
      Set<Tuple2<String, String>> tags = Collections.singleton(Tuples.of(tagName, tagValue));
      if (source instanceof MonoName) {
         MonoName<T> s = (MonoName)source;
         if (s.tags != null) {
            tags = new HashSet(tags);
            tags.addAll(s.tags);
         }

         return new MonoName<>(s.source, s.name, tags);
      } else if (source instanceof MonoNameFuseable) {
         MonoNameFuseable<T> s = (MonoNameFuseable)source;
         if (s.tags != null) {
            tags = new HashSet(tags);
            tags.addAll(s.tags);
         }

         return new MonoNameFuseable<>(s.source, s.name, tags);
      } else {
         return (Mono<T>)(source instanceof Fuseable ? new MonoNameFuseable<>(source, null, tags) : new MonoName<>(source, null, tags));
      }
   }

   MonoName(Mono<? extends T> source, @Nullable String name, @Nullable Set<Tuple2<String, String>> tags) {
      super(source);
      this.name = name;
      this.tags = tags;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return actual;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.NAME) {
         return this.name;
      } else if (key == Scannable.Attr.TAGS && this.tags != null) {
         return this.tags.stream();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
