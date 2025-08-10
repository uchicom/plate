// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.service.DateTimeService;
import com.uchicom.plate.util.ThrowRunnable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 起動クラス
 *
 * @author Uchiyama Shigeki
 */
public class Starter implements ThrowRunnable<Throwable> {

  private final DateTimeService dateTimeService = new DateTimeService();
  private KeyInfo startingKey;

  private String[] params;

  /** 実行クラス保持用 */
  private Class<?> classObject;

  private final Main plate;

  public String[] getParams() {
    return params;
  }

  public void setParams(String[] params) {
    this.params = params;
  }

  private LocalDateTime start;

  private LocalDateTime end;

  private long recoveryCount;

  private boolean finish;
  private boolean started;

  /** 起動元のソース */
  private StarterKind kind;

  public enum StarterKind {
    CALL,
    BATCH,
    SERVICE
  }

  public boolean isAlive() {
    return started && !finish;
  }

  public void setStarted(boolean started) {
    this.started = started;
  }

  public Starter(KeyInfo startingKey, String[] params, StarterKind kind, Main plate) {
    this.startingKey = startingKey;
    if (params == null) {
      this.params = new String[0];
    } else {
      this.params = params;
    }
    this.kind = kind;
    this.plate = plate;
  }

  @Override
  public void run() throws Throwable {
    if (start != null || !finish) {
      start = dateTimeService.getLocalDateTime();
      try {
        invoke(params);
      } finally {
        end = dateTimeService.getLocalDateTime();
      }
    }
  }

  /** オブジェクトの文字列表現を取得する。 */
  @Override
  public String toString() {
    StringBuilder strBuff = new StringBuilder(100);
    switch (kind) {
      case CALL -> strBuff.append("   CALL");
      case BATCH -> strBuff.append("   BATCH");
      case SERVICE -> strBuff.append("   SERVICE");
    }
    strBuff.append(" [");
    if (start == null) {
      strBuff.append("----/--/-- --:--:--.---");
    } else {
      strBuff.append(Constants.dateTimeFormater.format(start));
    }
    strBuff.append("]-[");
    if (end == null) {
      strBuff.append("----/--/-- --:--:--.---");
    } else {
      strBuff.append(Constants.dateTimeFormater.format(end));
    }
    strBuff.append("] ");
    if (recoveryCount > 0 || finish) {
      strBuff.append("(");
      if (finish) {
        strBuff.append("E:");
      }
      strBuff.append(recoveryCount);
      strBuff.append(") ");
    }
    if (params != null && params.length > 0) {
      for (int i = 0; i < params.length; i++) {
        if (i != 0) {
          strBuff.append(" ");
        }
        strBuff.append(params[i]);
      }
    }
    strBuff.append("\r\n");
    return strBuff.toString();
  }

  public void shutdown() {
    if (!started) {
      end = dateTimeService.getLocalDateTime();
      finish = true;
    } else if (!finish) {
      if (startingKey.shutdownMethodName != null) {
        try {
          Method method = classObject.getMethod(startingKey.shutdownMethodName);
          method.invoke(classObject);
          finish = true;
        } catch (SecurityException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (NoSuchMethodException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (IllegalArgumentException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (IllegalAccessException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (InvocationTargetException e) {
          plate.stackTrace("Starter invoke error", e);
        }
      }
    } else {
      end = dateTimeService.getLocalDateTime();
    }
  }

  /**
   * 実行プログラムを終了する 実行クラスのshutdownメソッドを呼び出す
   *
   * @param params メソッド呼び出し時に渡すパラメータ
   */
  public void shutdown(String[] params) {
    if (!started) {
      end = dateTimeService.getLocalDateTime();
      finish = true;
    } else if (!finish) {
      if (startingKey.shutdownMethodName != null) {
        try {
          Method method =
              classObject.getMethod(startingKey.shutdownMethodName, new Class[] {String[].class});
          method.invoke(classObject, new Object[] {params});
          finish = true;
        } catch (SecurityException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (NoSuchMethodException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (IllegalArgumentException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (IllegalAccessException e) {
          plate.stackTrace("Starter invoke error", e);
        } catch (InvocationTargetException e) {
          plate.stackTrace("Starter invoke error", e);
        }
      }
    } else {
      end = dateTimeService.getLocalDateTime();
    }
  }

  public void invoke(String[] params)
      throws SecurityException,
          NoSuchMethodException,
          ClassNotFoundException,
          IllegalArgumentException,
          IllegalAccessException,
          InvocationTargetException {
    invoke(startingKey.getMethodName() == null ? "main" : startingKey.getMethodName(), params);
  }

  public void invoke(String methodName, String[] params)
      throws ClassNotFoundException,
          IllegalArgumentException,
          IllegalAccessException,
          InvocationTargetException,
          SecurityException,
          NoSuchMethodException {
    if (startingKey.getClassLoader() != null) {
      Thread.currentThread().setContextClassLoader(startingKey.getClassLoader());
      classObject = startingKey.getClassLoader().loadClass(startingKey.getClassName());
    } else {
      Thread.currentThread().setContextClassLoader(startingKey.getPorter().getClassLoader());
      classObject = startingKey.getPorter().loadClass(startingKey.getClassName());
    }
    Method method = classObject.getMethod(methodName, new Class[] {String[].class});
    method.invoke(classObject, new Object[] {params});
  }

  public KeyInfo getStartingKey() {
    return startingKey;
  }

  public void setStartingKey(KeyInfo startingKey) {
    this.startingKey = startingKey;
  }

  public StarterKind getKind() {
    return kind;
  }

  public boolean isFinish() {
    return finish;
  }

  public void setFinish(boolean finish) {
    this.finish = finish;
  }

  public long getRecoveryCount() {
    return recoveryCount;
  }

  public void setRecoveryCount(long recoveryCount) {
    this.recoveryCount = recoveryCount;
  }

  public void stackTrace(String message, Throwable t) {
    plate.stackTrace(message, t);
  }
}
