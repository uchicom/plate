/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd.key;

import java.net.MalformedURLException;

import com.uchicom.plate.Commander;
import com.uchicom.plate.CpInfo;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーにクラスパスを追加するコマンド
 * 
 * @author Uchiyama Shigeki
 * 
 */
public class AddCpCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "addcp";

	/**
	 * @param broker
	 */
	public AddCpCmd(Commander broker) {
		super(CMD, broker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
	 */
	@Override
	public String getHelp() {
		return " "
				+ CMD
				+ ": A class path is added to a key. (The use command is required)\r\n"
				+ "  format)addcp key classPath\r\n"
				+ "  ex)addcp test ./test.jar\r\n"
				+ "  format)addcp key protocol host filepath\r\n"
				+ "  ex)addcp test http hogehoge.com /lib/test.jar\r\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean checkParam(CmdSocketHandler handler, String[] params) {
		return (params.length == 2 || params.length == 4)
				&& handler.getCurrentPort() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {

		CpInfo cpInfo = null;
		try {
			if (params.length < 4) {
				cpInfo = new CpInfo(params[1]);
			} else {
				cpInfo = new CpInfo(params[1], params[2], params[3]);
			}
		} catch (MalformedURLException e) {
			return false;
		}
		return broker.getMain().addCp(params[0], cpInfo, handler.getCurrentPort());
	}

}
