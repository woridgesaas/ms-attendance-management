package org.woridge.ms_attendance_management.integration.holiday;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

/**
 * Client responsible for communication with Internal Users microservice.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayClient {

    private final WebClient webClient;

    private static final String BASE_URL = "http://localhost:8081/api/holidays/check";

    /**
     * Check if a given date is a holiday.
     * Cached for performance optimization.
     */
    @Cacheable("holidays")
    public boolean isHoliday(LocalDate date) {

        try {
            Boolean response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(BASE_URL)
                            .queryParam("date", date)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(response);

        } catch (Exception e) {
            log.error("Error calling Holiday service for date {}: {}", date, e.getMessage());
            return false; // fallback safe
        }
    }
}