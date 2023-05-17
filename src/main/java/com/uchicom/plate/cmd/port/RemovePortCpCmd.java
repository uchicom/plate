// (C) 2012 uchicom
package com.uchicom.plate.cmd.port;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * ポートからクラスパスを除去する。
 *
 * @author Uchiyama Shigeki
 */
public class RemovePortCpCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "removeportcp";

  /**
   * @param broker
   */
  public RemovePortCpCmd(Commander broker) {
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
        + ": remove classPath from port\r\n"
        + "  format)removeportcp classPath [hostname:]port\r\n"
        + "  ex)removeportcp ./test.jar 8080 \r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#checkParam(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    if (params.length == 1 && handler.getCurrentPort() != null) {
      return true;
    } else if (params.length == 2) {
      return params[1].matches("^[A-Za-z0-9\\.:\\-/_]*$");
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
    if (params.length == 2) {
      port = params[1];
    }
    broker.getMain().removePortCp(params[0], port);
    return null;
  }
}
