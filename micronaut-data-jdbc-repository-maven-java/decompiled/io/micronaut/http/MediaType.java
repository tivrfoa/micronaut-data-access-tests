package io.micronaut.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.ImmutableArgumentConversionContext;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.OptionalValues;
import io.micronaut.http.annotation.Produces;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TypeHint({MediaType.class})
public class MediaType implements CharSequence {
   public static final String EXTENSION_JSON = "json";
   public static final String EXTENSION_XML = "xml";
   public static final String EXTENSION_PDF = "pdf";
   public static final String EXTENSION_XLSX = "xlsx";
   public static final String EXTENSION_XLS = "xls";
   public static final MediaType[] EMPTY_ARRAY = new MediaType[0];
   public static final String ALL = "*/*";
   public static final MediaType ALL_TYPE = new MediaType("*/*", "all");
   public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
   public static final MediaType APPLICATION_FORM_URLENCODED_TYPE = new MediaType("application/x-www-form-urlencoded");
   public static final MediaType FORM = APPLICATION_FORM_URLENCODED_TYPE;
   public static final String MULTIPART_FORM_DATA = "multipart/form-data";
   public static final MediaType MULTIPART_FORM_DATA_TYPE = new MediaType("multipart/form-data");
   public static final String TEXT_HTML = "text/html";
   public static final MediaType TEXT_HTML_TYPE = new MediaType("text/html");
   public static final String TEXT_CSV = "text/csv";
   public static final MediaType TEXT_CSV_TYPE = new MediaType("text/csv");
   public static final String APPLICATION_XHTML = "application/xhtml+xml";
   public static final MediaType APPLICATION_XHTML_TYPE = new MediaType("application/xhtml+xml", "html");
   public static final String APPLICATION_XML = "application/xml";
   public static final MediaType APPLICATION_XML_TYPE = new MediaType("application/xml");
   public static final String APPLICATION_JSON = "application/json";
   public static final MediaType APPLICATION_JSON_TYPE = new MediaType("application/json");
   public static final String APPLICATION_YAML = "application/x-yaml";
   public static final MediaType APPLICATION_YAML_TYPE = new MediaType("application/x-yaml");
   public static final String MICROSOFT_EXCEL_OPEN_XML = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
   public static final MediaType MICROSOFT_EXCEL_OPEN_XML_TYPE = new MediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
   public static final String MICROSOFT_EXCEL = "application/vnd.ms-excel";
   public static final MediaType MICROSOFT_EXCEL_TYPE = new MediaType("application/vnd.ms-excel", "xls");
   public static final String TEXT_XML = "text/xml";
   public static final MediaType TEXT_XML_TYPE = new MediaType("text/xml");
   public static final String TEXT_JSON = "text/json";
   public static final MediaType TEXT_JSON_TYPE = new MediaType("text/json");
   public static final String TEXT_PLAIN = "text/plain";
   public static final MediaType TEXT_PLAIN_TYPE = new MediaType("text/plain");
   public static final String APPLICATION_HAL_JSON = "application/hal+json";
   public static final MediaType APPLICATION_HAL_JSON_TYPE = new MediaType("application/hal+json");
   public static final String APPLICATION_HAL_XML = "application/hal+xml";
   public static final MediaType APPLICATION_HAL_XML_TYPE = new MediaType("application/hal+xml");
   public static final String APPLICATION_ATOM_XML = "application/atom+xml";
   public static final MediaType APPLICATION_ATOM_XML_TYPE = new MediaType("application/atom+xml");
   public static final String APPLICATION_VND_ERROR = "application/vnd.error+json";
   public static final MediaType APPLICATION_VND_ERROR_TYPE = new MediaType("application/vnd.error+json");
   public static final String TEXT_EVENT_STREAM = "text/event-stream";
   public static final MediaType TEXT_EVENT_STREAM_TYPE = new MediaType("text/event-stream");
   public static final String APPLICATION_JSON_STREAM = "application/x-json-stream";
   public static final MediaType APPLICATION_JSON_STREAM_TYPE = new MediaType("application/x-json-stream");
   public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
   public static final MediaType APPLICATION_OCTET_STREAM_TYPE = new MediaType("application/octet-stream");
   public static final String APPLICATION_GRAPHQL = "application/graphql";
   public static final MediaType APPLICATION_GRAPHQL_TYPE = new MediaType("application/graphql");
   public static final String APPLICATION_PDF = "application/pdf";
   public static final MediaType APPLICATION_PDF_TYPE = new MediaType("application/pdf");
   public static final String IMAGE_PNG = "image/png";
   public static final MediaType IMAGE_PNG_TYPE = new MediaType("image/png");
   public static final String IMAGE_JPEG = "image/jpeg";
   public static final MediaType IMAGE_JPEG_TYPE = new MediaType("image/jpeg");
   public static final String IMAGE_GIF = "image/gif";
   public static final MediaType IMAGE_GIF_TYPE = new MediaType("image/gif");
   public static final String IMAGE_WEBP = "image/webp";
   public static final MediaType IMAGE_WEBP_TYPE = new MediaType("image/webp");
   public static final String CHARSET_PARAMETER = "charset";
   public static final String Q_PARAMETER = "q";
   public static final String V_PARAMETER = "v";
   @Internal
   static final Argument<MediaType> ARGUMENT = Argument.of(MediaType.class);
   @Internal
   static final ArgumentConversionContext<MediaType> CONVERSION_CONTEXT = ImmutableArgumentConversionContext.of(ARGUMENT);
   private static final char SEMICOLON = ';';
   private static final String MIME_TYPES_FILE_NAME = "META-INF/http/mime.types";
   private static Map<String, String> mediaTypeFileExtensions;
   private static final List<Pattern> textTypePatterns = new ArrayList(4);
   protected final String name;
   protected final String subtype;
   protected final String type;
   protected final String extension;
   protected final Map<CharSequence, String> parameters;
   private final String strRepr;
   private BigDecimal qualityNumberField = BigDecimal.ONE;

   public MediaType(String name) {
      this(name, null, Collections.emptyMap());
   }

   public MediaType(String name, Map<String, String> params) {
      this(name, null, params);
   }

   public MediaType(String name, String extension) {
      this(name, extension, Collections.emptyMap());
   }

   public MediaType(String name, String extension, Map<String, String> params) {
      if (name == null) {
         throw new IllegalArgumentException("Argument [name] cannot be null");
      } else {
         name = name.trim();
         Iterator<String> splitIt = StringUtils.splitOmitEmptyStringsIterator(name, ';');
         String withoutArgs;
         if (splitIt.hasNext()) {
            withoutArgs = (String)splitIt.next();
            if (splitIt.hasNext()) {
               Map<CharSequence, String> parameters = null;

               while(splitIt.hasNext()) {
                  String paramExpression = (String)splitIt.next();
                  int i = paramExpression.indexOf(61);
                  if (i > -1) {
                     String paramName = paramExpression.substring(0, i).trim();
                     String paramValue = paramExpression.substring(i + 1).trim();
                     if ("q".equals(paramName)) {
                        this.qualityNumberField = new BigDecimal(paramValue);
                     }

                     if (parameters == null) {
                        parameters = new LinkedHashMap();
                     }

                     parameters.put(paramName, paramValue);
                  }
               }

               if (parameters == null) {
                  parameters = Collections.emptyMap();
               }

               this.parameters = parameters;
            } else if (params == null) {
               this.parameters = Collections.emptyMap();
            } else {
               this.parameters = params;
            }
         } else {
            if (params == null) {
               this.parameters = Collections.emptyMap();
            } else {
               this.parameters = params;
            }

            withoutArgs = name;
         }

         this.name = withoutArgs;
         int i = withoutArgs.indexOf(47);
         if (i > -1) {
            this.type = withoutArgs.substring(0, i);
            this.subtype = withoutArgs.substring(i + 1);
            if (extension != null) {
               this.extension = extension;
            } else {
               int j = this.subtype.indexOf(43);
               if (j > -1) {
                  this.extension = this.subtype.substring(j + 1);
               } else {
                  this.extension = this.subtype;
               }
            }

            this.strRepr = this.toString0();
         } else {
            throw new IllegalArgumentException("Invalid mime type: " + name);
         }
      }
   }

   public static MediaType of(String mediaType) {
      switch(mediaType) {
         case "*/*":
            return ALL_TYPE;
         case "application/x-www-form-urlencoded":
            return APPLICATION_FORM_URLENCODED_TYPE;
         case "multipart/form-data":
            return MULTIPART_FORM_DATA_TYPE;
         case "text/html":
            return TEXT_HTML_TYPE;
         case "text/csv":
            return TEXT_CSV_TYPE;
         case "application/xhtml+xml":
            return APPLICATION_XHTML_TYPE;
         case "application/xml":
            return APPLICATION_XML_TYPE;
         case "application/json":
            return APPLICATION_JSON_TYPE;
         case "application/x-yaml":
            return APPLICATION_YAML_TYPE;
         case "text/xml":
            return TEXT_XML_TYPE;
         case "text/json":
            return TEXT_JSON_TYPE;
         case "text/plain":
            return TEXT_PLAIN_TYPE;
         case "application/hal+json":
            return APPLICATION_HAL_JSON_TYPE;
         case "application/hal+xml":
            return APPLICATION_HAL_XML_TYPE;
         case "application/atom+xml":
            return APPLICATION_ATOM_XML_TYPE;
         case "application/vnd.error+json":
            return APPLICATION_VND_ERROR_TYPE;
         case "text/event-stream":
            return TEXT_EVENT_STREAM_TYPE;
         case "application/x-json-stream":
            return APPLICATION_JSON_STREAM_TYPE;
         case "application/octet-stream":
            return APPLICATION_OCTET_STREAM_TYPE;
         case "application/graphql":
            return APPLICATION_GRAPHQL_TYPE;
         case "application/pdf":
            return APPLICATION_PDF_TYPE;
         case "image/png":
            return IMAGE_PNG_TYPE;
         case "image/jpeg":
            return IMAGE_JPEG_TYPE;
         case "image/gif":
            return IMAGE_GIF_TYPE;
         case "image/webp":
            return IMAGE_WEBP_TYPE;
         default:
            return new MediaType(mediaType);
      }
   }

   public boolean matches(@NonNull MediaType expectedContentType) {
      if (expectedContentType == null) {
         return false;
      } else if (expectedContentType == this) {
         return true;
      } else {
         String expectedType = expectedContentType.getType();
         String expectedSubtype = expectedContentType.getSubtype();
         boolean typeMatch = this.type.equals("*") || this.type.equalsIgnoreCase(expectedType);
         boolean subtypeMatch = this.subtype.equals("*") || this.subtype.equalsIgnoreCase(expectedSubtype);
         return typeMatch && subtypeMatch;
      }
   }

   public String getName() {
      return this.name;
   }

   public String getType() {
      return this.type;
   }

   public String getSubtype() {
      return this.subtype;
   }

   public String getExtension() {
      return this.extension;
   }

   public OptionalValues<String> getParameters() {
      return OptionalValues.of(String.class, this.parameters);
   }

   public String getQuality() {
      return this.qualityNumberField.toString();
   }

   public BigDecimal getQualityAsNumber() {
      return this.qualityNumberField;
   }

   public String getVersion() {
      return (String)this.parameters.getOrDefault("v", null);
   }

   public Optional<Charset> getCharset() {
      return this.getParameters().get("charset").map(Charset::forName);
   }

   public int length() {
      return this.strRepr.length();
   }

   public char charAt(int index) {
      return this.strRepr.charAt(index);
   }

   public CharSequence subSequence(int start, int end) {
      return this.strRepr.subSequence(start, end);
   }

   public boolean isTextBased() {
      boolean matches = textTypePatterns.stream().anyMatch(p -> p.matcher(this.name).matches());
      if (!matches) {
         matches = this.subtype.equalsIgnoreCase("json") || this.subtype.equalsIgnoreCase("xml") || this.subtype.equalsIgnoreCase("x-yaml");
      }

      return matches;
   }

   public static boolean isTextBased(String contentType) {
      if (StringUtils.isEmpty(contentType)) {
         return false;
      } else {
         try {
            return of(contentType).isTextBased();
         } catch (IllegalArgumentException var2) {
            return false;
         }
      }
   }

   public String toString() {
      return this.strRepr;
   }

   private String toString0() {
      if (this.parameters.isEmpty()) {
         return this.name;
      } else {
         StringBuilder sb = new StringBuilder(this.name);
         this.parameters.forEach((name, value) -> {
            sb.append(";");
            sb.append(name);
            sb.append("=");
            sb.append(value);
         });
         return sb.toString();
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MediaType mediaType = (MediaType)o;
         return this.name.equalsIgnoreCase(mediaType.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public static List<MediaType> orderedOf(CharSequence... values) {
      return orderedOf(Arrays.asList(values));
   }

   public static List<MediaType> orderedOf(List<? extends CharSequence> values) {
      if (!CollectionUtils.isNotEmpty(values)) {
         return Collections.emptyList();
      } else {
         List<MediaType> mediaTypes = new LinkedList();

         for(CharSequence value : values) {
            for(String token : StringUtils.splitOmitEmptyStrings(value, ',')) {
               try {
                  mediaTypes.add(of(token));
               } catch (IllegalArgumentException var7) {
               }
            }
         }

         mediaTypes = new ArrayList(mediaTypes);
         mediaTypes.sort((o1, o2) -> {
            if (o1.type.equals("*")) {
               return 1;
            } else if (o2.type.equals("*")) {
               return -1;
            } else if (o2.subtype.equals("*") && !o1.subtype.equals("*")) {
               return -1;
            } else {
               return o1.subtype.equals("*") && !o2.subtype.equals("*") ? 1 : o2.getQualityAsNumber().compareTo(o1.getQualityAsNumber());
            }
         });
         return Collections.unmodifiableList(mediaTypes);
      }
   }

   public static MediaType of(CharSequence mediaType) {
      return of(mediaType.toString());
   }

   public static MediaType[] of(CharSequence... mediaType) {
      MediaType[] types = new MediaType[mediaType.length];

      for(int i = 0; i < mediaType.length; ++i) {
         types[i] = of(mediaType[i].toString());
      }

      return types;
   }

   public static Optional<MediaType> fromType(Class<?> type) {
      Produces producesAnn = (Produces)type.getAnnotation(Produces.class);
      if (producesAnn != null) {
         String[] var2 = producesAnn.value();
         int var3 = var2.length;
         byte var4 = 0;
         if (var4 < var3) {
            String mimeType = var2[var4];
            return Optional.of(of(mimeType));
         }
      }

      return Optional.empty();
   }

   public static Optional<MediaType> forExtension(String extension) {
      if (StringUtils.isNotEmpty(extension)) {
         String type = (String)getMediaTypeFileExtensions().get(extension);
         if (type != null) {
            return Optional.of(new MediaType(type, extension));
         }
      }

      return Optional.empty();
   }

   public static MediaType forFilename(String filename) {
      return StringUtils.isNotEmpty(filename) ? (MediaType)forExtension(NameUtils.extension(filename)).orElse(TEXT_PLAIN_TYPE) : TEXT_PLAIN_TYPE;
   }

   private static Map<String, String> getMediaTypeFileExtensions() {
      Map<String, String> extensions = mediaTypeFileExtensions;
      if (extensions == null) {
         synchronized(MediaType.class) {
            extensions = mediaTypeFileExtensions;
            if (extensions == null) {
               try {
                  extensions = loadMimeTypes();
                  mediaTypeFileExtensions = extensions;
               } catch (Exception var4) {
                  mediaTypeFileExtensions = Collections.emptyMap();
               }
            }
         }
      }

      return extensions;
   }

   private static Map<String, String> loadMimeTypes() {
      try {
         InputStream is = MediaType.class.getClassLoader().getResourceAsStream("META-INF/http/mime.types");
         Throwable var20 = null;

         Object var21;
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
            Map<String, String> result = new LinkedHashMap(100);

            String line;
            while((line = reader.readLine()) != null) {
               if (!line.isEmpty() && line.charAt(0) != '#') {
                  String formattedLine = line.trim().replaceAll("\\s{2,}", " ").replaceAll("\\s", "|");
                  String[] tokens = formattedLine.split("\\|");

                  for(int i = 1; i < tokens.length; ++i) {
                     String fileExtension = tokens[i].toLowerCase(Locale.ENGLISH);
                     result.put(fileExtension, tokens[0]);
                  }
               }
            }

            var21 = result;
         } catch (Throwable var17) {
            var20 = var17;
            throw var17;
         } finally {
            if (is != null) {
               if (var20 != null) {
                  try {
                     is.close();
                  } catch (Throwable var16) {
                     var20.addSuppressed(var16);
                  }
               } else {
                  is.close();
               }
            }

         }

         return (Map<String, String>)var21;
      } catch (IOException var19) {
         Logger logger = LoggerFactory.getLogger(MediaType.class);
         if (logger.isWarnEnabled()) {
            logger.warn("Failed to load mime types for file extension detection!");
         }

         return Collections.emptyMap();
      }
   }

   static {
      ConversionService.SHARED
         .addConverter(CharSequence.class, MediaType.class, charSequence -> StringUtils.isNotEmpty(charSequence) ? of(charSequence.toString()) : null);
      textTypePatterns.add(Pattern.compile("^text/.*$"));
      textTypePatterns.add(Pattern.compile("^.*\\+json$"));
      textTypePatterns.add(Pattern.compile("^.*\\+text$"));
      textTypePatterns.add(Pattern.compile("^.*\\+xml$"));
      textTypePatterns.add(Pattern.compile("^application/javascript$"));
   }
}
