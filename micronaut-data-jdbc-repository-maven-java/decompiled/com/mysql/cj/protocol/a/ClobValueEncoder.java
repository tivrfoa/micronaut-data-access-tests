package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.exceptions.ExceptionFactory;
import java.sql.Clob;

public class ClobValueEncoder extends ReaderValueEncoder {
   @Override
   public byte[] getBytes(BindValue binding) {
      try {
         return this.readBytes(((Clob)binding.getValue()).getCharacterStream(), binding);
      } catch (Throwable var3) {
         throw ExceptionFactory.createException(var3.getMessage(), var3, this.exceptionInterceptor);
      }
   }
}
