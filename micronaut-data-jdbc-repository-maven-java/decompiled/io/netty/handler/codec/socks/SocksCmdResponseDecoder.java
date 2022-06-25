package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.NetUtil;
import java.util.List;

public class SocksCmdResponseDecoder extends ReplayingDecoder<SocksCmdResponseDecoder.State> {
   private SocksCmdStatus cmdStatus;
   private SocksAddressType addressType;

   public SocksCmdResponseDecoder() {
      super(SocksCmdResponseDecoder.State.CHECK_PROTOCOL_VERSION);
   }

   @Override
   protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
      label22:
      switch((SocksCmdResponseDecoder.State)this.state()) {
         case CHECK_PROTOCOL_VERSION:
            if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
               out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
               break;
            } else {
               this.checkpoint(SocksCmdResponseDecoder.State.READ_CMD_HEADER);
            }
         case READ_CMD_HEADER:
            this.cmdStatus = SocksCmdStatus.valueOf(byteBuf.readByte());
            byteBuf.skipBytes(1);
            this.addressType = SocksAddressType.valueOf(byteBuf.readByte());
            this.checkpoint(SocksCmdResponseDecoder.State.READ_CMD_ADDRESS);
         case READ_CMD_ADDRESS:
            switch(this.addressType) {
               case IPv4: {
                  String host = NetUtil.intToIpAddress(byteBuf.readInt());
                  int port = byteBuf.readUnsignedShort();
                  out.add(new SocksCmdResponse(this.cmdStatus, this.addressType, host, port));
                  break label22;
               }
               case DOMAIN: {
                  int fieldLength = byteBuf.readByte();
                  String host = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
                  int port = byteBuf.readUnsignedShort();
                  out.add(new SocksCmdResponse(this.cmdStatus, this.addressType, host, port));
                  break label22;
               }
               case IPv6: {
                  byte[] bytes = new byte[16];
                  byteBuf.readBytes(bytes);
                  String host = SocksCommonUtils.ipv6toStr(bytes);
                  int port = byteBuf.readUnsignedShort();
                  out.add(new SocksCmdResponse(this.cmdStatus, this.addressType, host, port));
                  break label22;
               }
               case UNKNOWN:
                  out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
                  break label22;
               default:
                  throw new Error();
            }
         default:
            throw new Error();
      }

      ctx.pipeline().remove(this);
   }

   public static enum State {
      CHECK_PROTOCOL_VERSION,
      READ_CMD_HEADER,
      READ_CMD_ADDRESS;
   }
}
