// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * スターターリストの死んだものを除去する。
 *
 * @author Uchiyama Shigeki
 */
public class PurgeCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "purge";

  public PurgeCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    broker.getMain().purge();
    return null;
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": purge dead starter\r\n" + "  format)purge\r\n" + "  ex)purge\r\n";
  }
}
