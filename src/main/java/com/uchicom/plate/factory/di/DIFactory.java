// (C) 2025 uchicom
package com.uchicom.plate.factory.di;

import com.uchicom.plate.Main;
import com.uchicom.plate.scheduler.ScheduleFactory;
import com.uchicom.plate.scheduler.cron.CronParser;
import com.uchicom.plate.service.DateTimeService;

public class DIFactory {

  public static Main main() {
    return new Main(scheduleFactory());
  }

  static ScheduleFactory scheduleFactory() {
    return new ScheduleFactory(dateTimeService(), cronParser());
  }

  static DateTimeService dateTimeService() {
    return new DateTimeService();
  }

  static CronParser cronParser() {
    return new CronParser();
  }
}
