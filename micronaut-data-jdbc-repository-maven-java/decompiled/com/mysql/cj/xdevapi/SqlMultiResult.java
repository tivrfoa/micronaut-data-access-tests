package com.mysql.cj.xdevapi;

import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.ResultStreamer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class SqlMultiResult implements SqlResult, ResultStreamer {
   private Supplier<SqlResult> resultStream;
   private List<SqlResult> pendingResults = new ArrayList();
   private SqlResult currentResult;

   public SqlMultiResult(Supplier<SqlResult> resultStream) {
      this.resultStream = resultStream;
      this.currentResult = (SqlResult)resultStream.get();
   }

   private SqlResult getCurrentResult() {
      if (this.currentResult == null) {
         throw new WrongArgumentException("No active result");
      } else {
         return this.currentResult;
      }
   }

   @Override
   public boolean nextResult() {
      if (this.currentResult == null) {
         return false;
      } else {
         try {
            if (ResultStreamer.class.isAssignableFrom(this.currentResult.getClass())) {
               ((ResultStreamer)this.currentResult).finishStreaming();
            }
         } finally {
            this.currentResult = null;
         }

         this.currentResult = this.pendingResults.size() > 0 ? (SqlResult)this.pendingResults.remove(0) : (SqlResult)this.resultStream.get();
         return this.currentResult != null;
      }
   }

   @Override
   public void finishStreaming() {
      if (this.currentResult != null) {
         if (ResultStreamer.class.isAssignableFrom(this.currentResult.getClass())) {
            ((ResultStreamer)this.currentResult).finishStreaming();
         }

         SqlResult pendingRs;
         for(pendingRs = null; (pendingRs = (SqlResult)this.resultStream.get()) != null; this.pendingResults.add(pendingRs)) {
            if (ResultStreamer.class.isAssignableFrom(pendingRs.getClass())) {
               ((ResultStreamer)pendingRs).finishStreaming();
            }
         }

      }
   }

   @Override
   public boolean hasData() {
      return this.getCurrentResult().hasData();
   }

   @Override
   public long getAffectedItemsCount() {
      return this.getCurrentResult().getAffectedItemsCount();
   }

   @Override
   public Long getAutoIncrementValue() {
      return this.getCurrentResult().getAutoIncrementValue();
   }

   @Override
   public int getWarningsCount() {
      return this.getCurrentResult().getWarningsCount();
   }

   @Override
   public Iterator<Warning> getWarnings() {
      return this.getCurrentResult().getWarnings();
   }

   @Override
   public int getColumnCount() {
      return this.getCurrentResult().getColumnCount();
   }

   @Override
   public List<Column> getColumns() {
      return this.getCurrentResult().getColumns();
   }

   @Override
   public List<String> getColumnNames() {
      return this.getCurrentResult().getColumnNames();
   }

   @Override
   public long count() {
      return this.getCurrentResult().count();
   }

   @Override
   public List<Row> fetchAll() {
      return this.getCurrentResult().fetchAll();
   }

   public Row next() {
      return (Row)this.getCurrentResult().next();
   }

   public boolean hasNext() {
      return this.getCurrentResult().hasNext();
   }
}
