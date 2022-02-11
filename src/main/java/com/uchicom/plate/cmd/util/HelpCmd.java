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

  private StringBuffer strBuff = new StringBuffer();

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
  public boolean execute(CmdSocketHandler handler, String[] params) {
    synchronized (strBuff) {
      if (strBuff.length() == 0) {
        strBuff.append("Command List\r\n");
        for (AbstractCmd cmd : broker.getCmdList()) {
          strBuff.append(cmd.getHelp());
          strBuff.append("\r\n");
        }
        strBuff.append("Version 1.0.0\r\n");
      }
    }
    return true;
  }

  @Override
  public String getOkMessage() {
    return strBuff.toString();
  }
}
