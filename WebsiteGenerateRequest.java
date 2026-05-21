package com.webcraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WebsiteGenerateRequest {

    @NotBlank(message = "Prompt cannot be empty")
    @Size(min = 10, max = 1000, message = "Prompt must be between 10 and 1000 characters")
    private String prompt;

    private String style;       // "minimal", "bold", "corporate", "creative"
    private String colorTheme;  // "dark", "light", "auto"
    private String industry;    // "tech", "retail", "portfolio", "agency", etc.

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String colorTheme) { this.colorTheme = colorTheme; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
}
