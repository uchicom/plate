// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.scheduler.cron.CronParser;

public class ScheduleFactory {
  CronParser cronParser;

  public ScheduleFactory(CronParser cronParser) {
    this.cronParser = cronParser;
  }

  public Schedule create(String description) {
    return new Schedule(cronParser.parse(description));
  }
}
