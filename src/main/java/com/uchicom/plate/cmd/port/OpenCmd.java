// (C) 2012 uchicom
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ポートをオープンするコマンド
 *
 * @author Uchiyama Shigeki
 */
public class OpenCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "open";

  /** @param plate */
  public OpenCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.Cmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": port open\r\n"
        + "  format)open [hostname:]port\r\n"
        + "  ex)open 8080\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 0 && handler.getCurrentPort() != null) {
      return true;
    } else if (params.length == 1) {
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
    String port = handler.getCurrentPort();
    if (params.length == 1) {
      port = params[0];
    }
    return broker.getMain().openPort(port, true);
  }
}
