package com.yunit.stt_performance_test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalCerResultDto {
    private double totalCer;
    private int totalSubstitutions;
    private int totalDeletions;
    private int totalInsertions;
    private int totalReferenceLength;
}
