package io.micronaut.http.server.util;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProxyHeaderParser {
   private static final String FOR = "for";
   private static final String BY = "by";
   private static final String HOST = "host";
   private static final String PROTO = "proto";
   private static final String PARAM_DELIMITER = ";";
   private static final String ELEMENT_DELIMITER = ",";
   private static final String PAIR_DELIMITER = "=";
   private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
   private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
   private static final String X_FORWARDED_FOR = "X-Forwarded-For";
   private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
   private List<String> forwardedFor = new ArrayList();
   private String forwardedBy = null;
   private String forwardedHost = null;
   private String forwardedProto = null;
   private Integer forwardedPort = null;

   public ProxyHeaderParser(HttpRequest request) {
      HttpHeaders headers = request.getHeaders();
      if (headers.contains("Forwarded")) {
         headers.getAll("Forwarded")
            .stream()
            .flatMap(header -> header.contains(",") ? Arrays.stream(header.split(",")) : Stream.of(header))
            .forEach(forwarded -> {
               String[] params = forwarded.split(";");
   
               for(String param : params) {
                  String[] parts = param.split("=");
                  if (parts.length == 2) {
                     String key = parts[0].trim();
                     String value = this.trimQuotes(parts[1].trim());
                     if (key.equalsIgnoreCase("for")) {
                        this.forwardedFor.add(value);
                     } else if (key.equalsIgnoreCase("by") && this.forwardedBy == null) {
                        this.forwardedBy = value;
                     } else if (key.equalsIgnoreCase("proto") && this.forwardedProto == null) {
                        this.forwardedProto = value;
                     } else if (key.equalsIgnoreCase("host") && this.forwardedHost == null) {
                        if (value.contains(":")) {
                           String[] host = value.split(":");
                           this.forwardedHost = host[0];
                           this.forwardedPort = Integer.valueOf(host[1]);
                        } else {
                           this.forwardedHost = value;
                        }
                     }
                  }
               }
   
            });
      } else {
         this.forwardedProto = StringUtils.trimToNull(headers.get("X-Forwarded-Proto"));
         this.forwardedHost = headers.get("X-Forwarded-Host");

         try {
            if (this.forwardedHost != null && this.forwardedHost.contains(":")) {
               String[] parts = this.forwardedHost.split(":");
               this.forwardedHost = parts[0];
               this.forwardedPort = Integer.valueOf(parts[1]);
            } else {
               String portHeader = headers.get("X-Forwarded-Port");
               if (portHeader != null) {
                  this.forwardedPort = Integer.valueOf(portHeader);
               }
            }
         } catch (NumberFormatException var4) {
         }

         String forwardedForHeader = headers.get("X-Forwarded-For");
         if (forwardedForHeader != null) {
            this.forwardedFor = (List)Arrays.stream(forwardedForHeader.split(",")).map(String::trim).collect(Collectors.toList());
         }
      }

   }

   @NonNull
   public List<String> getFor() {
      return this.forwardedFor;
   }

   public String getBy() {
      return this.forwardedBy;
   }

   public String getHost() {
      return this.forwardedHost;
   }

   public String getScheme() {
      return this.forwardedProto;
   }

   public Integer getPort() {
      return this.forwardedPort;
   }

   private String trimQuotes(String value) {
      return value != null && value.startsWith("\"") ? value.substring(1, value.length() - 1) : value;
   }
}
