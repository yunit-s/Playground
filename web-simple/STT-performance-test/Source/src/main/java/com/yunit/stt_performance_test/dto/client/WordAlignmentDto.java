package com.yunit.stt_performance_test.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WordAlignmentDto {
    private double offset;
    private double duration;
    private String word;
}
