// (C) 2026 uchicom
package com.uchicom.plate;

/** テスト用: mainがスレッドを起動してすぐ戻るサービス。 */
public class FixtureService {
  public static volatile int shutdownCount;
  public static volatile boolean failShutdown;

  public static void main(String[] args) {
    var t = new Thread(() -> {});
    t.start();
    if (args.length > 0 && "error".equals(args[0])) {
      throw new IllegalStateException("main error");
    }
  }

  public static void shutdown() {
    shutdownCount++;
    if (failShutdown) {
      throw new IllegalStateException("shutdown error");
    }
  }
}
