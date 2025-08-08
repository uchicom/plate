// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * 開始中のキー名を変更するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class RenameCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "rename";

  public RenameCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": a key is changed.(The use command is required)\r\n"
        + "  format)edit key key2\r\n"
        + "  ex)edit test test2\r\n";
  }

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

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    String port = handler.getCurrentPort();
    if (params.length == 3) {
      port = params[2];
    }
    broker.getMain().renameKey(params[0], params[1], port);
    return null;
  }
}
