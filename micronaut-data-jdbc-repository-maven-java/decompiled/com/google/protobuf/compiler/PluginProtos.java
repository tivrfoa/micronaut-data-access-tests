package com.google.protobuf.compiler;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.LazyStringArrayList;
import com.google.protobuf.LazyStringList;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.RepeatedFieldBuilderV3;
import com.google.protobuf.SingleFieldBuilderV3;
import com.google.protobuf.UninitializedMessageException;
import com.google.protobuf.UnknownFieldSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PluginProtos {
   private static final Descriptors.Descriptor internal_static_google_protobuf_compiler_Version_descriptor = (Descriptors.Descriptor)getDescriptor()
      .getMessageTypes()
      .get(0);
   private static final GeneratedMessageV3.FieldAccessorTable internal_static_google_protobuf_compiler_Version_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(
      internal_static_google_protobuf_compiler_Version_descriptor, new String[]{"Major", "Minor", "Patch", "Suffix"}
   );
   private static final Descriptors.Descriptor internal_static_google_protobuf_compiler_CodeGeneratorRequest_descriptor = (Descriptors.Descriptor)getDescriptor(
         
      )
      .getMessageTypes()
      .get(1);
   private static final GeneratedMessageV3.FieldAccessorTable internal_static_google_protobuf_compiler_CodeGeneratorRequest_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(
      internal_static_google_protobuf_compiler_CodeGeneratorRequest_descriptor, new String[]{"FileToGenerate", "Parameter", "ProtoFile", "CompilerVersion"}
   );
   private static final Descriptors.Descriptor internal_static_google_protobuf_compiler_CodeGeneratorResponse_descriptor = (Descriptors.Descriptor)getDescriptor(
         
      )
      .getMessageTypes()
      .get(2);
   private static final GeneratedMessageV3.FieldAccessorTable internal_static_google_protobuf_compiler_CodeGeneratorResponse_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(
      internal_static_google_protobuf_compiler_CodeGeneratorResponse_descriptor, new String[]{"Error", "SupportedFeatures", "File"}
   );
   private static final Descriptors.Descriptor internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_descriptor = (Descriptors.Descriptor)internal_static_google_protobuf_compiler_CodeGeneratorResponse_descriptor.getNestedTypes(
         
      )
      .get(0);
   private static final GeneratedMessageV3.FieldAccessorTable internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(
      internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_descriptor, new String[]{"Name", "InsertionPoint", "Content", "GeneratedCodeInfo"}
   );
   private static Descriptors.FileDescriptor descriptor;

   private PluginProtos() {
   }

   public static void registerAllExtensions(ExtensionRegistryLite registry) {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
      registerAllExtensions((ExtensionRegistryLite)registry);
   }

   public static Descriptors.FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n%google/protobuf/compiler/plugin.proto\u0012\u0018google.protobuf.compiler\u001a google/protobuf/descriptor.proto\"F\n\u0007Version\u0012\r\n\u0005major\u0018\u0001 \u0001(\u0005\u0012\r\n\u0005minor\u0018\u0002 \u0001(\u0005\u0012\r\n\u0005patch\u0018\u0003 \u0001(\u0005\u0012\u000e\n\u0006suffix\u0018\u0004 \u0001(\t\"º\u0001\n\u0014CodeGeneratorRequest\u0012\u0018\n\u0010file_to_generate\u0018\u0001 \u0003(\t\u0012\u0011\n\tparameter\u0018\u0002 \u0001(\t\u00128\n\nproto_file\u0018\u000f \u0003(\u000b2$.google.protobuf.FileDescriptorProto\u0012;\n\u0010compiler_version\u0018\u0003 \u0001(\u000b2!.google.protobuf.compiler.Version\"Á\u0002\n\u0015CodeGeneratorResponse\u0012\r\n\u0005error\u0018\u0001 \u0001(\t\u0012\u001a\n\u0012supported_features\u0018\u0002 \u0001(\u0004\u0012B\n\u0004file\u0018\u000f \u0003(\u000b24.google.protobuf.compiler.CodeGeneratorResponse.File\u001a\u007f\n\u0004File\u0012\f\n\u0004name\u0018\u0001 \u0001(\t\u0012\u0017\n\u000finsertion_point\u0018\u0002 \u0001(\t\u0012\u000f\n\u0007content\u0018\u000f \u0001(\t\u0012?\n\u0013generated_code_info\u0018\u0010 \u0001(\u000b2\".google.protobuf.GeneratedCodeInfo\"8\n\u0007Feature\u0012\u0010\n\fFEATURE_NONE\u0010\u0000\u0012\u001b\n\u0017FEATURE_PROTO3_OPTIONAL\u0010\u0001BW\n\u001ccom.google.protobuf.compilerB\fPluginProtosZ)google.golang.org/protobuf/types/pluginpb"
      };
      descriptor = Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new Descriptors.FileDescriptor[]{DescriptorProtos.getDescriptor()});
      DescriptorProtos.getDescriptor();
   }

   public static final class CodeGeneratorRequest extends GeneratedMessageV3 implements PluginProtos.CodeGeneratorRequestOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int FILE_TO_GENERATE_FIELD_NUMBER = 1;
      private LazyStringList fileToGenerate_;
      public static final int PARAMETER_FIELD_NUMBER = 2;
      private volatile Object parameter_;
      public static final int PROTO_FILE_FIELD_NUMBER = 15;
      private List<DescriptorProtos.FileDescriptorProto> protoFile_;
      public static final int COMPILER_VERSION_FIELD_NUMBER = 3;
      private PluginProtos.Version compilerVersion_;
      private byte memoizedIsInitialized = -1;
      private static final PluginProtos.CodeGeneratorRequest DEFAULT_INSTANCE = new PluginProtos.CodeGeneratorRequest();
      @Deprecated
      public static final Parser<PluginProtos.CodeGeneratorRequest> PARSER = new AbstractParser<PluginProtos.CodeGeneratorRequest>() {
         public PluginProtos.CodeGeneratorRequest parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new PluginProtos.CodeGeneratorRequest(input, extensionRegistry);
         }
      };

      private CodeGeneratorRequest(GeneratedMessageV3.Builder<?> builder) {
         super(builder);
      }

      private CodeGeneratorRequest() {
         this.fileToGenerate_ = LazyStringArrayList.EMPTY;
         this.parameter_ = "";
         this.protoFile_ = Collections.emptyList();
      }

      @Override
      protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
         return new PluginProtos.CodeGeneratorRequest();
      }

      @Override
      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private CodeGeneratorRequest(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         this();
         if (extensionRegistry == null) {
            throw new NullPointerException();
         } else {
            int mutable_bitField0_ = 0;
            UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

            try {
               boolean done = false;

               while(!done) {
                  int tag = input.readTag();
                  switch(tag) {
                     case 0:
                        done = true;
                        break;
                     case 10: {
                        ByteString bs = input.readBytes();
                        if ((mutable_bitField0_ & 1) == 0) {
                           this.fileToGenerate_ = new LazyStringArrayList();
                           mutable_bitField0_ |= 1;
                        }

                        this.fileToGenerate_.add(bs);
                        break;
                     }
                     case 18: {
                        ByteString bs = input.readBytes();
                        this.bitField0_ |= 1;
                        this.parameter_ = bs;
                        break;
                     }
                     case 26:
                        PluginProtos.Version.Builder subBuilder = null;
                        if ((this.bitField0_ & 2) != 0) {
                           subBuilder = this.compilerVersion_.toBuilder();
                        }

                        this.compilerVersion_ = input.readMessage(PluginProtos.Version.PARSER, extensionRegistry);
                        if (subBuilder != null) {
                           subBuilder.mergeFrom(this.compilerVersion_);
                           this.compilerVersion_ = subBuilder.buildPartial();
                        }

                        this.bitField0_ |= 2;
                        break;
                     case 122:
                        if ((mutable_bitField0_ & 4) == 0) {
                           this.protoFile_ = new ArrayList();
                           mutable_bitField0_ |= 4;
                        }

                        this.protoFile_.add(input.readMessage(DescriptorProtos.FileDescriptorProto.PARSER, extensionRegistry));
                        break;
                     default:
                        if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                           done = true;
                        }
                  }
               }
            } catch (InvalidProtocolBufferException var13) {
               throw var13.setUnfinishedMessage(this);
            } catch (UninitializedMessageException var14) {
               throw var14.asInvalidProtocolBufferException().setUnfinishedMessage(this);
            } catch (IOException var15) {
               throw new InvalidProtocolBufferException(var15).setUnfinishedMessage(this);
            } finally {
               if ((mutable_bitField0_ & 1) != 0) {
                  this.fileToGenerate_ = this.fileToGenerate_.getUnmodifiableView();
               }

               if ((mutable_bitField0_ & 4) != 0) {
                  this.protoFile_ = Collections.unmodifiableList(this.protoFile_);
               }

               this.unknownFields = unknownFields.build();
               this.makeExtensionsImmutable();
            }

         }
      }

      public static final Descriptors.Descriptor getDescriptor() {
         return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorRequest_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorRequest_fieldAccessorTable
            .ensureFieldAccessorsInitialized(PluginProtos.CodeGeneratorRequest.class, PluginProtos.CodeGeneratorRequest.Builder.class);
      }

      public ProtocolStringList getFileToGenerateList() {
         return this.fileToGenerate_;
      }

      @Override
      public int getFileToGenerateCount() {
         return this.fileToGenerate_.size();
      }

      @Override
      public String getFileToGenerate(int index) {
         return (String)this.fileToGenerate_.get(index);
      }

      @Override
      public ByteString getFileToGenerateBytes(int index) {
         return this.fileToGenerate_.getByteString(index);
      }

      @Override
      public boolean hasParameter() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public String getParameter() {
         Object ref = this.parameter_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.parameter_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getParameterBytes() {
         Object ref = this.parameter_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.parameter_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public List<DescriptorProtos.FileDescriptorProto> getProtoFileList() {
         return this.protoFile_;
      }

      @Override
      public List<? extends DescriptorProtos.FileDescriptorProtoOrBuilder> getProtoFileOrBuilderList() {
         return this.protoFile_;
      }

      @Override
      public int getProtoFileCount() {
         return this.protoFile_.size();
      }

      @Override
      public DescriptorProtos.FileDescriptorProto getProtoFile(int index) {
         return (DescriptorProtos.FileDescriptorProto)this.protoFile_.get(index);
      }

      @Override
      public DescriptorProtos.FileDescriptorProtoOrBuilder getProtoFileOrBuilder(int index) {
         return (DescriptorProtos.FileDescriptorProtoOrBuilder)this.protoFile_.get(index);
      }

      @Override
      public boolean hasCompilerVersion() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public PluginProtos.Version getCompilerVersion() {
         return this.compilerVersion_ == null ? PluginProtos.Version.getDefaultInstance() : this.compilerVersion_;
      }

      @Override
      public PluginProtos.VersionOrBuilder getCompilerVersionOrBuilder() {
         return this.compilerVersion_ == null ? PluginProtos.Version.getDefaultInstance() : this.compilerVersion_;
      }

      @Override
      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for(int i = 0; i < this.getProtoFileCount(); ++i) {
               if (!this.getProtoFile(i).isInitialized()) {
                  this.memoizedIsInitialized = 0;
                  return false;
               }
            }

            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      @Override
      public void writeTo(CodedOutputStream output) throws IOException {
         for(int i = 0; i < this.fileToGenerate_.size(); ++i) {
            GeneratedMessageV3.writeString(output, 1, this.fileToGenerate_.getRaw(i));
         }

         if ((this.bitField0_ & 1) != 0) {
            GeneratedMessageV3.writeString(output, 2, this.parameter_);
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeMessage(3, this.getCompilerVersion());
         }

         for(int i = 0; i < this.protoFile_.size(); ++i) {
            output.writeMessage(15, (MessageLite)this.protoFile_.get(i));
         }

         this.unknownFields.writeTo(output);
      }

      @Override
      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;
            int dataSize = 0;

            for(int i = 0; i < this.fileToGenerate_.size(); ++i) {
               dataSize += computeStringSizeNoTag(this.fileToGenerate_.getRaw(i));
            }

            size += dataSize;
            size += 1 * this.getFileToGenerateList().size();
            if ((this.bitField0_ & 1) != 0) {
               size += GeneratedMessageV3.computeStringSize(2, this.parameter_);
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeMessageSize(3, this.getCompilerVersion());
            }

            for(int i = 0; i < this.protoFile_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(15, (MessageLite)this.protoFile_.get(i));
            }

            size += this.unknownFields.getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof PluginProtos.CodeGeneratorRequest)) {
            return super.equals(obj);
         } else {
            PluginProtos.CodeGeneratorRequest other = (PluginProtos.CodeGeneratorRequest)obj;
            if (!this.getFileToGenerateList().equals(other.getFileToGenerateList())) {
               return false;
            } else if (this.hasParameter() != other.hasParameter()) {
               return false;
            } else if (this.hasParameter() && !this.getParameter().equals(other.getParameter())) {
               return false;
            } else if (!this.getProtoFileList().equals(other.getProtoFileList())) {
               return false;
            } else if (this.hasCompilerVersion() != other.hasCompilerVersion()) {
               return false;
            } else if (this.hasCompilerVersion() && !this.getCompilerVersion().equals(other.getCompilerVersion())) {
               return false;
            } else {
               return this.unknownFields.equals(other.unknownFields);
            }
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            if (this.getFileToGenerateCount() > 0) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getFileToGenerateList().hashCode();
            }

            if (this.hasParameter()) {
               hash = 37 * hash + 2;
               hash = 53 * hash + this.getParameter().hashCode();
            }

            if (this.getProtoFileCount() > 0) {
               hash = 37 * hash + 15;
               hash = 53 * hash + this.getProtoFileList().hashCode();
            }

            if (this.hasCompilerVersion()) {
               hash = 37 * hash + 3;
               hash = 53 * hash + this.getCompilerVersion().hashCode();
            }

            hash = 29 * hash + this.unknownFields.hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(InputStream input) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorRequest parseDelimitedFrom(InputStream input) throws IOException {
         return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
      }

      public static PluginProtos.CodeGeneratorRequest parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(CodedInputStream input) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input);
      }

      public static PluginProtos.CodeGeneratorRequest parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public PluginProtos.CodeGeneratorRequest.Builder newBuilderForType() {
         return newBuilder();
      }

      public static PluginProtos.CodeGeneratorRequest.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static PluginProtos.CodeGeneratorRequest.Builder newBuilder(PluginProtos.CodeGeneratorRequest prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public PluginProtos.CodeGeneratorRequest.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new PluginProtos.CodeGeneratorRequest.Builder() : new PluginProtos.CodeGeneratorRequest.Builder().mergeFrom(this);
      }

      protected PluginProtos.CodeGeneratorRequest.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
         return new PluginProtos.CodeGeneratorRequest.Builder(parent);
      }

      public static PluginProtos.CodeGeneratorRequest getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<PluginProtos.CodeGeneratorRequest> parser() {
         return PARSER;
      }

      @Override
      public Parser<PluginProtos.CodeGeneratorRequest> getParserForType() {
         return PARSER;
      }

      public PluginProtos.CodeGeneratorRequest getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      public static final class Builder
         extends GeneratedMessageV3.Builder<PluginProtos.CodeGeneratorRequest.Builder>
         implements PluginProtos.CodeGeneratorRequestOrBuilder {
         private int bitField0_;
         private LazyStringList fileToGenerate_ = LazyStringArrayList.EMPTY;
         private Object parameter_ = "";
         private List<DescriptorProtos.FileDescriptorProto> protoFile_ = Collections.emptyList();
         private RepeatedFieldBuilderV3<DescriptorProtos.FileDescriptorProto, DescriptorProtos.FileDescriptorProto.Builder, DescriptorProtos.FileDescriptorProtoOrBuilder> protoFileBuilder_;
         private PluginProtos.Version compilerVersion_;
         private SingleFieldBuilderV3<PluginProtos.Version, PluginProtos.Version.Builder, PluginProtos.VersionOrBuilder> compilerVersionBuilder_;

         public static final Descriptors.Descriptor getDescriptor() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorRequest_descriptor;
         }

         @Override
         protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorRequest_fieldAccessorTable
               .ensureFieldAccessorsInitialized(PluginProtos.CodeGeneratorRequest.class, PluginProtos.CodeGeneratorRequest.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(GeneratedMessageV3.BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (PluginProtos.CodeGeneratorRequest.alwaysUseFieldBuilders) {
               this.getProtoFileFieldBuilder();
               this.getCompilerVersionFieldBuilder();
            }

         }

         public PluginProtos.CodeGeneratorRequest.Builder clear() {
            super.clear();
            this.fileToGenerate_ = LazyStringArrayList.EMPTY;
            this.bitField0_ &= -2;
            this.parameter_ = "";
            this.bitField0_ &= -3;
            if (this.protoFileBuilder_ == null) {
               this.protoFile_ = Collections.emptyList();
               this.bitField0_ &= -5;
            } else {
               this.protoFileBuilder_.clear();
            }

            if (this.compilerVersionBuilder_ == null) {
               this.compilerVersion_ = null;
            } else {
               this.compilerVersionBuilder_.clear();
            }

            this.bitField0_ &= -9;
            return this;
         }

         @Override
         public Descriptors.Descriptor getDescriptorForType() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorRequest_descriptor;
         }

         public PluginProtos.CodeGeneratorRequest getDefaultInstanceForType() {
            return PluginProtos.CodeGeneratorRequest.getDefaultInstance();
         }

         public PluginProtos.CodeGeneratorRequest build() {
            PluginProtos.CodeGeneratorRequest result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public PluginProtos.CodeGeneratorRequest buildPartial() {
            PluginProtos.CodeGeneratorRequest result = new PluginProtos.CodeGeneratorRequest(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((this.bitField0_ & 1) != 0) {
               this.fileToGenerate_ = this.fileToGenerate_.getUnmodifiableView();
               this.bitField0_ &= -2;
            }

            result.fileToGenerate_ = this.fileToGenerate_;
            if ((from_bitField0_ & 2) != 0) {
               to_bitField0_ |= 1;
            }

            result.parameter_ = this.parameter_;
            if (this.protoFileBuilder_ == null) {
               if ((this.bitField0_ & 4) != 0) {
                  this.protoFile_ = Collections.unmodifiableList(this.protoFile_);
                  this.bitField0_ &= -5;
               }

               result.protoFile_ = this.protoFile_;
            } else {
               result.protoFile_ = this.protoFileBuilder_.build();
            }

            if ((from_bitField0_ & 8) != 0) {
               if (this.compilerVersionBuilder_ == null) {
                  result.compilerVersion_ = this.compilerVersion_;
               } else {
                  result.compilerVersion_ = this.compilerVersionBuilder_.build();
               }

               to_bitField0_ |= 2;
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public PluginProtos.CodeGeneratorRequest.Builder clone() {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.clone();
         }

         public PluginProtos.CodeGeneratorRequest.Builder setField(Descriptors.FieldDescriptor field, Object value) {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.setField(field, value);
         }

         public PluginProtos.CodeGeneratorRequest.Builder clearField(Descriptors.FieldDescriptor field) {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.clearField(field);
         }

         public PluginProtos.CodeGeneratorRequest.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.clearOneof(oneof);
         }

         public PluginProtos.CodeGeneratorRequest.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.setRepeatedField(field, index, value);
         }

         public PluginProtos.CodeGeneratorRequest.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.addRepeatedField(field, value);
         }

         public PluginProtos.CodeGeneratorRequest.Builder mergeFrom(Message other) {
            if (other instanceof PluginProtos.CodeGeneratorRequest) {
               return this.mergeFrom((PluginProtos.CodeGeneratorRequest)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public PluginProtos.CodeGeneratorRequest.Builder mergeFrom(PluginProtos.CodeGeneratorRequest other) {
            if (other == PluginProtos.CodeGeneratorRequest.getDefaultInstance()) {
               return this;
            } else {
               if (!other.fileToGenerate_.isEmpty()) {
                  if (this.fileToGenerate_.isEmpty()) {
                     this.fileToGenerate_ = other.fileToGenerate_;
                     this.bitField0_ &= -2;
                  } else {
                     this.ensureFileToGenerateIsMutable();
                     this.fileToGenerate_.addAll(other.fileToGenerate_);
                  }

                  this.onChanged();
               }

               if (other.hasParameter()) {
                  this.bitField0_ |= 2;
                  this.parameter_ = other.parameter_;
                  this.onChanged();
               }

               if (this.protoFileBuilder_ == null) {
                  if (!other.protoFile_.isEmpty()) {
                     if (this.protoFile_.isEmpty()) {
                        this.protoFile_ = other.protoFile_;
                        this.bitField0_ &= -5;
                     } else {
                        this.ensureProtoFileIsMutable();
                        this.protoFile_.addAll(other.protoFile_);
                     }

                     this.onChanged();
                  }
               } else if (!other.protoFile_.isEmpty()) {
                  if (this.protoFileBuilder_.isEmpty()) {
                     this.protoFileBuilder_.dispose();
                     this.protoFileBuilder_ = null;
                     this.protoFile_ = other.protoFile_;
                     this.bitField0_ &= -5;
                     this.protoFileBuilder_ = PluginProtos.CodeGeneratorRequest.alwaysUseFieldBuilders ? this.getProtoFileFieldBuilder() : null;
                  } else {
                     this.protoFileBuilder_.addAllMessages(other.protoFile_);
                  }
               }

               if (other.hasCompilerVersion()) {
                  this.mergeCompilerVersion(other.getCompilerVersion());
               }

               this.mergeUnknownFields(other.unknownFields);
               this.onChanged();
               return this;
            }
         }

         @Override
         public final boolean isInitialized() {
            for(int i = 0; i < this.getProtoFileCount(); ++i) {
               if (!this.getProtoFile(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public PluginProtos.CodeGeneratorRequest.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            PluginProtos.CodeGeneratorRequest parsedMessage = null;

            try {
               parsedMessage = PluginProtos.CodeGeneratorRequest.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (PluginProtos.CodeGeneratorRequest)var8.getUnfinishedMessage();
               throw var8.unwrapIOException();
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }

            }

            return this;
         }

         private void ensureFileToGenerateIsMutable() {
            if ((this.bitField0_ & 1) == 0) {
               this.fileToGenerate_ = new LazyStringArrayList(this.fileToGenerate_);
               this.bitField0_ |= 1;
            }

         }

         public ProtocolStringList getFileToGenerateList() {
            return this.fileToGenerate_.getUnmodifiableView();
         }

         @Override
         public int getFileToGenerateCount() {
            return this.fileToGenerate_.size();
         }

         @Override
         public String getFileToGenerate(int index) {
            return (String)this.fileToGenerate_.get(index);
         }

         @Override
         public ByteString getFileToGenerateBytes(int index) {
            return this.fileToGenerate_.getByteString(index);
         }

         public PluginProtos.CodeGeneratorRequest.Builder setFileToGenerate(int index, String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.ensureFileToGenerateIsMutable();
               this.fileToGenerate_.set(index, value);
               this.onChanged();
               return this;
            }
         }

         public PluginProtos.CodeGeneratorRequest.Builder addFileToGenerate(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.ensureFileToGenerateIsMutable();
               this.fileToGenerate_.add(value);
               this.onChanged();
               return this;
            }
         }

         public PluginProtos.CodeGeneratorRequest.Builder addAllFileToGenerate(Iterable<String> values) {
            this.ensureFileToGenerateIsMutable();
            AbstractMessageLite.Builder.addAll(values, this.fileToGenerate_);
            this.onChanged();
            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder clearFileToGenerate() {
            this.fileToGenerate_ = LazyStringArrayList.EMPTY;
            this.bitField0_ &= -2;
            this.onChanged();
            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder addFileToGenerateBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.ensureFileToGenerateIsMutable();
               this.fileToGenerate_.add(value);
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasParameter() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public String getParameter() {
            Object ref = this.parameter_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.parameter_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getParameterBytes() {
            Object ref = this.parameter_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.parameter_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public PluginProtos.CodeGeneratorRequest.Builder setParameter(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.parameter_ = value;
               this.onChanged();
               return this;
            }
         }

         public PluginProtos.CodeGeneratorRequest.Builder clearParameter() {
            this.bitField0_ &= -3;
            this.parameter_ = PluginProtos.CodeGeneratorRequest.getDefaultInstance().getParameter();
            this.onChanged();
            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder setParameterBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.parameter_ = value;
               this.onChanged();
               return this;
            }
         }

         private void ensureProtoFileIsMutable() {
            if ((this.bitField0_ & 4) == 0) {
               this.protoFile_ = new ArrayList(this.protoFile_);
               this.bitField0_ |= 4;
            }

         }

         @Override
         public List<DescriptorProtos.FileDescriptorProto> getProtoFileList() {
            return this.protoFileBuilder_ == null ? Collections.unmodifiableList(this.protoFile_) : this.protoFileBuilder_.getMessageList();
         }

         @Override
         public int getProtoFileCount() {
            return this.protoFileBuilder_ == null ? this.protoFile_.size() : this.protoFileBuilder_.getCount();
         }

         @Override
         public DescriptorProtos.FileDescriptorProto getProtoFile(int index) {
            return this.protoFileBuilder_ == null ? (DescriptorProtos.FileDescriptorProto)this.protoFile_.get(index) : this.protoFileBuilder_.getMessage(index);
         }

         public PluginProtos.CodeGeneratorRequest.Builder setProtoFile(int index, DescriptorProtos.FileDescriptorProto value) {
            if (this.protoFileBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureProtoFileIsMutable();
               this.protoFile_.set(index, value);
               this.onChanged();
            } else {
               this.protoFileBuilder_.setMessage(index, value);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder setProtoFile(int index, DescriptorProtos.FileDescriptorProto.Builder builderForValue) {
            if (this.protoFileBuilder_ == null) {
               this.ensureProtoFileIsMutable();
               this.protoFile_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.protoFileBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder addProtoFile(DescriptorProtos.FileDescriptorProto value) {
            if (this.protoFileBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureProtoFileIsMutable();
               this.protoFile_.add(value);
               this.onChanged();
            } else {
               this.protoFileBuilder_.addMessage(value);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder addProtoFile(int index, DescriptorProtos.FileDescriptorProto value) {
            if (this.protoFileBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureProtoFileIsMutable();
               this.protoFile_.add(index, value);
               this.onChanged();
            } else {
               this.protoFileBuilder_.addMessage(index, value);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder addProtoFile(DescriptorProtos.FileDescriptorProto.Builder builderForValue) {
            if (this.protoFileBuilder_ == null) {
               this.ensureProtoFileIsMutable();
               this.protoFile_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.protoFileBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder addProtoFile(int index, DescriptorProtos.FileDescriptorProto.Builder builderForValue) {
            if (this.protoFileBuilder_ == null) {
               this.ensureProtoFileIsMutable();
               this.protoFile_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.protoFileBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder addAllProtoFile(Iterable<? extends DescriptorProtos.FileDescriptorProto> values) {
            if (this.protoFileBuilder_ == null) {
               this.ensureProtoFileIsMutable();
               AbstractMessageLite.Builder.addAll(values, this.protoFile_);
               this.onChanged();
            } else {
               this.protoFileBuilder_.addAllMessages(values);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder clearProtoFile() {
            if (this.protoFileBuilder_ == null) {
               this.protoFile_ = Collections.emptyList();
               this.bitField0_ &= -5;
               this.onChanged();
            } else {
               this.protoFileBuilder_.clear();
            }

            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder removeProtoFile(int index) {
            if (this.protoFileBuilder_ == null) {
               this.ensureProtoFileIsMutable();
               this.protoFile_.remove(index);
               this.onChanged();
            } else {
               this.protoFileBuilder_.remove(index);
            }

            return this;
         }

         public DescriptorProtos.FileDescriptorProto.Builder getProtoFileBuilder(int index) {
            return this.getProtoFileFieldBuilder().getBuilder(index);
         }

         @Override
         public DescriptorProtos.FileDescriptorProtoOrBuilder getProtoFileOrBuilder(int index) {
            return this.protoFileBuilder_ == null
               ? (DescriptorProtos.FileDescriptorProtoOrBuilder)this.protoFile_.get(index)
               : this.protoFileBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends DescriptorProtos.FileDescriptorProtoOrBuilder> getProtoFileOrBuilderList() {
            return this.protoFileBuilder_ != null ? this.protoFileBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.protoFile_);
         }

         public DescriptorProtos.FileDescriptorProto.Builder addProtoFileBuilder() {
            return this.getProtoFileFieldBuilder().addBuilder(DescriptorProtos.FileDescriptorProto.getDefaultInstance());
         }

         public DescriptorProtos.FileDescriptorProto.Builder addProtoFileBuilder(int index) {
            return this.getProtoFileFieldBuilder().addBuilder(index, DescriptorProtos.FileDescriptorProto.getDefaultInstance());
         }

         public List<DescriptorProtos.FileDescriptorProto.Builder> getProtoFileBuilderList() {
            return this.getProtoFileFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilderV3<DescriptorProtos.FileDescriptorProto, DescriptorProtos.FileDescriptorProto.Builder, DescriptorProtos.FileDescriptorProtoOrBuilder> getProtoFileFieldBuilder() {
            if (this.protoFileBuilder_ == null) {
               this.protoFileBuilder_ = new RepeatedFieldBuilderV3<>(this.protoFile_, (this.bitField0_ & 4) != 0, this.getParentForChildren(), this.isClean());
               this.protoFile_ = null;
            }

            return this.protoFileBuilder_;
         }

         @Override
         public boolean hasCompilerVersion() {
            return (this.bitField0_ & 8) != 0;
         }

         @Override
         public PluginProtos.Version getCompilerVersion() {
            if (this.compilerVersionBuilder_ == null) {
               return this.compilerVersion_ == null ? PluginProtos.Version.getDefaultInstance() : this.compilerVersion_;
            } else {
               return this.compilerVersionBuilder_.getMessage();
            }
         }

         public PluginProtos.CodeGeneratorRequest.Builder setCompilerVersion(PluginProtos.Version value) {
            if (this.compilerVersionBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.compilerVersion_ = value;
               this.onChanged();
            } else {
               this.compilerVersionBuilder_.setMessage(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder setCompilerVersion(PluginProtos.Version.Builder builderForValue) {
            if (this.compilerVersionBuilder_ == null) {
               this.compilerVersion_ = builderForValue.build();
               this.onChanged();
            } else {
               this.compilerVersionBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 8;
            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder mergeCompilerVersion(PluginProtos.Version value) {
            if (this.compilerVersionBuilder_ == null) {
               if ((this.bitField0_ & 8) != 0 && this.compilerVersion_ != null && this.compilerVersion_ != PluginProtos.Version.getDefaultInstance()) {
                  this.compilerVersion_ = PluginProtos.Version.newBuilder(this.compilerVersion_).mergeFrom(value).buildPartial();
               } else {
                  this.compilerVersion_ = value;
               }

               this.onChanged();
            } else {
               this.compilerVersionBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public PluginProtos.CodeGeneratorRequest.Builder clearCompilerVersion() {
            if (this.compilerVersionBuilder_ == null) {
               this.compilerVersion_ = null;
               this.onChanged();
            } else {
               this.compilerVersionBuilder_.clear();
            }

            this.bitField0_ &= -9;
            return this;
         }

         public PluginProtos.Version.Builder getCompilerVersionBuilder() {
            this.bitField0_ |= 8;
            this.onChanged();
            return this.getCompilerVersionFieldBuilder().getBuilder();
         }

         @Override
         public PluginProtos.VersionOrBuilder getCompilerVersionOrBuilder() {
            if (this.compilerVersionBuilder_ != null) {
               return this.compilerVersionBuilder_.getMessageOrBuilder();
            } else {
               return this.compilerVersion_ == null ? PluginProtos.Version.getDefaultInstance() : this.compilerVersion_;
            }
         }

         private SingleFieldBuilderV3<PluginProtos.Version, PluginProtos.Version.Builder, PluginProtos.VersionOrBuilder> getCompilerVersionFieldBuilder() {
            if (this.compilerVersionBuilder_ == null) {
               this.compilerVersionBuilder_ = new SingleFieldBuilderV3<>(this.getCompilerVersion(), this.getParentForChildren(), this.isClean());
               this.compilerVersion_ = null;
            }

            return this.compilerVersionBuilder_;
         }

         public final PluginProtos.CodeGeneratorRequest.Builder setUnknownFields(UnknownFieldSet unknownFields) {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.setUnknownFields(unknownFields);
         }

         public final PluginProtos.CodeGeneratorRequest.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
            return (PluginProtos.CodeGeneratorRequest.Builder)super.mergeUnknownFields(unknownFields);
         }
      }
   }

   public interface CodeGeneratorRequestOrBuilder extends MessageOrBuilder {
      List<String> getFileToGenerateList();

      int getFileToGenerateCount();

      String getFileToGenerate(int var1);

      ByteString getFileToGenerateBytes(int var1);

      boolean hasParameter();

      String getParameter();

      ByteString getParameterBytes();

      List<DescriptorProtos.FileDescriptorProto> getProtoFileList();

      DescriptorProtos.FileDescriptorProto getProtoFile(int var1);

      int getProtoFileCount();

      List<? extends DescriptorProtos.FileDescriptorProtoOrBuilder> getProtoFileOrBuilderList();

      DescriptorProtos.FileDescriptorProtoOrBuilder getProtoFileOrBuilder(int var1);

      boolean hasCompilerVersion();

      PluginProtos.Version getCompilerVersion();

      PluginProtos.VersionOrBuilder getCompilerVersionOrBuilder();
   }

   public static final class CodeGeneratorResponse extends GeneratedMessageV3 implements PluginProtos.CodeGeneratorResponseOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int ERROR_FIELD_NUMBER = 1;
      private volatile Object error_;
      public static final int SUPPORTED_FEATURES_FIELD_NUMBER = 2;
      private long supportedFeatures_;
      public static final int FILE_FIELD_NUMBER = 15;
      private List<PluginProtos.CodeGeneratorResponse.File> file_;
      private byte memoizedIsInitialized = -1;
      private static final PluginProtos.CodeGeneratorResponse DEFAULT_INSTANCE = new PluginProtos.CodeGeneratorResponse();
      @Deprecated
      public static final Parser<PluginProtos.CodeGeneratorResponse> PARSER = new AbstractParser<PluginProtos.CodeGeneratorResponse>() {
         public PluginProtos.CodeGeneratorResponse parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new PluginProtos.CodeGeneratorResponse(input, extensionRegistry);
         }
      };

      private CodeGeneratorResponse(GeneratedMessageV3.Builder<?> builder) {
         super(builder);
      }

      private CodeGeneratorResponse() {
         this.error_ = "";
         this.file_ = Collections.emptyList();
      }

      @Override
      protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
         return new PluginProtos.CodeGeneratorResponse();
      }

      @Override
      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private CodeGeneratorResponse(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         this();
         if (extensionRegistry == null) {
            throw new NullPointerException();
         } else {
            int mutable_bitField0_ = 0;
            UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

            try {
               boolean done = false;

               while(!done) {
                  int tag = input.readTag();
                  switch(tag) {
                     case 0:
                        done = true;
                        break;
                     case 10:
                        ByteString bs = input.readBytes();
                        this.bitField0_ |= 1;
                        this.error_ = bs;
                        break;
                     case 16:
                        this.bitField0_ |= 2;
                        this.supportedFeatures_ = input.readUInt64();
                        break;
                     case 122:
                        if ((mutable_bitField0_ & 4) == 0) {
                           this.file_ = new ArrayList();
                           mutable_bitField0_ |= 4;
                        }

                        this.file_.add(input.readMessage(PluginProtos.CodeGeneratorResponse.File.PARSER, extensionRegistry));
                        break;
                     default:
                        if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                           done = true;
                        }
                  }
               }
            } catch (InvalidProtocolBufferException var13) {
               throw var13.setUnfinishedMessage(this);
            } catch (UninitializedMessageException var14) {
               throw var14.asInvalidProtocolBufferException().setUnfinishedMessage(this);
            } catch (IOException var15) {
               throw new InvalidProtocolBufferException(var15).setUnfinishedMessage(this);
            } finally {
               if ((mutable_bitField0_ & 4) != 0) {
                  this.file_ = Collections.unmodifiableList(this.file_);
               }

               this.unknownFields = unknownFields.build();
               this.makeExtensionsImmutable();
            }

         }
      }

      public static final Descriptors.Descriptor getDescriptor() {
         return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(PluginProtos.CodeGeneratorResponse.class, PluginProtos.CodeGeneratorResponse.Builder.class);
      }

      @Override
      public boolean hasError() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public String getError() {
         Object ref = this.error_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.error_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getErrorBytes() {
         Object ref = this.error_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.error_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasSupportedFeatures() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public long getSupportedFeatures() {
         return this.supportedFeatures_;
      }

      @Override
      public List<PluginProtos.CodeGeneratorResponse.File> getFileList() {
         return this.file_;
      }

      @Override
      public List<? extends PluginProtos.CodeGeneratorResponse.FileOrBuilder> getFileOrBuilderList() {
         return this.file_;
      }

      @Override
      public int getFileCount() {
         return this.file_.size();
      }

      @Override
      public PluginProtos.CodeGeneratorResponse.File getFile(int index) {
         return (PluginProtos.CodeGeneratorResponse.File)this.file_.get(index);
      }

      @Override
      public PluginProtos.CodeGeneratorResponse.FileOrBuilder getFileOrBuilder(int index) {
         return (PluginProtos.CodeGeneratorResponse.FileOrBuilder)this.file_.get(index);
      }

      @Override
      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      @Override
      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            GeneratedMessageV3.writeString(output, 1, this.error_);
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeUInt64(2, this.supportedFeatures_);
         }

         for(int i = 0; i < this.file_.size(); ++i) {
            output.writeMessage(15, (MessageLite)this.file_.get(i));
         }

         this.unknownFields.writeTo(output);
      }

      @Override
      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
               size += GeneratedMessageV3.computeStringSize(1, this.error_);
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeUInt64Size(2, this.supportedFeatures_);
            }

            for(int i = 0; i < this.file_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(15, (MessageLite)this.file_.get(i));
            }

            size += this.unknownFields.getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof PluginProtos.CodeGeneratorResponse)) {
            return super.equals(obj);
         } else {
            PluginProtos.CodeGeneratorResponse other = (PluginProtos.CodeGeneratorResponse)obj;
            if (this.hasError() != other.hasError()) {
               return false;
            } else if (this.hasError() && !this.getError().equals(other.getError())) {
               return false;
            } else if (this.hasSupportedFeatures() != other.hasSupportedFeatures()) {
               return false;
            } else if (this.hasSupportedFeatures() && this.getSupportedFeatures() != other.getSupportedFeatures()) {
               return false;
            } else if (!this.getFileList().equals(other.getFileList())) {
               return false;
            } else {
               return this.unknownFields.equals(other.unknownFields);
            }
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            if (this.hasError()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getError().hashCode();
            }

            if (this.hasSupportedFeatures()) {
               hash = 37 * hash + 2;
               hash = 53 * hash + Internal.hashLong(this.getSupportedFeatures());
            }

            if (this.getFileCount() > 0) {
               hash = 37 * hash + 15;
               hash = 53 * hash + this.getFileList().hashCode();
            }

            hash = 29 * hash + this.unknownFields.hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(InputStream input) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorResponse parseDelimitedFrom(InputStream input) throws IOException {
         return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
      }

      public static PluginProtos.CodeGeneratorResponse parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(CodedInputStream input) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input);
      }

      public static PluginProtos.CodeGeneratorResponse parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public PluginProtos.CodeGeneratorResponse.Builder newBuilderForType() {
         return newBuilder();
      }

      public static PluginProtos.CodeGeneratorResponse.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static PluginProtos.CodeGeneratorResponse.Builder newBuilder(PluginProtos.CodeGeneratorResponse prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public PluginProtos.CodeGeneratorResponse.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new PluginProtos.CodeGeneratorResponse.Builder() : new PluginProtos.CodeGeneratorResponse.Builder().mergeFrom(this);
      }

      protected PluginProtos.CodeGeneratorResponse.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
         return new PluginProtos.CodeGeneratorResponse.Builder(parent);
      }

      public static PluginProtos.CodeGeneratorResponse getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<PluginProtos.CodeGeneratorResponse> parser() {
         return PARSER;
      }

      @Override
      public Parser<PluginProtos.CodeGeneratorResponse> getParserForType() {
         return PARSER;
      }

      public PluginProtos.CodeGeneratorResponse getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      public static final class Builder
         extends GeneratedMessageV3.Builder<PluginProtos.CodeGeneratorResponse.Builder>
         implements PluginProtos.CodeGeneratorResponseOrBuilder {
         private int bitField0_;
         private Object error_ = "";
         private long supportedFeatures_;
         private List<PluginProtos.CodeGeneratorResponse.File> file_ = Collections.emptyList();
         private RepeatedFieldBuilderV3<PluginProtos.CodeGeneratorResponse.File, PluginProtos.CodeGeneratorResponse.File.Builder, PluginProtos.CodeGeneratorResponse.FileOrBuilder> fileBuilder_;

         public static final Descriptors.Descriptor getDescriptor() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_descriptor;
         }

         @Override
         protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_fieldAccessorTable
               .ensureFieldAccessorsInitialized(PluginProtos.CodeGeneratorResponse.class, PluginProtos.CodeGeneratorResponse.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(GeneratedMessageV3.BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (PluginProtos.CodeGeneratorResponse.alwaysUseFieldBuilders) {
               this.getFileFieldBuilder();
            }

         }

         public PluginProtos.CodeGeneratorResponse.Builder clear() {
            super.clear();
            this.error_ = "";
            this.bitField0_ &= -2;
            this.supportedFeatures_ = 0L;
            this.bitField0_ &= -3;
            if (this.fileBuilder_ == null) {
               this.file_ = Collections.emptyList();
               this.bitField0_ &= -5;
            } else {
               this.fileBuilder_.clear();
            }

            return this;
         }

         @Override
         public Descriptors.Descriptor getDescriptorForType() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_descriptor;
         }

         public PluginProtos.CodeGeneratorResponse getDefaultInstanceForType() {
            return PluginProtos.CodeGeneratorResponse.getDefaultInstance();
         }

         public PluginProtos.CodeGeneratorResponse build() {
            PluginProtos.CodeGeneratorResponse result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public PluginProtos.CodeGeneratorResponse buildPartial() {
            PluginProtos.CodeGeneratorResponse result = new PluginProtos.CodeGeneratorResponse(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               to_bitField0_ |= 1;
            }

            result.error_ = this.error_;
            if ((from_bitField0_ & 2) != 0) {
               result.supportedFeatures_ = this.supportedFeatures_;
               to_bitField0_ |= 2;
            }

            if (this.fileBuilder_ == null) {
               if ((this.bitField0_ & 4) != 0) {
                  this.file_ = Collections.unmodifiableList(this.file_);
                  this.bitField0_ &= -5;
               }

               result.file_ = this.file_;
            } else {
               result.file_ = this.fileBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public PluginProtos.CodeGeneratorResponse.Builder clone() {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.clone();
         }

         public PluginProtos.CodeGeneratorResponse.Builder setField(Descriptors.FieldDescriptor field, Object value) {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.setField(field, value);
         }

         public PluginProtos.CodeGeneratorResponse.Builder clearField(Descriptors.FieldDescriptor field) {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.clearField(field);
         }

         public PluginProtos.CodeGeneratorResponse.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.clearOneof(oneof);
         }

         public PluginProtos.CodeGeneratorResponse.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.setRepeatedField(field, index, value);
         }

         public PluginProtos.CodeGeneratorResponse.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.addRepeatedField(field, value);
         }

         public PluginProtos.CodeGeneratorResponse.Builder mergeFrom(Message other) {
            if (other instanceof PluginProtos.CodeGeneratorResponse) {
               return this.mergeFrom((PluginProtos.CodeGeneratorResponse)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public PluginProtos.CodeGeneratorResponse.Builder mergeFrom(PluginProtos.CodeGeneratorResponse other) {
            if (other == PluginProtos.CodeGeneratorResponse.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasError()) {
                  this.bitField0_ |= 1;
                  this.error_ = other.error_;
                  this.onChanged();
               }

               if (other.hasSupportedFeatures()) {
                  this.setSupportedFeatures(other.getSupportedFeatures());
               }

               if (this.fileBuilder_ == null) {
                  if (!other.file_.isEmpty()) {
                     if (this.file_.isEmpty()) {
                        this.file_ = other.file_;
                        this.bitField0_ &= -5;
                     } else {
                        this.ensureFileIsMutable();
                        this.file_.addAll(other.file_);
                     }

                     this.onChanged();
                  }
               } else if (!other.file_.isEmpty()) {
                  if (this.fileBuilder_.isEmpty()) {
                     this.fileBuilder_.dispose();
                     this.fileBuilder_ = null;
                     this.file_ = other.file_;
                     this.bitField0_ &= -5;
                     this.fileBuilder_ = PluginProtos.CodeGeneratorResponse.alwaysUseFieldBuilders ? this.getFileFieldBuilder() : null;
                  } else {
                     this.fileBuilder_.addAllMessages(other.file_);
                  }
               }

               this.mergeUnknownFields(other.unknownFields);
               this.onChanged();
               return this;
            }
         }

         @Override
         public final boolean isInitialized() {
            return true;
         }

         public PluginProtos.CodeGeneratorResponse.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            PluginProtos.CodeGeneratorResponse parsedMessage = null;

            try {
               parsedMessage = PluginProtos.CodeGeneratorResponse.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (PluginProtos.CodeGeneratorResponse)var8.getUnfinishedMessage();
               throw var8.unwrapIOException();
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }

            }

            return this;
         }

         @Override
         public boolean hasError() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public String getError() {
            Object ref = this.error_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.error_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getErrorBytes() {
            Object ref = this.error_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.error_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public PluginProtos.CodeGeneratorResponse.Builder setError(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.error_ = value;
               this.onChanged();
               return this;
            }
         }

         public PluginProtos.CodeGeneratorResponse.Builder clearError() {
            this.bitField0_ &= -2;
            this.error_ = PluginProtos.CodeGeneratorResponse.getDefaultInstance().getError();
            this.onChanged();
            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder setErrorBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.error_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasSupportedFeatures() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public long getSupportedFeatures() {
            return this.supportedFeatures_;
         }

         public PluginProtos.CodeGeneratorResponse.Builder setSupportedFeatures(long value) {
            this.bitField0_ |= 2;
            this.supportedFeatures_ = value;
            this.onChanged();
            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder clearSupportedFeatures() {
            this.bitField0_ &= -3;
            this.supportedFeatures_ = 0L;
            this.onChanged();
            return this;
         }

         private void ensureFileIsMutable() {
            if ((this.bitField0_ & 4) == 0) {
               this.file_ = new ArrayList(this.file_);
               this.bitField0_ |= 4;
            }

         }

         @Override
         public List<PluginProtos.CodeGeneratorResponse.File> getFileList() {
            return this.fileBuilder_ == null ? Collections.unmodifiableList(this.file_) : this.fileBuilder_.getMessageList();
         }

         @Override
         public int getFileCount() {
            return this.fileBuilder_ == null ? this.file_.size() : this.fileBuilder_.getCount();
         }

         @Override
         public PluginProtos.CodeGeneratorResponse.File getFile(int index) {
            return this.fileBuilder_ == null ? (PluginProtos.CodeGeneratorResponse.File)this.file_.get(index) : this.fileBuilder_.getMessage(index);
         }

         public PluginProtos.CodeGeneratorResponse.Builder setFile(int index, PluginProtos.CodeGeneratorResponse.File value) {
            if (this.fileBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFileIsMutable();
               this.file_.set(index, value);
               this.onChanged();
            } else {
               this.fileBuilder_.setMessage(index, value);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder setFile(int index, PluginProtos.CodeGeneratorResponse.File.Builder builderForValue) {
            if (this.fileBuilder_ == null) {
               this.ensureFileIsMutable();
               this.file_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fileBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder addFile(PluginProtos.CodeGeneratorResponse.File value) {
            if (this.fileBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFileIsMutable();
               this.file_.add(value);
               this.onChanged();
            } else {
               this.fileBuilder_.addMessage(value);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder addFile(int index, PluginProtos.CodeGeneratorResponse.File value) {
            if (this.fileBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFileIsMutable();
               this.file_.add(index, value);
               this.onChanged();
            } else {
               this.fileBuilder_.addMessage(index, value);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder addFile(PluginProtos.CodeGeneratorResponse.File.Builder builderForValue) {
            if (this.fileBuilder_ == null) {
               this.ensureFileIsMutable();
               this.file_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.fileBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder addFile(int index, PluginProtos.CodeGeneratorResponse.File.Builder builderForValue) {
            if (this.fileBuilder_ == null) {
               this.ensureFileIsMutable();
               this.file_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fileBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder addAllFile(Iterable<? extends PluginProtos.CodeGeneratorResponse.File> values) {
            if (this.fileBuilder_ == null) {
               this.ensureFileIsMutable();
               AbstractMessageLite.Builder.addAll(values, this.file_);
               this.onChanged();
            } else {
               this.fileBuilder_.addAllMessages(values);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder clearFile() {
            if (this.fileBuilder_ == null) {
               this.file_ = Collections.emptyList();
               this.bitField0_ &= -5;
               this.onChanged();
            } else {
               this.fileBuilder_.clear();
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.Builder removeFile(int index) {
            if (this.fileBuilder_ == null) {
               this.ensureFileIsMutable();
               this.file_.remove(index);
               this.onChanged();
            } else {
               this.fileBuilder_.remove(index);
            }

            return this;
         }

         public PluginProtos.CodeGeneratorResponse.File.Builder getFileBuilder(int index) {
            return this.getFileFieldBuilder().getBuilder(index);
         }

         @Override
         public PluginProtos.CodeGeneratorResponse.FileOrBuilder getFileOrBuilder(int index) {
            return this.fileBuilder_ == null
               ? (PluginProtos.CodeGeneratorResponse.FileOrBuilder)this.file_.get(index)
               : this.fileBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends PluginProtos.CodeGeneratorResponse.FileOrBuilder> getFileOrBuilderList() {
            return this.fileBuilder_ != null ? this.fileBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.file_);
         }

         public PluginProtos.CodeGeneratorResponse.File.Builder addFileBuilder() {
            return this.getFileFieldBuilder().addBuilder(PluginProtos.CodeGeneratorResponse.File.getDefaultInstance());
         }

         public PluginProtos.CodeGeneratorResponse.File.Builder addFileBuilder(int index) {
            return this.getFileFieldBuilder().addBuilder(index, PluginProtos.CodeGeneratorResponse.File.getDefaultInstance());
         }

         public List<PluginProtos.CodeGeneratorResponse.File.Builder> getFileBuilderList() {
            return this.getFileFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilderV3<PluginProtos.CodeGeneratorResponse.File, PluginProtos.CodeGeneratorResponse.File.Builder, PluginProtos.CodeGeneratorResponse.FileOrBuilder> getFileFieldBuilder() {
            if (this.fileBuilder_ == null) {
               this.fileBuilder_ = new RepeatedFieldBuilderV3<>(this.file_, (this.bitField0_ & 4) != 0, this.getParentForChildren(), this.isClean());
               this.file_ = null;
            }

            return this.fileBuilder_;
         }

         public final PluginProtos.CodeGeneratorResponse.Builder setUnknownFields(UnknownFieldSet unknownFields) {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.setUnknownFields(unknownFields);
         }

         public final PluginProtos.CodeGeneratorResponse.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
            return (PluginProtos.CodeGeneratorResponse.Builder)super.mergeUnknownFields(unknownFields);
         }
      }

      public static enum Feature implements ProtocolMessageEnum {
         FEATURE_NONE(0),
         FEATURE_PROTO3_OPTIONAL(1);

         public static final int FEATURE_NONE_VALUE = 0;
         public static final int FEATURE_PROTO3_OPTIONAL_VALUE = 1;
         private static final Internal.EnumLiteMap<PluginProtos.CodeGeneratorResponse.Feature> internalValueMap = new Internal.EnumLiteMap<PluginProtos.CodeGeneratorResponse.Feature>(
            
         ) {
            public PluginProtos.CodeGeneratorResponse.Feature findValueByNumber(int number) {
               return PluginProtos.CodeGeneratorResponse.Feature.forNumber(number);
            }
         };
         private static final PluginProtos.CodeGeneratorResponse.Feature[] VALUES = values();
         private final int value;

         @Override
         public final int getNumber() {
            return this.value;
         }

         @Deprecated
         public static PluginProtos.CodeGeneratorResponse.Feature valueOf(int value) {
            return forNumber(value);
         }

         public static PluginProtos.CodeGeneratorResponse.Feature forNumber(int value) {
            switch(value) {
               case 0:
                  return FEATURE_NONE;
               case 1:
                  return FEATURE_PROTO3_OPTIONAL;
               default:
                  return null;
            }
         }

         public static Internal.EnumLiteMap<PluginProtos.CodeGeneratorResponse.Feature> internalGetValueMap() {
            return internalValueMap;
         }

         @Override
         public final Descriptors.EnumValueDescriptor getValueDescriptor() {
            return (Descriptors.EnumValueDescriptor)getDescriptor().getValues().get(this.ordinal());
         }

         @Override
         public final Descriptors.EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final Descriptors.EnumDescriptor getDescriptor() {
            return (Descriptors.EnumDescriptor)PluginProtos.CodeGeneratorResponse.getDescriptor().getEnumTypes().get(0);
         }

         public static PluginProtos.CodeGeneratorResponse.Feature valueOf(Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Feature(int value) {
            this.value = value;
         }
      }

      public static final class File extends GeneratedMessageV3 implements PluginProtos.CodeGeneratorResponse.FileOrBuilder {
         private static final long serialVersionUID = 0L;
         private int bitField0_;
         public static final int NAME_FIELD_NUMBER = 1;
         private volatile Object name_;
         public static final int INSERTION_POINT_FIELD_NUMBER = 2;
         private volatile Object insertionPoint_;
         public static final int CONTENT_FIELD_NUMBER = 15;
         private volatile Object content_;
         public static final int GENERATED_CODE_INFO_FIELD_NUMBER = 16;
         private DescriptorProtos.GeneratedCodeInfo generatedCodeInfo_;
         private byte memoizedIsInitialized = -1;
         private static final PluginProtos.CodeGeneratorResponse.File DEFAULT_INSTANCE = new PluginProtos.CodeGeneratorResponse.File();
         @Deprecated
         public static final Parser<PluginProtos.CodeGeneratorResponse.File> PARSER = new AbstractParser<PluginProtos.CodeGeneratorResponse.File>() {
            public PluginProtos.CodeGeneratorResponse.File parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               return new PluginProtos.CodeGeneratorResponse.File(input, extensionRegistry);
            }
         };

         private File(GeneratedMessageV3.Builder<?> builder) {
            super(builder);
         }

         private File() {
            this.name_ = "";
            this.insertionPoint_ = "";
            this.content_ = "";
         }

         @Override
         protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
            return new PluginProtos.CodeGeneratorResponse.File();
         }

         @Override
         public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
         }

         private File(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
               throw new NullPointerException();
            } else {
               int mutable_bitField0_ = 0;
               UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

               try {
                  boolean done = false;

                  while(!done) {
                     int tag = input.readTag();
                     switch(tag) {
                        case 0:
                           done = true;
                           break;
                        case 10: {
                           ByteString bs = input.readBytes();
                           this.bitField0_ |= 1;
                           this.name_ = bs;
                           break;
                        }
                        case 18: {
                           ByteString bs = input.readBytes();
                           this.bitField0_ |= 2;
                           this.insertionPoint_ = bs;
                           break;
                        }
                        case 122: {
                           ByteString bs = input.readBytes();
                           this.bitField0_ |= 4;
                           this.content_ = bs;
                           break;
                        }
                        case 130:
                           DescriptorProtos.GeneratedCodeInfo.Builder subBuilder = null;
                           if ((this.bitField0_ & 8) != 0) {
                              subBuilder = this.generatedCodeInfo_.toBuilder();
                           }

                           this.generatedCodeInfo_ = input.readMessage(DescriptorProtos.GeneratedCodeInfo.PARSER, extensionRegistry);
                           if (subBuilder != null) {
                              subBuilder.mergeFrom(this.generatedCodeInfo_);
                              this.generatedCodeInfo_ = subBuilder.buildPartial();
                           }

                           this.bitField0_ |= 8;
                           break;
                        default:
                           if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                              done = true;
                           }
                     }
                  }
               } catch (InvalidProtocolBufferException var13) {
                  throw var13.setUnfinishedMessage(this);
               } catch (UninitializedMessageException var14) {
                  throw var14.asInvalidProtocolBufferException().setUnfinishedMessage(this);
               } catch (IOException var15) {
                  throw new InvalidProtocolBufferException(var15).setUnfinishedMessage(this);
               } finally {
                  this.unknownFields = unknownFields.build();
                  this.makeExtensionsImmutable();
               }

            }
         }

         public static final Descriptors.Descriptor getDescriptor() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_descriptor;
         }

         @Override
         protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_fieldAccessorTable
               .ensureFieldAccessorsInitialized(PluginProtos.CodeGeneratorResponse.File.class, PluginProtos.CodeGeneratorResponse.File.Builder.class);
         }

         @Override
         public boolean hasName() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public String getName() {
            Object ref = this.name_;
            if (ref instanceof String) {
               return (String)ref;
            } else {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.name_ = s;
               }

               return s;
            }
         }

         @Override
         public ByteString getNameBytes() {
            Object ref = this.name_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.name_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         @Override
         public boolean hasInsertionPoint() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public String getInsertionPoint() {
            Object ref = this.insertionPoint_;
            if (ref instanceof String) {
               return (String)ref;
            } else {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.insertionPoint_ = s;
               }

               return s;
            }
         }

         @Override
         public ByteString getInsertionPointBytes() {
            Object ref = this.insertionPoint_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.insertionPoint_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         @Override
         public boolean hasContent() {
            return (this.bitField0_ & 4) != 0;
         }

         @Override
         public String getContent() {
            Object ref = this.content_;
            if (ref instanceof String) {
               return (String)ref;
            } else {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.content_ = s;
               }

               return s;
            }
         }

         @Override
         public ByteString getContentBytes() {
            Object ref = this.content_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.content_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         @Override
         public boolean hasGeneratedCodeInfo() {
            return (this.bitField0_ & 8) != 0;
         }

         @Override
         public DescriptorProtos.GeneratedCodeInfo getGeneratedCodeInfo() {
            return this.generatedCodeInfo_ == null ? DescriptorProtos.GeneratedCodeInfo.getDefaultInstance() : this.generatedCodeInfo_;
         }

         @Override
         public DescriptorProtos.GeneratedCodeInfoOrBuilder getGeneratedCodeInfoOrBuilder() {
            return this.generatedCodeInfo_ == null ? DescriptorProtos.GeneratedCodeInfo.getDefaultInstance() : this.generatedCodeInfo_;
         }

         @Override
         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else {
               this.memoizedIsInitialized = 1;
               return true;
            }
         }

         @Override
         public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
               GeneratedMessageV3.writeString(output, 1, this.name_);
            }

            if ((this.bitField0_ & 2) != 0) {
               GeneratedMessageV3.writeString(output, 2, this.insertionPoint_);
            }

            if ((this.bitField0_ & 4) != 0) {
               GeneratedMessageV3.writeString(output, 15, this.content_);
            }

            if ((this.bitField0_ & 8) != 0) {
               output.writeMessage(16, this.getGeneratedCodeInfo());
            }

            this.unknownFields.writeTo(output);
         }

         @Override
         public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
               return size;
            } else {
               size = 0;
               if ((this.bitField0_ & 1) != 0) {
                  size += GeneratedMessageV3.computeStringSize(1, this.name_);
               }

               if ((this.bitField0_ & 2) != 0) {
                  size += GeneratedMessageV3.computeStringSize(2, this.insertionPoint_);
               }

               if ((this.bitField0_ & 4) != 0) {
                  size += GeneratedMessageV3.computeStringSize(15, this.content_);
               }

               if ((this.bitField0_ & 8) != 0) {
                  size += CodedOutputStream.computeMessageSize(16, this.getGeneratedCodeInfo());
               }

               size += this.unknownFields.getSerializedSize();
               this.memoizedSize = size;
               return size;
            }
         }

         @Override
         public boolean equals(Object obj) {
            if (obj == this) {
               return true;
            } else if (!(obj instanceof PluginProtos.CodeGeneratorResponse.File)) {
               return super.equals(obj);
            } else {
               PluginProtos.CodeGeneratorResponse.File other = (PluginProtos.CodeGeneratorResponse.File)obj;
               if (this.hasName() != other.hasName()) {
                  return false;
               } else if (this.hasName() && !this.getName().equals(other.getName())) {
                  return false;
               } else if (this.hasInsertionPoint() != other.hasInsertionPoint()) {
                  return false;
               } else if (this.hasInsertionPoint() && !this.getInsertionPoint().equals(other.getInsertionPoint())) {
                  return false;
               } else if (this.hasContent() != other.hasContent()) {
                  return false;
               } else if (this.hasContent() && !this.getContent().equals(other.getContent())) {
                  return false;
               } else if (this.hasGeneratedCodeInfo() != other.hasGeneratedCodeInfo()) {
                  return false;
               } else if (this.hasGeneratedCodeInfo() && !this.getGeneratedCodeInfo().equals(other.getGeneratedCodeInfo())) {
                  return false;
               } else {
                  return this.unknownFields.equals(other.unknownFields);
               }
            }
         }

         @Override
         public int hashCode() {
            if (this.memoizedHashCode != 0) {
               return this.memoizedHashCode;
            } else {
               int hash = 41;
               hash = 19 * hash + getDescriptor().hashCode();
               if (this.hasName()) {
                  hash = 37 * hash + 1;
                  hash = 53 * hash + this.getName().hashCode();
               }

               if (this.hasInsertionPoint()) {
                  hash = 37 * hash + 2;
                  hash = 53 * hash + this.getInsertionPoint().hashCode();
               }

               if (this.hasContent()) {
                  hash = 37 * hash + 15;
                  hash = 53 * hash + this.getContent().hashCode();
               }

               if (this.hasGeneratedCodeInfo()) {
                  hash = 37 * hash + 16;
                  hash = 53 * hash + this.getGeneratedCodeInfo().hashCode();
               }

               hash = 29 * hash + this.unknownFields.hashCode();
               this.memoizedHashCode = hash;
               return hash;
            }
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(InputStream input) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
         }

         public static PluginProtos.CodeGeneratorResponse.File parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public PluginProtos.CodeGeneratorResponse.File.Builder newBuilderForType() {
            return newBuilder();
         }

         public static PluginProtos.CodeGeneratorResponse.File.Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
         }

         public static PluginProtos.CodeGeneratorResponse.File.Builder newBuilder(PluginProtos.CodeGeneratorResponse.File prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
         }

         public PluginProtos.CodeGeneratorResponse.File.Builder toBuilder() {
            return this == DEFAULT_INSTANCE
               ? new PluginProtos.CodeGeneratorResponse.File.Builder()
               : new PluginProtos.CodeGeneratorResponse.File.Builder().mergeFrom(this);
         }

         protected PluginProtos.CodeGeneratorResponse.File.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
            return new PluginProtos.CodeGeneratorResponse.File.Builder(parent);
         }

         public static PluginProtos.CodeGeneratorResponse.File getDefaultInstance() {
            return DEFAULT_INSTANCE;
         }

         public static Parser<PluginProtos.CodeGeneratorResponse.File> parser() {
            return PARSER;
         }

         @Override
         public Parser<PluginProtos.CodeGeneratorResponse.File> getParserForType() {
            return PARSER;
         }

         public PluginProtos.CodeGeneratorResponse.File getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
         }

         public static final class Builder
            extends GeneratedMessageV3.Builder<PluginProtos.CodeGeneratorResponse.File.Builder>
            implements PluginProtos.CodeGeneratorResponse.FileOrBuilder {
            private int bitField0_;
            private Object name_ = "";
            private Object insertionPoint_ = "";
            private Object content_ = "";
            private DescriptorProtos.GeneratedCodeInfo generatedCodeInfo_;
            private SingleFieldBuilderV3<DescriptorProtos.GeneratedCodeInfo, DescriptorProtos.GeneratedCodeInfo.Builder, DescriptorProtos.GeneratedCodeInfoOrBuilder> generatedCodeInfoBuilder_;

            public static final Descriptors.Descriptor getDescriptor() {
               return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_descriptor;
            }

            @Override
            protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
               return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(PluginProtos.CodeGeneratorResponse.File.class, PluginProtos.CodeGeneratorResponse.File.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(GeneratedMessageV3.BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (PluginProtos.CodeGeneratorResponse.File.alwaysUseFieldBuilders) {
                  this.getGeneratedCodeInfoFieldBuilder();
               }

            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clear() {
               super.clear();
               this.name_ = "";
               this.bitField0_ &= -2;
               this.insertionPoint_ = "";
               this.bitField0_ &= -3;
               this.content_ = "";
               this.bitField0_ &= -5;
               if (this.generatedCodeInfoBuilder_ == null) {
                  this.generatedCodeInfo_ = null;
               } else {
                  this.generatedCodeInfoBuilder_.clear();
               }

               this.bitField0_ &= -9;
               return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
               return PluginProtos.internal_static_google_protobuf_compiler_CodeGeneratorResponse_File_descriptor;
            }

            public PluginProtos.CodeGeneratorResponse.File getDefaultInstanceForType() {
               return PluginProtos.CodeGeneratorResponse.File.getDefaultInstance();
            }

            public PluginProtos.CodeGeneratorResponse.File build() {
               PluginProtos.CodeGeneratorResponse.File result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File buildPartial() {
               PluginProtos.CodeGeneratorResponse.File result = new PluginProtos.CodeGeneratorResponse.File(this);
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) != 0) {
                  to_bitField0_ |= 1;
               }

               result.name_ = this.name_;
               if ((from_bitField0_ & 2) != 0) {
                  to_bitField0_ |= 2;
               }

               result.insertionPoint_ = this.insertionPoint_;
               if ((from_bitField0_ & 4) != 0) {
                  to_bitField0_ |= 4;
               }

               result.content_ = this.content_;
               if ((from_bitField0_ & 8) != 0) {
                  if (this.generatedCodeInfoBuilder_ == null) {
                     result.generatedCodeInfo_ = this.generatedCodeInfo_;
                  } else {
                     result.generatedCodeInfo_ = this.generatedCodeInfoBuilder_.build();
                  }

                  to_bitField0_ |= 8;
               }

               result.bitField0_ = to_bitField0_;
               this.onBuilt();
               return result;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clone() {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.clone();
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setField(Descriptors.FieldDescriptor field, Object value) {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.setField(field, value);
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clearField(Descriptors.FieldDescriptor field) {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.clearField(field);
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.clearOneof(oneof);
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.setRepeatedField(field, index, value);
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.addRepeatedField(field, value);
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder mergeFrom(Message other) {
               if (other instanceof PluginProtos.CodeGeneratorResponse.File) {
                  return this.mergeFrom((PluginProtos.CodeGeneratorResponse.File)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder mergeFrom(PluginProtos.CodeGeneratorResponse.File other) {
               if (other == PluginProtos.CodeGeneratorResponse.File.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasName()) {
                     this.bitField0_ |= 1;
                     this.name_ = other.name_;
                     this.onChanged();
                  }

                  if (other.hasInsertionPoint()) {
                     this.bitField0_ |= 2;
                     this.insertionPoint_ = other.insertionPoint_;
                     this.onChanged();
                  }

                  if (other.hasContent()) {
                     this.bitField0_ |= 4;
                     this.content_ = other.content_;
                     this.onChanged();
                  }

                  if (other.hasGeneratedCodeInfo()) {
                     this.mergeGeneratedCodeInfo(other.getGeneratedCodeInfo());
                  }

                  this.mergeUnknownFields(other.unknownFields);
                  this.onChanged();
                  return this;
               }
            }

            @Override
            public final boolean isInitialized() {
               return true;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               PluginProtos.CodeGeneratorResponse.File parsedMessage = null;

               try {
                  parsedMessage = PluginProtos.CodeGeneratorResponse.File.PARSER.parsePartialFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var8) {
                  parsedMessage = (PluginProtos.CodeGeneratorResponse.File)var8.getUnfinishedMessage();
                  throw var8.unwrapIOException();
               } finally {
                  if (parsedMessage != null) {
                     this.mergeFrom(parsedMessage);
                  }

               }

               return this;
            }

            @Override
            public boolean hasName() {
               return (this.bitField0_ & 1) != 0;
            }

            @Override
            public String getName() {
               Object ref = this.name_;
               if (!(ref instanceof String)) {
                  ByteString bs = (ByteString)ref;
                  String s = bs.toStringUtf8();
                  if (bs.isValidUtf8()) {
                     this.name_ = s;
                  }

                  return s;
               } else {
                  return (String)ref;
               }
            }

            @Override
            public ByteString getNameBytes() {
               Object ref = this.name_;
               if (ref instanceof String) {
                  ByteString b = ByteString.copyFromUtf8((String)ref);
                  this.name_ = b;
                  return b;
               } else {
                  return (ByteString)ref;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setName(String value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.name_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clearName() {
               this.bitField0_ &= -2;
               this.name_ = PluginProtos.CodeGeneratorResponse.File.getDefaultInstance().getName();
               this.onChanged();
               return this;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setNameBytes(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.name_ = value;
                  this.onChanged();
                  return this;
               }
            }

            @Override
            public boolean hasInsertionPoint() {
               return (this.bitField0_ & 2) != 0;
            }

            @Override
            public String getInsertionPoint() {
               Object ref = this.insertionPoint_;
               if (!(ref instanceof String)) {
                  ByteString bs = (ByteString)ref;
                  String s = bs.toStringUtf8();
                  if (bs.isValidUtf8()) {
                     this.insertionPoint_ = s;
                  }

                  return s;
               } else {
                  return (String)ref;
               }
            }

            @Override
            public ByteString getInsertionPointBytes() {
               Object ref = this.insertionPoint_;
               if (ref instanceof String) {
                  ByteString b = ByteString.copyFromUtf8((String)ref);
                  this.insertionPoint_ = b;
                  return b;
               } else {
                  return (ByteString)ref;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setInsertionPoint(String value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 2;
                  this.insertionPoint_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clearInsertionPoint() {
               this.bitField0_ &= -3;
               this.insertionPoint_ = PluginProtos.CodeGeneratorResponse.File.getDefaultInstance().getInsertionPoint();
               this.onChanged();
               return this;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setInsertionPointBytes(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 2;
                  this.insertionPoint_ = value;
                  this.onChanged();
                  return this;
               }
            }

            @Override
            public boolean hasContent() {
               return (this.bitField0_ & 4) != 0;
            }

            @Override
            public String getContent() {
               Object ref = this.content_;
               if (!(ref instanceof String)) {
                  ByteString bs = (ByteString)ref;
                  String s = bs.toStringUtf8();
                  if (bs.isValidUtf8()) {
                     this.content_ = s;
                  }

                  return s;
               } else {
                  return (String)ref;
               }
            }

            @Override
            public ByteString getContentBytes() {
               Object ref = this.content_;
               if (ref instanceof String) {
                  ByteString b = ByteString.copyFromUtf8((String)ref);
                  this.content_ = b;
                  return b;
               } else {
                  return (ByteString)ref;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setContent(String value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 4;
                  this.content_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clearContent() {
               this.bitField0_ &= -5;
               this.content_ = PluginProtos.CodeGeneratorResponse.File.getDefaultInstance().getContent();
               this.onChanged();
               return this;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setContentBytes(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 4;
                  this.content_ = value;
                  this.onChanged();
                  return this;
               }
            }

            @Override
            public boolean hasGeneratedCodeInfo() {
               return (this.bitField0_ & 8) != 0;
            }

            @Override
            public DescriptorProtos.GeneratedCodeInfo getGeneratedCodeInfo() {
               if (this.generatedCodeInfoBuilder_ == null) {
                  return this.generatedCodeInfo_ == null ? DescriptorProtos.GeneratedCodeInfo.getDefaultInstance() : this.generatedCodeInfo_;
               } else {
                  return this.generatedCodeInfoBuilder_.getMessage();
               }
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setGeneratedCodeInfo(DescriptorProtos.GeneratedCodeInfo value) {
               if (this.generatedCodeInfoBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.generatedCodeInfo_ = value;
                  this.onChanged();
               } else {
                  this.generatedCodeInfoBuilder_.setMessage(value);
               }

               this.bitField0_ |= 8;
               return this;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder setGeneratedCodeInfo(DescriptorProtos.GeneratedCodeInfo.Builder builderForValue) {
               if (this.generatedCodeInfoBuilder_ == null) {
                  this.generatedCodeInfo_ = builderForValue.build();
                  this.onChanged();
               } else {
                  this.generatedCodeInfoBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 8;
               return this;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder mergeGeneratedCodeInfo(DescriptorProtos.GeneratedCodeInfo value) {
               if (this.generatedCodeInfoBuilder_ == null) {
                  if ((this.bitField0_ & 8) != 0
                     && this.generatedCodeInfo_ != null
                     && this.generatedCodeInfo_ != DescriptorProtos.GeneratedCodeInfo.getDefaultInstance()) {
                     this.generatedCodeInfo_ = DescriptorProtos.GeneratedCodeInfo.newBuilder(this.generatedCodeInfo_).mergeFrom(value).buildPartial();
                  } else {
                     this.generatedCodeInfo_ = value;
                  }

                  this.onChanged();
               } else {
                  this.generatedCodeInfoBuilder_.mergeFrom(value);
               }

               this.bitField0_ |= 8;
               return this;
            }

            public PluginProtos.CodeGeneratorResponse.File.Builder clearGeneratedCodeInfo() {
               if (this.generatedCodeInfoBuilder_ == null) {
                  this.generatedCodeInfo_ = null;
                  this.onChanged();
               } else {
                  this.generatedCodeInfoBuilder_.clear();
               }

               this.bitField0_ &= -9;
               return this;
            }

            public DescriptorProtos.GeneratedCodeInfo.Builder getGeneratedCodeInfoBuilder() {
               this.bitField0_ |= 8;
               this.onChanged();
               return this.getGeneratedCodeInfoFieldBuilder().getBuilder();
            }

            @Override
            public DescriptorProtos.GeneratedCodeInfoOrBuilder getGeneratedCodeInfoOrBuilder() {
               if (this.generatedCodeInfoBuilder_ != null) {
                  return this.generatedCodeInfoBuilder_.getMessageOrBuilder();
               } else {
                  return this.generatedCodeInfo_ == null ? DescriptorProtos.GeneratedCodeInfo.getDefaultInstance() : this.generatedCodeInfo_;
               }
            }

            private SingleFieldBuilderV3<DescriptorProtos.GeneratedCodeInfo, DescriptorProtos.GeneratedCodeInfo.Builder, DescriptorProtos.GeneratedCodeInfoOrBuilder> getGeneratedCodeInfoFieldBuilder() {
               if (this.generatedCodeInfoBuilder_ == null) {
                  this.generatedCodeInfoBuilder_ = new SingleFieldBuilderV3<>(this.getGeneratedCodeInfo(), this.getParentForChildren(), this.isClean());
                  this.generatedCodeInfo_ = null;
               }

               return this.generatedCodeInfoBuilder_;
            }

            public final PluginProtos.CodeGeneratorResponse.File.Builder setUnknownFields(UnknownFieldSet unknownFields) {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.setUnknownFields(unknownFields);
            }

            public final PluginProtos.CodeGeneratorResponse.File.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
               return (PluginProtos.CodeGeneratorResponse.File.Builder)super.mergeUnknownFields(unknownFields);
            }
         }
      }

      public interface FileOrBuilder extends MessageOrBuilder {
         boolean hasName();

         String getName();

         ByteString getNameBytes();

         boolean hasInsertionPoint();

         String getInsertionPoint();

         ByteString getInsertionPointBytes();

         boolean hasContent();

         String getContent();

         ByteString getContentBytes();

         boolean hasGeneratedCodeInfo();

         DescriptorProtos.GeneratedCodeInfo getGeneratedCodeInfo();

         DescriptorProtos.GeneratedCodeInfoOrBuilder getGeneratedCodeInfoOrBuilder();
      }
   }

   public interface CodeGeneratorResponseOrBuilder extends MessageOrBuilder {
      boolean hasError();

      String getError();

      ByteString getErrorBytes();

      boolean hasSupportedFeatures();

      long getSupportedFeatures();

      List<PluginProtos.CodeGeneratorResponse.File> getFileList();

      PluginProtos.CodeGeneratorResponse.File getFile(int var1);

      int getFileCount();

      List<? extends PluginProtos.CodeGeneratorResponse.FileOrBuilder> getFileOrBuilderList();

      PluginProtos.CodeGeneratorResponse.FileOrBuilder getFileOrBuilder(int var1);
   }

   public static final class Version extends GeneratedMessageV3 implements PluginProtos.VersionOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int MAJOR_FIELD_NUMBER = 1;
      private int major_;
      public static final int MINOR_FIELD_NUMBER = 2;
      private int minor_;
      public static final int PATCH_FIELD_NUMBER = 3;
      private int patch_;
      public static final int SUFFIX_FIELD_NUMBER = 4;
      private volatile Object suffix_;
      private byte memoizedIsInitialized = -1;
      private static final PluginProtos.Version DEFAULT_INSTANCE = new PluginProtos.Version();
      @Deprecated
      public static final Parser<PluginProtos.Version> PARSER = new AbstractParser<PluginProtos.Version>() {
         public PluginProtos.Version parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new PluginProtos.Version(input, extensionRegistry);
         }
      };

      private Version(GeneratedMessageV3.Builder<?> builder) {
         super(builder);
      }

      private Version() {
         this.suffix_ = "";
      }

      @Override
      protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
         return new PluginProtos.Version();
      }

      @Override
      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Version(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         this();
         if (extensionRegistry == null) {
            throw new NullPointerException();
         } else {
            int mutable_bitField0_ = 0;
            UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

            try {
               boolean done = false;

               while(!done) {
                  int tag = input.readTag();
                  switch(tag) {
                     case 0:
                        done = true;
                        break;
                     case 8:
                        this.bitField0_ |= 1;
                        this.major_ = input.readInt32();
                        break;
                     case 16:
                        this.bitField0_ |= 2;
                        this.minor_ = input.readInt32();
                        break;
                     case 24:
                        this.bitField0_ |= 4;
                        this.patch_ = input.readInt32();
                        break;
                     case 34:
                        ByteString bs = input.readBytes();
                        this.bitField0_ |= 8;
                        this.suffix_ = bs;
                        break;
                     default:
                        if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                           done = true;
                        }
                  }
               }
            } catch (InvalidProtocolBufferException var13) {
               throw var13.setUnfinishedMessage(this);
            } catch (UninitializedMessageException var14) {
               throw var14.asInvalidProtocolBufferException().setUnfinishedMessage(this);
            } catch (IOException var15) {
               throw new InvalidProtocolBufferException(var15).setUnfinishedMessage(this);
            } finally {
               this.unknownFields = unknownFields.build();
               this.makeExtensionsImmutable();
            }

         }
      }

      public static final Descriptors.Descriptor getDescriptor() {
         return PluginProtos.internal_static_google_protobuf_compiler_Version_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return PluginProtos.internal_static_google_protobuf_compiler_Version_fieldAccessorTable
            .ensureFieldAccessorsInitialized(PluginProtos.Version.class, PluginProtos.Version.Builder.class);
      }

      @Override
      public boolean hasMajor() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public int getMajor() {
         return this.major_;
      }

      @Override
      public boolean hasMinor() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public int getMinor() {
         return this.minor_;
      }

      @Override
      public boolean hasPatch() {
         return (this.bitField0_ & 4) != 0;
      }

      @Override
      public int getPatch() {
         return this.patch_;
      }

      @Override
      public boolean hasSuffix() {
         return (this.bitField0_ & 8) != 0;
      }

      @Override
      public String getSuffix() {
         Object ref = this.suffix_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.suffix_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getSuffixBytes() {
         Object ref = this.suffix_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.suffix_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      @Override
      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            output.writeInt32(1, this.major_);
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeInt32(2, this.minor_);
         }

         if ((this.bitField0_ & 4) != 0) {
            output.writeInt32(3, this.patch_);
         }

         if ((this.bitField0_ & 8) != 0) {
            GeneratedMessageV3.writeString(output, 4, this.suffix_);
         }

         this.unknownFields.writeTo(output);
      }

      @Override
      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
               size += CodedOutputStream.computeInt32Size(1, this.major_);
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeInt32Size(2, this.minor_);
            }

            if ((this.bitField0_ & 4) != 0) {
               size += CodedOutputStream.computeInt32Size(3, this.patch_);
            }

            if ((this.bitField0_ & 8) != 0) {
               size += GeneratedMessageV3.computeStringSize(4, this.suffix_);
            }

            size += this.unknownFields.getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof PluginProtos.Version)) {
            return super.equals(obj);
         } else {
            PluginProtos.Version other = (PluginProtos.Version)obj;
            if (this.hasMajor() != other.hasMajor()) {
               return false;
            } else if (this.hasMajor() && this.getMajor() != other.getMajor()) {
               return false;
            } else if (this.hasMinor() != other.hasMinor()) {
               return false;
            } else if (this.hasMinor() && this.getMinor() != other.getMinor()) {
               return false;
            } else if (this.hasPatch() != other.hasPatch()) {
               return false;
            } else if (this.hasPatch() && this.getPatch() != other.getPatch()) {
               return false;
            } else if (this.hasSuffix() != other.hasSuffix()) {
               return false;
            } else if (this.hasSuffix() && !this.getSuffix().equals(other.getSuffix())) {
               return false;
            } else {
               return this.unknownFields.equals(other.unknownFields);
            }
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            if (this.hasMajor()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getMajor();
            }

            if (this.hasMinor()) {
               hash = 37 * hash + 2;
               hash = 53 * hash + this.getMinor();
            }

            if (this.hasPatch()) {
               hash = 37 * hash + 3;
               hash = 53 * hash + this.getPatch();
            }

            if (this.hasSuffix()) {
               hash = 37 * hash + 4;
               hash = 53 * hash + this.getSuffix().hashCode();
            }

            hash = 29 * hash + this.unknownFields.hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static PluginProtos.Version parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.Version parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.Version parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.Version parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.Version parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data);
      }

      public static PluginProtos.Version parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return PARSER.parseFrom(data, extensionRegistry);
      }

      public static PluginProtos.Version parseFrom(InputStream input) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input);
      }

      public static PluginProtos.Version parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static PluginProtos.Version parseDelimitedFrom(InputStream input) throws IOException {
         return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
      }

      public static PluginProtos.Version parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static PluginProtos.Version parseFrom(CodedInputStream input) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input);
      }

      public static PluginProtos.Version parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public PluginProtos.Version.Builder newBuilderForType() {
         return newBuilder();
      }

      public static PluginProtos.Version.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static PluginProtos.Version.Builder newBuilder(PluginProtos.Version prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public PluginProtos.Version.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new PluginProtos.Version.Builder() : new PluginProtos.Version.Builder().mergeFrom(this);
      }

      protected PluginProtos.Version.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
         return new PluginProtos.Version.Builder(parent);
      }

      public static PluginProtos.Version getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<PluginProtos.Version> parser() {
         return PARSER;
      }

      @Override
      public Parser<PluginProtos.Version> getParserForType() {
         return PARSER;
      }

      public PluginProtos.Version getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      public static final class Builder extends GeneratedMessageV3.Builder<PluginProtos.Version.Builder> implements PluginProtos.VersionOrBuilder {
         private int bitField0_;
         private int major_;
         private int minor_;
         private int patch_;
         private Object suffix_ = "";

         public static final Descriptors.Descriptor getDescriptor() {
            return PluginProtos.internal_static_google_protobuf_compiler_Version_descriptor;
         }

         @Override
         protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return PluginProtos.internal_static_google_protobuf_compiler_Version_fieldAccessorTable
               .ensureFieldAccessorsInitialized(PluginProtos.Version.class, PluginProtos.Version.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(GeneratedMessageV3.BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (PluginProtos.Version.alwaysUseFieldBuilders) {
            }

         }

         public PluginProtos.Version.Builder clear() {
            super.clear();
            this.major_ = 0;
            this.bitField0_ &= -2;
            this.minor_ = 0;
            this.bitField0_ &= -3;
            this.patch_ = 0;
            this.bitField0_ &= -5;
            this.suffix_ = "";
            this.bitField0_ &= -9;
            return this;
         }

         @Override
         public Descriptors.Descriptor getDescriptorForType() {
            return PluginProtos.internal_static_google_protobuf_compiler_Version_descriptor;
         }

         public PluginProtos.Version getDefaultInstanceForType() {
            return PluginProtos.Version.getDefaultInstance();
         }

         public PluginProtos.Version build() {
            PluginProtos.Version result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public PluginProtos.Version buildPartial() {
            PluginProtos.Version result = new PluginProtos.Version(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.major_ = this.major_;
               to_bitField0_ |= 1;
            }

            if ((from_bitField0_ & 2) != 0) {
               result.minor_ = this.minor_;
               to_bitField0_ |= 2;
            }

            if ((from_bitField0_ & 4) != 0) {
               result.patch_ = this.patch_;
               to_bitField0_ |= 4;
            }

            if ((from_bitField0_ & 8) != 0) {
               to_bitField0_ |= 8;
            }

            result.suffix_ = this.suffix_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public PluginProtos.Version.Builder clone() {
            return (PluginProtos.Version.Builder)super.clone();
         }

         public PluginProtos.Version.Builder setField(Descriptors.FieldDescriptor field, Object value) {
            return (PluginProtos.Version.Builder)super.setField(field, value);
         }

         public PluginProtos.Version.Builder clearField(Descriptors.FieldDescriptor field) {
            return (PluginProtos.Version.Builder)super.clearField(field);
         }

         public PluginProtos.Version.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
            return (PluginProtos.Version.Builder)super.clearOneof(oneof);
         }

         public PluginProtos.Version.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
            return (PluginProtos.Version.Builder)super.setRepeatedField(field, index, value);
         }

         public PluginProtos.Version.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
            return (PluginProtos.Version.Builder)super.addRepeatedField(field, value);
         }

         public PluginProtos.Version.Builder mergeFrom(Message other) {
            if (other instanceof PluginProtos.Version) {
               return this.mergeFrom((PluginProtos.Version)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public PluginProtos.Version.Builder mergeFrom(PluginProtos.Version other) {
            if (other == PluginProtos.Version.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasMajor()) {
                  this.setMajor(other.getMajor());
               }

               if (other.hasMinor()) {
                  this.setMinor(other.getMinor());
               }

               if (other.hasPatch()) {
                  this.setPatch(other.getPatch());
               }

               if (other.hasSuffix()) {
                  this.bitField0_ |= 8;
                  this.suffix_ = other.suffix_;
                  this.onChanged();
               }

               this.mergeUnknownFields(other.unknownFields);
               this.onChanged();
               return this;
            }
         }

         @Override
         public final boolean isInitialized() {
            return true;
         }

         public PluginProtos.Version.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            PluginProtos.Version parsedMessage = null;

            try {
               parsedMessage = PluginProtos.Version.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (PluginProtos.Version)var8.getUnfinishedMessage();
               throw var8.unwrapIOException();
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }

            }

            return this;
         }

         @Override
         public boolean hasMajor() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public int getMajor() {
            return this.major_;
         }

         public PluginProtos.Version.Builder setMajor(int value) {
            this.bitField0_ |= 1;
            this.major_ = value;
            this.onChanged();
            return this;
         }

         public PluginProtos.Version.Builder clearMajor() {
            this.bitField0_ &= -2;
            this.major_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasMinor() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public int getMinor() {
            return this.minor_;
         }

         public PluginProtos.Version.Builder setMinor(int value) {
            this.bitField0_ |= 2;
            this.minor_ = value;
            this.onChanged();
            return this;
         }

         public PluginProtos.Version.Builder clearMinor() {
            this.bitField0_ &= -3;
            this.minor_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasPatch() {
            return (this.bitField0_ & 4) != 0;
         }

         @Override
         public int getPatch() {
            return this.patch_;
         }

         public PluginProtos.Version.Builder setPatch(int value) {
            this.bitField0_ |= 4;
            this.patch_ = value;
            this.onChanged();
            return this;
         }

         public PluginProtos.Version.Builder clearPatch() {
            this.bitField0_ &= -5;
            this.patch_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasSuffix() {
            return (this.bitField0_ & 8) != 0;
         }

         @Override
         public String getSuffix() {
            Object ref = this.suffix_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.suffix_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getSuffixBytes() {
            Object ref = this.suffix_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.suffix_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public PluginProtos.Version.Builder setSuffix(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 8;
               this.suffix_ = value;
               this.onChanged();
               return this;
            }
         }

         public PluginProtos.Version.Builder clearSuffix() {
            this.bitField0_ &= -9;
            this.suffix_ = PluginProtos.Version.getDefaultInstance().getSuffix();
            this.onChanged();
            return this;
         }

         public PluginProtos.Version.Builder setSuffixBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 8;
               this.suffix_ = value;
               this.onChanged();
               return this;
            }
         }

         public final PluginProtos.Version.Builder setUnknownFields(UnknownFieldSet unknownFields) {
            return (PluginProtos.Version.Builder)super.setUnknownFields(unknownFields);
         }

         public final PluginProtos.Version.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
            return (PluginProtos.Version.Builder)super.mergeUnknownFields(unknownFields);
         }
      }
   }

   public interface VersionOrBuilder extends MessageOrBuilder {
      boolean hasMajor();

      int getMajor();

      boolean hasMinor();

      int getMinor();

      boolean hasPatch();

      int getPatch();

      boolean hasSuffix();

      String getSuffix();

      ByteString getSuffixBytes();
   }
}
