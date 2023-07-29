// (C) 2023 uchicom
package com.uchicom.plate.service;

import java.time.LocalDateTime;
import javax.inject.Inject;

public class DateTimeService {
  @Inject
  public DateTimeService() {}

  public LocalDateTime getLocalDateTime() {
    return LocalDateTime.now();
  }
}
