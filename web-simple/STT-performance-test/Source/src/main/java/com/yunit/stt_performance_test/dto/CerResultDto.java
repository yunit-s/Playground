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

    // Mode A results (includes whitespace)
    private double cerModeA;
    private int substitutionsModeA;
    private int deletionsModeA;
    private int insertionsModeA;
    private int referenceLengthModeA; // N for Mode A

    // Mode B results (ignores whitespace)
    private double cerModeB;
    private int substitutionsModeB;
    private int deletionsModeB;
    private int insertionsModeB;
    private int referenceLengthModeB; // N for Mode B

    private String errorMessage; // 에러 발생 시 메시지
}
