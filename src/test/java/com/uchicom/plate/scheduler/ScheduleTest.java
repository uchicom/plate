// (C) 2022 uchicom
package com.uchicom.plate.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.uchicom.plate.scheduler.cron.Cron;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class ScheduleTest {
  @Test
  public void nextDate1() {
    Cron cron = mock(Cron.class);
    doReturn(LocalDateTime.of(2022, 2, 14, 0, 0, 0)).when(cron).nextDateTime();
    Schedule schedule = new Schedule(cron, LocalDateTime.of(2023, 7, 30, 12, 34, 56));
    assertThat(schedule.nextDate())
        .isEqualTo(
            Date.from(
                LocalDateTime.of(2022, 2, 14, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));
  }
}
