package com.mysql.cj.protocol;

import com.mysql.cj.BindValue;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;

public interface ValueEncoder {
   void init(PropertySet var1, ServerSession var2, ExceptionInterceptor var3);

   byte[] getBytes(BindValue var1);

   String getString(BindValue var1);

   long getTextLength(BindValue var1);

   long getBinaryLength(BindValue var1);

   void encodeAsText(Message var1, BindValue var2);

   void encodeAsBinary(Message var1, BindValue var2);

   void encodeAsQueryAttribute(Message var1, BindValue var2);
}
