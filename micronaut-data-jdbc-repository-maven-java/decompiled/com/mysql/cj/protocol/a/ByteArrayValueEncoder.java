package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.util.StringUtils;

public class ByteArrayValueEncoder extends AbstractValueEncoder {
   @Override
   public void init(PropertySet pset, ServerSession serverSess, ExceptionInterceptor excInterceptor) {
      super.init(pset, serverSess, excInterceptor);
   }

   @Override
   public byte[] getBytes(BindValue binding) {
      if (binding.isNull()) {
         return StringUtils.getBytes("null");
      } else {
         return binding.escapeBytesIfNeeded() ? this.escapeBytesIfNeeded((byte[])binding.getValue()) : (byte[])binding.getValue();
      }
   }

   @Override
   public String getString(BindValue binding) {
      return "** BYTE ARRAY DATA **";
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
      ((NativePacketPayload)msg).writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, (byte[])binding.getValue());
   }
}
