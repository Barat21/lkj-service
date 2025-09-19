package com.example.vanrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VanRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(VanRentalApplication.class, args);
    }
}
