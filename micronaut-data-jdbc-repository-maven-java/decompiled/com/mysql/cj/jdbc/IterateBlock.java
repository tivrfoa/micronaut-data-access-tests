package com.mysql.cj.jdbc;

import java.sql.SQLException;
import java.util.Iterator;

public abstract class IterateBlock<T> {
   DatabaseMetaData.IteratorWithCleanup<T> iteratorWithCleanup;
   Iterator<T> javaIterator;
   boolean stopIterating = false;

   IterateBlock(DatabaseMetaData.IteratorWithCleanup<T> i) {
      this.iteratorWithCleanup = i;
      this.javaIterator = null;
   }

   IterateBlock(Iterator<T> i) {
      this.javaIterator = i;
      this.iteratorWithCleanup = null;
   }

   public void doForAll() throws SQLException {
      if (this.iteratorWithCleanup != null) {
         try {
            while(this.iteratorWithCleanup.hasNext()) {
               this.forEach(this.iteratorWithCleanup.next());
               if (this.stopIterating) {
                  break;
               }
            }
         } finally {
            this.iteratorWithCleanup.close();
         }
      } else {
         while(this.javaIterator.hasNext()) {
            this.forEach((T)this.javaIterator.next());
            if (this.stopIterating) {
               break;
            }
         }
      }

   }

   abstract void forEach(T var1) throws SQLException;

   public final boolean fullIteration() {
      return !this.stopIterating;
   }
}
