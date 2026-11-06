// (C) 2026 uchicom
package com.uchicom.plate.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.uchicom.plate.exception.ServiceException;
import java.net.URI;
import java.net.http.HttpRequest;
import org.junit.jupiter.api.Test;

public class GithubHelperTest {

  HttpRequest request = HttpRequest.newBuilder(URI.create("https://a/")).build();

  @Test
  public void checkStatusOk() throws Exception {
    var helper = new GithubHelper();
    helper.checkStatus(200, request);
    helper.checkStatus(299, request);
  }

  @Test
  public void checkStatusError() {
    var helper = new GithubHelper();
    for (var status : new int[] {199, 301, 401, 404, 500}) {
      assertThrows(ServiceException.class, () -> helper.checkStatus(status, request));
    }
  }
}
