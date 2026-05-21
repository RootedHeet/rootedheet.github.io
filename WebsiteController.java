package com.webcraft.controller;

import com.webcraft.dto.WebsiteGenerateRequest;
import com.webcraft.dto.WebsiteGenerateResponse;
import com.webcraft.service.WebsiteGenerationService;
import com.webcraft.service.WebsiteStorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class WebsiteController {

    private final WebsiteGenerationService generationService;
    private final WebsiteStorageService storageService;

    public WebsiteController(WebsiteGenerationService generationService,
                              WebsiteStorageService storageService) {
        this.generationService = generationService;
        this.storageService = storageService;
    }

    @PostMapping("/generate")
    public ResponseEntity<WebsiteGenerateResponse> generateWebsite(
            @Valid @RequestBody WebsiteGenerateRequest request) {
        WebsiteGenerateResponse response = generationService.generateWebsite(request);
        if ("error".equals(response.getStatus())) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preview/{websiteId}")
    public ResponseEntity<WebsiteGenerateResponse> getWebsite(@PathVariable String websiteId) {
        return generationService.getWebsite(websiteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "WebCraft API",
            "version", "1.0.0",
            "totalWebsites", storageService.totalCount()
        ));
    }
}
