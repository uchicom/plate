// (C) 2012 uchicom
package com.uchicom.plate;

import java.time.format.DateTimeFormatter;

/**
 * 定数クラス.
 *
 * @author uchicom: Shigeki Uchiyama
 */
public class Constants {

  /** コマンドプロンプトのデフォルトポート */
  public static final int DEFAULT_PORT = 8124;

  public static final int DEFAULT_POOL_SIZE = 11;

  public static final String DEFAULT_ADDRESS = "localhost";

  public static final DateTimeFormatter dateTimeFormater =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  /** ログ出力ディレクトリ. */
  public static final String LOG_DIR = "./logs";

  /** ログ出力フォーマット. */
  public static final String LOG_FORMAT =
      "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n";
}
