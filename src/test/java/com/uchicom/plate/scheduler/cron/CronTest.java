// (C) 2022 uchicom
package com.uchicom.plate.scheduler.cron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
    Cron cron = new Cron(new String[] {"1", "2", "3", "4", "5", "test"});
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
    cron.initTriggers("0", "9", "*", "*", "*", "test");
    assertEquals(12 * 31, cron.triggers.length);
    assertEquals(1010900, cron.triggers[0]);
    assertEquals(12310900, cron.triggers[12 * 31 - 1]);
  }

  @Test
  public void getIndexByNibun() {
    Cron cron = new Cron();
    cron.initTriggers("0", "9", "*", "*", "*", "test");
    assertThat(cron.getIndexByNibun(1010800, 0, cron.triggers.length)).isEqualTo(12 * 31 - 1);
    assertThat(cron.getIndexByNibun(1010900, 0, cron.triggers.length)).isEqualTo(0);
    assertThat(cron.getIndexByNibun(1011000, 0, cron.triggers.length)).isEqualTo(0);
    assertThat(cron.getIndexByNibun(1021000, 0, cron.triggers.length)).isEqualTo(1);
    assertThat(cron.getIndexByNibun(12310800, 0, cron.triggers.length)).isEqualTo(12 * 31 - 2);
    assertThat(cron.getIndexByNibun(12310900, 0, cron.triggers.length)).isEqualTo(12 * 31 - 1);
    assertThat(cron.getIndexByNibun(12311000, 0, cron.triggers.length)).isEqualTo(12 * 31 - 1);
  }

  @Test
  public void constractor() {
    Cron cron = new Cron(new String[] {"0", "9", "*", "*", "*", "test"});
    assertEquals(12 * 31, cron.triggers.length);
    assertTrue(cron.scheduledTriggerIndex > -1);
  }

  @Test
  public void pattern1() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "1,2", "*", "test");
    assertEquals(2, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
  }

  @Test
  public void pattern2() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "2,1", "*", "test");
    assertEquals(2, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
  }

  @Test
  public void pattern3() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "1-3", "*", "test");
    assertEquals(3, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
    assertEquals(3010000, cron.triggers[2]);
  }

  @Test
  public void pattern4() {
    Cron cron = new Cron();
    cron.initTriggers("0", "0", "1", "1-3,5", "*", "test");
    assertEquals(4, cron.triggers.length);
    assertEquals(1010000, cron.triggers[0]);
    assertEquals(2010000, cron.triggers[1]);
    assertEquals(3010000, cron.triggers[2]);
    assertEquals(5010000, cron.triggers[3]);
  }
}
