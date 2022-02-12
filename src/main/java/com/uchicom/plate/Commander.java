// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.cmd.key.AddCmd;
import com.uchicom.plate.cmd.key.AddCpCmd;
import com.uchicom.plate.cmd.key.AutoCmd;
import com.uchicom.plate.cmd.key.CallCmd;
import com.uchicom.plate.cmd.key.DisableCmd;
import com.uchicom.plate.cmd.key.EditCmd;
import com.uchicom.plate.cmd.key.EnableCmd;
import com.uchicom.plate.cmd.key.ManualCmd;
import com.uchicom.plate.cmd.key.RemoveCmd;
import com.uchicom.plate.cmd.key.RemoveCpCmd;
import com.uchicom.plate.cmd.key.RenameCmd;
import com.uchicom.plate.cmd.key.StopCmd;
import com.uchicom.plate.cmd.port.AddPortCpCmd;
import com.uchicom.plate.cmd.port.RemovePortCpCmd;
import com.uchicom.plate.cmd.port.UseCmd;
import com.uchicom.plate.cmd.util.BuildCmd;
import com.uchicom.plate.cmd.util.CPassCmd;
import com.uchicom.plate.cmd.util.CUserCmd;
import com.uchicom.plate.cmd.util.ExitCmd;
import com.uchicom.plate.cmd.util.HelpCmd;
import com.uchicom.plate.cmd.util.ListCmd;
import com.uchicom.plate.cmd.util.LoadCmd;
import com.uchicom.plate.cmd.util.PassCmd;
import com.uchicom.plate.cmd.util.PurgeCmd;
import com.uchicom.plate.cmd.util.SaveCmd;
import com.uchicom.plate.cmd.util.ShutdownCmd;
import com.uchicom.plate.cmd.util.UserCmd;
import com.uchicom.plate.handler.CmdServerHandler;
import com.uchicom.plate.handler.Handler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * コマンド制御クラス
 *
 * @author Uchiyama Shigeki
 */
public class Commander implements Runnable {

  public static final String CMD_SET = "plate";
  public static final String CMD_PROMPT = ">";

  /** 文字列分割 */
  public static final String SPRIT_CHAR = ":";
  /** 文字列分割 */
  public static final String CMD_SPRIT_CHAR = " +";

  /** 生死フラグ */
  private static boolean alive = true;
  /** シングルトン用のコマンダー */
  private static Commander commander = null;

  /** コマンドで指定できる文字列数 */
  private Main plate;

  private String address;
  private int port = 0;
  private String currentPort;
  private List<AbstractCmd> cmdList = null;

  private Map<String, AbstractCmd> cmdMap = new HashMap<String, AbstractCmd>();
  /**
   * cmdMapを取得します。
   *
   * @return cmdMap
   */
  public Map<String, AbstractCmd> getCmdMap() {
    return cmdMap;
  }

  /**
   * cmdMapを設定します。
   *
   * @param cmdMap
   */
  public void setCmdMap(Map<String, AbstractCmd> cmdMap) {
    this.cmdMap = cmdMap;
  }

  public Commander(String address, int port, Main plate) {
    this.address = address;
    this.port = port;
    this.plate = plate;

    cmdList = new ArrayList<AbstractCmd>();
    // ポート制御コマンド
    cmdList.add(new UseCmd(this));
    cmdList.add(new AddPortCpCmd(this));
    cmdList.add(new RemovePortCpCmd(this));
    cmdList.add(new StopCmd(this));
    cmdList.add(new AutoCmd(this));
    cmdList.add(new ManualCmd(this));
    // キー制御コマンド
    cmdList.add(new AddCmd(this));
    cmdList.add(new RemoveCmd(this));
    cmdList.add(new EditCmd(this));
    cmdList.add(new EnableCmd(this));
    cmdList.add(new DisableCmd(this));
    cmdList.add(new CallCmd(this));
    cmdList.add(new AddCpCmd(this));
    cmdList.add(new RemoveCpCmd(this));
    cmdList.add(new RenameCmd(this));
    // 管理コマンド
    cmdList.add(new LoadCmd(this));
    cmdList.add(new SaveCmd(this));
    cmdList.add(new ExitCmd(this));
    cmdList.add(new ShutdownCmd(this));
    cmdList.add(new ListCmd(this));
    cmdList.add(new HelpCmd(this));
    cmdList.add(new BuildCmd(this));
    cmdList.add(new UserCmd(this));
    cmdList.add(new CUserCmd(this));
    cmdList.add(new PassCmd(this));
    cmdList.add(new CPassCmd(this));
    cmdList.add(new PurgeCmd(this));
    // コマンドクラスの準備
    for (AbstractCmd cmd : cmdList) {
      cmdMap.put(cmd.getName(), cmd);
    }
    commander = this;
  }

  public static Commander getInstance() {
    return commander;
  }

  /** スレッドとして起動し、 バッチ起動コマンド入力をひたすら待ち続ける。 */
  public void run() {
    // サーバーソケットを作成する
    try (ServerSocketChannel serverChannel = ServerSocketChannel.open(); ) {
      serverChannel.socket().setReuseAddress(true);
      // backはいくつか指定可能にしたほうが良いかな
      serverChannel.socket().bind(new InetSocketAddress(address, port), 10);
      serverChannel.configureBlocking(false);

      Selector selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT, new CmdServerHandler());
      while (alive) {
        try {
          if (selector.select() > 0) {
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> ite = keys.iterator();
            while (ite.hasNext()) {
              SelectionKey key = ite.next();
              ite.remove();
              try {
                ((Handler) key.attachment()).handle(key);
              } catch (IOException e) {
                key.cancel();
              }
            }
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  /**
   * @param writer
   * @throws IOException
   */
  public void writeCmdLine(OutputStreamWriter writer) throws IOException {
    writer.write("\r");
    writer.write(CMD_SET);
    if (currentPort != null) {
      writer.write(":");
      writer.write(currentPort);
    }
    writer.write(CMD_PROMPT);
  }
  /**
   * plateを取得します。
   *
   * @return plate
   */
  public Main getMain() {
    return plate;
  }

  /**
   * plateを設定します。
   *
   * @param plate
   */
  public void setMain(Main plate) {
    this.plate = plate;
  }

  /**
   * cmdListを取得します。
   *
   * @return cmdList
   */
  public List<AbstractCmd> getCmdList() {
    return cmdList;
  }

  /**
   * cmdListを設定します。
   *
   * @param cmdList
   */
  public void setCmdList(List<AbstractCmd> cmdList) {
    this.cmdList = cmdList;
  }
}
