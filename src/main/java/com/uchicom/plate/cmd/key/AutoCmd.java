// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーを自動で設定するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class AutoCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "auto";

  public AutoCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    String port = handler.getCurrentPort();
    if (params.length == 2) {
      port = params[1];
    }
    broker.getMain().autoKey(params[0], port);
    return null;
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": A key is set to auto.(The use command is required)\r\n"
        + "  format)auto key\r\n"
        + "  ex)auto test\r\n";
  }

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
