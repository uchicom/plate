// (C) 2013 uchicom
package com.uchicom.plate.util;

/**
 * TELNETを表すクラス. RFC854 TELNET プロトコル仕様 RFC855 TELNET オプション仕様
 *
 * @author uchicom: Shigeki Uchiyama
 */
public class Telnet {
  /** 副交渉の終わり. */
  public static final byte SE = (byte) 240;
  /** 無操作. */
  public static final byte NOP = (byte) 241;
  /** Synch のデータストリーム部分. */
  public static final byte DATA_MARK = (byte) 242;
  /** これは常に TCP Urgent 通知を伴うべきである. */
  public static final byte BREAK = (byte) 243;
  /** NVT 文字 BRK. */
  public static final byte INTERRUPT_PROCESS = (byte) 244;
  /** IP 機能. */
  public static final byte ABORT_OUTPUT = (byte) 245;
  /** AO 機能. */
  public static final byte ARE_YOU_THERE = (byte) 246;
  /** AYT 機能. */
  public static final byte ERASE_CHARACTER = (byte) 247;
  /** EC 機能. */
  public static final byte ERASE_LINE = (byte) 248;
  /** GA シグナル. */
  public static final byte GO_AHEAD = (byte) 249;
  /** 後に続くのが示されたオプションの副交渉であることを表す. */
  public static final byte SB = (byte) 250;
  /** 示されたオプションの実行開始、または実行中かどうかの確認を望むことを表す. */
  public static final byte WILL = (byte) 251;
  /** 示されたオプションの実行拒否または継続実行拒否を表す. */
  public static final byte WONT = (byte) 252;
  /** 示されたオプションを実行するという相手側の要求、またはあなたがそれを実行することを期待しているという確認を表す. */
  public static final byte DO = (byte) 253;
  /** 示されたオプションを停止するという相手側の要求、またはあなたがそれを実行することをもはや期待しないという確認を表す. */
  public static final byte DONT = (byte) 254;
  /** データバイト 255. */
  public static final byte IAC = (byte) 255;

  /* 代表的なサブオプション */
  public static final byte TRANSMIT_BINARY = (byte) 0x0;
  /** データのエコー. */
  public static final byte ECHO = (byte) 0x1;
  /** Go Ahead抑止 */
  public static final byte SUPPRESS_GO_AHEAD = (byte) 0x3;
  /** telnet状態オプション */
  public static final byte TELNET_STATUS_OPTION = (byte) 0x5;
  /** telnetタイミングマーク */
  public static final byte TELNET_TIMING_MARK = (byte) 0x6;
  /** ターミナルタイプ */
  public static final byte TERMINAL_TYPE = (byte) 0x18;
  /** telnetラインモード */
  public static final byte TELNET_LINE_MODE = (byte) 0x22;
}
