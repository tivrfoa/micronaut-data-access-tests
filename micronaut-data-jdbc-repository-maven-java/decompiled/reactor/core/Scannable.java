package reactor.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;

@FunctionalInterface
public interface Scannable {
   Pattern OPERATOR_NAME_UNRELATED_WORDS_PATTERN = Pattern.compile("Parallel|Flux|Mono|Publisher|Subscriber|Fuseable|Operator|Conditional");

   static Scannable from(@Nullable Object o) {
      if (o == null) {
         return Scannable.Attr.NULL_SCAN;
      } else {
         return o instanceof Scannable ? (Scannable)o : Scannable.Attr.UNAVAILABLE_SCAN;
      }
   }

   default Stream<? extends Scannable> actuals() {
      return Scannable.Attr.recurse(this, Scannable.Attr.ACTUAL);
   }

   default Stream<? extends Scannable> inners() {
      return Stream.empty();
   }

   default boolean isScanAvailable() {
      return true;
   }

   default String name() {
      String thisName = this.scan(Scannable.Attr.NAME);
      return thisName != null
         ? thisName
         : (String)this.parents().map(s -> s.scan(Scannable.Attr.NAME)).filter(Objects::nonNull).findFirst().orElse(this.stepName());
   }

   default String stepName() {
      String name = this.getClass().getName();
      int innerClassIndex = name.indexOf(36);
      if (innerClassIndex != -1) {
         name = name.substring(0, innerClassIndex);
      }

      int stripPackageIndex = name.lastIndexOf(46);
      if (stripPackageIndex != -1) {
         name = name.substring(stripPackageIndex + 1);
      }

      String stripped = OPERATOR_NAME_UNRELATED_WORDS_PATTERN.matcher(name).replaceAll("");
      return !stripped.isEmpty() ? stripped.substring(0, 1).toLowerCase() + stripped.substring(1) : stripped;
   }

   default Stream<String> steps() {
      List<Scannable> chain = new ArrayList();
      chain.addAll((Collection)this.parents().collect(Collectors.toList()));
      Collections.reverse(chain);
      chain.add(this);
      chain.addAll((Collection)this.actuals().collect(Collectors.toList()));
      List<String> chainNames = new ArrayList(chain.size());

      for(int i = 0; i < chain.size(); ++i) {
         Scannable step = (Scannable)chain.get(i);
         Scannable stepAfter = null;
         if (i < chain.size() - 1) {
            stepAfter = (Scannable)chain.get(i + 1);
         }

         if (stepAfter != null && stepAfter.scan(Scannable.Attr.ACTUAL_METADATA)) {
            chainNames.add(stepAfter.stepName());
            ++i;
         } else {
            chainNames.add(step.stepName());
         }
      }

      return chainNames.stream();
   }

   default Stream<? extends Scannable> parents() {
      return Scannable.Attr.recurse(this, Scannable.Attr.PARENT);
   }

   @Nullable
   Object scanUnsafe(Scannable.Attr var1);

   @Nullable
   default <T> T scan(Scannable.Attr<T> key) {
      T value = key.tryConvert(this.scanUnsafe(key));
      return (T)(value == null ? key.defaultValue() : value);
   }

   default <T> T scanOrDefault(Scannable.Attr<T> key, T defaultValue) {
      T v = key.tryConvert(this.scanUnsafe(key));
      return (T)(v == null ? Objects.requireNonNull(defaultValue, "defaultValue") : v);
   }

   default Stream<Tuple2<String, String>> tags() {
      Stream<Tuple2<String, String>> parentTags = this.parents().flatMap(s -> s.scan(Scannable.Attr.TAGS));
      Stream<Tuple2<String, String>> thisTags = this.scan(Scannable.Attr.TAGS);
      return thisTags == null ? parentTags : Stream.concat(thisTags, parentTags);
   }

   public static class Attr<T> {
      public static final Scannable.Attr<Scannable> ACTUAL = new Scannable.Attr<>(null, Scannable::from);
      public static final Scannable.Attr<Boolean> ACTUAL_METADATA = new Scannable.Attr((T)false);
      public static final Scannable.Attr<Integer> BUFFERED = new Scannable.Attr(0);
      public static final Scannable.Attr<Integer> CAPACITY = new Scannable.Attr(0);
      public static final Scannable.Attr<Boolean> CANCELLED = new Scannable.Attr((T)false);
      public static final Scannable.Attr<Boolean> DELAY_ERROR = new Scannable.Attr((T)false);
      public static final Scannable.Attr<Throwable> ERROR = new Scannable.Attr((T)null);
      public static final Scannable.Attr<Long> LARGE_BUFFERED = new Scannable.Attr((T)null);
      public static final Scannable.Attr<String> NAME = new Scannable.Attr((T)null);
      public static final Scannable.Attr<Scannable> PARENT = new Scannable.Attr<>(null, Scannable::from);
      public static final Scannable.Attr<Scannable> RUN_ON = new Scannable.Attr<>(null, Scannable::from);
      public static final Scannable.Attr<Integer> PREFETCH = new Scannable.Attr(0);
      public static final Scannable.Attr<Long> REQUESTED_FROM_DOWNSTREAM = new Scannable.Attr((T)0L);
      public static final Scannable.Attr<Boolean> TERMINATED = new Scannable.Attr((T)false);
      public static final Scannable.Attr<Stream<Tuple2<String, String>>> TAGS = new Scannable.Attr((T)null);
      public static final Scannable.Attr<Scannable.Attr.RunStyle> RUN_STYLE = new Scannable.Attr((T)Scannable.Attr.RunStyle.UNKNOWN);
      public static final Scannable.Attr<String> LIFTER = new Scannable.Attr((T)null);
      final T defaultValue;
      final Function<Object, ? extends T> safeConverter;
      static final Scannable UNAVAILABLE_SCAN = new Scannable() {
         @Override
         public Object scanUnsafe(Scannable.Attr key) {
            return null;
         }

         @Override
         public boolean isScanAvailable() {
            return false;
         }

         public String toString() {
            return "UNAVAILABLE_SCAN";
         }

         @Override
         public String stepName() {
            return "UNAVAILABLE_SCAN";
         }
      };
      static final Scannable NULL_SCAN = new Scannable() {
         @Override
         public Object scanUnsafe(Scannable.Attr key) {
            return null;
         }

         @Override
         public boolean isScanAvailable() {
            return false;
         }

         public String toString() {
            return "NULL_SCAN";
         }

         @Override
         public String stepName() {
            return "NULL_SCAN";
         }
      };

      @Nullable
      public T defaultValue() {
         return this.defaultValue;
      }

      boolean isConversionSafe() {
         return this.safeConverter != null;
      }

      @Nullable
      T tryConvert(@Nullable Object o) {
         if (o == null) {
            return null;
         } else {
            return (T)(this.safeConverter == null ? o : this.safeConverter.apply(o));
         }
      }

      protected Attr(@Nullable T defaultValue) {
         this(defaultValue, null);
      }

      protected Attr(@Nullable T defaultValue, @Nullable Function<Object, ? extends T> safeConverter) {
         this.defaultValue = defaultValue;
         this.safeConverter = safeConverter;
      }

      static Stream<? extends Scannable> recurse(Scannable _s, final Scannable.Attr<Scannable> key) {
         final Scannable s = Scannable.from(_s.scan(key));
         return !s.isScanAvailable() ? Stream.empty() : StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Scannable>() {
            Scannable c = s;

            public boolean hasNext() {
               return this.c != null && this.c.isScanAvailable();
            }

            public Scannable next() {
               Scannable _c = this.c;
               this.c = Scannable.from(this.c.scan(key));
               return _c;
            }
         }, 0), false);
      }

      public static enum RunStyle {
         UNKNOWN,
         ASYNC,
         SYNC;
      }
   }
}
