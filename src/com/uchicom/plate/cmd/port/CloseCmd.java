/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ポートを閉じるコマンド。
 * 
 * @author Uchiyama Shigeki
 * 
 */
public class CloseCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "close";

	/**
	 * @param plate
	 */
	public CloseCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " " + CMD + ": port close\r\n"
				+ "  format)close [hostname:]port\r\n"
				+ "  ex)close 8081\r\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean checkParam(CmdSocketHandler handler, String[] params) {
		if (params.length == 0 && handler.getCurrentPort() != null) {
			return true;
		} else if (params.length == 1) {
			return params[0].matches("^[0-9]*$");
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {
		String port = handler.getCurrentPort();
		if (params.length == 1) {
			port = params[0];
		}
		return broker.getMain().closePort(port, true);

	}

}
