package io.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakHint;
import io.netty.util.concurrent.AbstractEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.OrderedEventExecutor;
import io.netty.util.internal.ObjectPool;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PromiseNotificationUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

abstract class AbstractChannelHandlerContext implements ChannelHandlerContext, ResourceLeakHint {
   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannelHandlerContext.class);
   volatile AbstractChannelHandlerContext next;
   volatile AbstractChannelHandlerContext prev;
   private static final AtomicIntegerFieldUpdater<AbstractChannelHandlerContext> HANDLER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(
      AbstractChannelHandlerContext.class, "handlerState"
   );
   private static final int ADD_PENDING = 1;
   private static final int ADD_COMPLETE = 2;
   private static final int REMOVE_COMPLETE = 3;
   private static final int INIT = 0;
   private final DefaultChannelPipeline pipeline;
   private final String name;
   private final boolean ordered;
   private final int executionMask;
   final EventExecutor executor;
   private ChannelFuture succeededFuture;
   private AbstractChannelHandlerContext.Tasks invokeTasks;
   private volatile int handlerState = 0;

   AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutor executor, String name, Class<? extends ChannelHandler> handlerClass) {
      this.name = ObjectUtil.checkNotNull(name, "name");
      this.pipeline = pipeline;
      this.executor = executor;
      this.executionMask = ChannelHandlerMask.mask(handlerClass);
      this.ordered = executor == null || executor instanceof OrderedEventExecutor;
   }

   @Override
   public Channel channel() {
      return this.pipeline.channel();
   }

   @Override
   public ChannelPipeline pipeline() {
      return this.pipeline;
   }

   @Override
   public ByteBufAllocator alloc() {
      return this.channel().config().getAllocator();
   }

   @Override
   public EventExecutor executor() {
      return (EventExecutor)(this.executor == null ? this.channel().eventLoop() : this.executor);
   }

   @Override
   public String name() {
      return this.name;
   }

   @Override
   public ChannelHandlerContext fireChannelRegistered() {
      invokeChannelRegistered(this.findContextInbound(2));
      return this;
   }

   static void invokeChannelRegistered(final AbstractChannelHandlerContext next) {
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeChannelRegistered();
      } else {
         executor.execute(new Runnable() {
            public void run() {
               next.invokeChannelRegistered();
            }
         });
      }

   }

   private void invokeChannelRegistered() {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).channelRegistered(this);
         } catch (Throwable var2) {
            this.invokeExceptionCaught(var2);
         }
      } else {
         this.fireChannelRegistered();
      }

   }

   @Override
   public ChannelHandlerContext fireChannelUnregistered() {
      invokeChannelUnregistered(this.findContextInbound(4));
      return this;
   }

   static void invokeChannelUnregistered(final AbstractChannelHandlerContext next) {
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeChannelUnregistered();
      } else {
         executor.execute(new Runnable() {
            public void run() {
               next.invokeChannelUnregistered();
            }
         });
      }

   }

   private void invokeChannelUnregistered() {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).channelUnregistered(this);
         } catch (Throwable var2) {
            this.invokeExceptionCaught(var2);
         }
      } else {
         this.fireChannelUnregistered();
      }

   }

   @Override
   public ChannelHandlerContext fireChannelActive() {
      invokeChannelActive(this.findContextInbound(8));
      return this;
   }

   static void invokeChannelActive(final AbstractChannelHandlerContext next) {
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeChannelActive();
      } else {
         executor.execute(new Runnable() {
            public void run() {
               next.invokeChannelActive();
            }
         });
      }

   }

   private void invokeChannelActive() {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).channelActive(this);
         } catch (Throwable var2) {
            this.invokeExceptionCaught(var2);
         }
      } else {
         this.fireChannelActive();
      }

   }

   @Override
   public ChannelHandlerContext fireChannelInactive() {
      invokeChannelInactive(this.findContextInbound(16));
      return this;
   }

   static void invokeChannelInactive(final AbstractChannelHandlerContext next) {
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeChannelInactive();
      } else {
         executor.execute(new Runnable() {
            public void run() {
               next.invokeChannelInactive();
            }
         });
      }

   }

   private void invokeChannelInactive() {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).channelInactive(this);
         } catch (Throwable var2) {
            this.invokeExceptionCaught(var2);
         }
      } else {
         this.fireChannelInactive();
      }

   }

   @Override
   public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
      invokeExceptionCaught(this.findContextInbound(1), cause);
      return this;
   }

   static void invokeExceptionCaught(final AbstractChannelHandlerContext next, final Throwable cause) {
      ObjectUtil.checkNotNull(cause, "cause");
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeExceptionCaught(cause);
      } else {
         try {
            executor.execute(new Runnable() {
               public void run() {
                  next.invokeExceptionCaught(cause);
               }
            });
         } catch (Throwable var4) {
            if (logger.isWarnEnabled()) {
               logger.warn("Failed to submit an exceptionCaught() event.", var4);
               logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
            }
         }
      }

   }

   private void invokeExceptionCaught(Throwable cause) {
      if (this.invokeHandler()) {
         try {
            this.handler().exceptionCaught(this, cause);
         } catch (Throwable var3) {
            if (logger.isDebugEnabled()) {
               logger.debug(
                  "An exception {}was thrown by a user handler's exceptionCaught() method while handling the following exception:",
                  ThrowableUtil.stackTraceToString(var3),
                  cause
               );
            } else if (logger.isWarnEnabled()) {
               logger.warn(
                  "An exception '{}' [enable DEBUG level for full stacktrace] was thrown by a user handler's exceptionCaught() method while handling the following exception:",
                  var3,
                  cause
               );
            }
         }
      } else {
         this.fireExceptionCaught(cause);
      }

   }

   @Override
   public ChannelHandlerContext fireUserEventTriggered(Object event) {
      invokeUserEventTriggered(this.findContextInbound(128), event);
      return this;
   }

   static void invokeUserEventTriggered(final AbstractChannelHandlerContext next, final Object event) {
      ObjectUtil.checkNotNull(event, "event");
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeUserEventTriggered(event);
      } else {
         executor.execute(new Runnable() {
            public void run() {
               next.invokeUserEventTriggered(event);
            }
         });
      }

   }

   private void invokeUserEventTriggered(Object event) {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).userEventTriggered(this, event);
         } catch (Throwable var3) {
            this.invokeExceptionCaught(var3);
         }
      } else {
         this.fireUserEventTriggered(event);
      }

   }

   @Override
   public ChannelHandlerContext fireChannelRead(Object msg) {
      invokeChannelRead(this.findContextInbound(32), msg);
      return this;
   }

   static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
      final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeChannelRead(m);
      } else {
         executor.execute(new Runnable() {
            public void run() {
               next.invokeChannelRead(m);
            }
         });
      }

   }

   private void invokeChannelRead(Object msg) {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).channelRead(this, msg);
         } catch (Throwable var3) {
            this.invokeExceptionCaught(var3);
         }
      } else {
         this.fireChannelRead(msg);
      }

   }

   @Override
   public ChannelHandlerContext fireChannelReadComplete() {
      invokeChannelReadComplete(this.findContextInbound(64));
      return this;
   }

   static void invokeChannelReadComplete(AbstractChannelHandlerContext next) {
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeChannelReadComplete();
      } else {
         AbstractChannelHandlerContext.Tasks tasks = next.invokeTasks;
         if (tasks == null) {
            next.invokeTasks = tasks = new AbstractChannelHandlerContext.Tasks(next);
         }

         executor.execute(tasks.invokeChannelReadCompleteTask);
      }

   }

   private void invokeChannelReadComplete() {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).channelReadComplete(this);
         } catch (Throwable var2) {
            this.invokeExceptionCaught(var2);
         }
      } else {
         this.fireChannelReadComplete();
      }

   }

   @Override
   public ChannelHandlerContext fireChannelWritabilityChanged() {
      invokeChannelWritabilityChanged(this.findContextInbound(256));
      return this;
   }

   static void invokeChannelWritabilityChanged(AbstractChannelHandlerContext next) {
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeChannelWritabilityChanged();
      } else {
         AbstractChannelHandlerContext.Tasks tasks = next.invokeTasks;
         if (tasks == null) {
            next.invokeTasks = tasks = new AbstractChannelHandlerContext.Tasks(next);
         }

         executor.execute(tasks.invokeChannelWritableStateChangedTask);
      }

   }

   private void invokeChannelWritabilityChanged() {
      if (this.invokeHandler()) {
         try {
            ((ChannelInboundHandler)this.handler()).channelWritabilityChanged(this);
         } catch (Throwable var2) {
            this.invokeExceptionCaught(var2);
         }
      } else {
         this.fireChannelWritabilityChanged();
      }

   }

   @Override
   public ChannelFuture bind(SocketAddress localAddress) {
      return this.bind(localAddress, this.newPromise());
   }

   @Override
   public ChannelFuture connect(SocketAddress remoteAddress) {
      return this.connect(remoteAddress, this.newPromise());
   }

   @Override
   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
      return this.connect(remoteAddress, localAddress, this.newPromise());
   }

   @Override
   public ChannelFuture disconnect() {
      return this.disconnect(this.newPromise());
   }

   @Override
   public ChannelFuture close() {
      return this.close(this.newPromise());
   }

   @Override
   public ChannelFuture deregister() {
      return this.deregister(this.newPromise());
   }

   @Override
   public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
      ObjectUtil.checkNotNull(localAddress, "localAddress");
      if (this.isNotValidPromise(promise, false)) {
         return promise;
      } else {
         final AbstractChannelHandlerContext next = this.findContextOutbound(512);
         EventExecutor executor = next.executor();
         if (executor.inEventLoop()) {
            next.invokeBind(localAddress, promise);
         } else {
            safeExecute(executor, new Runnable() {
               public void run() {
                  next.invokeBind(localAddress, promise);
               }
            }, promise, null, false);
         }

         return promise;
      }
   }

   private void invokeBind(SocketAddress localAddress, ChannelPromise promise) {
      if (this.invokeHandler()) {
         try {
            ((ChannelOutboundHandler)this.handler()).bind(this, localAddress, promise);
         } catch (Throwable var4) {
            notifyOutboundHandlerException(var4, promise);
         }
      } else {
         this.bind(localAddress, promise);
      }

   }

   @Override
   public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
      return this.connect(remoteAddress, null, promise);
   }

   @Override
   public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
      ObjectUtil.checkNotNull(remoteAddress, "remoteAddress");
      if (this.isNotValidPromise(promise, false)) {
         return promise;
      } else {
         final AbstractChannelHandlerContext next = this.findContextOutbound(1024);
         EventExecutor executor = next.executor();
         if (executor.inEventLoop()) {
            next.invokeConnect(remoteAddress, localAddress, promise);
         } else {
            safeExecute(executor, new Runnable() {
               public void run() {
                  next.invokeConnect(remoteAddress, localAddress, promise);
               }
            }, promise, null, false);
         }

         return promise;
      }
   }

   private void invokeConnect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      if (this.invokeHandler()) {
         try {
            ((ChannelOutboundHandler)this.handler()).connect(this, remoteAddress, localAddress, promise);
         } catch (Throwable var5) {
            notifyOutboundHandlerException(var5, promise);
         }
      } else {
         this.connect(remoteAddress, localAddress, promise);
      }

   }

   @Override
   public ChannelFuture disconnect(final ChannelPromise promise) {
      if (!this.channel().metadata().hasDisconnect()) {
         return this.close(promise);
      } else if (this.isNotValidPromise(promise, false)) {
         return promise;
      } else {
         final AbstractChannelHandlerContext next = this.findContextOutbound(2048);
         EventExecutor executor = next.executor();
         if (executor.inEventLoop()) {
            next.invokeDisconnect(promise);
         } else {
            safeExecute(executor, new Runnable() {
               public void run() {
                  next.invokeDisconnect(promise);
               }
            }, promise, null, false);
         }

         return promise;
      }
   }

   private void invokeDisconnect(ChannelPromise promise) {
      if (this.invokeHandler()) {
         try {
            ((ChannelOutboundHandler)this.handler()).disconnect(this, promise);
         } catch (Throwable var3) {
            notifyOutboundHandlerException(var3, promise);
         }
      } else {
         this.disconnect(promise);
      }

   }

   @Override
   public ChannelFuture close(final ChannelPromise promise) {
      if (this.isNotValidPromise(promise, false)) {
         return promise;
      } else {
         final AbstractChannelHandlerContext next = this.findContextOutbound(4096);
         EventExecutor executor = next.executor();
         if (executor.inEventLoop()) {
            next.invokeClose(promise);
         } else {
            safeExecute(executor, new Runnable() {
               public void run() {
                  next.invokeClose(promise);
               }
            }, promise, null, false);
         }

         return promise;
      }
   }

   private void invokeClose(ChannelPromise promise) {
      if (this.invokeHandler()) {
         try {
            ((ChannelOutboundHandler)this.handler()).close(this, promise);
         } catch (Throwable var3) {
            notifyOutboundHandlerException(var3, promise);
         }
      } else {
         this.close(promise);
      }

   }

   @Override
   public ChannelFuture deregister(final ChannelPromise promise) {
      if (this.isNotValidPromise(promise, false)) {
         return promise;
      } else {
         final AbstractChannelHandlerContext next = this.findContextOutbound(8192);
         EventExecutor executor = next.executor();
         if (executor.inEventLoop()) {
            next.invokeDeregister(promise);
         } else {
            safeExecute(executor, new Runnable() {
               public void run() {
                  next.invokeDeregister(promise);
               }
            }, promise, null, false);
         }

         return promise;
      }
   }

   private void invokeDeregister(ChannelPromise promise) {
      if (this.invokeHandler()) {
         try {
            ((ChannelOutboundHandler)this.handler()).deregister(this, promise);
         } catch (Throwable var3) {
            notifyOutboundHandlerException(var3, promise);
         }
      } else {
         this.deregister(promise);
      }

   }

   @Override
   public ChannelHandlerContext read() {
      AbstractChannelHandlerContext next = this.findContextOutbound(16384);
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeRead();
      } else {
         AbstractChannelHandlerContext.Tasks tasks = next.invokeTasks;
         if (tasks == null) {
            next.invokeTasks = tasks = new AbstractChannelHandlerContext.Tasks(next);
         }

         executor.execute(tasks.invokeReadTask);
      }

      return this;
   }

   private void invokeRead() {
      if (this.invokeHandler()) {
         try {
            ((ChannelOutboundHandler)this.handler()).read(this);
         } catch (Throwable var2) {
            this.invokeExceptionCaught(var2);
         }
      } else {
         this.read();
      }

   }

   @Override
   public ChannelFuture write(Object msg) {
      return this.write(msg, this.newPromise());
   }

   @Override
   public ChannelFuture write(Object msg, ChannelPromise promise) {
      this.write(msg, false, promise);
      return promise;
   }

   void invokeWrite(Object msg, ChannelPromise promise) {
      if (this.invokeHandler()) {
         this.invokeWrite0(msg, promise);
      } else {
         this.write(msg, promise);
      }

   }

   private void invokeWrite0(Object msg, ChannelPromise promise) {
      try {
         ((ChannelOutboundHandler)this.handler()).write(this, msg, promise);
      } catch (Throwable var4) {
         notifyOutboundHandlerException(var4, promise);
      }

   }

   @Override
   public ChannelHandlerContext flush() {
      AbstractChannelHandlerContext next = this.findContextOutbound(65536);
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         next.invokeFlush();
      } else {
         AbstractChannelHandlerContext.Tasks tasks = next.invokeTasks;
         if (tasks == null) {
            next.invokeTasks = tasks = new AbstractChannelHandlerContext.Tasks(next);
         }

         safeExecute(executor, tasks.invokeFlushTask, this.channel().voidPromise(), null, false);
      }

      return this;
   }

   private void invokeFlush() {
      if (this.invokeHandler()) {
         this.invokeFlush0();
      } else {
         this.flush();
      }

   }

   private void invokeFlush0() {
      try {
         ((ChannelOutboundHandler)this.handler()).flush(this);
      } catch (Throwable var2) {
         this.invokeExceptionCaught(var2);
      }

   }

   @Override
   public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
      this.write(msg, true, promise);
      return promise;
   }

   void invokeWriteAndFlush(Object msg, ChannelPromise promise) {
      if (this.invokeHandler()) {
         this.invokeWrite0(msg, promise);
         this.invokeFlush0();
      } else {
         this.writeAndFlush(msg, promise);
      }

   }

   private void write(Object msg, boolean flush, ChannelPromise promise) {
      ObjectUtil.checkNotNull(msg, "msg");

      try {
         if (this.isNotValidPromise(promise, true)) {
            ReferenceCountUtil.release(msg);
            return;
         }
      } catch (RuntimeException var8) {
         ReferenceCountUtil.release(msg);
         throw var8;
      }

      AbstractChannelHandlerContext next = this.findContextOutbound(flush ? 98304 : '???');
      Object m = this.pipeline.touch(msg, next);
      EventExecutor executor = next.executor();
      if (executor.inEventLoop()) {
         if (flush) {
            next.invokeWriteAndFlush(m, promise);
         } else {
            next.invokeWrite(m, promise);
         }
      } else {
         AbstractChannelHandlerContext.WriteTask task = AbstractChannelHandlerContext.WriteTask.newInstance(next, m, promise, flush);
         if (!safeExecute(executor, task, promise, m, !flush)) {
            task.cancel();
         }
      }

   }

   @Override
   public ChannelFuture writeAndFlush(Object msg) {
      return this.writeAndFlush(msg, this.newPromise());
   }

   private static void notifyOutboundHandlerException(Throwable cause, ChannelPromise promise) {
      PromiseNotificationUtil.tryFailure(promise, cause, promise instanceof VoidChannelPromise ? null : logger);
   }

   @Override
   public ChannelPromise newPromise() {
      return new DefaultChannelPromise(this.channel(), this.executor());
   }

   @Override
   public ChannelProgressivePromise newProgressivePromise() {
      return new DefaultChannelProgressivePromise(this.channel(), this.executor());
   }

   @Override
   public ChannelFuture newSucceededFuture() {
      ChannelFuture succeededFuture = this.succeededFuture;
      if (succeededFuture == null) {
         this.succeededFuture = succeededFuture = new SucceededChannelFuture(this.channel(), this.executor());
      }

      return succeededFuture;
   }

   @Override
   public ChannelFuture newFailedFuture(Throwable cause) {
      return new FailedChannelFuture(this.channel(), this.executor(), cause);
   }

   private boolean isNotValidPromise(ChannelPromise promise, boolean allowVoidPromise) {
      ObjectUtil.checkNotNull(promise, "promise");
      if (promise.isDone()) {
         if (promise.isCancelled()) {
            return true;
         } else {
            throw new IllegalArgumentException("promise already done: " + promise);
         }
      } else if (promise.channel() != this.channel()) {
         throw new IllegalArgumentException(String.format("promise.channel does not match: %s (expected: %s)", promise.channel(), this.channel()));
      } else if (promise.getClass() == DefaultChannelPromise.class) {
         return false;
      } else if (!allowVoidPromise && promise instanceof VoidChannelPromise) {
         throw new IllegalArgumentException(StringUtil.simpleClassName(VoidChannelPromise.class) + " not allowed for this operation");
      } else if (promise instanceof AbstractChannel.CloseFuture) {
         throw new IllegalArgumentException(StringUtil.simpleClassName(AbstractChannel.CloseFuture.class) + " not allowed in a pipeline");
      } else {
         return false;
      }
   }

   private AbstractChannelHandlerContext findContextInbound(int mask) {
      AbstractChannelHandlerContext ctx = this;
      EventExecutor currentExecutor = this.executor();

      do {
         ctx = ctx.next;
      } while(skipContext(ctx, currentExecutor, mask, 510));

      return ctx;
   }

   private AbstractChannelHandlerContext findContextOutbound(int mask) {
      AbstractChannelHandlerContext ctx = this;
      EventExecutor currentExecutor = this.executor();

      do {
         ctx = ctx.prev;
      } while(skipContext(ctx, currentExecutor, mask, 130560));

      return ctx;
   }

   private static boolean skipContext(AbstractChannelHandlerContext ctx, EventExecutor currentExecutor, int mask, int onlyMask) {
      return (ctx.executionMask & (onlyMask | mask)) == 0 || ctx.executor() == currentExecutor && (ctx.executionMask & mask) == 0;
   }

   @Override
   public ChannelPromise voidPromise() {
      return this.channel().voidPromise();
   }

   final void setRemoved() {
      this.handlerState = 3;
   }

   final boolean setAddComplete() {
      int oldState;
      do {
         oldState = this.handlerState;
         if (oldState == 3) {
            return false;
         }
      } while(!HANDLER_STATE_UPDATER.compareAndSet(this, oldState, 2));

      return true;
   }

   final void setAddPending() {
      boolean updated = HANDLER_STATE_UPDATER.compareAndSet(this, 0, 1);

      assert updated;

   }

   final void callHandlerAdded() throws Exception {
      if (this.setAddComplete()) {
         this.handler().handlerAdded(this);
      }

   }

   final void callHandlerRemoved() throws Exception {
      try {
         if (this.handlerState == 2) {
            this.handler().handlerRemoved(this);
         }
      } finally {
         this.setRemoved();
      }

   }

   private boolean invokeHandler() {
      int handlerState = this.handlerState;
      return handlerState == 2 || !this.ordered && handlerState == 1;
   }

   @Override
   public boolean isRemoved() {
      return this.handlerState == 3;
   }

   @Override
   public <T> Attribute<T> attr(AttributeKey<T> key) {
      return this.channel().attr(key);
   }

   @Override
   public <T> boolean hasAttr(AttributeKey<T> key) {
      return this.channel().hasAttr(key);
   }

   private static boolean safeExecute(EventExecutor executor, Runnable runnable, ChannelPromise promise, Object msg, boolean lazy) {
      try {
         if (lazy && executor instanceof AbstractEventExecutor) {
            ((AbstractEventExecutor)executor).lazyExecute(runnable);
         } else {
            executor.execute(runnable);
         }

         return true;
      } catch (Throwable var10) {
         try {
            if (msg != null) {
               ReferenceCountUtil.release(msg);
            }
         } finally {
            promise.setFailure(var10);
         }

         return false;
      }
   }

   @Override
   public String toHintString() {
      return '\'' + this.name + "' will handle the message from this point.";
   }

   public String toString() {
      return StringUtil.simpleClassName(ChannelHandlerContext.class) + '(' + this.name + ", " + this.channel() + ')';
   }

   private static final class Tasks {
      private final AbstractChannelHandlerContext next;
      private final Runnable invokeChannelReadCompleteTask = new Runnable() {
         public void run() {
            Tasks.this.next.invokeChannelReadComplete();
         }
      };
      private final Runnable invokeReadTask = new Runnable() {
         public void run() {
            Tasks.this.next.invokeRead();
         }
      };
      private final Runnable invokeChannelWritableStateChangedTask = new Runnable() {
         public void run() {
            Tasks.this.next.invokeChannelWritabilityChanged();
         }
      };
      private final Runnable invokeFlushTask = new Runnable() {
         public void run() {
            Tasks.this.next.invokeFlush();
         }
      };

      Tasks(AbstractChannelHandlerContext next) {
         this.next = next;
      }
   }

   static final class WriteTask implements Runnable {
      private static final ObjectPool<AbstractChannelHandlerContext.WriteTask> RECYCLER = ObjectPool.newPool(
         new ObjectPool.ObjectCreator<AbstractChannelHandlerContext.WriteTask>() {
            public AbstractChannelHandlerContext.WriteTask newObject(ObjectPool.Handle<AbstractChannelHandlerContext.WriteTask> handle) {
               return new AbstractChannelHandlerContext.WriteTask(handle);
            }
         }
      );
      private static final boolean ESTIMATE_TASK_SIZE_ON_SUBMIT = SystemPropertyUtil.getBoolean("io.netty.transport.estimateSizeOnSubmit", true);
      private static final int WRITE_TASK_OVERHEAD = SystemPropertyUtil.getInt("io.netty.transport.writeTaskSizeOverhead", 32);
      private final ObjectPool.Handle<AbstractChannelHandlerContext.WriteTask> handle;
      private AbstractChannelHandlerContext ctx;
      private Object msg;
      private ChannelPromise promise;
      private int size;

      static AbstractChannelHandlerContext.WriteTask newInstance(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise, boolean flush) {
         AbstractChannelHandlerContext.WriteTask task = RECYCLER.get();
         init(task, ctx, msg, promise, flush);
         return task;
      }

      private WriteTask(ObjectPool.Handle<? extends AbstractChannelHandlerContext.WriteTask> handle) {
         this.handle = handle;
      }

      protected static void init(
         AbstractChannelHandlerContext.WriteTask task, AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise, boolean flush
      ) {
         task.ctx = ctx;
         task.msg = msg;
         task.promise = promise;
         if (ESTIMATE_TASK_SIZE_ON_SUBMIT) {
            task.size = ctx.pipeline.estimatorHandle().size(msg) + WRITE_TASK_OVERHEAD;
            ctx.pipeline.incrementPendingOutboundBytes((long)task.size);
         } else {
            task.size = 0;
         }

         if (flush) {
            task.size |= Integer.MIN_VALUE;
         }

      }

      public void run() {
         try {
            this.decrementPendingOutboundBytes();
            if (this.size >= 0) {
               this.ctx.invokeWrite(this.msg, this.promise);
            } else {
               this.ctx.invokeWriteAndFlush(this.msg, this.promise);
            }
         } finally {
            this.recycle();
         }

      }

      void cancel() {
         try {
            this.decrementPendingOutboundBytes();
         } finally {
            this.recycle();
         }

      }

      private void decrementPendingOutboundBytes() {
         if (ESTIMATE_TASK_SIZE_ON_SUBMIT) {
            this.ctx.pipeline.decrementPendingOutboundBytes((long)(this.size & 2147483647));
         }

      }

      private void recycle() {
         this.ctx = null;
         this.msg = null;
         this.promise = null;
         this.handle.recycle(this);
      }
   }
}
