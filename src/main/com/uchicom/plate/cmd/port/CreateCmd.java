/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ポートを生成するコマンド。
 * 
 * @author Uchiyama Shigeki
 * 
 */
public class CreateCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "create";

	/**
	 * @param plate
	 */
	public CreateCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " " + CMD + ": port create\r\n"
				+ "  format)create [hostname:]port\r\n"
				+ "  ex)create 8081\r\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean checkParam(CmdSocketHandler handler, String[] params) {
		if (params.length == 1) {
			return params[0].matches("^[A-Za-z0-9\\.:\\-/_]*$");
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
		return broker.getMain().createPort(params[0]);

	}

}
