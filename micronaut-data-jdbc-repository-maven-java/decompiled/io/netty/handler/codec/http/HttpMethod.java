package io.netty.handler.codec.http;

import io.netty.util.AsciiString;
import io.netty.util.internal.MathUtil;
import io.netty.util.internal.ObjectUtil;

public class HttpMethod implements Comparable<HttpMethod> {
   public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");
   public static final HttpMethod GET = new HttpMethod("GET");
   public static final HttpMethod HEAD = new HttpMethod("HEAD");
   public static final HttpMethod POST = new HttpMethod("POST");
   public static final HttpMethod PUT = new HttpMethod("PUT");
   public static final HttpMethod PATCH = new HttpMethod("PATCH");
   public static final HttpMethod DELETE = new HttpMethod("DELETE");
   public static final HttpMethod TRACE = new HttpMethod("TRACE");
   public static final HttpMethod CONNECT = new HttpMethod("CONNECT");
   private static final HttpMethod.EnumNameMap<HttpMethod> methodMap = new HttpMethod.EnumNameMap<>(
      new HttpMethod.EnumNameMap.Node<>(OPTIONS.toString(), OPTIONS),
      new HttpMethod.EnumNameMap.Node<>(GET.toString(), GET),
      new HttpMethod.EnumNameMap.Node<>(HEAD.toString(), HEAD),
      new HttpMethod.EnumNameMap.Node<>(POST.toString(), POST),
      new HttpMethod.EnumNameMap.Node<>(PUT.toString(), PUT),
      new HttpMethod.EnumNameMap.Node<>(PATCH.toString(), PATCH),
      new HttpMethod.EnumNameMap.Node<>(DELETE.toString(), DELETE),
      new HttpMethod.EnumNameMap.Node<>(TRACE.toString(), TRACE),
      new HttpMethod.EnumNameMap.Node<>(CONNECT.toString(), CONNECT)
   );
   private final AsciiString name;

   public static HttpMethod valueOf(String name) {
      HttpMethod result = methodMap.get(name);
      return result != null ? result : new HttpMethod(name);
   }

   public HttpMethod(String name) {
      name = ObjectUtil.checkNonEmptyAfterTrim(name, "name");

      for(int i = 0; i < name.length(); ++i) {
         char c = name.charAt(i);
         if (Character.isISOControl(c) || Character.isWhitespace(c)) {
            throw new IllegalArgumentException("invalid character in name");
         }
      }

      this.name = AsciiString.cached(name);
   }

   public String name() {
      return this.name.toString();
   }

   public AsciiString asciiName() {
      return this.name;
   }

   public int hashCode() {
      return this.name().hashCode();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof HttpMethod)) {
         return false;
      } else {
         HttpMethod that = (HttpMethod)o;
         return this.name().equals(that.name());
      }
   }

   public String toString() {
      return this.name.toString();
   }

   public int compareTo(HttpMethod o) {
      return o == this ? 0 : this.name().compareTo(o.name());
   }

   private static final class EnumNameMap<T> {
      private final HttpMethod.EnumNameMap.Node<T>[] values;
      private final int valuesMask;

      EnumNameMap(HttpMethod.EnumNameMap.Node<T>... nodes) {
         this.values = new HttpMethod.EnumNameMap.Node[MathUtil.findNextPositivePowerOfTwo(nodes.length)];
         this.valuesMask = this.values.length - 1;

         for(HttpMethod.EnumNameMap.Node<T> node : nodes) {
            int i = hashCode(node.key) & this.valuesMask;
            if (this.values[i] != null) {
               throw new IllegalArgumentException("index " + i + " collision between values: [" + this.values[i].key + ", " + node.key + ']');
            }

            this.values[i] = node;
         }

      }

      T get(String name) {
         HttpMethod.EnumNameMap.Node<T> node = this.values[hashCode(name) & this.valuesMask];
         return node != null && node.key.equals(name) ? node.value : null;
      }

      private static int hashCode(String name) {
         return name.hashCode() >>> 6;
      }

      private static final class Node<T> {
         final String key;
         final T value;

         Node(String key, T value) {
            this.key = key;
            this.value = value;
         }
      }
   }
}
