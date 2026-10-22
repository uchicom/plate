// (C) 2026 uchicom
package com.uchicom.plate.cmd.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uchicom.plate.Porter;
import com.uchicom.plate.cmd.AbstractCmd;
import com.uchicom.plate.cmd.AbstractCmdTest;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.util.Crypt;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class UtilCmdTest extends AbstractCmdTest {

  @Test
  public void user_isAlwaysAuthorizedAndSetsUser() {
    var cmd = new UserCmd(broker);
    assertThat(cmd.checkAuth(handler)).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"u"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"u", "v"})).isFalse();
    cmd.execute(handler, new String[] {"u"});
    verify(handler).setUser("u");
  }

  @Test
  public void pass_isAlwaysAuthorizedAndSetsPass() {
    var cmd = new PassCmd(broker);
    assertThat(cmd.checkAuth(handler)).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"p"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    cmd.execute(handler, new String[] {"p"});
    verify(handler).setPass("p");
  }

  @Test
  public void cuser_updatesMainUserAndCryptPass() {
    when(handler.getPass()).thenReturn("pass");
    var cmd = new CUserCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"u2"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    cmd.execute(handler, new String[] {"u2"});
    verify(main).setUser("u2");
    verify(main).setCryptPass(Base64.getEncoder().encodeToString(Crypt.encrypt3("u2", "pass")));
    verify(handler).setUser("u2");
  }

  @Test
  public void cpass_updatesCryptPass() {
    when(main.getUser()).thenReturn("user");
    var cmd = new CPassCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"p2"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"a", "b"})).isFalse();
    cmd.execute(handler, new String[] {"p2"});
    verify(main).setCryptPass(Base64.getEncoder().encodeToString(Crypt.encrypt3("user", "p2")));
    verify(handler).setPass("p2");
  }

  @Test
  public void exit_isAlwaysAuthorizedAndExitsHandler() throws Exception {
    var cmd = new ExitCmd(broker);
    assertThat(cmd.checkAuth(handler)).isTrue();
    cmd.execute(handler, new String[] {});
    verify(handler).exit();
  }

  @Test
  public void exit_wrapsIOException() throws Exception {
    doThrow(new IOException("closed")).when(handler).exit();
    assertThatThrownBy(() -> new ExitCmd(broker).execute(handler, new String[] {}))
        .isInstanceOf(CmdException.class)
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  public void shutdown_exitsMain() {
    new ShutdownCmd(broker).execute(handler, new String[] {});
    verify(main).exit();
  }

  @Test
  public void purge_purgesMain() {
    new PurgeCmd(broker).execute(handler, new String[] {});
    verify(main).purge();
  }

  @Test
  public void save_checkParam() {
    var cmd = new SaveCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"f.sv"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    when(main.getLoadFile()).thenReturn(new File("f.sv"));
    assertThat(cmd.checkParam(handler, new String[] {})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"a", "b"})).isFalse();
  }

  @Test
  public void save_execute() throws Exception {
    when(handler.getUser()).thenReturn("u");
    when(handler.getPass()).thenReturn("p");
    var cmd = new SaveCmd(broker);
    cmd.execute(handler, new String[] {"f.sv"});
    verify(main).save(new File("f.sv"), "u", "p");
    cmd.execute(handler, new String[] {});
    verify(main).save("u", "p");
  }

  @Test
  public void load_checkParamAndExecute() throws Exception {
    when(handler.getUser()).thenReturn("u");
    when(handler.getPass()).thenReturn("p");
    var cmd = new LoadCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"f.sv"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    cmd.execute(handler, new String[] {"f.sv"});
    verify(main).load("f.sv", "u", "p");
  }

  @Test
  public void dset_checkParamAndExecute() {
    var cmd = new DSetCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {"name", "value"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"na.me", "value"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"name", "va lue"})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"name"})).isFalse();
    cmd.execute(handler, new String[] {"plateTestProp", "abc"});
    try {
      assertThat(System.getProperty("plateTestProp")).isEqualTo("abc");
    } finally {
      System.clearProperty("plateTestProp");
    }
  }

  @Test
  public void build_checkParam() {
    var cmd = new BuildCmd(broker);
    assertThat(cmd.checkParam(handler, new String[] {})).isFalse();
    assertThat(cmd.checkParam(handler, new String[] {"9090"})).isTrue();
    assertThat(cmd.checkParam(handler, new String[] {"90.90"})).isFalse();
    usePort();
    assertThat(cmd.checkParam(handler, new String[] {})).isTrue();
  }

  @Test
  public void build_execute() throws Exception {
    usePort();
    var porter = mock(Porter.class);
    when(main.getPortMap()).thenReturn(Map.of(PORT, porter));
    var cmd = new BuildCmd(broker);
    cmd.execute(handler, new String[] {});
    cmd.execute(handler, new String[] {PORT});
    verify(porter, org.mockito.Mockito.times(2)).build();
  }

  @Test
  public void build_execute_unknownPort() {
    when(main.getPortMap()).thenReturn(Map.of());
    assertThatThrownBy(() -> new BuildCmd(broker).execute(handler, new String[] {"9090"}))
        .isInstanceOf(CmdException.class);
  }

  @Test
  public void list_empty() {
    when(main.getPortMap()).thenReturn(Map.of());
    var actual = new ListCmd(broker).execute(handler, new String[] {});
    assertThat(actual).isEqualTo("OK\r\n---plate Infomation---\r\nempty.\r\n");
  }

  @Test
  public void list_withPort() {
    var porter = mock(Porter.class);
    when(porter.getCpList()).thenReturn(List.of());
    when(porter.getList()).thenReturn(List.of());
    when(main.getPortMap()).thenReturn(Map.of(PORT, porter));
    var actual = new ListCmd(broker).execute(handler, new String[] {});
    assertThat(actual).startsWith("OK\r\n---plate Infomation---\r\n").contains(PORT);
  }

  @Test
  public void help_listsAllCommandHelp() {
    AbstractCmd a = new ExitCmd(broker);
    AbstractCmd b = new PurgeCmd(broker);
    when(broker.getCmdList()).thenReturn(List.of(a, b));
    var cmd = new HelpCmd(broker);
    assertThat(cmd.checkAuth(handler)).isTrue();
    var actual = cmd.execute(handler, new String[] {});
    assertThat(actual).startsWith("Command List\r\n").contains(a.getHelp()).contains(b.getHelp());
  }
}
