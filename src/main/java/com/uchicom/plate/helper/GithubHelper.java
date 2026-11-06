// (C) 2026 uchicom
package com.uchicom.plate.helper;

import com.uchicom.plate.enumeration.DownloadFileKind;
import com.uchicom.plate.exception.ServiceException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/** GitHub APIへのHTTPアクセスを行う. */
public class GithubHelper {
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(5);

  HttpClient createClient() {
    return HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .connectTimeout(CONNECT_TIMEOUT)
        .build();
  }

  public String fetchString(String token, String repos, DownloadFileKind downloadKind, String param)
      throws IOException, InterruptedException, ServiceException {
    var request = createRequest(token, repos, downloadKind, param);
    var response = createClient().send(request, BodyHandlers.ofString());
    checkStatus(response.statusCode(), request);
    return response.body();
  }

  /** 一時ファイルへダウンロードし、成功時のみ目的のパスへ移動する. */
  public Path fetchFile(
      String token, String repos, DownloadFileKind downloadKind, String param, Path target)
      throws IOException, InterruptedException, ServiceException {
    var request = createRequest(token, repos, downloadKind, param);
    var temp = Files.createTempFile(target.toAbsolutePath().getParent(), "download", ".tmp");
    try {
      var response = createClient().send(request, BodyHandlers.ofFile(temp));
      checkStatus(response.statusCode(), request);
      return Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temp);
    }
  }

  void checkStatus(int statusCode, HttpRequest request) throws ServiceException {
    if (statusCode < 200 || statusCode >= 300) {
      throw new ServiceException("ダウンロードに失敗しました. status=" + statusCode + " " + request.uri());
    }
  }

  HttpRequest createRequest(
      String token, String repos, DownloadFileKind downloadKind, String param) {
    var builder =
        HttpRequest.newBuilder()
            .uri(createUri(repos, downloadKind, param))
            .timeout(REQUEST_TIMEOUT)
            .GET();
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
