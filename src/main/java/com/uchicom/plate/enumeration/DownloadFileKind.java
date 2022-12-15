// (C) 2022 uchicom
package com.uchicom.plate.enumeration;

public enum DownloadFileKind {
  TAGS("releases/tags/"),
  ASSETS("releases/assets/"),
  TARBALL("tarball/"),
  ZIPBALL("zipball/");
  public final String path;

  DownloadFileKind(String path) {
    this.path = path;
  }

  public String getPath(String param) {
    return path + param;
  }
}
