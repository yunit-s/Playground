package com.yunit.stt_performance_test.controller;

import com.yunit.stt_performance_test.dto.CerResultDto;
import com.yunit.stt_performance_test.dto.DetailedEditDistance;
import com.yunit.stt_performance_test.dto.TotalCerResultDto;
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

        Map<String, MultipartFile> fileMap = new HashMap<>();
        for (MultipartFile file : files) {
            if (file.getOriginalFilename() != null) {
                fileMap.put(file.getOriginalFilename(), file);
            }
        }

        List<CerResultDto> cerResults = new ArrayList<>();

        for (MultipartFile audioFile : files) {
            if (audioFile.getOriginalFilename() != null && audioFile.getOriginalFilename().endsWith(".wav")) {
                String baseName = getBaseName(audioFile.getOriginalFilename());
                MultipartFile referenceTextFile = fileMap.get(baseName + ".txt");

                if (referenceTextFile != null) {
                    try {
                        String referenceText = new String(referenceTextFile.getBytes(), StandardCharsets.UTF_8);
                        String hypothesisText = sttService.convertSpeechToText(audioFile);
                        log.info("audioFileName={}, hypothesisText={}", audioFile.getOriginalFilename(), hypothesisText);

                        // Mode A (with whitespace)
                        DetailedEditDistance detailsA = cerCalculatorService.calculateDetailedEditDistance(referenceText, hypothesisText);
                        int refLengthA = referenceText.length();
                        double cerModeA = (refLengthA == 0) ? (hypothesisText.length() > 0 ? 1.0 : 0.0) : (double) detailsA.getTotalDistance() / refLengthA;

                        // Mode B (without whitespace)
                        String cleanedReference = referenceText.replaceAll("\\s", "");
                        String cleanedHypothesis = hypothesisText.replaceAll("\\s", "");
                        DetailedEditDistance detailsB = cerCalculatorService.calculateDetailedEditDistance(cleanedReference, cleanedHypothesis);
                        int refLengthB = cleanedReference.length();
                        double cerModeB = (refLengthB == 0) ? (cleanedHypothesis.length() > 0 ? 1.0 : 0.0) : (double) detailsB.getTotalDistance() / refLengthB;

                        CerResultDto resultDto = new CerResultDto();
                        resultDto.setOriginalFileName(audioFile.getOriginalFilename());
                        resultDto.setReferenceText(referenceText);
                        resultDto.setHypothesisText(hypothesisText);
                        resultDto.setCerModeA(cerModeA);
                        resultDto.setSubstitutionsModeA(detailsA.getSubstitutions());
                        resultDto.setDeletionsModeA(detailsA.getDeletions());
                        resultDto.setInsertionsModeA(detailsA.getInsertions());
                        resultDto.setReferenceLengthModeA(refLengthA);
                        resultDto.setCerModeB(cerModeB);
                        resultDto.setSubstitutionsModeB(detailsB.getSubstitutions());
                        resultDto.setDeletionsModeB(detailsB.getDeletions());
                        resultDto.setInsertionsModeB(detailsB.getInsertions());
                        resultDto.setReferenceLengthModeB(refLengthB);

                        cerResults.add(resultDto);

                    } catch (IOException e) {
                        log.error("Error processing file {}: {}", audioFile.getOriginalFilename(), e.getMessage());
                        CerResultDto errorDto = new CerResultDto();
                        errorDto.setOriginalFileName(audioFile.getOriginalFilename());
                        errorDto.setErrorMessage("파일 처리 오류");
                        cerResults.add(errorDto);
                    }
                } else {
                    log.warn("No matching .txt file found for audio file: {}", audioFile.getOriginalFilename());
                    CerResultDto errorDto = new CerResultDto();
                    errorDto.setOriginalFileName(audioFile.getOriginalFilename());
                    errorDto.setErrorMessage("매칭되는 .txt 파일 없음");
                    cerResults.add(errorDto);
                }
            }
        }

        // Calculate total result
        StringBuilder totalReference = new StringBuilder();
        StringBuilder totalHypothesis = new StringBuilder();
        for (CerResultDto result : cerResults) {
            if (result.getReferenceText() != null && result.getHypothesisText() != null) {
                totalReference.append(result.getReferenceText());
                totalHypothesis.append(result.getHypothesisText());
            }
        }

        TotalCerResultDto totalCerResult = null;
        if (totalReference.length() > 0) {
            DetailedEditDistance totalDetails = cerCalculatorService.calculateDetailedEditDistance(totalReference.toString(), totalHypothesis.toString());
            int totalRefLength = totalReference.length();
            double totalCer = (totalRefLength == 0) ? 0.0 : (double) totalDetails.getTotalDistance() / totalRefLength;
            totalCerResult = new TotalCerResultDto(totalCer, totalDetails.getSubstitutions(), totalDetails.getDeletions(), totalDetails.getInsertions(), totalRefLength);
        }

        redirectAttributes.addFlashAttribute("message", "STT가 성공적으로 실행되었습니다.");
        redirectAttributes.addFlashAttribute("cerResults", cerResults);
        redirectAttributes.addFlashAttribute("totalCerResult", totalCerResult);
        session.setAttribute("cerResults", cerResults);
        session.setAttribute("totalCerResult", totalCerResult);

        return "redirect:/";
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadExcel(HttpSession session, HttpServletResponse response) throws IOException {
        List<CerResultDto> cerResults = (List<CerResultDto>) session.getAttribute("cerResults");
        TotalCerResultDto totalCerResult = (TotalCerResultDto) session.getAttribute("totalCerResult");

        if (cerResults == null || cerResults.isEmpty()) {
            // 결과가 없을 때, 홈페이지로 리다이렉트
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/").build();
        }

        ByteArrayInputStream in = excelService.createExcelFile(cerResults, totalCerResult);

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