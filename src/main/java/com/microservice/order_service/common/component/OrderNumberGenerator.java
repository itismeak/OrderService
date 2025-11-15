package com.microservice.order_service.common.component;

import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Random;

@Service
public class OrderNumberGenerator {
    public  String generateOrderNumber() {
        // Last 2 digits of current year
        String year = String.valueOf(Year.now().getValue()).substring(2);

        // 4-digit random number
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000); // 1000-9999

        // Combine
        return "ORD" + year + randomNumber;
    }
}