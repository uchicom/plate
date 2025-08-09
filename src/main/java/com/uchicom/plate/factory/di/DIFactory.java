// (C) 2025 uchicom
package com.uchicom.plate.factory.di;

import com.uchicom.plate.Main;
import com.uchicom.plate.logging.DailyRollingFileHandler;
import com.uchicom.plate.scheduler.ScheduleFactory;
import com.uchicom.plate.scheduler.cron.CronParser;
import com.uchicom.plate.service.DateTimeService;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DIFactory {

  public static Main main() {
    return new Main(scheduleFactory(), logger());
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

  static Logger logger() {
    try {
      var PROJECT_NAME = "plate";
      var name =
          Stream.of(Thread.currentThread().getStackTrace())
              .map(StackTraceElement::getClassName)
              .filter(className -> className.endsWith("Main"))
              .findFirst()
              .orElse(PROJECT_NAME);
      Logger logger = Logger.getLogger(name);
      if (!PROJECT_NAME.equals(name)) {
        if (Arrays.stream(logger.getHandlers())
            .filter(handler -> handler instanceof DailyRollingFileHandler)
            .findFirst()
            .isEmpty()) {
          logger.addHandler(new DailyRollingFileHandler(name + "_%d.log"));
        }
      }
      return logger;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
