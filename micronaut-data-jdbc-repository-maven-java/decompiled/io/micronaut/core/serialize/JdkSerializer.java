package io.micronaut.core.serialize;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.Optional;

public class JdkSerializer implements ObjectSerializer {
   private final ConversionService<?> conversionService;

   public JdkSerializer(ConversionService<?> conversionService) {
      this.conversionService = conversionService;
   }

   public JdkSerializer() {
      this(ConversionService.SHARED);
   }

   @Override
   public void serialize(Object object, OutputStream outputStream) throws SerializationException {
      try {
         ObjectOutputStream objectOut = this.createObjectOutput(outputStream);
         Throwable var4 = null;

         try {
            objectOut.writeObject(object);
            objectOut.flush();
         } catch (Throwable var14) {
            var4 = var14;
            throw var14;
         } finally {
            if (objectOut != null) {
               if (var4 != null) {
                  try {
                     objectOut.close();
                  } catch (Throwable var13) {
                     var4.addSuppressed(var13);
                  }
               } else {
                  objectOut.close();
               }
            }

         }

      } catch (IOException var16) {
         throw new SerializationException("I/O error occurred during serialization: " + var16.getMessage(), var16);
      }
   }

   @Override
   public <T> Optional<T> deserialize(InputStream inputStream, Class<T> requiredType) throws SerializationException {
      try {
         ObjectInputStream objectIn = this.createObjectInput(inputStream, requiredType);
         Throwable var4 = null;

         Optional var6;
         try {
            try {
               Object readObject = objectIn.readObject();
               var6 = this.conversionService.convert(readObject, requiredType);
            } catch (ClassCastException var18) {
               throw new SerializationException("Invalid type deserialized from stream: " + var18.getMessage(), var18);
            } catch (ClassNotFoundException var19) {
               throw new SerializationException("Type not found deserializing from stream: " + var19.getMessage(), var19);
            }
         } catch (Throwable var20) {
            var4 = var20;
            throw var20;
         } finally {
            if (objectIn != null) {
               if (var4 != null) {
                  try {
                     objectIn.close();
                  } catch (Throwable var17) {
                     var4.addSuppressed(var17);
                  }
               } else {
                  objectIn.close();
               }
            }

         }

         return var6;
      } catch (IOException var22) {
         throw new SerializationException("I/O error occurred during deserialization: " + var22.getMessage(), var22);
      }
   }

   @Override
   public <T> Optional<T> deserialize(InputStream inputStream, Argument<T> requiredType) throws SerializationException {
      try {
         ObjectInputStream objectIn = this.createObjectInput(inputStream, requiredType.getType());
         Throwable var4 = null;

         Optional var6;
         try {
            try {
               Object readObject = objectIn.readObject();
               var6 = this.conversionService.convert(readObject, requiredType);
            } catch (ClassCastException var18) {
               throw new SerializationException("Invalid type deserialized from stream: " + var18.getMessage(), var18);
            } catch (ClassNotFoundException var19) {
               throw new SerializationException("Type not found deserializing from stream: " + var19.getMessage(), var19);
            }
         } catch (Throwable var20) {
            var4 = var20;
            throw var20;
         } finally {
            if (objectIn != null) {
               if (var4 != null) {
                  try {
                     objectIn.close();
                  } catch (Throwable var17) {
                     var4.addSuppressed(var17);
                  }
               } else {
                  objectIn.close();
               }
            }

         }

         return var6;
      } catch (IOException var22) {
         throw new SerializationException("I/O error occurred during deserialization: " + var22.getMessage(), var22);
      }
   }

   protected ObjectOutputStream createObjectOutput(OutputStream outputStream) throws IOException {
      return new ObjectOutputStream(outputStream);
   }

   protected ObjectInputStream createObjectInput(InputStream inputStream, Class<?> requiredType) throws IOException {
      return new ObjectInputStream(inputStream) {
         protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            Optional<Class> aClass = ClassUtils.forName(desc.getName(), requiredType.getClassLoader());
            return aClass.isPresent() ? (Class)aClass.get() : super.resolveClass(desc);
         }
      };
   }
}
