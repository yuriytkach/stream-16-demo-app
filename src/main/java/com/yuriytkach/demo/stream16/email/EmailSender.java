package com.yuriytkach.demo.stream16.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {

  private final JavaMailSender sender;
  private final MailProperties properties;

  public void send(final String filename) {
    log.info("Sending email...");

    final SimpleMailMessage mail = new SimpleMailMessage();
    mail.setFrom(properties.getFromEmail());
    mail.setTo(properties.getToEmail());
    mail.setSubject("New file on SFTP!");
    mail.setText("Filename: " + filename);

    try {
      sender.send(mail);
      log.info("Email done!");
    } catch (final Exception ex) {
      log.error("Failed to send email: {}", ex.getMessage());
    }
  }
}
