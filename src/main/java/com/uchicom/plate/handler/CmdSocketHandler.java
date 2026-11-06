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
        // 受信データを読み込み、改行ごとにコマンドを順次実行する
        readChannel(socketChannel);
        while (parseLine()) {
          executeLine();
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

  /** 入力済みの1行を解析して実行し、結果とプロンプトをresBuffに追記する */
  private void executeLine() throws IOException {
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

  private static final int IAC = 0xFF;
  private static final int SB = 0xFA;
  private static final int SE = 0xF0;
  private static final int WILL = 0xFB;
  private static final int DONT = 0xFE;

  /** Telnet IACシーケンスの解析状態 */
  private enum IacState {
    NONE,
    /** IAC受信直後 */
    COMMAND,
    /** WILL/WONT/DO/DONT受信後、オプションコード待ち */
    OPTION,
    /** サブネゴシエーション中 */
    SUB,
    /** サブネゴシエーション中にIAC受信 */
    SUB_IAC
  }

  /** エスケープシーケンスの解析状態 */
  private enum EscState {
    NONE,
    /** ESC受信直後 */
    ESC,
    /** CSI(ESC [)内。終端バイトまで読み飛ばす */
    CSI
  }

  private IacState iacState = IacState.NONE;
  private EscState escState = EscState.NONE;

  /** チャンネルから受信バッファへ追記する。未処理データは保持する。 */
  private void readChannel(SocketChannel socketChannel) throws IOException {
    int length = socketChannel.read(cmd);
    if (length == -1) {
      throw new IOException("クライアントが切断しました.");
    }
  }

  /**
   * 受信バッファを改行まで解析する。改行以降の未処理バイトは次回のために保持する。
   *
   * @return 改行を検出した場合true
   */
  private boolean parseLine() {
    boolean lineEnd = false;
    cmd.flip();
    while (cmd.hasRemaining()) {
      int ch = cmd.get() & 0xFF;
      if (skipIac(ch) || skipEscape(ch)) {
        continue;
      }
      if (ch == IAC) {
        iacState = IacState.COMMAND;
      } else if (ch == 0x1B) {
        escState = EscState.ESC;
      } else if (ch == 0x08) {
        // BSなので一文字消す。
        if (cmdBuff.length() > 0) {
          if (!cmdBuff.toString().matches("^ *c?pass +[^ ]+$")) {
            resBuff.append((char) ch);
            resBuff.append(" ");
            resBuff.append((char) ch);
          }
          cmdBuff.setLength(cmdBuff.length() - 1);
        }
      } else if (ch == '\n' || ch == '\r') {
        resBuff.append("\r\n");
        lineEnd = true;
        break;
      } else if (ch >= 0x20 && ch < 0x7F) {
        cmdBuff.append((char) ch);
        if (!cmdBuff.toString().matches("^ *c?pass +[^ ]+$")) {
          resBuff.append((char) ch);
        }
      }
      // 0x7F、その他の制御文字、0x80以上のバイトは無視する
    }
    cmd.compact();
    return lineEnd;
  }

  /**
   * IACシーケンス中のバイトを読み飛ばす。
   *
   * @return IACシーケンスの一部として消費した場合true
   */
  private boolean skipIac(int ch) {
    switch (iacState) {
      case COMMAND:
        if (ch == SB) {
          iacState = IacState.SUB;
        } else if (WILL <= ch && ch <= DONT) {
          iacState = IacState.OPTION;
        } else {
          // IAC IAC(0xFFリテラル)やNOPなどの2バイトコマンド
          iacState = IacState.NONE;
        }
        return true;
      case OPTION:
        iacState = IacState.NONE;
        return true;
      case SUB:
        if (ch == IAC) {
          iacState = IacState.SUB_IAC;
        }
        return true;
      case SUB_IAC:
        iacState = ch == SE ? IacState.NONE : IacState.SUB;
        return true;
      default:
        return false;
    }
  }

  /**
   * ESCシーケンス(カーソルキー等)中のバイトを読み飛ばす。リテラルの'['は対象外。
   *
   * @return エスケープシーケンスの一部として消費した場合true
   */
  private boolean skipEscape(int ch) {
    switch (escState) {
      case ESC:
        escState = ch == '[' ? EscState.CSI : EscState.NONE;
        return true;
      case CSI:
        if (0x40 <= ch && ch <= 0x7E) {
          escState = EscState.NONE;
        }
        return true;
      default:
        return false;
    }
  }

  /** データを書き込んで0に設定。部分書き込みに備えて全量書き込むまで繰り返す。 */
  private void writeMessage(SocketChannel socketChannel) throws IOException {
    ByteBuffer buffer = ByteBuffer.wrap(resBuff.toString().getBytes(StandardCharsets.UTF_8));
    while (buffer.hasRemaining()) {
      socketChannel.write(buffer);
    }
    resBuff.setLength(0);
  }
}
