// (C) 2022 uchicom
package com.uchicom.plate.service;

import com.uchicom.plate.dto.ReleaseDto;
import com.uchicom.plate.enumeration.DownloadFileKind;
import com.uchicom.plate.exception.ServiceException;
import com.uchicom.plate.helper.GithubHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GithubService {
  private final GithubHelper githubHelper;

  public GithubService(GithubHelper githubHelper) {
    this.githubHelper = githubHelper;
  }

  public String download(ReleaseDto releaseDto, String tag) throws ServiceException {
    if (releaseDto.github == null) {
      throw new ServiceException("github設定は存在しません.");
    }
    if (tag == null
        || tag.isEmpty()
        || tag.equals(".")
        || tag.equals("..")
        || tag.contains("/")
        || tag.contains("\\")) {
      throw new ServiceException("tagが不正です." + tag);
    }
    try {
      var dir = createFile(releaseDto.dirPath, tag);
      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          throw new ServiceException("ディレクトリ作成に失敗しました." + dir.getPath());
        }
      }
      var dto = releaseDto.github;
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
    } catch (ServiceException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  /** JARダウンロード. */
  List<Path> downloadAssets(String token, File dir, String repos, String filter, String tag)
      throws IOException, InterruptedException, ServiceException {
    String body = githubHelper.fetchString(token, repos, DownloadFileKind.TAGS, tag);
    var matcher =
        Pattern.compile("\"id\":([0-9]+),\"node_id\":\"[^\"]+\",\"name\":\"(" + filter + ")\",")
            .matcher(body);
    var list = new ArrayList<Path>();
    while (matcher.find()) {
      list.add(
          githubHelper.fetchFile(
              token,
              repos,
              DownloadFileKind.ASSETS,
              matcher.group(1),
              new File(dir, matcher.group(2)).toPath()));
    }
    return list;
  }

  Path downloadFile(String token, File dir, String repos, DownloadFileKind downloadKind, String tag)
      throws IOException, InterruptedException, ServiceException {
    return githubHelper.fetchFile(
        token, repos, downloadKind, tag, new File(dir, downloadKind.getFileName(tag)).toPath());
  }

  File createFile(String parent, String child) {
    return new File(parent, child);
  }
}
