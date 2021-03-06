package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.base64.Base64Dialect;
import io.netty.util.NetUtil;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;

final class SslUtils {
   private static final InternalLogger logger = InternalLoggerFactory.getInstance(SslUtils.class);
   static final Set<String> TLSV13_CIPHERS = Collections.unmodifiableSet(
      new LinkedHashSet(
         Arrays.asList("TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256", "TLS_AES_128_GCM_SHA256", "TLS_AES_128_CCM_8_SHA256", "TLS_AES_128_CCM_SHA256")
      )
   );
   static final int GMSSL_PROTOCOL_VERSION = 257;
   static final String INVALID_CIPHER = "SSL_NULL_WITH_NULL_NULL";
   static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;
   static final int SSL_CONTENT_TYPE_ALERT = 21;
   static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;
   static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;
   static final int SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24;
   static final int SSL_RECORD_HEADER_LENGTH = 5;
   static final int NOT_ENOUGH_DATA = -1;
   static final int NOT_ENCRYPTED = -2;
   static final String[] DEFAULT_CIPHER_SUITES;
   static final String[] DEFAULT_TLSV13_CIPHER_SUITES;
   static final String[] TLSV13_CIPHER_SUITES = new String[]{"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384"};
   private static final boolean TLSV1_3_JDK_SUPPORTED = isTLSv13SupportedByJDK0(null);
   private static final boolean TLSV1_3_JDK_DEFAULT_ENABLED = isTLSv13EnabledByJDK0(null);

   static boolean isTLSv13SupportedByJDK(Provider provider) {
      return provider == null ? TLSV1_3_JDK_SUPPORTED : isTLSv13SupportedByJDK0(provider);
   }

   private static boolean isTLSv13SupportedByJDK0(Provider provider) {
      try {
         return arrayContains(newInitContext(provider).getSupportedSSLParameters().getProtocols(), "TLSv1.3");
      } catch (Throwable var2) {
         logger.debug("Unable to detect if JDK SSLEngine with provider {} supports TLSv1.3, assuming no", provider, var2);
         return false;
      }
   }

   static boolean isTLSv13EnabledByJDK(Provider provider) {
      return provider == null ? TLSV1_3_JDK_DEFAULT_ENABLED : isTLSv13EnabledByJDK0(provider);
   }

   private static boolean isTLSv13EnabledByJDK0(Provider provider) {
      try {
         return arrayContains(newInitContext(provider).getDefaultSSLParameters().getProtocols(), "TLSv1.3");
      } catch (Throwable var2) {
         logger.debug("Unable to detect if JDK SSLEngine with provider {} enables TLSv1.3 by default, assuming no", provider, var2);
         return false;
      }
   }

   private static SSLContext newInitContext(Provider provider) throws NoSuchAlgorithmException, KeyManagementException {
      SSLContext context;
      if (provider == null) {
         context = SSLContext.getInstance("TLS");
      } else {
         context = SSLContext.getInstance("TLS", provider);
      }

      context.init(null, new TrustManager[0], null);
      return context;
   }

   static SSLContext getSSLContext(String provider) throws NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
      SSLContext context;
      if (StringUtil.isNullOrEmpty(provider)) {
         context = SSLContext.getInstance(getTlsVersion());
      } else {
         context = SSLContext.getInstance(getTlsVersion(), provider);
      }

      context.init(null, new TrustManager[0], null);
      return context;
   }

   private static String getTlsVersion() {
      return TLSV1_3_JDK_SUPPORTED ? "TLSv1.3" : "TLSv1.2";
   }

   static boolean arrayContains(String[] array, String value) {
      for(String v : array) {
         if (value.equals(v)) {
            return true;
         }
      }

      return false;
   }

   static void addIfSupported(Set<String> supported, List<String> enabled, String... names) {
      for(String n : names) {
         if (supported.contains(n)) {
            enabled.add(n);
         }
      }

   }

   static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, Iterable<String> fallbackCiphers) {
      if (defaultCiphers.isEmpty()) {
         for(String cipher : fallbackCiphers) {
            if (!cipher.startsWith("SSL_") && !cipher.contains("_RC4_")) {
               defaultCiphers.add(cipher);
            }
         }
      }

   }

   static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, String... fallbackCiphers) {
      useFallbackCiphersIfDefaultIsEmpty(defaultCiphers, Arrays.asList(fallbackCiphers));
   }

   static SSLHandshakeException toSSLHandshakeException(Throwable e) {
      return e instanceof SSLHandshakeException ? (SSLHandshakeException)e : (SSLHandshakeException)new SSLHandshakeException(e.getMessage()).initCause(e);
   }

   static int getEncryptedPacketLength(ByteBuf buffer, int offset) {
      int packetLength = 0;
      boolean tls;
      switch(buffer.getUnsignedByte(offset)) {
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
            tls = true;
            break;
         default:
            tls = false;
      }

      if (tls) {
         int majorVersion = buffer.getUnsignedByte(offset + 1);
         if (majorVersion != 3 && buffer.getShort(offset + 1) != 257) {
            tls = false;
         } else {
            packetLength = unsignedShortBE(buffer, offset + 3) + 5;
            if (packetLength <= 5) {
               tls = false;
            }
         }
      }

      if (!tls) {
         int headerLength = (buffer.getUnsignedByte(offset) & 128) != 0 ? 2 : 3;
         int majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
         if (majorVersion != 2 && majorVersion != 3) {
            return -2;
         }

         packetLength = headerLength == 2 ? (shortBE(buffer, offset) & 32767) + 2 : (shortBE(buffer, offset) & 16383) + 3;
         if (packetLength <= headerLength) {
            return -1;
         }
      }

      return packetLength;
   }

   private static int unsignedShortBE(ByteBuf buffer, int offset) {
      int value = buffer.getUnsignedShort(offset);
      if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
         value = Integer.reverseBytes(value) >>> 16;
      }

      return value;
   }

   private static short shortBE(ByteBuf buffer, int offset) {
      short value = buffer.getShort(offset);
      if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
         value = Short.reverseBytes(value);
      }

      return value;
   }

   private static short unsignedByte(byte b) {
      return (short)(b & 255);
   }

   private static int unsignedShortBE(ByteBuffer buffer, int offset) {
      return shortBE(buffer, offset) & 65535;
   }

   private static short shortBE(ByteBuffer buffer, int offset) {
      return buffer.order() == ByteOrder.BIG_ENDIAN ? buffer.getShort(offset) : ByteBufUtil.swapShort(buffer.getShort(offset));
   }

   static int getEncryptedPacketLength(ByteBuffer[] buffers, int offset) {
      ByteBuffer buffer = buffers[offset];
      if (buffer.remaining() >= 5) {
         return getEncryptedPacketLength(buffer);
      } else {
         ByteBuffer tmp = ByteBuffer.allocate(5);

         do {
            buffer = buffers[offset++].duplicate();
            if (buffer.remaining() > tmp.remaining()) {
               buffer.limit(buffer.position() + tmp.remaining());
            }

            tmp.put(buffer);
         } while(tmp.hasRemaining());

         tmp.flip();
         return getEncryptedPacketLength(tmp);
      }
   }

   private static int getEncryptedPacketLength(ByteBuffer buffer) {
      int packetLength = 0;
      int pos = buffer.position();
      boolean tls;
      switch(unsignedByte(buffer.get(pos))) {
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
            tls = true;
            break;
         default:
            tls = false;
      }

      if (tls) {
         int majorVersion = unsignedByte(buffer.get(pos + 1));
         if (majorVersion != 3 && buffer.getShort(pos + 1) != 257) {
            tls = false;
         } else {
            packetLength = unsignedShortBE(buffer, pos + 3) + 5;
            if (packetLength <= 5) {
               tls = false;
            }
         }
      }

      if (!tls) {
         int headerLength = (unsignedByte(buffer.get(pos)) & 128) != 0 ? 2 : 3;
         int majorVersion = unsignedByte(buffer.get(pos + headerLength + 1));
         if (majorVersion != 2 && majorVersion != 3) {
            return -2;
         }

         packetLength = headerLength == 2 ? (shortBE(buffer, pos) & 32767) + 2 : (shortBE(buffer, pos) & 16383) + 3;
         if (packetLength <= headerLength) {
            return -1;
         }
      }

      return packetLength;
   }

   static void handleHandshakeFailure(ChannelHandlerContext ctx, Throwable cause, boolean notify) {
      ctx.flush();
      if (notify) {
         ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause));
      }

      ctx.close();
   }

   static void zeroout(ByteBuf buffer) {
      if (!buffer.isReadOnly()) {
         buffer.setZero(0, buffer.capacity());
      }

   }

   static void zerooutAndRelease(ByteBuf buffer) {
      zeroout(buffer);
      buffer.release();
   }

   static ByteBuf toBase64(ByteBufAllocator allocator, ByteBuf src) {
      ByteBuf dst = Base64.encode(src, src.readerIndex(), src.readableBytes(), true, Base64Dialect.STANDARD, allocator);
      src.readerIndex(src.writerIndex());
      return dst;
   }

   static boolean isValidHostNameForSNI(String hostname) {
      return hostname != null
         && hostname.indexOf(46) > 0
         && !hostname.endsWith(".")
         && !hostname.startsWith("/")
         && !NetUtil.isValidIpV4Address(hostname)
         && !NetUtil.isValidIpV6Address(hostname);
   }

   static boolean isTLSv13Cipher(String cipher) {
      return TLSV13_CIPHERS.contains(cipher);
   }

   private SslUtils() {
   }

   static {
      if (TLSV1_3_JDK_SUPPORTED) {
         DEFAULT_TLSV13_CIPHER_SUITES = TLSV13_CIPHER_SUITES;
      } else {
         DEFAULT_TLSV13_CIPHER_SUITES = EmptyArrays.EMPTY_STRINGS;
      }

      Set<String> defaultCiphers = new LinkedHashSet();
      defaultCiphers.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
      defaultCiphers.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
      defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
      defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
      defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
      defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");
      defaultCiphers.add("TLS_RSA_WITH_AES_128_GCM_SHA256");
      defaultCiphers.add("TLS_RSA_WITH_AES_128_CBC_SHA");
      defaultCiphers.add("TLS_RSA_WITH_AES_256_CBC_SHA");
      Collections.addAll(defaultCiphers, DEFAULT_TLSV13_CIPHER_SUITES);
      DEFAULT_CIPHER_SUITES = (String[])defaultCiphers.toArray(EmptyArrays.EMPTY_STRINGS);
   }
}
