package io.micronaut.flyway.graalvm;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.micronaut.core.annotation.Internal;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
@AutomaticFeature
final class FlywayFeature implements Feature {
   private static final Logger LOG = LoggerFactory.getLogger(FlywayFeature.class);
   private static final String CLASSPATH_APPLICATION_MIGRATIONS_PROTOCOL = "classpath";
   private static final String JAR_APPLICATION_MIGRATIONS_PROTOCOL = "jar";
   private static final String FILE_APPLICATION_MIGRATIONS_PROTOCOL = "file";
   private static final String FLYWAY_LOCATIONS = "flyway.locations";
   private static final String DEFAULT_FLYWAY_LOCATIONS = "classpath:db/migration";

   public void beforeAnalysis(BeforeAnalysisAccess access) {
      List<String> locations = (List)Stream.of(System.getProperty("flyway.locations", "classpath:db/migration").split(",")).collect(Collectors.toList());

      try {
         List<String> migrations = this.discoverApplicationMigrations(locations);
         MicronautPathLocationScanner.setApplicationMigrationFiles(migrations);
      } catch (URISyntaxException | IOException var4) {
         LOG.error("There was an error discovering the Flyway migrations: {}", var4.getMessage());
      }

   }

   private List<String> discoverApplicationMigrations(List<String> locations) throws IOException, URISyntaxException {
      List<String> applicationMigrationResources = new ArrayList();

      for(String location : locations) {
         if (location != null && location.startsWith("classpath:")) {
            location = location.substring("classpath".length() + 1);
         }

         Enumeration<URL> migrations = Thread.currentThread().getContextClassLoader().getResources(location);

         while(migrations.hasMoreElements()) {
            URL path = (URL)migrations.nextElement();
            LOG.debug("Adding application migrations in path '{}' using protocol '{}'", path.getPath(), path.getProtocol());
            Set<String> applicationMigrations;
            if ("jar".equals(path.getProtocol())) {
               FileSystem fileSystem = this.initFileSystem(path.toURI());
               Throwable var9 = null;

               try {
                  applicationMigrations = this.getApplicationMigrationsFromPath(location, path);
               } catch (Throwable var18) {
                  var9 = var18;
                  throw var18;
               } finally {
                  if (fileSystem != null) {
                     if (var9 != null) {
                        try {
                           fileSystem.close();
                        } catch (Throwable var17) {
                           var9.addSuppressed(var17);
                        }
                     } else {
                        fileSystem.close();
                     }
                  }

               }
            } else if ("file".equals(path.getProtocol())) {
               applicationMigrations = this.getApplicationMigrationsFromPath(location, path);
            } else {
               LOG.warn("Unsupported URL protocol '{}' for path '{}'. Migration files will not be discovered.", path.getProtocol(), path.getPath());
               applicationMigrations = null;
            }

            if (applicationMigrations != null) {
               applicationMigrationResources.addAll(applicationMigrations);
            }
         }
      }

      return applicationMigrationResources;
   }

   private Set<String> getApplicationMigrationsFromPath(final String location, final URL path) throws IOException, URISyntaxException {
      Stream<Path> pathStream = Files.walk(Paths.get(path.toURI()));
      Throwable var4 = null;

      Set var5;
      try {
         var5 = (Set)pathStream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0]))
            .map(it -> Paths.get(location, it.getFileName().toString()).toString())
            .map(it -> it.replace('\\', '/'))
            .peek(it -> LOG.trace("Discovered path: {}", it))
            .collect(Collectors.toSet());
      } catch (Throwable var14) {
         var4 = var14;
         throw var14;
      } finally {
         if (pathStream != null) {
            if (var4 != null) {
               try {
                  pathStream.close();
               } catch (Throwable var13) {
                  var4.addSuppressed(var13);
               }
            } else {
               pathStream.close();
            }
         }

      }

      return var5;
   }

   private FileSystem initFileSystem(final URI uri) throws IOException {
      return FileSystems.newFileSystem(uri, Collections.singletonMap("create", "true"));
   }
}
