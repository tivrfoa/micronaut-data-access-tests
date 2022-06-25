package io.micronaut.context.i18n;

import io.micronaut.context.AbstractMessageSource;
import io.micronaut.context.MessageSource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.clhm.ConcurrentLinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBundleMessageSource extends AbstractMessageSource {
   private static final Logger LOG = LoggerFactory.getLogger(ResourceBundleMessageSource.class);
   private final String baseName;
   private final Map<AbstractMessageSource.MessageKey, Optional<String>> messageCache = this.buildMessageCache();
   private final Map<AbstractMessageSource.MessageKey, Optional<ResourceBundle>> bundleCache = this.buildBundleCache();
   @Nullable
   private final ResourceBundle defaultBundle;

   public ResourceBundleMessageSource(@NonNull String baseName) {
      this(baseName, null);
   }

   public ResourceBundleMessageSource(@NonNull String baseName, @Nullable Locale defaultLocale) {
      ArgumentUtils.requireNonNull("baseName", baseName);
      this.baseName = baseName;

      ResourceBundle defaultBundle;
      try {
         if (defaultLocale != null) {
            defaultBundle = ResourceBundle.getBundle(baseName, defaultLocale, this.getClassLoader());
         } else {
            defaultBundle = ResourceBundle.getBundle(baseName);
         }
      } catch (MissingResourceException var5) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("No default bundle (locale: " + defaultLocale + ") found for base name " + baseName);
         }

         defaultBundle = null;
      }

      this.defaultBundle = defaultBundle;
   }

   @NonNull
   @Override
   public Optional<String> getRawMessage(@NonNull String code, @NonNull MessageSource.MessageContext context) {
      Locale locale = this.defaultBundle != null ? context.getLocale(this.defaultBundle.getLocale()) : context.getLocale();
      AbstractMessageSource.MessageKey messageKey = new AbstractMessageSource.MessageKey(locale, code);
      Optional<String> opt = (Optional)this.messageCache.get(messageKey);
      if (opt == null) {
         try {
            Optional<ResourceBundle> bundle = this.resolveBundle(locale);
            if (bundle.isPresent()) {
               return bundle.map(b -> b.getString(code));
            }

            return this.resolveDefault(code);
         } catch (MissingResourceException var7) {
            opt = this.resolveDefault(code);
            this.messageCache.put(messageKey, opt);
         }
      }

      return opt;
   }

   protected ClassLoader getClassLoader() {
      return this.getClass().getClassLoader();
   }

   @NonNull
   protected Map<AbstractMessageSource.MessageKey, Optional<String>> buildMessageCache() {
      return new ConcurrentLinkedHashMap.Builder().maximumWeightedCapacity(100L).build();
   }

   @NonNull
   protected Map<AbstractMessageSource.MessageKey, Optional<ResourceBundle>> buildBundleCache() {
      return new ConcurrentHashMap(18);
   }

   @NonNull
   private Optional<String> resolveDefault(@NonNull String code) {
      Optional<String> opt;
      if (this.defaultBundle != null) {
         try {
            opt = Optional.of(this.defaultBundle.getString(code));
         } catch (MissingResourceException var4) {
            opt = Optional.empty();
         }
      } else {
         opt = Optional.empty();
      }

      return opt;
   }

   private Optional<ResourceBundle> resolveBundle(Locale locale) {
      AbstractMessageSource.MessageKey key = new AbstractMessageSource.MessageKey(locale, this.baseName);
      Optional<ResourceBundle> resourceBundle = (Optional)this.bundleCache.get(key);
      if (resourceBundle != null) {
         return resourceBundle;
      } else {
         Optional<ResourceBundle> opt;
         try {
            opt = Optional.of(ResourceBundle.getBundle(this.baseName, locale, this.getClassLoader()));
         } catch (MissingResourceException var6) {
            opt = Optional.empty();
         }

         this.bundleCache.put(key, opt);
         return opt;
      }
   }
}
