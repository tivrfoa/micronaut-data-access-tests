package io.micronaut.asm.commons;

import io.micronaut.asm.ConstantDynamic;
import io.micronaut.asm.Handle;
import io.micronaut.asm.Type;
import io.micronaut.asm.signature.SignatureReader;
import io.micronaut.asm.signature.SignatureVisitor;
import io.micronaut.asm.signature.SignatureWriter;

public abstract class Remapper {
   public String mapDesc(String descriptor) {
      return this.mapType(Type.getType(descriptor)).getDescriptor();
   }

   private Type mapType(Type type) {
      switch(type.getSort()) {
         case 9:
            StringBuilder remappedDescriptor = new StringBuilder();

            for(int i = 0; i < type.getDimensions(); ++i) {
               remappedDescriptor.append('[');
            }

            remappedDescriptor.append(this.mapType(type.getElementType()).getDescriptor());
            return Type.getType(remappedDescriptor.toString());
         case 10:
            String remappedInternalName = this.map(type.getInternalName());
            return remappedInternalName != null ? Type.getObjectType(remappedInternalName) : type;
         case 11:
            return Type.getMethodType(this.mapMethodDesc(type.getDescriptor()));
         default:
            return type;
      }
   }

   public String mapType(String internalName) {
      return internalName == null ? null : this.mapType(Type.getObjectType(internalName)).getInternalName();
   }

   public String[] mapTypes(String[] internalNames) {
      String[] remappedInternalNames = null;

      for(int i = 0; i < internalNames.length; ++i) {
         String internalName = internalNames[i];
         String remappedInternalName = this.mapType(internalName);
         if (remappedInternalName != null) {
            if (remappedInternalNames == null) {
               remappedInternalNames = (String[])internalNames.clone();
            }

            remappedInternalNames[i] = remappedInternalName;
         }
      }

      return remappedInternalNames != null ? remappedInternalNames : internalNames;
   }

   public String mapMethodDesc(String methodDescriptor) {
      if ("()V".equals(methodDescriptor)) {
         return methodDescriptor;
      } else {
         StringBuilder stringBuilder = new StringBuilder("(");

         for(Type argumentType : Type.getArgumentTypes(methodDescriptor)) {
            stringBuilder.append(this.mapType(argumentType).getDescriptor());
         }

         Type returnType = Type.getReturnType(methodDescriptor);
         if (returnType == Type.VOID_TYPE) {
            stringBuilder.append(")V");
         } else {
            stringBuilder.append(')').append(this.mapType(returnType).getDescriptor());
         }

         return stringBuilder.toString();
      }
   }

   public Object mapValue(Object value) {
      if (value instanceof Type) {
         return this.mapType((Type)value);
      } else if (value instanceof Handle) {
         Handle handle = (Handle)value;
         return new Handle(
            handle.getTag(),
            this.mapType(handle.getOwner()),
            this.mapMethodName(handle.getOwner(), handle.getName(), handle.getDesc()),
            handle.getTag() <= 4 ? this.mapDesc(handle.getDesc()) : this.mapMethodDesc(handle.getDesc()),
            handle.isInterface()
         );
      } else if (!(value instanceof ConstantDynamic)) {
         return value;
      } else {
         ConstantDynamic constantDynamic = (ConstantDynamic)value;
         int bootstrapMethodArgumentCount = constantDynamic.getBootstrapMethodArgumentCount();
         Object[] remappedBootstrapMethodArguments = new Object[bootstrapMethodArgumentCount];

         for(int i = 0; i < bootstrapMethodArgumentCount; ++i) {
            remappedBootstrapMethodArguments[i] = this.mapValue(constantDynamic.getBootstrapMethodArgument(i));
         }

         String descriptor = constantDynamic.getDescriptor();
         return new ConstantDynamic(
            this.mapInvokeDynamicMethodName(constantDynamic.getName(), descriptor),
            this.mapDesc(descriptor),
            (Handle)this.mapValue(constantDynamic.getBootstrapMethod()),
            remappedBootstrapMethodArguments
         );
      }
   }

   public String mapSignature(String signature, boolean typeSignature) {
      if (signature == null) {
         return null;
      } else {
         SignatureReader signatureReader = new SignatureReader(signature);
         SignatureWriter signatureWriter = new SignatureWriter();
         SignatureVisitor signatureRemapper = this.createSignatureRemapper(signatureWriter);
         if (typeSignature) {
            signatureReader.acceptType(signatureRemapper);
         } else {
            signatureReader.accept(signatureRemapper);
         }

         return signatureWriter.toString();
      }
   }

   @Deprecated
   protected SignatureVisitor createRemappingSignatureAdapter(SignatureVisitor signatureVisitor) {
      return this.createSignatureRemapper(signatureVisitor);
   }

   protected SignatureVisitor createSignatureRemapper(SignatureVisitor signatureVisitor) {
      return new SignatureRemapper(signatureVisitor, this);
   }

   public String mapAnnotationAttributeName(String descriptor, String name) {
      return name;
   }

   public String mapInnerClassName(String name, String ownerName, String innerName) {
      String remappedInnerName = this.mapType(name);
      if (!remappedInnerName.contains("$")) {
         return innerName;
      } else {
         int index = remappedInnerName.lastIndexOf(36) + 1;

         while(index < remappedInnerName.length() && Character.isDigit(remappedInnerName.charAt(index))) {
            ++index;
         }

         return remappedInnerName.substring(index);
      }
   }

   public String mapMethodName(String owner, String name, String descriptor) {
      return name;
   }

   public String mapInvokeDynamicMethodName(String name, String descriptor) {
      return name;
   }

   public String mapRecordComponentName(String owner, String name, String descriptor) {
      return name;
   }

   public String mapFieldName(String owner, String name, String descriptor) {
      return name;
   }

   public String mapPackageName(String name) {
      return name;
   }

   public String mapModuleName(String name) {
      return name;
   }

   public String map(String internalName) {
      return internalName;
   }
}
