/**
 * LangChain4j Basics Examples
 * Demonstrates fundamental concepts and usage patterns
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.output.Response;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Basic LangChain4j operations and patterns
 */
public class LangChainBasicsExample {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j Basics Examples ===");

        try {
            // Initialize configuration
            ConfigurationManager config = new ConfigurationManager();

            if (config.getConfig("openai.api.key").isEmpty()) {
                System.out.println("Please set OPENAI_API_KEY environment variable");
                return;
            }

            // Create chat model
            ChatLanguageModel model = config.createConfiguredChatModel();

            // Run examples
            runBasicChatExample(model);
            runMultiTurnExample(model);
            runErrorHandlingExample(model);
            runUtilityExamples();
            runMonitoringExample(model);

        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Basic chat interaction example
     */
    private static void runBasicChatExample(ChatLanguageModel model) {
        System.out.println("\n1. Basic Chat Example:");

        String[] prompts = {
            "Hello, introduce yourself",
            "What is artificial intelligence?",
            "Explain machine learning in simple terms"
        };

        for (String prompt : prompts) {
            long startTime = System.currentTimeMillis();
            String response = LangChainBasics.simpleChat(model, prompt);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("User: " + prompt);
            System.out.println("AI: " + response);
            System.out.println("Duration: " + duration + "ms");
            System.out.println("Valid response: " + LangChainUtils.isValidResponse(response));
            System.out.println("---");
        }
    }

    /**
     * Multi-turn conversation example
     */
    private static void runMultiTurnExample(ChatLanguageModel model) {
        System.out.println("\n2. Multi-Turn Conversation Example:");

        try {
            // Simulate a conversation about a specific topic
            List<ChatMessage> conversation = new ArrayList<>();

            // User starts the conversation
            conversation.add(UserMessage.from("I'm planning to learn Java programming. Can you help me?"));

            // Get AI response
            Response<AiMessage> response1 = model.generate(conversation);
            conversation.add(response1.content());
            System.out.println("AI: " + response1.content().text());

            // User follow-up
            conversation.add(UserMessage.from("What topics should I focus on first?"));

            // Get AI response
            Response<AiMessage> response2 = model.generate(conversation);
            conversation.add(response2.content());
            System.out.println("AI: " + response2.content().text());

            // User specific question
            conversation.add(UserMessage.from("How long might it take to become proficient?"));

            // Get final AI response
            Response<AiMessage> response3 = model.generate(conversation);
            System.out.println("AI: " + response3.content().text());

            System.out.println("Conversation turns: " + (conversation.size() / 2 + 1));

        } catch (Exception e) {
            System.err.println("Multi-turn conversation error: " + e.getMessage());
        }
    }

    /**
     * Error handling and retry examples
     */
    private static void runErrorHandlingExample(ChatLanguageModel model) {
        System.out.println("\n3. Error Handling Example:");

        // Test robust call with potential network issues
        String prompt = "Generate a creative story about space exploration";

        System.out.println("Testing robust LLM call...");
        long startTime = System.currentTimeMillis();
        String response = ErrorHandlingPatterns.robustLLMCall(model, prompt, 3);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Response received after " + duration + "ms");
        System.out.println("Response length: " + response.length() + " characters");

        // Test timeout handling
        System.out.println("\nTesting timeout handling...");
        String timeoutResponse = ErrorHandlingPatterns.llmCallWithTimeout(model,
            "Write a detailed analysis of quantum computing", 5000); // 5 second timeout

        System.out.println("Timeout test response: " +
            (timeoutResponse.length() > 100 ?
             timeoutResponse.substring(0, 100) + "..." : timeoutResponse));
    }

    /**
     * Utility functions examples
     */
    private static void runUtilityExamples() {
        System.out.println("\n4. Utility Functions Example:");

        // Test text cleaning
        String dirtyText = "  This    is\n\tmessy   text\rwith   \x00control chars  ";
        String cleanText = LangChainUtils.cleanText(dirtyText);
        System.out.println("Original: '" + dirtyText + "'");
        System.out.println("Cleaned: '" + cleanText + "'");

        // Test key-value extraction
        String kvText = "Name: John Doe\nAge: 30\nCity: San Francisco\nOccupation: Software Engineer";
        Map<String, String> extracted = LangChainUtils.extractKeyValuePairs(kvText);
        System.out.println("Extracted key-value pairs: " + extracted);

        // Test text truncation
        String longText = "This is a very long piece of text that needs to be truncated " +
                         "to fit within token limits. It contains multiple sentences and " +
                         "should be cut at a reasonable point like the end of a sentence " +
                         "rather than in the middle of a word.";
        String truncated = LangChainUtils.truncateToTokenLimit(longText, 20); // ~20 tokens
        System.out.println("Original length: " + longText.length());
        System.out.println("Truncated: " + truncated);
        System.out.println("Truncated length: " + truncated.length());
    }

    /**
     * Monitoring and logging examples
     */
    private static void runMonitoringExample(ChatLanguageModel model) {
        System.out.println("\n5. Monitoring Example:");

        String prompt = "Explain the benefits of cloud computing";

        long startTime = System.currentTimeMillis();
        String response = LangChainBasics.simpleChat(model, prompt);
        long duration = System.currentTimeMillis() - startTime;

        // Log the interaction
        LangChainMonitoring.logInteraction(prompt, response, duration);

        // Simulate token usage monitoring (normally you'd get this from the API)
        int promptTokens = estimateTokens(prompt);
        int responseTokens = estimateTokens(response);
        double estimatedCost = calculateEstimatedCost(promptTokens, responseTokens);

        LangChainMonitoring.logTokenUsage(promptTokens, responseTokens, estimatedCost);

        System.out.println("Prompt tokens: " + promptTokens);
        System.out.println("Response tokens: " + responseTokens);
        System.out.println("Estimated cost: $" + String.format("%.4f", estimatedCost));
    }

    /**
     * Rough token estimation (for demonstration)
     */
    private static int estimateTokens(String text) {
        return Math.max(1, text.length() / 4); // Rough approximation
    }

    /**
     * Estimate cost based on token usage (GPT-3.5-turbo pricing)
     */
    private static double calculateEstimatedCost(int promptTokens, int responseTokens) {
        double promptCost = promptTokens * 0.0015 / 1000; // $0.0015 per 1K tokens
        double responseCost = responseTokens * 0.002 / 1000; // $0.002 per 1K tokens
        return promptCost + responseCost;
    }
}

/**
 * Configuration management for LangChain4j
 */
class ConfigurationManager {

    private final Map<String, String> config;

    public ConfigurationManager() {
        this.config = loadConfiguration();
    }

    /**
     * Load configuration from environment variables and properties
     */
    private Map<String, String> loadConfiguration() {
        Map<String, String> config = new HashMap<>();

        // Load from environment variables
        config.put("openai.api.key", getEnvOrDefault("OPENAI_API_KEY", ""));
        config.put("openai.model", getEnvOrDefault("OPENAI_MODEL", "gpt-3.5-turbo"));
        config.put("openai.temperature", getEnvOrDefault("OPENAI_TEMPERATURE", "0.7"));
        config.put("openai.max.tokens", getEnvOrDefault("OPENAI_MAX_TOKENS", "1000"));
        config.put("openai.timeout", getEnvOrDefault("OPENAI_TIMEOUT", "30000"));

        // Load from system properties (overrides env vars)
        config.putAll(loadFromSystemProperties());

        validateConfiguration(config);

        return config;
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    private Map<String, String> loadFromSystemProperties() {
        Map<String, String> props = new HashMap<>();

        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("langchain.") || keyStr.startsWith("openai.")) {
                props.put(keyStr, value.toString());
            }
        });

        return props;
    }

    private void validateConfiguration(Map<String, String> config) {
        // Validate required configurations
        List<String> errors = new ArrayList<>();

        if (config.get("openai.api.key").isEmpty()) {
            errors.add("OpenAI API key is required (set OPENAI_API_KEY environment variable)");
        }

        try {
            double temp = Double.parseDouble(config.get("openai.temperature"));
            if (temp < 0 || temp > 2) {
                errors.add("Temperature must be between 0 and 2");
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid temperature value");
        }

        try {
            int maxTokens = Integer.parseInt(config.get("openai.max.tokens"));
            if (maxTokens <= 0 || maxTokens > 4096) {
                errors.add("Max tokens must be between 1 and 4096");
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid max tokens value");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Configuration validation failed: " + String.join(", ", errors));
        }
    }

    /**
     * Create configured chat model
     */
    public ChatLanguageModel createConfiguredChatModel() {
        String apiKey = config.get("openai.api.key");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        return OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.get("openai.model"))
            .temperature(Double.parseDouble(config.get("openai.temperature")))
            .maxTokens(Integer.parseInt(config.get("openai.max.tokens")))
            .timeout(Duration.ofMillis(Long.parseLong(config.get("openai.timeout"))))
            .logRequests(true)
            .logResponses(true)
            .build();
    }

    public String getConfig(String key) {
        return config.get(key);
    }

    public void setConfig(String key, String value) {
        config.put(key, value);
    }

    public Map<String, String> getAllConfig() {
        Map<String, String> safeCopy = new HashMap<>(config);
        // Hide sensitive information
        if (safeCopy.containsKey("openai.api.key") && !safeCopy.get("openai.api.key").isEmpty()) {
            safeCopy.put("openai.api.key", "sk-***" + safeCopy.get("openai.api.key").substring(Math.max(0, safeCopy.get("openai.api.key").length() - 4)));
        }
        return safeCopy;
    }
}

/**
 * Basic LangChain4j operations
 */
class LangChainBasics {

    /**
     * Simple LLM interaction
     */
    public static String simpleChat(ChatLanguageModel model, String userMessage) {
        try {
            UserMessage message = UserMessage.from(userMessage);
            Response<AiMessage> response = model.generate(message);
            return response.content().text();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Chat with system message
     */
    public static String chatWithSystemMessage(ChatLanguageModel model, String systemMessage, String userMessage) {
        try {
            List<ChatMessage> messages = Arrays.asList(
                dev.langchain4j.data.message.SystemMessage.from(systemMessage),
                UserMessage.from(userMessage)
            );

            Response<AiMessage> response = model.generate(messages);
            return response.content().text();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Streaming chat (conceptual - actual streaming would need different implementation)
     */
    public static String streamingChat(ChatLanguageModel model, String userMessage) {
        try {
            // For demonstration, we'll simulate streaming by breaking response into chunks
            String response = simpleChat(model, userMessage);

            System.out.print("AI (streaming): ");
            String[] words = response.split(" ");
            for (String word : words) {
                System.out.print(word + " ");
                Thread.sleep(50); // Simulate streaming delay
            }
            System.out.println();

            return response;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

/**
 * Error handling patterns for robust LLM interactions
 */
class ErrorHandlingPatterns {

    /**
     * Robust LLM call with retry logic
     */
    public static String robustLLMCall(ChatLanguageModel model, String prompt, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                Response<AiMessage> response = model.generate(UserMessage.from(prompt));
                return response.content().text();
            } catch (Exception e) {
                lastException = e;
                attempts++;

                System.out.println("Attempt " + attempts + " failed: " + e.getMessage());

                if (attempts < maxRetries) {
                    // Wait before retry with exponential backoff
                    long delay = (long) Math.pow(2, attempts) * 1000; // 2^attempts seconds
                    try {
                        Thread.sleep(Math.min(delay, 10000)); // Max 10 seconds
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return "Failed after " + maxRetries + " attempts. Last error: " +
               (lastException != null ? lastException.getMessage() : "Unknown error");
    }

    /**
     * LLM call with timeout
     */
    public static String llmCallWithTimeout(ChatLanguageModel model, String prompt, long timeoutMs) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Response<AiMessage> response = model.generate(UserMessage.from(prompt));
                return response.content().text();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "Request timed out after " + timeoutMs + "ms";
        } catch (Exception e) {
            return "Error: " + e.getCause().getMessage();
        }
    }

    /**
     * Circuit breaker pattern for LLM calls
     */
    public static class LLMCircuitBreaker {
        private int failureCount = 0;
        private long lastFailureTime = 0;
        private final int failureThreshold;
        private final long recoveryTimeMs;

        public LLMCircuitBreaker(int failureThreshold, long recoveryTimeMs) {
            this.failureThreshold = failureThreshold;
            this.recoveryTimeMs = recoveryTimeMs;
        }

        public String callWithCircuitBreaker(ChatLanguageModel model, String prompt) {
            // Check if circuit is open
            if (failureCount >= failureThreshold) {
                if (System.currentTimeMillis() - lastFailureTime < recoveryTimeMs) {
                    return "Circuit breaker is OPEN - service temporarily unavailable";
                } else {
                    // Try to reset circuit
                    failureCount = 0;
                }
            }

            try {
                Response<AiMessage> response = model.generate(UserMessage.from(prompt));
                // Success - reset failure count
                failureCount = 0;
                return response.content().text();
            } catch (Exception e) {
                // Failure - increment counter and record time
                failureCount++;
                lastFailureTime = System.currentTimeMillis();
                return "Service call failed: " + e.getMessage();
            }
        }
    }
}

/**
 * Utility classes for common LangChain operations
 */
class LangChainUtils {

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("(\\w+):\\s*([^\\n]+)");

    /**
     * Validate LLM response quality
     */
    public static boolean isValidResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }

        // Check for common error patterns
        String lower = response.toLowerCase();
        String[] errorPatterns = {
            "i cannot", "i don't know", "i'm not able", "error",
            "sorry, i can't", "i don't have", "unavailable"
        };

        for (String pattern : errorPatterns) {
            if (lower.contains(pattern)) {
                return false;
            }
        }

        // Check minimum length
        return response.trim().length() >= 10;
    }

    /**
     * Extract key-value pairs from LLM response
     */
    public static Map<String, String> extractKeyValuePairs(String response) {
        Map<String, String> pairs = new HashMap<>();

        Matcher matcher = KEY_VALUE_PATTERN.matcher(response);
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String value = matcher.group(2).trim();
            pairs.put(key, value);
        }

        return pairs;
    }

    /**
     * Clean and normalize text for LLM processing
     */
    public static String cleanText(String text) {
        if (text == null) return "";

        return text
            .replaceAll("\\s+", " ")  // Normalize whitespace
            .replaceAll("[\\x00-\\x1F\\x7F]", "") // Remove control characters
            .replaceAll("[^\\p{Print}]", " ") // Remove non-printable characters
            .trim();
    }

    /**
     * Truncate text to approximate token limit
     */
    public static String truncateToTokenLimit(String text, int maxTokens) {
        if (text == null || text.isEmpty()) return text;

        // Rough estimation: 1 token ≈ 4 characters for English text
        int maxChars = maxTokens * 4;

        if (text.length() <= maxChars) {
            return text;
        }

        // Try to break at sentence boundaries
        int breakPoint = text.lastIndexOf('.', maxChars);
        if (breakPoint == -1) {
            breakPoint = text.lastIndexOf('!', maxChars);
        }
        if (breakPoint == -1) {
            breakPoint = text.lastIndexOf('?', maxChars);
        }
        if (breakPoint == -1) {
            breakPoint = text.lastIndexOf(' ', maxChars);
        }
        if (breakPoint == -1) {
            breakPoint = maxChars;
        }

        return text.substring(0, breakPoint).trim() + "...";
    }

    /**
     * Extract structured information from unstructured response
     */
    public static Map<String, Object> parseStructuredResponse(String response) {
        Map<String, Object> structured = new HashMap<>();

        // Try to extract JSON-like patterns
        Pattern jsonPattern = Pattern.compile("\\{[^}]*\\}");
        Matcher jsonMatcher = jsonPattern.matcher(response);

        if (jsonMatcher.find()) {
            structured.put("json_content", jsonMatcher.group());
        }

        // Extract lists
        Pattern listPattern = Pattern.compile("(?:^|\\n)[-*]\\s+(.+)");
        Matcher listMatcher = listPattern.matcher(response);
        List<String> listItems = new ArrayList<>();

        while (listMatcher.find()) {
            listItems.add(listMatcher.group(1).trim());
        }

        if (!listItems.isEmpty()) {
            structured.put("list_items", listItems);
        }

        // Extract numbered items
        Pattern numberedPattern = Pattern.compile("(?:^|\\n)\\d+\\.\\s+(.+)");
        Matcher numberedMatcher = numberedPattern.matcher(response);
        List<String> numberedItems = new ArrayList<>();

        while (numberedMatcher.find()) {
            numberedItems.add(numberedMatcher.group(1).trim());
        }

        if (!numberedItems.isEmpty()) {
            structured.put("numbered_items", numberedItems);
        }

        return structured;
    }
}

/**
 * Monitoring and logging utilities
 */
class LangChainMonitoring {

    /**
     * Log LLM interactions for debugging
     */
    public static void logInteraction(String prompt, String response, long durationMs) {
        System.out.printf("LLM Interaction - Duration: %dms, Prompt: %d chars, Response: %d chars%n",
                         durationMs, prompt.length(), response.length());

        // In a real application, you'd use a proper logging framework
        if (Boolean.parseBoolean(System.getProperty("langchain.debug", "false"))) {
            System.out.println("DEBUG - Prompt: " + truncateForLogging(prompt, 200));
            System.out.println("DEBUG - Response: " + truncateForLogging(response, 200));
        }
    }

    /**
     * Monitor token usage and costs
     */
    public static void logTokenUsage(int promptTokens, int responseTokens, double cost) {
        System.out.printf("Token Usage - Prompt: %d, Response: %d, Total: %d, Cost: $%.4f%n",
                         promptTokens, responseTokens, promptTokens + responseTokens, cost);
    }

    /**
     * Log errors with context
     */
    public static void logError(String operation, String context, Exception error) {
        System.err.printf("LangChain Error - Operation: %s, Context: %s, Error: %s%n",
                         operation, context, error.getMessage());

        if (Boolean.parseBoolean(System.getProperty("langchain.debug", "false"))) {
            error.printStackTrace();
        }
    }

    private static String truncateForLogging(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
