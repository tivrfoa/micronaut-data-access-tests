package io.micronaut.http.uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UriMatchTemplate extends UriTemplate implements UriMatcher {
   protected static final String VARIABLE_MATCH_PATTERN = "([^\\/\\?#&;\\+]";
   protected StringBuilder pattern;
   protected List<UriMatchVariable> variables;
   private final Pattern matchPattern;
   private final boolean isRoot;
   private final boolean exactMatch;
   private Optional<UriMatchInfo> rootMatchInfo;
   private Optional<UriMatchInfo> exactMatchInfo;

   public UriMatchTemplate(CharSequence templateString) {
      this(templateString);
   }

   protected UriMatchTemplate(CharSequence templateString, Object... parserArguments) {
      super(templateString, parserArguments);
      if (this.variables.isEmpty() && Pattern.quote(templateString.toString()).equals(this.pattern.toString())) {
         this.matchPattern = null;
         this.exactMatch = true;
      } else {
         this.matchPattern = Pattern.compile(this.pattern.toString());
         this.exactMatch = false;
      }

      this.isRoot = this.isRoot();
      this.pattern = null;
   }

   protected UriMatchTemplate(CharSequence templateString, List<UriTemplate.PathSegment> segments, Pattern matchPattern, List<UriMatchVariable> variables) {
      super(templateString.toString(), segments);
      this.variables = variables;
      this.isRoot = this.isRoot();
      if (variables.isEmpty() && matchPattern.matcher(templateString).matches()) {
         this.matchPattern = null;
         this.exactMatch = true;
      } else {
         this.matchPattern = matchPattern;
         this.exactMatch = false;
      }

   }

   protected UriMatchTemplate newUriMatchTemplate(
      CharSequence uriTemplate, List<UriTemplate.PathSegment> newSegments, Pattern newPattern, List<UriMatchVariable> variables
   ) {
      return new UriMatchTemplate(uriTemplate, newSegments, newPattern, variables);
   }

   public List<String> getVariableNames() {
      return (List<String>)this.variables.stream().map(UriMatchVariable::getName).collect(Collectors.toList());
   }

   public List<UriMatchVariable> getVariables() {
      return Collections.unmodifiableList(this.variables);
   }

   public String toPathString() {
      return this.toString(pathSegment -> {
         Optional<String> var = pathSegment.getVariable();
         if (var.isPresent()) {
            Optional<UriMatchVariable> umv = this.variables.stream().filter(v -> v.getName().equals(var.get())).findFirst();
            if (umv.isPresent()) {
               UriMatchVariable uriMatchVariable = (UriMatchVariable)umv.get();
               if (uriMatchVariable.isQuery()) {
                  return false;
               }
            }
         }

         return true;
      });
   }

   @Override
   public Optional<UriMatchInfo> match(String uri) {
      if (uri == null) {
         throw new IllegalArgumentException("Argument 'uri' cannot be null");
      } else {
         int length = uri.length();
         if (length > 1 && uri.charAt(length - 1) == '/') {
            uri = uri.substring(0, length - 1);
         }

         if (!this.isRoot || length != 0 && (length != 1 || uri.charAt(0) != '/')) {
            int parameterIndex = uri.indexOf(63);
            if (parameterIndex > -1) {
               uri = uri.substring(0, parameterIndex);
            }

            if (uri.endsWith("/")) {
               uri = uri.substring(0, uri.length() - 1);
            }

            if (!this.exactMatch) {
               Matcher matcher = this.matchPattern.matcher(uri);
               if (!matcher.matches()) {
                  return Optional.empty();
               } else if (this.variables.isEmpty()) {
                  return Optional.of(new UriMatchTemplate.DefaultUriMatchInfo(uri, Collections.emptyMap(), this.variables));
               } else {
                  int count = matcher.groupCount();
                  Map<String, Object> variableMap = new LinkedHashMap(count);

                  for(int j = 0; j < this.variables.size(); ++j) {
                     int index = j * 2 + 2;
                     if (index > count) {
                        break;
                     }

                     UriMatchVariable variable = (UriMatchVariable)this.variables.get(j);
                     String value = matcher.group(index);
                     variableMap.put(variable.getName(), value);
                  }

                  return Optional.of(new UriMatchTemplate.DefaultUriMatchInfo(uri, variableMap, this.variables));
               }
            } else if (uri.equals(this.templateString)) {
               if (this.exactMatchInfo == null) {
                  this.exactMatchInfo = Optional.of(new UriMatchTemplate.DefaultUriMatchInfo(uri, Collections.emptyMap(), this.variables));
               }

               return this.exactMatchInfo;
            } else {
               return Optional.empty();
            }
         } else {
            if (this.rootMatchInfo == null) {
               this.rootMatchInfo = Optional.of(new UriMatchTemplate.DefaultUriMatchInfo(uri, Collections.emptyMap(), this.variables));
            }

            return this.rootMatchInfo;
         }
      }
   }

   public UriMatchTemplate nest(CharSequence uriTemplate) {
      return (UriMatchTemplate)super.nest(uriTemplate);
   }

   public static UriMatchTemplate of(String uri) {
      return new UriMatchTemplate(uri);
   }

   @Override
   protected UriTemplate newUriTemplate(CharSequence uriTemplate, List<UriTemplate.PathSegment> newSegments) {
      Pattern newPattern = Pattern.compile(
         this.exactMatch ? Pattern.quote(this.templateString) + this.pattern.toString() : this.matchPattern.pattern() + this.pattern.toString()
      );
      this.pattern = null;
      return this.newUriMatchTemplate(this.normalizeNested(this.toString(), uriTemplate), newSegments, newPattern, new ArrayList(this.variables));
   }

   @Override
   protected UriTemplate.UriTemplateParser createParser(String templateString, Object... parserArguments) {
      if (Objects.isNull(this.pattern)) {
         this.pattern = new StringBuilder();
      }

      if (this.variables == null) {
         this.variables = new ArrayList();
      }

      return new UriMatchTemplate.UriMatchTemplateParser(templateString, this);
   }

   private boolean isRoot() {
      CharSequence rawSegment = null;

      for(UriTemplate.PathSegment segment : this.segments) {
         if (segment.isVariable()) {
            if (!segment.isQuerySegment()) {
               return false;
            }
         } else {
            if (rawSegment != null) {
               return false;
            }

            rawSegment = segment;
         }
      }

      if (rawSegment == null) {
         return true;
      } else {
         int len = rawSegment.length();
         return len == 0 || len == 1 && rawSegment.charAt(0) == '/';
      }
   }

   protected static class DefaultUriMatchInfo implements UriMatchInfo {
      private final String uri;
      private final Map<String, Object> variableValues;
      private final List<UriMatchVariable> variables;
      private final Map<String, UriMatchVariable> variableMap;

      protected DefaultUriMatchInfo(String uri, Map<String, Object> variableValues, List<UriMatchVariable> variables) {
         this.uri = uri;
         this.variableValues = variableValues;
         this.variables = variables;
         LinkedHashMap<String, UriMatchVariable> vm = new LinkedHashMap(variables.size());

         for(UriMatchVariable variable : variables) {
            vm.put(variable.getName(), variable);
         }

         this.variableMap = Collections.unmodifiableMap(vm);
      }

      @Override
      public String getUri() {
         return this.uri;
      }

      @Override
      public Map<String, Object> getVariableValues() {
         return this.variableValues;
      }

      @Override
      public List<UriMatchVariable> getVariables() {
         return Collections.unmodifiableList(this.variables);
      }

      @Override
      public Map<String, UriMatchVariable> getVariableMap() {
         return this.variableMap;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            UriMatchTemplate.DefaultUriMatchInfo that = (UriMatchTemplate.DefaultUriMatchInfo)o;
            return this.uri.equals(that.uri) && this.variables.equals(that.variables);
         } else {
            return false;
         }
      }

      public String toString() {
         return this.getUri();
      }

      public int hashCode() {
         int result = this.uri.hashCode();
         return 31 * result + this.variables.hashCode();
      }
   }

   protected static class UriMatchTemplateParser extends UriTemplate.UriTemplateParser {
      final UriMatchTemplate matchTemplate;

      protected UriMatchTemplateParser(String templateText, UriMatchTemplate matchTemplate) {
         super(templateText);
         this.matchTemplate = matchTemplate;
      }

      public UriMatchTemplate getMatchTemplate() {
         return this.matchTemplate;
      }

      @Override
      protected void addRawContentSegment(List<UriTemplate.PathSegment> segments, String value, boolean isQuerySegment) {
         this.matchTemplate.pattern.append(Pattern.quote(value));
         super.addRawContentSegment(segments, value, isQuerySegment);
      }

      @Override
      protected void addVariableSegment(
         List<UriTemplate.PathSegment> segments,
         String variable,
         String prefix,
         String delimiter,
         boolean encode,
         boolean repeatPrefix,
         String modifierStr,
         char modifierChar,
         char operator,
         String previousDelimiter,
         boolean isQuerySegment
      ) {
         this.matchTemplate.variables.add(new UriMatchVariable(variable, modifierChar, operator));
         StringBuilder pattern = this.matchTemplate.pattern;
         int modLen = modifierStr.length();
         boolean hasModifier = modifierChar == ':' && modLen > 0;
         String operatorPrefix = "";
         String operatorQuantifier = "";
         String variableQuantifier = "+?)";
         String variablePattern = this.getVariablePattern(variable, operator);
         if (hasModifier) {
            char firstChar = modifierStr.charAt(0);
            if (firstChar == '?') {
               operatorQuantifier = "";
            } else if (modifierStr.chars().allMatch(Character::isDigit)) {
               variableQuantifier = "{1," + modifierStr + "})";
            } else {
               char lastChar = modifierStr.charAt(modLen - 1);
               if (lastChar == '*' || modLen > 1 && lastChar == '?' && (modifierStr.charAt(modLen - 2) == '*' || modifierStr.charAt(modLen - 2) == '+')) {
                  operatorQuantifier = "?";
               }

               if (operator != '/' && operator != '.') {
                  operatorPrefix = "(";
                  variablePattern = (firstChar == '^' ? modifierStr.substring(1) : modifierStr) + ")";
               } else {
                  variablePattern = "(" + (firstChar == '^' ? modifierStr.substring(1) : modifierStr) + ")";
               }

               variableQuantifier = "";
            }
         }

         boolean operatorAppended = false;
         switch(operator) {
            case ',':
            case '-':
            default:
               break;
            case '.':
            case '/':
               pattern.append("(").append(operatorPrefix).append("\\").append(String.valueOf(operator)).append(operatorQuantifier);
               operatorAppended = true;
            case '+':
            case '0':
               if (!operatorAppended) {
                  pattern.append("(").append(operatorPrefix);
               }

               pattern.append(variablePattern).append(variableQuantifier).append(")");
         }

         if (operator == '/' || modifierStr.equals("?")) {
            pattern.append("?");
         }

         super.addVariableSegment(
            segments, variable, prefix, delimiter, encode, repeatPrefix, modifierStr, modifierChar, operator, previousDelimiter, isQuerySegment
         );
      }

      protected String getVariablePattern(String variable, char operator) {
         return operator == '+' ? "([\\S]" : "([^\\/\\?#&;\\+]";
      }
   }
}
