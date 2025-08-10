// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.enumeration.CpState;
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

  private URL url = null;

  private CpState status = CpState.EXCLUDED;

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

  public void setStatus(CpState status) {
    this.status = status;
  }

  /** リストからURLの配列に入れ替えつつ、 statusに引数のステータスを設定する。 */
  public static URL[] toUrlArray(List<CpInfo> cpList, CpState status) {
    int iMaxList = cpList.size();
    URL[] urls = new URL[iMaxList];
    for (int iList = 0; iList < iMaxList; iList++) {
      CpInfo cpInfo = cpList.get(iList);
      urls[iList] = cpInfo.getUrl();
      if (status == CpState.INCLUDED) {
        cpInfo.setStatus(CpState.INCLUDED);
      } else if (status == CpState.EXCLUDED) {
        cpInfo.setStatus(CpState.EXCLUDED);
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
    strBuff.append(status);
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
