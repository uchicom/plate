// (C) 2012 uchicom
package com.uchicom.plate;

import java.io.IOException;
import java.net.MalformedURLException;
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

  /**
   * @param port
   * @param plate
   * @throws IOException
   */
  public Porter(Main plate) {
    this.plate = plate;
  }

  /**
   * listを取得します。
   *
   * @return list
   */
  public List<KeyInfo> getList() {
    return list;
  }

  /**
   * listを設定します。
   *
   * @param list
   */
  public void setList(List<KeyInfo> list) {
    this.list = list;
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

  /**
   * クラスをロードする。
   *
   * @param className
   * @return
   * @throws ClassNotFoundException
   */
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    if (classLoader != null) {
      return classLoader.loadClass(className);
    } else {
      return Class.forName(className);
    }
  }
}
