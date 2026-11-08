// (C) 2026 uchicom
package com.uchicom.plate.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.junit.jupiter.api.Test;

public class PriorityClassLoaderTest {

  @Test
  public void loadClass_javaClassIsLoadedByParent() throws Exception {
    try (var loader = new PriorityClassLoader(new URL[0])) {
      // test
      var clazz = loader.loadClass("java.lang.String");

      // assert
      assertThat(clazz).isSameAs(String.class);
    }
  }

  @Test
  public void loadClass_selfIsPreferredOverParent() throws Exception {
    var location =
        PriorityClassLoaderTest.class.getProtectionDomain().getCodeSource().getLocation();
    try (var loader = new PriorityClassLoader(new URL[] {location}, getClass().getClassLoader())) {
      // test
      var clazz = loader.loadClass(PriorityClassLoaderTest.class.getName());

      // assert
      assertThat(clazz).isNotSameAs(PriorityClassLoaderTest.class);
      assertThat(clazz.getClassLoader()).isSameAs(loader);
    }
  }

  @Test
  public void loadClass_fallsBackToParent() throws Exception {
    try (var loader = new PriorityClassLoader(new URL[0], getClass().getClassLoader())) {
      // test
      var clazz = loader.loadClass(PriorityClassLoaderTest.class.getName());

      // assert
      assertThat(clazz).isSameAs(PriorityClassLoaderTest.class);
    }
  }
}
