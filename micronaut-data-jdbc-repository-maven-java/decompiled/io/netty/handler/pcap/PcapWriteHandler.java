package io.netty.handler.pcap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;

public final class PcapWriteHandler extends ChannelDuplexHandler implements Closeable {
   private final InternalLogger logger = InternalLoggerFactory.getInstance(PcapWriteHandler.class);
   private PcapWriter pCapWriter;
   private final OutputStream outputStream;
   private final boolean captureZeroByte;
   private final boolean writePcapGlobalHeader;
   private int sendSegmentNumber = 1;
   private int receiveSegmentNumber = 1;
   private InetSocketAddress initiatiorAddr;
   private InetSocketAddress handlerAddr;
   private boolean isServerPipeline;
   private boolean isClosed;

   public PcapWriteHandler(OutputStream outputStream) {
      this(outputStream, false, true);
   }

   public PcapWriteHandler(OutputStream outputStream, boolean captureZeroByte, boolean writePcapGlobalHeader) {
      this.outputStream = ObjectUtil.checkNotNull(outputStream, "OutputStream");
      this.captureZeroByte = captureZeroByte;
      this.writePcapGlobalHeader = writePcapGlobalHeader;
   }

   @Override
   public void channelActive(ChannelHandlerContext ctx) throws Exception {
      ByteBufAllocator byteBufAllocator = ctx.alloc();
      if (this.writePcapGlobalHeader) {
         ByteBuf byteBuf = byteBufAllocator.buffer();

         try {
            this.pCapWriter = new PcapWriter(this.outputStream, byteBuf);
         } catch (IOException var14) {
            ctx.channel().close();
            ctx.fireExceptionCaught(var14);
            this.logger.error("Caught Exception While Initializing PcapWriter, Closing Channel.", var14);
         } finally {
            byteBuf.release();
         }
      } else {
         this.pCapWriter = new PcapWriter(this.outputStream);
      }

      if (ctx.channel() instanceof SocketChannel) {
         if (ctx.channel().parent() instanceof ServerSocketChannel) {
            this.isServerPipeline = true;
            this.initiatiorAddr = (InetSocketAddress)ctx.channel().remoteAddress();
            this.handlerAddr = (InetSocketAddress)ctx.channel().localAddress();
         } else {
            this.isServerPipeline = false;
            this.initiatiorAddr = (InetSocketAddress)ctx.channel().localAddress();
            this.handlerAddr = (InetSocketAddress)ctx.channel().remoteAddress();
         }

         this.logger.debug("Initiating Fake TCP 3-Way Handshake");
         ByteBuf tcpBuf = byteBufAllocator.buffer();

         try {
            TCPPacket.writePacket(tcpBuf, null, 0, 0, this.initiatiorAddr.getPort(), this.handlerAddr.getPort(), TCPPacket.TCPFlag.SYN);
            this.completeTCPWrite(this.initiatiorAddr, this.handlerAddr, tcpBuf, byteBufAllocator, ctx);
            TCPPacket.writePacket(tcpBuf, null, 0, 1, this.handlerAddr.getPort(), this.initiatiorAddr.getPort(), TCPPacket.TCPFlag.SYN, TCPPacket.TCPFlag.ACK);
            this.completeTCPWrite(this.handlerAddr, this.initiatiorAddr, tcpBuf, byteBufAllocator, ctx);
            TCPPacket.writePacket(tcpBuf, null, 1, 1, this.initiatiorAddr.getPort(), this.handlerAddr.getPort(), TCPPacket.TCPFlag.ACK);
            this.completeTCPWrite(this.initiatiorAddr, this.handlerAddr, tcpBuf, byteBufAllocator, ctx);
         } finally {
            tcpBuf.release();
         }

         this.logger.debug("Finished Fake TCP 3-Way Handshake");
      } else if (ctx.channel() instanceof DatagramChannel) {
         DatagramChannel datagramChannel = (DatagramChannel)ctx.channel();
         if (datagramChannel.isConnected()) {
            this.initiatiorAddr = (InetSocketAddress)ctx.channel().localAddress();
            this.handlerAddr = (InetSocketAddress)ctx.channel().remoteAddress();
         }
      }

      super.channelActive(ctx);
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (!this.isClosed) {
         if (ctx.channel() instanceof SocketChannel) {
            this.handleTCP(ctx, msg, false);
         } else if (ctx.channel() instanceof DatagramChannel) {
            this.handleUDP(ctx, msg);
         } else {
            this.logger.debug("Discarding Pcap Write for Unknown Channel Type: {}", ctx.channel());
         }
      }

      super.channelRead(ctx, msg);
   }

   @Override
   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (!this.isClosed) {
         if (ctx.channel() instanceof SocketChannel) {
            this.handleTCP(ctx, msg, true);
         } else if (ctx.channel() instanceof DatagramChannel) {
            this.handleUDP(ctx, msg);
         } else {
            this.logger.debug("Discarding Pcap Write for Unknown Channel Type: {}", ctx.channel());
         }
      }

      super.write(ctx, msg, promise);
   }

   private void handleTCP(ChannelHandlerContext ctx, Object msg, boolean isWriteOperation) {
      if (msg instanceof ByteBuf) {
         if (((ByteBuf)msg).readableBytes() == 0 && !this.captureZeroByte) {
            this.logger.debug("Discarding Zero Byte TCP Packet. isWriteOperation {}", isWriteOperation);
            return;
         }

         ByteBufAllocator byteBufAllocator = ctx.alloc();
         ByteBuf packet = ((ByteBuf)msg).duplicate();
         ByteBuf tcpBuf = byteBufAllocator.buffer();
         int bytes = packet.readableBytes();

         try {
            if (isWriteOperation) {
               InetSocketAddress srcAddr;
               InetSocketAddress dstAddr;
               if (this.isServerPipeline) {
                  srcAddr = this.handlerAddr;
                  dstAddr = this.initiatiorAddr;
               } else {
                  srcAddr = this.initiatiorAddr;
                  dstAddr = this.handlerAddr;
               }

               TCPPacket.writePacket(
                  tcpBuf, packet, this.sendSegmentNumber, this.receiveSegmentNumber, srcAddr.getPort(), dstAddr.getPort(), TCPPacket.TCPFlag.ACK
               );
               this.completeTCPWrite(srcAddr, dstAddr, tcpBuf, byteBufAllocator, ctx);
               this.logTCP(true, bytes, this.sendSegmentNumber, this.receiveSegmentNumber, srcAddr, dstAddr, false);
               this.sendSegmentNumber += bytes;
               TCPPacket.writePacket(
                  tcpBuf, null, this.receiveSegmentNumber, this.sendSegmentNumber, dstAddr.getPort(), srcAddr.getPort(), TCPPacket.TCPFlag.ACK
               );
               this.completeTCPWrite(dstAddr, srcAddr, tcpBuf, byteBufAllocator, ctx);
               this.logTCP(true, bytes, this.sendSegmentNumber, this.receiveSegmentNumber, dstAddr, srcAddr, true);
            } else {
               InetSocketAddress srcAddr;
               InetSocketAddress dstAddr;
               if (this.isServerPipeline) {
                  srcAddr = this.initiatiorAddr;
                  dstAddr = this.handlerAddr;
               } else {
                  srcAddr = this.handlerAddr;
                  dstAddr = this.initiatiorAddr;
               }

               TCPPacket.writePacket(
                  tcpBuf, packet, this.receiveSegmentNumber, this.sendSegmentNumber, srcAddr.getPort(), dstAddr.getPort(), TCPPacket.TCPFlag.ACK
               );
               this.completeTCPWrite(srcAddr, dstAddr, tcpBuf, byteBufAllocator, ctx);
               this.logTCP(false, bytes, this.receiveSegmentNumber, this.sendSegmentNumber, srcAddr, dstAddr, false);
               this.receiveSegmentNumber += bytes;
               TCPPacket.writePacket(
                  tcpBuf, null, this.sendSegmentNumber, this.receiveSegmentNumber, dstAddr.getPort(), srcAddr.getPort(), TCPPacket.TCPFlag.ACK
               );
               this.completeTCPWrite(dstAddr, srcAddr, tcpBuf, byteBufAllocator, ctx);
               this.logTCP(false, bytes, this.sendSegmentNumber, this.receiveSegmentNumber, dstAddr, srcAddr, true);
            }
         } finally {
            tcpBuf.release();
         }
      } else {
         this.logger.debug("Discarding Pcap Write for TCP Object: {}", msg);
      }

   }

   private void completeTCPWrite(
      InetSocketAddress srcAddr, InetSocketAddress dstAddr, ByteBuf tcpBuf, ByteBufAllocator byteBufAllocator, ChannelHandlerContext ctx
   ) {
      ByteBuf ipBuf = byteBufAllocator.buffer();
      ByteBuf ethernetBuf = byteBufAllocator.buffer();
      ByteBuf pcap = byteBufAllocator.buffer();

      try {
         try {
            if (srcAddr.getAddress() instanceof Inet4Address && dstAddr.getAddress() instanceof Inet4Address) {
               IPPacket.writeTCPv4(
                  ipBuf, tcpBuf, NetUtil.ipv4AddressToInt((Inet4Address)srcAddr.getAddress()), NetUtil.ipv4AddressToInt((Inet4Address)dstAddr.getAddress())
               );
               EthernetPacket.writeIPv4(ethernetBuf, ipBuf);
            } else {
               if (!(srcAddr.getAddress() instanceof Inet6Address) || !(dstAddr.getAddress() instanceof Inet6Address)) {
                  this.logger
                     .error(
                        "Source and Destination IP Address versions are not same. Source Address: {}, Destination Address: {}",
                        srcAddr.getAddress(),
                        dstAddr.getAddress()
                     );
                  return;
               }

               IPPacket.writeTCPv6(ipBuf, tcpBuf, srcAddr.getAddress().getAddress(), dstAddr.getAddress().getAddress());
               EthernetPacket.writeIPv6(ethernetBuf, ipBuf);
            }

            this.pCapWriter.writePacket(pcap, ethernetBuf);
         } catch (IOException var13) {
            this.logger.error("Caught Exception While Writing Packet into Pcap", var13);
            ctx.fireExceptionCaught(var13);
         }

      } finally {
         ipBuf.release();
         ethernetBuf.release();
         pcap.release();
      }
   }

   private void logTCP(
      boolean isWriteOperation,
      int bytes,
      int sendSegmentNumber,
      int receiveSegmentNumber,
      InetSocketAddress srcAddr,
      InetSocketAddress dstAddr,
      boolean ackOnly
   ) {
      if (this.logger.isDebugEnabled()) {
         if (ackOnly) {
            this.logger
               .debug(
                  "Writing TCP ACK, isWriteOperation {}, Segment Number {}, Ack Number {}, Src Addr {}, Dst Addr {}",
                  isWriteOperation,
                  sendSegmentNumber,
                  receiveSegmentNumber,
                  dstAddr,
                  srcAddr
               );
         } else {
            this.logger
               .debug(
                  "Writing TCP Data of {} Bytes, isWriteOperation {}, Segment Number {}, Ack Number {}, Src Addr {}, Dst Addr {}",
                  bytes,
                  isWriteOperation,
                  sendSegmentNumber,
                  receiveSegmentNumber,
                  srcAddr,
                  dstAddr
               );
         }
      }

   }

   private void handleUDP(ChannelHandlerContext ctx, Object msg) {
      ByteBuf udpBuf = ctx.alloc().buffer();

      try {
         if (!(msg instanceof DatagramPacket)) {
            if (msg instanceof ByteBuf && ((DatagramChannel)ctx.channel()).isConnected()) {
               if (((ByteBuf)msg).readableBytes() != 0 || this.captureZeroByte) {
                  ByteBuf byteBuf = ((ByteBuf)msg).duplicate();
                  this.logger.debug("Writing UDP Data of {} Bytes, Src Addr {}, Dst Addr {}", byteBuf.readableBytes(), this.initiatiorAddr, this.handlerAddr);
                  UDPPacket.writePacket(udpBuf, byteBuf, this.initiatiorAddr.getPort(), this.handlerAddr.getPort());
                  this.completeUDPWrite(this.initiatiorAddr, this.handlerAddr, udpBuf, ctx.alloc(), ctx);
                  return;
               }

               this.logger.debug("Discarding Zero Byte UDP Packet");
               return;
            }

            this.logger.debug("Discarding Pcap Write for UDP Object: {}", msg);
            return;
         }

         if (((DatagramPacket)msg).content().readableBytes() != 0 || this.captureZeroByte) {
            DatagramPacket datagramPacket = ((DatagramPacket)msg).duplicate();
            InetSocketAddress srcAddr = datagramPacket.sender();
            InetSocketAddress dstAddr = datagramPacket.recipient();
            if (srcAddr == null) {
               srcAddr = (InetSocketAddress)ctx.channel().localAddress();
            }

            this.logger.debug("Writing UDP Data of {} Bytes, Src Addr {}, Dst Addr {}", datagramPacket.content().readableBytes(), srcAddr, dstAddr);
            UDPPacket.writePacket(udpBuf, datagramPacket.content(), srcAddr.getPort(), dstAddr.getPort());
            this.completeUDPWrite(srcAddr, dstAddr, udpBuf, ctx.alloc(), ctx);
            return;
         }

         this.logger.debug("Discarding Zero Byte UDP Packet");
      } finally {
         udpBuf.release();
      }

   }

   private void completeUDPWrite(
      InetSocketAddress srcAddr, InetSocketAddress dstAddr, ByteBuf udpBuf, ByteBufAllocator byteBufAllocator, ChannelHandlerContext ctx
   ) {
      ByteBuf ipBuf = byteBufAllocator.buffer();
      ByteBuf ethernetBuf = byteBufAllocator.buffer();
      ByteBuf pcap = byteBufAllocator.buffer();

      try {
         try {
            if (srcAddr.getAddress() instanceof Inet4Address && dstAddr.getAddress() instanceof Inet4Address) {
               IPPacket.writeUDPv4(
                  ipBuf, udpBuf, NetUtil.ipv4AddressToInt((Inet4Address)srcAddr.getAddress()), NetUtil.ipv4AddressToInt((Inet4Address)dstAddr.getAddress())
               );
               EthernetPacket.writeIPv4(ethernetBuf, ipBuf);
            } else {
               if (!(srcAddr.getAddress() instanceof Inet6Address) || !(dstAddr.getAddress() instanceof Inet6Address)) {
                  this.logger
                     .error(
                        "Source and Destination IP Address versions are not same. Source Address: {}, Destination Address: {}",
                        srcAddr.getAddress(),
                        dstAddr.getAddress()
                     );
                  return;
               }

               IPPacket.writeUDPv6(ipBuf, udpBuf, srcAddr.getAddress().getAddress(), dstAddr.getAddress().getAddress());
               EthernetPacket.writeIPv6(ethernetBuf, ipBuf);
            }

            this.pCapWriter.writePacket(pcap, ethernetBuf);
         } catch (IOException var13) {
            this.logger.error("Caught Exception While Writing Packet into Pcap", var13);
            ctx.fireExceptionCaught(var13);
         }

      } finally {
         ipBuf.release();
         ethernetBuf.release();
         pcap.release();
      }
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      if (ctx.channel() instanceof SocketChannel) {
         this.logger.debug("Starting Fake TCP FIN+ACK Flow to close connection");
         ByteBufAllocator byteBufAllocator = ctx.alloc();
         ByteBuf tcpBuf = byteBufAllocator.buffer();

         try {
            TCPPacket.writePacket(
               tcpBuf,
               null,
               this.sendSegmentNumber,
               this.receiveSegmentNumber,
               this.initiatiorAddr.getPort(),
               this.handlerAddr.getPort(),
               TCPPacket.TCPFlag.FIN,
               TCPPacket.TCPFlag.ACK
            );
            this.completeTCPWrite(this.initiatiorAddr, this.handlerAddr, tcpBuf, byteBufAllocator, ctx);
            TCPPacket.writePacket(
               tcpBuf,
               null,
               this.receiveSegmentNumber,
               this.sendSegmentNumber,
               this.handlerAddr.getPort(),
               this.initiatiorAddr.getPort(),
               TCPPacket.TCPFlag.FIN,
               TCPPacket.TCPFlag.ACK
            );
            this.completeTCPWrite(this.handlerAddr, this.initiatiorAddr, tcpBuf, byteBufAllocator, ctx);
            TCPPacket.writePacket(
               tcpBuf,
               null,
               this.sendSegmentNumber + 1,
               this.receiveSegmentNumber + 1,
               this.initiatiorAddr.getPort(),
               this.handlerAddr.getPort(),
               TCPPacket.TCPFlag.ACK
            );
            this.completeTCPWrite(this.initiatiorAddr, this.handlerAddr, tcpBuf, byteBufAllocator, ctx);
         } finally {
            tcpBuf.release();
         }

         this.logger.debug("Finished Fake TCP FIN+ACK Flow to close connection");
      }

      this.close();
      super.handlerRemoved(ctx);
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      if (ctx.channel() instanceof SocketChannel) {
         ByteBuf tcpBuf = ctx.alloc().buffer();

         try {
            TCPPacket.writePacket(
               tcpBuf,
               null,
               this.sendSegmentNumber,
               this.receiveSegmentNumber,
               this.initiatiorAddr.getPort(),
               this.handlerAddr.getPort(),
               TCPPacket.TCPFlag.RST,
               TCPPacket.TCPFlag.ACK
            );
            this.completeTCPWrite(this.initiatiorAddr, this.handlerAddr, tcpBuf, ctx.alloc(), ctx);
         } finally {
            tcpBuf.release();
         }

         this.logger.debug("Sent Fake TCP RST to close connection");
      }

      this.close();
      ctx.fireExceptionCaught(cause);
   }

   public void close() throws IOException {
      if (this.isClosed) {
         this.logger.debug("PcapWriterHandler is already closed");
      } else {
         this.isClosed = true;
         this.pCapWriter.close();
         this.logger.debug("PcapWriterHandler is now closed");
      }

   }
}
