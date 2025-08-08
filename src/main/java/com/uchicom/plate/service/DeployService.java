// (C) 2023 uchicom
package com.uchicom.plate.service;

import com.uchicom.plate.dto.DeployDto;
import com.uchicom.plate.exception.ServiceException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class DeployService {

  public String lsdl(DeployDto dto) {
    var root = createFile(dto.dirPath);
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

  public boolean deploy(DeployDto dto, String tag) throws ServiceException {
    var dir = createFile(dto.dirPath, tag);
    if (!dir.exists()) {
      throw new ServiceException("ディレクトリが存在しません." + dir.getPath());
    }
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

      var fileList = new ArrayList<File>();
      fileList.addAll(List.of(fromFile));

      // ファイルの配置
      var toDir = createFile(deployFile.to);
      try {
        for (var file : fileList) {
          copy(file.toPath(), createFile(toDir, file.getName()).toPath());
        }
      } catch (IOException e) {
        throw new ServiceException(e);
      }
    }
    return true;
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
    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
  }
}
