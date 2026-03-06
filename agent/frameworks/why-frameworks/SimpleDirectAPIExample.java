/**
 * Simple Direct OpenAI API Example
 * Shows how to make basic API calls without any framework
 */
package com.example.direct.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
/**
 * Direct OpenAI API call example - No framework needed
 */
public class DirectAPIExample {

    private static final String OPENAI_API_KEY = "your-openai-api-key-here";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public static void main(String[] args) {
        DirectAPIExample example = new DirectAPIExample();

        try {
            // Simple question - Direct API works perfectly!
            String response = example.askQuestion("What is artificial intelligence?");
            System.out.println("Question: What is artificial intelligence?");
            System.out.println("Response: " + response);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Make a simple API call to OpenAI
     */
    public String askQuestion(String question) throws IOException, InterruptedException {
        // Create request body
        Map<String, Object> requestBody = Map.of(
            "model", "gpt-3.5-turbo",
            "messages", List.of(
                Map.of("role", "user", "content", question)
            ),
            "max_tokens", 150,
            "temperature", 0.7
        );

        // Convert to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(requestBody);

        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OPENAI_API_URL))
            .header("Authorization", "Bearer " + OPENAI_API_KEY)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .timeout(Duration.ofSeconds(30))
            .build();

        // Send request
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Handle error
        if (response.statusCode() != 200) {
            throw new RuntimeException("API call failed: " + response.statusCode() + " - " + response.body());
        }

        // Parse response
        Map<String, Object> responseJson = mapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseJson.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}

/*
 * What this example shows:
 *
 * ✅ WORKS GREAT FOR:
 * - Simple one-off questions
 * - Learning how OpenAI API works
 * - Quick prototypes
 * - Basic text generation
 *
 * ❌ LIMITATIONS:
 * - No conversation memory (each call is independent)
 * - No access to real-time data
 * - Manual error handling
 * - No tool integration
 * - No user session management
 * - No enterprise features (auth, monitoring, scaling)
 *
 * This is perfect for getting started, but when you need more sophisticated
 * features like memory, tools, or complex workflows, frameworks become essential!
 */
