// (C) 2025 uchicom
package com.uchicom.plate.enumeration;

public enum KeyState {
  ENABLE,
  DISABLE;

  public boolean isEnable() {
    return this == ENABLE;
  }
}
