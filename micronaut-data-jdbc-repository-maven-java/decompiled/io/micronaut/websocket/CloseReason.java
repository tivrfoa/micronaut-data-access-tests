package io.micronaut.websocket;

import java.util.Objects;

public class CloseReason {
   public static final CloseReason NORMAL = new CloseReason(1000, "Normal Closure");
   public static final CloseReason GOING_AWAY = new CloseReason(1001, "Going Away");
   public static final CloseReason PROTOCOL_ERROR = new CloseReason(1002, "Protocol Error");
   public static final CloseReason UNSUPPORTED_DATA = new CloseReason(1003, "Unsupported Data");
   public static final CloseReason NO_STATUS_RECEIVED = new CloseReason(1005, "No Status Recvd");
   public static final CloseReason ABNORMAL_CLOSURE = new CloseReason(1006, "Abnormal Closure");
   public static final CloseReason INVALID_FRAME_PAYLOAD_DATA = new CloseReason(1007, "Invalid frame payload data");
   public static final CloseReason POLICY_VIOLATION = new CloseReason(1008, "Policy Violation");
   public static final CloseReason MESSAGE_TO_BIG = new CloseReason(1009, "Message Too Big");
   public static final CloseReason MISSING_EXTENSION = new CloseReason(1010, "Missing Extension");
   public static final CloseReason INTERNAL_ERROR = new CloseReason(1011, "Internal Error");
   public static final CloseReason SERVICE_RESTART = new CloseReason(1012, "Service Restart");
   public static final CloseReason TRY_AGAIN_LATER = new CloseReason(1013, "Try Again Later");
   public static final CloseReason BAD_GATEWAY = new CloseReason(1014, "Bad Gateway");
   public static final CloseReason TLS_HANDSHAKE = new CloseReason(1015, "TLS Handshake");
   private final int code;
   private String reason;

   public CloseReason(int code, String reason) {
      this.code = code;
      this.reason = reason;
   }

   public int getCode() {
      return this.code;
   }

   public String getReason() {
      return this.reason;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CloseReason that = (CloseReason)o;
         return this.code == that.code && Objects.equals(this.reason, that.reason);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.code, this.reason});
   }

   public String toString() {
      return "CloseReason{code=" + this.code + ", reason='" + this.reason + '\'' + '}';
   }
}
