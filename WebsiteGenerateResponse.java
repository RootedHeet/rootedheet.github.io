package com.webcraft.dto;

public class WebsiteGenerateResponse {

    private String websiteId;
    private String htmlContent;
    private String cssContent;
    private String jsContent;
    private String previewUrl;
    private String status;       // "generating", "complete", "error"
    private String message;
    private long generatedAt;

    public WebsiteGenerateResponse() {
        this.generatedAt = System.currentTimeMillis();
    }

    // Getters & Setters
    public String getWebsiteId() { return websiteId; }
    public void setWebsiteId(String websiteId) { this.websiteId = websiteId; }

    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }

    public String getCssContent() { return cssContent; }
    public void setCssContent(String cssContent) { this.cssContent = cssContent; }

    public String getJsContent() { return jsContent; }
    public void setJsContent(String jsContent) { this.jsContent = jsContent; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }
}
