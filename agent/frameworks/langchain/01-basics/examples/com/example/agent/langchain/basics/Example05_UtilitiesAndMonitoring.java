/**
 * Example 5: Utilities and Monitoring
 *
 * This class demonstrates useful utility functions for working with AI responses
 * and monitoring the performance and costs of your AI applications.
 *
 * Learning Objectives:
 * - How to analyze and validate AI responses
 * - How to extract structured information from text
 * - How to monitor performance and costs
 * - How to implement logging for debugging
 * - How to process and clean text data
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Executable class demonstrating utilities and monitoring
 */
public class Example05_UtilitiesAndMonitoring {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j Utilities and Monitoring Example ===\n");

        // Step 1: Check if we have an API key using utility class
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        try {
            // Step 2: Create a chat model using utility
            System.out.println("Step 1: Creating AI Chat Model...");
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createCustomChatModel(
                "gpt-3.5-turbo",       // Model name
                0.7,                   // Temperature
                600,                   // Token limit for monitoring examples
                30                     // Timeout
            );
            System.out.println("✅ Chat model created!\n");

            // Step 3: Create utility and monitoring demos
            UtilitiesDemo demo = new UtilitiesDemo(chatModel);

            // Demo 1: Text processing utilities
            System.out.println("🔧 Demo 1: Text Processing Utilities");
            demo.demonstrateTextProcessing();

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Demo 2: Response analysis
            System.out.println("📊 Demo 2: Response Analysis");
            demo.demonstrateResponseAnalysis();

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Demo 3: Performance monitoring
            System.out.println("⏱️ Demo 3: Performance Monitoring");
            demo.demonstratePerformanceMonitoring();

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Demo 4: Structured data extraction
            System.out.println("🎯 Demo 4: Structured Data Extraction");
            demo.demonstrateDataExtraction();

            System.out.println("\n✅ All utility demos completed!");

        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Class demonstrating various utility functions
 */
class UtilitiesDemo {

    private final ChatLanguageModel chatModel;
    private final PerformanceMonitor monitor;

    public UtilitiesDemo(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.monitor = new PerformanceMonitor();
    }

    /**
     * Demonstrate text processing utilities
     */
    public void demonstrateTextProcessing() {
        System.out.println("Testing text cleaning and processing utilities...\n");

        // Test 1: Clean messy text
        String messyText = "  This    text\n\n\thas   many    problems!!!   \r\n  ";
        System.out.println("Original messy text: '" + messyText + "'");

        String cleanedText = TextUtils.cleanText(messyText);
        System.out.println("Cleaned text: '" + cleanedText + "'");
        System.out.println("✅ Text cleaned successfully!\n");

        // Test 2: Text truncation
        String longText = "This is a very long piece of text that needs to be truncated " +
                         "to fit within certain limits. It has multiple sentences and should " +
                         "be cut at a reasonable point, preferably at the end of a sentence " +
                         "rather than in the middle of a word or phrase.";

        System.out.println("Original text length: " + longText.length() + " characters");

        String truncated = TextUtils.truncateToTokenLimit(longText, 20); // ~20 tokens
        System.out.println("Truncated text: " + truncated);
        System.out.println("Truncated length: " + truncated.length() + " characters");
        System.out.println("✅ Text truncated intelligently!\n");

        // Test 3: Word counting
        String sampleText = "The quick brown fox jumps over the lazy dog. This sentence has exactly fifteen words.";
        int wordCount = TextUtils.countWords(sampleText);
        System.out.println("Sample text: " + sampleText);
        System.out.println("Word count: " + wordCount);
        System.out.println("✅ Word counting works correctly!");
    }

    /**
     * Demonstrate response analysis utilities
     */
    public void demonstrateResponseAnalysis() {
        System.out.println("Testing AI response analysis...\n");

        // Get a response from AI for analysis
        String prompt = "Please provide information about machine learning in the following format:\n" +
                       "Definition: [brief definition]\n" +
                       "Benefits: [list main benefits]\n" +
                       "Applications: [list common applications]";

        System.out.println("Asking AI: " + prompt + "\n");

        try {
            String response = makeCall(prompt);
            System.out.println("AI Response:\n" + response + "\n");

            // Analyze the response
            ResponseAnalyzer analyzer = new ResponseAnalyzer();

            // Test 1: Quality validation
            boolean isGood = analyzer.isGoodResponse(response);
            System.out.println("Response quality check: " + (isGood ? "✅ Good" : "❌ Poor"));

            // Test 2: Extract key-value pairs
            Map<String, String> keyValuePairs = analyzer.extractKeyValuePairs(response);
            System.out.println("Extracted key-value pairs: " + keyValuePairs.size() + " found");
            for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " +
                                 TextUtils.truncateString(entry.getValue(), 50));
            }

            // Test 3: Find structured content
            Map<String, Object> structuredData = analyzer.extractStructuredContent(response);
            System.out.println("Structured content found: " + structuredData.size() + " elements");
            for (Map.Entry<String, Object> entry : structuredData.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }

            System.out.println("✅ Response analysis completed!");

        } catch (Exception e) {
            System.err.println("❌ Error during response analysis: " + e.getMessage());
        }
    }

    /**
     * Demonstrate performance monitoring
     */
    public void demonstratePerformanceMonitoring() {
        System.out.println("Testing performance monitoring...\n");

        String[] testPrompts = {
            "What is artificial intelligence?",
            "Explain quantum computing in simple terms.",
            "List the benefits of renewable energy."
        };

        for (int i = 0; i < testPrompts.length; i++) {
            System.out.println("Test " + (i + 1) + "/" + testPrompts.length);

            // Start monitoring
            monitor.startRequest(testPrompts[i]);

            try {
                // Make the call
                String response = makeCall(testPrompts[i]);

                // End monitoring with success
                monitor.endRequest(true, response, null);

                // Display stats
                RequestStats stats = monitor.getLastRequestStats();
                displayRequestStats(stats);

            } catch (Exception e) {
                // End monitoring with failure
                monitor.endRequest(false, null, e.getMessage());

                System.out.println("❌ Request failed: " + e.getMessage());
            }

            System.out.println();
        }

        // Show overall statistics
        System.out.println("📈 Overall Performance Summary:");
        OverallStats overall = monitor.getOverallStats();
        displayOverallStats(overall);
    }

    /**
     * Demonstrate structured data extraction
     */
    public void demonstrateDataExtraction() {
        System.out.println("Testing structured data extraction...\n");

        // Ask AI for structured information
        String structuredPrompt = "Please provide information about Python programming language in this format:\n" +
                                 "Language: Python\n" +
                                 "Year Created: 1991\n" +
                                 "Creator: Guido van Rossum\n" +
                                 "Main Uses:\n" +
                                 "- Web development\n" +
                                 "- Data science\n" +
                                 "- Machine learning\n" +
                                 "Advantages:\n" +
                                 "1. Easy to learn\n" +
                                 "2. Large ecosystem\n" +
                                 "3. Versatile";

        System.out.println("Requesting structured data from AI...\n");

        try {
            String response = makeCall(structuredPrompt);
            System.out.println("AI Response:\n" + response + "\n");

            // Extract structured data
            DataExtractor extractor = new DataExtractor();

            // Extract different types of structured content
            Map<String, String> keyValues = extractor.extractKeyValuePairs(response);
            List<String> bulletPoints = extractor.extractBulletPoints(response);
            List<String> numberedItems = extractor.extractNumberedItems(response);

            // Display extracted data
            System.out.println("🔍 Extracted Structured Data:");

            if (!keyValues.isEmpty()) {
                System.out.println("\nKey-Value Pairs (" + keyValues.size() + " found):");
                for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                    System.out.println("  📝 " + entry.getKey() + " → " + entry.getValue());
                }
            }

            if (!bulletPoints.isEmpty()) {
                System.out.println("\nBullet Points (" + bulletPoints.size() + " found):");
                for (String point : bulletPoints) {
                    System.out.println("  • " + point);
                }
            }

            if (!numberedItems.isEmpty()) {
                System.out.println("\nNumbered Items (" + numberedItems.size() + " found):");
                for (int i = 0; i < numberedItems.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + numberedItems.get(i));
                }
            }

            System.out.println("\n✅ Data extraction completed!");

        } catch (Exception e) {
            System.err.println("❌ Error during data extraction: " + e.getMessage());
        }
    }

    // Helper methods
    private String makeCall(String prompt) throws Exception {
        UserMessage message = UserMessage.from(prompt);
        Response<AiMessage> response = chatModel.generate(message);
        return response.content().text();
    }

    private void displayRequestStats(RequestStats stats) {
        System.out.println("📊 Request Statistics:");
        System.out.println("  ⏱️  Duration: " + stats.durationMs + "ms");
        System.out.println("  📝 Prompt length: " + stats.promptLength + " characters");
        System.out.println("  💬 Response length: " + stats.responseLength + " characters");
        System.out.println("  💰 Estimated cost: $" + String.format("%.4f", stats.estimatedCost));
        System.out.println("  ✅ Success: " + stats.success);
    }

    private void displayOverallStats(OverallStats stats) {
        System.out.println("  🔢 Total requests: " + stats.totalRequests);
        System.out.println("  ✅ Successful: " + stats.successfulRequests);
        System.out.println("  ❌ Failed: " + stats.failedRequests);
        System.out.println("  ⏱️  Average duration: " + String.format("%.1f", stats.averageDurationMs) + "ms");
        System.out.println("  💰 Total estimated cost: $" + String.format("%.4f", stats.totalEstimatedCost));
        System.out.println("  📈 Success rate: " + String.format("%.1f", stats.successRate * 100) + "%");
    }
}

/**
 * Utility class for text processing operations
 */
class TextUtils {

    /**
     * Clean and normalize text
     */
    public static String cleanText(String text) {
        if (text == null) return "";

        return text
            .replaceAll("\\s+", " ")               // Replace multiple spaces with single space
            .replaceAll("[\\r\\n\\t]+", " ")       // Replace line breaks and tabs with spaces
            .replaceAll("[^\\p{Print}]", " ")      // Remove non-printable characters
            .trim();                               // Remove leading/trailing spaces
    }

    /**
     * Truncate text intelligently at sentence or word boundaries
     */
    public static String truncateToTokenLimit(String text, int maxTokens) {
        if (text == null || text.isEmpty()) return text;

        // Rough estimation: 1 token ≈ 4 characters for English
        int maxChars = maxTokens * 4;

        if (text.length() <= maxChars) {
            return text;
        }

        // Try to break at sentence boundaries first
        int breakPoint = findBestBreakPoint(text, maxChars);

        return text.substring(0, breakPoint).trim() + "...";
    }

    private static int findBestBreakPoint(String text, int maxChars) {
        // Look for sentence endings
        for (String ending : new String[]{".", "!", "?"}) {
            int pos = text.lastIndexOf(ending, maxChars);
            if (pos > maxChars * 0.7) { // At least 70% of desired length
                return pos + 1;
            }
        }

        // Look for word boundary
        int spacePos = text.lastIndexOf(' ', maxChars);
        if (spacePos > maxChars * 0.7) {
            return spacePos;
        }

        // Last resort: cut at character limit
        return maxChars;
    }

    /**
     * Count words in text
     */
    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String cleanText = cleanText(text);
        String[] words = cleanText.split("\\s+");
        return words.length;
    }

    /**
     * Truncate string to specified length
     */
    public static String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}

/**
 * Class for analyzing AI responses
 */
class ResponseAnalyzer {

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("([A-Za-z][\\w\\s]*?):\\s*([^\\n]+)");

    /**
     * Check if response appears to be of good quality
     */
    public boolean isGoodResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }

        // Check minimum length
        if (response.trim().length() < 20) {
            return false;
        }

        // Check for error indicators
        String lower = response.toLowerCase();
        String[] errorIndicators = {
            "i cannot", "i don't know", "i'm unable", "error occurred",
            "sorry, i can't", "i don't have information", "unavailable"
        };

        for (String indicator : errorIndicators) {
            if (lower.contains(indicator)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extract key-value pairs from response
     */
    public Map<String, String> extractKeyValuePairs(String response) {
        Map<String, String> pairs = new LinkedHashMap<>();

        Matcher matcher = KEY_VALUE_PATTERN.matcher(response);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            pairs.put(key, value);
        }

        return pairs;
    }

    /**
     * Extract structured content from response
     */
    public Map<String, Object> extractStructuredContent(String response) {
        Map<String, Object> structured = new HashMap<>();

        // Count sentences
        int sentences = response.split("[.!?]+").length;
        structured.put("sentence_count", sentences);

        // Count paragraphs
        int paragraphs = response.split("\\n\\s*\\n").length;
        structured.put("paragraph_count", paragraphs);

        // Check for lists
        boolean hasBulletList = response.contains("•") || response.matches("(?s).*\\n\\s*[-*]\\s+.*");
        structured.put("has_bullet_list", hasBulletList);

        boolean hasNumberedList = response.matches("(?s).*\\n\\s*\\d+\\.\\s+.*");
        structured.put("has_numbered_list", hasNumberedList);

        return structured;
    }
}

/**
 * Class for extracting structured data from text
 */
class DataExtractor {

    public Map<String, String> extractKeyValuePairs(String text) {
        Map<String, String> pairs = new LinkedHashMap<>();

        Pattern pattern = Pattern.compile("([A-Za-z][\\w\\s]*?):\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            pairs.put(key, value);
        }

        return pairs;
    }

    public List<String> extractBulletPoints(String text) {
        List<String> points = new ArrayList<>();

        Pattern pattern = Pattern.compile("(?:^|\\n)\\s*[-•*]\\s+(.+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            points.add(matcher.group(1).trim());
        }

        return points;
    }

    public List<String> extractNumberedItems(String text) {
        List<String> items = new ArrayList<>();

        Pattern pattern = Pattern.compile("(?:^|\\n)\\s*\\d+\\.\\s+(.+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            items.add(matcher.group(1).trim());
        }

        return items;
    }
}

/**
 * Performance monitoring classes
 */
class PerformanceMonitor {
    private final List<RequestStats> allRequests = new ArrayList<>();
    private RequestStats currentRequest;

    public void startRequest(String prompt) {
        currentRequest = new RequestStats();
        currentRequest.prompt = prompt;
        currentRequest.promptLength = prompt.length();
        currentRequest.startTime = System.currentTimeMillis();
    }

    public void endRequest(boolean success, String response, String errorMessage) {
        if (currentRequest == null) return;

        currentRequest.endTime = System.currentTimeMillis();
        currentRequest.durationMs = currentRequest.endTime - currentRequest.startTime;
        currentRequest.success = success;

        if (success && response != null) {
            currentRequest.response = response;
            currentRequest.responseLength = response.length();
            currentRequest.estimatedCost = estimateCost(currentRequest.promptLength, currentRequest.responseLength);
        } else {
            currentRequest.errorMessage = errorMessage;
        }

        allRequests.add(currentRequest);
    }

    public RequestStats getLastRequestStats() {
        return currentRequest;
    }

    public OverallStats getOverallStats() {
        OverallStats stats = new OverallStats();
        stats.totalRequests = allRequests.size();

        for (RequestStats req : allRequests) {
            if (req.success) {
                stats.successfulRequests++;
                stats.totalDurationMs += req.durationMs;
                stats.totalEstimatedCost += req.estimatedCost;
            } else {
                stats.failedRequests++;
            }
        }

        if (stats.successfulRequests > 0) {
            stats.averageDurationMs = (double) stats.totalDurationMs / stats.successfulRequests;
        }

        if (stats.totalRequests > 0) {
            stats.successRate = (double) stats.successfulRequests / stats.totalRequests;
        }

        return stats;
    }

    private double estimateCost(int promptLength, int responseLength) {
        // Rough estimate for GPT-3.5-turbo pricing
        int promptTokens = promptLength / 4;  // Rough approximation
        int responseTokens = responseLength / 4;

        double promptCost = promptTokens * 0.0015 / 1000;  // $0.0015 per 1K tokens
        double responseCost = responseTokens * 0.002 / 1000; // $0.002 per 1K tokens

        return promptCost + responseCost;
    }
}

class RequestStats {
    public String prompt;
    public String response;
    public String errorMessage;
    public long startTime;
    public long endTime;
    public long durationMs;
    public int promptLength;
    public int responseLength;
    public double estimatedCost;
    public boolean success;
}

class OverallStats {
    public int totalRequests = 0;
    public int successfulRequests = 0;
    public int failedRequests = 0;
    public long totalDurationMs = 0;
    public double averageDurationMs = 0;
    public double totalEstimatedCost = 0;
    public double successRate = 0;
}
