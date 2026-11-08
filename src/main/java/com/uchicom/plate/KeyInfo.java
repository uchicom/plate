// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.Starter.StarterKind;
import com.uchicom.plate.enumeration.CpState;
import com.uchicom.plate.enumeration.KeyState;
import com.uchicom.plate.enumeration.RecoveryMethod;
import com.uchicom.plate.scheduler.Schedule;
import com.uchicom.plate.util.PriorityClassLoader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 起動名情報クラス
 *
 * @author Uchiyama Shigeki
 */
public class KeyInfo {

  /** キー */
  private String key;

  /** 完全クラス名 */
  private String className;

  /** スタティックメソッド名 */
  private String methodName;

  public String shutdownMethodName;

  /** 状況取得のスタティックメソッド名(引数なしでStringを返す)。nullの場合は未対応。 */
  public String statusMethodName = "status";

  /** 状況の最大文字数 */
  static final int MAX_STATUS_LENGTH = 64 * 1024;

  private Schedule schedule;

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /** ポート */
  private Porter porter;

  public Porter getPorter() {
    return porter;
  }

  public void setPorter(Porter porter) {
    this.porter = porter;
  }

  public KeyState status = KeyState.DISABLE;

  private RecoveryMethod recovery = RecoveryMethod.MANUAL;

  private List<CpInfo> cpList = new ArrayList<CpInfo>();

  private PriorityClassLoader classLoader = null;

  private final Main plate;

  public PriorityClassLoader getClassLoader() {
    return classLoader;
  }

  private List<Starter> starterList = new ArrayList<Starter>();

  public List<Starter> getStarterList() {
    return starterList;
  }

  public KeyInfo(String key) {
    this.key = key;
    this.plate = null;
  }

  public KeyInfo(String key, String className, String methodName, Main plate) {
    this.key = key;
    this.className = className;
    this.methodName = methodName;
    this.plate = plate;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof KeyInfo keyInfo) {
      return keyInfo != null && this.key != null && this.key.equals(keyInfo.key);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  public void setStatus(KeyState status) {
    this.status = status;
  }

  public Starter create(String[] params, StarterKind kind) {
    Starter starter = new Starter(this, params, kind, plate);
    starterList.add(starter);
    return starter;
  }

  public void addCp(CpInfo cpInfo) {
    cpList.add(cpInfo);
  }

  public void removeCp(String iCp) {
    for (CpInfo cpInfo : cpList) {
      if (iCp.equals(cpInfo.getUrl().getPath())) {
        cpList.remove(cpInfo);
        break;
      }
    }
  }

  public void build() {
    if (cpList.size() > 0) {
      URL[] urls = CpInfo.toUrlArray(cpList, CpState.INCLUDED);
      closeClassLoader();
      if (porter.getClassLoader() != null) {
        classLoader = new PriorityClassLoader(urls, porter.getClassLoader());
      } else {
        classLoader = new PriorityClassLoader(urls);
      }
    }
  }

  /** 旧ClassLoaderをcloseしてMetaspaceリークとjarのファイルロックを防ぐ。 */
  void closeClassLoader() {
    if (classLoader == null) {
      return;
    }
    try {
      classLoader.close();
    } catch (IOException e) {
      if (plate != null) {
        plate.stackTrace("ClassLoader close error", e);
      }
    } finally {
      classLoader = null;
    }
  }

  /** 稼働中のStarterがあるか。 */
  public boolean isRunning() {
    return starterList.stream().anyMatch(Starter::isAlive);
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public RecoveryMethod getRecovery() {
    return recovery;
  }

  public void setRecovery(RecoveryMethod recovery) {
    this.recovery = recovery;
  }

  public void setSchedule(Schedule schedule) {
    this.schedule = schedule;
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append(' ');
    builder.append(key);
    builder.append(' ');
    builder.append(className);
    builder.append(' ');
    builder.append(methodName);
    builder.append(' ');
    builder.append(status);
    builder.append(' ');
    builder.append(recovery);
    builder.append("\r\n");
    // 別名クラスパス情報

    for (CpInfo cpInfo : cpList) {
      builder.append(cpInfo);
    }
    // スターター情報
    for (Starter starter : starterList) {
      builder.append(starter);
    }
    // スケジュール
    if (schedule != null) {
      builder.append(schedule);
    }
    builder.append("\r\n");
    return builder.toString();
  }

  /** クラスをロードする。 */
  public Class<?> loadClass() throws ClassNotFoundException {
    if (classLoader == null) {
      return porter.loadClass(className);
    }
    return classLoader.loadClass(className);
  }

  /**
   * アプリの状況取得メソッド(引数なしのstaticメソッド)を呼び出して結果の文字列を返す。
   *
   * <p>例外は呼び出し元に伝播させず"error: ..."として返す。改行は\r\nに正規化し、長すぎる場合は切り詰める。
   */
  public String getStatus() {
    if (statusMethodName == null) {
      return "(not supported)";
    }
    if (!isRunning()) {
      return "not running";
    }
    var thread = Thread.currentThread();
    var originalClassLoader = thread.getContextClassLoader();
    try {
      var clazz = loadClass();
      var method = clazz.getMethod(statusMethodName);
      if (!Modifier.isStatic(method.getModifiers())) {
        return "error: " + statusMethodName + " is not static";
      }
      // Starter.invokeと同様に、呼び出し先のクラスローダをコンテキストクラスローダにする
      thread.setContextClassLoader(clazz.getClassLoader());
      return normalizeStatus(String.valueOf(method.invoke(null)));
    } catch (NoSuchMethodException e) {
      return "(not supported)";
    } catch (InvocationTargetException e) {
      return "error: " + describe(e.getCause() != null ? e.getCause() : e);
    } catch (Exception | LinkageError e) {
      return "error: " + describe(e);
    } finally {
      thread.setContextClassLoader(originalClassLoader);
    }
  }

  private static String describe(Throwable t) {
    var name = t.getClass().getName();
    return normalizeStatus(t.getMessage() == null ? name : name + ": " + t.getMessage());
  }

  static String normalizeStatus(String text) {
    var normalized = text.replace("\r\n", "\n").replace('\r', '\n').replace("\n", "\r\n");
    if (normalized.length() > MAX_STATUS_LENGTH) {
      return normalized.substring(0, MAX_STATUS_LENGTH) + "...(truncated)";
    }
    return normalized;
  }
}
