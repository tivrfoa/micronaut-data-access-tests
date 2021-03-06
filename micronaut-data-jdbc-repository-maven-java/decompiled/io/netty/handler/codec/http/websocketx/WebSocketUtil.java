package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

final class WebSocketUtil {
   private static final FastThreadLocal<MessageDigest> MD5 = new FastThreadLocal<MessageDigest>() {
      protected MessageDigest initialValue() throws Exception {
         try {
            return MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException var2) {
            throw new InternalError("MD5 not supported on this platform - Outdated?");
         }
      }
   };
   private static final FastThreadLocal<MessageDigest> SHA1 = new FastThreadLocal<MessageDigest>() {
      protected MessageDigest initialValue() throws Exception {
         try {
            return MessageDigest.getInstance("SHA1");
         } catch (NoSuchAlgorithmException var2) {
            throw new InternalError("SHA-1 not supported on this platform - Outdated?");
         }
      }
   };

   static byte[] md5(byte[] data) {
      return digest(MD5, data);
   }

   static byte[] sha1(byte[] data) {
      return digest(SHA1, data);
   }

   private static byte[] digest(FastThreadLocal<MessageDigest> digestFastThreadLocal, byte[] data) {
      MessageDigest digest = (MessageDigest)digestFastThreadLocal.get();
      digest.reset();
      return digest.digest(data);
   }

   @SuppressJava6Requirement(
      reason = "Guarded with java version check"
   )
   static String base64(byte[] data) {
      if (PlatformDependent.javaVersion() >= 8) {
         return Base64.getEncoder().encodeToString(data);
      } else {
         ByteBuf encodedData = Unpooled.wrappedBuffer(data);

         String encodedString;
         try {
            ByteBuf encoded = io.netty.handler.codec.base64.Base64.encode(encodedData);

            try {
               encodedString = encoded.toString(CharsetUtil.UTF_8);
            } finally {
               encoded.release();
            }
         } finally {
            encodedData.release();
         }

         return encodedString;
      }
   }

   static byte[] randomBytes(int size) {
      byte[] bytes = new byte[size];
      PlatformDependent.threadLocalRandom().nextBytes(bytes);
      return bytes;
   }

   static int randomNumber(int minimum, int maximum) {
      assert minimum < maximum;

      double fraction = PlatformDependent.threadLocalRandom().nextDouble();
      return (int)((double)minimum + fraction * (double)(maximum - minimum));
   }

   private WebSocketUtil() {
   }
}
