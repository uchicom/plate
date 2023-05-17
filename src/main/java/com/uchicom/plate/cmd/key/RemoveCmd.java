// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーを除去するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class RemoveCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "remove";

  /**
   * @param plate
   */
  public RemoveCmd(Commander broker) {
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
        + ": A key is removed from a port.(The use command is required)\r\n"
        + "  format)remove key\r\n"
        + "  ex)remove test\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 1 && handler.getCurrentPort() != null) {
      return true;
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
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    broker.getMain().removeKey(params[0], handler.getCurrentPort());
    return null;
  }
}
