package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.List;

public class ElementPath {
   ArrayList<String> partList = new ArrayList();

   public ElementPath() {
   }

   public ElementPath(List<String> list) {
      this.partList.addAll(list);
   }

   public ElementPath(String pathStr) {
      if (pathStr != null) {
         String[] partArray = pathStr.split("/");
         if (partArray != null) {
            for(String part : partArray) {
               if (part.length() > 0) {
                  this.partList.add(part);
               }
            }

         }
      }
   }

   public ElementPath duplicate() {
      ElementPath p = new ElementPath();
      p.partList.addAll(this.partList);
      return p;
   }

   public boolean equals(Object o) {
      if (o != null && o instanceof ElementPath) {
         ElementPath r = (ElementPath)o;
         if (r.size() != this.size()) {
            return false;
         } else {
            int len = this.size();

            for(int i = 0; i < len; ++i) {
               if (!this.equalityCheck(this.get(i), r.get(i))) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private boolean equalityCheck(String x, String y) {
      return x.equalsIgnoreCase(y);
   }

   public List<String> getCopyOfPartList() {
      return new ArrayList(this.partList);
   }

   public void push(String s) {
      this.partList.add(s);
   }

   public String get(int i) {
      return (String)this.partList.get(i);
   }

   public void pop() {
      if (!this.partList.isEmpty()) {
         this.partList.remove(this.partList.size() - 1);
      }

   }

   public String peekLast() {
      if (!this.partList.isEmpty()) {
         int size = this.partList.size();
         return (String)this.partList.get(size - 1);
      } else {
         return null;
      }
   }

   public int size() {
      return this.partList.size();
   }

   protected String toStableString() {
      StringBuilder result = new StringBuilder();

      for(String current : this.partList) {
         result.append("[").append(current).append("]");
      }

      return result.toString();
   }

   public String toString() {
      return this.toStableString();
   }
}
