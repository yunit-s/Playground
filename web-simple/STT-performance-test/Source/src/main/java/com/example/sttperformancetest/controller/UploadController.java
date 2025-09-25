package com.example.sttperformancetest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class UploadController {

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

        String uploadedFileNames = Arrays.stream(files)
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.joining(", "));

        log.info("Uploaded files: {}", uploadedFileNames);

        // TODO: 파일 저장 및 CER 계산 로직 추가

        redirectAttributes.addFlashAttribute("message",
                "파일이 성공적으로 업로드되었습니다: " + uploadedFileNames);

        return "redirect:/";
    }
}
