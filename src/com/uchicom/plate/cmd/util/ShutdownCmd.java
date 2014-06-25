/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * サーバーを止めるコマンド
 * 
 * @author Uchiyama Shigeki
 * 
 */
public class ShutdownCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "shutdown";

	/**
	 * @param plate
	 */
	public ShutdownCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " " + CMD + ": shutdown plate server.\r\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {
		broker.getMain().exit();
		return true;
	}

}
