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
public class StopCmd extends AbstractCmd {

	/** コマンド文字列 */
	public static final String CMD = "stop";

	/**
	 * @param broker
	 */
	public StopCmd(Commander broker) {
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
				+ ": A started key is stopped.(The use command is required)\r\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
	 * CmdSocketHandler, java.lang.String[])
	 */
	@Override
	public boolean execute(CmdSocketHandler handler, String[] params) {
		String[] newParams = new String[params.length - 1];
		System.arraycopy(params, 1, newParams, 0, newParams.length);
		return broker.getMain().shutdownKey(handler.getCurrentPort(),
				params[0], newParams);
	}
	

    /*
     * (non-Javadoc)
     * 
     * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
     * CmdSocketHandler, java.lang.String[])
     */
    @Override
    public boolean checkParam(CmdSocketHandler handler, String[] params) {
        if (params.length == 1 && handler.getCurrentPort() != null) {
            return true;
        } else if (params.length == 2) {
            return params[1].matches("^[A-Za-z0-9]*$");
        } else {
            return false;
        }
    }

}
