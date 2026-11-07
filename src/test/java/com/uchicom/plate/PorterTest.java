// (C) 2026 uchicom
package com.uchicom.plate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URLClassLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PorterTest {

  @Test
  public void build_closesOldClassLoader(@TempDir java.nio.file.Path tempDir) throws Exception {
    var porter = new Porter(mock(Main.class));
    porter.addCp(new CpInfo(tempDir.resolve("a.jar").toUri().toString()));
    var oldLoader = mock(URLClassLoader.class);
    porter.classLoader = oldLoader;

    // test
    porter.build();

    // assert: 旧ローダーがcloseされ、新しいローダーに差し替わる
    verify(oldLoader).close();
    assertThat(porter.getClassLoader()).isNotNull().isNotSameAs(oldLoader);
  }

  @Test
  public void closeClassLoader_noLoader() {
    var porter = new Porter(mock(Main.class));

    // test
    porter.closeClassLoader();

    // assert: ローダー未生成でも例外にならない
    assertThat(porter.getClassLoader()).isNull();
  }
}
