package com.yunit.stt_performance_test.service;

import org.springframework.web.multipart.MultipartFile;

public interface SttService {
    String convertSpeechToText(MultipartFile audioFile);
}
