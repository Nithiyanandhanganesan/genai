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
public class SimpleDirectAPIExample {

    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";


    public static void main(String[] args) {
        // Validate API key
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            System.err.println("❌ Error: OPENAI_API_KEY environment variable is not set");
            System.err.println();
            System.err.println("🔧 To set it in IntelliJ IDEA:");
            System.err.println("   1. Right-click this file → 'Run SimpleDirectAPIExample.main()'");
            System.err.println("   2. Click Run dropdown → 'Edit Configurations...'");
            System.err.println("   3. In Environment variables section, click folder icon 📁");
            System.err.println("   4. Click + to add: Name=OPENAI_API_KEY, Value=your-api-key");
            System.err.println("   5. Click OK and run again");
            System.err.println();
            System.err.println("💡 Get your API key from: https://platform.openai.com/api-keys");
            return;
        }

        SimpleDirectAPIExample example = new SimpleDirectAPIExample();

        try {
            System.out.println("🤖 Testing Direct OpenAI API call...");
            System.out.println("==================================================");

            // Simple question - Direct API works perfectly!
            String response = example.askQuestion("What is artificial intelligence?");
            System.out.println("❓ Question: What is artificial intelligence?");
            System.out.println("🤖 Response: " + response);
            System.out.println();
            System.out.println("✅ Success! Direct API call completed.");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            System.err.println();
            if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                System.err.println("💡 This looks like an API key issue. Please check:");
                System.err.println("   - Your API key is correct");
                System.err.println("   - Your OpenAI account has credits");
                System.err.println("   - The API key has proper permissions");
            }
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
        @SuppressWarnings("unchecked")
        Map<String, Object> responseJson = mapper.readValue(response.body(), Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseJson.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No response choices returned from OpenAI API");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        if (message == null) {
            throw new RuntimeException("No message found in API response");
        }

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
