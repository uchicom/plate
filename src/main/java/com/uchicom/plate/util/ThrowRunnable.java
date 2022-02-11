// (C) 2022 uchicom
package com.uchicom.plate.util;

public interface ThrowRunnable<T extends Throwable> {
  void run() throws T;
}
