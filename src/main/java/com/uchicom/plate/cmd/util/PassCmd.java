// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * パスワードを設定するコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class PassCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "pass";

  public PassCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": pass password\r\n" + "  format)pass password\r\n" + "  ex)pass xxxxx\r\n";
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
    System.out.println("[" + params[0] + "]");
    handler.setPass(params[0]);
    return null;
  }
}
