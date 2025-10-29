package com.yunit.stt_performance_test.service;

import com.yunit.stt_performance_test.dto.CerResultDto;
import org.apache.poi.ss.usermodel.*;
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

            // Create a cell style for numeric formatting
            CellStyle numericStyle = workbook.createCellStyle();
            numericStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("0.0000"));

            // Header
            String[] headers = {
                "파일", "참조 텍스트", "가설 텍스트",
                "CER(A)", "S(A)", "D(A)", "I(A)", "N(A)",
                "CER(B)", "S(B)", "D(B)", "I(B)", "N(B)",
                "오류"
            };
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
            }

            // Data
            int rowIdx = 1;
            for (CerResultDto result : cerResults) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(result.getOriginalFileName());
                row.createCell(1).setCellValue(result.getReferenceText());
                row.createCell(2).setCellValue(result.getHypothesisText());

                // Mode A
                Cell cerA = row.createCell(3);
                cerA.setCellValue(result.getCerModeA());
                cerA.setCellStyle(numericStyle);
                row.createCell(4).setCellValue(result.getSubstitutionsModeA());
                row.createCell(5).setCellValue(result.getDeletionsModeA());
                row.createCell(6).setCellValue(result.getInsertionsModeA());
                row.createCell(7).setCellValue(result.getReferenceLengthModeA());

                // Mode B
                Cell cerB = row.createCell(8);
                cerB.setCellValue(result.getCerModeB());
                cerB.setCellStyle(numericStyle);
                row.createCell(9).setCellValue(result.getSubstitutionsModeB());
                row.createCell(10).setCellValue(result.getDeletionsModeB());
                row.createCell(11).setCellValue(result.getInsertionsModeB());
                row.createCell(12).setCellValue(result.getReferenceLengthModeB());

                row.createCell(13).setCellValue(result.getErrorMessage());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
