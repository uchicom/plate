/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.uchicom.plate.util.Telnet;

/**
 * @author Uchiyama Shigeki
 * 
 */
public class ConnectSocketHandler implements Handler {
    private static final ByteBuffer lineOff = ByteBuffer.wrap(new byte[] { Telnet.IAC, Telnet.WILL, Telnet.TELNET_LINE_MODE});
    private static final ByteBuffer suppress = ByteBuffer.wrap(new byte[] { Telnet.IAC, Telnet.WILL, Telnet.SUPPRESS_GO_AHEAD});
    
    private static final ByteBuffer echoOff = ByteBuffer.wrap(new byte[] { Telnet.IAC, Telnet.WILL, Telnet.ECHO});
    private static final ByteBuffer dontEcho = ByteBuffer.wrap(new byte[] { Telnet.IAC, Telnet.WONT, Telnet.ECHO});
    private static final ByteBuffer buffer = ByteBuffer
			.wrap(new byte[] { 
			        'W', 'e', 'l', 'c', 'o', 'm', ' ', 't', 'o',
					' ', 'p', 'l', 'a', 't', 'e', ' ', 'C', 'o', 'n', 's',
					'o', 'l', 'e', '.', '\r', '\n', 'p', 'l', 'a', 't', 'e', '>' });
	
	private int status = 0;
	private final ByteBuffer cmd = ByteBuffer.allocate(256);

    boolean off = false;
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uchicom.plate.Handler#handle(java.nio.channels.SelectionKey)
	 */
	@Override
	public void handle(SelectionKey key) throws IOException {

	    
		if (key.isWritable()) {
			//サーバーのあくせぷと実施(サーバは一個だからいいけど。
			SocketChannel socketChannel = (SocketChannel)key.channel();
			switch (status) {
			case 0:
            socketChannel.write(echoOff.asReadOnlyBuffer());
            key.interestOps(SelectionKey.OP_READ);
            System.out.println("ECHOOFF1");
            status = 1;
            break;
			case 2:
            socketChannel.write(suppress.asReadOnlyBuffer());
            key.interestOps(SelectionKey.OP_READ);
            System.out.println("SUPPRESS");
            status = 3;
            break;
			case 4:
            socketChannel.write(lineOff.asReadOnlyBuffer());
            System.out.println("LINEOFF");
            key.interestOps(SelectionKey.OP_READ);
            status = 5;
            break;
			case 6:
            socketChannel.write(buffer.asReadOnlyBuffer());
            socketChannel.register(key.selector(), SelectionKey.OP_READ, new CmdSocketHandler(socketChannel));
            System.out.println("呼び出し");
            status = 7;
            break;
            default :
                //何も無し
			}
		}
		
		if (key.isReadable()) {
		    ((SocketChannel)key.channel()).read(cmd);
	        
	        for (int i= 0; i < cmd.position(); i++) {
	            byte ch = (byte)cmd.get(i++);
	            if (Telnet.IAC == ch) {
	                status++;

	                switch (cmd.get(i++)) {
	                case Telnet.DONT:
	                    System.out.print("DONT");
                        break;
	                case Telnet.WILL:
                        System.out.print("WILL");
                        break;
	                case Telnet.DO:
                        System.out.print("DO");
                        break;
	                case Telnet.WONT:
                        System.out.print("WONT");
	                    break;
	                    default:
	                    System.out.println("DO,DONT,WILL,WONTでない");
	                }

                    switch (cmd.get(i++)) {
                    case Telnet.ECHO:
                        off = true;
                        System.out.println("ECHO");
                        break;
                    case Telnet.TELNET_LINE_MODE:
                        System.out.println("LINE");
                        break;
                    case Telnet.SUPPRESS_GO_AHEAD:
                        System.out.println("SUPPRESS_GO_AHEAD");
                        break;
                        default:
                        System.out.println("その他");
                    }
	            } else {
	                System.out.println(Integer.toHexString(0xFF & ch));
	            }
              key.interestOps(SelectionKey.OP_WRITE);
	        }
	        cmd.clear();
		}
	}
}
