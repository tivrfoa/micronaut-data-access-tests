package com.mysql.cj.xdevapi;

public interface RemoveStatement extends Statement<RemoveStatement, Result> {
   @Deprecated
   RemoveStatement orderBy(String... var1);

   RemoveStatement sort(String... var1);

   RemoveStatement limit(long var1);
}
