/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.uchicom.plate.handler.Handler;
import com.uchicom.plate.handler.PortServerHandler;

/**
 * ポート制御クラス
 * ポートの開閉とキー起動受付
 * @author Uchiyama Shigeki
 *
 */
public class Porter implements Runnable {

    private String address;
	/** ポート情報 */
	private String port = null;

	/** キー起動受付サーバー */
	ServerSocketChannel server = null;

	/** セレクター */
	Selector selector = null;

	/** plate */
	Main plate = null;

	/** 起動時刻 */
	long start = 0;

	/** 登録されている別名情報リスト */
	private List<KeyInfo> list = new ArrayList<KeyInfo>();

	/** ポートクローズ状態 */
	public static final int STATUS_CLOSE = 0;

	/** ポートオープン状態 */
	public static final int STATUS_OPEN = 1;

	/** ポートの状態 */
	private int status = 0;

	/** ポートのクラスパスリスト */
	private List<CpInfo> cpList = new ArrayList<CpInfo>();

	/** ポートのクラスローダー */
	URLClassLoader classLoader = null;

	/**
	 *
	 * @param port
	 * @param plate
	 * @throws IOException
	 */
	public Porter(String port, Main plate){
	    String[] addresses = port.split(":");
	    if (addresses.length > 1) {
	        this.address = addresses[0];
	        this.port = addresses[1];
	    } else {
	        this.address = "localhost";
	        this.port = port;
	    }
		this.plate = plate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//サーバーソケットを作成する
		try (ServerSocketChannel server = ServerSocketChannel.open();) {
			this.server = server;
			server.socket().setReuseAddress(true);
			server.socket().bind(new InetSocketAddress(address, Integer.parseInt(port)));
			server.configureBlocking(false);

			//セレクターを取得する
			selector = Selector.open();
			server.register(selector, SelectionKey.OP_ACCEPT, new PortServerHandler(plate, this));

			start = System.currentTimeMillis();
			//常駐するため無限ループ
			while (true) {
                try {
                    if (selector.select() > 0) {
                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> ite = keys.iterator();
                        while(ite.hasNext()) {
                            SelectionKey key = ite.next();
                            ite.remove();
                            try {
                                ((Handler)key.attachment()).handle(key);
                            } catch (IOException e) {
                                key.cancel();
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * サーバーを強制的にクローズする
	 */
	public void close() {
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				server = null;
			}
		}
	}



	/**
	 * listを取得します。
	 * @return list
	 */
	public List<KeyInfo> getList() {
		return list;
	}

	/**
	 * listを設定します。
	 * @param list
	 */
	public void setList(List<KeyInfo> list) {
		this.list = list;
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
	 * portを設定します。
	 * @param port
	 */
	public void setPort(String port) {
		this.port = port;
	}


	/**
	 * portを取得します。
	 * @return port
	 */
	public String getPort() {
		return port;
	}


	/**
	 * cpListを取得します。
	 * @return cpList
	 */
	public List<CpInfo> getCpList() {
		return cpList;
	}


	/**
	 * cpListを設定します。
	 * @param cpList
	 */
	public void setCpList(List<CpInfo> cpList) {
		this.cpList = cpList;
	}


	/**
	 *
	 * @param classPath
	 */
	public void addCp(CpInfo cpInfo) {
		cpList.add(cpInfo);
	}

	/**
	 *
	 * @param protocol
	 * @param host
	 * @param file
	 * @throws MalformedURLException
	 */
	public void addCp(String protocol, String host, String file) throws MalformedURLException {
		cpList.add(new CpInfo(protocol, host, file));
	}

	/**
	 *
	 * @param classPath
	 */
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

	/** load時のビルド
	 *
	 */
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
	 * @return classLoader
	 */
	public URLClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * classLoaderを設定します。
	 * @param classLoader
	 */
	public void setClassLoader(URLClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * クラスをロードする。
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
