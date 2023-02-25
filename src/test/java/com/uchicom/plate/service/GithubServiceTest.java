// (C) 2022 uchicom
package com.uchicom.plate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.uchicom.plate.dto.DownloadFileDto;
import com.uchicom.plate.dto.GithubDto;
import com.uchicom.plate.enumeration.DownloadFileKind;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

public class GithubServiceTest {

  @Captor ArgumentCaptor<String> tokenCaptor;
  @Captor ArgumentCaptor<File> dirCaptor;
  @Captor ArgumentCaptor<String> reposCaptor;
  @Captor ArgumentCaptor<String> filterCaptor;
  @Captor ArgumentCaptor<String> tagCaptor;
  @Captor ArgumentCaptor<DownloadFileKind> kindCaptor;

  AutoCloseable closeable;

  @BeforeEach
  public void openMocks() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void releaseMocks() throws Exception {
    closeable.close();
  }

  @Test
  public void downloadJar() throws Exception {
    // mock
    var service = spy(new GithubService());
    var dto = new GithubDto();
    dto.token = "token";
    dto.dirPath = "/workspace/plate/release";
    dto.repos = "uchicom/smtp";
    dto.downloadFiles = new ArrayList<>();

    var downloadFileDto = new DownloadFileDto();
    downloadFileDto.kind = DownloadFileKind.ASSETS;
    downloadFileDto.filter = "[^\"]+.jar";
    dto.downloadFiles.add(downloadFileDto);

    var tag = "0.1.14";

    var dir = mock(File.class);
    doReturn(dir).when(service).createFile(dto.dirPath, tag);
    doReturn(true).when(dir).exists();
    var path = mock(Path.class);
    var file = mock(File.class);
    doReturn(file).when(path).toFile();
    doReturn(List.of(path))
        .when(service)
        .downloadAssets(
            tokenCaptor.capture(),
            dirCaptor.capture(),
            reposCaptor.capture(),
            filterCaptor.capture(),
            tagCaptor.capture());

    // test
    service.download(dto, tag);

    // assert
    assertEquals(dto.token, tokenCaptor.getValue());
    assertEquals(dir, dirCaptor.getValue());
    assertEquals(dto.repos, reposCaptor.getValue());
    assertEquals(downloadFileDto.filter, filterCaptor.getValue());
    assertEquals(tag, tagCaptor.getValue());
  }

  @Test
  public void downloadZip() throws Exception {
    // mock
    var service = spy(new GithubService());
    var dto = new GithubDto();
    dto.token = "token";
    dto.dirPath = "/workspace/plate/release";
    dto.repos = "uchicom/smtp";
    dto.downloadFiles = new ArrayList<>();
    var downloadFileDto = new DownloadFileDto();
    downloadFileDto.kind = DownloadFileKind.ZIPBALL;
    dto.downloadFiles.add(downloadFileDto);

    var tag = "0.1.14";

    var dir = mock(File.class);
    doReturn(dir).when(service).createFile(dto.dirPath, tag);
    doReturn(true).when(dir).exists();
    var path = mock(Path.class);
    var file = mock(File.class);
    doReturn(file).when(path).toFile();
    doReturn(path)
        .when(service)
        .downloadFile(
            tokenCaptor.capture(),
            dirCaptor.capture(),
            reposCaptor.capture(),
            kindCaptor.capture(),
            tagCaptor.capture());

    service.download(dto, tag);

    // assert
    assertEquals(dto.token, tokenCaptor.getValue());
    assertEquals(dir, dirCaptor.getValue());
    assertEquals(dto.repos, reposCaptor.getValue());
    assertEquals(downloadFileDto.kind, kindCaptor.getValue());
    assertEquals(tag, tagCaptor.getValue());
  }

  @Test
  public void downloadTar() throws Exception {
    // mock
    var service = spy(new GithubService());
    var dto = new GithubDto();
    dto.token = "token";
    dto.dirPath = "/workspace/plate/release";
    dto.repos = "uchicom/smtp";
    dto.downloadFiles = new ArrayList<>();
    var downloadFileDto = new DownloadFileDto();
    downloadFileDto.kind = DownloadFileKind.TARBALL;
    dto.downloadFiles.add(downloadFileDto);

    var tag = "0.1.14";

    var dir = mock(File.class);
    doReturn(dir).when(service).createFile(dto.dirPath, tag);
    doReturn(true).when(dir).exists();
    var path = mock(Path.class);
    var file = mock(File.class);
    doReturn(file).when(path).toFile();
    doReturn(path)
        .when(service)
        .downloadFile(
            tokenCaptor.capture(),
            dirCaptor.capture(),
            reposCaptor.capture(),
            kindCaptor.capture(),
            tagCaptor.capture());

    // test
    service.download(dto, tag);

    // assert
    assertEquals(dto.token, tokenCaptor.getValue());
    assertEquals(dir, dirCaptor.getValue());
    assertEquals(dto.repos, reposCaptor.getValue());
    assertEquals(downloadFileDto.kind, kindCaptor.getValue());
    assertEquals(tag, tagCaptor.getValue());
  }
}
