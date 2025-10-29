package com.yunit.stt_performance_test.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailedEditDistance {
    private int substitutions;
    private int deletions;
    private int insertions;

    public int getTotalDistance() {
        return substitutions + deletions + insertions;
    }
}
