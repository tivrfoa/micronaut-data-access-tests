package com.google.protobuf;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class Descriptors {
   private static final Logger logger = Logger.getLogger(Descriptors.class.getName());
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   private static final Descriptors.Descriptor[] EMPTY_DESCRIPTORS = new Descriptors.Descriptor[0];
   private static final Descriptors.FieldDescriptor[] EMPTY_FIELD_DESCRIPTORS = new Descriptors.FieldDescriptor[0];
   private static final Descriptors.EnumDescriptor[] EMPTY_ENUM_DESCRIPTORS = new Descriptors.EnumDescriptor[0];
   private static final Descriptors.ServiceDescriptor[] EMPTY_SERVICE_DESCRIPTORS = new Descriptors.ServiceDescriptor[0];
   private static final Descriptors.OneofDescriptor[] EMPTY_ONEOF_DESCRIPTORS = new Descriptors.OneofDescriptor[0];

   private static String computeFullName(Descriptors.FileDescriptor file, Descriptors.Descriptor parent, String name) {
      if (parent != null) {
         return parent.getFullName() + '.' + name;
      } else {
         String packageName = file.getPackage();
         return !packageName.isEmpty() ? packageName + '.' + name : name;
      }
   }

   private static <T> T binarySearch(T[] array, int size, Descriptors.NumberGetter<T> getter, int number) {
      int left = 0;
      int right = size - 1;

      while(left <= right) {
         int mid = (left + right) / 2;
         T midValue = array[mid];
         int midValueNumber = getter.getNumber(midValue);
         if (number < midValueNumber) {
            right = mid - 1;
         } else {
            if (number <= midValueNumber) {
               return midValue;
            }

            left = mid + 1;
         }
      }

      return null;
   }

   public static final class Descriptor extends Descriptors.GenericDescriptor {
      private final int index;
      private DescriptorProtos.DescriptorProto proto;
      private final String fullName;
      private final Descriptors.FileDescriptor file;
      private final Descriptors.Descriptor containingType;
      private final Descriptors.Descriptor[] nestedTypes;
      private final Descriptors.EnumDescriptor[] enumTypes;
      private final Descriptors.FieldDescriptor[] fields;
      private final Descriptors.FieldDescriptor[] fieldsSortedByNumber;
      private final Descriptors.FieldDescriptor[] extensions;
      private final Descriptors.OneofDescriptor[] oneofs;
      private final int realOneofCount;
      private final int[] extensionRangeLowerBounds;
      private final int[] extensionRangeUpperBounds;

      public int getIndex() {
         return this.index;
      }

      public DescriptorProtos.DescriptorProto toProto() {
         return this.proto;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public String getFullName() {
         return this.fullName;
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this.file;
      }

      public Descriptors.Descriptor getContainingType() {
         return this.containingType;
      }

      public DescriptorProtos.MessageOptions getOptions() {
         return this.proto.getOptions();
      }

      public List<Descriptors.FieldDescriptor> getFields() {
         return Collections.unmodifiableList(Arrays.asList(this.fields));
      }

      public List<Descriptors.OneofDescriptor> getOneofs() {
         return Collections.unmodifiableList(Arrays.asList(this.oneofs));
      }

      public List<Descriptors.OneofDescriptor> getRealOneofs() {
         return Collections.unmodifiableList(Arrays.asList(this.oneofs).subList(0, this.realOneofCount));
      }

      public List<Descriptors.FieldDescriptor> getExtensions() {
         return Collections.unmodifiableList(Arrays.asList(this.extensions));
      }

      public List<Descriptors.Descriptor> getNestedTypes() {
         return Collections.unmodifiableList(Arrays.asList(this.nestedTypes));
      }

      public List<Descriptors.EnumDescriptor> getEnumTypes() {
         return Collections.unmodifiableList(Arrays.asList(this.enumTypes));
      }

      public boolean isExtensionNumber(int number) {
         int index = Arrays.binarySearch(this.extensionRangeLowerBounds, number);
         if (index < 0) {
            index = ~index - 1;
         }

         return index >= 0 && number < this.extensionRangeUpperBounds[index];
      }

      public boolean isReservedNumber(int number) {
         for(DescriptorProtos.DescriptorProto.ReservedRange range : this.proto.getReservedRangeList()) {
            if (range.getStart() <= number && number < range.getEnd()) {
               return true;
            }
         }

         return false;
      }

      public boolean isReservedName(String name) {
         Internal.checkNotNull(name);

         for(String reservedName : this.proto.getReservedNameList()) {
            if (reservedName.equals(name)) {
               return true;
            }
         }

         return false;
      }

      public boolean isExtendable() {
         return !this.proto.getExtensionRangeList().isEmpty();
      }

      public Descriptors.FieldDescriptor findFieldByName(String name) {
         Descriptors.GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
         return result instanceof Descriptors.FieldDescriptor ? (Descriptors.FieldDescriptor)result : null;
      }

      public Descriptors.FieldDescriptor findFieldByNumber(int number) {
         return Descriptors.binarySearch(this.fieldsSortedByNumber, this.fieldsSortedByNumber.length, Descriptors.FieldDescriptor.NUMBER_GETTER, number);
      }

      public Descriptors.Descriptor findNestedTypeByName(String name) {
         Descriptors.GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
         return result instanceof Descriptors.Descriptor ? (Descriptors.Descriptor)result : null;
      }

      public Descriptors.EnumDescriptor findEnumTypeByName(String name) {
         Descriptors.GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
         return result instanceof Descriptors.EnumDescriptor ? (Descriptors.EnumDescriptor)result : null;
      }

      Descriptor(String fullname) throws Descriptors.DescriptorValidationException {
         String name = fullname;
         String packageName = "";
         int pos = fullname.lastIndexOf(46);
         if (pos != -1) {
            name = fullname.substring(pos + 1);
            packageName = fullname.substring(0, pos);
         }

         this.index = 0;
         this.proto = DescriptorProtos.DescriptorProto.newBuilder()
            .setName(name)
            .addExtensionRange(DescriptorProtos.DescriptorProto.ExtensionRange.newBuilder().setStart(1).setEnd(536870912).build())
            .build();
         this.fullName = fullname;
         this.containingType = null;
         this.nestedTypes = Descriptors.EMPTY_DESCRIPTORS;
         this.enumTypes = Descriptors.EMPTY_ENUM_DESCRIPTORS;
         this.fields = Descriptors.EMPTY_FIELD_DESCRIPTORS;
         this.fieldsSortedByNumber = Descriptors.EMPTY_FIELD_DESCRIPTORS;
         this.extensions = Descriptors.EMPTY_FIELD_DESCRIPTORS;
         this.oneofs = Descriptors.EMPTY_ONEOF_DESCRIPTORS;
         this.realOneofCount = 0;
         this.file = new Descriptors.FileDescriptor(packageName, this);
         this.extensionRangeLowerBounds = new int[]{1};
         this.extensionRangeUpperBounds = new int[]{536870912};
      }

      private Descriptor(DescriptorProtos.DescriptorProto proto, Descriptors.FileDescriptor file, Descriptors.Descriptor parent, int index) throws Descriptors.DescriptorValidationException {
         this.index = index;
         this.proto = proto;
         this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
         this.file = file;
         this.containingType = parent;
         this.oneofs = proto.getOneofDeclCount() > 0 ? new Descriptors.OneofDescriptor[proto.getOneofDeclCount()] : Descriptors.EMPTY_ONEOF_DESCRIPTORS;

         for(int i = 0; i < proto.getOneofDeclCount(); ++i) {
            this.oneofs[i] = new Descriptors.OneofDescriptor(proto.getOneofDecl(i), file, this, i);
         }

         this.nestedTypes = proto.getNestedTypeCount() > 0 ? new Descriptors.Descriptor[proto.getNestedTypeCount()] : Descriptors.EMPTY_DESCRIPTORS;

         for(int i = 0; i < proto.getNestedTypeCount(); ++i) {
            this.nestedTypes[i] = new Descriptors.Descriptor(proto.getNestedType(i), file, this, i);
         }

         this.enumTypes = proto.getEnumTypeCount() > 0 ? new Descriptors.EnumDescriptor[proto.getEnumTypeCount()] : Descriptors.EMPTY_ENUM_DESCRIPTORS;

         for(int i = 0; i < proto.getEnumTypeCount(); ++i) {
            this.enumTypes[i] = new Descriptors.EnumDescriptor(proto.getEnumType(i), file, this, i);
         }

         this.fields = proto.getFieldCount() > 0 ? new Descriptors.FieldDescriptor[proto.getFieldCount()] : Descriptors.EMPTY_FIELD_DESCRIPTORS;

         for(int i = 0; i < proto.getFieldCount(); ++i) {
            this.fields[i] = new Descriptors.FieldDescriptor(proto.getField(i), file, this, i, false);
         }

         this.fieldsSortedByNumber = proto.getFieldCount() > 0 ? (Descriptors.FieldDescriptor[])this.fields.clone() : Descriptors.EMPTY_FIELD_DESCRIPTORS;
         this.extensions = proto.getExtensionCount() > 0 ? new Descriptors.FieldDescriptor[proto.getExtensionCount()] : Descriptors.EMPTY_FIELD_DESCRIPTORS;

         for(int i = 0; i < proto.getExtensionCount(); ++i) {
            this.extensions[i] = new Descriptors.FieldDescriptor(proto.getExtension(i), file, this, i, true);
         }

         for(int i = 0; i < proto.getOneofDeclCount(); ++i) {
            this.oneofs[i].fields = new Descriptors.FieldDescriptor[this.oneofs[i].getFieldCount()];
            this.oneofs[i].fieldCount = 0;
         }

         for(int i = 0; i < proto.getFieldCount(); ++i) {
            Descriptors.OneofDescriptor oneofDescriptor = this.fields[i].getContainingOneof();
            if (oneofDescriptor != null) {
               oneofDescriptor.fields[oneofDescriptor.fieldCount++] = this.fields[i];
            }
         }

         int syntheticOneofCount = 0;

         for(Descriptors.OneofDescriptor oneof : this.oneofs) {
            if (oneof.isSynthetic()) {
               ++syntheticOneofCount;
            } else if (syntheticOneofCount > 0) {
               throw new Descriptors.DescriptorValidationException(this, "Synthetic oneofs must come last.");
            }
         }

         this.realOneofCount = this.oneofs.length - syntheticOneofCount;
         file.pool.addSymbol(this);
         if (proto.getExtensionRangeCount() > 0) {
            this.extensionRangeLowerBounds = new int[proto.getExtensionRangeCount()];
            this.extensionRangeUpperBounds = new int[proto.getExtensionRangeCount()];
            int i = 0;

            for(DescriptorProtos.DescriptorProto.ExtensionRange range : proto.getExtensionRangeList()) {
               this.extensionRangeLowerBounds[i] = range.getStart();
               this.extensionRangeUpperBounds[i] = range.getEnd();
               ++i;
            }

            Arrays.sort(this.extensionRangeLowerBounds);
            Arrays.sort(this.extensionRangeUpperBounds);
         } else {
            this.extensionRangeLowerBounds = Descriptors.EMPTY_INT_ARRAY;
            this.extensionRangeUpperBounds = Descriptors.EMPTY_INT_ARRAY;
         }

      }

      private void crossLink() throws Descriptors.DescriptorValidationException {
         for(Descriptors.Descriptor nestedType : this.nestedTypes) {
            nestedType.crossLink();
         }

         for(Descriptors.FieldDescriptor field : this.fields) {
            field.crossLink();
         }

         Arrays.sort(this.fieldsSortedByNumber);
         this.validateNoDuplicateFieldNumbers();

         for(Descriptors.FieldDescriptor extension : this.extensions) {
            extension.crossLink();
         }

      }

      private void validateNoDuplicateFieldNumbers() throws Descriptors.DescriptorValidationException {
         for(int i = 0; i + 1 < this.fieldsSortedByNumber.length; ++i) {
            Descriptors.FieldDescriptor old = this.fieldsSortedByNumber[i];
            Descriptors.FieldDescriptor field = this.fieldsSortedByNumber[i + 1];
            if (old.getNumber() == field.getNumber()) {
               throw new Descriptors.DescriptorValidationException(
                  field,
                  "Field number "
                     + field.getNumber()
                     + " has already been used in \""
                     + field.getContainingType().getFullName()
                     + "\" by field \""
                     + old.getName()
                     + "\"."
               );
            }
         }

      }

      private void setProto(DescriptorProtos.DescriptorProto proto) {
         this.proto = proto;

         for(int i = 0; i < this.nestedTypes.length; ++i) {
            this.nestedTypes[i].setProto(proto.getNestedType(i));
         }

         for(int i = 0; i < this.oneofs.length; ++i) {
            this.oneofs[i].setProto(proto.getOneofDecl(i));
         }

         for(int i = 0; i < this.enumTypes.length; ++i) {
            this.enumTypes[i].setProto(proto.getEnumType(i));
         }

         for(int i = 0; i < this.fields.length; ++i) {
            this.fields[i].setProto(proto.getField(i));
         }

         for(int i = 0; i < this.extensions.length; ++i) {
            this.extensions[i].setProto(proto.getExtension(i));
         }

      }
   }

   private static final class DescriptorPool {
      private final Set<Descriptors.FileDescriptor> dependencies;
      private final boolean allowUnknownDependencies;
      private final Map<String, Descriptors.GenericDescriptor> descriptorsByName = new HashMap();

      DescriptorPool(Descriptors.FileDescriptor[] dependencies, boolean allowUnknownDependencies) {
         this.dependencies = Collections.newSetFromMap(new IdentityHashMap(dependencies.length));
         this.allowUnknownDependencies = allowUnknownDependencies;

         for(Descriptors.FileDescriptor dependency : dependencies) {
            this.dependencies.add(dependency);
            this.importPublicDependencies(dependency);
         }

         for(Descriptors.FileDescriptor dependency : this.dependencies) {
            try {
               this.addPackage(dependency.getPackage(), dependency);
            } catch (Descriptors.DescriptorValidationException var7) {
               throw new AssertionError(var7);
            }
         }

      }

      private void importPublicDependencies(Descriptors.FileDescriptor file) {
         for(Descriptors.FileDescriptor dependency : file.getPublicDependencies()) {
            if (this.dependencies.add(dependency)) {
               this.importPublicDependencies(dependency);
            }
         }

      }

      Descriptors.GenericDescriptor findSymbol(String fullName) {
         return this.findSymbol(fullName, Descriptors.DescriptorPool.SearchFilter.ALL_SYMBOLS);
      }

      Descriptors.GenericDescriptor findSymbol(String fullName, Descriptors.DescriptorPool.SearchFilter filter) {
         Descriptors.GenericDescriptor result = (Descriptors.GenericDescriptor)this.descriptorsByName.get(fullName);
         if (result == null
            || filter != Descriptors.DescriptorPool.SearchFilter.ALL_SYMBOLS
               && (filter != Descriptors.DescriptorPool.SearchFilter.TYPES_ONLY || !this.isType(result))
               && (filter != Descriptors.DescriptorPool.SearchFilter.AGGREGATES_ONLY || !this.isAggregate(result))) {
            for(Descriptors.FileDescriptor dependency : this.dependencies) {
               result = (Descriptors.GenericDescriptor)dependency.pool.descriptorsByName.get(fullName);
               if (result != null
                  && (
                     filter == Descriptors.DescriptorPool.SearchFilter.ALL_SYMBOLS
                        || filter == Descriptors.DescriptorPool.SearchFilter.TYPES_ONLY && this.isType(result)
                        || filter == Descriptors.DescriptorPool.SearchFilter.AGGREGATES_ONLY && this.isAggregate(result)
                  )) {
                  return result;
               }
            }

            return null;
         } else {
            return result;
         }
      }

      boolean isType(Descriptors.GenericDescriptor descriptor) {
         return descriptor instanceof Descriptors.Descriptor || descriptor instanceof Descriptors.EnumDescriptor;
      }

      boolean isAggregate(Descriptors.GenericDescriptor descriptor) {
         return descriptor instanceof Descriptors.Descriptor
            || descriptor instanceof Descriptors.EnumDescriptor
            || descriptor instanceof Descriptors.DescriptorPool.PackageDescriptor
            || descriptor instanceof Descriptors.ServiceDescriptor;
      }

      Descriptors.GenericDescriptor lookupSymbol(String name, Descriptors.GenericDescriptor relativeTo, Descriptors.DescriptorPool.SearchFilter filter) throws Descriptors.DescriptorValidationException {
         Descriptors.GenericDescriptor result;
         String fullname;
         if (name.startsWith(".")) {
            fullname = name.substring(1);
            result = this.findSymbol(fullname, filter);
         } else {
            int firstPartLength = name.indexOf(46);
            String firstPart;
            if (firstPartLength == -1) {
               firstPart = name;
            } else {
               firstPart = name.substring(0, firstPartLength);
            }

            StringBuilder scopeToTry = new StringBuilder(relativeTo.getFullName());

            while(true) {
               int dotpos = scopeToTry.lastIndexOf(".");
               if (dotpos == -1) {
                  fullname = name;
                  result = this.findSymbol(name, filter);
                  break;
               }

               scopeToTry.setLength(dotpos + 1);
               scopeToTry.append(firstPart);
               result = this.findSymbol(scopeToTry.toString(), Descriptors.DescriptorPool.SearchFilter.AGGREGATES_ONLY);
               if (result != null) {
                  if (firstPartLength != -1) {
                     scopeToTry.setLength(dotpos + 1);
                     scopeToTry.append(name);
                     result = this.findSymbol(scopeToTry.toString(), filter);
                  }

                  fullname = scopeToTry.toString();
                  break;
               }

               scopeToTry.setLength(dotpos);
            }
         }

         if (result == null) {
            if (this.allowUnknownDependencies && filter == Descriptors.DescriptorPool.SearchFilter.TYPES_ONLY) {
               Descriptors.logger.warning("The descriptor for message type \"" + name + "\" cannot be found and a placeholder is created for it");
               result = new Descriptors.Descriptor(fullname);
               this.dependencies.add(result.getFile());
               return result;
            } else {
               throw new Descriptors.DescriptorValidationException(relativeTo, '"' + name + "\" is not defined.");
            }
         } else {
            return result;
         }
      }

      void addSymbol(Descriptors.GenericDescriptor descriptor) throws Descriptors.DescriptorValidationException {
         validateSymbolName(descriptor);
         String fullName = descriptor.getFullName();
         Descriptors.GenericDescriptor old = (Descriptors.GenericDescriptor)this.descriptorsByName.put(fullName, descriptor);
         if (old != null) {
            this.descriptorsByName.put(fullName, old);
            if (descriptor.getFile() == old.getFile()) {
               int dotpos = fullName.lastIndexOf(46);
               if (dotpos == -1) {
                  throw new Descriptors.DescriptorValidationException(descriptor, '"' + fullName + "\" is already defined.");
               } else {
                  throw new Descriptors.DescriptorValidationException(
                     descriptor, '"' + fullName.substring(dotpos + 1) + "\" is already defined in \"" + fullName.substring(0, dotpos) + "\"."
                  );
               }
            } else {
               throw new Descriptors.DescriptorValidationException(
                  descriptor, '"' + fullName + "\" is already defined in file \"" + old.getFile().getName() + "\"."
               );
            }
         }
      }

      void addPackage(String fullName, Descriptors.FileDescriptor file) throws Descriptors.DescriptorValidationException {
         int dotpos = fullName.lastIndexOf(46);
         String name;
         if (dotpos == -1) {
            name = fullName;
         } else {
            this.addPackage(fullName.substring(0, dotpos), file);
            name = fullName.substring(dotpos + 1);
         }

         Descriptors.GenericDescriptor old = (Descriptors.GenericDescriptor)this.descriptorsByName
            .put(fullName, new Descriptors.DescriptorPool.PackageDescriptor(name, fullName, file));
         if (old != null) {
            this.descriptorsByName.put(fullName, old);
            if (!(old instanceof Descriptors.DescriptorPool.PackageDescriptor)) {
               throw new Descriptors.DescriptorValidationException(
                  file, '"' + name + "\" is already defined (as something other than a package) in file \"" + old.getFile().getName() + "\"."
               );
            }
         }

      }

      static void validateSymbolName(Descriptors.GenericDescriptor descriptor) throws Descriptors.DescriptorValidationException {
         String name = descriptor.getName();
         if (name.length() == 0) {
            throw new Descriptors.DescriptorValidationException(descriptor, "Missing name.");
         } else {
            for(int i = 0; i < name.length(); ++i) {
               char c = name.charAt(i);
               if (('a' > c || c > 'z') && ('A' > c || c > 'Z') && c != '_' && ('0' > c || c > '9' || i <= 0)) {
                  throw new Descriptors.DescriptorValidationException(descriptor, '"' + name + "\" is not a valid identifier.");
               }
            }

         }
      }

      private static final class PackageDescriptor extends Descriptors.GenericDescriptor {
         private final String name;
         private final String fullName;
         private final Descriptors.FileDescriptor file;

         @Override
         public Message toProto() {
            return this.file.toProto();
         }

         @Override
         public String getName() {
            return this.name;
         }

         @Override
         public String getFullName() {
            return this.fullName;
         }

         @Override
         public Descriptors.FileDescriptor getFile() {
            return this.file;
         }

         PackageDescriptor(String name, String fullName, Descriptors.FileDescriptor file) {
            this.file = file;
            this.fullName = fullName;
            this.name = name;
         }
      }

      static enum SearchFilter {
         TYPES_ONLY,
         AGGREGATES_ONLY,
         ALL_SYMBOLS;
      }
   }

   public static class DescriptorValidationException extends Exception {
      private static final long serialVersionUID = 5750205775490483148L;
      private final String name;
      private final Message proto;
      private final String description;

      public String getProblemSymbolName() {
         return this.name;
      }

      public Message getProblemProto() {
         return this.proto;
      }

      public String getDescription() {
         return this.description;
      }

      private DescriptorValidationException(Descriptors.GenericDescriptor problemDescriptor, String description) {
         super(problemDescriptor.getFullName() + ": " + description);
         this.name = problemDescriptor.getFullName();
         this.proto = problemDescriptor.toProto();
         this.description = description;
      }

      private DescriptorValidationException(Descriptors.GenericDescriptor problemDescriptor, String description, Throwable cause) {
         this(problemDescriptor, description);
         this.initCause(cause);
      }

      private DescriptorValidationException(Descriptors.FileDescriptor problemDescriptor, String description) {
         super(problemDescriptor.getName() + ": " + description);
         this.name = problemDescriptor.getName();
         this.proto = problemDescriptor.toProto();
         this.description = description;
      }
   }

   public static final class EnumDescriptor extends Descriptors.GenericDescriptor implements Internal.EnumLiteMap<Descriptors.EnumValueDescriptor> {
      private final int index;
      private DescriptorProtos.EnumDescriptorProto proto;
      private final String fullName;
      private final Descriptors.FileDescriptor file;
      private final Descriptors.Descriptor containingType;
      private final Descriptors.EnumValueDescriptor[] values;
      private final Descriptors.EnumValueDescriptor[] valuesSortedByNumber;
      private final int distinctNumbers;
      private Map<Integer, WeakReference<Descriptors.EnumValueDescriptor>> unknownValues = null;
      private ReferenceQueue<Descriptors.EnumValueDescriptor> cleanupQueue = null;

      public int getIndex() {
         return this.index;
      }

      public DescriptorProtos.EnumDescriptorProto toProto() {
         return this.proto;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public String getFullName() {
         return this.fullName;
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this.file;
      }

      public Descriptors.Descriptor getContainingType() {
         return this.containingType;
      }

      public DescriptorProtos.EnumOptions getOptions() {
         return this.proto.getOptions();
      }

      public List<Descriptors.EnumValueDescriptor> getValues() {
         return Collections.unmodifiableList(Arrays.asList(this.values));
      }

      public Descriptors.EnumValueDescriptor findValueByName(String name) {
         Descriptors.GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
         return result instanceof Descriptors.EnumValueDescriptor ? (Descriptors.EnumValueDescriptor)result : null;
      }

      public Descriptors.EnumValueDescriptor findValueByNumber(int number) {
         return Descriptors.binarySearch(this.valuesSortedByNumber, this.distinctNumbers, Descriptors.EnumValueDescriptor.NUMBER_GETTER, number);
      }

      public Descriptors.EnumValueDescriptor findValueByNumberCreatingIfUnknown(int number) {
         Descriptors.EnumValueDescriptor result = this.findValueByNumber(number);
         if (result != null) {
            return result;
         } else {
            synchronized(this) {
               if (this.cleanupQueue == null) {
                  this.cleanupQueue = new ReferenceQueue();
                  this.unknownValues = new HashMap();
               } else {
                  while(true) {
                     Descriptors.EnumDescriptor.UnknownEnumValueReference toClean = (Descriptors.EnumDescriptor.UnknownEnumValueReference)this.cleanupQueue
                        .poll();
                     if (toClean == null) {
                        break;
                     }

                     this.unknownValues.remove(toClean.number);
                  }
               }

               WeakReference<Descriptors.EnumValueDescriptor> reference = (WeakReference)this.unknownValues.get(number);
               result = reference == null ? null : (Descriptors.EnumValueDescriptor)reference.get();
               if (result == null) {
                  result = new Descriptors.EnumValueDescriptor(this, number);
                  this.unknownValues.put(number, new Descriptors.EnumDescriptor.UnknownEnumValueReference(number, result));
               }

               return result;
            }
         }
      }

      int getUnknownEnumValueDescriptorCount() {
         return this.unknownValues.size();
      }

      private EnumDescriptor(DescriptorProtos.EnumDescriptorProto proto, Descriptors.FileDescriptor file, Descriptors.Descriptor parent, int index) throws Descriptors.DescriptorValidationException {
         this.index = index;
         this.proto = proto;
         this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
         this.file = file;
         this.containingType = parent;
         if (proto.getValueCount() == 0) {
            throw new Descriptors.DescriptorValidationException(this, "Enums must contain at least one value.");
         } else {
            this.values = new Descriptors.EnumValueDescriptor[proto.getValueCount()];

            for(int i = 0; i < proto.getValueCount(); ++i) {
               this.values[i] = new Descriptors.EnumValueDescriptor(proto.getValue(i), file, this, i);
            }

            this.valuesSortedByNumber = (Descriptors.EnumValueDescriptor[])this.values.clone();
            Arrays.sort(this.valuesSortedByNumber, Descriptors.EnumValueDescriptor.BY_NUMBER);
            int j = 0;

            for(int i = 1; i < proto.getValueCount(); ++i) {
               Descriptors.EnumValueDescriptor oldValue = this.valuesSortedByNumber[j];
               Descriptors.EnumValueDescriptor newValue = this.valuesSortedByNumber[i];
               if (oldValue.getNumber() != newValue.getNumber()) {
                  ++j;
                  this.valuesSortedByNumber[j] = newValue;
               }
            }

            this.distinctNumbers = j + 1;
            Arrays.fill(this.valuesSortedByNumber, this.distinctNumbers, proto.getValueCount(), null);
            file.pool.addSymbol(this);
         }
      }

      private void setProto(DescriptorProtos.EnumDescriptorProto proto) {
         this.proto = proto;

         for(int i = 0; i < this.values.length; ++i) {
            this.values[i].setProto(proto.getValue(i));
         }

      }

      private static class UnknownEnumValueReference extends WeakReference<Descriptors.EnumValueDescriptor> {
         private final int number;

         private UnknownEnumValueReference(int number, Descriptors.EnumValueDescriptor descriptor) {
            super(descriptor);
            this.number = number;
         }
      }
   }

   public static final class EnumValueDescriptor extends Descriptors.GenericDescriptor implements Internal.EnumLite {
      static final Comparator<Descriptors.EnumValueDescriptor> BY_NUMBER = new Comparator<Descriptors.EnumValueDescriptor>() {
         public int compare(Descriptors.EnumValueDescriptor o1, Descriptors.EnumValueDescriptor o2) {
            return Integer.valueOf(o1.getNumber()).compareTo(o2.getNumber());
         }
      };
      static final Descriptors.NumberGetter<Descriptors.EnumValueDescriptor> NUMBER_GETTER = new Descriptors.NumberGetter<Descriptors.EnumValueDescriptor>() {
         public int getNumber(Descriptors.EnumValueDescriptor enumValueDescriptor) {
            return enumValueDescriptor.getNumber();
         }
      };
      private final int index;
      private DescriptorProtos.EnumValueDescriptorProto proto;
      private final String fullName;
      private final Descriptors.EnumDescriptor type;

      public int getIndex() {
         return this.index;
      }

      public DescriptorProtos.EnumValueDescriptorProto toProto() {
         return this.proto;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public int getNumber() {
         return this.proto.getNumber();
      }

      public String toString() {
         return this.proto.getName();
      }

      @Override
      public String getFullName() {
         return this.fullName;
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this.type.file;
      }

      public Descriptors.EnumDescriptor getType() {
         return this.type;
      }

      public DescriptorProtos.EnumValueOptions getOptions() {
         return this.proto.getOptions();
      }

      private EnumValueDescriptor(
         DescriptorProtos.EnumValueDescriptorProto proto, Descriptors.FileDescriptor file, Descriptors.EnumDescriptor parent, int index
      ) throws Descriptors.DescriptorValidationException {
         this.index = index;
         this.proto = proto;
         this.type = parent;
         this.fullName = parent.getFullName() + '.' + proto.getName();
         file.pool.addSymbol(this);
      }

      private EnumValueDescriptor(Descriptors.EnumDescriptor parent, Integer number) {
         String name = "UNKNOWN_ENUM_VALUE_" + parent.getName() + "_" + number;
         DescriptorProtos.EnumValueDescriptorProto proto = DescriptorProtos.EnumValueDescriptorProto.newBuilder().setName(name).setNumber(number).build();
         this.index = -1;
         this.proto = proto;
         this.type = parent;
         this.fullName = parent.getFullName() + '.' + proto.getName();
      }

      private void setProto(DescriptorProtos.EnumValueDescriptorProto proto) {
         this.proto = proto;
      }
   }

   public static final class FieldDescriptor
      extends Descriptors.GenericDescriptor
      implements Comparable<Descriptors.FieldDescriptor>,
      FieldSet.FieldDescriptorLite<Descriptors.FieldDescriptor> {
      private static final Descriptors.NumberGetter<Descriptors.FieldDescriptor> NUMBER_GETTER = new Descriptors.NumberGetter<Descriptors.FieldDescriptor>() {
         public int getNumber(Descriptors.FieldDescriptor fieldDescriptor) {
            return fieldDescriptor.getNumber();
         }
      };
      private static final WireFormat.FieldType[] table = WireFormat.FieldType.values();
      private final int index;
      private DescriptorProtos.FieldDescriptorProto proto;
      private final String fullName;
      private String jsonName;
      private final Descriptors.FileDescriptor file;
      private final Descriptors.Descriptor extensionScope;
      private final boolean isProto3Optional;
      private Descriptors.FieldDescriptor.Type type;
      private Descriptors.Descriptor containingType;
      private Descriptors.Descriptor messageType;
      private Descriptors.OneofDescriptor containingOneof;
      private Descriptors.EnumDescriptor enumType;
      private Object defaultValue;

      public int getIndex() {
         return this.index;
      }

      public DescriptorProtos.FieldDescriptorProto toProto() {
         return this.proto;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public int getNumber() {
         return this.proto.getNumber();
      }

      @Override
      public String getFullName() {
         return this.fullName;
      }

      public String getJsonName() {
         String result = this.jsonName;
         if (result != null) {
            return result;
         } else {
            return this.proto.hasJsonName() ? (this.jsonName = this.proto.getJsonName()) : (this.jsonName = fieldNameToJsonName(this.proto.getName()));
         }
      }

      public Descriptors.FieldDescriptor.JavaType getJavaType() {
         return this.type.getJavaType();
      }

      @Override
      public WireFormat.JavaType getLiteJavaType() {
         return this.getLiteType().getJavaType();
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this.file;
      }

      public Descriptors.FieldDescriptor.Type getType() {
         return this.type;
      }

      @Override
      public WireFormat.FieldType getLiteType() {
         return table[this.type.ordinal()];
      }

      public boolean needsUtf8Check() {
         if (this.type != Descriptors.FieldDescriptor.Type.STRING) {
            return false;
         } else if (this.getContainingType().getOptions().getMapEntry()) {
            return true;
         } else {
            return this.getFile().getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO3 ? true : this.getFile().getOptions().getJavaStringCheckUtf8();
         }
      }

      public boolean isMapField() {
         return this.getType() == Descriptors.FieldDescriptor.Type.MESSAGE && this.isRepeated() && this.getMessageType().getOptions().getMapEntry();
      }

      public boolean isRequired() {
         return this.proto.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED;
      }

      public boolean isOptional() {
         return this.proto.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL;
      }

      @Override
      public boolean isRepeated() {
         return this.proto.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
      }

      @Override
      public boolean isPacked() {
         if (!this.isPackable()) {
            return false;
         } else if (this.getFile().getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO2) {
            return this.getOptions().getPacked();
         } else {
            return !this.getOptions().hasPacked() || this.getOptions().getPacked();
         }
      }

      public boolean isPackable() {
         return this.isRepeated() && this.getLiteType().isPackable();
      }

      public boolean hasDefaultValue() {
         return this.proto.hasDefaultValue();
      }

      public Object getDefaultValue() {
         if (this.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            throw new UnsupportedOperationException("FieldDescriptor.getDefaultValue() called on an embedded message field.");
         } else {
            return this.defaultValue;
         }
      }

      public DescriptorProtos.FieldOptions getOptions() {
         return this.proto.getOptions();
      }

      public boolean isExtension() {
         return this.proto.hasExtendee();
      }

      public Descriptors.Descriptor getContainingType() {
         return this.containingType;
      }

      public Descriptors.OneofDescriptor getContainingOneof() {
         return this.containingOneof;
      }

      public Descriptors.OneofDescriptor getRealContainingOneof() {
         return this.containingOneof != null && !this.containingOneof.isSynthetic() ? this.containingOneof : null;
      }

      public boolean hasOptionalKeyword() {
         return this.isProto3Optional
            || this.file.getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO2 && this.isOptional() && this.getContainingOneof() == null;
      }

      public boolean hasPresence() {
         if (this.isRepeated()) {
            return false;
         } else {
            return this.getType() == Descriptors.FieldDescriptor.Type.MESSAGE
               || this.getType() == Descriptors.FieldDescriptor.Type.GROUP
               || this.getContainingOneof() != null
               || this.file.getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO2;
         }
      }

      public Descriptors.Descriptor getExtensionScope() {
         if (!this.isExtension()) {
            throw new UnsupportedOperationException(String.format("This field is not an extension. (%s)", this.fullName));
         } else {
            return this.extensionScope;
         }
      }

      public Descriptors.Descriptor getMessageType() {
         if (this.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            throw new UnsupportedOperationException(String.format("This field is not of message type. (%s)", this.fullName));
         } else {
            return this.messageType;
         }
      }

      public Descriptors.EnumDescriptor getEnumType() {
         if (this.getJavaType() != Descriptors.FieldDescriptor.JavaType.ENUM) {
            throw new UnsupportedOperationException(String.format("This field is not of enum type. (%s)", this.fullName));
         } else {
            return this.enumType;
         }
      }

      public int compareTo(Descriptors.FieldDescriptor other) {
         if (other.containingType != this.containingType) {
            throw new IllegalArgumentException("FieldDescriptors can only be compared to other FieldDescriptors for fields of the same message type.");
         } else {
            return this.getNumber() - other.getNumber();
         }
      }

      public String toString() {
         return this.getFullName();
      }

      private static String fieldNameToJsonName(String name) {
         int length = name.length();
         StringBuilder result = new StringBuilder(length);
         boolean isNextUpperCase = false;

         for(int i = 0; i < length; ++i) {
            char ch = name.charAt(i);
            if (ch == '_') {
               isNextUpperCase = true;
            } else if (isNextUpperCase) {
               if ('a' <= ch && ch <= 'z') {
                  ch = (char)(ch - 'a' + 65);
               }

               result.append(ch);
               isNextUpperCase = false;
            } else {
               result.append(ch);
            }
         }

         return result.toString();
      }

      private FieldDescriptor(
         DescriptorProtos.FieldDescriptorProto proto, Descriptors.FileDescriptor file, Descriptors.Descriptor parent, int index, boolean isExtension
      ) throws Descriptors.DescriptorValidationException {
         this.index = index;
         this.proto = proto;
         this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
         this.file = file;
         if (proto.hasType()) {
            this.type = Descriptors.FieldDescriptor.Type.valueOf(proto.getType());
         }

         this.isProto3Optional = proto.getProto3Optional();
         if (this.getNumber() <= 0) {
            throw new Descriptors.DescriptorValidationException(this, "Field numbers must be positive integers.");
         } else {
            if (isExtension) {
               if (!proto.hasExtendee()) {
                  throw new Descriptors.DescriptorValidationException(this, "FieldDescriptorProto.extendee not set for extension field.");
               }

               this.containingType = null;
               if (parent != null) {
                  this.extensionScope = parent;
               } else {
                  this.extensionScope = null;
               }

               if (proto.hasOneofIndex()) {
                  throw new Descriptors.DescriptorValidationException(this, "FieldDescriptorProto.oneof_index set for extension field.");
               }

               this.containingOneof = null;
            } else {
               if (proto.hasExtendee()) {
                  throw new Descriptors.DescriptorValidationException(this, "FieldDescriptorProto.extendee set for non-extension field.");
               }

               this.containingType = parent;
               if (proto.hasOneofIndex()) {
                  if (proto.getOneofIndex() < 0 || proto.getOneofIndex() >= parent.toProto().getOneofDeclCount()) {
                     throw new Descriptors.DescriptorValidationException(this, "FieldDescriptorProto.oneof_index is out of range for type " + parent.getName());
                  }

                  this.containingOneof = (Descriptors.OneofDescriptor)parent.getOneofs().get(proto.getOneofIndex());
                  this.containingOneof.fieldCount++;
               } else {
                  this.containingOneof = null;
               }

               this.extensionScope = null;
            }

            file.pool.addSymbol(this);
         }
      }

      private void crossLink() throws Descriptors.DescriptorValidationException {
         if (this.proto.hasExtendee()) {
            Descriptors.GenericDescriptor extendee = this.file
               .pool
               .lookupSymbol(this.proto.getExtendee(), this, Descriptors.DescriptorPool.SearchFilter.TYPES_ONLY);
            if (!(extendee instanceof Descriptors.Descriptor)) {
               throw new Descriptors.DescriptorValidationException(this, '"' + this.proto.getExtendee() + "\" is not a message type.");
            }

            this.containingType = (Descriptors.Descriptor)extendee;
            if (!this.getContainingType().isExtensionNumber(this.getNumber())) {
               throw new Descriptors.DescriptorValidationException(
                  this, '"' + this.getContainingType().getFullName() + "\" does not declare " + this.getNumber() + " as an extension number."
               );
            }
         }

         if (this.proto.hasTypeName()) {
            Descriptors.GenericDescriptor typeDescriptor = this.file
               .pool
               .lookupSymbol(this.proto.getTypeName(), this, Descriptors.DescriptorPool.SearchFilter.TYPES_ONLY);
            if (!this.proto.hasType()) {
               if (typeDescriptor instanceof Descriptors.Descriptor) {
                  this.type = Descriptors.FieldDescriptor.Type.MESSAGE;
               } else {
                  if (!(typeDescriptor instanceof Descriptors.EnumDescriptor)) {
                     throw new Descriptors.DescriptorValidationException(this, '"' + this.proto.getTypeName() + "\" is not a type.");
                  }

                  this.type = Descriptors.FieldDescriptor.Type.ENUM;
               }
            }

            if (this.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
               if (!(typeDescriptor instanceof Descriptors.Descriptor)) {
                  throw new Descriptors.DescriptorValidationException(this, '"' + this.proto.getTypeName() + "\" is not a message type.");
               }

               this.messageType = (Descriptors.Descriptor)typeDescriptor;
               if (this.proto.hasDefaultValue()) {
                  throw new Descriptors.DescriptorValidationException(this, "Messages can't have default values.");
               }
            } else {
               if (this.getJavaType() != Descriptors.FieldDescriptor.JavaType.ENUM) {
                  throw new Descriptors.DescriptorValidationException(this, "Field with primitive type has type_name.");
               }

               if (!(typeDescriptor instanceof Descriptors.EnumDescriptor)) {
                  throw new Descriptors.DescriptorValidationException(this, '"' + this.proto.getTypeName() + "\" is not an enum type.");
               }

               this.enumType = (Descriptors.EnumDescriptor)typeDescriptor;
            }
         } else if (this.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE || this.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
            throw new Descriptors.DescriptorValidationException(this, "Field with message or enum type missing type_name.");
         }

         if (this.proto.getOptions().getPacked() && !this.isPackable()) {
            throw new Descriptors.DescriptorValidationException(this, "[packed = true] can only be specified for repeated primitive fields.");
         } else {
            if (this.proto.hasDefaultValue()) {
               if (this.isRepeated()) {
                  throw new Descriptors.DescriptorValidationException(this, "Repeated fields cannot have default values.");
               }

               try {
                  switch(this.getType()) {
                     case INT32:
                     case SINT32:
                     case SFIXED32:
                        this.defaultValue = TextFormat.parseInt32(this.proto.getDefaultValue());
                        break;
                     case UINT32:
                     case FIXED32:
                        this.defaultValue = TextFormat.parseUInt32(this.proto.getDefaultValue());
                        break;
                     case INT64:
                     case SINT64:
                     case SFIXED64:
                        this.defaultValue = TextFormat.parseInt64(this.proto.getDefaultValue());
                        break;
                     case UINT64:
                     case FIXED64:
                        this.defaultValue = TextFormat.parseUInt64(this.proto.getDefaultValue());
                        break;
                     case FLOAT:
                        if (this.proto.getDefaultValue().equals("inf")) {
                           this.defaultValue = Float.POSITIVE_INFINITY;
                        } else if (this.proto.getDefaultValue().equals("-inf")) {
                           this.defaultValue = Float.NEGATIVE_INFINITY;
                        } else if (this.proto.getDefaultValue().equals("nan")) {
                           this.defaultValue = Float.NaN;
                        } else {
                           this.defaultValue = Float.valueOf(this.proto.getDefaultValue());
                        }
                        break;
                     case DOUBLE:
                        if (this.proto.getDefaultValue().equals("inf")) {
                           this.defaultValue = Double.POSITIVE_INFINITY;
                        } else if (this.proto.getDefaultValue().equals("-inf")) {
                           this.defaultValue = Double.NEGATIVE_INFINITY;
                        } else if (this.proto.getDefaultValue().equals("nan")) {
                           this.defaultValue = Double.NaN;
                        } else {
                           this.defaultValue = Double.valueOf(this.proto.getDefaultValue());
                        }
                        break;
                     case BOOL:
                        this.defaultValue = Boolean.valueOf(this.proto.getDefaultValue());
                        break;
                     case STRING:
                        this.defaultValue = this.proto.getDefaultValue();
                        break;
                     case BYTES:
                        try {
                           this.defaultValue = TextFormat.unescapeBytes(this.proto.getDefaultValue());
                           break;
                        } catch (TextFormat.InvalidEscapeSequenceException var2) {
                           throw new Descriptors.DescriptorValidationException(this, "Couldn't parse default value: " + var2.getMessage(), var2);
                        }
                     case ENUM:
                        this.defaultValue = this.enumType.findValueByName(this.proto.getDefaultValue());
                        if (this.defaultValue == null) {
                           throw new Descriptors.DescriptorValidationException(this, "Unknown enum default value: \"" + this.proto.getDefaultValue() + '"');
                        }
                        break;
                     case MESSAGE:
                     case GROUP:
                        throw new Descriptors.DescriptorValidationException(this, "Message type had default value.");
                  }
               } catch (NumberFormatException var3) {
                  throw new Descriptors.DescriptorValidationException(this, "Could not parse default value: \"" + this.proto.getDefaultValue() + '"', var3);
               }
            } else if (this.isRepeated()) {
               this.defaultValue = Collections.emptyList();
            } else {
               switch(this.getJavaType()) {
                  case ENUM:
                     this.defaultValue = this.enumType.getValues().get(0);
                     break;
                  case MESSAGE:
                     this.defaultValue = null;
                     break;
                  default:
                     this.defaultValue = this.getJavaType().defaultDefault;
               }
            }

            if (this.containingType != null && this.containingType.getOptions().getMessageSetWireFormat()) {
               if (!this.isExtension()) {
                  throw new Descriptors.DescriptorValidationException(this, "MessageSets cannot have fields, only extensions.");
               }

               if (!this.isOptional() || this.getType() != Descriptors.FieldDescriptor.Type.MESSAGE) {
                  throw new Descriptors.DescriptorValidationException(this, "Extensions of MessageSets must be optional messages.");
               }
            }

         }
      }

      private void setProto(DescriptorProtos.FieldDescriptorProto proto) {
         this.proto = proto;
      }

      @Override
      public MessageLite.Builder internalMergeFrom(MessageLite.Builder to, MessageLite from) {
         return ((Message.Builder)to).mergeFrom((Message)from);
      }

      static {
         if (Descriptors.FieldDescriptor.Type.types.length != DescriptorProtos.FieldDescriptorProto.Type.values().length) {
            throw new RuntimeException("descriptor.proto has a new declared type but Descriptors.java wasn't updated.");
         }
      }

      public static enum JavaType {
         INT(0),
         LONG(0L),
         FLOAT(0.0F),
         DOUBLE(0.0),
         BOOLEAN(false),
         STRING(""),
         BYTE_STRING(ByteString.EMPTY),
         ENUM(null),
         MESSAGE(null);

         private final Object defaultDefault;

         private JavaType(Object defaultDefault) {
            this.defaultDefault = defaultDefault;
         }
      }

      public static enum Type {
         DOUBLE(Descriptors.FieldDescriptor.JavaType.DOUBLE),
         FLOAT(Descriptors.FieldDescriptor.JavaType.FLOAT),
         INT64(Descriptors.FieldDescriptor.JavaType.LONG),
         UINT64(Descriptors.FieldDescriptor.JavaType.LONG),
         INT32(Descriptors.FieldDescriptor.JavaType.INT),
         FIXED64(Descriptors.FieldDescriptor.JavaType.LONG),
         FIXED32(Descriptors.FieldDescriptor.JavaType.INT),
         BOOL(Descriptors.FieldDescriptor.JavaType.BOOLEAN),
         STRING(Descriptors.FieldDescriptor.JavaType.STRING),
         GROUP(Descriptors.FieldDescriptor.JavaType.MESSAGE),
         MESSAGE(Descriptors.FieldDescriptor.JavaType.MESSAGE),
         BYTES(Descriptors.FieldDescriptor.JavaType.BYTE_STRING),
         UINT32(Descriptors.FieldDescriptor.JavaType.INT),
         ENUM(Descriptors.FieldDescriptor.JavaType.ENUM),
         SFIXED32(Descriptors.FieldDescriptor.JavaType.INT),
         SFIXED64(Descriptors.FieldDescriptor.JavaType.LONG),
         SINT32(Descriptors.FieldDescriptor.JavaType.INT),
         SINT64(Descriptors.FieldDescriptor.JavaType.LONG);

         private static final Descriptors.FieldDescriptor.Type[] types = values();
         private final Descriptors.FieldDescriptor.JavaType javaType;

         private Type(Descriptors.FieldDescriptor.JavaType javaType) {
            this.javaType = javaType;
         }

         public DescriptorProtos.FieldDescriptorProto.Type toProto() {
            return DescriptorProtos.FieldDescriptorProto.Type.forNumber(this.ordinal() + 1);
         }

         public Descriptors.FieldDescriptor.JavaType getJavaType() {
            return this.javaType;
         }

         public static Descriptors.FieldDescriptor.Type valueOf(DescriptorProtos.FieldDescriptorProto.Type type) {
            return types[type.getNumber() - 1];
         }
      }
   }

   public static final class FileDescriptor extends Descriptors.GenericDescriptor {
      private DescriptorProtos.FileDescriptorProto proto;
      private final Descriptors.Descriptor[] messageTypes;
      private final Descriptors.EnumDescriptor[] enumTypes;
      private final Descriptors.ServiceDescriptor[] services;
      private final Descriptors.FieldDescriptor[] extensions;
      private final Descriptors.FileDescriptor[] dependencies;
      private final Descriptors.FileDescriptor[] publicDependencies;
      private final Descriptors.DescriptorPool pool;

      public DescriptorProtos.FileDescriptorProto toProto() {
         return this.proto;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this;
      }

      @Override
      public String getFullName() {
         return this.proto.getName();
      }

      public String getPackage() {
         return this.proto.getPackage();
      }

      public DescriptorProtos.FileOptions getOptions() {
         return this.proto.getOptions();
      }

      public List<Descriptors.Descriptor> getMessageTypes() {
         return Collections.unmodifiableList(Arrays.asList(this.messageTypes));
      }

      public List<Descriptors.EnumDescriptor> getEnumTypes() {
         return Collections.unmodifiableList(Arrays.asList(this.enumTypes));
      }

      public List<Descriptors.ServiceDescriptor> getServices() {
         return Collections.unmodifiableList(Arrays.asList(this.services));
      }

      public List<Descriptors.FieldDescriptor> getExtensions() {
         return Collections.unmodifiableList(Arrays.asList(this.extensions));
      }

      public List<Descriptors.FileDescriptor> getDependencies() {
         return Collections.unmodifiableList(Arrays.asList(this.dependencies));
      }

      public List<Descriptors.FileDescriptor> getPublicDependencies() {
         return Collections.unmodifiableList(Arrays.asList(this.publicDependencies));
      }

      public Descriptors.FileDescriptor.Syntax getSyntax() {
         return Descriptors.FileDescriptor.Syntax.PROTO3.name.equals(this.proto.getSyntax())
            ? Descriptors.FileDescriptor.Syntax.PROTO3
            : Descriptors.FileDescriptor.Syntax.PROTO2;
      }

      public Descriptors.Descriptor findMessageTypeByName(String name) {
         if (name.indexOf(46) != -1) {
            return null;
         } else {
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
               name = packageName + '.' + name;
            }

            Descriptors.GenericDescriptor result = this.pool.findSymbol(name);
            return result instanceof Descriptors.Descriptor && result.getFile() == this ? (Descriptors.Descriptor)result : null;
         }
      }

      public Descriptors.EnumDescriptor findEnumTypeByName(String name) {
         if (name.indexOf(46) != -1) {
            return null;
         } else {
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
               name = packageName + '.' + name;
            }

            Descriptors.GenericDescriptor result = this.pool.findSymbol(name);
            return result instanceof Descriptors.EnumDescriptor && result.getFile() == this ? (Descriptors.EnumDescriptor)result : null;
         }
      }

      public Descriptors.ServiceDescriptor findServiceByName(String name) {
         if (name.indexOf(46) != -1) {
            return null;
         } else {
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
               name = packageName + '.' + name;
            }

            Descriptors.GenericDescriptor result = this.pool.findSymbol(name);
            return result instanceof Descriptors.ServiceDescriptor && result.getFile() == this ? (Descriptors.ServiceDescriptor)result : null;
         }
      }

      public Descriptors.FieldDescriptor findExtensionByName(String name) {
         if (name.indexOf(46) != -1) {
            return null;
         } else {
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
               name = packageName + '.' + name;
            }

            Descriptors.GenericDescriptor result = this.pool.findSymbol(name);
            return result instanceof Descriptors.FieldDescriptor && result.getFile() == this ? (Descriptors.FieldDescriptor)result : null;
         }
      }

      public static Descriptors.FileDescriptor buildFrom(DescriptorProtos.FileDescriptorProto proto, Descriptors.FileDescriptor[] dependencies) throws Descriptors.DescriptorValidationException {
         return buildFrom(proto, dependencies, false);
      }

      public static Descriptors.FileDescriptor buildFrom(
         DescriptorProtos.FileDescriptorProto proto, Descriptors.FileDescriptor[] dependencies, boolean allowUnknownDependencies
      ) throws Descriptors.DescriptorValidationException {
         Descriptors.DescriptorPool pool = new Descriptors.DescriptorPool(dependencies, allowUnknownDependencies);
         Descriptors.FileDescriptor result = new Descriptors.FileDescriptor(proto, dependencies, pool, allowUnknownDependencies);
         result.crossLink();
         return result;
      }

      private static byte[] latin1Cat(String[] strings) {
         if (strings.length == 1) {
            return strings[0].getBytes(Internal.ISO_8859_1);
         } else {
            StringBuilder descriptorData = new StringBuilder();

            for(String part : strings) {
               descriptorData.append(part);
            }

            return descriptorData.toString().getBytes(Internal.ISO_8859_1);
         }
      }

      private static Descriptors.FileDescriptor[] findDescriptors(Class<?> descriptorOuterClass, String[] dependencyClassNames, String[] dependencyFileNames) {
         List<Descriptors.FileDescriptor> descriptors = new ArrayList();

         for(int i = 0; i < dependencyClassNames.length; ++i) {
            try {
               Class<?> clazz = descriptorOuterClass.getClassLoader().loadClass(dependencyClassNames[i]);
               descriptors.add((Descriptors.FileDescriptor)clazz.getField("descriptor").get(null));
            } catch (Exception var6) {
               Descriptors.logger.warning("Descriptors for \"" + dependencyFileNames[i] + "\" can not be found.");
            }
         }

         return (Descriptors.FileDescriptor[])descriptors.toArray(new Descriptors.FileDescriptor[0]);
      }

      @Deprecated
      public static void internalBuildGeneratedFileFrom(
         String[] descriptorDataParts, Descriptors.FileDescriptor[] dependencies, Descriptors.FileDescriptor.InternalDescriptorAssigner descriptorAssigner
      ) {
         byte[] descriptorBytes = latin1Cat(descriptorDataParts);

         DescriptorProtos.FileDescriptorProto proto;
         try {
            proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptorBytes);
         } catch (InvalidProtocolBufferException var10) {
            throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", var10);
         }

         Descriptors.FileDescriptor result;
         try {
            result = buildFrom(proto, dependencies, true);
         } catch (Descriptors.DescriptorValidationException var9) {
            throw new IllegalArgumentException("Invalid embedded descriptor for \"" + proto.getName() + "\".", var9);
         }

         ExtensionRegistry registry = descriptorAssigner.assignDescriptors(result);
         if (registry != null) {
            try {
               proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptorBytes, registry);
            } catch (InvalidProtocolBufferException var8) {
               throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", var8);
            }

            result.setProto(proto);
         }

      }

      public static Descriptors.FileDescriptor internalBuildGeneratedFileFrom(String[] descriptorDataParts, Descriptors.FileDescriptor[] dependencies) {
         byte[] descriptorBytes = latin1Cat(descriptorDataParts);

         DescriptorProtos.FileDescriptorProto proto;
         try {
            proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptorBytes);
         } catch (InvalidProtocolBufferException var6) {
            throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", var6);
         }

         try {
            return buildFrom(proto, dependencies, true);
         } catch (Descriptors.DescriptorValidationException var5) {
            throw new IllegalArgumentException("Invalid embedded descriptor for \"" + proto.getName() + "\".", var5);
         }
      }

      @Deprecated
      public static void internalBuildGeneratedFileFrom(
         String[] descriptorDataParts,
         Class<?> descriptorOuterClass,
         String[] dependencyClassNames,
         String[] dependencyFileNames,
         Descriptors.FileDescriptor.InternalDescriptorAssigner descriptorAssigner
      ) {
         Descriptors.FileDescriptor[] dependencies = findDescriptors(descriptorOuterClass, dependencyClassNames, dependencyFileNames);
         internalBuildGeneratedFileFrom(descriptorDataParts, dependencies, descriptorAssigner);
      }

      public static Descriptors.FileDescriptor internalBuildGeneratedFileFrom(
         String[] descriptorDataParts, Class<?> descriptorOuterClass, String[] dependencyClassNames, String[] dependencyFileNames
      ) {
         Descriptors.FileDescriptor[] dependencies = findDescriptors(descriptorOuterClass, dependencyClassNames, dependencyFileNames);
         return internalBuildGeneratedFileFrom(descriptorDataParts, dependencies);
      }

      public static void internalUpdateFileDescriptor(Descriptors.FileDescriptor descriptor, ExtensionRegistry registry) {
         ByteString bytes = descriptor.proto.toByteString();

         DescriptorProtos.FileDescriptorProto proto;
         try {
            proto = DescriptorProtos.FileDescriptorProto.parseFrom(bytes, registry);
         } catch (InvalidProtocolBufferException var5) {
            throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", var5);
         }

         descriptor.setProto(proto);
      }

      private FileDescriptor(
         DescriptorProtos.FileDescriptorProto proto,
         Descriptors.FileDescriptor[] dependencies,
         Descriptors.DescriptorPool pool,
         boolean allowUnknownDependencies
      ) throws Descriptors.DescriptorValidationException {
         this.pool = pool;
         this.proto = proto;
         this.dependencies = (Descriptors.FileDescriptor[])dependencies.clone();
         HashMap<String, Descriptors.FileDescriptor> nameToFileMap = new HashMap();

         for(Descriptors.FileDescriptor file : dependencies) {
            nameToFileMap.put(file.getName(), file);
         }

         List<Descriptors.FileDescriptor> publicDependencies = new ArrayList();

         for(int i = 0; i < proto.getPublicDependencyCount(); ++i) {
            int index = proto.getPublicDependency(i);
            if (index < 0 || index >= proto.getDependencyCount()) {
               throw new Descriptors.DescriptorValidationException(this, "Invalid public dependency index.");
            }

            String name = proto.getDependency(index);
            Descriptors.FileDescriptor file = (Descriptors.FileDescriptor)nameToFileMap.get(name);
            if (file == null) {
               if (!allowUnknownDependencies) {
                  throw new Descriptors.DescriptorValidationException(this, "Invalid public dependency: " + name);
               }
            } else {
               publicDependencies.add(file);
            }
         }

         this.publicDependencies = new Descriptors.FileDescriptor[publicDependencies.size()];
         publicDependencies.toArray(this.publicDependencies);
         pool.addPackage(this.getPackage(), this);
         this.messageTypes = proto.getMessageTypeCount() > 0 ? new Descriptors.Descriptor[proto.getMessageTypeCount()] : Descriptors.EMPTY_DESCRIPTORS;

         for(int i = 0; i < proto.getMessageTypeCount(); ++i) {
            this.messageTypes[i] = new Descriptors.Descriptor(proto.getMessageType(i), this, null, i);
         }

         this.enumTypes = proto.getEnumTypeCount() > 0 ? new Descriptors.EnumDescriptor[proto.getEnumTypeCount()] : Descriptors.EMPTY_ENUM_DESCRIPTORS;

         for(int i = 0; i < proto.getEnumTypeCount(); ++i) {
            this.enumTypes[i] = new Descriptors.EnumDescriptor(proto.getEnumType(i), this, null, i);
         }

         this.services = proto.getServiceCount() > 0 ? new Descriptors.ServiceDescriptor[proto.getServiceCount()] : Descriptors.EMPTY_SERVICE_DESCRIPTORS;

         for(int i = 0; i < proto.getServiceCount(); ++i) {
            this.services[i] = new Descriptors.ServiceDescriptor(proto.getService(i), this, i);
         }

         this.extensions = proto.getExtensionCount() > 0 ? new Descriptors.FieldDescriptor[proto.getExtensionCount()] : Descriptors.EMPTY_FIELD_DESCRIPTORS;

         for(int i = 0; i < proto.getExtensionCount(); ++i) {
            this.extensions[i] = new Descriptors.FieldDescriptor(proto.getExtension(i), this, null, i, true);
         }

      }

      FileDescriptor(String packageName, Descriptors.Descriptor message) throws Descriptors.DescriptorValidationException {
         this.pool = new Descriptors.DescriptorPool(new Descriptors.FileDescriptor[0], true);
         this.proto = DescriptorProtos.FileDescriptorProto.newBuilder()
            .setName(message.getFullName() + ".placeholder.proto")
            .setPackage(packageName)
            .addMessageType(message.toProto())
            .build();
         this.dependencies = new Descriptors.FileDescriptor[0];
         this.publicDependencies = new Descriptors.FileDescriptor[0];
         this.messageTypes = new Descriptors.Descriptor[]{message};
         this.enumTypes = Descriptors.EMPTY_ENUM_DESCRIPTORS;
         this.services = Descriptors.EMPTY_SERVICE_DESCRIPTORS;
         this.extensions = Descriptors.EMPTY_FIELD_DESCRIPTORS;
         this.pool.addPackage(packageName, this);
         this.pool.addSymbol(message);
      }

      private void crossLink() throws Descriptors.DescriptorValidationException {
         for(Descriptors.Descriptor messageType : this.messageTypes) {
            messageType.crossLink();
         }

         for(Descriptors.ServiceDescriptor service : this.services) {
            service.crossLink();
         }

         for(Descriptors.FieldDescriptor extension : this.extensions) {
            extension.crossLink();
         }

      }

      private void setProto(DescriptorProtos.FileDescriptorProto proto) {
         this.proto = proto;

         for(int i = 0; i < this.messageTypes.length; ++i) {
            this.messageTypes[i].setProto(proto.getMessageType(i));
         }

         for(int i = 0; i < this.enumTypes.length; ++i) {
            this.enumTypes[i].setProto(proto.getEnumType(i));
         }

         for(int i = 0; i < this.services.length; ++i) {
            this.services[i].setProto(proto.getService(i));
         }

         for(int i = 0; i < this.extensions.length; ++i) {
            this.extensions[i].setProto(proto.getExtension(i));
         }

      }

      boolean supportsUnknownEnumValue() {
         return this.getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO3;
      }

      @Deprecated
      public interface InternalDescriptorAssigner {
         ExtensionRegistry assignDescriptors(Descriptors.FileDescriptor var1);
      }

      public static enum Syntax {
         UNKNOWN("unknown"),
         PROTO2("proto2"),
         PROTO3("proto3");

         private final String name;

         private Syntax(String name) {
            this.name = name;
         }
      }
   }

   public abstract static class GenericDescriptor {
      private GenericDescriptor() {
      }

      public abstract Message toProto();

      public abstract String getName();

      public abstract String getFullName();

      public abstract Descriptors.FileDescriptor getFile();
   }

   public static final class MethodDescriptor extends Descriptors.GenericDescriptor {
      private final int index;
      private DescriptorProtos.MethodDescriptorProto proto;
      private final String fullName;
      private final Descriptors.FileDescriptor file;
      private final Descriptors.ServiceDescriptor service;
      private Descriptors.Descriptor inputType;
      private Descriptors.Descriptor outputType;

      public int getIndex() {
         return this.index;
      }

      public DescriptorProtos.MethodDescriptorProto toProto() {
         return this.proto;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public String getFullName() {
         return this.fullName;
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this.file;
      }

      public Descriptors.ServiceDescriptor getService() {
         return this.service;
      }

      public Descriptors.Descriptor getInputType() {
         return this.inputType;
      }

      public Descriptors.Descriptor getOutputType() {
         return this.outputType;
      }

      public boolean isClientStreaming() {
         return this.proto.getClientStreaming();
      }

      public boolean isServerStreaming() {
         return this.proto.getServerStreaming();
      }

      public DescriptorProtos.MethodOptions getOptions() {
         return this.proto.getOptions();
      }

      private MethodDescriptor(DescriptorProtos.MethodDescriptorProto proto, Descriptors.FileDescriptor file, Descriptors.ServiceDescriptor parent, int index) throws Descriptors.DescriptorValidationException {
         this.index = index;
         this.proto = proto;
         this.file = file;
         this.service = parent;
         this.fullName = parent.getFullName() + '.' + proto.getName();
         file.pool.addSymbol(this);
      }

      private void crossLink() throws Descriptors.DescriptorValidationException {
         Descriptors.GenericDescriptor input = this.getFile()
            .pool
            .lookupSymbol(this.proto.getInputType(), this, Descriptors.DescriptorPool.SearchFilter.TYPES_ONLY);
         if (!(input instanceof Descriptors.Descriptor)) {
            throw new Descriptors.DescriptorValidationException(this, '"' + this.proto.getInputType() + "\" is not a message type.");
         } else {
            this.inputType = (Descriptors.Descriptor)input;
            Descriptors.GenericDescriptor output = this.getFile()
               .pool
               .lookupSymbol(this.proto.getOutputType(), this, Descriptors.DescriptorPool.SearchFilter.TYPES_ONLY);
            if (!(output instanceof Descriptors.Descriptor)) {
               throw new Descriptors.DescriptorValidationException(this, '"' + this.proto.getOutputType() + "\" is not a message type.");
            } else {
               this.outputType = (Descriptors.Descriptor)output;
            }
         }
      }

      private void setProto(DescriptorProtos.MethodDescriptorProto proto) {
         this.proto = proto;
      }
   }

   private interface NumberGetter<T> {
      int getNumber(T var1);
   }

   public static final class OneofDescriptor extends Descriptors.GenericDescriptor {
      private final int index;
      private DescriptorProtos.OneofDescriptorProto proto;
      private final String fullName;
      private final Descriptors.FileDescriptor file;
      private Descriptors.Descriptor containingType;
      private int fieldCount;
      private Descriptors.FieldDescriptor[] fields;

      public int getIndex() {
         return this.index;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this.file;
      }

      @Override
      public String getFullName() {
         return this.fullName;
      }

      public Descriptors.Descriptor getContainingType() {
         return this.containingType;
      }

      public int getFieldCount() {
         return this.fieldCount;
      }

      public DescriptorProtos.OneofOptions getOptions() {
         return this.proto.getOptions();
      }

      public boolean isSynthetic() {
         return this.fields.length == 1 && this.fields[0].isProto3Optional;
      }

      public List<Descriptors.FieldDescriptor> getFields() {
         return Collections.unmodifiableList(Arrays.asList(this.fields));
      }

      public Descriptors.FieldDescriptor getField(int index) {
         return this.fields[index];
      }

      public DescriptorProtos.OneofDescriptorProto toProto() {
         return this.proto;
      }

      private void setProto(DescriptorProtos.OneofDescriptorProto proto) {
         this.proto = proto;
      }

      private OneofDescriptor(DescriptorProtos.OneofDescriptorProto proto, Descriptors.FileDescriptor file, Descriptors.Descriptor parent, int index) {
         this.proto = proto;
         this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
         this.file = file;
         this.index = index;
         this.containingType = parent;
         this.fieldCount = 0;
      }
   }

   public static final class ServiceDescriptor extends Descriptors.GenericDescriptor {
      private final int index;
      private DescriptorProtos.ServiceDescriptorProto proto;
      private final String fullName;
      private final Descriptors.FileDescriptor file;
      private Descriptors.MethodDescriptor[] methods;

      public int getIndex() {
         return this.index;
      }

      public DescriptorProtos.ServiceDescriptorProto toProto() {
         return this.proto;
      }

      @Override
      public String getName() {
         return this.proto.getName();
      }

      @Override
      public String getFullName() {
         return this.fullName;
      }

      @Override
      public Descriptors.FileDescriptor getFile() {
         return this.file;
      }

      public DescriptorProtos.ServiceOptions getOptions() {
         return this.proto.getOptions();
      }

      public List<Descriptors.MethodDescriptor> getMethods() {
         return Collections.unmodifiableList(Arrays.asList(this.methods));
      }

      public Descriptors.MethodDescriptor findMethodByName(String name) {
         Descriptors.GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
         return result instanceof Descriptors.MethodDescriptor ? (Descriptors.MethodDescriptor)result : null;
      }

      private ServiceDescriptor(DescriptorProtos.ServiceDescriptorProto proto, Descriptors.FileDescriptor file, int index) throws Descriptors.DescriptorValidationException {
         this.index = index;
         this.proto = proto;
         this.fullName = Descriptors.computeFullName(file, null, proto.getName());
         this.file = file;
         this.methods = new Descriptors.MethodDescriptor[proto.getMethodCount()];

         for(int i = 0; i < proto.getMethodCount(); ++i) {
            this.methods[i] = new Descriptors.MethodDescriptor(proto.getMethod(i), file, this, i);
         }

         file.pool.addSymbol(this);
      }

      private void crossLink() throws Descriptors.DescriptorValidationException {
         for(Descriptors.MethodDescriptor method : this.methods) {
            method.crossLink();
         }

      }

      private void setProto(DescriptorProtos.ServiceDescriptorProto proto) {
         this.proto = proto;

         for(int i = 0; i < this.methods.length; ++i) {
            this.methods[i].setProto(proto.getMethod(i));
         }

      }
   }
}
