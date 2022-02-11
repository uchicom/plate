// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.scheduler.cron.Cron;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;

public class Schedule {
  Cron cron;

  public Schedule(Cron cron) {
    this.cron = cron;
  }

  public void register(Timer timer) {
    ScheduleTimerTask timerTask =
        new ScheduleTimerTask(
            this,
            () -> {
              // TODO ここに起動処理
              // cron.command
            });
    timer.schedule(timerTask, nextDate());
  }

  Date nextDate() {
    return Date.from(cron.nextDate().atZone(ZoneId.systemDefault()).toInstant());
  }
}
