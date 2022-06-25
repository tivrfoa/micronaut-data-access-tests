package io.micronaut.http.server.netty.converters;

import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.CompletedPart;
import io.micronaut.http.netty.channel.converters.ChannelOptionFactory;
import io.micronaut.http.server.netty.multipart.NettyCompletedAttribute;
import io.micronaut.http.server.netty.multipart.NettyCompletedFileUpload;
import io.micronaut.http.server.netty.multipart.NettyPartData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
@Internal
public class NettyConverters implements TypeConverterRegistrar {
   private final ConversionService<?> conversionService;
   private final BeanProvider<MediaTypeCodecRegistry> decoderRegistryProvider;
   private final ChannelOptionFactory channelOptionFactory;

   public NettyConverters(
      ConversionService<?> conversionService, BeanProvider<MediaTypeCodecRegistry> decoderRegistryProvider, ChannelOptionFactory channelOptionFactory
   ) {
      this.conversionService = conversionService;
      this.decoderRegistryProvider = decoderRegistryProvider;
      this.channelOptionFactory = channelOptionFactory;
   }

   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(CharSequence.class, ChannelOption.class, (TypeConverter)((object, targetType, context) -> {
         String str = object.toString();
         String name = NameUtils.underscoreSeparate(str).toUpperCase(Locale.ENGLISH);
         return Optional.of(this.channelOptionFactory.channelOption(name));
      }));
      conversionService.addConverter(ByteBuf.class, CharSequence.class, this.byteBufCharSequenceTypeConverter());
      conversionService.addConverter(CompositeByteBuf.class, CharSequence.class, this.compositeByteBufCharSequenceTypeConverter());
      conversionService.addConverter(ByteBuf.class, byte[].class, this.byteBufToArrayTypeConverter());
      conversionService.addConverter(byte[].class, ByteBuf.class, this.byteArrayToByteBuffTypeConverter());
      conversionService.addConverter(ByteBuf.class, Object.class, this.byteBufToObjectConverter());
      conversionService.addConverter(FileUpload.class, CompletedFileUpload.class, this.fileUploadToCompletedFileUploadConverter());
      conversionService.addConverter(Attribute.class, CompletedPart.class, this.attributeToCompletedPartConverter());
      conversionService.addConverter(FileUpload.class, Object.class, this.fileUploadToObjectConverter());
      conversionService.addConverter(HttpData.class, byte[].class, this.httpDataToByteArrayConverter());
      conversionService.addConverter(HttpData.class, CharSequence.class, this.httpDataToStringConverter());
      conversionService.addConverter(NettyPartData.class, byte[].class, this.nettyPartDataToByteArrayConverter());
      conversionService.addConverter(NettyPartData.class, Object.class, this.nettyPartDataToObjectConverter());
      conversionService.addConverter(Attribute.class, Object.class, this.nettyAttributeToObjectConverter());
      conversionService.addConverter(String.class, ChannelOption.class, s -> this.channelOptionFactory.channelOption(NameUtils.environmentName(s)));
      conversionService.addConverter(Map.class, WriteBufferWaterMark.class, (TypeConverter)((map, targetType, context) -> {
         Object h = map.get("high");
         Object l = map.get("low");
         if (h != null && l != null) {
            try {
               int high = Integer.parseInt(h.toString());
               int low = Integer.parseInt(l.toString());
               return Optional.of(new WriteBufferWaterMark(low, high));
            } catch (NumberFormatException var7) {
               context.reject(var7);
               return Optional.empty();
            }
         } else {
            return Optional.empty();
         }
      }));
   }

   private TypeConverter<Attribute, Object> nettyAttributeToObjectConverter() {
      return (object, targetType, context) -> {
         try {
            String value = object.getValue();
            return targetType.isInstance(value) ? Optional.of(value) : this.conversionService.convert(value, targetType, context);
         } catch (IOException var5) {
            context.reject(var5);
            return Optional.empty();
         }
      };
   }

   private TypeConverter<NettyPartData, byte[]> nettyPartDataToByteArrayConverter() {
      return (upload, targetType, context) -> {
         try {
            return Optional.of(upload.getBytes());
         } catch (IOException var4) {
            context.reject(var4);
            return Optional.empty();
         }
      };
   }

   private TypeConverter<NettyPartData, Object> nettyPartDataToObjectConverter() {
      return (object, targetType, context) -> {
         try {
            if (targetType.isAssignableFrom(ByteBuffer.class)) {
               return Optional.of(object.getByteBuffer());
            } else if (targetType.isAssignableFrom(InputStream.class)) {
               return Optional.of(object.getInputStream());
            } else {
               ByteBuf byteBuf = object.getByteBuf();

               Optional var5;
               try {
                  var5 = this.conversionService.convert(byteBuf, targetType, context);
               } finally {
                  byteBuf.release();
               }

               return var5;
            }
         } catch (IOException var10) {
            context.reject(var10);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<HttpData, CharSequence> httpDataToStringConverter() {
      return (upload, targetType, context) -> {
         try {
            if (!upload.isCompleted()) {
               return Optional.empty();
            } else {
               ByteBuf byteBuf = upload.getByteBuf();
               return this.conversionService.convert(byteBuf, targetType, context);
            }
         } catch (Exception var5) {
            context.reject(var5);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<HttpData, byte[]> httpDataToByteArrayConverter() {
      return (upload, targetType, context) -> {
         try {
            if (!upload.isCompleted()) {
               return Optional.empty();
            } else {
               ByteBuf byteBuf = upload.getByteBuf();
               return this.conversionService.convert(byteBuf, targetType, context);
            }
         } catch (Exception var5) {
            context.reject(var5);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<FileUpload, CompletedFileUpload> fileUploadToCompletedFileUploadConverter() {
      return (object, targetType, context) -> {
         try {
            return !object.isCompleted() ? Optional.empty() : Optional.of(new NettyCompletedFileUpload(object));
         } catch (Exception var4) {
            context.reject(var4);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<Attribute, CompletedPart> attributeToCompletedPartConverter() {
      return (object, targetType, context) -> {
         try {
            return !object.isCompleted() ? Optional.empty() : Optional.of(new NettyCompletedAttribute(object));
         } catch (Exception var4) {
            context.reject(var4);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<FileUpload, Object> fileUploadToObjectConverter() {
      return (object, targetType, context) -> {
         try {
            if (!object.isCompleted()) {
               return Optional.empty();
            } else {
               String contentType = object.getContentType();
               ByteBuf byteBuf = object.getByteBuf();
               if (StringUtils.isNotEmpty(contentType)) {
                  MediaType mediaType = MediaType.of(contentType);
                  Optional<MediaTypeCodec> registered = this.decoderRegistryProvider.get().findCodec(mediaType);
                  if (registered.isPresent()) {
                     MediaTypeCodec decoder = (MediaTypeCodec)registered.get();
                     Object val = decoder.decode(targetType, (InputStream)(new ByteBufInputStream(byteBuf)));
                     return Optional.of(val);
                  } else {
                     return this.conversionService.convert(byteBuf, targetType, context);
                  }
               } else {
                  return this.conversionService.convert(byteBuf, targetType, context);
               }
            }
         } catch (Exception var10) {
            context.reject(var10);
            return Optional.empty();
         }
      };
   }

   protected TypeConverter<ByteBuf, Object> byteBufToObjectConverter() {
      return (object, targetType, context) -> this.conversionService.convert(object.toString(context.getCharset()), targetType, context);
   }

   protected TypeConverter<ByteBuf, CharSequence> byteBufCharSequenceTypeConverter() {
      return (object, targetType, context) -> Optional.of(object.toString(context.getCharset()));
   }

   protected TypeConverter<CompositeByteBuf, CharSequence> compositeByteBufCharSequenceTypeConverter() {
      return (object, targetType, context) -> Optional.of(object.toString(context.getCharset()));
   }

   protected TypeConverter<ByteBuf, byte[]> byteBufToArrayTypeConverter() {
      return (object, targetType, context) -> Optional.of(ByteBufUtil.getBytes(object));
   }

   protected TypeConverter<byte[], ByteBuf> byteArrayToByteBuffTypeConverter() {
      return (object, targetType, context) -> Optional.of(Unpooled.wrappedBuffer(object));
   }
}
