package ch.qos.logback.core;

import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.spi.LogbackLock;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import java.util.ArrayList;
import java.util.List;

public class BasicStatusManager implements StatusManager {
   public static final int MAX_HEADER_COUNT = 150;
   public static final int TAIL_SIZE = 150;
   int count = 0;
   protected final List<Status> statusList = new ArrayList();
   protected final CyclicBuffer<Status> tailBuffer = new CyclicBuffer<>(150);
   protected final LogbackLock statusListLock = new LogbackLock();
   int level = 0;
   protected final List<StatusListener> statusListenerList = new ArrayList();
   protected final LogbackLock statusListenerListLock = new LogbackLock();

   @Override
   public void add(Status newStatus) {
      this.fireStatusAddEvent(newStatus);
      ++this.count;
      if (newStatus.getLevel() > this.level) {
         this.level = newStatus.getLevel();
      }

      synchronized(this.statusListLock) {
         if (this.statusList.size() < 150) {
            this.statusList.add(newStatus);
         } else {
            this.tailBuffer.add(newStatus);
         }

      }
   }

   @Override
   public List<Status> getCopyOfStatusList() {
      synchronized(this.statusListLock) {
         List<Status> tList = new ArrayList(this.statusList);
         tList.addAll(this.tailBuffer.asList());
         return tList;
      }
   }

   private void fireStatusAddEvent(Status status) {
      synchronized(this.statusListenerListLock) {
         for(StatusListener sl : this.statusListenerList) {
            sl.addStatusEvent(status);
         }

      }
   }

   @Override
   public void clear() {
      synchronized(this.statusListLock) {
         this.count = 0;
         this.statusList.clear();
         this.tailBuffer.clear();
      }
   }

   public int getLevel() {
      return this.level;
   }

   @Override
   public int getCount() {
      return this.count;
   }

   @Override
   public boolean add(StatusListener listener) {
      synchronized(this.statusListenerListLock) {
         if (listener instanceof OnConsoleStatusListener) {
            boolean alreadyPresent = this.checkForPresence(this.statusListenerList, listener.getClass());
            if (alreadyPresent) {
               return false;
            }
         }

         this.statusListenerList.add(listener);
         return true;
      }
   }

   private boolean checkForPresence(List<StatusListener> statusListenerList, Class<?> aClass) {
      for(StatusListener e : statusListenerList) {
         if (e.getClass() == aClass) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void remove(StatusListener listener) {
      synchronized(this.statusListenerListLock) {
         this.statusListenerList.remove(listener);
      }
   }

   @Override
   public List<StatusListener> getCopyOfStatusListenerList() {
      synchronized(this.statusListenerListLock) {
         return new ArrayList(this.statusListenerList);
      }
   }
}
