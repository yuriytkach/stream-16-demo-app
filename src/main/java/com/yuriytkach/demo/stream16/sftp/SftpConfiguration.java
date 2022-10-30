package com.yuriytkach.demo.stream16.sftp;

import java.io.File;
import java.security.PublicKey;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

@Configuration
public class SftpConfiguration {

  @Bean
  SSHClient sshClient() {
    final SSHClient client = new SSHClient();
    client.addHostKeyVerifier(new AllowAllHosts());
    return client;
  }

  @Bean
  OpenSSHKeyFile keyFile(final SftpProperties properties) {
    final OpenSSHKeyFile file = new OpenSSHKeyFile();
    file.init(new File(properties.getPrivateKeyFile()), new PasswordFinder() {
      @Override
      public char[] reqPassword(final Resource<?> resource) {
        return properties.getPrivateKeyPassword().toCharArray();
      }

      @Override
      public boolean shouldRetry(final Resource<?> resource) {
        return false;
      }
    });
    return file;
  }

  public static class AllowAllHosts implements HostKeyVerifier {

    @Override
    public boolean verify(final String hostname, final int port, final PublicKey key) {
      return true;
    }

    @Override
    public List<String> findExistingAlgorithms(final String hostname, final int port) {
      return List.of();
    }
  }
}
