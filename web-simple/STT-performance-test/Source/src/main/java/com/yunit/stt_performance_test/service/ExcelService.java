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

            // 헤더 생성
            String[] headers = {"파일", "참조 텍스트", "가설 텍스트", "CER", "오류"};
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

//                Cell cellModeA = row.createCell(3);
//                cellModeA.setCellValue(result.getCerModeA());
//                cellModeA.setCellStyle(numericStyle);

                Cell cellModeB = row.createCell(3);
                cellModeB.setCellValue(result.getCerModeB());
                cellModeB.setCellStyle(numericStyle);

                row.createCell(4).setCellValue(result.getErrorMessage());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
