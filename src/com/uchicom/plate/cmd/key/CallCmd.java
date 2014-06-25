/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーを呼び出すコマンド
 * 
 * @author Uchiyama Shigeki
 * 
 */
public class CallCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "call";

	/**
	 * @param plate
	 */
	public CallCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean checkParam(CmdSocketHandler handler, String[] params) {
		return params.length >= 1 && handler.getCurrentPort() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {
		int length = params.length - 1;
		String[] newParams = new String[length];
		System.arraycopy(params, 1, newParams, 0, length);
		return broker.getMain().callKey(handler.getCurrentPort(), params[0],
				newParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " " + CMD
				+ ": A key is called.(The use command is required)\r\n"
				+ "  format)call key [param1 ..]\r\n"
				+ "  ex)call test param1 param2\r\n";
	}

}
