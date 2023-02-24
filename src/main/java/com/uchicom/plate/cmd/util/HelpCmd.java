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

  /** @param plate */
  public HelpCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " " + CMD + ": display command list or version infomation\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkAuth(com.uchicom.plate.
   * CmdSocketHandler)
   */
  @Override
  public boolean checkAuth(CmdSocketHandler handler) {
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
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
