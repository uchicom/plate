// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * 保存データから設定を読み込むコマンド。
 *
 * @author Uchiyama Shigeki
 */
public class LoadCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "load";

  public LoadCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": load filename\r\n"
        + "  format)load filename \r\n"
        + "  ex)load test.sv\r\n";
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length == 1;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    broker.getMain().load(params[0], handler.getUser(), handler.getPass());
    return null;
  }
}
