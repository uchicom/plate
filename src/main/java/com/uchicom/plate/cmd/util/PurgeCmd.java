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

  /**
   * @param plate
   */
  public PurgeCmd(Commander broker) {
    super(CMD, broker);
  }

  /* (non-Javadoc)
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.CmdSocketHandler, java.lang.String[])
   */
  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    broker.getMain().purge();
    return null;
  }

  /* (non-Javadoc)
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " " + CMD + ": purge dead starter\r\n" + "  format)purge\r\n" + "  ex)purge\r\n";
  }
}
