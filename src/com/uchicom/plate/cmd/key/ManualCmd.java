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
public class ManualCmd extends AbstractCmd {

    public static final String CMD = "manual";

    /**
     * @param name
     * @param broker
     */
    public ManualCmd(Commander broker) {
        super(CMD, broker);
    }
    /* (non-Javadoc)
     * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.CmdSocketHandler, java.lang.String[])
     */
    @Override
    public boolean execute(CmdSocketHandler handler, String[] params) {
        String port = handler.getCurrentPort();
        if (params.length == 2) {
            port = params[1];
        }
        return broker.getMain().manualKey(params[0], port);
    }

    /* (non-Javadoc)
     * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
     */
    @Override
    public String getHelp() {
        return " "
        + CMD
        + ": A key is set to manual.(The use command is required)\r\n"
        + "  format)manual key\r\n"
        + "  ex)manual test\r\n";
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
            return params[1].matches("^[A-Za-z0-9\\.:\\-/_]*$");
        } else {
            return false;
        }
    }
}
