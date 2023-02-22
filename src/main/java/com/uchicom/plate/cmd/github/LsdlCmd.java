// (C) 2023 uchicom
package com.uchicom.plate.cmd.github;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ダウンロードファイルを確認するコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class LsdlCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "lsdl";

  /** @param plate */
  public LsdlCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 1;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean execute(CmdSocketHandler handler, String[] params) {
    return broker.getMain().lsdl(params[0]);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": You can see download list.(The use command is required)\r\n"
        + "  format)lsdl key\r\n"
        + "  ex)ldsl hoge\r\n";
  }
}
