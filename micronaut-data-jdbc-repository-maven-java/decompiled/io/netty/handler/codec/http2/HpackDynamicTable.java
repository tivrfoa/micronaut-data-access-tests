package io.netty.handler.codec.http2;

final class HpackDynamicTable {
   HpackHeaderField[] hpackHeaderFields;
   int head;
   int tail;
   private long size;
   private long capacity = -1L;

   HpackDynamicTable(long initialCapacity) {
      this.setCapacity(initialCapacity);
   }

   public int length() {
      int length;
      if (this.head < this.tail) {
         length = this.hpackHeaderFields.length - this.tail + this.head;
      } else {
         length = this.head - this.tail;
      }

      return length;
   }

   public long size() {
      return this.size;
   }

   public long capacity() {
      return this.capacity;
   }

   public HpackHeaderField getEntry(int index) {
      if (index > 0 && index <= this.length()) {
         int i = this.head - index;
         return i < 0 ? this.hpackHeaderFields[i + this.hpackHeaderFields.length] : this.hpackHeaderFields[i];
      } else {
         throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + this.length());
      }
   }

   public void add(HpackHeaderField header) {
      int headerSize = header.size();
      if ((long)headerSize > this.capacity) {
         this.clear();
      } else {
         while(this.capacity - this.size < (long)headerSize) {
            this.remove();
         }

         this.hpackHeaderFields[this.head++] = header;
         this.size += (long)headerSize;
         if (this.head == this.hpackHeaderFields.length) {
            this.head = 0;
         }

      }
   }

   public HpackHeaderField remove() {
      HpackHeaderField removed = this.hpackHeaderFields[this.tail];
      if (removed == null) {
         return null;
      } else {
         this.size -= (long)removed.size();
         this.hpackHeaderFields[this.tail++] = null;
         if (this.tail == this.hpackHeaderFields.length) {
            this.tail = 0;
         }

         return removed;
      }
   }

   public void clear() {
      while(this.tail != this.head) {
         this.hpackHeaderFields[this.tail++] = null;
         if (this.tail == this.hpackHeaderFields.length) {
            this.tail = 0;
         }
      }

      this.head = 0;
      this.tail = 0;
      this.size = 0L;
   }

   public void setCapacity(long capacity) {
      if (capacity < 0L || capacity > 4294967295L) {
         throw new IllegalArgumentException("capacity is invalid: " + capacity);
      } else if (this.capacity != capacity) {
         this.capacity = capacity;
         if (capacity == 0L) {
            this.clear();
         } else {
            while(this.size > capacity) {
               this.remove();
            }
         }

         int maxEntries = (int)(capacity / 32L);
         if (capacity % 32L != 0L) {
            ++maxEntries;
         }

         if (this.hpackHeaderFields == null || this.hpackHeaderFields.length != maxEntries) {
            HpackHeaderField[] tmp = new HpackHeaderField[maxEntries];
            int len = this.length();
            if (this.hpackHeaderFields != null) {
               int cursor = this.tail;

               for(int i = 0; i < len; ++i) {
                  HpackHeaderField entry = this.hpackHeaderFields[cursor++];
                  tmp[i] = entry;
                  if (cursor == this.hpackHeaderFields.length) {
                     cursor = 0;
                  }
               }
            }

            this.tail = 0;
            this.head = this.tail + len;
            this.hpackHeaderFields = tmp;
         }
      }
   }
}
