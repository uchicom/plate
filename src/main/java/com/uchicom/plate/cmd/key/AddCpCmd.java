// (C) 2012 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.CpInfo;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーにクラスパスを追加するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class AddCpCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "addcp";

  public AddCpCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": A class path is added to a key. (The use command is required)\r\n"
        + "  format)addcp key classPath\r\n"
        + "  ex)addcp test ./test.jar\r\n"
        + "  format)addcp key protocol host filepath\r\n"
        + "  ex)addcp test http hogehoge.com /lib/test.jar\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return (params.length == 2 || params.length == 4) && handler.getCurrentPort() != null;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {

    CpInfo cpInfo = new CpInfo(params[1]);
    broker.getMain().addCp(params[0], cpInfo, handler.getCurrentPort());
    return null;
  }
}
