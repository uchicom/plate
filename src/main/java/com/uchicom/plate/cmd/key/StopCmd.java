// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * 開始中のキーを止めるコマンド
 *
 * @author Uchiyama Shigeki
 */
public class StopCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "stop";

  public StopCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": A started key is stopped.(The use command is required)\r\n";
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    String[] newParams = new String[params.length - 1];
    System.arraycopy(params, 1, newParams, 0, newParams.length);
    broker.getMain().shutdownKey(handler.getCurrentPort(), params[0], newParams);
    return null;
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
