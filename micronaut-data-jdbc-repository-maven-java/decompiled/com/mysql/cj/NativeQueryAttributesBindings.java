package com.mysql.cj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class NativeQueryAttributesBindings implements QueryAttributesBindings {
   Session session = null;
   private List<NativeQueryBindValue> bindAttributes = new ArrayList();

   public NativeQueryAttributesBindings(Session sess) {
      this.session = sess;
   }

   @Override
   public void setAttribute(String name, Object value) {
      MysqlType defaultMysqlType = value == null ? MysqlType.NULL : (MysqlType)NativeQueryBindings.DEFAULT_MYSQL_TYPES.get(value.getClass());
      Object val = value;
      if (defaultMysqlType == null) {
         Optional<MysqlType> mysqlType = NativeQueryBindings.DEFAULT_MYSQL_TYPES
            .entrySet()
            .stream()
            .filter(m -> ((Class)m.getKey()).isAssignableFrom(value.getClass()))
            .map(m -> (MysqlType)m.getValue())
            .findFirst();
         if (mysqlType.isPresent()) {
            defaultMysqlType = (MysqlType)mysqlType.get();
         } else {
            defaultMysqlType = MysqlType.VARCHAR;
            val = value.toString();
         }
      }

      NativeQueryBindValue bv = new NativeQueryBindValue(this.session);
      bv.setName(name);
      bv.setBinding(val, defaultMysqlType, 0, null);
      this.bindAttributes.add(bv);
   }

   @Override
   public int getCount() {
      return this.bindAttributes.size();
   }

   @Override
   public BindValue getAttributeValue(int index) {
      return (BindValue)this.bindAttributes.get(index);
   }

   @Override
   public void runThroughAll(Consumer<BindValue> bindAttribute) {
      this.bindAttributes.forEach(bindAttribute::accept);
   }

   @Override
   public void clearAttributes() {
      this.bindAttributes.clear();
   }
}
