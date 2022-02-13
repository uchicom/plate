// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import com.uchicom.plate.util.ThrowRunnable;
import java.util.TimerTask;

public class ScheduleTimerTask extends TimerTask {

  Schedule schedule;
  ThrowRunnable<Throwable> runnable;

  public ScheduleTimerTask(Schedule schedule, ThrowRunnable<Throwable> runnable) {
    this.schedule = schedule;
    this.runnable = runnable;
  }

  @Override
  public void run() {
    try {
      runnable.run();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      schedule.register();
    }
  }
}
