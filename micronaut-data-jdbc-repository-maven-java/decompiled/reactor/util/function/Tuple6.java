package reactor.util.function;

import java.util.Objects;
import java.util.function.Function;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

public class Tuple6<T1, T2, T3, T4, T5, T6> extends Tuple5<T1, T2, T3, T4, T5> {
   private static final long serialVersionUID = 770306356087176830L;
   @NonNull
   final T6 t6;

   Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
      super(t1, t2, t3, t4, t5);
      this.t6 = (T6)Objects.requireNonNull(t6, "t6");
   }

   public T6 getT6() {
      return this.t6;
   }

   public <R> Tuple6<R, T2, T3, T4, T5, T6> mapT1(Function<T1, R> mapper) {
      return new Tuple6<>((R)mapper.apply(this.t1), this.t2, this.t3, this.t4, this.t5, this.t6);
   }

   public <R> Tuple6<T1, R, T3, T4, T5, T6> mapT2(Function<T2, R> mapper) {
      return new Tuple6<>(this.t1, (R)mapper.apply(this.t2), this.t3, this.t4, this.t5, this.t6);
   }

   public <R> Tuple6<T1, T2, R, T4, T5, T6> mapT3(Function<T3, R> mapper) {
      return new Tuple6<>(this.t1, this.t2, (R)mapper.apply(this.t3), this.t4, this.t5, this.t6);
   }

   public <R> Tuple6<T1, T2, T3, R, T5, T6> mapT4(Function<T4, R> mapper) {
      return new Tuple6<>(this.t1, this.t2, this.t3, (R)mapper.apply(this.t4), this.t5, this.t6);
   }

   public <R> Tuple6<T1, T2, T3, T4, R, T6> mapT5(Function<T5, R> mapper) {
      return new Tuple6<>(this.t1, this.t2, this.t3, this.t4, (R)mapper.apply(this.t5), this.t6);
   }

   public <R> Tuple6<T1, T2, T3, T4, T5, R> mapT6(Function<T6, R> mapper) {
      return new Tuple6<>(this.t1, this.t2, this.t3, this.t4, this.t5, (R)mapper.apply(this.t6));
   }

   @Nullable
   @Override
   public Object get(int index) {
      switch(index) {
         case 0:
            return this.t1;
         case 1:
            return this.t2;
         case 2:
            return this.t3;
         case 3:
            return this.t4;
         case 4:
            return this.t5;
         case 5:
            return this.t6;
         default:
            return null;
      }
   }

   @Override
   public Object[] toArray() {
      return new Object[]{this.t1, this.t2, this.t3, this.t4, this.t5, this.t6};
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Tuple6)) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         Tuple6 tuple6 = (Tuple6)o;
         return this.t6.equals(tuple6.t6);
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + this.t6.hashCode();
   }

   @Override
   public int size() {
      return 6;
   }
}
