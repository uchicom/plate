// (C) 2022 uchicom
package com.uchicom.plate.enumeration;

public enum DownloadFileKind {
  TAGS("releases/tags/", ".json"),
  ASSETS("releases/assets/", ""),
  TARBALL("tarball/", ".tar.gz"),
  ZIPBALL("zipball/", ".zip");
  public final String path;
  public final String extension;

  DownloadFileKind(String path, String extension) {
    this.path = path;
    this.extension = extension;
  }

  public String getPath(String param) {
    return path + param;
  }

  public String getFileName(String tag) {
    return tag + extension;
  }
}
