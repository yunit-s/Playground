package com.yunit.stt_performance_test.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.yunit.stt_performance_test.service.FileStorageService;
import com.yunit.stt_performance_test.service.SttService;

import java.util.List;

@Slf4j
@Controller
public class UploadController {

    private final FileStorageService fileStorageService;
    private final SttService sttService;

    public UploadController(FileStorageService fileStorageService, SttService sttService) {
        this.fileStorageService = fileStorageService;
        this.sttService = sttService;
    }

    @GetMapping("/")
    public String showUploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("files") MultipartFile[] files,
                                   RedirectAttributes redirectAttributes) {

        if (files == null || files.length == 0 || files[0].isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "업로드할 파일을 선택해주세요.");
            return "redirect:/";
        }

        List<String> storedFileNames = fileStorageService.storeFiles(files);

        if (storedFileNames.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "파일 저장에 실패했습니다.");
            return "redirect:/";
        }

        log.info("Stored files: {}", String.join(", ", storedFileNames));

        // STT API 연동 및 결과 반환 로직 추가
        for (MultipartFile file : files) {
            if (file.getOriginalFilename() != null && (file.getOriginalFilename().endsWith(".wav") || file.getOriginalFilename().endsWith(".mp3"))) {
                String sttResult = sttService.convertSpeechToText(file);
                log.info("STT Result for {}: {}", file.getOriginalFilename(), sttResult);
                // TODO: 이 결과를 화면에 표시하거나 CER 계산에 사용
            }
        }

        redirectAttributes.addFlashAttribute("message",
                "파일이 성공적으로 업로드되었고 STT 변환이 시도되었습니다: " + String.join(", ", storedFileNames));

        return "redirect:/";
    }
}
