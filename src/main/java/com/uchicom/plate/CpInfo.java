// (C) 2012 uchicom
package com.uchicom.plate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * クラスパス情報
 *
 * @author Uchiyama Shigeki
 */
public class CpInfo {

  public static final int STATUS_INCLUDED = 1;
  public static final int STATUS_EXCLUDED = 0;
  public static final int STATUS_UNCHANGE = -1;

  private URL url = null;

  private int status = 0;

  public CpInfo(String classPath) {
    try {
      url = URI.create(classPath).toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  /** リストからURLの配列に入れ替えつつ、 statusに引数のステータスを設定する。 */
  public static URL[] toUrlArray(List<CpInfo> cpList, int status) {
    int iMaxList = cpList.size();
    URL[] urls = new URL[iMaxList];
    for (int iList = 0; iList < iMaxList; iList++) {
      CpInfo cpInfo = cpList.get(iList);
      urls[iList] = cpInfo.getUrl();
      if (status == STATUS_INCLUDED) {
        cpInfo.setStatus(STATUS_INCLUDED);
      } else if (status == STATUS_EXCLUDED) {
        cpInfo.setStatus(STATUS_EXCLUDED);
      }
    }
    return urls;
  }

  /** 初回ロード時にINCLUDEされていたデータをURLを作成する。 */
  public static URL[] toUrlArrayInclude(List<CpInfo> cpList) {
    int iMaxList = 0;
    for (int iList = 0; iList < iMaxList; iList++) {
      CpInfo cpInfo = cpList.get(iList);
      if (cpInfo.getStatus() == STATUS_INCLUDED) {
        iMaxList++;
      }
    }
    URL[] urls = new URL[iMaxList];
    int iArray = 0;
    for (int iList = 0; iList < iMaxList; iList++) {
      CpInfo cpInfo = cpList.get(iList);
      if (cpInfo.getStatus() == STATUS_INCLUDED) {
        urls[iArray++] = cpInfo.getUrl();
      }
    }
    return urls;
  }

  @Override
  public String toString() {
    StringBuilder strBuff = new StringBuilder();
    strBuff.append("  ");
    //        strBuff.append((char) ('0' + iList)); //番号指定ではなくてパスを直接指定したほうがよさそう。
    //        strBuff.append(' ');
    strBuff.append(url.toString());
    strBuff.append(' ');
    strBuff.append(status == CpInfo.STATUS_EXCLUDED ? "EXCLUDED" : "INCLUDED");
    strBuff.append("\r\n");
    return strBuff.toString();
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (url != null) {
      hashCode = url.hashCode();
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object object) {
    if (url != null && object != null) {
      if (object instanceof CpInfo cp) {
        if (url.getPath() != null && cp.url != null) {
          return url.getPath().equals(cp.url.getPath());
        }
      }
    }
    return false;
  }
}
