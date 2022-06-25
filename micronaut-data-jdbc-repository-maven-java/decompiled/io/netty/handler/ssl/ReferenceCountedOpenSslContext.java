package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.util.LazyX509Certificate;
import io.netty.internal.tcnative.AsyncSSLPrivateKeyMethod;
import io.netty.internal.tcnative.CertificateCompressionAlgo;
import io.netty.internal.tcnative.CertificateVerifier;
import io.netty.internal.tcnative.ResultCallback;
import io.netty.internal.tcnative.SSL;
import io.netty.internal.tcnative.SSLContext;
import io.netty.internal.tcnative.SSLPrivateKeyMethod;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.X509Certificate;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertPathValidatorException.Reason;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public abstract class ReferenceCountedOpenSslContext extends SslContext implements ReferenceCounted {
   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslContext.class);
   private static final int DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE = Math.max(
      1, SystemPropertyUtil.getInt("io.netty.handler.ssl.openssl.bioNonApplicationBufferSize", 2048)
   );
   static final boolean USE_TASKS = SystemPropertyUtil.getBoolean("io.netty.handler.ssl.openssl.useTasks", true);
   private static final Integer DH_KEY_LENGTH;
   private static final ResourceLeakDetector<ReferenceCountedOpenSslContext> leakDetector = ResourceLeakDetectorFactory.instance()
      .newResourceLeakDetector(ReferenceCountedOpenSslContext.class);
   protected static final int VERIFY_DEPTH = 10;
   static final boolean CLIENT_ENABLE_SESSION_TICKET = SystemPropertyUtil.getBoolean("jdk.tls.client.enableSessionTicketExtension", false);
   static final boolean CLIENT_ENABLE_SESSION_TICKET_TLSV13 = SystemPropertyUtil.getBoolean("jdk.tls.client.enableSessionTicketExtension", true);
   static final boolean SERVER_ENABLE_SESSION_TICKET = SystemPropertyUtil.getBoolean("jdk.tls.server.enableSessionTicketExtension", false);
   static final boolean SERVER_ENABLE_SESSION_TICKET_TLSV13 = SystemPropertyUtil.getBoolean("jdk.tls.server.enableSessionTicketExtension", true);
   static final boolean SERVER_ENABLE_SESSION_CACHE = SystemPropertyUtil.getBoolean("io.netty.handler.ssl.openssl.sessionCacheServer", true);
   static final boolean CLIENT_ENABLE_SESSION_CACHE = SystemPropertyUtil.getBoolean("io.netty.handler.ssl.openssl.sessionCacheClient", false);
   protected long ctx;
   private final List<String> unmodifiableCiphers;
   private final OpenSslApplicationProtocolNegotiator apn;
   private final int mode;
   private final ResourceLeakTracker<ReferenceCountedOpenSslContext> leak;
   private final AbstractReferenceCounted refCnt = new AbstractReferenceCounted() {
      @Override
      public ReferenceCounted touch(Object hint) {
         if (ReferenceCountedOpenSslContext.this.leak != null) {
            ReferenceCountedOpenSslContext.this.leak.record(hint);
         }

         return ReferenceCountedOpenSslContext.this;
      }

      @Override
      protected void deallocate() {
         ReferenceCountedOpenSslContext.this.destroy();
         if (ReferenceCountedOpenSslContext.this.leak != null) {
            boolean closed = ReferenceCountedOpenSslContext.this.leak.close(ReferenceCountedOpenSslContext.this);

            assert closed;
         }

      }
   };
   final Certificate[] keyCertChain;
   final ClientAuth clientAuth;
   final String[] protocols;
   final boolean enableOcsp;
   final OpenSslEngineMap engineMap = new ReferenceCountedOpenSslContext.DefaultOpenSslEngineMap();
   final ReadWriteLock ctxLock = new ReentrantReadWriteLock();
   private volatile int bioNonApplicationBufferSize = DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE;
   static final OpenSslApplicationProtocolNegotiator NONE_PROTOCOL_NEGOTIATOR = new OpenSslApplicationProtocolNegotiator() {
      @Override
      public ApplicationProtocolConfig.Protocol protocol() {
         return ApplicationProtocolConfig.Protocol.NONE;
      }

      @Override
      public List<String> protocols() {
         return Collections.emptyList();
      }

      @Override
      public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior() {
         return ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
      }

      @Override
      public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
         return ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
      }
   };
   final boolean tlsFalseStart;

   ReferenceCountedOpenSslContext(
      Iterable<String> ciphers,
      CipherSuiteFilter cipherFilter,
      OpenSslApplicationProtocolNegotiator apn,
      int mode,
      Certificate[] keyCertChain,
      ClientAuth clientAuth,
      String[] protocols,
      boolean startTls,
      boolean enableOcsp,
      boolean leakDetection,
      Entry<SslContextOption<?>, Object>... ctxOptions
   ) throws SSLException {
      super(startTls);
      OpenSsl.ensureAvailability();
      if (enableOcsp && !OpenSsl.isOcspSupported()) {
         throw new IllegalStateException("OCSP is not supported.");
      } else if (mode != 1 && mode != 0) {
         throw new IllegalArgumentException("mode most be either SSL.SSL_MODE_SERVER or SSL.SSL_MODE_CLIENT");
      } else {
         boolean tlsFalseStart = false;
         boolean useTasks = USE_TASKS;
         OpenSslPrivateKeyMethod privateKeyMethod = null;
         OpenSslAsyncPrivateKeyMethod asyncPrivateKeyMethod = null;
         OpenSslCertificateCompressionConfig certCompressionConfig = null;
         if (ctxOptions != null) {
            for(Entry<SslContextOption<?>, Object> ctxOpt : ctxOptions) {
               SslContextOption<?> option = (SslContextOption)ctxOpt.getKey();
               if (option == OpenSslContextOption.TLS_FALSE_START) {
                  tlsFalseStart = ctxOpt.getValue();
               } else if (option == OpenSslContextOption.USE_TASKS) {
                  useTasks = ctxOpt.getValue();
               } else if (option == OpenSslContextOption.PRIVATE_KEY_METHOD) {
                  privateKeyMethod = (OpenSslPrivateKeyMethod)ctxOpt.getValue();
               } else if (option == OpenSslContextOption.ASYNC_PRIVATE_KEY_METHOD) {
                  asyncPrivateKeyMethod = (OpenSslAsyncPrivateKeyMethod)ctxOpt.getValue();
               } else if (option == OpenSslContextOption.CERTIFICATE_COMPRESSION_ALGORITHMS) {
                  certCompressionConfig = (OpenSslCertificateCompressionConfig)ctxOpt.getValue();
               } else {
                  logger.debug("Skipping unsupported " + SslContextOption.class.getSimpleName() + ": " + ctxOpt.getKey());
               }
            }
         }

         if (privateKeyMethod != null && asyncPrivateKeyMethod != null) {
            throw new IllegalArgumentException(
               "You can either only use " + OpenSslAsyncPrivateKeyMethod.class.getSimpleName() + " or " + OpenSslPrivateKeyMethod.class.getSimpleName()
            );
         } else {
            this.tlsFalseStart = tlsFalseStart;
            this.leak = leakDetection ? leakDetector.track(this) : null;
            this.mode = mode;
            this.clientAuth = this.isServer() ? ObjectUtil.checkNotNull(clientAuth, "clientAuth") : ClientAuth.NONE;
            this.protocols = protocols;
            this.enableOcsp = enableOcsp;
            this.keyCertChain = keyCertChain == null ? null : (Certificate[])keyCertChain.clone();
            String[] suites = ObjectUtil.checkNotNull(cipherFilter, "cipherFilter")
               .filterCipherSuites(ciphers, OpenSsl.DEFAULT_CIPHERS, OpenSsl.availableJavaCipherSuites());
            LinkedHashSet<String> suitesSet = new LinkedHashSet(suites.length);
            Collections.addAll(suitesSet, suites);
            this.unmodifiableCiphers = new ArrayList(suitesSet);
            this.apn = ObjectUtil.checkNotNull(apn, "apn");
            boolean success = false;

            try {
               boolean tlsv13Supported = OpenSsl.isTlsv13Supported();

               try {
                  int protocolOpts = 30;
                  if (tlsv13Supported) {
                     protocolOpts |= 32;
                  }

                  this.ctx = SSLContext.make(protocolOpts, mode);
               } catch (Exception var35) {
                  throw new SSLException("failed to create an SSL_CTX", var35);
               }

               StringBuilder cipherBuilder = new StringBuilder();
               StringBuilder cipherTLSv13Builder = new StringBuilder();

               try {
                  if (this.unmodifiableCiphers.isEmpty()) {
                     SSLContext.setCipherSuite(this.ctx, "", false);
                     if (tlsv13Supported) {
                        SSLContext.setCipherSuite(this.ctx, "", true);
                     }
                  } else {
                     CipherSuiteConverter.convertToCipherStrings(this.unmodifiableCiphers, cipherBuilder, cipherTLSv13Builder, OpenSsl.isBoringSSL());
                     SSLContext.setCipherSuite(this.ctx, cipherBuilder.toString(), false);
                     if (tlsv13Supported) {
                        SSLContext.setCipherSuite(this.ctx, OpenSsl.checkTls13Ciphers(logger, cipherTLSv13Builder.toString()), true);
                     }
                  }
               } catch (SSLException var33) {
                  throw var33;
               } catch (Exception var34) {
                  throw new SSLException("failed to set cipher suite: " + this.unmodifiableCiphers, var34);
               }

               int options = SSLContext.getOptions(this.ctx)
                  | SSL.SSL_OP_NO_SSLv2
                  | SSL.SSL_OP_NO_SSLv3
                  | SSL.SSL_OP_NO_TLSv1
                  | SSL.SSL_OP_NO_TLSv1_1
                  | SSL.SSL_OP_CIPHER_SERVER_PREFERENCE
                  | SSL.SSL_OP_NO_COMPRESSION
                  | SSL.SSL_OP_NO_TICKET;
               if (cipherBuilder.length() == 0) {
                  options |= SSL.SSL_OP_NO_SSLv2 | SSL.SSL_OP_NO_SSLv3 | SSL.SSL_OP_NO_TLSv1 | SSL.SSL_OP_NO_TLSv1_1 | SSL.SSL_OP_NO_TLSv1_2;
               }

               SSLContext.setOptions(this.ctx, options);
               SSLContext.setMode(this.ctx, SSLContext.getMode(this.ctx) | SSL.SSL_MODE_ACCEPT_MOVING_WRITE_BUFFER);
               if (DH_KEY_LENGTH != null) {
                  SSLContext.setTmpDHLength(this.ctx, DH_KEY_LENGTH);
               }

               List<String> nextProtoList = apn.protocols();
               if (!nextProtoList.isEmpty()) {
                  String[] appProtocols = (String[])nextProtoList.toArray(new String[0]);
                  int selectorBehavior = opensslSelectorFailureBehavior(apn.selectorFailureBehavior());
                  switch(apn.protocol()) {
                     case NPN:
                        SSLContext.setNpnProtos(this.ctx, appProtocols, selectorBehavior);
                        break;
                     case ALPN:
                        SSLContext.setAlpnProtos(this.ctx, appProtocols, selectorBehavior);
                        break;
                     case NPN_AND_ALPN:
                        SSLContext.setNpnProtos(this.ctx, appProtocols, selectorBehavior);
                        SSLContext.setAlpnProtos(this.ctx, appProtocols, selectorBehavior);
                        break;
                     default:
                        throw new Error();
                  }
               }

               if (enableOcsp) {
                  SSLContext.enableOcsp(this.ctx, this.isClient());
               }

               SSLContext.setUseTasks(this.ctx, useTasks);
               if (privateKeyMethod != null) {
                  SSLContext.setPrivateKeyMethod(this.ctx, new ReferenceCountedOpenSslContext.PrivateKeyMethod(this.engineMap, privateKeyMethod));
               }

               if (asyncPrivateKeyMethod != null) {
                  SSLContext.setPrivateKeyMethod(this.ctx, new ReferenceCountedOpenSslContext.AsyncPrivateKeyMethod(this.engineMap, asyncPrivateKeyMethod));
               }

               if (certCompressionConfig != null) {
                  for(OpenSslCertificateCompressionConfig.AlgorithmConfig configPair : certCompressionConfig) {
                     CertificateCompressionAlgo algo = new ReferenceCountedOpenSslContext.CompressionAlgorithm(this.engineMap, configPair.algorithm());
                     switch(configPair.mode()) {
                        case Decompress:
                           SSLContext.addCertificateCompressionAlgorithm(this.ctx, SSL.SSL_CERT_COMPRESSION_DIRECTION_DECOMPRESS, algo);
                           break;
                        case Compress:
                           SSLContext.addCertificateCompressionAlgorithm(this.ctx, SSL.SSL_CERT_COMPRESSION_DIRECTION_COMPRESS, algo);
                           break;
                        case Both:
                           SSLContext.addCertificateCompressionAlgorithm(this.ctx, SSL.SSL_CERT_COMPRESSION_DIRECTION_BOTH, algo);
                           break;
                        default:
                           throw new IllegalStateException();
                     }
                  }
               }

               SSLContext.setCurvesList(this.ctx, OpenSsl.NAMED_GROUPS);
               success = true;
            } finally {
               if (!success) {
                  this.release();
               }

            }

         }
      }
   }

   private static int opensslSelectorFailureBehavior(ApplicationProtocolConfig.SelectorFailureBehavior behavior) {
      switch(behavior) {
         case NO_ADVERTISE:
            return 0;
         case CHOOSE_MY_LAST_PROTOCOL:
            return 1;
         default:
            throw new Error();
      }
   }

   @Override
   public final List<String> cipherSuites() {
      return this.unmodifiableCiphers;
   }

   @Override
   public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
      return this.apn;
   }

   @Override
   public final boolean isClient() {
      return this.mode == 0;
   }

   @Override
   public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
      return this.newEngine0(alloc, peerHost, peerPort, true);
   }

   @Override
   protected final SslHandler newHandler(ByteBufAllocator alloc, boolean startTls) {
      return new SslHandler(this.newEngine0(alloc, null, -1, false), startTls);
   }

   @Override
   protected final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls) {
      return new SslHandler(this.newEngine0(alloc, peerHost, peerPort, false), startTls);
   }

   @Override
   protected SslHandler newHandler(ByteBufAllocator alloc, boolean startTls, Executor executor) {
      return new SslHandler(this.newEngine0(alloc, null, -1, false), startTls, executor);
   }

   @Override
   protected SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls, Executor executor) {
      return new SslHandler(this.newEngine0(alloc, peerHost, peerPort, false), executor);
   }

   SSLEngine newEngine0(ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode) {
      return new ReferenceCountedOpenSslEngine(this, alloc, peerHost, peerPort, jdkCompatibilityMode, true);
   }

   @Override
   public final SSLEngine newEngine(ByteBufAllocator alloc) {
      return this.newEngine(alloc, null, -1);
   }

   @Deprecated
   public final long context() {
      return this.sslCtxPointer();
   }

   @Deprecated
   public final OpenSslSessionStats stats() {
      return this.sessionContext().stats();
   }

   @Deprecated
   public void setRejectRemoteInitiatedRenegotiation(boolean rejectRemoteInitiatedRenegotiation) {
      if (!rejectRemoteInitiatedRenegotiation) {
         throw new UnsupportedOperationException("Renegotiation is not supported");
      }
   }

   @Deprecated
   public boolean getRejectRemoteInitiatedRenegotiation() {
      return true;
   }

   public void setBioNonApplicationBufferSize(int bioNonApplicationBufferSize) {
      this.bioNonApplicationBufferSize = ObjectUtil.checkPositiveOrZero(bioNonApplicationBufferSize, "bioNonApplicationBufferSize");
   }

   public int getBioNonApplicationBufferSize() {
      return this.bioNonApplicationBufferSize;
   }

   @Deprecated
   public final void setTicketKeys(byte[] keys) {
      this.sessionContext().setTicketKeys(keys);
   }

   public abstract OpenSslSessionContext sessionContext();

   @Deprecated
   public final long sslCtxPointer() {
      Lock readerLock = this.ctxLock.readLock();
      readerLock.lock();

      long var2;
      try {
         var2 = SSLContext.getSslCtx(this.ctx);
      } finally {
         readerLock.unlock();
      }

      return var2;
   }

   @Deprecated
   public final void setPrivateKeyMethod(OpenSslPrivateKeyMethod method) {
      ObjectUtil.checkNotNull(method, "method");
      Lock writerLock = this.ctxLock.writeLock();
      writerLock.lock();

      try {
         SSLContext.setPrivateKeyMethod(this.ctx, new ReferenceCountedOpenSslContext.PrivateKeyMethod(this.engineMap, method));
      } finally {
         writerLock.unlock();
      }

   }

   @Deprecated
   public final void setUseTasks(boolean useTasks) {
      Lock writerLock = this.ctxLock.writeLock();
      writerLock.lock();

      try {
         SSLContext.setUseTasks(this.ctx, useTasks);
      } finally {
         writerLock.unlock();
      }

   }

   private void destroy() {
      Lock writerLock = this.ctxLock.writeLock();
      writerLock.lock();

      try {
         if (this.ctx != 0L) {
            if (this.enableOcsp) {
               SSLContext.disableOcsp(this.ctx);
            }

            SSLContext.free(this.ctx);
            this.ctx = 0L;
            OpenSslSessionContext context = this.sessionContext();
            if (context != null) {
               context.destroy();
            }
         }
      } finally {
         writerLock.unlock();
      }

   }

   protected static X509Certificate[] certificates(byte[][] chain) {
      X509Certificate[] peerCerts = new X509Certificate[chain.length];

      for(int i = 0; i < peerCerts.length; ++i) {
         peerCerts[i] = new LazyX509Certificate(chain[i]);
      }

      return peerCerts;
   }

   protected static X509TrustManager chooseTrustManager(TrustManager[] managers) {
      for(TrustManager m : managers) {
         if (m instanceof X509TrustManager) {
            if (PlatformDependent.javaVersion() >= 7) {
               return OpenSslX509TrustManagerWrapper.wrapIfNeeded((X509TrustManager)m);
            }

            return (X509TrustManager)m;
         }
      }

      throw new IllegalStateException("no X509TrustManager found");
   }

   protected static X509KeyManager chooseX509KeyManager(KeyManager[] kms) {
      for(KeyManager km : kms) {
         if (km instanceof X509KeyManager) {
            return (X509KeyManager)km;
         }
      }

      throw new IllegalStateException("no X509KeyManager found");
   }

   static OpenSslApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config) {
      if (config == null) {
         return NONE_PROTOCOL_NEGOTIATOR;
      } else {
         switch(config.protocol()) {
            case NPN:
            case ALPN:
            case NPN_AND_ALPN:
               switch(config.selectedListenerFailureBehavior()) {
                  case CHOOSE_MY_LAST_PROTOCOL:
                  case ACCEPT:
                     switch(config.selectorFailureBehavior()) {
                        case NO_ADVERTISE:
                        case CHOOSE_MY_LAST_PROTOCOL:
                           return new OpenSslDefaultApplicationProtocolNegotiator(config);
                        default:
                           throw new UnsupportedOperationException("OpenSSL provider does not support " + config.selectorFailureBehavior() + " behavior");
                     }
                  default:
                     throw new UnsupportedOperationException("OpenSSL provider does not support " + config.selectedListenerFailureBehavior() + " behavior");
               }
            case NONE:
               return NONE_PROTOCOL_NEGOTIATOR;
            default:
               throw new Error();
         }
      }
   }

   @SuppressJava6Requirement(
      reason = "Guarded by java version check"
   )
   static boolean useExtendedTrustManager(X509TrustManager trustManager) {
      return PlatformDependent.javaVersion() >= 7 && trustManager instanceof X509ExtendedTrustManager;
   }

   @Override
   public final int refCnt() {
      return this.refCnt.refCnt();
   }

   @Override
   public final ReferenceCounted retain() {
      this.refCnt.retain();
      return this;
   }

   @Override
   public final ReferenceCounted retain(int increment) {
      this.refCnt.retain(increment);
      return this;
   }

   @Override
   public final ReferenceCounted touch() {
      this.refCnt.touch();
      return this;
   }

   @Override
   public final ReferenceCounted touch(Object hint) {
      this.refCnt.touch(hint);
      return this;
   }

   @Override
   public final boolean release() {
      return this.refCnt.release();
   }

   @Override
   public final boolean release(int decrement) {
      return this.refCnt.release(decrement);
   }

   static void setKeyMaterial(long ctx, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword) throws SSLException {
      long keyBio = 0L;
      long keyCertChainBio = 0L;
      long keyCertChainBio2 = 0L;
      PemEncoded encoded = null;

      try {
         encoded = PemX509Certificate.toPEM(ByteBufAllocator.DEFAULT, true, keyCertChain);
         keyCertChainBio = toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
         keyCertChainBio2 = toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
         if (key != null) {
            keyBio = toBIO(ByteBufAllocator.DEFAULT, key);
         }

         SSLContext.setCertificateBio(ctx, keyCertChainBio, keyBio, keyPassword == null ? "" : keyPassword);
         SSLContext.setCertificateChainBio(ctx, keyCertChainBio2, true);
      } catch (SSLException var17) {
         throw var17;
      } catch (Exception var18) {
         throw new SSLException("failed to set certificate and key", var18);
      } finally {
         freeBio(keyBio);
         freeBio(keyCertChainBio);
         freeBio(keyCertChainBio2);
         if (encoded != null) {
            encoded.release();
         }

      }

   }

   static void freeBio(long bio) {
      if (bio != 0L) {
         SSL.freeBIO(bio);
      }

   }

   static long toBIO(ByteBufAllocator allocator, PrivateKey key) throws Exception {
      if (key == null) {
         return 0L;
      } else {
         PemEncoded pem = PemPrivateKey.toPEM(allocator, true, key);

         long var3;
         try {
            var3 = toBIO(allocator, pem.retain());
         } finally {
            pem.release();
         }

         return var3;
      }
   }

   static long toBIO(ByteBufAllocator allocator, X509Certificate... certChain) throws Exception {
      if (certChain == null) {
         return 0L;
      } else {
         ObjectUtil.checkNonEmpty((Object[])certChain, "certChain");
         PemEncoded pem = PemX509Certificate.toPEM(allocator, true, certChain);

         long var3;
         try {
            var3 = toBIO(allocator, pem.retain());
         } finally {
            pem.release();
         }

         return var3;
      }
   }

   static long toBIO(ByteBufAllocator allocator, PemEncoded pem) throws Exception {
      long var4;
      try {
         ByteBuf content = pem.content();
         if (content.isDirect()) {
            return newBIO(content.retainedSlice());
         }

         ByteBuf buffer = allocator.directBuffer(content.readableBytes());

         try {
            buffer.writeBytes(content, content.readerIndex(), content.readableBytes());
            var4 = newBIO(buffer.retainedSlice());
         } finally {
            try {
               if (pem.isSensitive()) {
                  SslUtils.zeroout(buffer);
               }
            } finally {
               buffer.release();
            }

         }
      } finally {
         pem.release();
      }

      return var4;
   }

   private static long newBIO(ByteBuf buffer) throws Exception {
      long var4;
      try {
         long bio = SSL.newMemBIO();
         int readable = buffer.readableBytes();
         if (SSL.bioWrite(bio, OpenSsl.memoryAddress(buffer) + (long)buffer.readerIndex(), readable) != readable) {
            SSL.freeBIO(bio);
            throw new IllegalStateException("Could not write data to memory BIO");
         }

         var4 = bio;
      } finally {
         buffer.release();
      }

      return var4;
   }

   static OpenSslKeyMaterialProvider providerFor(KeyManagerFactory factory, String password) {
      if (factory instanceof OpenSslX509KeyManagerFactory) {
         return ((OpenSslX509KeyManagerFactory)factory).newProvider();
      } else {
         return factory instanceof OpenSslCachingX509KeyManagerFactory
            ? ((OpenSslCachingX509KeyManagerFactory)factory).newProvider(password)
            : new OpenSslKeyMaterialProvider(chooseX509KeyManager(factory.getKeyManagers()), password);
      }
   }

   private static ReferenceCountedOpenSslEngine retrieveEngine(OpenSslEngineMap engineMap, long ssl) throws SSLException {
      ReferenceCountedOpenSslEngine engine = engineMap.get(ssl);
      if (engine == null) {
         throw new SSLException("Could not find a " + StringUtil.simpleClassName(ReferenceCountedOpenSslEngine.class) + " for sslPointer " + ssl);
      } else {
         return engine;
      }
   }

   private static byte[] verifyResult(byte[] result) throws SignatureException {
      if (result == null) {
         throw new SignatureException();
      } else {
         return result;
      }
   }

   static {
      Integer dhLen = null;

      try {
         String dhKeySize = SystemPropertyUtil.get("jdk.tls.ephemeralDHKeySize");
         if (dhKeySize != null) {
            try {
               dhLen = Integer.valueOf(dhKeySize);
            } catch (NumberFormatException var3) {
               logger.debug("ReferenceCountedOpenSslContext supports -Djdk.tls.ephemeralDHKeySize={int}, but got: " + dhKeySize);
            }
         }
      } catch (Throwable var4) {
      }

      DH_KEY_LENGTH = dhLen;
   }

   abstract static class AbstractCertificateVerifier extends CertificateVerifier {
      private final OpenSslEngineMap engineMap;

      AbstractCertificateVerifier(OpenSslEngineMap engineMap) {
         this.engineMap = engineMap;
      }

      public final int verify(long ssl, byte[][] chain, String auth) {
         ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
         if (engine == null) {
            return CertificateVerifier.X509_V_ERR_UNSPECIFIED;
         } else {
            X509Certificate[] peerCerts = ReferenceCountedOpenSslContext.certificates(chain);

            try {
               this.verify(engine, peerCerts, auth);
               return CertificateVerifier.X509_V_OK;
            } catch (Throwable var8) {
               ReferenceCountedOpenSslContext.logger.debug("verification of certificate failed", var8);
               engine.initHandshakeException(var8);
               if (var8 instanceof OpenSslCertificateException) {
                  return ((OpenSslCertificateException)var8).errorCode();
               } else if (var8 instanceof CertificateExpiredException) {
                  return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED;
               } else if (var8 instanceof CertificateNotYetValidException) {
                  return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID;
               } else {
                  return PlatformDependent.javaVersion() >= 7 ? translateToError(var8) : CertificateVerifier.X509_V_ERR_UNSPECIFIED;
               }
            }
         }
      }

      @SuppressJava6Requirement(
         reason = "Usage guarded by java version check"
      )
      private static int translateToError(Throwable cause) {
         if (cause instanceof CertificateRevokedException) {
            return CertificateVerifier.X509_V_ERR_CERT_REVOKED;
         } else {
            for(Throwable wrapped = cause.getCause(); wrapped != null; wrapped = wrapped.getCause()) {
               if (wrapped instanceof CertPathValidatorException) {
                  CertPathValidatorException ex = (CertPathValidatorException)wrapped;
                  Reason reason = ex.getReason();
                  if (reason == BasicReason.EXPIRED) {
                     return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED;
                  }

                  if (reason == BasicReason.NOT_YET_VALID) {
                     return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID;
                  }

                  if (reason == BasicReason.REVOKED) {
                     return CertificateVerifier.X509_V_ERR_CERT_REVOKED;
                  }
               }
            }

            return CertificateVerifier.X509_V_ERR_UNSPECIFIED;
         }
      }

      abstract void verify(ReferenceCountedOpenSslEngine var1, X509Certificate[] var2, String var3) throws Exception;
   }

   private static final class AsyncPrivateKeyMethod implements AsyncSSLPrivateKeyMethod {
      private final OpenSslEngineMap engineMap;
      private final OpenSslAsyncPrivateKeyMethod keyMethod;

      AsyncPrivateKeyMethod(OpenSslEngineMap engineMap, OpenSslAsyncPrivateKeyMethod keyMethod) {
         this.engineMap = engineMap;
         this.keyMethod = keyMethod;
      }

      public void sign(long ssl, int signatureAlgorithm, byte[] bytes, ResultCallback<byte[]> resultCallback) {
         try {
            ReferenceCountedOpenSslEngine engine = ReferenceCountedOpenSslContext.retrieveEngine(this.engineMap, ssl);
            this.keyMethod
               .sign(engine, signatureAlgorithm, bytes)
               .addListener(new ReferenceCountedOpenSslContext.AsyncPrivateKeyMethod.ResultCallbackListener(engine, ssl, resultCallback));
         } catch (SSLException var7) {
            resultCallback.onError(ssl, var7);
         }

      }

      public void decrypt(long ssl, byte[] bytes, ResultCallback<byte[]> resultCallback) {
         try {
            ReferenceCountedOpenSslEngine engine = ReferenceCountedOpenSslContext.retrieveEngine(this.engineMap, ssl);
            this.keyMethod
               .decrypt(engine, bytes)
               .addListener(new ReferenceCountedOpenSslContext.AsyncPrivateKeyMethod.ResultCallbackListener(engine, ssl, resultCallback));
         } catch (SSLException var6) {
            resultCallback.onError(ssl, var6);
         }

      }

      private static final class ResultCallbackListener implements FutureListener<byte[]> {
         private final ReferenceCountedOpenSslEngine engine;
         private final long ssl;
         private final ResultCallback<byte[]> resultCallback;

         ResultCallbackListener(ReferenceCountedOpenSslEngine engine, long ssl, ResultCallback<byte[]> resultCallback) {
            this.engine = engine;
            this.ssl = ssl;
            this.resultCallback = resultCallback;
         }

         @Override
         public void operationComplete(Future<byte[]> future) {
            Throwable cause = future.cause();
            if (cause == null) {
               try {
                  byte[] result = ReferenceCountedOpenSslContext.verifyResult(future.getNow());
                  this.resultCallback.onSuccess(this.ssl, result);
                  return;
               } catch (SignatureException var4) {
                  cause = var4;
                  this.engine.initHandshakeException(var4);
               }
            }

            this.resultCallback.onError(this.ssl, cause);
         }
      }
   }

   private static final class CompressionAlgorithm implements CertificateCompressionAlgo {
      private final OpenSslEngineMap engineMap;
      private final OpenSslCertificateCompressionAlgorithm compressionAlgorithm;

      CompressionAlgorithm(OpenSslEngineMap engineMap, OpenSslCertificateCompressionAlgorithm compressionAlgorithm) {
         this.engineMap = engineMap;
         this.compressionAlgorithm = compressionAlgorithm;
      }

      public byte[] compress(long ssl, byte[] bytes) throws Exception {
         ReferenceCountedOpenSslEngine engine = ReferenceCountedOpenSslContext.retrieveEngine(this.engineMap, ssl);
         return this.compressionAlgorithm.compress(engine, bytes);
      }

      public byte[] decompress(long ssl, int len, byte[] bytes) throws Exception {
         ReferenceCountedOpenSslEngine engine = ReferenceCountedOpenSslContext.retrieveEngine(this.engineMap, ssl);
         return this.compressionAlgorithm.decompress(engine, len, bytes);
      }

      public int algorithmId() {
         return this.compressionAlgorithm.algorithmId();
      }
   }

   private static final class DefaultOpenSslEngineMap implements OpenSslEngineMap {
      private final Map<Long, ReferenceCountedOpenSslEngine> engines = PlatformDependent.newConcurrentHashMap();

      private DefaultOpenSslEngineMap() {
      }

      @Override
      public ReferenceCountedOpenSslEngine remove(long ssl) {
         return (ReferenceCountedOpenSslEngine)this.engines.remove(ssl);
      }

      @Override
      public void add(ReferenceCountedOpenSslEngine engine) {
         this.engines.put(engine.sslPointer(), engine);
      }

      @Override
      public ReferenceCountedOpenSslEngine get(long ssl) {
         return (ReferenceCountedOpenSslEngine)this.engines.get(ssl);
      }
   }

   private static final class PrivateKeyMethod implements SSLPrivateKeyMethod {
      private final OpenSslEngineMap engineMap;
      private final OpenSslPrivateKeyMethod keyMethod;

      PrivateKeyMethod(OpenSslEngineMap engineMap, OpenSslPrivateKeyMethod keyMethod) {
         this.engineMap = engineMap;
         this.keyMethod = keyMethod;
      }

      public byte[] sign(long ssl, int signatureAlgorithm, byte[] digest) throws Exception {
         ReferenceCountedOpenSslEngine engine = ReferenceCountedOpenSslContext.retrieveEngine(this.engineMap, ssl);

         try {
            return ReferenceCountedOpenSslContext.verifyResult(this.keyMethod.sign(engine, signatureAlgorithm, digest));
         } catch (Exception var7) {
            engine.initHandshakeException(var7);
            throw var7;
         }
      }

      public byte[] decrypt(long ssl, byte[] input) throws Exception {
         ReferenceCountedOpenSslEngine engine = ReferenceCountedOpenSslContext.retrieveEngine(this.engineMap, ssl);

         try {
            return ReferenceCountedOpenSslContext.verifyResult(this.keyMethod.decrypt(engine, input));
         } catch (Exception var6) {
            engine.initHandshakeException(var6);
            throw var6;
         }
      }
   }
}
