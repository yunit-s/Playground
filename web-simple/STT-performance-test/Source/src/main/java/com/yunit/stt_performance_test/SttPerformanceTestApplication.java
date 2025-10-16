package com.yunit.stt_performance_test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SttPerformanceTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SttPerformanceTestApplication.class, args);
	}

}
