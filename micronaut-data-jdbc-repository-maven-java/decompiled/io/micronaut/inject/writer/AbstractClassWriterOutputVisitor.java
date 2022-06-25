package io.micronaut.inject.writer;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ast.Element;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

@Internal
public abstract class AbstractClassWriterOutputVisitor implements ClassWriterOutputVisitor {
   private final Map<String, Set<String>> serviceDescriptors = new LinkedHashMap();
   private final boolean isWriteOnFinish;

   protected AbstractClassWriterOutputVisitor(boolean isWriteOnFinish) {
      this.isWriteOnFinish = isWriteOnFinish;
   }

   public AbstractClassWriterOutputVisitor() {
      this.isWriteOnFinish = false;
   }

   @Override
   public final Map<String, Set<String>> getServiceEntries() {
      return this.serviceDescriptors;
   }

   @Override
   public final void visitServiceDescriptor(String type, String classname) {
      if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(classname)) {
         ((Set)this.serviceDescriptors.computeIfAbsent(type, s -> new LinkedHashSet())).add(classname);
      }

   }

   @Override
   public final void finish() {
      if (this.isWriteOnFinish) {
         Map<String, Set<String>> serviceEntries = this.getServiceEntries();
         this.writeServiceEntries(serviceEntries);
      }

   }

   public void writeServiceEntries(Map<String, Set<String>> serviceEntries, Element... originatingElements) {
      for(Entry<String, Set<String>> entry : serviceEntries.entrySet()) {
         String serviceName = (String)entry.getKey();
         Set<String> serviceTypes = new TreeSet((Collection)entry.getValue());
         Optional<GeneratedFile> serviceFile = this.visitMetaInfFile("services/" + serviceName, originatingElements);
         if (serviceFile.isPresent()) {
            GeneratedFile generatedFile = (GeneratedFile)serviceFile.get();

            try {
               BufferedReader bufferedReader = new BufferedReader(generatedFile.openReader());
               Throwable var50 = null;

               try {
                  for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                     serviceTypes.add(line);
                  }
               } catch (Throwable var41) {
                  var50 = var41;
                  throw var41;
               } finally {
                  if (bufferedReader != null) {
                     if (var50 != null) {
                        try {
                           bufferedReader.close();
                        } catch (Throwable var40) {
                           var50.addSuppressed(var40);
                        }
                     } else {
                        bufferedReader.close();
                     }
                  }

               }
            } catch (NoSuchFileException | FileNotFoundException var43) {
            } catch (IOException var44) {
               Throwable cause = var44.getCause();
               if (this.isNotEclipseNotFound(cause)) {
                  throw new ClassGenerationException("Failed to load existing service definition files: " + var44, var44);
               }
            } catch (Throwable var45) {
               if (this.isNotEclipseNotFound(var45)) {
                  throw new ClassGenerationException("Failed to load existing service definition files: " + var45, var45);
               }
            }

            try {
               BufferedWriter writer = new BufferedWriter(generatedFile.openWriter());
               Throwable var51 = null;

               try {
                  for(String serviceType : serviceTypes) {
                     writer.write(serviceType);
                     writer.newLine();
                  }
               } catch (Throwable var46) {
                  var51 = var46;
                  throw var46;
               } finally {
                  if (writer != null) {
                     if (var51 != null) {
                        try {
                           writer.close();
                        } catch (Throwable var39) {
                           var51.addSuppressed(var39);
                        }
                     } else {
                        writer.close();
                     }
                  }

               }
            } catch (IOException var48) {
               throw new ClassGenerationException("Failed to open writer for service definition files: " + var48);
            }
         }
      }

   }

   private boolean isNotEclipseNotFound(Throwable e) {
      if (this.isWriteOnFinish) {
         return false;
      } else {
         String message = e.getMessage();
         return !message.contains("does not exist") || !e.getClass().getName().startsWith("org.eclipse");
      }
   }
}
