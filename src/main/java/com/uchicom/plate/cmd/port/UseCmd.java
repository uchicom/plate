// (C) 2012 uchicom
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * カレントポートを設定するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class UseCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "use";

  public UseCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": port use\r\n" + "  format)use [hostname:]port\r\n" + "  ex)use 8080\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 1) {
      return params[0].matches("^[A-Za-z0-9\\.:\\-/_]*$");
    } else {
      return false;
    }
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    if (!broker.getMain().exists(params[0])) {
      throw new CmdException("ポートが設定されていません.");
    }
    handler.setCurrentPort(params[0]);
    return null;
  }
}
