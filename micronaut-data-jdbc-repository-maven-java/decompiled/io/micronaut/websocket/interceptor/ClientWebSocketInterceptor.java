package io.micronaut.websocket.interceptor;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.exceptions.WebSocketClientException;
import java.io.Closeable;

@Prototype
public class ClientWebSocketInterceptor implements MethodInterceptor<Object, Object> {
   private WebSocketSession webSocketSession;

   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      Class<?> declaringType = context.getDeclaringType();
      if (declaringType == WebSocketSessionAware.class) {
         Object[] values = context.getParameterValues();
         if (ArrayUtils.isNotEmpty(values)) {
            Object o = values[0];
            if (o instanceof WebSocketSession) {
               this.webSocketSession = (WebSocketSession)o;
               return null;
            }
         }
      }

      if (declaringType != Closeable.class && declaringType != AutoCloseable.class) {
         String methodName = context.getMethodName();
         if (!methodName.startsWith("send") && !methodName.startsWith("broadcast")) {
            return context.proceed();
         } else {
            MediaType mediaType = (MediaType)context.stringValue(Produces.class).map(MediaType::of).orElse(MediaType.APPLICATION_JSON_TYPE);
            this.validateSession();
            InterceptedMethod interceptedMethod = InterceptedMethod.of(context);
            Class<?> javaReturnType = context.getReturnType().getType();
            if (interceptedMethod.resultType() == InterceptedMethod.ResultType.SYNCHRONOUS && javaReturnType != Void.TYPE) {
               return context.proceed();
            } else {
               try {
                  Object[] parameterValues = context.getParameterValues();
                  switch(parameterValues.length) {
                     case 0:
                        throw new IllegalArgumentException("At least 1 parameter is required to a send method");
                     case 1:
                        Object value = parameterValues[0];
                        if (value == null) {
                           throw new IllegalArgumentException("Parameter cannot be null");
                        }

                        return this.send(interceptedMethod, value, mediaType);
                     default:
                        return this.send(interceptedMethod, context.getParameterValueMap(), mediaType);
                  }
               } catch (Exception var9) {
                  return interceptedMethod.handleException(var9);
               }
            }
         }
      } else {
         if (this.webSocketSession != null) {
            this.webSocketSession.close();
         }

         return null;
      }
   }

   private Object send(InterceptedMethod interceptedMethod, Object message, MediaType mediaType) {
      switch(interceptedMethod.resultType()) {
         case COMPLETION_STAGE:
            return interceptedMethod.handleResult(this.webSocketSession.<Object>sendAsync(message, mediaType));
         case PUBLISHER:
            return interceptedMethod.handleResult(this.webSocketSession.send(message, mediaType));
         case SYNCHRONOUS:
            this.webSocketSession.sendSync(message, mediaType);
            return null;
         default:
            return interceptedMethod.unsupported();
      }
   }

   private void validateSession() {
      if (this.webSocketSession == null || !this.webSocketSession.isOpen()) {
         throw new WebSocketClientException("No available and open WebSocket session");
      }
   }
}
