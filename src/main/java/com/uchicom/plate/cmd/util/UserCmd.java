// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ユーザを設定するコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class UserCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "user";

  public UserCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": user username\r\n" + "  format)user username\r\n" + "  ex)user user1\r\n";
  }

  @Override
  public boolean checkAuth(CmdSocketHandler handler) {
    return true;
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 1;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    handler.setUser(params[0]);
    return null;
  }
}
