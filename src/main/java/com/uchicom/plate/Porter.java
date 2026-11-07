// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.enumeration.CpState;
import java.io.IOException;
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

  public List<CpInfo> getCpList() {
    return cpList;
  }

  public void addCp(CpInfo cpInfo) {
    cpList.add(cpInfo);
  }

  public void removeCp(String iCp) {
    cpList.remove(Integer.parseInt(iCp));
  }

  public void build() {
    if (cpList.size() > 0) {
      var urls = CpInfo.toUrlArray(cpList, CpState.INCLUDED);
      closeClassLoader();
      classLoader = new URLClassLoader(urls);
    }
    for (KeyInfo key : list) {
      key.build();
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

  public URLClassLoader getClassLoader() {
    return classLoader;
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
