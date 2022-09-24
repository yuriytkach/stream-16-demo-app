package com.yuriytkach.demo.stream16.model;

public record UploadResponse(
  int loadedRecords,
  int failedRecord
) {

}
