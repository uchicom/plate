// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.Starter;
import com.uchicom.plate.scheduler.cron.Cron;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Timer;

public class Schedule {
  Cron cron;
  Timer timer;
  Starter starter;
  ZonedDateTime nextDateTime;

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
    this.nextDateTime = cron.nextDateTime().atZone(ZoneId.systemDefault());
    return Date.from(nextDateTime.toInstant());
  }

  @Override
  public String toString() {
    return "    CRON:" + cron.toString() + "\r\n" + "    SCHDULE:" + nextDateTime.toString();
  }
}
