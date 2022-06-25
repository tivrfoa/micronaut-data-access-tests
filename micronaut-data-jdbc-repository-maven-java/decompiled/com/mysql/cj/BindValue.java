package com.mysql.cj;

import com.mysql.cj.protocol.Message;
import com.mysql.cj.result.Field;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

public interface BindValue {
   BindValue clone();

   void reset();

   boolean isNull();

   void setNull(boolean var1);

   boolean isStream();

   MysqlType getMysqlType();

   void setMysqlType(MysqlType var1);

   byte[] getByteValue();

   boolean isSet();

   void setBinding(Object var1, MysqlType var2, int var3, AtomicBoolean var4);

   Calendar getCalendar();

   void setCalendar(Calendar var1);

   boolean escapeBytesIfNeeded();

   void setEscapeBytesIfNeeded(boolean var1);

   Object getValue();

   boolean isNational();

   void setIsNational(boolean var1);

   int getFieldType();

   long getTextLength();

   long getBinaryLength();

   long getBoundBeforeExecutionNum();

   String getString();

   Field getField();

   void setField(Field var1);

   boolean keepOrigNanos();

   void setKeepOrigNanos(boolean var1);

   void setScaleOrLength(long var1);

   long getScaleOrLength();

   String getName();

   void setName(String var1);

   void writeAsText(Message var1);

   void writeAsBinary(Message var1);

   void writeAsQueryAttribute(Message var1);
}
