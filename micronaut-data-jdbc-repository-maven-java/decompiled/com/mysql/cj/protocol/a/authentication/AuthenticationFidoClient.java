package com.mysql.cj.protocol.a.authentication;

import com.mysql.cj.Messages;
import com.mysql.cj.callback.FidoAuthenticationCallback;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.callback.UsernameCallback;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.Util;
import java.util.List;

public class AuthenticationFidoClient implements AuthenticationPlugin<NativePacketPayload> {
   public static String PLUGIN_NAME = "authentication_fido_client";
   private String sourceOfAuthData = PLUGIN_NAME;
   private MysqlCallbackHandler usernameCallbackHandler = null;
   private MysqlCallbackHandler fidoAuthenticationCallbackHandler = null;

   @Override
   public void init(Protocol<NativePacketPayload> protocol, MysqlCallbackHandler callbackHandler) {
      this.usernameCallbackHandler = callbackHandler;
      String fidoCallbackHandlerClassName = (String)protocol.getPropertySet().getStringProperty(PropertyKey.authenticationFidoCallbackHandler).getValue();
      if (fidoCallbackHandlerClassName == null) {
         throw ExceptionFactory.createException(Messages.getString("AuthenticationFidoClientPlugin.MissingCallbackHandler"));
      } else {
         this.fidoAuthenticationCallbackHandler = (MysqlCallbackHandler)Util.getInstance(
            fidoCallbackHandlerClassName,
            null,
            null,
            protocol.getExceptionInterceptor(),
            Messages.getString("AuthenticationFidoClientPlugin.FailedInstantiatingCallbackHandler", new Object[]{fidoCallbackHandlerClassName})
         );
      }
   }

   @Override
   public void destroy() {
      this.reset();
      this.usernameCallbackHandler = null;
      this.fidoAuthenticationCallbackHandler = null;
   }

   @Override
   public String getProtocolPluginName() {
      return PLUGIN_NAME;
   }

   @Override
   public boolean requiresConfidentiality() {
      return false;
   }

   @Override
   public boolean isReusable() {
      return false;
   }

   @Override
   public void setAuthenticationParameters(String user, String password) {
      if (user == null && this.usernameCallbackHandler != null) {
         this.usernameCallbackHandler.handle(new UsernameCallback(System.getProperty("user.name")));
      }

   }

   @Override
   public void setSourceOfAuthData(String sourceOfAuthData) {
      this.sourceOfAuthData = sourceOfAuthData;
   }

   public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
      toServer.clear();
      if (!this.sourceOfAuthData.equals(PLUGIN_NAME)) {
         return true;
      } else if (fromServer.getPayloadLength() == 0) {
         throw ExceptionFactory.createException(Messages.getString("AuthenticationFidoClientPlugin.IncompleteRegistration"));
      } else {
         byte[] scramble = fromServer.readBytes(NativeConstants.StringSelfDataType.STRING_LENENC);
         String relyingPartyId = fromServer.readString(NativeConstants.StringSelfDataType.STRING_LENENC, "UTF-8");
         byte[] credentialId = fromServer.readBytes(NativeConstants.StringSelfDataType.STRING_LENENC);
         FidoAuthenticationCallback fidoAuthCallback = new FidoAuthenticationCallback(scramble, relyingPartyId, credentialId);
         this.fidoAuthenticationCallbackHandler.handle(fidoAuthCallback);
         byte[] authenticatorData = fidoAuthCallback.getAuthenticatorData();
         if (authenticatorData != null && authenticatorData.length != 0) {
            byte[] signature = fidoAuthCallback.getSignature();
            if (signature != null && signature.length != 0) {
               NativePacketPayload packet = new NativePacketPayload(authenticatorData.length + signature.length + 4);
               packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, authenticatorData);
               packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, signature);
               toServer.add(packet);
               return true;
            } else {
               throw ExceptionFactory.createException(Messages.getString("AuthenticationFidoClientPlugin.InvalidSignature"));
            }
         } else {
            throw ExceptionFactory.createException(Messages.getString("AuthenticationFidoClientPlugin.InvalidAuthenticatorData"));
         }
      }
   }
}
