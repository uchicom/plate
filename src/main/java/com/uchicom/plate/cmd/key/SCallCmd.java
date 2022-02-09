// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/** @author Uchiyama Shigeki */
public class SCallCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "scall";

  /** @param plate */
  public SCallCmd(Commander broker) {
    super(CMD, broker);
  }
  /* (non-Javadoc)
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean execute(CmdSocketHandler handler, String[] params) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": A key is called with param crypting.(The use command is required)\r\n"
        + "  format)call key [param1 ..]\r\n"
        + "  ex)call test param1 param2\r\n";
  }
}
