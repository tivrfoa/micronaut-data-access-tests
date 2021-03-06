package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.ObjectUtil;
import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

public final class PemX509Certificate extends X509Certificate implements PemEncoded {
   private static final byte[] BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n".getBytes(CharsetUtil.US_ASCII);
   private static final byte[] END_CERT = "\n-----END CERTIFICATE-----\n".getBytes(CharsetUtil.US_ASCII);
   private final ByteBuf content;

   static PemEncoded toPEM(ByteBufAllocator allocator, boolean useDirect, X509Certificate... chain) throws CertificateEncodingException {
      ObjectUtil.checkNonEmpty((Object[])chain, "chain");
      if (chain.length == 1) {
         X509Certificate first = chain[0];
         if (first instanceof PemEncoded) {
            return ((PemEncoded)first).retain();
         }
      }

      boolean success = false;
      ByteBuf pem = null;

      PemValue var14;
      try {
         for(X509Certificate cert : chain) {
            if (cert == null) {
               throw new IllegalArgumentException("Null element in chain: " + Arrays.toString(chain));
            }

            if (cert instanceof PemEncoded) {
               pem = append(allocator, useDirect, (PemEncoded)cert, chain.length, pem);
            } else {
               pem = append(allocator, useDirect, cert, chain.length, pem);
            }
         }

         PemValue value = new PemValue(pem, false);
         success = true;
         var14 = value;
      } finally {
         if (!success && pem != null) {
            pem.release();
         }

      }

      return var14;
   }

   private static ByteBuf append(ByteBufAllocator allocator, boolean useDirect, PemEncoded encoded, int count, ByteBuf pem) {
      ByteBuf content = encoded.content();
      if (pem == null) {
         pem = newBuffer(allocator, useDirect, content.readableBytes() * count);
      }

      pem.writeBytes(content.slice());
      return pem;
   }

   private static ByteBuf append(ByteBufAllocator allocator, boolean useDirect, X509Certificate cert, int count, ByteBuf pem) throws CertificateEncodingException {
      ByteBuf encoded = Unpooled.wrappedBuffer(cert.getEncoded());

      try {
         ByteBuf base64 = SslUtils.toBase64(allocator, encoded);

         try {
            if (pem == null) {
               pem = newBuffer(allocator, useDirect, (BEGIN_CERT.length + base64.readableBytes() + END_CERT.length) * count);
            }

            pem.writeBytes(BEGIN_CERT);
            pem.writeBytes(base64);
            pem.writeBytes(END_CERT);
         } finally {
            base64.release();
         }
      } finally {
         encoded.release();
      }

      return pem;
   }

   private static ByteBuf newBuffer(ByteBufAllocator allocator, boolean useDirect, int initialCapacity) {
      return useDirect ? allocator.directBuffer(initialCapacity) : allocator.buffer(initialCapacity);
   }

   public static PemX509Certificate valueOf(byte[] key) {
      return valueOf(Unpooled.wrappedBuffer(key));
   }

   public static PemX509Certificate valueOf(ByteBuf key) {
      return new PemX509Certificate(key);
   }

   private PemX509Certificate(ByteBuf content) {
      this.content = ObjectUtil.checkNotNull(content, "content");
   }

   @Override
   public boolean isSensitive() {
      return false;
   }

   @Override
   public int refCnt() {
      return this.content.refCnt();
   }

   @Override
   public ByteBuf content() {
      int count = this.refCnt();
      if (count <= 0) {
         throw new IllegalReferenceCountException(count);
      } else {
         return this.content;
      }
   }

   public PemX509Certificate copy() {
      return this.replace(this.content.copy());
   }

   public PemX509Certificate duplicate() {
      return this.replace(this.content.duplicate());
   }

   public PemX509Certificate retainedDuplicate() {
      return this.replace(this.content.retainedDuplicate());
   }

   public PemX509Certificate replace(ByteBuf content) {
      return new PemX509Certificate(content);
   }

   public PemX509Certificate retain() {
      this.content.retain();
      return this;
   }

   public PemX509Certificate retain(int increment) {
      this.content.retain(increment);
      return this;
   }

   public PemX509Certificate touch() {
      this.content.touch();
      return this;
   }

   public PemX509Certificate touch(Object hint) {
      this.content.touch(hint);
      return this;
   }

   @Override
   public boolean release() {
      return this.content.release();
   }

   @Override
   public boolean release(int decrement) {
      return this.content.release(decrement);
   }

   public byte[] getEncoded() {
      throw new UnsupportedOperationException();
   }

   public boolean hasUnsupportedCriticalExtension() {
      throw new UnsupportedOperationException();
   }

   public Set<String> getCriticalExtensionOIDs() {
      throw new UnsupportedOperationException();
   }

   public Set<String> getNonCriticalExtensionOIDs() {
      throw new UnsupportedOperationException();
   }

   public byte[] getExtensionValue(String oid) {
      throw new UnsupportedOperationException();
   }

   public void checkValidity() {
      throw new UnsupportedOperationException();
   }

   public void checkValidity(Date date) {
      throw new UnsupportedOperationException();
   }

   public int getVersion() {
      throw new UnsupportedOperationException();
   }

   public BigInteger getSerialNumber() {
      throw new UnsupportedOperationException();
   }

   public Principal getIssuerDN() {
      throw new UnsupportedOperationException();
   }

   public Principal getSubjectDN() {
      throw new UnsupportedOperationException();
   }

   public Date getNotBefore() {
      throw new UnsupportedOperationException();
   }

   public Date getNotAfter() {
      throw new UnsupportedOperationException();
   }

   public byte[] getTBSCertificate() {
      throw new UnsupportedOperationException();
   }

   public byte[] getSignature() {
      throw new UnsupportedOperationException();
   }

   public String getSigAlgName() {
      throw new UnsupportedOperationException();
   }

   public String getSigAlgOID() {
      throw new UnsupportedOperationException();
   }

   public byte[] getSigAlgParams() {
      throw new UnsupportedOperationException();
   }

   public boolean[] getIssuerUniqueID() {
      throw new UnsupportedOperationException();
   }

   public boolean[] getSubjectUniqueID() {
      throw new UnsupportedOperationException();
   }

   public boolean[] getKeyUsage() {
      throw new UnsupportedOperationException();
   }

   public int getBasicConstraints() {
      throw new UnsupportedOperationException();
   }

   public void verify(PublicKey key) {
      throw new UnsupportedOperationException();
   }

   public void verify(PublicKey key, String sigProvider) {
      throw new UnsupportedOperationException();
   }

   public PublicKey getPublicKey() {
      throw new UnsupportedOperationException();
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof PemX509Certificate)) {
         return false;
      } else {
         PemX509Certificate other = (PemX509Certificate)o;
         return this.content.equals(other.content);
      }
   }

   public int hashCode() {
      return this.content.hashCode();
   }

   public String toString() {
      return this.content.toString(CharsetUtil.UTF_8);
   }
}
