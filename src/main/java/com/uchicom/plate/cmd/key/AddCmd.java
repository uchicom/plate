// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * Key(別名)を追加するコマンド key, classPath, [static method]を引数に指定する。 static methodを指定しなかった場合はmainが呼び出される。
 *
 * @author Uchiyama Shigeki
 */
public class AddCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "add";

  public AddCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": A key is added to a port. (The use command is required)\r\n"
        + "  format)add key className [static method(default main)]\r\n"
        + "  ex)add test com.uchicom.Test start\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if ((params.length == 2 || params.length == 3) && handler.getCurrentPort() != null) {
      return true;
    } else {
      // パラメータ数エラー
      return false;
    }
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    if (params.length == 2) {
      broker.getMain().addKey(params[0], params[1], handler.getCurrentPort());
    } else {
      broker.getMain().addKey(params[0], params[1], params[2], handler.getCurrentPort());
    }
    return null;
  }
}
