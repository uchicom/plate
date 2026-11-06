// (C) 2026 uchicom
package com.uchicom.plate.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uchicom.plate.AbstractTest;
import com.uchicom.plate.Commander;
import com.uchicom.plate.Main;
import com.uchicom.plate.cmd.AbstractCmd;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class CmdSocketHandlerTest extends AbstractTest {

  private MockedStatic<Commander> commanderStatic;
  private AbstractCmd fooCmd;
  private AbstractCmd barCmd;
  private SocketChannel channel;
  private SelectionKey key;
  private ByteArrayOutputStream written;
  private int maxWrite = Integer.MAX_VALUE;
  private CmdSocketHandler handler;

  @BeforeEach
  void setUp() throws Exception {
    fooCmd = mock(AbstractCmd.class);
    barCmd = mock(AbstractCmd.class);
    for (AbstractCmd cmd : new AbstractCmd[] {fooCmd, barCmd}) {
      when(cmd.checkAuth(any())).thenReturn(true);
      when(cmd.checkParam(any(), any())).thenReturn(true);
      when(cmd.execute(any(), any())).thenReturn("");
    }
    Map<String, AbstractCmd> cmdMap = new HashMap<>();
    cmdMap.put("foo", fooCmd);
    cmdMap.put("bar", barCmd);
    Commander commander = mock(Commander.class);
    when(commander.getCmdMap()).thenReturn(cmdMap);
    commanderStatic = mockStatic(Commander.class);
    commanderStatic.when(Commander::getInstance).thenReturn(commander);

    written = new ByteArrayOutputStream();
    channel = mock(SocketChannel.class);
    when(channel.write(any(ByteBuffer.class)))
        .thenAnswer(
            invocation -> {
              ByteBuffer buffer = invocation.getArgument(0);
              int length = Math.min(buffer.remaining(), maxWrite);
              byte[] bytes = new byte[length];
              buffer.get(bytes);
              written.write(bytes);
              return length;
            });
    key = mock(SelectionKey.class);
    when(key.isValid()).thenReturn(true);
    when(key.isReadable()).thenReturn(true);
    when(key.isWritable()).thenReturn(true);
    when(key.channel()).thenReturn(channel);
    handler = new CmdSocketHandler(channel, mock(Main.class));
  }

  @AfterEach
  void tearDown() {
    commanderStatic.close();
  }

  private void receive(int... bytes) throws IOException {
    when(channel.read(any(ByteBuffer.class)))
        .thenAnswer(
            invocation -> {
              ByteBuffer buffer = invocation.getArgument(0);
              for (int b : bytes) {
                buffer.put((byte) b);
              }
              return bytes.length;
            });
    handler.handle(key);
  }

  private void receive(String text) throws IOException {
    int[] bytes = new int[text.length()];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = text.charAt(i);
    }
    receive(bytes);
  }

  private String output() {
    return written.toString(StandardCharsets.UTF_8);
  }

  @Test
  void iacNegotiationDoesNotExecuteCommand() throws Exception {
    // fo + IAC WILL ECHO + o + CRLF
    receive('f', 'o', 0xFF, 0xFB, 0x01, 'o', '\r', '\n');
    // IACで行が分断されず、"foo"として1回だけ実行される
    verify(fooCmd, times(1)).execute(any(), eq(new String[0]));
    assertThat(output()).doesNotContain("Command not found");
  }

  @Test
  void iacSubnegotiationIsSkipped() throws Exception {
    // IAC SB 24 1 IAC SE
    receive(0xFF, 0xFA, 0x18, 0x01, 0xFF, 0xF0, 'f', 'o', 'o', '\r', '\n');
    verify(fooCmd).execute(any(), eq(new String[0]));
  }

  @Test
  void iacSequenceSplitAcrossReads() throws Exception {
    receive('f', 'o', 0xFF);
    receive(0xFD);
    receive(0x01, 'o', '\r', '\n');
    verify(fooCmd).execute(any(), eq(new String[0]));
  }

  @Test
  void nonAsciiInputDoesNotTriggerNewline() throws Exception {
    // "fo" + あ(UTF-8) + "o" + CRLF
    receive('f', 'o', 0xE3, 0x81, 0x82, 'o', '\r', '\n');
    verify(fooCmd).execute(any(), eq(new String[0]));
  }

  @Test
  void multipleLinesInOneReadAreAllExecuted() throws Exception {
    receive("foo\r\nbar\r\n");
    verify(fooCmd, times(1)).execute(any(), any());
    verify(barCmd, times(1)).execute(any(), any());
  }

  @Test
  void literalBracketDoesNotSwallowNextChar() throws Exception {
    receive("foo [a\r\n");
    verify(fooCmd).execute(any(), eq(new String[] {"[a"}));
  }

  @Test
  void arrowKeySequenceIsSkipped() throws Exception {
    receive(0x1B, '[', 'A', 'f', 'o', 'o', '\r', '\n');
    verify(fooCmd).execute(any(), eq(new String[0]));
  }

  @Test
  void errorMessageIsWrittenAsUtf8() throws Exception {
    when(fooCmd.execute(any(), any())).thenThrow(new RuntimeException("エラー"));
    receive("foo\r\n");
    assertThat(output()).contains("NG: エラー");
  }

  @Test
  void partialWriteIsRepeatedUntilComplete() throws Exception {
    maxWrite = 3;
    when(fooCmd.execute(any(), any())).thenReturn("0123456789\r\n");
    receive("foo\r\n");
    assertThat(output()).contains("0123456789\r\nOK\r\nplate>");
    verify(barCmd, never()).execute(any(), any());
  }
}
