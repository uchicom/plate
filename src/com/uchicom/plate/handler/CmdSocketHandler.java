/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;

import com.uchicom.plate.Commander;
import com.uchicom.plate.Constants;
import com.uchicom.plate.cmd.AbstractCmd;

/**
 * @author Uchiyama Shigeki
 * 
 */
public class CmdSocketHandler implements Handler {

    private static final ByteBuffer CMD_SET = ByteBuffer.wrap(new byte[] {
            '\r', 'p', 'l', 'a', 't', 'e' });
    private static final ByteBuffer PASS_SET = ByteBuffer.wrap(new byte[] {
            '\r', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' });
    private static final ByteBuffer CMD_PROMPT_SEPARATOR = ByteBuffer.wrap(new byte[] { ':' });
    private static final ByteBuffer CMD_PROMPT = ByteBuffer.wrap(new byte[] { '>' });

    private final ByteBuffer cmd = ByteBuffer.allocate(256);
    StringBuffer cmdBuff = new StringBuffer(256);
    StringBuffer resBuff = new StringBuffer(256);
    /** ユーザーが入力したユーザー名 */
    private String user = null;
    /** ユーザーが入力したパスワード */
    private String pass = null;

    /** ソケットチャンネル */
    private SocketChannel socketChannel;

    /**
     * 
     * @param socketChannel
     */
    public CmdSocketHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void exit() throws IOException {
        if (socketChannel != null) {
            socketChannel.finishConnect();
            socketChannel.close();
        }
    }

    /**
     * passを取得します。
     * 
     * @return pass
     */
    public String getPass() {
        return pass;
    }

    /**
     * passを設定します。
     * 
     * @param pass
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * userを取得します。
     * 
     * @return user
     */
    public String getUser() {
        return user;
    }

    /**
     * userを設定します。
     * 
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /** ユーザー名を入力しているかどうかのフラグ */
    private boolean bUser = false;

    /**
     * bUserを取得します。
     * 
     * @return bUser
     */
    public boolean isbUser() {
        return bUser;
    }

    /**
     * bUserを設定します。
     * 
     * @param bUser
     */
    public void setbUser(boolean bUser) {
        this.bUser = bUser;
    }

    /** パスワードを入力して認証が通ったかのフラグ */
    private boolean bPass = false;

    /**
     * bPassを取得します。
     * 
     * @return bPass
     */
    public boolean isbPass() {
        return bPass;
    }

    /**
     * bPassを設定します。
     * 
     * @param bPass
     */
    public void setbPass(boolean bPass) {
        this.bPass = bPass;
    }

    /** ユーザーのカレントポート */
    private String currentPort = null;

    /**
     * currentPortを取得します。
     * 
     * @return currentPort
     */
    public String getCurrentPort() {
        return currentPort;
    }

    /**
     * currentPortを設定します。
     * 
     * @param currentPort
     */
    public void setCurrentPort(String currentPort) {
        this.currentPort = currentPort;
        this.currentPortBuffer = ByteBuffer.wrap(currentPort.getBytes());
    }

    /** バッファー情報のカレントポート */
    private ByteBuffer currentPortBuffer = null;

    /*
     * (non-Javadoc)
     * 
     * @see com.uchicom.plate.Handler#handle(java.nio.channels.SelectionKey)
     */
    @Override
    public void handle(SelectionKey key) throws IOException {

        System.out.println("ループ");
        if (!key.isValid()) {
            System.out.println("生きてる？");
            socketChannel.finishConnect();
        }
        
        
        if (key.isReadable()) {
            System.out.println("読める？");
            try {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                // \r\nまで読み込み続ける
                if (checkCmd(socketChannel, key)) {
                    // コマンド解析可能
                    // コマンド実行
                    // 読み込むデータがなくても監視は続ける。
                    // cmdチェック
                    String cmdLine = cmdBuff.toString();
                    cmdBuff.setLength(0);
                    // 改行のみは次行へ
                    if (cmdLine.length() > 0) {
                        Map<String, AbstractCmd> cmdMap = Commander.getInstance()
                                                                   .getCmdMap();
                        String[] cmdParams = cmdLine.split(Commander.CMD_SPRIT_CHAR);
                        if (cmdParams.length > 0) {
                            String cmdString = cmdParams[0];
                            if (cmdMap.containsKey(cmdString)) {
                                AbstractCmd cmd = cmdMap.get(cmdString);
                                String[] params = Arrays.copyOfRange(cmdParams,
                                                                     1,
                                                                     cmdParams.length);
                                // 認証チェックしてエラーの場合はNG
                                if (cmd.checkAuth(this)) {
                                    // パラメータチェックしてエラーだったら
                                    if (cmd.checkParam(this, params)) {
                                        // 
                                        if (cmd.checkConfirm()) {
                                            resBuff.append("execute ok ? yes/[no]\r\n");
                                        }
                                        if (cmd.execute(this, params)) {
                                            resBuff.append(cmd.getOkMessage());
                                        } else {
                                            resBuff.append(cmd.getNgMessage());
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
                    System.out.println("ここは？");
                    // コマンドラインの読み込み完了時に書き込み開始
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                key.cancel();
            }
        }

        // 書き込みOKの場合は書き込む
        if (key.isValid() && key.isWritable()) {
            System.out.println("ここにきてる？");
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
    private void writeCmdLine(StringBuffer cmdBuff) throws IOException {
        cmdBuff.append("plate");
        if (currentPort != null) {
            cmdBuff.append(":");
            cmdBuff.append(currentPort);
        }
        cmdBuff.append(">");
    }
    /**
     * 
     * @param writer
     * @throws IOException
     */
    private void writeCmdLine(SocketChannel socketChannel) throws IOException {
        socketChannel.write(CMD_SET.asReadOnlyBuffer());
        if (currentPort != null) {
            socketChannel.write(CMD_PROMPT_SEPARATOR.asReadOnlyBuffer());
            socketChannel.write(currentPortBuffer.asReadOnlyBuffer());
        }
        socketChannel.write(CMD_PROMPT.asReadOnlyBuffer());
//        if (cmdBuff.length() > 0) {
//            socketChannel.write(ByteBuffer.wrap((cmdBuff.toString()).getBytes()));
//        }
    }
    private void writeNextLine() {
        
    }
    /**
     * 
     * @param writer
     * @throws IOException
     */
    private void eraseChar(SocketChannel socketChannel) throws IOException {
        byte[] bytes = null;
        if (currentPort == null) {
            bytes = new byte[1024];
        } else {
            bytes = new byte[1024];
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.put(CMD_SET.asReadOnlyBuffer());
        if (currentPort != null) {
            byteBuffer.put(CMD_PROMPT_SEPARATOR.asReadOnlyBuffer());
            byteBuffer.put(currentPortBuffer.asReadOnlyBuffer());
        }
        byteBuffer.put(CMD_PROMPT.asReadOnlyBuffer());
        if (cmdBuff.length() > 0) {
            byteBuffer.put(ByteBuffer.wrap((cmdBuff.toString() + " ").getBytes()).asReadOnlyBuffer());
        }
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }

    // 実行クラスとパラメータ引数の文字列長を抽出する
    boolean escape = false;

    private boolean checkCmd(SocketChannel socketChannel, SelectionKey key)
            throws IOException {
        boolean lineEnd = false;
        int length = socketChannel.read(cmd);
        if (length == -1) {
            throw new IOException("クライアントが切断しました.");
        }
        cmd.flip();
        // 後ろからチェックして\r\nがあればコマンド解析開始
        while (cmd.position() < cmd.limit()) {
            int ch = (int) cmd.get();
            System.out.print((char)ch);
            System.out.println(Integer.toHexString((char)ch));
            if (Constants.DEBUG) System.out.println("[" + (char) ch + "]");
            if (ch == 0x08) {
                // BSなので一文字消す。
                if (cmdBuff.length() > 0) {

                    if (!cmdBuff.toString().matches("^ *c?pass +[^ ]+$")) {
                        resBuff.append((char)ch);
                        resBuff.append(" ");
                        resBuff.append((char)ch);
                    }
                    cmdBuff.setLength(cmdBuff.length() - 1);
                }
                System.out.println("BSですよ");
            } else if (ch < 0x0 || ch == '\n' || ch == '\r') {
                System.out.println("\\r\\nですよ");
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
                        resBuff.append((char)ch);
                    }
                }
            } else if (0x7F == ch) {
                System.out.println("7Fですよ");
                //なにもしていない
            } else {

                System.out.println("その他ですよ");
            }
        }
        cmd.clear();
        return lineEnd;
    }

    /**
     * データを書き込んで0に設定
     * 
     * @param socketChannel
     * @throws IOException
     */
    private void writeMessage(SocketChannel socketChannel) throws IOException {
        socketChannel.write(ByteBuffer.wrap(resBuff.toString().getBytes()));
        resBuff.setLength(0);
    }

}
