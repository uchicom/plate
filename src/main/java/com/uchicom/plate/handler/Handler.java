// (C) 2012 uchicom
package com.uchicom.plate.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * ハンドラインターフェース.
 *
 * @author Uchiyama Shigeki
 */
public interface Handler {

  public void handle(SelectionKey key) throws IOException;
}
