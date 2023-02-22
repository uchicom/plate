// (C) 2023 uchicom
package com.uchicom.plate.cmd.deploy;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * デプロイするコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class DeployCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "deploy";

  /** @param plate */
  public DeployCmd(Commander broker) {
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
    return params.length == 2;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean execute(CmdSocketHandler handler, String[] params) {
    return broker.getMain().deploy(params[0], params[1]);
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
        + ": You can deploy from dir to dir.(The use command is required)\r\n"
        + "  format)deploy key tag\r\n"
        + "  ex)deploy hoge v20221214\r\n";
  }
}
