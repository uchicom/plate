// (C) 2026 uchicom
package com.uchicom.plate.util;

import java.net.URL;
import java.net.URLClassLoader;

public class PriorityClassLoader extends URLClassLoader {
  public PriorityClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public PriorityClassLoader(URL[] urls) {
    super(urls);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    // JDKコアクラスは自身では定義できない(SecurityException)ため常に親へ委譲する
    if (name.startsWith("java.")) {
      return super.loadClass(name, resolve);
    }
    synchronized (getClassLoadingLock(name)) {
      // 1. すでにロードされているか確認
      Class<?> c = findLoadedClass(name);

      if (c == null) {
        try {
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          c = super.loadClass(name, resolve);
        }
      }
      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return super.findClass(name);
  }
}
