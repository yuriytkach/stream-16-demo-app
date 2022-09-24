package com.yuriytkach.demo.stream16;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Data;
import software.amazon.awssdk.regions.Region;

@Data
@ConstructorBinding
@ConfigurationProperties("app")
public class AppProperties {
  private final int consumerConnectTimeout;
  private final int consumerReadTimeout;
  private final String consumerBasePath;

  private final String s3Bucket;
  private final Region awsRegion;
}
