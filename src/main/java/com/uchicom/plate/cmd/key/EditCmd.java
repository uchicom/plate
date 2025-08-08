// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーを編集するコマンド。
 *
 * @author Uchiyama Shigeki
 */
public class EditCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "edit";

  public EditCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": A key is edited..(The use command is required)\r\n"
        + "  format)edit key className2\r\n"
        + "  ex)edit test com.uchicom.Test2\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length >= 1 && params.length <= 3 && handler.getCurrentPort() != null;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    if (params.length == 1) {
      broker.getMain().editKey(params[0], null, null, handler.getCurrentPort());
    } else if (params.length == 2) {
      broker.getMain().editKey(params[0], params[1], null, handler.getCurrentPort());
    } else {
      broker.getMain().editKey(params[0], params[1], params[2], handler.getCurrentPort());
    }
    return null;
  }
}
