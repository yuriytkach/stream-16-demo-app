package com.yuriytkach.demo.stream16.sftp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Data;

@Data
@ConstructorBinding
@ConfigurationProperties(value = "app.sftp", ignoreUnknownFields = false)
public class SftpProperties {
  private final String host;
  private final int port;
  private final String username;
  private final String privateKeyFile;
  private final String privateKeyPassword;
  private final String folder;

}
