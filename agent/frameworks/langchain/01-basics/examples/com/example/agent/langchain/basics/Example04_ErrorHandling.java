/**
 * Example 4: Error Handling and Retry Logic
 *
 * This class demonstrates how to handle errors gracefully when working with AI models.
 * Sometimes network requests fail, APIs are busy, or other problems occur. This shows
 * how to make your application robust and reliable.
 *
 * Learning Objectives:
 * - Understanding common types of errors that can occur
 * - How to implement retry logic with exponential backoff
 * - How to handle timeouts gracefully
 * - How to provide meaningful error messages to users
 * - How to implement circuit breaker pattern for reliability
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Executable class demonstrating robust error handling
 */
public class Example04_ErrorHandling {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j Error Handling Example ===\n");

        // Step 1: Check if we have an API key using utility class
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        try {
            // Step 2: Create a chat model using utility
            System.out.println("Step 1: Creating AI Chat Model...");
            ChatLanguageModel chatModel = ConfigurationUtil.createDefaultChatModel();
            System.out.println("✅ Chat model created!\n");

            // Step 3: Create error handling examples
            ErrorHandlingDemo demo = new ErrorHandlingDemo(chatModel);

            // Demo 1: Basic retry logic
            System.out.println("🔄 Demo 1: Testing Retry Logic");
            demo.demonstrateRetryLogic();

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Demo 2: Timeout handling
            System.out.println("⏰ Demo 2: Testing Timeout Handling");
            demo.demonstrateTimeoutHandling();

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Demo 3: Circuit breaker pattern
            System.out.println("🚦 Demo 3: Testing Circuit Breaker");
            demo.demonstrateCircuitBreaker();

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Demo 4: Input validation
            System.out.println("✅ Demo 4: Testing Input Validation");
            demo.demonstrateInputValidation();

            System.out.println("\n✅ All error handling demos completed!");

        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Class demonstrating various error handling patterns
 */
class ErrorHandlingDemo {

    private final ChatLanguageModel chatModel;
    private final SimpleCircuitBreaker circuitBreaker;

    public ErrorHandlingDemo(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.circuitBreaker = new SimpleCircuitBreaker(3, 10000); // 3 failures, 10 second recovery
    }

    /**
     * Demonstrate retry logic with exponential backoff
     */
    public void demonstrateRetryLogic() {
        System.out.println("Testing retry logic with a normal prompt...");

        String prompt = "What are the benefits of using retry logic in software applications?";

        // Try the request with retry logic
        String result = retryWithBackoff(prompt, 3);

        if (result.startsWith("Error:")) {
            System.out.println("❌ Final result: " + result);
        } else {
            System.out.println("✅ Success! Response length: " + result.length() + " characters");
            System.out.println("🤖 AI Response: " + truncateString(result, 100));
        }
    }

    /**
     * Demonstrate timeout handling
     */
    public void demonstrateTimeoutHandling() {
        System.out.println("Testing timeout handling with a complex prompt...");

        // This is a complex prompt that might take longer to process
        String complexPrompt = "Write a detailed analysis of the economic, social, and environmental " +
                              "impacts of renewable energy adoption across different countries, including " +
                              "specific case studies and statistical data.";

        // Try with a short timeout (might fail)
        String result = callWithTimeout(complexPrompt, 5000); // 5 seconds

        System.out.println("Result with 5-second timeout: ");
        if (result.contains("timed out")) {
            System.out.println("⏰ Request timed out as expected");
        } else {
            System.out.println("✅ Completed within timeout: " + truncateString(result, 100));
        }

        // Try with a reasonable timeout
        System.out.println("\nTrying again with 30-second timeout...");
        result = callWithTimeout(complexPrompt, 30000); // 30 seconds

        if (result.contains("timed out")) {
            System.out.println("⏰ Still timed out (network might be slow)");
        } else {
            System.out.println("✅ Completed successfully: " + truncateString(result, 100));
        }
    }

    /**
     * Demonstrate circuit breaker pattern
     */
    public void demonstrateCircuitBreaker() {
        System.out.println("Testing circuit breaker pattern...");

        String prompt = "Explain the circuit breaker pattern in software design.";

        // Make several calls to demonstrate circuit breaker
        for (int i = 1; i <= 5; i++) {
            System.out.println("\nAttempt " + i + ":");

            final int attemptNumber = i; // Make variable effectively final for lambda

            String result = circuitBreaker.call(() -> {
                // Simulate occasional failures for demonstration
                if (attemptNumber == 2 || attemptNumber == 3) {
                    throw new RuntimeException("Simulated network error");
                }
                return makeSimpleCall(prompt);
            });

            if (result.startsWith("Circuit breaker")) {
                System.out.println("🚦 " + result);
            } else if (result.startsWith("Error:")) {
                System.out.println("❌ " + result);
            } else {
                System.out.println("✅ Success: " + truncateString(result, 80));
            }
        }
    }

    /**
     * Demonstrate input validation
     */
    public void demonstrateInputValidation() {
        System.out.println("Testing input validation...");

        String[] testInputs = {
            "",                                              // Empty input
            "   ",                                          // Whitespace only
            "Hi",                                           // Too short
            generateLongString(5000),                       // Too long
            "What is the capital of France?",               // Valid input
            null                                            // Null input
        };

        for (int i = 0; i < testInputs.length; i++) {
            System.out.println("\nTest " + (i + 1) + ":");

            try {
                String validatedInput = validateInput(testInputs[i]);
                System.out.println("✅ Input valid: \"" + truncateString(validatedInput, 50) + "\"");

                // If validation passes, try the actual call
                String response = makeSimpleCall(validatedInput);
                System.out.println("✅ AI Response: " + truncateString(response, 80));

            } catch (IllegalArgumentException e) {
                System.out.println("❌ Input validation failed: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Unexpected error: " + e.getMessage());
            }
        }
    }

    /**
     * Retry a request with exponential backoff
     */
    private String retryWithBackoff(String prompt, int maxRetries) {
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                attempt++;
                System.out.println("  Attempt " + attempt + "...");

                // Make the actual call
                return makeSimpleCall(prompt);

            } catch (Exception e) {
                System.out.println("  ❌ Attempt " + attempt + " failed: " + e.getMessage());

                // If this was our last attempt, give up
                if (attempt >= maxRetries) {
                    return "Error: Failed after " + maxRetries + " attempts. Last error: " + e.getMessage();
                }

                // Wait before retrying (exponential backoff)
                long delay = (long) Math.pow(2, attempt) * 1000; // 2^attempt seconds
                delay = Math.min(delay, 10000); // Max 10 seconds

                System.out.println("  ⏳ Waiting " + (delay / 1000) + " seconds before retry...");

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "Error: Interrupted during retry delay";
                }
            }
        }

        return "Error: This should never happen";
    }

    /**
     * Make a call with timeout
     */
    private String callWithTimeout(String prompt, long timeoutMs) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                return makeSimpleCall(prompt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "Error: Request timed out after " + timeoutMs + "ms";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Make a simple call to the AI model
     */
    private String makeSimpleCall(String prompt) {
        try {
            UserMessage message = UserMessage.from(prompt);
            Response<AiMessage> response = chatModel.generate(message);
            return response.content().text();
        } catch (Exception e) {
            throw new RuntimeException("AI call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate user input before sending to AI
     */
    private String validateInput(String input) {
        // Check for null
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        // Check for empty or whitespace-only
        if (input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty or whitespace only");
        }

        // Check minimum length
        if (input.trim().length() < 3) {
            throw new IllegalArgumentException("Input too short (minimum 3 characters)");
        }

        // Check maximum length (approximate token limit)
        if (input.length() > 4000) { // Rough estimate for token limits
            throw new IllegalArgumentException("Input too long (maximum ~4000 characters)");
        }

        // Check for potentially harmful content (basic check)
        String lowerInput = input.toLowerCase();
        String[] blockedTerms = {"hack", "exploit", "malware", "virus"};
        for (String term : blockedTerms) {
            if (lowerInput.contains(term)) {
                throw new IllegalArgumentException("Input contains blocked content");
            }
        }

        return input.trim();
    }

    // Helper methods
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    private String generateLongString(int length) {
        StringBuilder sb = new StringBuilder();
        String pattern = "This is a very long string. ";
        while (sb.length() < length) {
            sb.append(pattern);
        }
        return sb.substring(0, length);
    }
}

/**
 * Simple implementation of the Circuit Breaker pattern
 */
class SimpleCircuitBreaker {

    private int failureCount = 0;
    private long lastFailureTime = 0;
    private final int failureThreshold;
    private final long recoveryTimeMs;

    /**
     * Create a circuit breaker
     * @param failureThreshold Number of failures before opening circuit
     * @param recoveryTimeMs How long to wait before trying again
     */
    public SimpleCircuitBreaker(int failureThreshold, long recoveryTimeMs) {
        this.failureThreshold = failureThreshold;
        this.recoveryTimeMs = recoveryTimeMs;
    }

    /**
     * Make a call through the circuit breaker
     * @param operation The operation to perform
     * @return The result or an error message
     */
    public String call(Operation operation) {
        // Check if circuit is open (too many recent failures)
        if (failureCount >= failureThreshold) {
            long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime;

            if (timeSinceLastFailure < recoveryTimeMs) {
                return "Circuit breaker is OPEN - service temporarily unavailable. " +
                       "Try again in " + ((recoveryTimeMs - timeSinceLastFailure) / 1000) + " seconds.";
            } else {
                // Try to reset the circuit (half-open state)
                System.out.println("🔄 Circuit breaker attempting to reset...");
                failureCount = 0;
            }
        }

        try {
            // Try to make the call
            String result = operation.execute();

            // Success! Reset failure count
            failureCount = 0;
            return result;

        } catch (Exception e) {
            // Failure! Increment counter and record time
            failureCount++;
            lastFailureTime = System.currentTimeMillis();

            System.out.println("🚨 Circuit breaker recorded failure " + failureCount + "/" + failureThreshold);

            return "Error: " + e.getMessage();
        }
    }

    /**
     * Interface for operations that can be called through the circuit breaker
     */
    @FunctionalInterface
    public interface Operation {
        String execute() throws Exception;
    }
}
