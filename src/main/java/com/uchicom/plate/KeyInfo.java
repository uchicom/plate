// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.Starter.StarterKind;
import com.uchicom.plate.enumeration.CpState;
import com.uchicom.plate.enumeration.KeyState;
import com.uchicom.plate.enumeration.RecoveryMethod;
import com.uchicom.plate.scheduler.Schedule;
import java.net.URL;
import java.net.URLClassLoader;
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

  private URLClassLoader classLoader = null;

  private final Main plate;

  public URLClassLoader getClassLoader() {
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
      if (porter.getClassLoader() != null) {
        classLoader = new URLClassLoader(urls, porter.getClassLoader());
      } else {
        classLoader = new URLClassLoader(urls);
      }
    }
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
}
