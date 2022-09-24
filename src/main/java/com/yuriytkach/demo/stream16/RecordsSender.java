package com.yuriytkach.demo.stream16;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.yuriytkach.demo.stream16.model.ExcelRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordsSender {

  private final RestTemplate restTemplate;

  public void send(final List<ExcelRecord> records) {
    try {
      final ResponseEntity<String> responseEntity = restTemplate.postForEntity(
        "/records",
        records,
        String.class
      );

      log.info("Records submit status: [{}] {}", responseEntity.getStatusCode(), responseEntity.getBody());
    } catch (final Exception ex) {
      log.error("Failed to submit records: {}", ex.getMessage(), ex);
    }
  }
}
