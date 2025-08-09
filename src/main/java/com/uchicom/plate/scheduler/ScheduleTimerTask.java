// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.Starter;
import java.util.TimerTask;

public class ScheduleTimerTask extends TimerTask {

  Schedule schedule;
  Starter starter;

  public ScheduleTimerTask(Schedule schedule, Starter starter) {
    this.schedule = schedule;
    this.starter = starter;
  }

  @Override
  public void run() {
    try {
      starter.run();
    } catch (Throwable t) {
      starter.stackTrace("Starter run error", t);
    } finally {
      schedule.register();
    }
  }
}
