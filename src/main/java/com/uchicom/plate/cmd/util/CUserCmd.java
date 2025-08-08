// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.util.Crypt;
import java.util.Base64;

/**
 * ユーザを変更するコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class CUserCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "cuser";

  public CUserCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": change user\r\n" + "  format)cuser newuser\r\n" + "  ex)cuser test2\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 1;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    broker.getMain().setUser(params[0]);
    broker
        .getMain()
        .setCryptPass(
            Base64.getEncoder().encodeToString(Crypt.encrypt3(params[0], handler.getPass())));
    handler.setUser(params[0]);
    return null;
  }
}
