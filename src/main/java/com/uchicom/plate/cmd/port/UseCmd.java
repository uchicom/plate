// (C) 2012 uchicom
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * カレントポートを設定するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class UseCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "use";

  /** @param plate */
  public UseCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " " + CMD + ": port use\r\n" + "  format)use [hostname:]port\r\n" + "  ex)use 8080\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 1) {
      return params[0].matches("^[A-Za-z0-9\\.:\\-/_]*$");
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean execute(CmdSocketHandler handler, String[] params) {
    if (broker.getMain().exists(params[0])) {
      handler.setCurrentPort(params[0]);
      return true;
    } else {
      return false;
    }
  }
}
