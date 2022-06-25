package io.netty.handler.ssl.util;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SuppressJava6Requirement;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

public final class LazyX509Certificate extends X509Certificate {
   static final CertificateFactory X509_CERT_FACTORY;
   private final byte[] bytes;
   private X509Certificate wrapped;

   public LazyX509Certificate(byte[] bytes) {
      this.bytes = ObjectUtil.checkNotNull(bytes, "bytes");
   }

   public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
      this.unwrap().checkValidity();
   }

   public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
      this.unwrap().checkValidity(date);
   }

   public X500Principal getIssuerX500Principal() {
      return this.unwrap().getIssuerX500Principal();
   }

   public X500Principal getSubjectX500Principal() {
      return this.unwrap().getSubjectX500Principal();
   }

   public List<String> getExtendedKeyUsage() throws CertificateParsingException {
      return this.unwrap().getExtendedKeyUsage();
   }

   public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
      return this.unwrap().getSubjectAlternativeNames();
   }

   public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
      return this.unwrap().getSubjectAlternativeNames();
   }

   @SuppressJava6Requirement(
      reason = "Can only be called from Java8 as class is package-private"
   )
   public void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
      this.unwrap().verify(key, sigProvider);
   }

   public int getVersion() {
      return this.unwrap().getVersion();
   }

   public BigInteger getSerialNumber() {
      return this.unwrap().getSerialNumber();
   }

   public Principal getIssuerDN() {
      return this.unwrap().getIssuerDN();
   }

   public Principal getSubjectDN() {
      return this.unwrap().getSubjectDN();
   }

   public Date getNotBefore() {
      return this.unwrap().getNotBefore();
   }

   public Date getNotAfter() {
      return this.unwrap().getNotAfter();
   }

   public byte[] getTBSCertificate() throws CertificateEncodingException {
      return this.unwrap().getTBSCertificate();
   }

   public byte[] getSignature() {
      return this.unwrap().getSignature();
   }

   public String getSigAlgName() {
      return this.unwrap().getSigAlgName();
   }

   public String getSigAlgOID() {
      return this.unwrap().getSigAlgOID();
   }

   public byte[] getSigAlgParams() {
      return this.unwrap().getSigAlgParams();
   }

   public boolean[] getIssuerUniqueID() {
      return this.unwrap().getIssuerUniqueID();
   }

   public boolean[] getSubjectUniqueID() {
      return this.unwrap().getSubjectUniqueID();
   }

   public boolean[] getKeyUsage() {
      return this.unwrap().getKeyUsage();
   }

   public int getBasicConstraints() {
      return this.unwrap().getBasicConstraints();
   }

   public byte[] getEncoded() {
      return (byte[])this.bytes.clone();
   }

   public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      this.unwrap().verify(key);
   }

   public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      this.unwrap().verify(key, sigProvider);
   }

   public String toString() {
      return this.unwrap().toString();
   }

   public PublicKey getPublicKey() {
      return this.unwrap().getPublicKey();
   }

   public boolean hasUnsupportedCriticalExtension() {
      return this.unwrap().hasUnsupportedCriticalExtension();
   }

   public Set<String> getCriticalExtensionOIDs() {
      return this.unwrap().getCriticalExtensionOIDs();
   }

   public Set<String> getNonCriticalExtensionOIDs() {
      return this.unwrap().getNonCriticalExtensionOIDs();
   }

   public byte[] getExtensionValue(String oid) {
      return this.unwrap().getExtensionValue(oid);
   }

   private X509Certificate unwrap() {
      X509Certificate wrapped = this.wrapped;
      if (wrapped == null) {
         try {
            wrapped = this.wrapped = (X509Certificate)X509_CERT_FACTORY.generateCertificate(new ByteArrayInputStream(this.bytes));
         } catch (CertificateException var3) {
            throw new IllegalStateException(var3);
         }
      }

      return wrapped;
   }

   static {
      try {
         X509_CERT_FACTORY = CertificateFactory.getInstance("X.509");
      } catch (CertificateException var1) {
         throw new ExceptionInInitializerError(var1);
      }
   }
}
