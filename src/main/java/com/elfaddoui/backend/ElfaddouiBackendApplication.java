package com.elfaddoui.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElfaddouiBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElfaddouiBackendApplication.class, args);
    }
}
