/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.uchicom.plate.Main;
import com.uchicom.plate.Porter;

/**
 * @author Uchiyama Shigeki
 *
 */
public class PortServerHandler implements Handler {

    private ConnectPortHandler connectPortHandler;
    public PortServerHandler(Main plate, Porter porter) {
        this.connectPortHandler = new ConnectPortHandler(plate, porter);
    }
    /* (non-Javadoc)
     * @see com.uchicom.plate.Handler#handle(java.nio.channels.SelectionKey)
     */
    @Override
    public void handle(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            //サーバーの受付処理。
            SocketChannel socketChannel = ((ServerSocketChannel)key.channel()).accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(key.selector(), SelectionKey.OP_READ, connectPortHandler);
        }
    }

}
