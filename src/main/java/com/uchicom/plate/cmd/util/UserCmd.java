// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/** @author Uchiyama Shigeki */
public class UserCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "user";

  /** @param broker */
  public UserCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " " + CMD + ": user username\r\n" + "  format)user username\r\n" + "  ex)user user1\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkAuth(com.uchicom.plate.
   * CmdSocketHandler)
   */
  @Override
  public boolean checkAuth(CmdSocketHandler handler) {
    return true;
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

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    handler.setUser(params[0]);
    return null;
  }
}
