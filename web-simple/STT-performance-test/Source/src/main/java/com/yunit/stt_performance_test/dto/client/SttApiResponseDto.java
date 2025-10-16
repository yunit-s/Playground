package com.yunit.stt_performance_test.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SttApiResponseDto {

    private int segment;
    private String transcript;
    private List<WordAlignmentDto> wordAlignment;
    private double segmentOffset;
    private double segmentDuration;
    private String recvTimestamp;
    private String sendTimestamp;
    private boolean final_;

    @JsonProperty("SessionID")
    private String SessionID;

    private String filename;
    private String sessionID;
    private String customerRecords;
    private String serviceType;

    @JsonProperty("SourceIP")
    private String SourceIP;

    // 'final' is a Java keyword, so we need to map it to a different field name.
    // The JSON response has a field named "final", so we map it to "final_".
    @JsonProperty("final")
    public void setFinal(boolean finalValue) {
        this.final_ = finalValue;
    }
}
