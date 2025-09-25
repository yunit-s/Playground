package com.example.sttperformancetest.service;

import org.springframework.web.multipart.MultipartFile;

public interface SttService {
    String convertSpeechToText(MultipartFile audioFile);
}
