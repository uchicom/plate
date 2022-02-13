// (C) 2022 uchicom
package com.uchicom.plate.scheduler.cron;

import com.uchicom.util.Numbre;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

public class Cron {
  // enum EVERY="*",  ARRAY(1,3 or 1-3)
  String minute;
  String hour;
  String day;
  String month;
  String dayOfWeek;
  String command;
  int[] months;
  int[] days;
  int[] hours;
  int[] minutes;

  int[] triggers;
  int scheduledTriggerIndex = -1;
  // strategyを用意して検索する

  public Cron() {}

  public Cron(String[] description) {
    minute = description[0];
    hour = description[1];
    day = description[2];
    month = description[3];
    dayOfWeek = description[4];
    command = description[5];
    initTriggers(minute, hour, day, month, dayOfWeek, command);
    setScheduledTriggerIndex();
  }

  int[] parse(String pattern) {
    List<Integer> expandList = Numbre.expand(pattern);
    return expandList.stream().mapToInt(Integer::intValue).sorted().toArray();
  }

  int[] createNumbers(int start, int end) {
    int[] numbers = new int[end - start + 1];
    for (int i = 0; i < end - start + 1; i++) {
      numbers[i] = start + i;
    }
    return numbers;
  }

  int[] getNumbers(String condition, int start, int end) {
    if (condition == "*") {
      return createNumbers(start, end);
    } else {
      return parse(condition);
    }
  }

  int createTrigger(int month, int day, int hour, int minute) {
    return month * 100_00_00 + day * 100_00 + hour * 100 + minute;
  }

  void setScheduledTriggerIndex() {
    LocalDateTime now = LocalDateTime.now();
    int nowTrigger =
        createTrigger(now.getMonthValue(), now.getDayOfMonth(), now.getHour(), now.getMinute());
    scheduledTriggerIndex = getIndexByNibun(nowTrigger, 0, triggers.length);
  }

  int getIndexByNibun(int now, int start, int length) {
    if (length == 1) {
      if (triggers[start] <= now) {
        return start;
      }
      if (start == 0) {
        return triggers.length - 1;
      } else {
        return start - 1;
      }
    }
    int index = start + length / 2;
    if (triggers[index] == now) {
      return index;
    } else if (triggers[index] > now) {
      return getIndexByNibun(now, start, index - start);
    } else {
      int nextLength = length - (index + 1 - start);
      if (nextLength == 0) {
        return index;
      }
      return getIndexByNibun(now, index + 1, nextLength);
    }
  }

  void initTriggers(
      String minute, String hour, String day, String month, String dayOfWeek, String command) {
    // 作成時にint[]で01010000 1/1 0:0などを保持
    months = getNumbers(month, 1, 12);
    days = getNumbers(day, 1, 31);
    hours = getNumbers(hour, 0, 23);
    minutes = getNumbers(minute, 0, 59);
    triggers =
        IntStream.of(months)
            .flatMap(
                m ->
                    IntStream.of(days)
                        .flatMap(
                            d ->
                                IntStream.of(hours)
                                    .flatMap(
                                        h ->
                                            IntStream.of(minutes)
                                                .map(mi -> createTrigger(m, d, h, mi)))))
            .toArray();
  }

  public LocalDateTime nextDate() {
    // triggerIndex++をして順に移動する時間がオーバーしている場合は、triggerIndex++を進める
    // triggerIndexが最後まで行ったら、翌年で設定する
    // 365 * 24 * 60 * 60
    LocalDateTime now = LocalDateTime.now();

    // * = 0-59 map key(0-59)
    scheduledTriggerIndex++;
    if (scheduledTriggerIndex == triggers.length) {
      scheduledTriggerIndex = 0;
    }
    int nextPeriod = triggers[scheduledTriggerIndex];
    LocalDateTime schedule =
        LocalDateTime.now()
            .withMonth(nextPeriod / 100_00_00)
            .withDayOfMonth(nextPeriod / 100_00 % 100)
            .withHour(nextPeriod / 100 % 100)
            .withMinute(nextPeriod % 100)
            .withSecond(0)
            .withNano(0);
    if (now.isAfter(schedule)) {
      return schedule.plusYears(1);
    }
    return schedule;
  }
}
