// (C) 2022 uchicom
package com.uchicom.plate.scheduler.cron;

import java.time.LocalDateTime;

public class Cron {
  // enum EVERY="*",  ARRAY(1,3 or 1-3)
  private static final String EVERY = "*";
  String minute;
  String hour;
  String day;
  String month;
  String dayOfWeek;
  String command;

  int[] trigger;
  int scheduledTriggerIndex = -1;
  // strategyを用意して検索する

  public Cron(String[] description) {
    minute = description[0];
    hour = description[1];
    day = description[2];
    month = description[3];
    dayOfWeek = description[4];
    command = description[5];
    init();
  }

  void init() {
    // 作成時にint[]で01010000 1/1 0:0などを保持

    // TODO 2分法で起動対象を検索して最初のtriggerIndexを確定する
    int nibunIndex = nibun(); // nowをもとに起動するindexの1つ前を設定する
  }

  int nibun() {
    return 0;
  }

  public LocalDateTime nextDate() {
    // triggerIndex++をして順に移動する時間がオーバーしている場合は、triggerIndex++を進める
    // triggerIndexが最後まで行ったら、翌年で設定する
    // 365 * 24 * 60 * 60
    LocalDateTime now = LocalDateTime.now();
    // * = 0-59 map key(0-59)
    return now;
  }
}
