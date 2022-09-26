package com.yuriytkach.demo.stream16;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.yuriytkach.demo.stream16.model.ExcelReadResult;
import com.yuriytkach.demo.stream16.model.ExcelRecord;

import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Service
public class ExcelReader {

  public ExcelReadResult read(final InputStream inputStream) throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
      final XSSFSheet sheet0 = workbook.getSheetAt(0);

      log.debug("Parsing excel sheet: {}", sheet0.getSheetName());

      final int totalRows = sheet0.getLastRowNum();

      final List<ExcelRecord> excelRecords = StreamEx.of(sheet0.rowIterator())
        .skip(1) //header row
        .map(this::extractRow)
        .flatMap(Optional::stream)
        .filter(ExcelRecord::isValid)
        .toImmutableList();

      log.debug("Parsed rows {} of {}", excelRecords.size(), totalRows);

      return new ExcelReadResult(excelRecords, totalRows - excelRecords.size());
    }
  }

  private Optional<ExcelRecord> extractRow(final Row row) {
    try {
      return Optional.of(new ExcelRecord(
        row.getCell(0).getStringCellValue(),
        row.getCell(1).getStringCellValue()
      ));
    } catch (final Exception ex) {
      log.debug("Invalid row {}: {}", row.getRowNum(), ex.getMessage());
      return Optional.empty();
    }
  }
}
