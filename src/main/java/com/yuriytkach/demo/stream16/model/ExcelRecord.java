package com.yuriytkach.demo.stream16.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ExcelRecord(
  String field1,
  String field2
) {

  @JsonIgnore
  public boolean isValid() {
    return field1 != null && field2 != null && !field1.isBlank() && !field2.isBlank();
  }
}
