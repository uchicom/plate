// (C) 2023 uchicom
package com.uchicom.plate.service;

import com.uchicom.plate.dto.ReleaseDto;
import com.uchicom.plate.exception.ServiceException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class DeployService {

  public String lsdl(ReleaseDto releaseDto) {
    var root = createFile(releaseDto.dirPath);
    var builder = new StringBuilder(1024);
    printFiles(0, root, builder);
    return builder.toString();
  }

  void printFiles(int level, File dir, StringBuilder builder) {

    printNest(builder, level).append("- ").append(dir.getName()).append("/").append("\n");
    for (var file : dir.listFiles()) {
      if (file.isDirectory()) {
        printFiles(level + 1, file, builder);
        continue;
      }
      printNest(builder, level).append(" - ").append(file.getName()).append("\n");
    }
  }

  StringBuilder printNest(StringBuilder builder, int level) {
    for (int i = 0; i < level; i++) {
      builder.append(" ");
    }
    return builder;
  }

  public boolean deploy(ReleaseDto releaseDto, String tag) throws ServiceException {
    if (releaseDto.deploy == null) {
      throw new ServiceException("deploy設定は存在しません.");
    }
    var dir = createFile(releaseDto.dirPath, tag);
    if (!dir.exists()) {
      throw new ServiceException("ディレクトリが存在しません." + dir.getPath());
    }
    var dto = releaseDto.deploy;
    // ファイル存在チェック
    for (var deployFile : dto.deployFiles) {
      var file = createFile(dir, deployFile.from);
      if (!file.exists()) {
        throw new ServiceException("ファイルが存在しません." + dir.getPath() + "/" + deployFile.from);
      }
    }
    // ファイル配置
    for (var deployFile : dto.deployFiles) {
      var fromFile = createFile(dir, deployFile.from);
      var toDir = createFile(deployFile.to);
      deployFile(fromFile, deployFile.decompress, toDir);
    }
    return true;
  }

  void deployFile(File fromFile, String decompress, File toDir) throws ServiceException {
    try {
      if (decompress == null) {
        copy(fromFile.toPath(), createFile(toDir, fromFile.getName()).toPath());
      } else {
        var name = fromFile.getName();
        if (name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
          decompressTarGz(fromFile, decompress, toDir);
        } else if (name.endsWith(".zip")) {
          decompressZip(fromFile, decompress, toDir);
        }
      }
    } catch (IOException e) {
      throw new ServiceException(e);
    }
  }

  void decompressTarGz(File file, String filter, File outputDir) throws IOException {
    var matcher = FileSystems.getDefault().getPathMatcher(filter);
    try (var gis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)))) {
      decompressTar(gis, matcher, outputDir);
    }
  }

  void decompressTar(InputStream is, PathMatcher matcher, File outputDir) throws IOException {
    var header = new byte[512];
    while (true) {
      if (is.readNBytes(header, 0, 512) < 512) break;

      // ファイル名取得（UStar prefix対応）
      var fileName = nullTerminated(header, 0, 100);
      if (fileName.isEmpty()) break;
      var prefix = nullTerminated(header, 345, 155);
      if (!prefix.isEmpty()) fileName = prefix + "/" + fileName;

      var sizeStr = new String(header, 124, 12, StandardCharsets.US_ASCII).trim();
      long fileSize = sizeStr.isEmpty() ? 0L : Long.parseLong(sizeStr, 8);
      byte fileType = header[156];

      var outFile = new File(outputDir, fileName);

      if (fileType != '5') { // ディレクトリ
        boolean matches = matcher.matches(Path.of(fileName));
        if (matches) {
          if (outFile.getParentFile() != null) outFile.getParentFile().mkdirs();

          try (var out = new FileOutputStream(outFile)) {
            var buf = new byte[4096];
            long remaining = fileSize;
            while (remaining > 0) {
              int read = is.readNBytes(buf, 0, (int) Math.min(buf.length, remaining));
              if (read == 0) break;
              out.write(buf, 0, read);
              remaining -= read;
            }
          }
        }
      }

      // 512バイト境界までパディングをスキップ
      int padding = (int) (fileSize % 512);
      if (padding != 0) is.skip(512 - padding);
    }
  }

  void decompressZip(File file, String filter, File outputDir) throws IOException {
    var matcher = FileSystems.getDefault().getPathMatcher(filter);
    try (var zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
      var entry = zis.getNextEntry();
      while (entry != null) {
        var name = entry.getName();
        var outFile = new File(outputDir, name);
        if (!entry.isDirectory()) {
          boolean matches = matcher.matches(Path.of(name));
          if (matches) {
            if (outFile.getParentFile() != null) outFile.getParentFile().mkdirs();
            try (var out = new FileOutputStream(outFile)) {
              zis.transferTo(out);
            }
          }
        }
        zis.closeEntry();
        entry = zis.getNextEntry();
      }
    }
  }

  String nullTerminated(byte[] buf, int offset, int length) {
    var s = new String(buf, offset, length, StandardCharsets.UTF_8);
    int idx = s.indexOf('\0');
    return (idx >= 0 ? s.substring(0, idx) : s).trim();
  }

  File createFile(String parent, String child) {
    return new File(parent, child);
  }

  File createFile(File parentFile, String child) {
    return new File(parentFile, child);
  }

  File createFile(String pathname) {
    return new File(pathname);
  }

  void copy(Path from, Path to) throws IOException {
    to.getParent().toFile().mkdirs();
    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
  }
}
