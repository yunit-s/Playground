package com.yunit.stt_performance_test.service;

import com.yunit.stt_performance_test.client.SttApiClient;
import com.yunit.stt_performance_test.dto.client.SttApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Profile("!mock")
public class FeignSttService implements SttService {

    private final SttApiClient sttApiClient;
    private static final int TIMEOUT = 60;

    @Override
    public String convertSpeechToText(MultipartFile audioFile) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        SttApiResponseDto response = sttApiClient.convertSpeechToText(audioFile, today, TIMEOUT);

        return response.getTranscript();
    }
}
