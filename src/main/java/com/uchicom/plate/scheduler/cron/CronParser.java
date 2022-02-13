// (C) 2022 uchicom
package com.uchicom.plate.scheduler.cron;

import com.uchicom.plate.util.Parser;

public class CronParser implements Parser<Cron> {
  /** @param description 分　時　日　月　曜日　コマンド */
  @Override
  public Cron parse(String description) {
    String[] splits = description.split(" +");
    if (splits.length != 6) {
      throw new CronParseException("column size is " + splits.length + "/6");
    }
    return new Cron(splits);
  }
}