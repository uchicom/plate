// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.Starter;
import com.uchicom.plate.scheduler.cron.Cron;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;

public class Schedule {
  Cron cron;
  Timer timer;
  Starter starter;

  public Schedule(Cron cron) {
    this.cron = cron;
  }

  public void register(Timer timer, Starter starter) {
    this.timer = timer;
    this.starter = starter;
    register();
  }

  public void register() {
    register(new ScheduleTimerTask(this, starter));
  }

  public void register(ScheduleTimerTask timerTask) {
    timer.schedule(timerTask, nextDate());
  }

  Date nextDate() {
    return Date.from(cron.nextDateTime().atZone(ZoneId.systemDefault()).toInstant());
  }
}
