/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 起動クラス
 * 
 * @author Uchiyama Shigeki
 * 
 */
public class Starter implements Runnable {

	/**  */
	private KeyInfo startingKey;
	/**  */
	private String[] params;
	
	/** 実行クラス保持用 */
	private Class<?> classObject;

	/**
	 * paramsを取得します。 
	 * @return params
	 */
	public String[] getParams() {
		return params;
	}

	/**
	 * paramsを設定します。 
	 * @param params
	 */
	public void setParams(String[] params) {
		this.params = params;
	}

	/**  */
	private long start;
	/**  */
	private long end;
	
	private long recoveryCount;
	
	private boolean finish;
	private boolean started;
	/**  */
	public static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	
	/** 起動元のソース */
	private int source;
	
	/** コマンドで実行 */
	public static int CMD = 0x1;
	
	/** ポートからキー呼び出し */
	public static int PORT = 0x2;
	
	/** 設定ファイルから呼び出し */
	public static int INIT = 0x4;

	/** 
	 */
	public boolean isAlive() {
		return started && !finish;
	}
	public void setStarted(boolean started) {
	    this.started = started;
	}

	/**
	 * 
	 * @param startingKey
	 * @param params
	 * @param plate
	 */
	public Starter(KeyInfo startingKey, String[] params, int source) {
		this.startingKey = startingKey;
		this.params = params;
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    if (start != 0 || !finish) {
    		start = System.currentTimeMillis();
    		try {
    		    invoke(params);
    		} catch (SecurityException e) {
    			e.printStackTrace();
    		} catch (NoSuchMethodException e) {
    			e.printStackTrace();
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		} catch (InvocationTargetException e) {
    			e.printStackTrace();
    		} catch (ClassNotFoundException e) {
    			e.printStackTrace();
    		} catch (Throwable e) {
    		    e.printStackTrace();
    		} finally {
    			end = System.currentTimeMillis();
    			shutdown(null);
    		}
	    }
	}

	/**
	 * オブジェクトの文字列表現を取得する。
	 */
	public String toString() {
		StringBuffer strBuff = new StringBuffer(100);
		if (source == CMD) {
			strBuff.append("   CMD");
		} else if (source == PORT) {
			strBuff.append("   NET");
		} else {
		    strBuff.append("   INI");
		}
		strBuff.append(" [");
		if (start == 0) {
			strBuff.append("----/--/-- --:--:--.---");
		} else {
			strBuff.append(format.format(new Date(start)));
		}
		strBuff.append("]-[");
		if (end == 0) {
			strBuff.append("----/--/-- --:--:--.---");
		} else {
			strBuff.append(format.format(new Date(end)));
		}
		strBuff.append("] ");
		if (recoveryCount > 0 || finish) {
	        strBuff.append("(");
	        if (finish) {
	            strBuff.append("E:");
	        }
	        strBuff.append(recoveryCount);
	        strBuff.append(") ");
		}
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				if (i != 0) {
					strBuff.append(" ");
				}
				strBuff.append(params[i]);
			}
		}
        strBuff.append("\r\n");
		return strBuff.toString();
	}

	public void shutdown() {
	    
	}
	/**
	 * 実行プログラムを終了する
	 * 実行クラスのshutdownメソッドを呼び出す
	 * @param params メソッド呼び出し時に渡すパラメータ
	 */
	public void shutdown(String[] params) {
	    if (!started) {
	        end = System.currentTimeMillis();
	        finish = true;
	    } else if (!finish) {
    		try {
    	        Method method = classObject.getMethod("shutdown",
    	                new Class[] { String[].class });
    	        method.invoke(classObject, new Object[] { params });
    		} catch (SecurityException e) {
    			e.printStackTrace();
    			System.err.print(e.getMessage());
    		} catch (NoSuchMethodException e) {
    			e.printStackTrace();
    			System.err.print(e.getMessage());
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    			System.err.print(e.getMessage());
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    			System.err.print(e.getMessage());
    		} catch (InvocationTargetException e) {
    			e.printStackTrace();
    			System.err.print(e.getMessage());
    		} finally {
    		}
	    } else {
	        end = System.currentTimeMillis();
	    }
	}

	/** 
	 * 
	 * @param params
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
    public void invoke(String[] params) throws SecurityException,
            NoSuchMethodException, ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        invoke(startingKey.getMethodName() == null ? "main" : startingKey.getMethodName(), params);
    }

    /**
     * 
     * @param methodName
     * @param params
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public void invoke(String methodName, String[] params)
            throws ClassNotFoundException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            SecurityException, NoSuchMethodException {
        System.out.println(Thread.currentThread().getContextClassLoader());
        if (startingKey.getClassLoader() != null) {
            Thread.currentThread().setContextClassLoader(startingKey.getClassLoader());
            classObject = startingKey.getClassLoader().loadClass(startingKey.getClassName());
        } else {
            Thread.currentThread().setContextClassLoader(startingKey.getPorter().getClassLoader());
            classObject = startingKey.getPorter().loadClass(startingKey.getClassName());
        }
        Method method = classObject.getMethod(methodName,
                new Class[] { String[].class });
        method.invoke(classObject, new Object[] { params });
    }


	/**
	 * startingKeyを取得します。
	 * 
	 * @return startingKey
	 */
	public KeyInfo getStartingKey() {
		return startingKey;
	}

	/**
	 * startingKeyを設定します。
	 * 
	 * @param startingKey
	 */
	public void setStartingKey(KeyInfo startingKey) {
		this.startingKey = startingKey;
	}

    /**
     * sourceを取得します。 
     * @return source
     */
    public int getSource() {
        return source;
    }

    /**
     * sourceを設定します。 
     * @param source
     */
    public void setSource(int source) {
        this.source = source;
    }

    /**
     * finishを取得します。 
     * @return finish
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * finishを設定します。 
     * @param finish
     */
    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    /**
     * recoveryCountを取得します。 
     * @return recoveryCount
     */
    public long getRecoveryCount() {
        return recoveryCount;
    }

    /**
     * recoveryCountを設定します。 
     * @param recoveryCount
     */
    public void setRecoveryCount(long recoveryCount) {
        this.recoveryCount = recoveryCount;
    }

}
