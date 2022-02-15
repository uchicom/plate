// (C) 2022 uchicom
package com.uchicom.plate.dto;

import java.util.List;

public class StarterDto {
  public String key;
  public List<String> classPath;
  public String className;
  public MethodDto method = new MethodDto();
  public String[] parameters;
  public boolean disabled;
  public OrderDto order = new OrderDto();
}
