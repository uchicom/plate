// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.Starter;
import com.uchicom.plate.scheduler.cron.Cron;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;

public class Schedule {
  Cron cron;
  ScheduleTimerTask timerTask;

  public Schedule(Cron cron) {
    this.cron = cron;
  }

  public void register(Timer timer, Starter starter) {
    timerTask = new ScheduleTimerTask(this, starter);
    timer.schedule(timerTask, nextDate());
  }

  Date nextDate() {
    return Date.from(cron.nextDate().atZone(ZoneId.systemDefault()).toInstant());
  }
}
