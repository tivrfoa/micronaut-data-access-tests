package org.flywaydb.core.internal.scanner.classpath.jboss;

import java.lang.reflect.Method;
import java.net.URL;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.scanner.classpath.UrlResolver;

public class JBossVFSv2UrlResolver implements UrlResolver {
   @Override
   public URL toStandardJavaUrl(URL url) {
      try {
         Class<?> vfsClass = Class.forName("org.jboss.virtual.VFS");
         Class<?> vfsUtilsClass = Class.forName("org.jboss.virtual.VFSUtils");
         Class<?> virtualFileClass = Class.forName("org.jboss.virtual.VirtualFile");
         Method getRootMethod = vfsClass.getMethod("getRoot", URL.class);
         Method getRealURLMethod = vfsUtilsClass.getMethod("getRealURL", virtualFileClass);
         Object root = getRootMethod.invoke(null, url);
         return (URL)getRealURLMethod.invoke(null, root);
      } catch (Exception var8) {
         throw new FlywayException("JBoss VFS v2 call failed", var8);
      }
   }
}
