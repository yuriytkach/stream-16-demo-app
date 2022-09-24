package com.yuriytkach.demo.stream16;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Saver {

  private final S3Client s3Client;
  private final AppProperties appProperties;

  public void save(final String filename, final byte[] file) {
    log.info("Saving file to S3: {}", filename);

    try {
      s3Client.putObject(
        PutObjectRequest.builder().bucket(appProperties.getS3Bucket()).key(filename).build(),
        RequestBody.fromBytes(file)
      );
    } catch (final Exception ex) {
      log.error("Failed to save to S3: {}", ex.getMessage(), ex);
      throw new IllegalStateException("Failed to save to S3", ex);
    }
  }
}
