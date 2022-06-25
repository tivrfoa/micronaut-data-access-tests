package ch.qos.logback.core.sift;

public class DefaultDiscriminator<E> extends AbstractDiscriminator<E> {
   public static final String DEFAULT = "default";

   @Override
   public String getDiscriminatingValue(E e) {
      return "default";
   }

   @Override
   public String getKey() {
      return "default";
   }
}
