// (C) 2026 uchicom
package com.uchicom.plate.cmd.key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.uchicom.plate.CpInfo;
import com.uchicom.plate.cmd.AbstractCmdTest;
import org.junit.jupiter.api.Test;

public class KeyCmdTest extends AbstractCmdTest {

  @Test
  public void add_checkParam() {
    var cmd = new AddCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k", "c"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "c"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "c", "m"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "c", "m", "x"})).isFalse();
  }

  @Test
  public void add_execute() {
    usePort();
    var cmd = new AddCmd(broker);
    assertThat(cmd.execute(handler, new String[] {"k", "c"})).isNull();
    verify(main).addKey("k", "c", PORT);
    cmd.execute(handler, new String[] {"k", "c", "m"});
    verify(main).addKey("k", "c", "m", PORT);
  }

  @Test
  public void addcp_checkParam() {
    var cmd = new AddCpCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k", "cp"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k", "cp"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "cp", "x"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "http", "host", "path"})).isTrue();
  }

  @Test
  public void addcp_execute() throws Exception {
    usePort();
    new AddCpCmd(broker).execute(handler, new String[] {"k", "file:/tmp/test.jar"});
    verify(main).addCp(eq("k"), any(CpInfo.class), eq(PORT));
  }

  @Test
  public void remove_checkParamAndExecute() throws Exception {
    var cmd = new RemoveCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "x"})).isFalse();
    cmd.execute(handler, new String[] {"k"});
    verify(main).removeKey("k", PORT);
  }

  @Test
  public void removecp_checkParam() {
    var cmd = new RemoveCpCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k", "cp"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k", "cp"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "cp", "9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "cp", "9/90"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
  }

  @Test
  public void removecp_execute() throws Exception {
    usePort();
    var cmd = new RemoveCpCmd(broker);
    cmd.execute(handler, new String[] {"k", "cp"});
    verify(main).removeCp("k", "cp", PORT);
    cmd.execute(handler, new String[] {"k", "cp", "9090"});
    verify(main).removeCp("k", "cp", "9090");
  }

  @Test
  public void edit_checkParam() {
    var cmd = new EditCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "c", "m"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "c", "m", "x"})).isFalse();
  }

  @Test
  public void edit_execute() throws Exception {
    usePort();
    var cmd = new EditCmd(broker);
    cmd.execute(handler, new String[] {"k"});
    verify(main).editKey("k", null, null, PORT);
    cmd.execute(handler, new String[] {"k", "c"});
    verify(main).editKey("k", "c", null, PORT);
    cmd.execute(handler, new String[] {"k", "c", "m"});
    verify(main).editKey("k", "c", "m", PORT);
  }

  @Test
  public void rename_checkParam() {
    var cmd = new RenameCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"a", "b"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"a", "b"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"a", "b", "9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"a", "b", "9-90"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"a"})).isFalse();
  }

  @Test
  public void rename_execute() throws Exception {
    usePort();
    var cmd = new RenameCmd(broker);
    cmd.execute(handler, new String[] {"a", "b"});
    verify(main).renameKey("a", "b", PORT);
    cmd.execute(handler, new String[] {"a", "b", "9090"});
    verify(main).renameKey("a", "b", "9090");
  }

  @Test
  public void enable_checkParamAndExecute() throws Exception {
    var cmd = new EnableCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "9.090"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    cmd.execute(handler, new String[] {"k"});
    verify(main).enableKey("k", PORT);
    cmd.execute(handler, new String[] {"k", "9090"});
    verify(main).enableKey("k", "9090");
  }

  @Test
  public void disable_checkParamAndExecute() throws Exception {
    var cmd = new DisableCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "9090"})).isTrue();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    cmd.execute(handler, new String[] {"k"});
    verify(main).disableKey("k", PORT);
    cmd.execute(handler, new String[] {"k", "9090"});
    verify(main).disableKey("k", "9090");
  }

  @Test
  public void auto_checkParamAndExecute() throws Exception {
    var cmd = new AutoCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "9090"})).isTrue();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    cmd.execute(handler, new String[] {"k"});
    verify(main).autoKey("k", PORT);
    cmd.execute(handler, new String[] {"k", "9090"});
    verify(main).autoKey("k", "9090");
  }

  @Test
  public void manual_checkParamAndExecute() throws Exception {
    var cmd = new ManualCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "localhost:9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "a b"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    cmd.execute(handler, new String[] {"k"});
    verify(main).manualKey("k", PORT);
    cmd.execute(handler, new String[] {"k", "9090"});
    verify(main).manualKey("k", "9090");
  }

  @Test
  public void call_checkParam() {
    var cmd = new CallCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "p1", "p2"})).isTrue();
  }

  @Test
  public void call_execute_passesRemainingParams() throws Exception {
    usePort();
    new CallCmd(broker).execute(handler, new String[] {"k", "p1", "p2"});
    verify(main).callKey(PORT, "k", new String[] {"p1", "p2"});
  }

  @Test
  public void stop_checkParam() {
    var cmd = new StopCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"k", "9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"k", "9-090"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {"k"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
  }

  @Test
  public void stop_execute_passesRemainingParams() throws Exception {
    usePort();
    new StopCmd(broker).execute(handler, new String[] {"k", "p1"});
    verify(main).shutdownKey(PORT, "k", new String[] {"p1"});
  }
}
