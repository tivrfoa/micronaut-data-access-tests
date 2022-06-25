package com.mysql.cj.protocol;

import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.conf.PropertySet;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.Proxy.Type;

public class SocksProxySocketFactory extends StandardSocketFactory {
   @Override
   protected Socket createSocket(PropertySet props) {
      String socksProxyHost = (String)props.getStringProperty(PropertyKey.socksProxyHost).getValue();
      int socksProxyPort = props.getIntegerProperty(PropertyKey.socksProxyPort).getValue();
      return new Socket(new Proxy(Type.SOCKS, new InetSocketAddress(socksProxyHost, socksProxyPort)));
   }

   @Override
   public <T extends Closeable> T connect(String hostname, int portNumber, PropertySet pset, int loginTimeout) throws IOException {
      if (!pset.getBooleanProperty(PropertyKey.socksProxyRemoteDns).getValue()) {
         return super.connect(hostname, portNumber, pset, loginTimeout);
      } else {
         this.loginTimeoutCountdown = loginTimeout;
         if (pset != null && hostname != null) {
            this.host = hostname;
            this.port = portNumber;
            String localSocketHostname = (String)pset.getStringProperty(PropertyKey.localSocketAddress).getValue();
            InetSocketAddress localSockAddr = localSocketHostname != null && localSocketHostname.length() > 0
               ? new InetSocketAddress(InetAddress.getByName(localSocketHostname), 0)
               : null;
            int connectTimeout = pset.getIntegerProperty(PropertyKey.connectTimeout).getValue();

            try {
               this.rawSocket = this.createSocket(pset);
               this.configureSocket(this.rawSocket, pset);
               if (localSockAddr != null) {
                  this.rawSocket.bind(localSockAddr);
               }

               this.rawSocket.connect(InetSocketAddress.createUnresolved(this.host, this.port), this.getRealTimeout(connectTimeout));
            } catch (SocketException var9) {
               this.rawSocket = null;
               throw var9;
            }

            this.resetLoginTimeCountdown();
            this.sslSocket = this.rawSocket;
            return (T)this.rawSocket;
         } else {
            throw new SocketException("Unable to create socket");
         }
      }
   }
}
