// (C) 2026 uchicom
package com.uchicom.plate.cmd.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uchicom.plate.CpInfo;
import com.uchicom.plate.Porter;
import com.uchicom.plate.cmd.AbstractCmdTest;
import com.uchicom.plate.exception.CmdException;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PortCmdTest extends AbstractCmdTest {

  @Test
  public void use_checkParam() {
    var cmd = new UseCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"8080"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"localhost:8080"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"80 80"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"a", "b"})).isFalse();
  }

  @Test
  public void use_execute_setsCurrentPort() throws Exception {
    when(main.exists("8080")).thenReturn(true);
    new UseCmd(broker).execute(handler, new String[] {"8080"});
    verify(handler).setCurrentPort("8080");
  }

  @Test
  public void use_execute_unknownPort() {
    when(main.exists("8080")).thenReturn(false);
    assertThatThrownBy(() -> new UseCmd(broker).execute(handler, new String[] {"8080"}))
        .isInstanceOf(CmdException.class);
  }

  @Test
  public void addportcp_checkParam() {
    var cmd = new AddPortCpCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"cp"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"cp", "9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"cp", "90 90"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"cp"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
  }

  @Test
  public void addportcp_execute() throws Exception {
    usePort();
    var porter = mock(Porter.class);
    when(main.getPortMap()).thenReturn(Map.of(PORT, porter));
    new AddPortCpCmd(broker).execute(handler, new String[] {"file:/tmp/test.jar"});
    verify(porter).addCp(any(CpInfo.class));
  }

  @Test
  public void addportcp_execute_unknownPort() {
    usePort();
    when(main.getPortMap()).thenReturn(Map.of());
    assertThatThrownBy(
            () -> new AddPortCpCmd(broker).execute(handler, new String[] {"file:/tmp/test.jar"}))
        .isInstanceOf(CmdException.class);
  }

  @Test
  public void removeportcp_checkParam() {
    var cmd = new RemovePortCpCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"cp"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"cp", "9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"cp", "90 90"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"cp"})).isTrue();
  }

  @Test
  public void removeportcp_execute() throws Exception {
    usePort();
    var cmd = new RemovePortCpCmd(broker);
    cmd.execute(handler, new String[] {"cp"});
    verify(main).removePortCp("cp", PORT);
    cmd.execute(handler, new String[] {"cp", "9090"});
    verify(main).removePortCp("cp", "9090");
  }
}
