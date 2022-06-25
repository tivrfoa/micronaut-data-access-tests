package io.netty.handler.codec.http2;

import io.netty.util.internal.StringUtil;

final class DefaultHttp2SettingsAckFrame implements Http2SettingsAckFrame {
   @Override
   public String name() {
      return "SETTINGS(ACK)";
   }

   public String toString() {
      return StringUtil.simpleClassName(this);
   }
}
