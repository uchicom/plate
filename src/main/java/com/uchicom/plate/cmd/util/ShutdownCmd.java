// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * サーバーを止めるコマンド
 *
 * @author Uchiyama Shigeki
 */
public class ShutdownCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "shutdown";

  public ShutdownCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": shutdown plate server.\r\n";
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    broker.getMain().exit();
    return null;
  }
}
