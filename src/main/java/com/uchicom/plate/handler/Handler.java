// (C) 2012 uchicom
package com.uchicom.plate.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * @author Uchiyama Shigeki
 */
public interface Handler {

  /**
   * @param key
   * @throws IOException
   */
  public void handle(SelectionKey key) throws IOException;
}
