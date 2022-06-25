package io.micronaut.core.io;

import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.NonNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtils {
   private static final int BUFFER_MAX = 8192;

   @Blocking
   public static void eachFile(@NonNull URL url, String path, @NonNull Consumer<Path> consumer) {
      try {
         eachFile(url.toURI(), path, consumer);
      } catch (URISyntaxException var4) {
      }

   }

   @Blocking
   public static void eachFile(@NonNull URI uri, String path, @NonNull Consumer<Path> consumer) {
      try {
         String scheme = uri.getScheme();
         FileSystem fileSystem = null;

         Path myPath;
         try {
            if ("jar".equals(scheme)) {
               try {
                  fileSystem = FileSystems.getFileSystem(uri);
               } catch (FileSystemNotFoundException var30) {
                  fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
               }

               myPath = fileSystem.getPath(path);
            } else if ("file".equals(scheme)) {
               myPath = Paths.get(uri).resolve(path);
            } else {
               myPath = Paths.get(uri);
            }
         } catch (FileSystemNotFoundException var31) {
            myPath = null;
         }

         if (myPath != null) {
            try {
               Stream<Path> walk = Files.walk(myPath, 1, new FileVisitOption[0]);
               Throwable var7 = null;

               try {
                  Iterator<Path> it = walk.iterator();

                  while(it.hasNext()) {
                     Path currentPath = (Path)it.next();
                     if (!currentPath.equals(myPath) && !Files.isHidden(currentPath) && !currentPath.getFileName().startsWith(".")) {
                        consumer.accept(currentPath);
                     }
                  }
               } catch (Throwable var32) {
                  var7 = var32;
                  throw var32;
               } finally {
                  if (walk != null) {
                     if (var7 != null) {
                        try {
                           walk.close();
                        } catch (Throwable var29) {
                           var7.addSuppressed(var29);
                        }
                     } else {
                        walk.close();
                     }
                  }

               }
            } finally {
               if (fileSystem != null && fileSystem.isOpen()) {
                  fileSystem.close();
               }

            }
         }
      } catch (IOException var35) {
      }

   }

   @Blocking
   public static String readText(BufferedReader reader) throws IOException {
      StringBuilder answer = new StringBuilder();
      if (reader == null) {
         return answer.toString();
      } else {
         char[] charBuffer = new char[8192];

         try {
            int nbCharRead;
            while((nbCharRead = reader.read(charBuffer)) != -1) {
               answer.append(charBuffer, 0, nbCharRead);
            }

            Reader temp = reader;
            reader = null;
            temp.close();
         } finally {
            try {
               if (reader != null) {
                  reader.close();
               }
            } catch (IOException var10) {
               if (IOUtils.IOLogging.LOG.isWarnEnabled()) {
                  IOUtils.IOLogging.LOG.warn("Failed to close reader: " + var10.getMessage(), var10);
               }
            }

         }

         return answer.toString();
      }
   }

   private static final class IOLogging {
      private static final Logger LOG = LoggerFactory.getLogger(IOUtils.IOLogging.class);
   }
}
