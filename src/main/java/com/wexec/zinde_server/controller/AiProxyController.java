package com.wexec.zinde_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @PostMapping("/ask-question")
    public ResponseEntity<String> askQuestion(@RequestBody String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServiceUrl + "/ask-question", entity, String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (Exception e) {
            log.error("AI ask-question proxy error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"error\",\"detail\":\"AI servisi yanıt vermedi\"}");
        }
    }

    @GetMapping("/coaches")
    public ResponseEntity<String> getCoaches() {
        try {
            String response = restTemplate.getForObject(
                    aiServiceUrl + "/coaches", String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            log.error("AI coaches proxy error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"error\",\"detail\":\"AI servisi yanıt vermedi\"}");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            String response = restTemplate.getForObject(
                    aiServiceUrl + "/health", String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"ai_service_down\",\"detail\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncDatabase() {
        try {
            HttpEntity<String> entity = new HttpEntity<>("");
            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServiceUrl + "/sync-database-to-ai", entity, String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (Exception e) {
            log.error("AI sync proxy error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":\"error\",\"detail\":\"Sync başarısız: " + e.getMessage() + "\"}");
        }
    }
}
