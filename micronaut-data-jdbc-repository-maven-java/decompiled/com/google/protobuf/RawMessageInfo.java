package com.google.protobuf;

@CheckReturnValue
final class RawMessageInfo implements MessageInfo {
   private final MessageLite defaultInstance;
   private final String info;
   private final Object[] objects;
   private final int flags;

   RawMessageInfo(MessageLite defaultInstance, String info, Object[] objects) {
      this.defaultInstance = defaultInstance;
      this.info = info;
      this.objects = objects;
      int position = 0;
      int value = info.charAt(position++);
      if (value < 55296) {
         this.flags = value;
      } else {
         int result = value & 8191;

         int shift;
         for(shift = 13; (value = info.charAt(position++)) >= 55296; shift += 13) {
            result |= (value & 8191) << shift;
         }

         this.flags = result | value << shift;
      }

   }

   String getStringInfo() {
      return this.info;
   }

   Object[] getObjects() {
      return this.objects;
   }

   @Override
   public MessageLite getDefaultInstance() {
      return this.defaultInstance;
   }

   @Override
   public ProtoSyntax getSyntax() {
      return (this.flags & 1) == 1 ? ProtoSyntax.PROTO2 : ProtoSyntax.PROTO3;
   }

   @Override
   public boolean isMessageSetWireFormat() {
      return (this.flags & 2) == 2;
   }
}
