package com.yunit.stt_performance_test.service;

import com.yunit.stt_performance_test.dto.CerResultDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public ByteArrayInputStream createExcelFile(List<CerResultDto> cerResults) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("CER_Results");

            // 헤더 생성
            String[] headers = {"파일", "참조 텍스트", "가설 텍스트", "CER (Mode A)", "CER (Mode B)", "오류"};
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
            }

            // 데이터 생성
            int rowIdx = 1;
            for (CerResultDto result : cerResults) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(result.getOriginalFileName());
                row.createCell(1).setCellValue(result.getReferenceText());
                row.createCell(2).setCellValue(result.getHypothesisText());
                row.createCell(3).setCellValue(String.format("%.4f", result.getCerModeA()));
                row.createCell(4).setCellValue(String.format("%.4f", result.getCerModeB()));
                row.createCell(5).setCellValue(result.getErrorMessage());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
