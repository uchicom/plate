// (C) 2012 uchicom
package com.uchicom.plate.cmd.util;

import com.uchicom.plate.Commander;
import com.uchicom.plate.CpInfo;
import com.uchicom.plate.KeyInfo;
import com.uchicom.plate.Porter;
import com.uchicom.plate.Starter;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.handler.CmdSocketHandler;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 登録されている情報を表示するコマンド
 *
 * @author Uchiyama Shigeki
 */
public class ListCmd extends AbstractCmd {

  /** コマンド文字列 */
  public static final String CMD = "list";

  /**
   * @param plate
   */
  public ListCmd(Commander broker) {
    super(CMD, broker);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.Cmd#getHelp()
   */
  @Override
  public String getHelp() {
    return " " + CMD + ": key className infomation listup\r\n" + "\r\n";
  }

  /*
   * (non-Javadoc)
   *
   * @see com.uchicom.plate.cmd.AbstractCmd#execute(com.uchicom.plate.
   * CmdSocketHandler, java.lang.String[])
   */
  @Override
  public String execute(CmdSocketHandler handler, String[] params) {
    var builder = new StringBuilder(1024);
    Map<String, Porter> portMap = broker.getMain().getPortMap();
    Iterator<Entry<String, Porter>> ite = portMap.entrySet().iterator();
    if (ite.hasNext()) {
      builder.append("OK\r\n---plate Infomation---\r\n");
      builder.append(Starter.format.format(new Date()));
      builder.append("\r\n\r\n");
      while (ite.hasNext()) {
        // ポート情報
        Entry<String, Porter> ent = ite.next();

        builder.append(ent.getKey());
        builder.append("\r\n");
        // ポートクラスパス情報
        for (CpInfo cpInfo : ent.getValue().getCpList()) {
          builder.append(cpInfo);
        }
        builder.append("\r\n");
        // 別名情報
        for (KeyInfo startingKey : ent.getValue().getList()) {
          builder.append(startingKey);
        }
      }
    } else {
      builder.append("OK\r\n---plate Infomation---\r\nempty.\r\n");
    }
    return builder.toString();
  }
}
