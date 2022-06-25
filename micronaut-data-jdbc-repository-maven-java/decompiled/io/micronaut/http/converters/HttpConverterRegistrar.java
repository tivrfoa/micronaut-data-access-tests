package io.micronaut.http.converters;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.io.Readable;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.MediaType;
import jakarta.inject.Singleton;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Optional;

@Singleton
public class HttpConverterRegistrar implements TypeConverterRegistrar {
   private final ResourceResolver resourceResolver;

   protected HttpConverterRegistrar(ResourceResolver resourceResolver) {
      this.resourceResolver = resourceResolver;
   }

   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(String.class, HttpVersion.class, s -> {
         try {
            return HttpVersion.valueOf(Double.parseDouble(s));
         } catch (NumberFormatException var2) {
            return HttpVersion.valueOf(s);
         }
      });
      conversionService.addConverter(Number.class, HttpVersion.class, s -> HttpVersion.valueOf(s.doubleValue()));
      conversionService.addConverter(
         CharSequence.class,
         Readable.class,
         (TypeConverter)((object, targetType, context) -> {
            String pathStr = object.toString();
            Optional<ResourceLoader> supportingLoader = this.resourceResolver.getSupportingLoader(pathStr);
            if (!supportingLoader.isPresent()) {
               context.reject(
                  pathStr,
                  new ConfigurationException(
                     "No supported resource loader for path [" + pathStr + "]. Prefix the path with a supported prefix such as 'classpath:' or 'file:'"
                  )
               );
               return Optional.empty();
            } else {
               Optional<URL> resource = this.resourceResolver.getResource(pathStr);
               if (resource.isPresent()) {
                  return Optional.of(Readable.of((URL)resource.get()));
               } else {
                  context.reject(object, new ConfigurationException("No resource exists for value: " + object));
                  return Optional.empty();
               }
            }
         })
      );
      conversionService.addConverter(CharSequence.class, MediaType.class, (TypeConverter)((object, targetType, context) -> {
         try {
            return Optional.of(MediaType.of(object));
         } catch (IllegalArgumentException var4) {
            context.reject(var4);
            return Optional.empty();
         }
      }));
      conversionService.addConverter(Number.class, HttpStatus.class, (TypeConverter)((object, targetType, context) -> {
         try {
            HttpStatus status = HttpStatus.valueOf(object.shortValue());
            return Optional.of(status);
         } catch (IllegalArgumentException var4) {
            context.reject(object, var4);
            return Optional.empty();
         }
      }));
      conversionService.addConverter(CharSequence.class, SocketAddress.class, (TypeConverter)((object, targetType, context) -> {
         String address = object.toString();

         try {
            URL url = new URL(address);
            int port = url.getPort();
            if (port == -1) {
               port = url.getDefaultPort();
            }

            if (port == -1) {
               context.reject(object, new ConfigurationException("Failed to find a port in the given value"));
               return Optional.empty();
            } else {
               return Optional.of(InetSocketAddress.createUnresolved(url.getHost(), port));
            }
         } catch (MalformedURLException var8) {
            String[] parts = object.toString().split(":");
            if (parts.length == 2) {
               try {
                  int port = Integer.parseInt(parts[1]);
                  return Optional.of(InetSocketAddress.createUnresolved(parts[0], port));
               } catch (IllegalArgumentException var7) {
                  context.reject(object, var7);
                  return Optional.empty();
               }
            } else {
               context.reject(object, new ConfigurationException("The address is not in a proper format of IP:PORT or a standard URL"));
               return Optional.empty();
            }
         }
      }));
      conversionService.addConverter(
         CharSequence.class,
         ProxySelector.class,
         (TypeConverter)((object, targetType, context) -> object.toString().equals("default") ? Optional.of(ProxySelector.getDefault()) : Optional.empty())
      );
   }
}
