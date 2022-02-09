/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.util;

import java.io.IOException;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * 接続を切断するコマンド。
 * 
 * @author Uchiyama Shigeki
 * 
 */
public class ExitCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "exit";

	//
	// private ConnectChecker checker = null;
	/**
	 * @param plate
	 */
	public ExitCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " " + CMD + ": session exit\r\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#checkAuth(com.uchicom.plate.
	 * CmdSocketHandler)
	 */
	@Override
	public boolean checkAuth(CmdSocketHandler handler) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {
		// writer.write("Good bye!\r\n");
		// writer.flush();

		try {
			handler.exit();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}
