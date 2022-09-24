package com.yuriytkach.demo.stream16;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.yuriytkach.demo.stream16.model.ExcelReadResult;
import com.yuriytkach.demo.stream16.model.ExcelRecord;

class ExcelReaderTest {

  private final ExcelReader tested = new ExcelReader();

  @Test
  void shouldReadExcel() throws URISyntaxException, IOException {
    final URL resource = getClass().getClassLoader().getResource("cool-excel.xlsx");
    final File file = new File(resource.toURI());

    try(FileInputStream inputStream = new FileInputStream(file)) {
      final ExcelReadResult result = tested.read(inputStream);

      assertThat(result.failedRows()).isEqualTo(2);
      assertThat(result.records()).containsExactlyInAnyOrder(
        new ExcelRecord("hello", "world"),
        new ExcelRecord("Slava", "Ukraini"),
        new ExcelRecord("Geroyam", "Slava")
      );
    }
  }

}
