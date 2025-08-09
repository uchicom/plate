// (C) 2012 uchicom
package com.uchicom.plate.handler;

import com.uchicom.plate.Main;
import com.uchicom.plate.util.Telnet;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * コネクトソケットハンドラー
 *
 * @author Uchiyama Shigeki
 */
public class ConnectSocketHandler implements Handler {
  private static final ByteBuffer lineOff =
      ByteBuffer.wrap(new byte[] {Telnet.IAC, Telnet.WILL, Telnet.TELNET_LINE_MODE});
  private static final ByteBuffer suppress =
      ByteBuffer.wrap(new byte[] {Telnet.IAC, Telnet.WILL, Telnet.SUPPRESS_GO_AHEAD});

  private static final ByteBuffer echoOff =
      ByteBuffer.wrap(new byte[] {Telnet.IAC, Telnet.WILL, Telnet.ECHO});
  //    private static final ByteBuffer dontEcho = ByteBuffer.wrap(new byte[] { Telnet.IAC,
  // Telnet.WONT, Telnet.ECHO});
  private static final ByteBuffer buffer =
      ByteBuffer.wrap(
          new byte[] {
            'W', 'e', 'l', 'c', 'o', 'm', ' ', 't', 'o', ' ', 'p', 'l', 'a', 't', 'e', ' ', 'C',
            'o', 'n', 's', 'o', 'l', 'e', '.', '\r', '\n', 'p', 'l', 'a', 't', 'e', '>'
          });

  private int status = 0;
  private final ByteBuffer cmd = ByteBuffer.allocate(256);

  boolean off = false;
  private final Main plate;

  public ConnectSocketHandler(Main plate) {
    this.plate = plate;
  }

  @Override
  public void handle(SelectionKey key) throws IOException {

    if (key.isWritable()) {
      // サーバーのあくせぷと実施(サーバは一個だからいいけど。
      SocketChannel socketChannel = (SocketChannel) key.channel();
      switch (status) {
        case 0 -> {
          socketChannel.write(echoOff.asReadOnlyBuffer());
          key.interestOps(SelectionKey.OP_READ);
          plate.info("ECHOOFF1");
          status = 1;
        }
        case 2 -> {
          socketChannel.write(suppress.asReadOnlyBuffer());
          key.interestOps(SelectionKey.OP_READ);
          plate.info("SUPPRESS");
          status = 3;
        }
        case 4 -> {
          socketChannel.write(lineOff.asReadOnlyBuffer());
          plate.info("LINEOFF");
          key.interestOps(SelectionKey.OP_READ);
          status = 5;
        }
        case 6 -> {
          socketChannel.write(buffer.asReadOnlyBuffer());
          socketChannel.register(
              key.selector(), SelectionKey.OP_READ, new CmdSocketHandler(socketChannel, plate));
          plate.info("REGISTER");
          status = 7;
        }
      }
    }

    if (key.isReadable()) {
      ((SocketChannel) key.channel()).read(cmd);

      for (int i = 0; i < cmd.position(); i++) {
        byte ch = (byte) cmd.get(i++);
        if (Telnet.IAC == ch) {
          status++;

          switch (cmd.get(i++)) {
            case Telnet.DONT -> plate.info("DONT");
            case Telnet.WILL -> plate.info("WILL");
            case Telnet.DO -> plate.info("DO");
            case Telnet.WONT -> plate.info("WONT");
            default -> plate.info("NOT DO,DONT,WILL,WONT");
          }

          switch (cmd.get(i++)) {
            case Telnet.ECHO -> {
              off = true;
              plate.info("ECHO");
            }
            case Telnet.TELNET_LINE_MODE -> plate.info("LINE");

            case Telnet.SUPPRESS_GO_AHEAD -> plate.info("SUPPRESS_GO_AHEAD");

            default -> plate.info("OTHER");
          }
        } else {
          plate.info(Integer.toHexString(0xFF & ch));
        }
        key.interestOps(SelectionKey.OP_WRITE);
      }
      cmd.clear();
    }
  }
}
