// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.util.Base64;
import com.uchicom.plate.util.Crypt;

/**
 * @author Uchiyama Shigeki
 */
public class CUserCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "cuser";

  /**
   * @param plate
   */
  public CUserCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " " + CMD + ": change user\r\n" + "  format)cuser newuser\r\n" + "  ex)cuser test2\r\n";
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

  /* (non-Javadoc)
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.CmdSocketHandler, java.lang.String[])
   */
  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    broker.getMain().setUser(params[0]);
    broker.getMain().setCryptPass(Base64.encode(Crypt.encrypt3(params[0], handler.getPass())));
    handler.setUser(params[0]);
    return null;
  }
}
