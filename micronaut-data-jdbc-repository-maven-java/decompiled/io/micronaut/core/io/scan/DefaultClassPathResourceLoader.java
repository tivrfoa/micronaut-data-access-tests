package io.micronaut.core.io.scan;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.clhm.ConcurrentLinkedHashMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultClassPathResourceLoader implements ClassPathResourceLoader {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultClassPathResourceLoader.class);
   private final ClassLoader classLoader;
   private final String basePath;
   private final URL baseURL;
   private final Map<String, Boolean> isDirectoryCache = new ConcurrentLinkedHashMap.Builder().maximumWeightedCapacity(50L).build();
   private final boolean missingPath;
   private final boolean checkBase;

   public DefaultClassPathResourceLoader(ClassLoader classLoader) {
      this(classLoader, null);
   }

   public DefaultClassPathResourceLoader(ClassLoader classLoader, String basePath) {
      this(classLoader, basePath, false);
   }

   public DefaultClassPathResourceLoader(ClassLoader classLoader, String basePath, boolean checkBase) {
      this.classLoader = classLoader;
      this.basePath = this.normalize(basePath);
      this.baseURL = checkBase && basePath != null ? classLoader.getResource(this.normalize(basePath)) : null;
      this.missingPath = checkBase && basePath != null && this.baseURL == null;
      this.checkBase = checkBase;
   }

   @Override
   public Optional<InputStream> getResourceAsStream(String path) {
      if (this.missingPath) {
         return Optional.empty();
      } else if (this.isProhibitedRelativePath(path)) {
         return Optional.empty();
      } else {
         URL url = this.classLoader.getResource(this.prefixPath(path));
         if (url != null && this.startsWithBase(url)) {
            try {
               URI uri = url.toURI();
               if (uri.getScheme().equals("jar")) {
                  synchronized(DefaultClassPathResourceLoader.class) {
                     FileSystem fileSystem = null;

                     Optional var7;
                     try {
                        try {
                           fileSystem = FileSystems.getFileSystem(uri);
                        } catch (FileSystemNotFoundException var22) {
                        }

                        if (fileSystem == null || !fileSystem.isOpen()) {
                           try {
                              fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap(), this.classLoader);
                           } catch (FileSystemAlreadyExistsException var21) {
                              fileSystem = FileSystems.getFileSystem(uri);
                           }
                        }

                        Path pathObject = fileSystem.getPath(path);
                        if (!Files.isDirectory(pathObject, new LinkOption[0])) {
                           return Optional.of(new ByteArrayInputStream(Files.readAllBytes(pathObject)));
                        }

                        var7 = Optional.empty();
                     } finally {
                        if (fileSystem != null && fileSystem.isOpen()) {
                           try {
                              fileSystem.close();
                           } catch (IOException var24) {
                              if (LOG.isDebugEnabled()) {
                                 LOG.debug("Error shutting down JAR file system [" + fileSystem + "]: " + var24.getMessage(), var24);
                              }
                           }
                        }

                     }

                     return var7;
                  }
               }

               if (uri.getScheme().equals("file")) {
                  Path pathObject = Paths.get(uri);
                  if (Files.isDirectory(pathObject, new LinkOption[0])) {
                     return Optional.empty();
                  }

                  return Optional.of(Files.newInputStream(pathObject));
               }
            } catch (IOException | ProviderNotFoundException | URISyntaxException var27) {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Error establishing whether path is a directory: " + var27.getMessage(), var27);
               }
            }
         }

         if (path.indexOf(46) == -1) {
            return Optional.empty();
         } else {
            URL u = (URL)this.getResource(path).orElse(null);
            if (u != null) {
               try {
                  return Optional.of(u.openStream());
               } catch (IOException var23) {
               }
            }

            return Optional.empty();
         }
      }
   }

   private boolean startsWithBase(URL url) {
      if (this.checkBase) {
         return this.baseURL == null ? true : url.toExternalForm().startsWith(this.baseURL.toExternalForm());
      } else {
         return true;
      }
   }

   @Override
   public Optional<URL> getResource(String path) {
      if (this.missingPath) {
         return Optional.empty();
      } else if (this.isProhibitedRelativePath(path)) {
         return Optional.empty();
      } else {
         boolean isDirectory = this.isDirectory(path);
         if (!isDirectory) {
            URL url = this.classLoader.getResource(this.prefixPath(path));
            if (url != null && this.startsWithBase(url)) {
               return Optional.of(url);
            }
         }

         return Optional.empty();
      }
   }

   private boolean isProhibitedRelativePath(String path) {
      return !this.checkBase && path.replace('\\', '/').contains("../");
   }

   @Override
   public Stream<URL> getResources(String path) {
      if (this.missingPath) {
         return Stream.empty();
      } else if (this.isProhibitedRelativePath(path)) {
         return Stream.empty();
      } else {
         Enumeration<URL> all;
         try {
            all = this.classLoader.getResources(this.prefixPath(path));
         } catch (IOException var5) {
            return Stream.empty();
         }

         Builder<URL> builder = Stream.builder();

         while(all.hasMoreElements()) {
            URL url = (URL)all.nextElement();
            if (this.startsWithBase(url)) {
               builder.accept(url);
            }
         }

         return builder.build();
      }
   }

   @Override
   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   @Override
   public ResourceLoader forBase(String basePath) {
      return new DefaultClassPathResourceLoader(this.classLoader, basePath);
   }

   private String normalize(String path) {
      if (path != null) {
         if (path.startsWith("classpath:")) {
            path = path.substring(10);
         }

         if (path.startsWith("/")) {
            path = path.substring(1);
         }

         if (!path.endsWith("/") && StringUtils.isNotEmpty(path)) {
            path = path + "/";
         }
      }

      return path;
   }

   private boolean isDirectory(String path) {
      return this.isDirectoryCache.computeIfAbsent(path, s -> {
         URL url = this.classLoader.getResource(this.prefixPath(path));
         if (url != null) {
            try {
               URI uri = url.toURI();
               if (uri.getScheme().equals("jar")) {
                  synchronized(DefaultClassPathResourceLoader.class) {
                     FileSystem fileSystem = null;

                     Boolean var8;
                     try {
                        try {
                           fileSystem = FileSystems.getFileSystem(uri);
                        } catch (FileSystemNotFoundException var19) {
                        }

                        if (fileSystem == null || !fileSystem.isOpen()) {
                           fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap(), this.classLoader);
                        }

                        Path pathObject = fileSystem.getPath(path);
                        var8 = pathObject == null || Files.isDirectory(pathObject, new LinkOption[0]);
                     } finally {
                        if (fileSystem != null && fileSystem.isOpen()) {
                           try {
                              fileSystem.close();
                           } catch (IOException var20) {
                              if (LOG.isDebugEnabled()) {
                                 LOG.debug("Error shutting down JAR file system [" + fileSystem + "]: " + var20.getMessage(), var20);
                              }
                           }
                        }

                     }

                     return var8;
                  }
               }

               if (uri.getScheme().equals("file")) {
                  Path pathObject = Paths.get(uri);
                  return pathObject == null || Files.isDirectory(pathObject, new LinkOption[0]);
               }
            } catch (IOException | ProviderNotFoundException | URISyntaxException var23) {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Error establishing whether path is a directory: " + var23.getMessage(), var23);
               }
            }
         }

         return path.indexOf(46) == -1;
      });
   }

   private String prefixPath(String path) {
      if (path.startsWith("classpath:")) {
         path = path.substring(10);
      }

      if (this.basePath != null) {
         return path.startsWith("/") ? this.basePath + path.substring(1) : this.basePath + path;
      } else {
         return path;
      }
   }
}
