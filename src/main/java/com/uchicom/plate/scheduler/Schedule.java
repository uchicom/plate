// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.Starter;
import com.uchicom.plate.scheduler.cron.Cron;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Timer;

public class Schedule {
  Cron cron;
  Timer timer;
  Starter starter;
  ZonedDateTime nextDateTime;

  public Schedule(Cron cron, LocalDateTime now) {
    this.cron = cron;
    cron.setScheduledTriggerIndex(now);
  }

  public void register(Timer timer, Starter starter) {
    this.timer = timer;
    this.starter = starter;
    register();
  }

  void register() {
    register(new ScheduleTimerTask(this, starter));
  }

  void register(ScheduleTimerTask timerTask) {
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
