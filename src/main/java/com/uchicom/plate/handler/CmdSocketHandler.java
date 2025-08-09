// (C) 2012 uchicom
package com.uchicom.plate.handler;

import com.uchicom.plate.Commander;
import com.uchicom.plate.Main;
import com.uchicom.plate.cmd.AbstractCmd;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * コマンドソケットハンドラー
 *
 * @author Uchiyama Shigeki
 */
public class CmdSocketHandler implements Handler {

  //    private static final ByteBuffer CMD_SET = ByteBuffer.wrap(new byte[] {
  //            '\r', 'p', 'l', 'a', 't', 'e' });
  //    private static final ByteBuffer CMD_PROMPT_SEPARATOR = ByteBuffer.wrap(new byte[] { ':' });
  //    private static final ByteBuffer CMD_PROMPT = ByteBuffer.wrap(new byte[] { '>' });

  private final ByteBuffer cmd = ByteBuffer.allocate(256);
  StringBuilder cmdBuff = new StringBuilder(256);
  StringBuilder resBuff = new StringBuilder(256);

  /** ユーザーが入力したユーザー名 */
  private String user = null;

  /** ユーザーが入力したパスワード */
  private String pass = null;

  /** ソケットチャンネル */
  private SocketChannel socketChannel;

  private final Main plate;

  public CmdSocketHandler(SocketChannel socketChannel, Main plate) {
    this.socketChannel = socketChannel;
    this.plate = plate;
  }

  public void exit() throws IOException {
    if (socketChannel != null) {
      socketChannel.finishConnect();
      socketChannel.close();
    }
  }

  public String getPass() {
    return pass;
  }

  public void setPass(String pass) {
    this.pass = pass;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  /** ユーザー名を入力しているかどうかのフラグ */
  private boolean bUser = false;

  public boolean isbUser() {
    return bUser;
  }

  public void setbUser(boolean bUser) {
    this.bUser = bUser;
  }

  /** パスワードを入力して認証が通ったかのフラグ */
  private boolean bPass = false;

  public boolean isbPass() {
    return bPass;
  }

  public void setbPass(boolean bPass) {
    this.bPass = bPass;
  }

  /** ユーザーのカレントポート */
  private String currentPort = null;

  public String getCurrentPort() {
    return currentPort;
  }

  public void setCurrentPort(String currentPort) {
    this.currentPort = currentPort;
    //        this.currentPortBuffer = ByteBuffer.wrap(currentPort.getBytes());
  }

  //    /** バッファー情報のカレントポート */
  //    private ByteBuffer currentPortBuffer = null;

  @Override
  public void handle(SelectionKey key) throws IOException {

    if (!key.isValid()) {
      socketChannel.finishConnect();
    }

    if (key.isReadable()) {
      try {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        // \r\nまで読み込み続ける
        if (checkCmd(socketChannel)) {
          // コマンド解析可能
          // コマンド実行
          // 読み込むデータがなくても監視は続ける。
          // cmdチェック
          String cmdLine = cmdBuff.toString();
          cmdBuff.setLength(0);
          // 改行のみは次行へ
          if (cmdLine.length() > 0) {
            Map<String, AbstractCmd> cmdMap = Commander.getInstance().getCmdMap();
            String[] cmdParams = cmdLine.split(Commander.CMD_SPRIT_CHAR, 0);
            if (cmdParams.length > 0) {
              String cmdString = cmdParams[0];
              if (cmdMap.containsKey(cmdString)) {
                AbstractCmd cmd = cmdMap.get(cmdString);
                String[] params = Arrays.copyOfRange(cmdParams, 1, cmdParams.length);
                // 認証チェックしてエラーの場合はNG
                if (cmd.checkAuth(this)) {
                  // パラメータチェックしてエラーだったら
                  if (cmd.checkParam(this, params)) {
                    //
                    if (cmd.checkConfirm()) {
                      resBuff.append("execute ok ? yes/[no]\r\n");
                    }
                    try {
                      var result = cmd.execute(this, params);
                      if (result != null) {
                        resBuff.append(result);
                      }
                      resBuff.append("OK\r\n");
                    } catch (Throwable t) {
                      resBuff.append("NG: " + t.getMessage() + "\r\n");
                    }
                  } else {
                    resBuff.append("NG: parameter error\r\n");
                    resBuff.append(cmd.getHelp());
                  }
                } else {
                  resBuff.append("NG: Authentication error\r\n");
                }
              } else {
                // コマンドなしエラー
                resBuff.append("NG: Command not found.");
                resBuff.append(cmdLine);
                resBuff.append("\r\n");
              }
            }
          }
          writeCmdLine(resBuff);
        }
        if (key.isValid() && resBuff.length() > 0) {
          // コマンドラインの読み込み完了時に書き込み開始
          key.interestOps(SelectionKey.OP_WRITE);
        }
      } catch (IOException e) {
        plate.stackTrace("Command error", e);
        key.cancel();
      }
    }

    // 書き込みOKの場合は書き込む
    if (key.isValid() && key.isWritable()) {
      SocketChannel socketChannel = (SocketChannel) key.channel();
      // 書き込むデータない場合は監視をやめる。
      if (resBuff.length() == 0) {
        //                // 次のコマンドライン読み込み
        //
      } else {
        // 書き込むデータがあれば書き込む
        writeMessage(socketChannel);
      }
      key.interestOps(SelectionKey.OP_READ);
    }
  }

  private void writeCmdLine(StringBuilder cmdBuff) throws IOException {
    cmdBuff.append("plate");
    if (currentPort != null) {
      cmdBuff.append(":");
      cmdBuff.append(currentPort);
    }
    cmdBuff.append(">");
  }

  //    /**
  //     *
  //     * @param writer
  //     * @throws IOException
  //     */
  //    private void writeCmdLine(SocketChannel socketChannel) throws IOException {
  //        socketChannel.write(CMD_SET.asReadOnlyBuffer());
  //        if (currentPort != null) {
  //            socketChannel.write(CMD_PROMPT_SEPARATOR.asReadOnlyBuffer());
  //            socketChannel.write(currentPortBuffer.asReadOnlyBuffer());
  //        }
  //        socketChannel.write(CMD_PROMPT.asReadOnlyBuffer());
  ////        if (cmdBuff.length() > 0) {
  ////            socketChannel.write(ByteBuffer.wrap((cmdBuff.toString()).getBytes()));
  ////        }
  //    }
  //    private void writeNextLine() {
  //
  //    }
  //    /**
  //     *
  //     * @param writer
  //     * @throws IOException
  //     */
  //    private void eraseChar(SocketChannel socketChannel) throws IOException {
  //        byte[] bytes = null;
  //        if (currentPort == null) {
  //            bytes = new byte[1024];
  //        } else {
  //            bytes = new byte[1024];
  //        }
  //        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
  //        byteBuffer.put(CMD_SET.asReadOnlyBuffer());
  //        if (currentPort != null) {
  //            byteBuffer.put(CMD_PROMPT_SEPARATOR.asReadOnlyBuffer());
  //            byteBuffer.put(currentPortBuffer.asReadOnlyBuffer());
  //        }
  //        byteBuffer.put(CMD_PROMPT.asReadOnlyBuffer());
  //        if (cmdBuff.length() > 0) {
  //            byteBuffer.put(ByteBuffer.wrap((cmdBuff.toString() + "
  // ").getBytes()).asReadOnlyBuffer());
  //        }
  //        byteBuffer.flip();
  //        socketChannel.write(byteBuffer);
  //    }

  // 実行クラスとパラメータ引数の文字列長を抽出する
  boolean escape = false;

  private boolean checkCmd(SocketChannel socketChannel) throws IOException {
    boolean lineEnd = false;
    int length = socketChannel.read(cmd);
    if (length == -1) {
      throw new IOException("クライアントが切断しました.");
    }
    cmd.flip();
    // 後ろからチェックして\r\nがあればコマンド解析開始
    while (cmd.position() < cmd.limit()) {
      int ch = (int) cmd.get();
      if (ch == 0x08) {
        // BSなので一文字消す。
        if (cmdBuff.length() > 0) {

          if (!cmdBuff.toString().matches("^ *c?pass +[^ ]+$")) {
            resBuff.append((char) ch);
            resBuff.append(" ");
            resBuff.append((char) ch);
          }
          cmdBuff.setLength(cmdBuff.length() - 1);
        }
      } else if (ch < 0x0 || ch == '\n' || ch == '\r') {
        resBuff.append("\r\n");
        lineEnd = true;
        break;
      } else if (ch >= 0x20 && ch != '\r') {
        if ('[' == ch || 0x7F < ch) {
          escape = true;
        } else if (escape) {
          escape = false;
        } else {
          cmdBuff.append((char) ch);
          if (!cmdBuff.toString().matches("^ *c?pass +[^ ]+$")) {
            resBuff.append((char) ch);
          }
        }
      } else if (0x7F == ch) {
        plate.info("7F");
        // なにもしていない
      } else {
        plate.info("other");
      }
    }
    cmd.clear();
    return lineEnd;
  }

  /** データを書き込んで0に設定 */
  private void writeMessage(SocketChannel socketChannel) throws IOException {
    socketChannel.write(ByteBuffer.wrap(resBuff.toString().getBytes(StandardCharsets.US_ASCII)));
    resBuff.setLength(0);
  }
}
