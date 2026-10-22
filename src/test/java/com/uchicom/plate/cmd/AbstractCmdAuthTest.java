// (C) 2026 uchicom
package com.uchicom.plate.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uchicom.plate.cmd.key.AddCmd;
import com.uchicom.plate.util.Crypt;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class AbstractCmdAuthTest extends AbstractCmdTest {

  @Test
  public void checkAuth_noMainUser_requiresUserAndPass() {
    var cmd = new AddCmd(broker);
    when(main.getUser()).thenReturn(null);

    when(handler.getUser()).thenReturn(null);
    assertThat(cmd.checkAuth(handler)).isFalse();

    when(handler.getUser()).thenReturn("user");
    when(handler.getPass()).thenReturn(null);
    assertThat(cmd.checkAuth(handler)).isFalse();

    when(handler.getPass()).thenReturn("pass");
    assertThat(cmd.checkAuth(handler)).isTrue();
  }

  @Test
  public void checkAuth_withMainUser_matchesCryptPass() {
    var cmd = new AddCmd(broker);
    when(main.getUser()).thenReturn("user");
    when(main.getCryptPass())
        .thenReturn(Base64.getEncoder().encodeToString(Crypt.encrypt3("user", "pass")));
    when(handler.getUser()).thenReturn("user");

    when(handler.getPass()).thenReturn("pass");
    assertThat(cmd.checkAuth(handler)).isTrue();

    when(handler.getPass()).thenReturn("wrong");
    assertThat(cmd.checkAuth(handler)).isFalse();

    when(handler.getPass()).thenReturn(null);
    assertThat(cmd.checkAuth(handler)).isFalse();

    when(handler.getUser()).thenReturn("other");
    when(handler.getPass()).thenReturn("pass");
    assertThat(cmd.checkAuth(handler)).isFalse();
  }
}
