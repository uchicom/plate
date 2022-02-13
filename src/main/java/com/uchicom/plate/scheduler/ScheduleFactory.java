// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.scheduler.cron.Cron;
import com.uchicom.plate.scheduler.cron.CronParser;

public class ScheduleFactory {
  CronParser cronParser;

  public ScheduleFactory(CronParser cronParser) {
    this.cronParser = cronParser;
  }

  public Schedule create(String description) {
    return new Schedule(cronParser.parse(description));
  }

  public Schedule create(String minute, String hour, String day, String month, String dayOfWeek) {
    return new Schedule(new Cron(minute, hour, day, month, dayOfWeek));
  }
}
