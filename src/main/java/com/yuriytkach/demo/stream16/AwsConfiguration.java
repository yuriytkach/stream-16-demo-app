package com.yuriytkach.demo.stream16;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class AwsConfiguration {

  private final AppProperties appProperties;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
      .region(appProperties.getAwsRegion())
      .credentialsProvider(DefaultCredentialsProvider.create())
      .build();
  }

}
