package io.netty.handler.codec;

import io.netty.util.AsciiString;
import io.netty.util.internal.PlatformDependent;
import java.text.ParseException;
import java.util.Date;

public class CharSequenceValueConverter implements ValueConverter<CharSequence> {
   public static final CharSequenceValueConverter INSTANCE = new CharSequenceValueConverter();
   private static final AsciiString TRUE_ASCII = new AsciiString((CharSequence)"true");

   public CharSequence convertObject(Object value) {
      return (CharSequence)(value instanceof CharSequence ? (CharSequence)value : value.toString());
   }

   public CharSequence convertInt(int value) {
      return String.valueOf(value);
   }

   public CharSequence convertLong(long value) {
      return String.valueOf(value);
   }

   public CharSequence convertDouble(double value) {
      return String.valueOf(value);
   }

   public CharSequence convertChar(char value) {
      return String.valueOf(value);
   }

   public CharSequence convertBoolean(boolean value) {
      return String.valueOf(value);
   }

   public CharSequence convertFloat(float value) {
      return String.valueOf(value);
   }

   public boolean convertToBoolean(CharSequence value) {
      return AsciiString.contentEqualsIgnoreCase(value, TRUE_ASCII);
   }

   public CharSequence convertByte(byte value) {
      return String.valueOf(value);
   }

   public byte convertToByte(CharSequence value) {
      return value instanceof AsciiString && value.length() == 1 ? ((AsciiString)value).byteAt(0) : Byte.parseByte(value.toString());
   }

   public char convertToChar(CharSequence value) {
      return value.charAt(0);
   }

   public CharSequence convertShort(short value) {
      return String.valueOf(value);
   }

   public short convertToShort(CharSequence value) {
      return value instanceof AsciiString ? ((AsciiString)value).parseShort() : Short.parseShort(value.toString());
   }

   public int convertToInt(CharSequence value) {
      return value instanceof AsciiString ? ((AsciiString)value).parseInt() : Integer.parseInt(value.toString());
   }

   public long convertToLong(CharSequence value) {
      return value instanceof AsciiString ? ((AsciiString)value).parseLong() : Long.parseLong(value.toString());
   }

   public CharSequence convertTimeMillis(long value) {
      return DateFormatter.format(new Date(value));
   }

   public long convertToTimeMillis(CharSequence value) {
      Date date = DateFormatter.parseHttpDate(value);
      if (date == null) {
         PlatformDependent.throwException(new ParseException("header can't be parsed into a Date: " + value, 0));
         return 0L;
      } else {
         return date.getTime();
      }
   }

   public float convertToFloat(CharSequence value) {
      return value instanceof AsciiString ? ((AsciiString)value).parseFloat() : Float.parseFloat(value.toString());
   }

   public double convertToDouble(CharSequence value) {
      return value instanceof AsciiString ? ((AsciiString)value).parseDouble() : Double.parseDouble(value.toString());
   }
}
