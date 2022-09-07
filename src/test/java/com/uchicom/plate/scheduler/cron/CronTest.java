// (C) 2022 uchicom
package com.uchicom.plate.scheduler.cron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class CronTest {
  @Test
  public void parse() {
    Cron cron = new Cron();
    int[] actual = cron.parse("1,2");
    assertEquals(2, actual.length);
    assertEquals(1, actual[0]);
    assertEquals(2, actual[1]);
  }

  @Test
  public void createNumbers() {
    Cron cron = new Cron();
    int[] actual = cron.createNumbers(1, 3);
    assertEquals(3, actual.length);
    assertEquals(1, actual[0]);
    assertEquals(2, actual[1]);
    assertEquals(3, actual[2]);
  }

  @Test
  public void getNumbers() {
    Cron cron = new Cron();
    int[] actual = cron.getNumbers("1,2", 1, 3);
    assertEquals(2, actual.length);
    assertEquals(1, actual[0]);
    assertEquals(2, actual[1]);
  }

  @Test
  public void getNumbersAsta() {
    Cron cron = new Cron(new String[] {"1", "2", "3", "4", "5"});
    int[] actual = cron.getNumbers("*", 1, 3);
    assertEquals(3, actual.length);
    assertEquals(1, actual[0]);
    assertEquals(2, actual[1]);
    assertEquals(3, actual[2]);
  }

  @Test
  public void createTrigger() {
    Cron cron = new Cron();
    int actual = cron.createTrigger(1, 2, 3, 4);
    assertEquals(1020304, actual);
    actual = cron.createTrigger(11, 2, 3, 4);
    assertEquals(11020304, actual);
  }

  public void initTriggers() {
    Cron cron = new Cron();
    cron.initTriggers("0", "9", "*", "*", "*");
    assertEquals(12 * 31, cron.triggers.length);
    assertEquals(1010900, cron.triggers[0]);
    assertEquals(12310900, cron.triggers[12 * 31 - 1]);
  }

  @Test
  public void getIndexByNibun() {
    Cron cron = new Cron();
    cron.initTriggers("0", "9", "*", "*", "*");
    assertThat(cron.getIndexByNibun(1010800, 0, cron.triggers.length)).isEqualTo(12 * 31 - 1);
    assertThat(cron.getIndexByNibun(1010900, 0, cron.triggers.length)).isEqualTo(12 * 31 - 1);
    assertThat(cron.getIndexByNibun(1011000, 0, cron.triggers.length)).isEqualTo(0);
    assertThat(cron.getIndexByNibun(1021000, 0, cron.triggers.length)).isEqualTo(1);
    assertThat(cron.getIndexByNibun(12310800, 0, cron.triggers.length)).isEqualTo(12 * 31 - 2);
    assertThat(cron.getIndexByNibun(12310900, 0, cron.triggers.length)).isEqualTo(12 * 31 - 2);
    assertThat(cron.getIndexByNibun(12311000, 0, cron.triggers.length)).isEqualTo(12 * 31 - 1);
  }

  @Test
  public void constractor() {
    Cron cron = new Cron(new String[] {"0", "9", "*", "*", "*"});
    assertEquals(12 * 31, cron.triggers.length);
    assertTrue(cron.scheduledTriggerIndex > -1);
  }

  @Test
  public void pattern1() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "1,2", "*");
    assertEquals(2, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
  }

  @Test
  public void pattern2() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "2,1", "*");
    assertEquals(2, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
  }

  @Test
  public void pattern3() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "1-3", "*");
    assertEquals(3, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
    assertEquals(3010000, cron.triggers[2]);
  }

  @Test
  public void pattern4() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "1-3,5", "*");
    assertEquals(4, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
    assertEquals(3010000, cron.triggers[2]);
    assertEquals(5010000, cron.triggers[3]);
  }

  @SuppressWarnings("ReturnValueIgnored")
  @Test
  public void nextDateTime() {
    LocalDateTime now = LocalDateTime.of(2022, 02, 14, 0, 0, 0);
    LocalDateTime nextYear = now.plusYears(1);
    LocalDateTime yearAfterNext = nextYear.plusYears(1);
    Cron cron = null;
    try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
      mocked.when(LocalDateTime::now).thenReturn(now);
      cron = new Cron("0", "0", "14", "2", "*");
    }
    assertEquals(1, cron.triggers.length);
    assertEquals(2140000, cron.triggers[0]);
    assertThat(cron.nextDateTime()).isEqualTo(now);
    assertThat(cron.nextDateTime()).isEqualTo(nextYear);
    assertThat(cron.nextDateTime()).isEqualTo(yearAfterNext);
  }

  @SuppressWarnings("ReturnValueIgnored")
  @Test
  public void nextDateTimeDayOfWeek() {
    LocalDateTime now = LocalDateTime.of(2022, 02, 14, 0, 0, 0);
    Cron cron = null;
    try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
      mocked.when(LocalDateTime::now).thenReturn(now);
      cron = new Cron("0", "0", "*", "2", "2");
    }
    assertEquals(31, cron.triggers.length);
    assertThat(cron.nextDateTime()).isEqualTo(LocalDateTime.of(2022, 2, 15, 0, 0, 0));
    assertThat(cron.nextDateTime()).isEqualTo(LocalDateTime.of(2022, 2, 22, 0, 0, 0));
    assertThat(cron.nextDateTime()).isEqualTo(LocalDateTime.of(2023, 2, 7, 0, 0, 0));
  }

  @SuppressWarnings("ReturnValueIgnored")
  @Test
  public void nextDateTimeLeapYear() {
    LocalDateTime now = LocalDateTime.of(2024, 02, 28, 0, 0, 0);
    LocalDateTime nextDay = now.plusDays(1);
    LocalDateTime nextYear = now.plusYears(1).withDayOfMonth(1);
    Cron cron = null;
    try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
      mocked.when(LocalDateTime::now).thenReturn(now);

      cron = new Cron("0", "0", "*", "2", "*");
    }
    assertEquals(31, cron.triggers.length);
    assertThat(cron.nextDateTime()).isEqualTo(now);
    assertThat(cron.nextDateTime()).isEqualTo(nextDay);
    assertThat(cron.nextDateTime()).isEqualTo(nextYear);
  }

  @SuppressWarnings("ReturnValueIgnored")
  @Test
  public void checkDayOfWeek() {
    LocalDateTime now = LocalDateTime.of(2022, 02, 14, 0, 0, 0);
    Cron cron = null;
    try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
      mocked.when(LocalDateTime::now).thenReturn(now);

      cron = new Cron("0", "0", "*", "2", "1-3,7");
    }
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 14))).isTrue(); // Mon
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 15))).isTrue(); // Tsu
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 16))).isTrue(); // Wed
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 17))).isFalse(); // Thu
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 18))).isFalse(); // Fri
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 19))).isFalse(); // Sat
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 20))).isTrue(); // Sun
  }

  @Test
  public void checkDayOfWeekNull() {
    Cron cron = new Cron();
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 14))).isTrue(); // Mon
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 15))).isTrue(); // Tsu
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 16))).isTrue(); // Wed
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 17))).isTrue(); // Thu
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 18))).isTrue(); // Fri
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 19))).isTrue(); // Sat
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 20))).isTrue(); // Sun
  }

  @Test
  public void checkDayOfWeek0() {
    Cron cron = new Cron();
    cron.dayOfWeeks = new int[0];
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 14))).isTrue(); // Mon
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 15))).isTrue(); // Tsu
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 16))).isTrue(); // Wed
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 17))).isTrue(); // Thu
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 18))).isTrue(); // Fri
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 19))).isTrue(); // Sat
    assertThat(cron.checkDayOfWeek(LocalDate.of(2022, 2, 20))).isTrue(); // Sun
  }
}
