// (C) 2022 uchicom
package com.uchicom.plate.service;

import com.uchicom.plate.dto.GithubDto;
import com.uchicom.plate.enumeration.DownloadFileKind;
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
  public boolean download(GithubDto dto, String tag) {
    try {
      var dirPath = dto.dirPath + "/" + tag;
      var dir = new File(dirPath);
      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          System.out.println("ディレクトリ作成に失敗しました." + dirPath);
          return false;
        }
      }
      var list = new ArrayList<Path>();
      for (var downloadFile : dto.downloadFiles) {
        switch (downloadFile.kind) {
          case ASSETS:
            list.addAll(downloadAssets(dto.token, dirPath, dto.repos, downloadFile.filter, tag));
            break;
          case TARBALL:
          case ZIPBALL:
            list.add(downloadFile(dto.token, dirPath, dto.repos, downloadFile.kind, tag));
          default:
            // fall through
        }
      }
      list.forEach(
          path -> {
            var file = path.toFile();
            System.out.println(file.getName() + ":" + file.length());
          });
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  /** JARダウンロード. */
  List<Path> downloadAssets(String token, String dirPath, String repos, String filter, String tag)
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
              BodyHandlers.ofFile(Path.of(dirPath + "/" + matcher.group(2)))));
    }
    return list;
  }

  /**
   * ファイルダウンロード.
   *
   * @param token
   * @param dirPath
   * @param repos
   * @param downloadKind
   * @param tag
   * @throws IOException
   * @throws InterruptedException
   */
  Path downloadFile(
      String token, String dirPath, String repos, DownloadFileKind downloadKind, String tag)
      throws IOException, InterruptedException {
    return dowload(
        token,
        repos,
        downloadKind,
        tag,
        BodyHandlers.ofFile(Path.of(dirPath + "/" + downloadKind.getFileName(tag))));
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
}
