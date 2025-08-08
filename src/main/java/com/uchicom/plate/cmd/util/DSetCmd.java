// (C) 2013 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * システムプロパティを設定するコマンド.
 *
 * @author uchicom: Shigeki Uchiyama
 */
public class DSetCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "dset";

  public DSetCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": dset System Property  \r\n"
        + "  format)dset name value\r\n"
        + "  ex)dset test config\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 2) {
      return params[params.length - 2].matches("^[A-Za-z0-9]*$")
          && params[params.length - 1].matches("^[A-Za-z0-9]*$");
    } else {
      return false;
    }
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    System.setProperty(params[0], params[1]);
    return null;
  }
}
