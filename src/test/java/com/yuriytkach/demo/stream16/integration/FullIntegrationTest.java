package com.yuriytkach.demo.stream16.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.wait.strategy.Wait.forHttp;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.yuriytkach.demo.stream16.Controller;
import com.yuriytkach.demo.stream16.email.MailProperties;
import com.yuriytkach.demo.stream16.model.ExcelRecord;
import com.yuriytkach.demo.stream16.sftp.SftpConfiguration;
import com.yuriytkach.demo.stream16.sftp.SftpProperties;

import io.restassured.RestAssured;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullIntegrationTest {

  private static final int CONSUMER_PORT = 8080;
  @Container
  private static final GenericContainer<?> WIREMOCK_CONTAINER = new GenericContainer<>("rodolpheche/wiremock:latest")
    .waitingFor(forHttp("/__admin"))
    .withExposedPorts(CONSUMER_PORT);

  @Container
  private static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(
    DockerImageName.parse("localstack/localstack")
  )
    .withServices(S3);

  @Container
  private static final GenericContainer<?> SFTP_CONTAINER = new GenericContainer<>("rastasheep/ubuntu-sshd:14.04")
    .withExposedPorts(22);
  private static final String COOL_EXCEL_XLSX = "cool-excel.xlsx";

  @DynamicPropertySource
  static void wiremockProperties(final DynamicPropertyRegistry registry) {
    registry.add("app.consumer-base-path", () ->
      "http://" + WIREMOCK_CONTAINER.getHost() + ":" + WIREMOCK_CONTAINER.getMappedPort(CONSUMER_PORT));

    registry.add("app.sftp.host", SFTP_CONTAINER::getHost);
    registry.add("app.sftp.port", SFTP_CONTAINER::getFirstMappedPort);

    final URL priKey = FullIntegrationTest.class.getClassLoader().getResource("ssh-private.key");
    registry.add("app.sftp.private-key-file", () -> priKey.getFile());
  }

  @LocalServerPort
  int localPort;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  S3Client s3Client;

  @Autowired
  SftpProperties sftpProperties;

  @Autowired
  OpenSSHKeyFile sshKeyFile;

  @Autowired
  MailProperties mailProperties;

  private GreenMail greenMail;

  @BeforeAll
  static void setUpWireMock() {
    final Logger log = LoggerFactory.getLogger(FullIntegrationTest.class);
    log.info(WIREMOCK_CONTAINER.getLogs());

    final Integer mappedPort = WIREMOCK_CONTAINER.getMappedPort(CONSUMER_PORT);
    WireMock.configureFor(WIREMOCK_CONTAINER.getHost(), mappedPort);

    log.info("http://" + WIREMOCK_CONTAINER.getHost() + ":" + mappedPort);
  }

  @BeforeAll
  static void setUpSftp() throws Exception {
    SFTP_CONTAINER.copyFileToContainer(
      MountableFile.forClasspathResource("ssh-private.key.pub"),
      "/root/.ssh/authorized_keys"
    );
    SFTP_CONTAINER.execInContainer("chown", "root:root", "/root/.ssh/authorized_keys");
    SFTP_CONTAINER.execInContainer("mkdir", "/root/inbox");
  }

  @BeforeEach
  void setupGreenMail() {
    greenMail = new GreenMail(new ServerSetup(
      mailProperties.getPort(),
      mailProperties.getHost(),
      "smtp"
    ));
    greenMail.setUser(mailProperties.getUsername(), mailProperties.getPassword());
    greenMail.start();
  }

  @AfterEach
  void stopGreenMail() {
    greenMail.stop();
  }

  @BeforeEach
  void setupRestAssured() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = localPort;
  }

  @Test
  void shouldSaveItem() throws Exception {
    WireMock.stubFor(
      WireMock.post(WireMock.urlPathEqualTo("/records")).willReturn(
        ResponseDefinitionBuilder.responseDefinition().withStatus(200).withBody("Yay!")
      )
    );

    final URL resource = getClass().getClassLoader().getResource(COOL_EXCEL_XLSX);
    final byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));

    RestAssured.given()
      .accept(MediaType.APPLICATION_JSON_VALUE)
      .contentType(Controller.EXCEL_MEDIA_TYPE)
      .multiPart("file", COOL_EXCEL_XLSX, bytes)
      .when()
      .post("/excel")
      .then()
      .statusCode(HttpStatus.ACCEPTED.value())
      .body("loadedRecords", Matchers.equalTo(3))
      .body("failedRecord", Matchers.equalTo(2));

    WireMock.verify(
      WireMock.postRequestedFor(WireMock.urlPathEqualTo("/records"))
        .withRequestBody(new EqualToJsonPattern(
          objectMapper.writeValueAsString(List.of(
            new ExcelRecord("hello", "world"),
            new ExcelRecord("Slava", "Ukraini"),
            new ExcelRecord("Geroyam", "Slava")
          )),
          true, // ignore order
          false // fail on extra
        ))
    );

    final ResponseInputStream<GetObjectResponse> response = s3Client.getObject(GetObjectRequest.builder()
      .bucket("my-bucket")
      .key(COOL_EXCEL_XLSX)
      .build());

    assertThat(response.readAllBytes()).isEqualTo(bytes);

    // ====== assert SFTP ======
    try (SSHClient sshClient= new SSHClient()) {
      sshClient.addHostKeyVerifier(new SftpConfiguration.AllowAllHosts());
      sshClient.connect(sftpProperties.getHost(), sftpProperties.getPort());
      sshClient.authPublickey(sftpProperties.getUsername(), sshKeyFile);

      try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
        final List<RemoteResourceInfo> ls = sftpClient.ls(sftpProperties.getFolder());
        final List<String> filesOnRemote = ls.stream().map(RemoteResourceInfo::getName).toList();
        System.out.println(filesOnRemote);
        assertThat(filesOnRemote).contains(COOL_EXCEL_XLSX);
      }
    }

    // ====== verify mail =======

    assertThat(greenMail.waitForIncomingEmail(1)).isTrue();
    final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

    final MimeMessage message = receivedMessages[0];
    assertThat(message.getSubject()).isEqualTo("New file on SFTP!");
    final String body = GreenMailUtil.getBody(message);
    assertThat(body).isEqualTo("Filename: " + COOL_EXCEL_XLSX);
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    @Primary
    public S3Client s3ClientForTest() {
      final Region region = Region.of(LOCAL_STACK_CONTAINER.getRegion());
      final var client = S3Client
        .builder()
        .endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(LocalStackContainer.Service.S3))
        .credentialsProvider(
          StaticCredentialsProvider.create(
            AwsBasicCredentials.create(LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey())
          )
        )
        .region(region)
        .build();

      client.createBucket(CreateBucketRequest
        .builder()
        .bucket("my-bucket")
        .createBucketConfiguration(
          CreateBucketConfiguration.builder()
            .locationConstraint(region.id())
            .build())
        .build());
      System.out.println("Creating bucket...");
      client.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
        .bucket("my-bucket")
        .build());
      System.out.println("Bucket is ready.");

      return client;
    }

  }
}
