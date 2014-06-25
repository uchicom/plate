/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * @author Uchiyama Shigeki
 * 
 */
public class PassCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "pass";

	/**
	 * @param broker
	 */
	public PassCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " " + CMD + ": pass password\r\n"
				+ "  format)pass password\r\n"
				+ "  ex)pass xxxxx\r\n";
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
	 * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean checkParam(CmdSocketHandler handler, String[] params) {
		return params.length == 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {
	    System.out.println("[" + params[0] + "]");
		handler.setPass(params[0]);
		return true;
	}

}
