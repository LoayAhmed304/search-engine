package com.project.searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class searchengineApplication {

    public static void main(String[] args) {
        //  SpringApplication.run(searchengineApplication.class, args);
        System.out.println("Hello my Search Engine!");
        ApplicationContext context = SpringApplication.run(searchengineApplication.class, args);
    }
}
