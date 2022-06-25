package ch.qos.logback.core.pattern.parser;

public class Node {
   static final int LITERAL = 0;
   static final int SIMPLE_KEYWORD = 1;
   static final int COMPOSITE_KEYWORD = 2;
   final int type;
   final Object value;
   Node next;

   Node(int type) {
      this(type, null);
   }

   Node(int type, Object value) {
      this.type = type;
      this.value = value;
   }

   public int getType() {
      return this.type;
   }

   public Object getValue() {
      return this.value;
   }

   public Node getNext() {
      return this.next;
   }

   public void setNext(Node next) {
      this.next = next;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Node)) {
         return false;
      } else {
         Node r = (Node)o;
         if (this.type == r.type) {
            if (this.value != null) {
               if (!this.value.equals(r.value)) {
                  return false;
               }
            } else if (r.value != null) {
               return false;
            }

            if (this.next != null) {
               if (this.next.equals(r.next)) {
                  return true;
               }
            } else if (r.next == null) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int result = this.type;
      return 31 * result + (this.value != null ? this.value.hashCode() : 0);
   }

   String printNext() {
      return this.next != null ? " -> " + this.next : "";
   }

   public String toString() {
      StringBuilder buf = new StringBuilder();
      switch(this.type) {
         case 0:
            buf.append("LITERAL(" + this.value + ")");
            break;
         default:
            buf.append(super.toString());
      }

      buf.append(this.printNext());
      return buf.toString();
   }
}
