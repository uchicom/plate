// (C) 2022 uchicom
package com.uchicom.plate.service;

import com.uchicom.plate.dto.GithubDto;
import com.uchicom.plate.enumeration.DownloadFileKind;
import com.uchicom.plate.exception.ServiceException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GithubService {
  public String download(GithubDto dto, String tag) throws ServiceException {
    try {
      var dir = createFile(dto.dirPath, tag);
      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          throw new ServiceException("ディレクトリ作成に失敗しました." + dir.getPath());
        }
      }
      var list = new ArrayList<Path>();
      for (var downloadFile : dto.downloadFiles) {
        switch (downloadFile.kind) {
          case ASSETS ->
              list.addAll(downloadAssets(dto.token, dir, dto.repos, downloadFile.filter, tag));
          case TARBALL, ZIPBALL ->
              list.add(downloadFile(dto.token, dir, dto.repos, downloadFile.kind, tag));
          default -> {}
        }
      }
      StringBuilder builder = new StringBuilder(1024);
      list.forEach(
          path -> {
            var file = path.toFile();
            builder.append(file.getName() + ":" + file.length());
          });
      return builder.toString();
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  /** JARダウンロード. */
  List<Path> downloadAssets(String token, File dir, String repos, String filter, String tag)
      throws IOException, InterruptedException {
    String body = dowload(token, repos, DownloadFileKind.TAGS, tag, BodyHandlers.ofString());
    var matcher =
        Pattern.compile("\"id\":([0-9]+),\"node_id\":\"[^\"]+\",\"name\":\"(" + filter + ")\",")
            .matcher(body);
    var list = new ArrayList<Path>();
    while (matcher.find()) {
      list.add(
          dowload(
              token,
              repos,
              DownloadFileKind.ASSETS,
              matcher.group(1),
              BodyHandlers.ofFile(new File(dir, matcher.group(2)).toPath())));
    }
    return list;
  }

  Path downloadFile(String token, File dir, String repos, DownloadFileKind downloadKind, String tag)
      throws IOException, InterruptedException {
    return dowload(
        token,
        repos,
        downloadKind,
        tag,
        BodyHandlers.ofFile(new File(dir, downloadKind.getFileName(tag)).toPath()));
  }

  <T> T dowload(
      String token,
      String repos,
      DownloadFileKind downloadKind,
      String param,
      BodyHandler<T> handler)
      throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    var request = createRequest(token, repos, downloadKind, param);

    var response = client.send(request, handler);
    return response.body();
  }

  HttpRequest createRequest(
      String token, String repos, DownloadFileKind downloadKind, String param) {
    var builder = HttpRequest.newBuilder().uri(createUri(repos, downloadKind, param)).GET();
    if (downloadKind == DownloadFileKind.ASSETS) {
      builder.header("Accept", "application/octet-stream");
    }
    if (token != null) {
      builder.header("Authorization", "Bearer " + token);
    }
    return builder.build();
  }

  String createPath(String repos, DownloadFileKind downloadKind, String param) {
    return "/repos/" + repos + "/" + downloadKind.getPath(param);
  }

  URI createUri(String repos, DownloadFileKind downloadKind, String param) {
    var uri = "https://api.github.com" + createPath(repos, downloadKind, param);
    return URI.create(uri);
  }

  File createFile(String parent, String child) {
    return new File(parent, child);
  }
}
