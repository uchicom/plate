// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーを使用可にするコマンド
 *
 * @author Uchiyama Shigeki
 */
public class EnableCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "enable";

  /**
   * @param plate
   */
  public EnableCmd(Commander broker) {
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
        + ": A key is set to enable.(The use command is required)\r\n"
        + "  format)enable key\r\n"
        + "  ex)enable test\r\n";
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
    } else if (params.length == 2) {
      return params[1].matches("^[A-Za-z0-9]*$");
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
    String port = handler.getCurrentPort();
    if (params.length == 2) {
      port = params[1];
    }
    broker.getMain().enableKey(params[0], port);
    return null;
  }
}
