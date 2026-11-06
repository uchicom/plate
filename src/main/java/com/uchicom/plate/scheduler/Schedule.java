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
  volatile ScheduleTimerTask timerTask;
  volatile boolean cancelled;

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
    if (cancelled) {
      return;
    }
    this.timerTask = timerTask;
    timer.schedule(timerTask, nextDate());
  }

  /** スケジュールを取り消し、以降の再登録も行わない。 */
  public void cancel() {
    cancelled = true;
    var task = timerTask;
    if (task != null) {
      task.cancel();
    }
  }

  public boolean isCancelled() {
    return cancelled;
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
