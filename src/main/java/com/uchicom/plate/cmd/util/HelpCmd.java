// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ヘルプ情報を表示するコマンド。
 *
 * @author Uchiyama Shigeki
 */
public class HelpCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "help";

  public HelpCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": display command list or version infomation\r\n";
  }

  @Override
  public boolean checkAuth(CmdSocketHandler handler) {
    return true;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    var builder = new StringBuilder(1024);
    builder.append("Command List\r\n");
    for (AbstractCmd cmd : broker.getCmdList()) {
      builder.append(cmd.getHelp());
      builder.append("\r\n");
    }
    return builder.toString();
  }
}
