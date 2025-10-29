package com.yunit.stt_performance_test.controller;

import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${server.tomcat.max-part-count}")
    private int maxFileCount;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "파일 크기가 너무 큽니다. " + maxFileSize + " 이하의 파일을 업로드해주세요.");
        return "redirect:/";
    }

    @ExceptionHandler(FileCountLimitExceededException.class)
    public String handleFileCountLimitException(FileCountLimitExceededException exc, RedirectAttributes redirectAttributes) {
        int maxFileCountTemp = maxFileCount - 1;
        redirectAttributes.addFlashAttribute("message", "한 번에 업로드할 수 있는 파일 개수를 초과했습니다. " + maxFileCountTemp + "개 이하의 파일을 업로드해주세요.");
        return "redirect:/";
    }
}
