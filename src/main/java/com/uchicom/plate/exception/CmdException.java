// (C) 2023 uchicom
package com.uchicom.plate.exception;

public class CmdException extends Exception {
  public CmdException(String message) {
    super(message);
  }

  public CmdException(Throwable cause) {
    super(cause);
  }
}
