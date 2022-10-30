package com.yuriytkach.demo.stream16.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Data;

@Data
@ConstructorBinding
@ConfigurationProperties(value = "app.email", ignoreUnknownFields = false)
public class MailProperties {
  private final String fromEmail;
  private final String toEmail;

  private final String host;
  private final int port;
  private final String username;
  private final String password;
}
