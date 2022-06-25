package com.mysql.cj;

import com.mysql.cj.protocol.Message;

public interface PreparedQuery extends Query {
   QueryInfo getQueryInfo();

   void setQueryInfo(QueryInfo var1);

   void checkNullOrEmptyQuery(String var1);

   String getOriginalSql();

   void setOriginalSql(String var1);

   int getParameterCount();

   void setParameterCount(int var1);

   QueryBindings getQueryBindings();

   void setQueryBindings(QueryBindings var1);

   int computeBatchSize(int var1);

   int getBatchCommandIndex();

   void setBatchCommandIndex(int var1);

   String asSql();

   <M extends Message> M fillSendPacket(QueryBindings var1);
}
