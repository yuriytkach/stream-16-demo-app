package com.yuriytkach.demo.stream16;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.yuriytkach.demo.stream16.model.ExcelReadResult;
import com.yuriytkach.demo.stream16.model.ExcelRecord;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@WebMvcTest(controllers = Controller.class)
class ControllerTest {

  private static final String EXCEL_FILE = "cool-excel.xlsx";
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ExcelReader excelReaderMock;

  @MockBean
  private RecordsSender recordsSenderMock;

  @MockBean
  private AwsS3Saver awsS3SaverMock;

  @Test
  void shouldAcceptFile() throws IOException, URISyntaxException {
    when(excelReaderMock.read(any())).thenReturn(
      new ExcelReadResult(
        List.of(new ExcelRecord("ptn", "pnh")),
        42
      )
    );

    final URL resource = getClass().getClassLoader().getResource(EXCEL_FILE);
    final byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
    RestAssuredMockMvc.given()
      .mockMvc(mockMvc)
      .accept(MediaType.APPLICATION_JSON_VALUE)
      .contentType(Controller.EXCEL_MEDIA_TYPE)
      .multiPart("file", EXCEL_FILE, bytes)
      .when()
      .post("/excel")
      .then()
      .statusCode(HttpStatus.ACCEPTED.value())
      .body("loadedRecords", Matchers.equalTo(1))
      .body("failedRecord", Matchers.equalTo(42));

    verify(recordsSenderMock).send(List.of(new ExcelRecord("ptn", "pnh")));
    verify(awsS3SaverMock).save(EXCEL_FILE, bytes);
  }

}
