// (C) 2022 uchicom
package com.uchicom.plate.dto;

import java.util.Map;

public class PlateConfig {
  public String user;
  public String hash;
  public ServiceDto service;
  public BatchDto batch;
  public Map<String, GithubDto> github;
  public Map<String, DeployDto> deploy;
}
