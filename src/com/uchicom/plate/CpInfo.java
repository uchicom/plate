/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * クラスパス情報
 * @author Uchiyama Shigeki
 *
 */
public class CpInfo {
	
	public static final int STATUS_INCLUDED = 1;
	public static final int STATUS_EXCLUDED = 0;
	public static final int STATUS_UNCHANGE = -1;
	
	private URL url = null;
	
	private int status = 0;
	
	/**
	 * 
	 * @param classPath
	 * @throws MalformedURLException
	 */
	public CpInfo(String classPath) throws MalformedURLException {
		url = new URL(classPath);
	}
	
	/**
	 * 
	 * @param protocol
	 * @param host
	 * @param file
	 * @throws MalformedURLException
	 */
	public CpInfo(String protocol, String host, String file) throws MalformedURLException {
		url = new URL(protocol, host, file);
	}

	/**
	 * urlを取得します。 
	 * @return url
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * urlを設定します。 
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * statusを取得します。 
	 * @return status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * statusを設定します。 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * リストからURLの配列に入れ替えつつ、
	 * statusに引数のステータスを設定する。
	 * @param cpList
	 * @param status
	 * @return
	 */
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
	
	/**
	 * 初回ロード時にINCLUDEされていたデータをURLを作成する。
	 * @param cpList
	 * @return
	 */
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
	
	public String toString() {
	    StringBuffer strBuff = new StringBuffer();
        strBuff.append("  ");
//        strBuff.append((char) ('0' + iList)); //番号指定ではなくてパスを直接指定したほうがよさそう。
//        strBuff.append(' ');
        strBuff.append(url.toString());
        strBuff.append(' ');
        strBuff.append(status == CpInfo.STATUS_EXCLUDED ? "EXCLUDED"
                : "INCLUDED");
        strBuff.append("\r\n");
        return strBuff.toString();
	}
	
	public int hashCode() {
	    int hashCode = 0;
	    if (url != null) {
	        hashCode = url.hashCode();
	    }
	    return hashCode;
	}
	
	public boolean equals(Object object) {
	    if (url != null && object != null) {
	        if (object instanceof CpInfo) {
	            CpInfo cp = (CpInfo)object;
	            if (url.getPath() != null && cp.url != null) {
	                return url.getPath().equals(cp.url.getPath());
	            }
	        }
	        
	    }
	    return false;
	}
	
}
