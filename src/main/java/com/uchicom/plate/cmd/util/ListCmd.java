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

  /** @param plate */
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
  public boolean execute(CmdSocketHandler handler, String[] params) {
    return true;
  }

  @Override
  public String getOkMessage() {
    StringBuffer strBuff = new StringBuffer(1024);
    Map<String, Porter> portMap = broker.getMain().getPortMap();
    Iterator<Entry<String, Porter>> ite = portMap.entrySet().iterator();
    if (ite.hasNext()) {
      strBuff.append("OK\r\n---plate Infomation---\r\n");
      strBuff.append(Starter.format.format(new Date()));
      strBuff.append("\r\n\r\n");
      while (ite.hasNext()) {
        // ポート情報
        Entry<String, Porter> ent = ite.next();

        strBuff.append(ent.getKey());
        strBuff.append("\r\n");
        // ポートクラスパス情報
        for (CpInfo cpInfo : ent.getValue().getCpList()) {
          strBuff.append(cpInfo);
        }
        strBuff.append("\r\n");
        // 別名情報
        for (KeyInfo startingKey : ent.getValue().getList()) {
          strBuff.append(startingKey);
        }
      }
    } else {
      strBuff.append("OK\r\n---plate Infomation---\r\nempty.\r\n");
    }
    return strBuff.toString();
  }
}
