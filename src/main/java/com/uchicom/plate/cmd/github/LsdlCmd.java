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

  public LsdlCmd(Commander broker, DeployService deployService) {
    super(CMD, broker);
    this.deployService = deployService;
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 1;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    var key = params[0];
    var config = broker.getMain().getConfig();
    var release = config.release.get(key);
    if (release == null) {
      throw new CmdException("release key:" + key + "は設定されていません.");
    }
    return deployService.lsdl(release);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": You can see download list.(The use command is required)\r\n"
        + "  format)lsdl key\r\n"
        + "  ex)ldsl hoge\r\n";
  }
}
