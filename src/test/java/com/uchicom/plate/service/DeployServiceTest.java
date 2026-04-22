// (C) 2022 uchicom
package com.uchicom.plate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.uchicom.plate.dto.DeployDto;
import com.uchicom.plate.dto.DeployFileDto;
import com.uchicom.plate.dto.ReleaseDto;
import com.uchicom.plate.exception.ServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

public class DeployServiceTest {
  @Captor ArgumentCaptor<Integer> levelCaptor;
  @Captor ArgumentCaptor<File> dirCaptor;
  @Captor ArgumentCaptor<StringBuilder> stringBuilderCaptor;
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
    var releaseDto = new ReleaseDto();
    var dto = new DeployDto();
    releaseDto.dirPath = "/workspace/plate/release";
    releaseDto.deploy = dto;
    var file = mock(File.class);

    doReturn(file).when(service).createFile(releaseDto.dirPath);
    doNothing()
        .when(service)
        .printFiles(levelCaptor.capture(), dirCaptor.capture(), stringBuilderCaptor.capture());

    // test
    service.lsdl(releaseDto);

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
    var releaseDto = new ReleaseDto();
    var dto = new DeployDto();
    releaseDto.dirPath = "/workspace/plate/release";
    releaseDto.deploy = dto;
    var deployFileDto = new DeployFileDto();
    deployFileDto.from = "from";
    deployFileDto.to = "to";
    dto.deployFiles = List.of(deployFileDto);
    var tag = "vTest";
    var dir = mock(File.class);
    doReturn(true).when(dir).exists();
    doReturn(dir).when(service).createFile(releaseDto.dirPath, tag);
    var fromFile = mock(File.class);
    doReturn(List.of(fromFile)).when(service).matchFiles(dir, deployFileDto.from);
    var toDir = mock(File.class);
    doReturn(toDir).when(service).createFile(deployFileDto.to);
    doNothing().when(service).deployFile(fromFile, null, toDir);

    // test
    service.deploy(releaseDto, tag);

    // assert
    verify(service).deployFile(fromFile, null, toDir);
  }

  @Test
  public void deployFile_noDecompress() throws Exception {
    // mock
    var service = spy(new DeployService());
    var fromFile = mock(File.class);
    var toDir = mock(File.class);
    doReturn("app.jar").when(fromFile).getName();
    var fromPath = mock(Path.class);
    doReturn(fromPath).when(fromFile).toPath();
    var toFile = mock(File.class);
    doReturn(toFile).when(service).createFile(toDir, "app.jar");
    var toPath = mock(Path.class);
    doReturn(toPath).when(toFile).toPath();
    doNothing().when(service).copy(fromPathCaptor.capture(), toPathCaptor.capture());

    // test
    service.deployFile(fromFile, null, toDir);

    // assert
    assertEquals(fromPath, fromPathCaptor.getValue());
    assertEquals(toPath, toPathCaptor.getValue());
  }

  @Test
  public void deployFile_tarGz() throws Exception {
    // mock
    var service = spy(new DeployService());
    var fromFile = mock(File.class);
    var toDir = mock(File.class);
    doReturn("app.tar.gz").when(fromFile).getName();
    doNothing().when(service).decompressTarGz(fromFile, "glob:*/www/*", toDir);

    // test
    service.deployFile(fromFile, "glob:*/www/*", toDir);

    // assert
    verify(service).decompressTarGz(fromFile, "glob:*/www/*", toDir);
    verify(service, never()).copy(any(), any());
  }

  @Test
  public void deployFile_tgz() throws Exception {
    // mock
    var service = spy(new DeployService());
    var fromFile = mock(File.class);
    var toDir = mock(File.class);
    doReturn("app.tgz").when(fromFile).getName();
    doNothing().when(service).decompressTarGz(fromFile, "glob:*/www/*", toDir);

    // test
    service.deployFile(fromFile, "glob:*/www/*", toDir);

    // assert
    verify(service).decompressTarGz(fromFile, "glob:*/www/*", toDir);
    verify(service, never()).copy(any(), any());
  }

  @Test
  public void deployFile_zip() throws Exception {
    // mock
    var service = spy(new DeployService());
    var fromFile = mock(File.class);
    var toDir = mock(File.class);
    doReturn("app.zip").when(fromFile).getName();
    doNothing().when(service).decompressZip(fromFile, "glob:*/www/*", toDir);

    // test
    service.deployFile(fromFile, "glob:*/www/*", toDir);

    // assert
    verify(service).decompressZip(fromFile, "glob:*/www/*", toDir);
    verify(service, never()).copy(any(), any());
  }

  @Test
  public void deployFile_otherExtension() throws Exception {
    // mock
    var service = spy(new DeployService());
    var fromFile = mock(File.class);
    var toDir = mock(File.class);
    doReturn("app.tar.bz2").when(fromFile).getName();

    // test: 例外なし、copy/decompressも呼ばれない
    service.deployFile(fromFile, "glob:*/www/*", toDir);

    // assert
    verify(service, never()).copy(any(), any());
    verify(service, never()).decompressTarGz(any(), any(), any());
    verify(service, never()).decompressZip(any(), any(), any());
  }

  @Test
  public void deployFile_ioException() throws Exception {
    // mock
    var service = spy(new DeployService());
    var fromFile = mock(File.class);
    var toDir = mock(File.class);
    doReturn("app.jar").when(fromFile).getName();
    var fromPath = mock(Path.class);
    doReturn(fromPath).when(fromFile).toPath();
    var toFile = mock(File.class);
    doReturn(toFile).when(service).createFile(toDir, "app.jar");
    doReturn(mock(Path.class)).when(toFile).toPath();
    doThrow(new IOException("test")).when(service).copy(any(), any());

    // test & assert
    assertThrows(ServiceException.class, () -> service.deployFile(fromFile, null, toDir));
  }

  @Test
  public void nullTerminated_withNullChar() {
    var service = new DeployService();
    var buf = new byte[100];
    var name = "hello";
    System.arraycopy(name.getBytes(StandardCharsets.UTF_8), 0, buf, 0, name.length());

    assertEquals("hello", service.nullTerminated(buf, 0, 100));
  }

  @Test
  public void nullTerminated_noNullChar() {
    var service = new DeployService();
    var name = "hello world";
    var buf = name.getBytes(StandardCharsets.UTF_8);

    assertEquals("hello world", service.nullTerminated(buf, 0, buf.length));
  }

  @Test
  public void decompressTar_matchingFile(@TempDir Path tempDir) throws Exception {
    var service = new DeployService();
    var outputDir = tempDir.toFile();
    var matcher = FileSystems.getDefault().getPathMatcher("glob:*/www/*");
    var tarStream = new ByteArrayOutputStream();
    tarStream.write(tarEntry("proj/www/index.html", "hello".getBytes(StandardCharsets.UTF_8)));
    tarStream.write(tarEntry("proj/other/script.js", "script".getBytes(StandardCharsets.UTF_8)));
    tarStream.write(new byte[1024]); // end-of-archive

    // test
    service.decompressTar(new ByteArrayInputStream(tarStream.toByteArray()), matcher, outputDir);

    // assert: マッチしたファイルのみ展開される
    assertThat(new File(outputDir, "proj/www/index.html")).exists();
    assertThat(new File(outputDir, "proj/other/script.js")).doesNotExist();
  }

  @Test
  public void decompressTar_directoryEntry(@TempDir Path tempDir) throws Exception {
    var service = new DeployService();
    var outputDir = tempDir.toFile();
    var matcher = FileSystems.getDefault().getPathMatcher("glob:**");
    var tarStream = new ByteArrayOutputStream();
    tarStream.write(tarDirEntry("proj/www/"));
    tarStream.write(new byte[1024]); // end-of-archive

    // test
    service.decompressTar(new ByteArrayInputStream(tarStream.toByteArray()), matcher, outputDir);

    // assert: ディレクトリエントリはファイルとして作成されない
    assertThat(new File(outputDir, "proj/www")).doesNotExist();
  }

  @Test
  public void decompressZip_matchingFile(@TempDir Path tempDir) throws Exception {
    var service = new DeployService();
    var zipFile = tempDir.resolve("test.zip").toFile();
    try (var zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
      zos.putNextEntry(new ZipEntry("proj/www/index.html"));
      zos.write("hello".getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("proj/other/script.js"));
      zos.write("script".getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
    }
    var outputDir = tempDir.resolve("output").toFile();
    outputDir.mkdir();

    // test
    service.decompressZip(zipFile, "glob:*/www/*", outputDir);

    // assert: マッチしたファイルのみ展開される
    assertThat(new File(outputDir, "proj/www/index.html")).exists();
    assertThat(new File(outputDir, "proj/other/script.js")).doesNotExist();
  }

  @Test
  public void decompressZip_directoryEntry(@TempDir Path tempDir) throws Exception {
    var service = new DeployService();
    var zipFile = tempDir.resolve("test.zip").toFile();
    try (var zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
      zos.putNextEntry(new ZipEntry("proj/www/"));
      zos.closeEntry();
    }
    var outputDir = tempDir.resolve("output").toFile();
    outputDir.mkdir();

    // test
    service.decompressZip(zipFile, "glob:**", outputDir);

    // assert: ディレクトリエントリはファイルとして作成されない
    assertThat(new File(outputDir, "proj/www")).doesNotExist();
  }

  private byte[] tarEntry(String name, byte[] content) {
    var header = new byte[512];
    var nameBytes = name.getBytes(StandardCharsets.UTF_8);
    System.arraycopy(nameBytes, 0, header, 0, nameBytes.length);
    var sizeBytes = String.format("%011o ", content.length).getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(sizeBytes, 0, header, 124, sizeBytes.length);
    header[156] = '0'; // regular file
    int padded = content.length == 0 ? 0 : ((content.length + 511) / 512) * 512;
    var result = new byte[512 + padded];
    System.arraycopy(header, 0, result, 0, 512);
    System.arraycopy(content, 0, result, 512, content.length);
    return result;
  }

  private byte[] tarDirEntry(String name) {
    var header = new byte[512];
    var nameBytes = name.getBytes(StandardCharsets.UTF_8);
    System.arraycopy(nameBytes, 0, header, 0, nameBytes.length);
    header[156] = '5'; // directory
    return header;
  }
}
