// (C) 2026 uchicom
package com.uchicom.plate.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

  private static URL dirUrl(Path dir) throws Exception {
    return dir.toUri().toURL();
  }

  @Test
  public void getResource_childIsPreferred(@TempDir Path childDir, @TempDir Path parentDir)
      throws Exception {
    Files.writeString(childDir.resolve("same.txt"), "child");
    Files.writeString(parentDir.resolve("same.txt"), "parent");
    try (var parent = new URLClassLoader(new URL[] {dirUrl(parentDir)}, null);
        var loader = new PriorityClassLoader(new URL[] {dirUrl(childDir)}, parent)) {
      // test
      String text;
      try (var in = loader.getResourceAsStream("same.txt")) {
        text = new String(in.readAllBytes());
      }
      var urls = Collections.list(loader.getResources("same.txt"));

      // assert
      assertThat(text).isEqualTo("child");
      assertThat(urls).hasSize(2);
      assertThat(urls.get(0).toString()).contains(childDir.getFileName().toString());
      assertThat(urls.get(1).toString()).contains(parentDir.getFileName().toString());
    }
  }

  @Test
  public void getResource_fallsBackToParent(@TempDir Path childDir, @TempDir Path parentDir)
      throws Exception {
    Files.writeString(parentDir.resolve("only.txt"), "parent");
    try (var parent = new URLClassLoader(new URL[] {dirUrl(parentDir)}, null);
        var loader = new PriorityClassLoader(new URL[] {dirUrl(childDir)}, parent)) {
      // test
      String text;
      try (var in = loader.getResourceAsStream("only.txt")) {
        text = new String(in.readAllBytes());
      }

      // assert
      assertThat(text).isEqualTo("parent");
    }
  }

  @Test
  public void getResource_serviceLoaderResolvesChildFirst(
      @TempDir Path childDir, @TempDir Path parentDir) throws Exception {
    var name = "META-INF/services/java.lang.Runnable";
    Files.createDirectories(childDir.resolve("META-INF/services"));
    Files.createDirectories(parentDir.resolve("META-INF/services"));
    Files.writeString(childDir.resolve(name), "child.Impl");
    Files.writeString(parentDir.resolve(name), "parent.Impl");
    try (var parent = new URLClassLoader(new URL[] {dirUrl(parentDir)}, null);
        var loader = new PriorityClassLoader(new URL[] {dirUrl(childDir)}, parent)) {
      // test
      var first = loader.getResources(name).nextElement();

      // assert
      try (var in = first.openStream()) {
        assertThat(new String(in.readAllBytes())).isEqualTo("child.Impl");
      }
    }
  }

  @Test
  public void loadClass_javaxClassIsLoadedByParent() throws Exception {
    try (var loader = new PriorityClassLoader(new URL[0])) {
      // test
      var clazz = loader.loadClass("javax.net.SocketFactory");

      // assert
      assertThat(clazz.getClassLoader()).isNotSameAs(loader);
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
