/**
 * (c) 2013 uchicom
 */
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class DSetCmd extends AbstractCmd {

    /** コマンド文字列 */
    public static final String CMD = "dset";

    /**
     * @param name
     * @param broker
     */
    public DSetCmd(Commander broker) {
        super(CMD, broker);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
     */
    @Override
    public String getHelp() {
        return " " + CMD + ": dset System Property  \r\n"
                + "  format)dset name value\r\n"
                + "  ex)dset test config\r\n";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
     * CmdSocketHandler, java.lang.String[])
     */
    @Override
    public boolean checkParam(CmdSocketHandler handler, String[] params) {
        if (params.length == 2) {
            return params[params.length - 2].matches("^[A-Za-z0-9]*$") &&
            params[params.length - 1].matches("^[A-Za-z0-9]*$");
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
        System.setProperty(params[0], params[1]);
        return true;
    }
}
