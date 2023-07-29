// (C) 2023 uchicom
package com.uchicom.plate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class AbstractTest {

  AutoCloseable closeable;

  @BeforeEach
  public void openMocks() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void releaseMocks() throws Exception {
    closeable.close();
  }
}
