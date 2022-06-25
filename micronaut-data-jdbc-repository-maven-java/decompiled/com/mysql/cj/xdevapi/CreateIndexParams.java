package com.mysql.cj.xdevapi;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.AssertionFailedException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class CreateIndexParams {
   public static final String INDEX = "INDEX";
   public static final String SPATIAL = "SPATIAL";
   public static final String GEOJSON = "GEOJSON";
   private String indexName;
   private String indexType = null;
   private List<CreateIndexParams.IndexField> fields = new ArrayList();

   public CreateIndexParams(String indexName, DbDoc indexDefinition) {
      this.init(indexName, indexDefinition);
   }

   public CreateIndexParams(String indexName, String jsonIndexDefinition) {
      if (jsonIndexDefinition != null && jsonIndexDefinition.trim().length() != 0) {
         try {
            this.init(indexName, JsonParser.parseDoc(new StringReader(jsonIndexDefinition)));
         } catch (IOException var4) {
            throw AssertionFailedException.shouldNotHappen(var4);
         }
      } else {
         throw new XDevAPIError(Messages.getString("CreateIndexParams.0", new String[]{"jsonIndexDefinition"}));
      }
   }

   private void init(String idxName, DbDoc indexDefinition) {
      if (idxName != null && idxName.trim().length() != 0) {
         if (indexDefinition == null) {
            throw new XDevAPIError(Messages.getString("CreateIndexParams.0", new String[]{"indexDefinition"}));
         } else {
            this.indexName = idxName;

            for(String key : indexDefinition.keySet()) {
               if (!"type".equals(key) && !"fields".equals(key)) {
                  throw new XDevAPIError("The '" + key + "' field is not allowed in indexDefinition.");
               }
            }

            JsonValue val = (JsonValue)indexDefinition.get("type");
            if (val != null) {
               if (!(val instanceof JsonString)) {
                  throw new XDevAPIError("Index type must be a string.");
               }

               String type = ((JsonString)val).getString();
               if (!"INDEX".equalsIgnoreCase(type) && !"SPATIAL".equalsIgnoreCase(type)) {
                  throw new XDevAPIError("Wrong index type '" + type + "'. Must be 'INDEX' or 'SPATIAL'.");
               }

               this.indexType = type;
            }

            val = (JsonValue)indexDefinition.get("fields");
            if (val == null) {
               throw new XDevAPIError("Index definition does not contain fields.");
            } else if (val instanceof JsonArray) {
               for(JsonValue field : (JsonArray)val) {
                  if (!(field instanceof DbDoc)) {
                     throw new XDevAPIError("Index field definition must be a JSON document.");
                  }

                  this.fields.add(new CreateIndexParams.IndexField((DbDoc)field));
               }

            } else {
               throw new XDevAPIError("Index definition 'fields' member must be an array of index fields.");
            }
         }
      } else {
         throw new XDevAPIError(Messages.getString("CreateIndexParams.0", new String[]{"indexName"}));
      }
   }

   public String getIndexName() {
      return this.indexName;
   }

   public String getIndexType() {
      return this.indexType;
   }

   public List<CreateIndexParams.IndexField> getFields() {
      return this.fields;
   }

   public static class IndexField {
      private static final String FIELD = "field";
      private static final String TYPE = "type";
      private static final String REQUIRED = "required";
      private static final String OPTIONS = "options";
      private static final String SRID = "srid";
      private static final String ARRAY = "array";
      private String field;
      private String type;
      private Boolean required = Boolean.FALSE;
      private Integer options = null;
      private Integer srid = null;
      private Boolean array;

      public IndexField(DbDoc indexField) {
         for(String key : indexField.keySet()) {
            if (!"type".equals(key) && !"field".equals(key) && !"required".equals(key) && !"options".equals(key) && !"srid".equals(key) && !"array".equals(key)
               )
             {
               throw new XDevAPIError("The '" + key + "' field is not allowed in indexField.");
            }
         }

         JsonValue val = (JsonValue)indexField.get("field");
         if (val != null) {
            if (!(val instanceof JsonString)) {
               throw new XDevAPIError("Index field 'field' member must be a string.");
            } else {
               this.field = ((JsonString)val).getString();
               val = (JsonValue)indexField.get("type");
               if (val != null) {
                  if (!(val instanceof JsonString)) {
                     throw new XDevAPIError("Index type must be a string.");
                  } else {
                     this.type = ((JsonString)val).getString();
                     val = (JsonValue)indexField.get("required");
                     if (val != null) {
                        if (!(val instanceof JsonLiteral) || JsonLiteral.NULL.equals(val)) {
                           throw new XDevAPIError("Index field 'required' member must be boolean.");
                        }

                        this.required = Boolean.valueOf(((JsonLiteral)val).value);
                     } else if ("GEOJSON".equalsIgnoreCase(this.type)) {
                        this.required = Boolean.TRUE;
                     }

                     val = (JsonValue)indexField.get("options");
                     if (val != null) {
                        if (!"GEOJSON".equalsIgnoreCase(this.type)) {
                           throw new XDevAPIError("Index field 'options' member should not be used for field types other than GEOJSON.");
                        }

                        if (!(val instanceof JsonNumber)) {
                           throw new XDevAPIError("Index field 'options' member must be integer.");
                        }

                        this.options = ((JsonNumber)val).getInteger();
                     }

                     val = (JsonValue)indexField.get("srid");
                     if (val != null) {
                        if (!"GEOJSON".equalsIgnoreCase(this.type)) {
                           throw new XDevAPIError("Index field 'srid' member should not be used for field types other than GEOJSON.");
                        }

                        if (!(val instanceof JsonNumber)) {
                           throw new XDevAPIError("Index field 'srid' member must be integer.");
                        }

                        this.srid = ((JsonNumber)val).getInteger();
                     }

                     val = (JsonValue)indexField.get("array");
                     if (val != null) {
                        if (!(val instanceof JsonLiteral) || JsonLiteral.NULL.equals(val)) {
                           throw new XDevAPIError("Index field 'array' member must be boolean.");
                        }

                        this.array = Boolean.valueOf(((JsonLiteral)val).value);
                     }

                  }
               } else {
                  throw new XDevAPIError("Index field definition has no field type.");
               }
            }
         } else {
            throw new XDevAPIError("Index field definition has no document path.");
         }
      }

      public String getField() {
         return this.field;
      }

      public String getType() {
         return this.type;
      }

      public Boolean isRequired() {
         return this.required;
      }

      public Integer getOptions() {
         return this.options;
      }

      public Integer getSrid() {
         return this.srid;
      }

      public Boolean isArray() {
         return this.array;
      }
   }
}
