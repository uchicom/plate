// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/** @author Uchiyama Shigeki */
public class RenameCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "rename";

  /** @param broker */
  public RenameCmd(Commander broker) {
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
        + ": a key is changed.(The use command is required)\r\n"
        + "  format)edit key key2\r\n"
        + "  ex)edit test test2\r\n";
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
    return broker.getMain().renameKey(params[0], params[1], port);
  }
}
