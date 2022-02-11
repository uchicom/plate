// (C) 2012 uchicom
package com.uchicom.plate.handler;

import com.uchicom.plate.Commander;
import com.uchicom.plate.KeyInfo;
import com.uchicom.plate.Main;
import com.uchicom.plate.Porter;
import com.uchicom.plate.Starter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/** @author Uchiyama Shigeki */
public class ConnectPortHandler implements Handler {
  Main plate;
  Porter porter;

  public ConnectPortHandler(Main plate, Porter porter) {
    this.plate = plate;
    this.porter = porter;
  }

  private final ByteBuffer cmd = ByteBuffer.allocate(256);
  StringBuffer cmdBuff = new StringBuffer(256);
  StringBuffer resBuff = new StringBuffer(256);
  /* (non-Javadoc)
   * @see com.uchicom.plate.Handler#handle(java.nio.channels.SelectionKey)
   */
  @Override
  public void handle(SelectionKey key) throws IOException {
    if (key.isReadable()) {
      SocketChannel socketChannel = (SocketChannel) key.channel();
      // \r\nまで読み込み続ける
      checkCmd(socketChannel, key);
      // コマンド解析可能
      // コマンド実行
      // 読み込むデータがなくても監視は続ける。
      // cmdチェック
      String cmdLine = cmdBuff.toString();
      cmdBuff.setLength(0);
      // 改行のみは次行へ
      if (cmdLine.length() > 0) {
        String[] cmds = cmdLine.split(Commander.CMD_SPRIT_CHAR);
        String[] params = new String[cmds.length - 1];
        for (int iArray = 1; iArray < cmds.length; iArray++) {
          params[iArray - 1] = cmds[iArray];
        }
        if (cmds[0] != null && !"".equals(cmds[0])) {
          for (KeyInfo startingKey : porter.getList()) {
            if (cmds[0].equals(startingKey.getKey())
                && startingKey.getStatus() == KeyInfo.STATUS_ENABLE) {
              plate.start(startingKey.create(params, Starter.PORT));
              break;
            }
          }
        }
        socketChannel.close();
        key.cancel();
      }
    }
  }

  // 実行クラスとパラメータ引数の文字列長を抽出する
  boolean escape = false;

  private boolean checkCmd(SocketChannel socketChannel, SelectionKey key) throws IOException {
    boolean lineEnd = false;
    socketChannel.read(cmd);
    cmd.flip();
    // 後ろからチェックして\r\nがあればコマンド解析開始
    while (cmd.position() < cmd.limit()) {
      int ch = (int) cmd.get();
      System.out.println("[" + (char) ch + "]");
      if (ch == 0x08) {
        // BSなので一文字消す。
        if (cmdBuff.length() > 0) {
          cmdBuff.setLength(cmdBuff.length() - 1);
        }
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        continue;
      }
      if (ch < 0x0 || ch == '\n') {
        lineEnd = true;
        break;
      }
      if (ch >= 0x20 && ch != '\r') {
        if ('[' == ch || 0x7F < ch) {
          escape = true;
        } else if (escape) {
          escape = false;
        } else {
          cmdBuff.append((char) ch);
        }
      }
    }
    cmd.clear();
    return lineEnd;
  }
}
