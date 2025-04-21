// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.scheduler.cron.Cron;
import com.uchicom.plate.scheduler.cron.CronParser;
import com.uchicom.plate.service.DateTimeService;

public class ScheduleFactory {
  private final DateTimeService dateTimeService;
  private final CronParser cronParser;

  public ScheduleFactory(DateTimeService dateTimeService, CronParser cronParser) {
    this.dateTimeService = dateTimeService;
    this.cronParser = cronParser;
  }

  public Schedule create(String description) {
    return new Schedule(cronParser.parse(description), dateTimeService.getLocalDateTime());
  }

  public Schedule create(String minute, String hour, String day, String month, String dayOfWeek) {
    return new Schedule(
        new Cron(minute, hour, day, month, dayOfWeek), dateTimeService.getLocalDateTime());
  }
}
