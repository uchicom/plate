// (C) 2026 uchicom
package com.uchicom.plate.cmd.key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.uchicom.plate.KeyInfo;
import com.uchicom.plate.Porter;
import com.uchicom.plate.cmd.AbstractCmdTest;
import com.uchicom.plate.exception.CmdException;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatusCmdTest extends AbstractCmdTest {

  private KeyInfo key1;
  private KeyInfo key2;

  @BeforeEach
  public void setUpPorts() {
    key1 = mock(KeyInfo.class);
    when(key1.getKey()).thenReturn("k1");
    when(key1.getStatus()).thenReturn("line1\r\nline2");
    key2 = mock(KeyInfo.class);
    when(key2.getKey()).thenReturn("k2");
    when(key2.getStatus()).thenReturn("not running");
    var porter1 = mock(Porter.class);
    when(porter1.getList()).thenReturn(List.of(key1));
    var porter2 = mock(Porter.class);
    when(porter2.getList()).thenReturn(List.of(key2));
    var portMap = new LinkedHashMap<String, Porter>();
    portMap.put(PORT, porter1);
    portMap.put("9090", porter2);
    when(main.getPortMap()).thenReturn(portMap);
  }

  @Test
  public void checkParam() {
    var cmd = new StatusCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "x"})).isFalse();
  }

  @Test
  public void execute_noPortSelected_showsAllPorts() throws Exception {
    var actual = new StatusCmd(broker).execute(handler, new String[] {});
    assertThat(actual)
        .contains("8080 k1 ")
        .contains("  status: line1\r\n    line2\r\n")
        .contains("9090 k2 ")
        .contains("  status: not running\r\n");
  }

  @Test
  public void execute_portSelected_showsOnlyThatPort() throws Exception {
    usePort();
    var actual = new StatusCmd(broker).execute(handler, new String[] {});
    assertThat(actual).contains("k1").doesNotContain("k2");
  }

  @Test
  public void execute_keySpecified() throws Exception {
    usePort();
    var actual = new StatusCmd(broker).execute(handler, new String[] {"k1"});
    assertThat(actual).contains("k1").doesNotContain("k2");
  }

  @Test
  public void execute_keyWithoutPort_searchesAllPorts() throws Exception {
    var actual = new StatusCmd(broker).execute(handler, new String[] {"k2"});
    assertThat(actual).contains("9090 k2").doesNotContain("k1");
  }

  @Test
  public void execute_unknownKey() {
    usePort();
    assertThatThrownBy(() -> new StatusCmd(broker).execute(handler, new String[] {"k2"}))
        .isInstanceOf(CmdException.class);
  }
}
