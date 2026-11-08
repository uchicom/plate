// (C) 2026 uchicom
package com.uchicom.plate.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

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
    if (isParentOnly(name)) {
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

  /** JDKのクラス・リソースは常に親へ委譲する(jar同梱による隠蔽防止、性能面)。 */
  private static boolean isParentOnly(String name) {
    return name.startsWith("java.")
        || name.startsWith("javax.")
        || name.startsWith("jdk.")
        || name.startsWith("sun.")
        || name.startsWith("java/")
        || name.startsWith("javax/")
        || name.startsWith("jdk/")
        || name.startsWith("sun/");
  }

  @Override
  public URL getResource(String name) {
    if (isParentOnly(name)) {
      return super.getResource(name);
    }
    var url = findResource(name);
    if (url != null) {
      return url;
    }
    return super.getResource(name);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if (isParentOnly(name)) {
      return super.getResources(name);
    }
    var urls = new ArrayList<URL>();
    urls.addAll(Collections.list(findResources(name)));
    var parent = getParent();
    if (parent != null) {
      urls.addAll(Collections.list(parent.getResources(name)));
    } else {
      urls.addAll(Collections.list(ClassLoader.getSystemResources(name)));
    }
    return Collections.enumeration(urls);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return super.findClass(name);
  }
}
