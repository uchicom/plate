// (C) 2022 uchicom
package com.uchicom.plate.service;

import com.uchicom.plate.dto.DownloadFileDto;
import com.uchicom.plate.dto.GithubDto;
import com.uchicom.plate.enumeration.DownloadFileKind;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class GithubServiceTest {
  @Test
  public void downloadJar() {
    var service = new GithubService();
    var dto = new GithubDto();
    dto.dirPath = "/workspace/plate/release";
    dto.repos = "uchicom/smtp";
    dto.downloadFiles = new ArrayList<>();
    var downloadFileDto = new DownloadFileDto();
    downloadFileDto.kind = DownloadFileKind.ASSETS;
    downloadFileDto.filter = ".*.jar";
    dto.downloadFiles.add(downloadFileDto);
    service.download(dto, "0.1.14");
  }

  @Test
  public void downloadZip() {
    var service = new GithubService();
    var dto = new GithubDto();
    dto.dirPath = "/workspace/plate/release";
    dto.repos = "uchicom/smtp";
    dto.downloadFiles = new ArrayList<>();
    var downloadFileDto = new DownloadFileDto();
    downloadFileDto.kind = DownloadFileKind.ZIPBALL;
    downloadFileDto.filter = ".*.jar";
    dto.downloadFiles.add(downloadFileDto);
    service.download(dto, "0.1.14");
  }

  @Test
  public void downloadTar() {
    var service = new GithubService();
    var dto = new GithubDto();
    dto.dirPath = "/workspace/plate/release";
    dto.repos = "uchicom/smtp";
    dto.downloadFiles = new ArrayList<>();
    var downloadFileDto = new DownloadFileDto();
    downloadFileDto.kind = DownloadFileKind.TARBALL;
    downloadFileDto.filter = ".*.jar";
    dto.downloadFiles.add(downloadFileDto);
    service.download(dto, "0.1.14");
  }
}
