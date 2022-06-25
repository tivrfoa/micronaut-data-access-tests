package io.micronaut.http.ssl;

public abstract class AbstractClientSslConfiguration extends SslConfiguration {
   private boolean insecureTrustAllCertificates;

   public boolean isInsecureTrustAllCertificates() {
      return this.insecureTrustAllCertificates;
   }

   public void setInsecureTrustAllCertificates(boolean insecureTrustAllCertificates) {
      this.insecureTrustAllCertificates = insecureTrustAllCertificates;
   }
}
