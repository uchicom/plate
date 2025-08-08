// (C) 2022 uchicom
package com.uchicom.plate.cmd.github;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.exception.ServiceException;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.service.GithubService;

/**
 * キーを呼び出すコマンド
 *
 * @author Uchiyama Shigeki
 */
public class DownloadCmd extends AbstractCmd {

  private final GithubService githubService;

  /** コマンド文字列 */
  public static final String CMD = "download";

  public DownloadCmd(Commander broker, GithubService githubService) {
    super(CMD, broker);
    this.githubService = githubService;
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 2;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    var key = params[0];
    var tag = params[1];
    var config = broker.getMain().getConfig();
    if (!config.github.containsKey(key)) {
      throw new CmdException("github key:" + key + "は設定されていません.");
    }
    try {
      githubService.download(config.github.get(key), tag);
      return null;
    } catch (ServiceException e) {
      throw new CmdException(e);
    }
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": You can download assets, tarball or zipball of github.(The use command is required)\r\n"
        + "  format)download key tag\r\n"
        + "  ex)download hoge v20221214\r\n";
  }
}
