package com.yuriytkach.demo.stream16;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yuriytkach.demo.stream16.email.EmailSender;
import com.yuriytkach.demo.stream16.model.ExcelReadResult;
import com.yuriytkach.demo.stream16.model.UploadResponse;
import com.yuriytkach.demo.stream16.sftp.SftpSender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Controller {

  Logger logger = LoggerFactory.getLogger(getClass());

  public static final String EXCEL_MEDIA_TYPE = "multipart/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  private final ExcelReader excelReader;
  private final RecordsSender recordsSender;
  private final AwsS3Saver awsS3Saver;
  private final SftpSender sftpSender;
  private final EmailSender emailSender;

  @PostMapping(
    value = "/excel",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = EXCEL_MEDIA_TYPE
  )
  public ResponseEntity<Object> uploadFile(
    @RequestParam final MultipartFile file
  ) {
    log.info("Loading file2 {} of size {}", file.getOriginalFilename(), file.getSize());

    try {
      final ExcelReadResult result = excelReader.read(file.getInputStream());
      log.debug("Excel read result ok: {}, failed: {}", result.records().size(), result.failedRows());
      recordsSender.send(result.records());
      awsS3Saver.save(file.getOriginalFilename(), file.getBytes());

      sftpSender.send(file.getOriginalFilename(), file.getBytes());
      emailSender.send(file.getOriginalFilename());

      return ResponseEntity.accepted().body(new UploadResponse(result.records().size(), result.failedRows()));
    } catch (final IOException ex) {
      log.error("Failed to read excel file: {}", ex.getMessage(), ex);
      return ResponseEntity.badRequest()
        .body(ex.getMessage());
    }
  }

}
