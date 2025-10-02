package com.yunit.stt_performance_test.controller;

import com.yunit.stt_performance_test.dto.CerResultDto;
import com.yunit.stt_performance_test.service.CerCalculatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.yunit.stt_performance_test.service.FileStorageService;
import com.yunit.stt_performance_test.service.SttService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class UploadController {

    private final FileStorageService fileStorageService;
    private final SttService sttService;
    private final CerCalculatorService cerCalculatorService;

    public UploadController(FileStorageService fileStorageService, SttService sttService, CerCalculatorService cerCalculatorService) {
        this.fileStorageService = fileStorageService;
        this.sttService = sttService;
        this.cerCalculatorService = cerCalculatorService;
    }

    @GetMapping("/")
    public String showUploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("files") MultipartFile[] files,
                                   Model model) {

        if (files == null || files.length == 0 || files[0].isEmpty()) {
            model.addAttribute("message", "업로드할 파일을 선택해주세요.");
            return "upload";
        }

        // 1. 파일 저장
        List<String> storedFileNames = fileStorageService.storeFiles(files);

        if (storedFileNames.isEmpty()) {
            model.addAttribute("message", "파일 저장에 실패했습니다.");
            return "upload";
        }

        log.info("Stored files: {}", String.join(", ", storedFileNames));

        // 2. 음성 파일과 텍스트 파일 매칭 및 CER 계산
        Map<String, MultipartFile> fileMap = new HashMap<>();
        for (MultipartFile file : files) {
            if (file.getOriginalFilename() != null) {
                fileMap.put(file.getOriginalFilename(), file);
            }
        }

        List<CerResultDto> cerResults = new ArrayList<>(); // CER 결과 리스트

        for (MultipartFile audioFile : files) {
            if (audioFile.getOriginalFilename() != null && (audioFile.getOriginalFilename().endsWith(".wav") || audioFile.getOriginalFilename().endsWith(".mp3"))) {
                String baseName = getBaseName(audioFile.getOriginalFilename());
                MultipartFile referenceTextFile = fileMap.get(baseName + ".txt");

                if (referenceTextFile != null) {
                    try {
                        String referenceText = new String(referenceTextFile.getBytes(), StandardCharsets.UTF_8);
                        String hypothesisText = sttService.convertSpeechToText(audioFile);

                        double cerModeA = cerCalculatorService.calculateCerModeA(referenceText, hypothesisText);
                        double cerModeB = cerCalculatorService.calculateCerModeB(referenceText, hypothesisText);

                        log.info("--- CER Result for Audio: {} ---", audioFile.getOriginalFilename());
                        log.info("Reference: {}", referenceText);
                        log.info("Hypothesis: {}", hypothesisText);
                        log.info("CER (Mode A - with whitespace): {}", String.format("%.4f", cerModeA));
                        log.info("CER (Mode B - without whitespace): {}", String.format("%.4f", cerModeB));
                        log.info("------------------------------------");

                        cerResults.add(new CerResultDto(
                                audioFile.getOriginalFilename(),
                                referenceText,
                                hypothesisText,
                                cerModeA,
                                cerModeB,
                                null // No error
                        ));

                    } catch (IOException e) {
                        log.error("Error reading reference text file for {}: {}", audioFile.getOriginalFilename(), e.getMessage());
                        cerResults.add(new CerResultDto(
                                audioFile.getOriginalFilename(),
                                null, null, 0.0, 0.0,
                                "참조 텍스트 파일 읽기 오류: " + e.getMessage()
                        ));
                    }
                } else {
                    log.warn("No matching .txt file found for audio file: {}", audioFile.getOriginalFilename());
                    cerResults.add(new CerResultDto(
                            audioFile.getOriginalFilename(),
                            null, null, 0.0, 0.0,
                            "매칭되는 .txt 파일 없음"
                    ));
                }
            }
        }

        model.addAttribute("message",
                "파일이 성공적으로 업로드되었고 CER 계산이 완료되었습니다.");
        model.addAttribute("cerResults", cerResults); // CER 결과 리스트 전달

        return "upload";
    }

    private String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}