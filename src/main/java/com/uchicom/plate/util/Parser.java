// (C) 2022 uchicom
package com.uchicom.plate.util;

public interface Parser<T> {
  T parse(String description);
}
