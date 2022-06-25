package org.flywaydb.core.internal.scanner.classpath;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv3ClassPathLocationScanner;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.UrlUtils;

public class ClassPathScanner<I> implements ResourceAndClassScanner<I> {
   private static final Log LOG = LogFactory.getLog(ClassPathScanner.class);
   private final Class<I> implementedInterface;
   private final ClassLoader classLoader;
   private final Location location;
   private final Set<LoadableResource> resources = new HashSet();
   private final Map<Location, List<URL>> locationUrlCache = new HashMap();
   private final LocationScannerCache locationScannerCache;
   private final ResourceNameCache resourceNameCache;
   private final boolean throwOnMissingLocations;

   public ClassPathScanner(
      Class<I> implementedInterface,
      ClassLoader classLoader,
      Charset encoding,
      Location location,
      ResourceNameCache resourceNameCache,
      LocationScannerCache locationScannerCache,
      boolean throwOnMissingLocations
   ) {
      this.implementedInterface = implementedInterface;
      this.classLoader = classLoader;
      this.location = location;
      this.resourceNameCache = resourceNameCache;
      this.locationScannerCache = locationScannerCache;
      this.throwOnMissingLocations = throwOnMissingLocations;
      LOG.debug("Scanning for classpath resources at '" + location + "' ...");

      for(Pair<String, String> resourceNameAndParentURL : this.findResourceNamesAndParentURLs()) {
         String resourceName = (String)resourceNameAndParentURL.getLeft();
         String parentURL = (String)resourceNameAndParentURL.getRight();
         this.resources.add(new ClassPathResource(location, resourceName, classLoader, encoding, parentURL));
         LOG.debug("Found resource: " + (String)resourceNameAndParentURL.getLeft());
      }

   }

   @Override
   public Collection<LoadableResource> scanForResources() {
      return this.resources;
   }

   @Override
   public Collection<Class<? extends I>> scanForClasses() {
      LOG.debug("Scanning for classes at " + this.location);
      List<Class<? extends I>> classes = new ArrayList();

      for(LoadableResource resource : this.resources) {
         if (resource.getAbsolutePath().endsWith(".class")) {
            Class<? extends I> clazz;
            try {
               clazz = ClassUtils.loadClass(this.implementedInterface, this.toClassName(resource.getAbsolutePath()), this.classLoader);
            } catch (Throwable var7) {
               Throwable rootCause = ExceptionUtils.getRootCause(var7);
               LOG.warn(
                  "Skipping "
                     + Callback.class
                     + ": "
                     + ClassUtils.formatThrowable(var7)
                     + (rootCause == var7 ? "" : " caused by " + ClassUtils.formatThrowable(rootCause) + " at " + ExceptionUtils.getThrowLocation(rootCause))
               );
               clazz = null;
            }

            if (clazz != null) {
               classes.add(clazz);
            }
         }
      }

      return classes;
   }

   private String toClassName(String resourceName) {
      String nameWithDots = resourceName.replace("/", ".");
      return nameWithDots.substring(0, nameWithDots.length() - ".class".length());
   }

   private Set<Pair<String, String>> findResourceNamesAndParentURLs() {
      Set<Pair<String, String>> resourceNamesAndParentURLs = new TreeSet();
      List<URL> locationUrls = this.getLocationUrlsForPath(this.location);

      for(URL locationUrl : locationUrls) {
         LOG.debug("Scanning URL: " + locationUrl.toExternalForm());
         UrlResolver urlResolver = this.createUrlResolver(locationUrl.getProtocol());
         URL resolvedUrl = urlResolver.toStandardJavaUrl(locationUrl);
         String protocol = resolvedUrl.getProtocol();
         ClassPathLocationScanner classPathLocationScanner = this.createLocationScanner(protocol);
         if (classPathLocationScanner == null) {
            String scanRoot = UrlUtils.toFilePath(resolvedUrl);
            LOG.warn("Unable to scan location: " + scanRoot + " (unsupported protocol: " + protocol + ")");
         } else {
            Set<String> names = this.resourceNameCache.get(classPathLocationScanner, resolvedUrl);
            if (names == null) {
               names = classPathLocationScanner.findResourceNames(this.location.getRootPath(), resolvedUrl);
               this.resourceNameCache.put(classPathLocationScanner, resolvedUrl, names);
            }

            Set<String> filteredNames = new HashSet();

            for(String name : names) {
               if (this.location.matchesPath(name)) {
                  filteredNames.add(name);
               }
            }

            for(String filteredName : filteredNames) {
               resourceNamesAndParentURLs.add(Pair.of(filteredName, resolvedUrl.getPath()));
            }
         }
      }

      boolean locationResolved = !locationUrls.isEmpty();
      boolean isClassPathRoot = this.location.isClassPath() && "".equals(this.location.getRootPath());
      if ((!locationResolved || isClassPathRoot) && this.classLoader instanceof URLClassLoader) {
         URLClassLoader urlClassLoader = (URLClassLoader)this.classLoader;

         for(URL url : urlClassLoader.getURLs()) {
            if ("file".equals(url.getProtocol()) && url.getPath().endsWith(".jar") && !url.getPath().matches(".*" + Pattern.quote("/jre/lib/") + ".*")) {
               JarFile jarFile;
               try {
                  try {
                     jarFile = new JarFile(url.toURI().getSchemeSpecificPart());
                  } catch (URISyntaxException var21) {
                     jarFile = new JarFile(url.getPath().substring("file:".length()));
                  }
               } catch (SecurityException | IOException var23) {
                  LOG.warn("Skipping unloadable jar file: " + url + " (" + var23.getMessage() + ")");
                  continue;
               }

               try {
                  Enumeration<JarEntry> entries = jarFile.entries();

                  while(entries.hasMoreElements()) {
                     String entryName = ((JarEntry)entries.nextElement()).getName();
                     if (entryName.startsWith(this.location.getRootPath())) {
                        locationResolved = true;
                        resourceNamesAndParentURLs.add(Pair.of(entryName, url.getPath()));
                     }
                  }
               } finally {
                  try {
                     jarFile.close();
                  } catch (IOException var20) {
                  }

               }
            }
         }
      }

      if (!locationResolved) {
         String message = "Unable to resolve location " + this.location + ".";
         if (this.throwOnMissingLocations) {
            throw new FlywayException(message);
         }

         LOG.debug(message);
      }

      return resourceNamesAndParentURLs;
   }

   private List<URL> getLocationUrlsForPath(Location location) {
      if (this.locationUrlCache.containsKey(location)) {
         return (List<URL>)this.locationUrlCache.get(location);
      } else {
         LOG.debug("Determining location urls for " + location + " using ClassLoader " + this.classLoader + " ...");
         List<URL> locationUrls = new ArrayList();
         if (this.classLoader.getClass().getName().startsWith("com.ibm")) {
            try {
               Enumeration<URL> urls = this.classLoader.getResources(location.getRootPath() + "/flyway.location");
               if (!urls.hasMoreElements()) {
                  LOG.error(
                     "Unable to resolve location "
                        + location
                        + " (ClassLoader: "
                        + this.classLoader
                        + ") On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!"
                  );
               }

               while(urls.hasMoreElements()) {
                  URL url = (URL)urls.nextElement();
                  locationUrls.add(new URL(UrlUtils.decodeURL(url.toExternalForm()).replace("/flyway.location", "")));
               }
            } catch (IOException var6) {
               LOG.error(
                  "Unable to resolve location "
                     + location
                     + " (ClassLoader: "
                     + this.classLoader
                     + ") On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!"
               );
            }
         } else {
            try {
               Enumeration<URL> urls = this.classLoader.getResources(location.getRootPath());

               while(urls.hasMoreElements()) {
                  locationUrls.add((URL)urls.nextElement());
               }
            } catch (IOException var5) {
               LOG.error("Unable to resolve location " + location + " (ClassLoader: " + this.classLoader + "): " + var5.getMessage() + ".");
            }
         }

         this.locationUrlCache.put(location, locationUrls);
         return locationUrls;
      }
   }

   private UrlResolver createUrlResolver(String protocol) {
      return (UrlResolver)(new FeatureDetector(this.classLoader).isJBossVFSv2Available() && protocol.startsWith("vfs")
         ? new JBossVFSv2UrlResolver()
         : new DefaultUrlResolver());
   }

   private ClassPathLocationScanner createLocationScanner(String protocol) {
      if (this.locationScannerCache.containsKey(protocol)) {
         return this.locationScannerCache.get(protocol);
      } else if ("file".equals(protocol)) {
         FileSystemClassPathLocationScanner locationScanner = new FileSystemClassPathLocationScanner();
         this.locationScannerCache.put(protocol, locationScanner);
         this.resourceNameCache.put(locationScanner, new HashMap());
         return locationScanner;
      } else if (!"jar".equals(protocol) && !this.isTomcat(protocol) && !this.isWebLogic(protocol) && !this.isWebSphere(protocol)) {
         FeatureDetector featureDetector = new FeatureDetector(this.classLoader);
         if (featureDetector.isJBossVFSv3Available() && "vfs".equals(protocol)) {
            JBossVFSv3ClassPathLocationScanner locationScanner = new JBossVFSv3ClassPathLocationScanner();
            this.locationScannerCache.put(protocol, locationScanner);
            this.resourceNameCache.put(locationScanner, new HashMap());
            return locationScanner;
         } else if (!featureDetector.isOsgiFrameworkAvailable() || !this.isFelix(protocol) && !this.isEquinox(protocol)) {
            return null;
         } else {
            OsgiClassPathLocationScanner locationScanner = new OsgiClassPathLocationScanner();
            this.locationScannerCache.put(protocol, locationScanner);
            this.resourceNameCache.put(locationScanner, new HashMap());
            return locationScanner;
         }
      } else {
         String separator = this.isTomcat(protocol) ? "*/" : "!/";
         ClassPathLocationScanner locationScanner = new JarFileClassPathLocationScanner(separator);
         this.locationScannerCache.put(protocol, locationScanner);
         this.resourceNameCache.put(locationScanner, new HashMap());
         return locationScanner;
      }
   }

   private boolean isEquinox(String protocol) {
      return "bundleresource".equals(protocol);
   }

   private boolean isFelix(String protocol) {
      return "bundle".equals(protocol);
   }

   private boolean isWebSphere(String protocol) {
      return "wsjar".equals(protocol);
   }

   private boolean isWebLogic(String protocol) {
      return "zip".equals(protocol);
   }

   private boolean isTomcat(String protocol) {
      return "war".equals(protocol);
   }
}
