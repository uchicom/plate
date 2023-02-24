// (C) 2023 uchicom
package com.uchicom.plate.exception;

public class ServiceException extends Exception {
  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(Throwable cause) {
    super(cause);
  }
}
