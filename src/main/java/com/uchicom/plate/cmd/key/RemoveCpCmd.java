// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーからクラスパスを除去するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class RemoveCpCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "removecp";

  /** @param broker */
  public RemoveCpCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": A class path is removed from a key.(The use command is required)\r\n"
        + "  format)removecp key classPath\r\n"
        + "  ex)removecp test ./test.jar\r\n";
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
    if (params.length == 3) {
      port = params[2];
    }
    return broker.getMain().removeCp(params[0], params[1], port);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 2 && handler.getCurrentPort() != null) {
      return true;
    } else if (params.length == 3) {
      return params[2].matches("^[A-Za-z0-9]*$");
    } else {
      return false;
    }
  }
}
