// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.Starter.StarterKind;
import java.net.MalformedURLException;
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

  /**
   * methodNameを取得します。
   *
   * @return methodName
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * methodNameを設定します。
   *
   * @param methodName
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /** ポート */
  private Porter porter;

  /**
   * porterを取得します。
   *
   * @return porter
   */
  public Porter getPorter() {
    return porter;
  }

  /**
   * porterを設定します。
   *
   * @param porter
   */
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

  /**
   * classLoaderを取得します。
   *
   * @return classLoader
   */
  public URLClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * classLoaderを設定します。
   *
   * @param classLoader
   */
  public void setClassLoader(URLClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private List<Starter> starterList = new ArrayList<Starter>();

  /**
   * starterListを取得します。
   *
   * @return starterList
   */
  public List<Starter> getStarterList() {
    return starterList;
  }

  /**
   * starterListを設定します。
   *
   * @param starterList
   */
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

  /**
   * keyを取得します。
   *
   * @return key
   */
  public String getKey() {
    return key;
  }

  /**
   * keyを設定します。
   *
   * @param key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * classNameを取得します。
   *
   * @return className
   */
  public String getClassName() {
    return className;
  }

  /**
   * classNameを設定します。
   *
   * @param className
   */
  public void setClassName(String className) {
    this.className = className;
  }

  public boolean equals(Object obj) {
    if (obj instanceof KeyInfo) {
      return obj != null && this.key != null && this.key.equals(((KeyInfo) obj).key);
    }
    return false;
  }

  public int hashCode() {
    return key.hashCode();
  }

  /**
   * statusを取得します。
   *
   * @return status
   */
  public int getStatus() {
    return status;
  }

  /**
   * statusを設定します。
   *
   * @param status
   */
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

  /** @param classPath */
  public void addCp(CpInfo cpInfo) {
    cpList.add(cpInfo);
  }

  /**
   * @param protocol
   * @param host
   * @param file
   * @throws MalformedURLException
   */
  public void addCp(String protocol, String host, String file) throws MalformedURLException {
    cpList.add(new CpInfo(protocol, host, file));
  }

  /** @param classPath */
  public void removeCp(String iCp) {
    for (CpInfo cpInfo : cpList) {
      if (iCp.equals(cpInfo.getUrl().getPath())) {
        cpList.remove(cpInfo);
        break;
      }
    }
  }

  /**
   * cpListを取得します。
   *
   * @return cpList
   */
  public List<CpInfo> getCpList() {
    return cpList;
  }

  /**
   * cpListを設定します。
   *
   * @param cpList
   */
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

  public String toString() {
    StringBuffer strBuff = new StringBuffer();
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
    strBuff.append("\r\n");
    return strBuff.toString();
  }
}
