package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.Util;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class ReaderValueEncoder extends AbstractValueEncoder {
   @Override
   public byte[] getBytes(BindValue binding) {
      return this.readBytes((Reader)binding.getValue(), binding);
   }

   @Override
   public String getString(BindValue binding) {
      return "'** STREAM DATA **'";
   }

   @Override
   public void encodeAsBinary(Message msg, BindValue binding) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   protected byte[] readBytes(Reader reader, BindValue binding) {
      try {
         char[] c = null;
         int len = 0;
         boolean useLength = this.propertySet.getBooleanProperty(PropertyKey.useStreamLengthsInPrepStmts).getValue();
         String forcedEncoding = binding.isNational() ? null : this.propertySet.getStringProperty(PropertyKey.clobCharacterEncoding).getStringValue();
         long scaleOrLength = binding.getScaleOrLength();
         byte[] bytes;
         if (useLength && scaleOrLength != -1L) {
            c = new char[(int)scaleOrLength];
            int numCharsRead = Util.readFully(reader, c, (int)scaleOrLength);
            bytes = StringUtils.getBytes(new String(c, 0, numCharsRead), forcedEncoding);
         } else {
            c = new char[4096];
            StringBuilder buf = new StringBuilder();

            while((len = reader.read(c)) != -1) {
               buf.append(c, 0, len);
            }

            bytes = StringUtils.getBytes(buf.toString(), forcedEncoding);
         }

         return this.escapeBytesIfNeeded(bytes);
      } catch (UnsupportedEncodingException var11) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var11.toString(), var11, this.exceptionInterceptor);
      } catch (IOException var12) {
         throw ExceptionFactory.createException(var12.toString(), var12, this.exceptionInterceptor);
      }
   }
}
