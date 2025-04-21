// (C) 2023 uchicom
package com.uchicom.plate.cmd.deploy;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.exception.ServiceException;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.service.DeployService;

/**
 * デプロイするコマンド.
 *
 * @author Uchiyama Shigeki
 */
public class DeployCmd extends AbstractCmd {

  private final DeployService deployService;

  /** コマンド文字列 */
  public static final String CMD = "deploy";

  /**
   * @param plate
   */
  public DeployCmd(Commander broker, DeployService deployService) {
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
    return params.length == 2;
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
    var tag = params[1];
    var config = broker.getMain().getConfig();
    if (!config.deploy.containsKey(key)) {
      throw new CmdException("deploy key:" + key + "は設定されていません.");
    }
    try {
      deployService.deploy(config.deploy.get(key), tag);
      return null;
    } catch (ServiceException e) {
      throw new CmdException(e);
    }
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
