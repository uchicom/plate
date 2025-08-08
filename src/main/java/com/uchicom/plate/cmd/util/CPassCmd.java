// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.util.Crypt;
import java.util.Base64;

/**
 * パスワードを変更するコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class CPassCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "cpass";

  public CPassCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": change password\r\n"
        + "  format)cpass newpassword\r\n"
        + "  ex)cpass yyyyy\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 1;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    broker
        .getMain()
        .setCryptPass(
            Base64.getEncoder()
                .encodeToString(Crypt.encrypt3(broker.getMain().getUser(), params[0])));
    handler.setPass(params[0]);
    return null;
  }
}
