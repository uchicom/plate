// (C) 2012 uchicom
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.CpInfo;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ポートにクラスパスを追加するコマンド。
 *
 * @author Uchiyama Shigeki
 */
public class AddPortCpCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "addportcp";

  public AddPortCpCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": add classPath to port\r\n"
        + "  format)addportcp classPath [hostname:]port\r\n"
        + "  ex)addportcp ./test.jar 8080\r\n";
  }

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

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {

    String port = handler.getCurrentPort();
    if (params.length == 2 || params.length == 4) {
      port = params[params.length - 1];
    }
    CpInfo cpInfo = new CpInfo(params[0]);
    var portMap = broker.getMain().getPortMap();
    if (!portMap.containsKey(port)) {
      throw new CmdException("ポート番号は存在しません." + port);
    }
    var porter = portMap.get(port);
    porter.addCp(cpInfo);
    return null;
  }
}
