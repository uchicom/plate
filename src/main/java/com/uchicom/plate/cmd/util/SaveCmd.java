// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;
import java.io.File;

/**
 * 設定を保存するコマンド。
 *
 * @author Uchiyama Shigeki
 */
public class SaveCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "save";

  public SaveCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": save filename\r\n"
        + "  format)save filename \r\n"
        + "  ex)save test.sv\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 1 || (params.length == 0 && broker.getMain().getLoadFile() != null);
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    if (params.length == 1) {
      broker.getMain().save(new File(params[0]), handler.getUser(), handler.getPass());
    } else {
      broker.getMain().save(handler.getUser(), handler.getPass());
    }
    return null;
  }
}
