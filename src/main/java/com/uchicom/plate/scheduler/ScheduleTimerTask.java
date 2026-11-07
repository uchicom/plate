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
      var key = starter.getStartingKey();
      // disableのキーは実行せずスケジュールだけ維持する
      if (key == null || key.status.isEnable()) {
        starter.run();
      }
    } catch (Throwable t) {
      starter.stackTrace("Starter run error", t);
    } finally {
      try {
        schedule.register();
      } catch (Throwable t) {
        // Timerスレッドを死なせない
        starter.stackTrace("Schedule register error", t);
      }
    }
  }
}
