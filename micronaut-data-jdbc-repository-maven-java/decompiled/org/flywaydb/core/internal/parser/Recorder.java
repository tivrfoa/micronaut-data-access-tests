package org.flywaydb.core.internal.parser;

public class Recorder {
   private StringBuilder recorder;
   private boolean recorderPaused = false;
   private int recorderConfirmedPos = 0;

   public void record(char c) {
      if (this.isRunning()) {
         this.recorder.append(c);
      }

   }

   public int length() {
      return this.recorder.length();
   }

   public void truncate(int length) {
      if (this.isRunning()) {
         this.recorder.delete(length, this.recorder.length());
      }

   }

   private boolean isRunning() {
      return this.recorder != null && !this.recorderPaused;
   }

   public void start() {
      this.recorder = new StringBuilder();
      this.recorderConfirmedPos = 0;
      this.recorderPaused = false;
   }

   public void pause() {
      this.recorderPaused = true;
   }

   public void record(String str) {
      this.recorder.append(str);
      this.confirm();
   }

   public void confirm() {
      this.recorderConfirmedPos = this.recorder.length();
   }

   public String stop() {
      this.recorder.delete(this.recorderConfirmedPos, this.recorder.length());
      String result = this.recorder.toString();
      this.recorder = null;
      return result;
   }
}
