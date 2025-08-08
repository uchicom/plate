// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;
import java.io.IOException;

/**
 * 接続を切断するコマンド。
 *
 * @author Uchiyama Shigeki
 */
public class ExitCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "exit";

  public ExitCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " " + CMD + ": session exit\r\n";
  }

  @Override
  public boolean checkAuth(CmdSocketHandler handler) {
    return true;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    // writer.write("Good bye!\r\n");
    // writer.flush();

    try {
      handler.exit();
    } catch (IOException e) {
      throw new CmdException(e);
    }
    return null;
  }
}
