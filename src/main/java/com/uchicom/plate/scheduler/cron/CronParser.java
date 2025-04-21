// (C) 2022 uchicom
package com.uchicom.plate.scheduler.cron;

import com.uchicom.plate.util.Parser;

public class CronParser implements Parser<Cron> {

  public CronParser() {}

  /**
   * @param description 分　時　日　月　曜日　コマンド
   */
  @Override
  public Cron parse(String description) {
    String[] splits = description.split(" +");
    if (splits.length != 5) {
      throw new CronParseException("column size is " + splits.length + "/5");
    }
    return new Cron(splits);
  }
}
