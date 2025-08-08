// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.Starter.StarterKind;
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

  public static final int STATUS_ENABLE = 1;
  public static final int STATUS_DISABLE = 0;

  public static final int AUTO = 1;
  public static final int MANUAL = 0;

  private int status = STATUS_DISABLE;

  private int recovery = MANUAL;

  private List<CpInfo> cpList = new ArrayList<CpInfo>();

  private URLClassLoader classLoader = null;

  public URLClassLoader getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(URLClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private List<Starter> starterList = new ArrayList<Starter>();

  public List<Starter> getStarterList() {
    return starterList;
  }

  public void setStarterList(List<Starter> starterList) {
    this.starterList = starterList;
  }

  public KeyInfo(String key) {
    this.key = key;
  }

  public KeyInfo(String key, String className) {
    this(key, className, "main");
  }

  public KeyInfo(String key, String className, String methodName) {
    this.key = key;
    this.className = className;
    this.methodName = methodName;
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

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Starter create(Starter starter) {
    return create(starter.getParams(), starter.getKind());
  }

  public Starter create(String[] params, StarterKind kind) {
    Starter starter = new Starter(this, params, kind);
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

  public List<CpInfo> getCpList() {
    return cpList;
  }

  public void setCpList(List<CpInfo> cpList) {
    this.cpList = cpList;
  }

  public void build() {
    if (cpList.size() > 0) {
      URL[] urls = CpInfo.toUrlArray(cpList, CpInfo.STATUS_INCLUDED);
      if (Constants.DEBUG) {
        for (URL url : urls) {
          System.out.println(url.getPath());
        }
      }
      if (porter.getClassLoader() != null) {
        classLoader = new URLClassLoader(urls, porter.getClassLoader());
      } else {
        classLoader = new URLClassLoader(urls);
      }
    }
  }

  public int getRecovery() {
    return recovery;
  }

  public void setRecovery(int recovery) {
    this.recovery = recovery;
  }

  public void setSchedule(Schedule schedule) {
    this.schedule = schedule;
  }

  @Override
  public String toString() {
    StringBuilder strBuff = new StringBuilder();
    strBuff.append(' ');
    strBuff.append(key);
    strBuff.append(' ');
    strBuff.append(className);
    strBuff.append(' ');
    strBuff.append(methodName);
    strBuff.append(' ');
    strBuff.append(status == KeyInfo.STATUS_DISABLE ? "DISABLE" : "ENABLE");
    strBuff.append(' ');
    strBuff.append(recovery == KeyInfo.AUTO ? "AUTO" : "MANUAL");
    strBuff.append("\r\n");
    // 別名クラスパス情報

    for (CpInfo cpInfo : cpList) {
      strBuff.append(cpInfo);
    }
    // スターター情報
    for (Starter starter : starterList) {
      strBuff.append(starter);
    }
    // スケジュール
    if (schedule != null) {
      strBuff.append(schedule);
    }
    strBuff.append("\r\n");
    return strBuff.toString();
  }
}
