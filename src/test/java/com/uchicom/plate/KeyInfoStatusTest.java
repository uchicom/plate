// (C) 2026 uchicom
package com.uchicom.plate;

import static org.assertj.core.api.Assertions.assertThat;

import com.uchicom.plate.Starter.StarterKind;
import org.junit.jupiter.api.Test;

public class KeyInfoStatusTest extends AbstractTest {

  public static class StatusApp {
    public static String status() {
      return "a\nb\r\nc";
    }

    public static String nullStatus() {
      return null;
    }

    public static String failStatus() {
      throw new IllegalStateException("boom");
    }

    public static String longStatus() {
      return "x".repeat(KeyInfo.MAX_STATUS_LENGTH + 10);
    }

    public String instanceStatus() {
      return "x";
    }

    public static String contextStatus() {
      return String.valueOf(Thread.currentThread().getContextClassLoader());
    }
  }

  private KeyInfo running(String statusMethod) {
    var porter = new Porter(null);
    var keyInfo = new KeyInfo("k", StatusApp.class.getName(), "main", null);
    keyInfo.setPorter(porter);
    keyInfo.statusMethodName = statusMethod;
    keyInfo.create(null, StarterKind.SERVICE).setStarted(true);
    return keyInfo;
  }

  @Test
  public void getStatus_multiLineIsNormalized() {
    assertThat(running("status").getStatus()).isEqualTo("a\r\nb\r\nc");
  }

  @Test
  public void getStatus_nullReturn() {
    assertThat(running("nullStatus").getStatus()).isEqualTo("null");
  }

  @Test
  public void getStatus_notConfigured() {
    assertThat(running(null).getStatus()).isEqualTo("(not supported)");
  }

  @Test
  public void getStatus_noMethod() {
    assertThat(running("unknown").getStatus()).isEqualTo("(not supported)");
  }

  @Test
  public void getStatus_notRunning() {
    var keyInfo = running("status");
    keyInfo.getStarterList().get(0).setStarted(false);
    assertThat(keyInfo.getStatus()).isEqualTo("not running");
  }

  @Test
  public void getStatus_exception() {
    assertThat(running("failStatus").getStatus())
        .isEqualTo("error: java.lang.IllegalStateException: boom");
  }

  @Test
  public void getStatus_instanceMethod() {
    assertThat(running("instanceStatus").getStatus()).startsWith("error: ");
  }

  @Test
  public void getStatus_truncated() {
    assertThat(running("longStatus").getStatus())
        .hasSize(KeyInfo.MAX_STATUS_LENGTH + "...(truncated)".length())
        .endsWith("...(truncated)");
  }

  @Test
  public void getStatus_restoresContextClassLoader() {
    var original = Thread.currentThread().getContextClassLoader();
    running("contextStatus").getStatus();
    assertThat(Thread.currentThread().getContextClassLoader()).isSameAs(original);
  }
}
