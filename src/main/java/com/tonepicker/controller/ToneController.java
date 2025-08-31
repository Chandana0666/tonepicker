package com.tonepicker.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.tonepicker.model.Tone;

@RestController
@CrossOrigin(origins = "*")
public class ToneController {

    @Value("${mistral.api.key}")
    private String apiKey;

    @Value("${mistral.api.url}")
    private String apiUrl;

    @GetMapping("/tones")
    public List<Tone> getTones() {
        return Arrays.asList(
            new Tone("1", "Formal", "Polite and professional style"),
            new Tone("2", "Friendly", "Warm and approachable style"),
            new Tone("3", "Casual", "Relaxed and informal style"),
            new Tone("4", "Professional", "Clear and business-like style")
        );
    }

    @PostMapping("/rewrite")
    public Map<String, String> rewriteTone(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        String formality = payload.get("formality");
        String warmth = payload.get("warmth");

        String prompt = "Rewrite the following text with formality: " + formality +
                        " and warmth: " + warmth +
                        ". Preserve meaning and keep length similar:\n" + text;

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                "model", "mistral-small-latest",
                "messages", List.of(
                    Map.of("role", "system", "content", "You are a helpful assistant."),
                    Map.of("role", "user", "content", prompt)
                )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String rewritten = (String) message.get("content");

            return Map.of("text",rewritten);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("text",text +"["+formality+","+warmth+"]");
        }
    }
}

