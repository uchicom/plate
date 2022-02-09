// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.util.Base64;
import com.uchicom.plate.util.Crypt;

/** @author Uchiyama Shigeki */
public class CPassCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "cpass";

  /** @param plate */
  public CPassCmd(Commander broker) {
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
        + ": change password\r\n"
        + "  format)cpass newpassword\r\n"
        + "  ex)cpass yyyyy\r\n";
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
  public boolean execute(CmdSocketHandler handler, String[] params) {
    broker
        .getMain()
        .setCryptPass(Base64.encode(Crypt.encrypt3(broker.getMain().getUser(), params[0])));
    handler.setPass(params[0]);
    return true;
  }
}
