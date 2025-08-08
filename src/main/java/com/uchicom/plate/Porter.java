// (C) 2012 uchicom
package com.uchicom.plate;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * ポート制御クラス ポートの開閉とキー起動受付
 *
 * @author Uchiyama Shigeki
 */
public class Porter { // implements Runnable {

  /** plate */
  Main plate;

  /** 登録されている別名情報リスト */
  private List<KeyInfo> list = new ArrayList<>();

  /** ポートのクラスパスリスト */
  private List<CpInfo> cpList = new ArrayList<>();

  /** ポートのクラスローダー */
  URLClassLoader classLoader;

  public Porter(Main plate) {
    this.plate = plate;
  }

  public List<KeyInfo> getList() {
    return list;
  }

  public void setList(List<KeyInfo> list) {
    this.list = list;
  }

  public List<CpInfo> getCpList() {
    return cpList;
  }

  public void setCpList(List<CpInfo> cpList) {
    this.cpList = cpList;
  }

  public void addCp(CpInfo cpInfo) {
    cpList.add(cpInfo);
  }

  public void removeCp(String iCp) {
    cpList.remove(Integer.parseInt(iCp));
  }

  public void build() {
    if (cpList.size() > 0) {
      classLoader = new URLClassLoader(CpInfo.toUrlArray(cpList, CpInfo.STATUS_INCLUDED));
    }
    for (KeyInfo key : list) {
      key.build();
    }
  }

  /** load時のビルド */
  public void initBuild() {
    if (cpList.size() > 0) {
      classLoader = new URLClassLoader(CpInfo.toUrlArrayInclude(cpList));
    }
    for (KeyInfo key : list) {
      key.build();
    }
  }

  public URLClassLoader getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(URLClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /** クラスをロードする。 */
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    if (classLoader != null) {
      return classLoader.loadClass(className);
    } else {
      return Class.forName(className);
    }
  }
}
