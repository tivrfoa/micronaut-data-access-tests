package ch.qos.logback.core.pattern.color;

public class BoldWhiteCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {
   @Override
   protected String getForegroundColorCode(E event) {
      return "1;37";
   }
}
