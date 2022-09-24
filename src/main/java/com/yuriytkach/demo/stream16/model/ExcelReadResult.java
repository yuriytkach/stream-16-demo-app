package com.yuriytkach.demo.stream16.model;

import java.util.List;

public record ExcelReadResult(
  List<ExcelRecord> records,
  int failedRows
) {

}
