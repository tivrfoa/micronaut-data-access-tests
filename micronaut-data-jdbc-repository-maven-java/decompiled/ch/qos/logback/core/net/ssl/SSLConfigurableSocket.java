package ch.qos.logback.core.net.ssl;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

public class SSLConfigurableSocket implements SSLConfigurable {
   private final SSLSocket delegate;

   public SSLConfigurableSocket(SSLSocket delegate) {
      this.delegate = delegate;
   }

   @Override
   public String[] getDefaultProtocols() {
      return this.delegate.getEnabledProtocols();
   }

   @Override
   public String[] getSupportedProtocols() {
      return this.delegate.getSupportedProtocols();
   }

   @Override
   public void setEnabledProtocols(String[] protocols) {
      this.delegate.setEnabledProtocols(protocols);
   }

   @Override
   public String[] getDefaultCipherSuites() {
      return this.delegate.getEnabledCipherSuites();
   }

   @Override
   public String[] getSupportedCipherSuites() {
      return this.delegate.getSupportedCipherSuites();
   }

   @Override
   public void setEnabledCipherSuites(String[] suites) {
      this.delegate.setEnabledCipherSuites(suites);
   }

   @Override
   public void setNeedClientAuth(boolean state) {
      this.delegate.setNeedClientAuth(state);
   }

   @Override
   public void setWantClientAuth(boolean state) {
      this.delegate.setWantClientAuth(state);
   }

   @Override
   public void setHostnameVerification(boolean hostnameVerification) {
      if (hostnameVerification) {
         SSLParameters sslParameters = this.delegate.getSSLParameters();
         sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
         this.delegate.setSSLParameters(sslParameters);
      }
   }
}
