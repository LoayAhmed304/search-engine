package com.project.searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class searchengineApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application context
        SpringApplication.run(searchengineApplication.class, args);
        System.out.println("Hello my Search Engine!");
    }
}
