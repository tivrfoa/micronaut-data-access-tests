package io.micronaut.http.uri;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class UriTypeMatchTemplate extends UriMatchTemplate {
   private Class[] variableTypes;

   public UriTypeMatchTemplate(CharSequence templateString, Class... variableTypes) {
      super(templateString, variableTypes);
      this.variableTypes = variableTypes == null ? new Class[0] : variableTypes;
   }

   protected UriTypeMatchTemplate(
      CharSequence templateString, List<UriTemplate.PathSegment> segments, Pattern matchPattern, Class[] variableTypes, List<UriMatchVariable> variables
   ) {
      super(templateString, segments, matchPattern, variables);
      this.variableTypes = variableTypes;
   }

   public UriTypeMatchTemplate nest(CharSequence uriTemplate) {
      return (UriTypeMatchTemplate)super.nest(uriTemplate);
   }

   public UriTypeMatchTemplate nest(CharSequence uriTemplate, Class... variableTypes) {
      return (UriTypeMatchTemplate)super.nest(uriTemplate, new Object[]{variableTypes});
   }

   @Override
   public String expand(Map<String, Object> parameters) {
      return super.expand(parameters);
   }

   @Override
   protected UriTemplate.UriTemplateParser createParser(String templateString, Object... parserArguments) {
      this.pattern = new StringBuilder();
      if (this.variables == null) {
         this.variables = new ArrayList();
      }

      this.variableTypes = parserArguments != null && parserArguments.length > 0 ? (Class[])parserArguments[0] : new Class[0];
      return new UriTypeMatchTemplate.TypedUriMatchTemplateParser(templateString, this);
   }

   @Override
   protected UriMatchTemplate newUriMatchTemplate(
      CharSequence uriTemplate, List<UriTemplate.PathSegment> newSegments, Pattern newPattern, List<UriMatchVariable> variables
   ) {
      return new UriTypeMatchTemplate(uriTemplate, newSegments, newPattern, this.variableTypes, variables);
   }

   protected String resolveTypePattern(Class variableType, String variable, char operator) {
      if (Number.class.isAssignableFrom(variableType)) {
         return Double.class != variableType && Float.class != variableType && BigDecimal.class != variableType ? "([\\d+]" : "([\\d\\.+]";
      } else {
         return "([^\\/\\?#&;\\+]";
      }
   }

   protected static class TypedUriMatchTemplateParser extends UriMatchTemplate.UriMatchTemplateParser {
      private int variableIndex = 0;

      TypedUriMatchTemplateParser(String templateText, UriTypeMatchTemplate matchTemplate) {
         super(templateText, matchTemplate);
      }

      public UriTypeMatchTemplate getMatchTemplate() {
         return (UriTypeMatchTemplate)super.getMatchTemplate();
      }

      @Override
      protected String getVariablePattern(String variable, char operator) {
         UriTypeMatchTemplate matchTemplate = this.getMatchTemplate();
         Class[] variableTypes = matchTemplate.variableTypes;

         String var6;
         try {
            if (this.variableIndex >= variableTypes.length) {
               return super.getVariablePattern(variable, operator);
            }

            Class variableType = variableTypes[this.variableIndex];
            var6 = matchTemplate.resolveTypePattern(variableType, variable, operator);
         } finally {
            ++this.variableIndex;
         }

         return var6;
      }
   }
}
