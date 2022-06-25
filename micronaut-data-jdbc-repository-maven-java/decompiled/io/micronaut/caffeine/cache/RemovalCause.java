package io.micronaut.caffeine.cache;

public enum RemovalCause {
   EXPLICIT {
      @Override
      public boolean wasEvicted() {
         return false;
      }
   },
   REPLACED {
      @Override
      public boolean wasEvicted() {
         return false;
      }
   },
   COLLECTED {
      @Override
      public boolean wasEvicted() {
         return true;
      }
   },
   EXPIRED {
      @Override
      public boolean wasEvicted() {
         return true;
      }
   },
   SIZE {
      @Override
      public boolean wasEvicted() {
         return true;
      }
   };

   private RemovalCause() {
   }

   public abstract boolean wasEvicted();
}
