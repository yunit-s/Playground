package com.yunit.stt_performance_test.controller;

import com.yunit.stt_performance_test.dto.CerResultDto;
import com.yunit.stt_performance_test.service.CerCalculatorService;
import com.yunit.stt_performance_test.service.ExcelService;
import com.yunit.stt_performance_test.service.FileStorageService;
import com.yunit.stt_performance_test.service.SttService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
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
    private final ExcelService excelService;

    public UploadController(FileStorageService fileStorageService, SttService sttService, CerCalculatorService cerCalculatorService, ExcelService excelService) {
        this.fileStorageService = fileStorageService;
        this.sttService = sttService;
        this.cerCalculatorService = cerCalculatorService;
        this.excelService = excelService;
    }

    @GetMapping("/")
    public String showUploadForm(Model model) {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("files") MultipartFile[] files,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

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

        Map<String, MultipartFile> fileMap = new HashMap<>();
        for (MultipartFile file : files) {
            if (file.getOriginalFilename() != null) {
                fileMap.put(file.getOriginalFilename(), file);
            }
        }

        List<CerResultDto> cerResults = new ArrayList<>();

        for (MultipartFile audioFile : files) {
            if (audioFile.getOriginalFilename() != null && (audioFile.getOriginalFilename().endsWith(".wav") || audioFile.getOriginalFilename().endsWith(".mp3"))) {
                String baseName = getBaseName(audioFile.getOriginalFilename());
                MultipartFile referenceTextFile = fileMap.get(baseName + ".txt");

                if (referenceTextFile != null) {
                    try {
                        String referenceText = new String(referenceTextFile.getBytes(), StandardCharsets.UTF_8);
                        String hypothesisText = sttService.convertSpeechToText(audioFile);
                        log.info("audioFileName={}, hypothesisText={}", audioFile.getOriginalFilename(), hypothesisText);
                        double cerModeA = cerCalculatorService.calculateCerModeA(referenceText, hypothesisText);
                        double cerModeB = cerCalculatorService.calculateCerModeB(referenceText, hypothesisText);

                        cerResults.add(new CerResultDto(
                                audioFile.getOriginalFilename(), referenceText, hypothesisText, cerModeA, cerModeB, null));

                    } catch (IOException e) {
                        log.error("Error processing file {}: {}", audioFile.getOriginalFilename(), e.getMessage());
                        cerResults.add(new CerResultDto(
                                audioFile.getOriginalFilename(), null, null, 0.0, 0.0, "파일 처리 오류"));
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

        redirectAttributes.addFlashAttribute("message", "파일이 성공적으로 업로드되었고 CER 계산이 완료되었습니다.");
        redirectAttributes.addFlashAttribute("cerResults", cerResults);
        session.setAttribute("cerResults", cerResults);

        return "redirect:/";
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadExcel(HttpSession session, HttpServletResponse response) throws IOException {
        List<CerResultDto> cerResults = (List<CerResultDto>) session.getAttribute("cerResults");

        if (cerResults == null || cerResults.isEmpty()) {
            // 결과가 없을 때, 홈페이지로 리다이렉트
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/").build();
        }

        ByteArrayInputStream in = excelService.createExcelFile(cerResults);

        // 파일명에 현재 시간 추가 (yyyyMMdd_HHmmssSSS 형식)
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new java.util.Date());
        String fileName = "cer-results_" + timeStamp + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    private String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}