package com.webcraft.service;

import com.webcraft.dto.WebsiteGenerateRequest;
import com.webcraft.dto.WebsiteGenerateResponse;
import com.webcraft.model.Website;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WebsiteGenerationService {

    private static final Logger log = LoggerFactory.getLogger(WebsiteGenerationService.class);

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.api.url:https://api.anthropic.com/v1/messages}")
    private String aiApiUrl;

    private final RestTemplate restTemplate;
    private final WebsiteStorageService storageService;

    public WebsiteGenerationService(RestTemplate restTemplate,
                                     WebsiteStorageService storageService) {
        this.restTemplate = restTemplate;
        this.storageService = storageService;
    }

    /**
     * Generates a full website based on the user prompt and preferences.
     * Calls the AI API and returns structured HTML/CSS/JS.
     */
    public WebsiteGenerateResponse generateWebsite(WebsiteGenerateRequest request) {
        String websiteId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        log.info("Generating website [{}] for prompt: {}", websiteId, request.getPrompt());

        try {
            // 1. Build AI prompt
            String aiPrompt = buildAIPrompt(request);

            // 2. Call AI API
            String aiResponse = callAIApi(aiPrompt);

            // 3. Parse the response into HTML/CSS/JS sections
            Map<String, String> parsedSections = parseAIResponse(aiResponse);

            // 4. Persist to database
            Website website = new Website();
            website.setWebsiteId(websiteId);
            website.setPrompt(request.getPrompt());
            website.setStyle(request.getStyle());
            website.setColorTheme(request.getColorTheme());
            website.setIndustry(request.getIndustry());
            website.setHtmlContent(parsedSections.get("html"));
            website.setCssContent(parsedSections.get("css"));
            website.setJsContent(parsedSections.get("js"));
            website.setStatus("COMPLETE");
            storageService.save(website);

            // 5. Build response
            WebsiteGenerateResponse response = new WebsiteGenerateResponse();
            response.setWebsiteId(websiteId);
            response.setHtmlContent(parsedSections.get("html"));
            response.setCssContent(parsedSections.get("css"));
            response.setJsContent(parsedSections.get("js"));
            response.setPreviewUrl("/preview/" + websiteId);
            response.setStatus("complete");
            response.setMessage("Website generated successfully!");

            return response;

        } catch (Exception e) {
            log.error("Failed to generate website [{}]: {}", websiteId, e.getMessage(), e);

            WebsiteGenerateResponse errorResponse = new WebsiteGenerateResponse();
            errorResponse.setWebsiteId(websiteId);
            errorResponse.setStatus("error");
            errorResponse.setMessage("Generation failed. Please try again.");
            return errorResponse;
        }
    }

    /**
     * Retrieves a previously generated website by ID.
     */
    public Optional<WebsiteGenerateResponse> getWebsite(String websiteId) {
        return storageService.findByWebsiteId(websiteId).map(website -> {
            storageService.incrementViewCount(websiteId);

            WebsiteGenerateResponse response = new WebsiteGenerateResponse();
            response.setWebsiteId(website.getWebsiteId());
            response.setHtmlContent(website.getHtmlContent());
            response.setCssContent(website.getCssContent());
            response.setJsContent(website.getJsContent());
            response.setPreviewUrl("/preview/" + websiteId);
            response.setStatus("complete");
            return response;
        });
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private String buildAIPrompt(WebsiteGenerateRequest request) {
        String style = request.getStyle() != null ? request.getStyle() : "modern";
        String theme = request.getColorTheme() != null ? request.getColorTheme() : "dark";
        String industry = request.getIndustry() != null ? request.getIndustry() : "general";

        return String.format("""
            Generate a complete, production-quality single-page website.
            
            USER REQUEST: %s
            STYLE: %s
            COLOR THEME: %s
            INDUSTRY: %s
            
            Return ONLY valid JSON with exactly these keys:
            {
              "html": "<full HTML structure without <!DOCTYPE> and <html> tags>",
              "css": "<all CSS styles including animations and responsive rules>",
              "js": "<all JavaScript for interactivity>"
            }
            
            Requirements:
            - Modern, visually stunning design
            - Fully responsive (mobile-first)
            - Include hero section, features, CTA, and footer
            - Use CSS custom properties for colors
            - Add smooth animations and hover effects
            - No external dependencies; pure HTML/CSS/JS only
            """,
            request.getPrompt(), style, theme, industry);
    }

    private String callAIApi(String prompt) {
        if (aiApiKey == null || aiApiKey.isBlank()) {
            log.warn("AI API key not configured. Returning mock response.");
            return getMockAIResponse();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", aiApiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "claude-sonnet-4-20250514");
        body.put("max_tokens", 8192);
        body.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(aiApiUrl, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
            if (content != null && !content.isEmpty()) {
                return (String) content.get(0).get("text");
            }
        }

        throw new RuntimeException("AI API call failed or returned empty response");
    }

    private Map<String, String> parseAIResponse(String aiResponse) {
        // Extract JSON from the response (handle markdown code blocks if present)
        String jsonStr = aiResponse
            .replaceAll("```json\\s*", "")
            .replaceAll("```\\s*", "")
            .trim();

        // Simple extraction for the three keys (use Jackson in production)
        Map<String, String> result = new HashMap<>();
        result.put("html", extractJsonValue(jsonStr, "html"));
        result.put("css", extractJsonValue(jsonStr, "css"));
        result.put("js", extractJsonValue(jsonStr, "js"));
        return result;
    }

    private String extractJsonValue(String json, String key) {
        // In production, use ObjectMapper. This is a simplified extractor.
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return "";

        int colonIndex = json.indexOf(":", keyIndex);
        int valueStart = json.indexOf("\"", colonIndex) + 1;
        int valueEnd = findClosingQuote(json, valueStart);
        if (valueStart <= 0 || valueEnd <= 0) return "";

        return json.substring(valueStart, valueEnd)
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\'", "'");
    }

    private int findClosingQuote(String str, int start) {
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '"' && str.charAt(i - 1) != '\\') {
                return i;
            }
        }
        return -1;
    }

    private String getMockAIResponse() {
        return """
            {
              "html": "<section class='hero'><h1>Your Website</h1><p>AI Generated</p></section>",
              "css": ".hero { display:flex; flex-direction:column; align-items:center; padding:4rem; background:#0f0f0f; color:white; }",
              "js": "console.log('Website ready');"
            }
            """;
    }
}
