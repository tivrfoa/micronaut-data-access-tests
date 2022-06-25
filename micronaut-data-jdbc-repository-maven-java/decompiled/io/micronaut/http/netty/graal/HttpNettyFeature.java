package io.micronaut.http.netty.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.bind.binders.ContinuationArgumentBinder;
import io.micronaut.http.netty.channel.NettyThreadFactory;
import io.micronaut.http.netty.channel.converters.EpollChannelOptionFactory;
import io.micronaut.http.netty.channel.converters.KQueueChannelOptionFactory;
import io.micronaut.http.netty.websocket.NettyWebSocketSession;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;

@Internal
@AutomaticFeature
public class HttpNettyFeature implements Feature {
   public void beforeAnalysis(BeforeAnalysisAccess access) {
      RuntimeClassInitialization.initializeAtRunTime(
         new String[]{
            "io.micronaut.http.server.netty.ServerAttributeKeys",
            "io.micronaut.http.server.netty.handler.accesslog.HttpAccessLogHandler",
            "io.micronaut.session.http.SessionLogElement",
            "io.micronaut.http.client.netty.ConnectTTLHandler",
            "io.micronaut.http.client.netty.DefaultHttpClient",
            "io.micronaut.http.server.netty.websocket.NettyServerWebSocketUpgradeHandler",
            "io.micronaut.buffer.netty.NettyByteBufferFactory"
         }
      );
      RuntimeClassInitialization.initializeAtRunTime(
         new Class[]{
            NettyWebSocketSession.class,
            NettyThreadFactory.class,
            EpollChannelOptionFactory.class,
            KQueueChannelOptionFactory.class,
            ContinuationArgumentBinder.class,
            ContinuationArgumentBinder.Companion.class
         }
      );
   }
}
