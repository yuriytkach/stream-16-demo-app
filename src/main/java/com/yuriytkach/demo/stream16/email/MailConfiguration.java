package com.yuriytkach.demo.stream16.email;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfiguration {

  @Bean
  JavaMailSender javaMailSender(final MailProperties properties) {
    final JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(properties.getHost());
    sender.setPort(properties.getPort());
    sender.setUsername(properties.getUsername());
    sender.setPassword(properties.getPassword());
    sender.setProtocol("smtp");

    final Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enabled", "true");
    props.put("mail.smtp.ssl.checkserveridentity", "true");
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

    sender.setJavaMailProperties(props);
    return sender;
  }

}
