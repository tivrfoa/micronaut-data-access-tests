package io.micronaut.core.io.file;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.util.SupplierUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DefaultFileSystemResourceLoader implements FileSystemResourceLoader {
   private final Supplier<DefaultFileSystemResourceLoader.BaseDir> baseDir;

   public DefaultFileSystemResourceLoader() {
      this.baseDir = SupplierUtil.memoized(DefaultFileSystemResourceLoader.BaseDir::new);
   }

   public DefaultFileSystemResourceLoader(File baseDirPath) {
      this(baseDirPath.toPath().normalize());
   }

   public DefaultFileSystemResourceLoader(String path) {
      this(Paths.get(normalize(path)));
   }

   public DefaultFileSystemResourceLoader(Path path) {
      this.baseDir = SupplierUtil.memoizedNonEmpty(() -> {
         try {
            Path baseDirPath = path.normalize().toRealPath();
            return new DefaultFileSystemResourceLoader.BaseDir(baseDirPath);
         } catch (IOException var3) {
            return null;
         }
      });
   }

   @Override
   public Optional<InputStream> getResourceAsStream(String path) {
      Path filePath = this.getFilePath(normalize(path));
      if (this.isResolvableFile(filePath)) {
         try {
            return Optional.of(Files.newInputStream(filePath));
         } catch (IOException var4) {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   @Override
   public Optional<URL> getResource(String path) {
      Path filePath = this.getFilePath(normalize(path));
      if (this.isResolvableFile(filePath)) {
         try {
            URL url = filePath.toUri().toURL();
            return Optional.of(url);
         } catch (MalformedURLException var4) {
         }
      }

      return Optional.empty();
   }

   @Override
   public Stream<URL> getResources(String name) {
      return (Stream<URL>)this.getResource(name).map(Stream::of).orElseGet(Stream::empty);
   }

   @Override
   public ResourceLoader forBase(String basePath) {
      return new DefaultFileSystemResourceLoader(basePath);
   }

   private boolean isResolvableFile(Path filePath) {
      return this.startsWithBase(filePath)
         && Files.exists(filePath, new LinkOption[0])
         && Files.isReadable(filePath)
         && !Files.isDirectory(filePath, new LinkOption[0]);
   }

   private static String normalize(String path) {
      if (path == null) {
         return null;
      } else {
         if (path.startsWith("file:")) {
            path = path.substring(5);
         }

         return path;
      }
   }

   private Path getFilePath(String path) {
      DefaultFileSystemResourceLoader.BaseDir base = (DefaultFileSystemResourceLoader.BaseDir)this.baseDir.get();
      return base != null ? base.resolve(path) : Paths.get(path);
   }

   private boolean startsWithBase(Path path) {
      DefaultFileSystemResourceLoader.BaseDir base = (DefaultFileSystemResourceLoader.BaseDir)this.baseDir.get();
      return base != null ? base.startsWith(path) : false;
   }

   private static class BaseDir {
      final boolean exists;
      final Path dir;

      BaseDir() {
         this.exists = true;
         this.dir = null;
      }

      BaseDir(Path path) {
         Path baseDirPath;
         try {
            baseDirPath = path.normalize().toRealPath();
         } catch (IOException var4) {
            baseDirPath = null;
         }

         this.exists = baseDirPath != null;
         this.dir = baseDirPath;
      }

      Path resolve(String path) {
         return this.dir != null ? this.dir.resolve(path) : Paths.get(path);
      }

      boolean startsWith(Path path) {
         if (this.dir != null) {
            try {
               Path relativePath = this.dir.resolve(path).toRealPath();
               return relativePath.startsWith(this.dir);
            } catch (IOException var4) {
               return false;
            }
         } else {
            return this.exists;
         }
      }
   }
}
