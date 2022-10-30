package com.yuriytkach.demo.stream16.sftp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.xfer.InMemorySourceFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpSender {

  private final SSHClient sshClient;
  private final SftpProperties properties;
  private final OpenSSHKeyFile sshKeyFile;

  public void send(final String filename, final byte[] bytes) {
    try {
      log.info("Connecting to sftp: {}:{}", properties.getHost(), properties.getPort());
      sshClient.connect(properties.getHost(), properties.getPort());

      log.info("SFTP auth for user: {}", properties.getUsername());
      sshClient.authPublickey(properties.getUsername(), sshKeyFile);

      try(SFTPClient sftpClient = sshClient.newSFTPClient()) {

        sftpClient.put(
          new InMemorySourceFile() {
            @Override
            public String getName() {
              return filename;
            }

            @Override
            public long getLength() {
              return bytes.length;
            }

            @Override
            public InputStream getInputStream() throws IOException {
              return new ByteArrayInputStream(bytes);
            }
          },
          properties.getFolder() + "/" + filename
        );

        log.info("File saved to SFTP!");
      }

    } catch (final IOException e) {
      log.error("Failed to send to SFTP: {}", e.getMessage(), e);
    } finally {
      try {
        sshClient.disconnect();
      } catch (IOException e) {
        log.error("Failed to disconnect: {}", e.getMessage());
      }
    }
  }
}
