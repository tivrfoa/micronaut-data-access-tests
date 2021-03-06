package io.netty.handler.ssl.util;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SuppressJava6Requirement;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

@SuppressJava6Requirement(
   reason = "Usage guarded by java version check"
)
final class X509KeyManagerWrapper extends X509ExtendedKeyManager {
   private final X509KeyManager delegate;

   X509KeyManagerWrapper(X509KeyManager delegate) {
      this.delegate = ObjectUtil.checkNotNull(delegate, "delegate");
   }

   public String[] getClientAliases(String var1, Principal[] var2) {
      return this.delegate.getClientAliases(var1, var2);
   }

   public String chooseClientAlias(String[] var1, Principal[] var2, Socket var3) {
      return this.delegate.chooseClientAlias(var1, var2, var3);
   }

   public String[] getServerAliases(String var1, Principal[] var2) {
      return this.delegate.getServerAliases(var1, var2);
   }

   public String chooseServerAlias(String var1, Principal[] var2, Socket var3) {
      return this.delegate.chooseServerAlias(var1, var2, var3);
   }

   public X509Certificate[] getCertificateChain(String var1) {
      return this.delegate.getCertificateChain(var1);
   }

   public PrivateKey getPrivateKey(String var1) {
      return this.delegate.getPrivateKey(var1);
   }

   public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
      return this.delegate.chooseClientAlias(keyType, issuers, null);
   }

   public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
      return this.delegate.chooseServerAlias(keyType, issuers, null);
   }
}
