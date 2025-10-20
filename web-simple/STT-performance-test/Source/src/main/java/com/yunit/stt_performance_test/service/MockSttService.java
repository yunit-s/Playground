package com.yunit.stt_performance_test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.context.annotation.Profile;

@Slf4j
@Service
@Profile("mock")
public class MockSttService implements SttService {

    @Override
    public String convertSpeechToText(MultipartFile audioFile) {
        log.info("Mock STT Service: Converting audio file '{}' to text.", audioFile.getOriginalFilename());
        // 실제 STT API 연동 대신 가상의 텍스트를 반환합니다.
        // 나중에 실제 STT API 클라이언트로 교체될 예정입니다.
        return "안녕하세요. 테스트 음성입니다. STT 변환 결과입니다.";
    }
}
