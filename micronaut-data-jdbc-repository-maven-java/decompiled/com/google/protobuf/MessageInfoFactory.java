package com.google.protobuf;

@CheckReturnValue
interface MessageInfoFactory {
   boolean isSupported(Class<?> var1);

   MessageInfo messageInfoFor(Class<?> var1);
}
