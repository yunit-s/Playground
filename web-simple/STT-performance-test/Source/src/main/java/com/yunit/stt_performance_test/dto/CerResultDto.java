package com.yunit.stt_performance_test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CerResultDto {
    private String originalFileName;
    private String referenceText;
    private String hypothesisText;
    private double cerModeA;
    private double cerModeB;
    private String errorMessage; // 에러 발생 시 메시지
}
