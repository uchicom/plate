// (C) 2026 uchicom
package com.uchicom.plate.cmd.key;

import com.uchicom.plate.Commander;
import com.uchicom.plate.KeyInfo;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;

/**
 * キーの状態と、アプリの状況取得メソッド(static String status())の戻り値を表示するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class StatusCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "status";

  public StatusCmd(Commander broker) {
    super(CMD, broker);
  }

  @Override
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return params.length <= 1;
  }

  @Override
  public String execute(CmdSocketHandler handler, String[] params) throws CmdException {
    var targetKey = params.length == 1 ? params[0] : null;
    var currentPort = handler.getCurrentPort();
    var builder = new StringBuilder(256);
    var found = false;
    for (var entry : broker.getMain().getPortMap().entrySet()) {
      if (currentPort != null && !currentPort.equals(entry.getKey())) {
        continue;
      }
      for (KeyInfo keyInfo : entry.getValue().getList()) {
        if (targetKey != null && !targetKey.equals(keyInfo.getKey())) {
          continue;
        }
        found = true;
        append(builder, entry.getKey(), keyInfo);
      }
    }
    if (targetKey != null && !found) {
      throw new CmdException("キーが設定されていません.");
    }
    return builder.toString();
  }

  private void append(StringBuilder builder, String port, KeyInfo keyInfo) {
    builder
        .append(port)
        .append(' ')
        .append(keyInfo.getKey())
        .append(' ')
        .append(keyInfo.status)
        .append(" alive:")
        .append(keyInfo.isRunning())
        .append("\r\n");
    // 複数行の場合は2行目以降もインデントして、キーごとの表示を区別できるようにする
    builder
        .append("  status: ")
        .append(keyInfo.getStatus().replace("\r\n", "\r\n    "))
        .append("\r\n");
  }

  @Override
  public String getHelp() {
    return " "
        + CMD
        + ": The status of keys is shown.(All ports if the use command is not executed)\r\n"
        + "  format)status [key]\r\n"
        + "  ex)status test\r\n";
  }
}
