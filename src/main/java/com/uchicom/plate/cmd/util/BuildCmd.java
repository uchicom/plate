// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/** @author Uchiyama Shigeki */
public class BuildCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "build";

  /** @param broker */
  public BuildCmd(Commander broker) {
    super(CMD, broker);
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
        + ": build ClassLoader  \r\n"
        + "  format)build [port:..]\r\n"
        + "  ex)build 8080:81\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 0 && handler.getCurrentPort() != null) {
      return true;
    } else if (params.length == 1) {
      return params[params.length - 1].matches("^[A-Za-z0-9]*$");
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    String port = handler.getCurrentPort();
    if (params.length == 1) {
      port = params[params.length - 1];
    }
    var portMap = broker.getMain().getPortMap();
    if (!portMap.containsKey(port)) {
      throw new CmdException("ポート番号は存在しません." + port);
    }
    var porter = portMap.get(port);
    porter.build();
    return null;
  }
}
