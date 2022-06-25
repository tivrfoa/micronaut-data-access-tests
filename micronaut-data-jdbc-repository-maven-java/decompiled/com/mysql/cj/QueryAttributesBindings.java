package com.mysql.cj;

import java.util.function.Consumer;

public interface QueryAttributesBindings {
   void setAttribute(String var1, Object var2);

   int getCount();

   BindValue getAttributeValue(int var1);

   void runThroughAll(Consumer<BindValue> var1);

   void clearAttributes();
}
