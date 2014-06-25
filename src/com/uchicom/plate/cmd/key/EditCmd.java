/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * @author Uchiyama Shigeki
 * 
 */
public class EditCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "edit";

	/**
	 * @param plate
	 */
	public EditCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " " + CMD
				+ ": A key is edited..(The use command is required)\r\n"
				+ "  format)edit key className2\r\n"
				+ "  ex)edit test com.uchicom.Test2\r\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean checkParam(CmdSocketHandler handler, String[] params) {
		return params.length >= 1 && params.length <= 3 && handler.getCurrentPort() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {
		if (params.length == 1) {
			return broker.getMain().editKey(params[0], null, null, handler.getCurrentPort());
		} else if (params.length == 2) {
			return broker.getMain().editKey(params[0], params[1], null, handler.getCurrentPort());
		} else if (params.length == 3) {
			return broker.getMain().editKey(params[0], params[1], params[2], handler.getCurrentPort());
		}
		return false;
	}

}
