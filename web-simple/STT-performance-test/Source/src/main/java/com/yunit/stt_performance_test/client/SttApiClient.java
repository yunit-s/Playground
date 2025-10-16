package com.yunit.stt_performance_test.client;

import com.yunit.stt_performance_test.dto.client.SttApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "stt-api", url = "http://10.200.1.22:55081")
public interface SttApiClient {

    @PostMapping(value = "/client/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    SttApiResponseDto convertSpeechToText(
            @RequestPart("file") MultipartFile file,
            @RequestPart("customerRecords") String customerRecords,
            @RequestPart("timeout") Integer timeout
    );
}
