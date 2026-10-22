// (C) 2026 uchicom
package com.uchicom.plate.cmd;

import static org.mockito.Mockito.when;

import com.uchicom.plate.AbstractTest;
import com.uchicom.plate.Commander;
import com.uchicom.plate.Main;
import com.uchicom.plate.handler.CmdSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

/** コマンドのテスト用の共通クラス。コマンダー、メイン、ハンドラーをモックにする。 */
public abstract class AbstractCmdTest extends AbstractTest {

  protected static final String PORT = "8080";

  @Mock protected Commander broker;

  @Mock protected Main main;

  @Mock protected CmdSocketHandler handler;

  @BeforeEach
  public void setUpBroker() {
    when(broker.getMain()).thenReturn(main);
  }

  /** ポート選択済みの状態にする。 */
  protected void usePort() {
    when(handler.getCurrentPort()).thenReturn(PORT);
  }
}
