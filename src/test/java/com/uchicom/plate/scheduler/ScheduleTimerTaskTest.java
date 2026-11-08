// (C) 2026 uchicom
package com.uchicom.plate.scheduler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.uchicom.plate.Starter;
import java.time.DateTimeException;
import org.junit.jupiter.api.Test;

public class ScheduleTimerTaskTest {

  @Test
  public void runRegisterError() {
    var schedule = mock(Schedule.class);
    var starter = mock(Starter.class);
    var exception = new DateTimeException("invalid");
    doThrow(exception).when(schedule).register();

    var task = new ScheduleTimerTask(schedule, starter);

    assertThatCode(task::run).doesNotThrowAnyException();
    verify(starter).stackTrace(eq("Schedule register error"), any(Throwable.class));
  }

  @Test
  public void runSkipsWhenDisabledAndRunsWhenEnabled() {
    var keyInfo = new com.uchicom.plate.KeyInfo("k");
    var starter = mock(Starter.class);
    org.mockito.Mockito.when(starter.getStartingKey()).thenReturn(keyInfo);
    var schedule = mock(Schedule.class);
    var task = new ScheduleTimerTask(schedule, starter);

    task.run();
    org.mockito.Mockito.verify(starter, org.mockito.Mockito.never()).run();
    verify(schedule).register();

    keyInfo.setStatus(com.uchicom.plate.enumeration.KeyState.ENABLE);
    task.run();
    verify(starter).run();
  }

  @Test
  public void runSetsStartedBeforeRun() {
    var keyInfo = new com.uchicom.plate.KeyInfo("k");
    keyInfo.setStatus(com.uchicom.plate.enumeration.KeyState.ENABLE);
    var starter = mock(Starter.class);
    org.mockito.Mockito.when(starter.getStartingKey()).thenReturn(keyInfo);
    var task = new ScheduleTimerTask(mock(Schedule.class), starter);

    task.run();

    var order = org.mockito.Mockito.inOrder(starter);
    order.verify(starter).setStarted(true);
    order.verify(starter).run();
  }
}
