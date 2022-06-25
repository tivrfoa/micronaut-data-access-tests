package com.mysql.cj.protocol.a.authentication;

import com.mysql.cj.Messages;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.UnableToConnectException;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.StringUtils;
import java.security.DigestException;
import java.util.List;

public class CachingSha2PasswordPlugin extends Sha256PasswordPlugin {
   public static String PLUGIN_NAME = "caching_sha2_password";
   private CachingSha2PasswordPlugin.AuthStage stage = CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_SEND_SCRAMBLE;

   @Override
   public void init(Protocol<NativePacketPayload> prot) {
      super.init(prot);
      this.stage = CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_SEND_SCRAMBLE;
   }

   @Override
   public void reset() {
      this.stage = CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_SEND_SCRAMBLE;
   }

   @Override
   public void destroy() {
      super.destroy();
      this.stage = CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_SEND_SCRAMBLE;
   }

   @Override
   public String getProtocolPluginName() {
      return PLUGIN_NAME;
   }

   @Override
   public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
      toServer.clear();
      if (this.password != null && this.password.length() != 0 && fromServer != null) {
         try {
            if (this.stage == CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_SEND_SCRAMBLE) {
               this.seed = fromServer.readString(NativeConstants.StringSelfDataType.STRING_TERM, null);
               toServer.add(
                  new NativePacketPayload(
                     Security.scrambleCachingSha2(
                        StringUtils.getBytes(this.password, this.protocol.getServerSession().getCharsetSettings().getPasswordCharacterEncoding()),
                        this.seed.getBytes()
                     )
                  )
               );
               this.stage = CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_READ_RESULT;
               return true;
            }

            if (this.stage == CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_READ_RESULT) {
               int fastAuthResult = fromServer.readBytes(NativeConstants.StringLengthDataType.STRING_FIXED, 1)[0];
               switch(fastAuthResult) {
                  case 3:
                     this.stage = CachingSha2PasswordPlugin.AuthStage.FAST_AUTH_COMPLETE;
                     return true;
                  case 4:
                     this.stage = CachingSha2PasswordPlugin.AuthStage.FULL_AUTH;
                     break;
                  default:
                     throw ExceptionFactory.createException("Unknown server response after fast auth.", this.protocol.getExceptionInterceptor());
               }
            }

            if (this.protocol.getSocketConnection().isSSLEstablished()) {
               NativePacketPayload packet = new NativePacketPayload(
                  StringUtils.getBytes(this.password, this.protocol.getServerSession().getCharsetSettings().getPasswordCharacterEncoding())
               );
               packet.setPosition(packet.getPayloadLength());
               packet.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
               packet.setPosition(0);
               toServer.add(packet);
            } else if (this.serverRSAPublicKeyFile.getValue() != null) {
               NativePacketPayload packet = new NativePacketPayload(this.encryptPassword());
               toServer.add(packet);
            } else {
               if (!this.protocol.getPropertySet().getBooleanProperty(PropertyKey.allowPublicKeyRetrieval).getValue()) {
                  throw (UnableToConnectException)ExceptionFactory.createException(
                     UnableToConnectException.class, Messages.getString("Sha256PasswordPlugin.2"), this.protocol.getExceptionInterceptor()
                  );
               }

               if (this.publicKeyRequested && fromServer.getPayloadLength() > 21) {
                  this.publicKeyString = fromServer.readString(NativeConstants.StringSelfDataType.STRING_TERM, null);
                  NativePacketPayload packet = new NativePacketPayload(this.encryptPassword());
                  toServer.add(packet);
                  this.publicKeyRequested = false;
               } else {
                  NativePacketPayload packet = new NativePacketPayload(new byte[]{2});
                  toServer.add(packet);
                  this.publicKeyRequested = true;
               }
            }
         } catch (DigestException | CJException var4) {
            throw ExceptionFactory.createException(var4.getMessage(), var4, this.protocol.getExceptionInterceptor());
         }
      } else {
         NativePacketPayload packet = new NativePacketPayload(new byte[]{0});
         toServer.add(packet);
      }

      return true;
   }

   @Override
   protected byte[] encryptPassword() {
      return this.protocol.versionMeetsMinimum(8, 0, 5) ? super.encryptPassword() : super.encryptPassword("RSA/ECB/PKCS1Padding");
   }

   public static enum AuthStage {
      FAST_AUTH_SEND_SCRAMBLE,
      FAST_AUTH_READ_RESULT,
      FAST_AUTH_COMPLETE,
      FULL_AUTH;
   }
}
