// (C) 2023 uchicom
package com.uchicom.plate.cmd.github;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.service.DeployService;

/**
 * ダウンロードファイルを確認するコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class LsdlCmd extends AbstractCmd {

  private final DeployService deployService;

  /** コマンド文字列 */
  public static final String CMD = "lsdl";

  /**
   * @param plate
   */
  public LsdlCmd(Commander broker, DeployService deployService) {
    super(CMD, broker);
    this.deployService = deployService;
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
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    var key = params[0];
    var config = broker.getMain().getConfig();
    if (!config.deploy.containsKey(key)) {
      throw new CmdException("deploy key:" + key + "は設定されていません.");
    }
    return deployService.lsdl(config.deploy.get(key));
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
