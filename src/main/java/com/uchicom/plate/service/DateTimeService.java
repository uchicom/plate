// (C) 2023 uchicom
package com.uchicom.plate.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeService {

  public DateTimeService() {}

  public LocalDateTime getLocalDateTime() {
    return LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
  }
}
