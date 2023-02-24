// (C) 2022 uchicom
package com.uchicom.plate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.uchicom.plate.dto.DeployDto;
import com.uchicom.plate.dto.DeployFileDto;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

public class DeployServiceTest {
  @Captor ArgumentCaptor<Integer> levelCaptor;
  @Captor ArgumentCaptor<File> dirCaptor;
  @Captor ArgumentCaptor<StringBuilder> stringBuilderCaptor;
  @Captor ArgumentCaptor<String> textCaptor;
  @Captor ArgumentCaptor<String> pathnameCaptor;
  @Captor ArgumentCaptor<String> parentCaptor;
  @Captor ArgumentCaptor<String> childCaptor;
  @Captor ArgumentCaptor<Path> fromPathCaptor;
  @Captor ArgumentCaptor<Path> toPathCaptor;

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
  public void lsdl() {
    // mock
    var service = spy(new DeployService());
    var dto = new DeployDto();
    dto.dirPath = "/workspace/plate/release";
    var file = mock(File.class);

    doReturn(file).when(service).createFile(dto.dirPath);
    doNothing()
        .when(service)
        .printFiles(levelCaptor.capture(), dirCaptor.capture(), stringBuilderCaptor.capture());

    // test
    service.lsdl(dto);

    // assert
    assertEquals(0, levelCaptor.getValue());
    assertEquals(file, dirCaptor.getValue());
  }

  @Test
  public void printFiles() {
    // mock
    var service = spy(new DeployService());
    var dir = mock(File.class);
    doReturn("name").when(dir).getName();
    doReturn(new File[0]).when(dir).listFiles();
    var builder = new StringBuilder();
    var dto = new DeployDto();
    dto.dirPath = "/workspace/plate/release";

    // test
    service.printFiles(0, dir, builder);

    // assert
    assertEquals("- name/\n", builder.toString());
  }

  @Test
  public void printNest() {
    var service = new DeployService();
    var builder = new StringBuilder();
    // test
    service.printNest(builder, 0);
    // assert
    assertEquals("", builder.toString());

    // test
    builder.setLength(0);
    service.printNest(builder, 1);
    // assert
    assertEquals(" ", builder.toString());

    // test
    builder.setLength(0);
    service.printNest(builder, 2);
    // assert
    assertEquals("  ", builder.toString());
  }

  @Test
  public void deploy() throws Exception {
    // mock
    var service = spy(new DeployService());
    var dto = new DeployDto();
    dto.dirPath = "/workspace/plate/release";
    var deployFileDto = new DeployFileDto();
    deployFileDto.from = "from";
    deployFileDto.to = "to";
    dto.deployFiles = List.of(deployFileDto);
    var tag = "vTest";
    var dir = mock(File.class);
    doReturn(true).when(dir).exists();
    doReturn(dir).when(service).createFile(dto.dirPath, tag);
    var fromFile = mock(File.class);
    doReturn(true).when(fromFile).exists();
    doReturn("name").when(fromFile).getName();
    var fromPath = mock(Path.class);
    doReturn(fromPath).when(fromFile).toPath();
    doReturn(fromFile).when(service).createFile(dir, deployFileDto.from);
    var toDir = mock(File.class);
    doReturn(toDir).when(service).createFile(deployFileDto.to);
    var toFile = mock(File.class);
    doReturn(toFile).when(service).createFile(toDir, "name");
    var toPath = mock(Path.class);
    doReturn(toPath).when(toFile).toPath();
    doNothing().when(service).copy(fromPathCaptor.capture(), toPathCaptor.capture());

    // test
    service.deploy(dto, tag);

    // assert
    assertEquals(fromPath, fromPathCaptor.getValue());
    assertEquals(toPath, toPathCaptor.getValue());
  }
}
