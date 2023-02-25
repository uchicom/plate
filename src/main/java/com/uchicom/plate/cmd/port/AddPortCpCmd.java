// (C) 2012 uchicom
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.CpInfo;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;
import java.net.MalformedURLException;

/**
 * ポートにクラスパスを追加するコマンド。
 *
 * @author Uchiyama Shigeki
 */
public class AddPortCpCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "addportcp";

  /** @param broker */
  public AddPortCpCmd(Commander broker) {
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
        + ": add classPath to port\r\n"
        + "  format)addportcp classPath [hostname:]port\r\n"
        + "  ex)addportcp ./test.jar 8080\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if ((params.length == 1) && handler.getCurrentPort() != null) {
      return true;
    } else if (params.length == 2) {
      return params[params.length - 1].matches("^[A-Za-z0-9\\.:\\-/_]*$");
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
    if (params.length == 2 || params.length == 4) {
      port = params[params.length - 1];
    }
    CpInfo cpInfo = null;
    try {
      if (params.length < 3) {
        cpInfo = new CpInfo(params[0]);
      } else {
        cpInfo = new CpInfo(params[0], params[1], params[2]);
      }
    } catch (MalformedURLException e) {
      throw new CmdException(e);
    }
    var portMap = broker.getMain().getPortMap();
    if (!portMap.containsKey(port)) {
      throw new CmdException("ポート番号は存在しません." + port);
    }
    var porter = portMap.get(port);
    porter.addCp(cpInfo);
    return null;
  }
}
