/**
 * LangChain4j Configuration Utility
 *
 * This is a reusable utility class for managing LangChain4j configuration
 * across all examples. It handles API keys, model settings, and validation.
 *
 * This class can be used by any example that needs to configure AI models.
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.time.Duration;
import java.util.*;

/**
 * Utility class for managing LangChain4j configuration
 * Handles loading, validating, and creating AI models with proper settings
 */
public class ConfigurationUtil {

    // Configuration keys - these are the names we use to store settings
    private static final String API_KEY = "openai.api.key";
    private static final String MODEL_NAME = "openai.model";
    private static final String TEMPERATURE = "openai.temperature";
    private static final String MAX_TOKENS = "openai.max.tokens";
    private static final String TIMEOUT = "openai.timeout.seconds";

    // Default values - these are used if no environment variables are set
    // Using GPT-4o mini for learning - much cheaper than GPT-3.5-turbo!
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final String DEFAULT_TEMPERATURE = "0.7";
    private static final String DEFAULT_MAX_TOKENS = "1000";
    private static final String DEFAULT_TIMEOUT = "30";

    // Storage for all configuration values
    private final Map<String, String> configuration;

    /**
     * Constructor - automatically loads and validates configuration
     */
    public ConfigurationUtil() {
        this.configuration = new HashMap<>();
        loadConfiguration();
        validateConfiguration();
    }

    /**
     * Static factory method for quick configuration creation
     * @return A new ConfigurationUtil instance
     */
    public static ConfigurationUtil create() {
        return new ConfigurationUtil();
    }

    /**
     * Static method to quickly check if API key is available
     * @return true if OPENAI_API_KEY environment variable is set
     */
    public static boolean isApiKeyAvailable() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * Static method to create a basic chat model with default settings
     * @return ChatLanguageModel with default configuration
     * @throws IllegalStateException if API key is not configured
     */
    public static ChatLanguageModel createDefaultChatModel() {
        return new ConfigurationUtil().createChatModel();
    }

    /**
     * Load configuration from environment variables
     * Environment variables take priority over defaults
     */
    private void loadConfiguration() {
        // Load API key from environment (no default for security)
        configuration.put(API_KEY, getEnvironmentValue("OPENAI_API_KEY", ""));

        // Load other settings with defaults
        configuration.put(MODEL_NAME, getEnvironmentValue("OPENAI_MODEL", DEFAULT_MODEL));
        configuration.put(TEMPERATURE, getEnvironmentValue("OPENAI_TEMPERATURE", DEFAULT_TEMPERATURE));
        configuration.put(MAX_TOKENS, getEnvironmentValue("OPENAI_MAX_TOKENS", DEFAULT_MAX_TOKENS));
        configuration.put(TIMEOUT, getEnvironmentValue("OPENAI_TIMEOUT", DEFAULT_TIMEOUT));
    }

    /**
     * Get value from environment variable or return default
     * @param envName Environment variable name
     * @param defaultValue Default value if environment variable is not set
     * @return The configuration value
     */
    private String getEnvironmentValue(String envName, String defaultValue) {
        String value = System.getenv(envName);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        } else {
            return value.trim();
        }
    }

    /**
     * Validate all configuration values
     * This checks that the values make sense and are in valid ranges
     */
    private void validateConfiguration() {
        List<String> errors = new ArrayList<>();

        // Validate temperature (must be between 0 and 2)
        //
        // WHAT IS TEMPERATURE?
        // Temperature controls the "creativity" or "randomness" of AI responses.
        // It affects how the AI chooses words when generating text.
        //
        // HOW IT WORKS:
        // - Temperature = 0.0: AI always picks the most probable next word
        //   Result: Very predictable, consistent, "boring" responses
        //   Use for: Code generation, factual questions, mathematical problems
        //
        // - Temperature = 0.7: Balanced creativity and consistency (RECOMMENDED)
        //   Result: Natural, varied responses that still make sense
        //   Use for: General conversations, explanations, most use cases
        //
        // - Temperature = 1.0: More creative and varied responses
        //   Result: More interesting but sometimes unexpected answers
        //   Use for: Creative writing, brainstorming, storytelling
        //
        // - Temperature = 2.0: Very creative and unpredictable (MAXIMUM)
        //   Result: Highly varied, sometimes strange or inconsistent responses
        //   Use for: Experimental creative tasks (use with caution)
        //
        // WHY 0-2 RANGE?
        // - Below 0: Mathematically invalid (negative randomness doesn't exist)
        // - Above 2: Responses become too chaotic and often nonsensical
        // - OpenAI API specifically accepts 0.0 to 2.0 as valid range
        //
        // EXAMPLES:
        // Question: "What is the capital of France?"
        // Temperature 0.0: "The capital of France is Paris."
        // Temperature 0.7: "The capital of France is Paris, a beautiful city known for its culture."
        // Temperature 1.5: "Paris is the capital of France! It's an amazing city with rich history..."
        // Temperature 2.0: "France's capital? That would be Paris - oh, the romance and croissants!"
        try {
            double temp = Double.parseDouble(configuration.get(TEMPERATURE));
            if (temp < 0 || temp > 2) {
                errors.add("Temperature must be between 0 and 2, got: " + temp +
                          " (0.0=predictable, 0.7=balanced, 1.0=creative, 2.0=very creative)");
            }
        } catch (NumberFormatException e) {
            errors.add("Temperature must be a valid number, got: " + configuration.get(TEMPERATURE));
        }

        // Validate max tokens (must be positive and reasonable)
        try {
            int maxTokens = Integer.parseInt(configuration.get(MAX_TOKENS));
            if (maxTokens <= 0) {
                errors.add("Max tokens must be positive, got: " + maxTokens);
            }
            if (maxTokens > 4096) {
                errors.add("Max tokens should not exceed 4096 for most models, got: " + maxTokens);
            }
        } catch (NumberFormatException e) {
            errors.add("Max tokens must be a valid integer, got: " + configuration.get(MAX_TOKENS));
        }

        // Validate timeout (must be positive)
        try {
            int timeout = Integer.parseInt(configuration.get(TIMEOUT));
            if (timeout <= 0) {
                errors.add("Timeout must be positive, got: " + timeout);
            }
        } catch (NumberFormatException e) {
            errors.add("Timeout must be a valid integer, got: " + configuration.get(TIMEOUT));
        }

        // If there are errors, throw an exception
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Configuration validation failed: " + String.join(", ", errors));
        }
    }

    /**
     * Check if we have a valid API key
     * @return true if API key is present and not empty
     */
    public boolean hasValidApiKey() {
        String apiKey = configuration.get(API_KEY);
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * Create a ChatLanguageModel with current configuration
     * @return Configured ChatLanguageModel
     * @throws IllegalStateException if API key is not configured
     */
    public ChatLanguageModel createChatModel() {
        if (!hasValidApiKey()) {
            throw new IllegalStateException("Cannot create chat model: API key is not configured. " +
                    "Please set OPENAI_API_KEY environment variable.");
        }

        return OpenAiChatModel.builder()
            .apiKey(configuration.get(API_KEY))
            .modelName(configuration.get(MODEL_NAME))
            .temperature(Double.parseDouble(configuration.get(TEMPERATURE)))
            .maxTokens(Integer.parseInt(configuration.get(MAX_TOKENS)))
            .timeout(Duration.ofSeconds(Long.parseLong(configuration.get(TIMEOUT))))
            .logRequests(false)  // Set to true if you want to see API requests
            .logResponses(false) // Set to true if you want to see API responses
            .build();
    }

    /**
     * Create a ChatLanguageModel with custom settings
     *
     * @param modelName The model to use:
     *                  💰 COST-EFFECTIVE FOR LEARNING:
     *                  - "gpt-4o-mini": ~$0.15/1M tokens (CHEAPEST, perfect for learning!)
     *                  - "gpt-3.5-turbo": ~$0.50/1M tokens (still affordable)
     *
     *                  💸 MORE EXPENSIVE (use sparingly while learning):
     *                  - "gpt-4o": ~$2.50/1M tokens (4x more expensive than gpt-3.5-turbo)
     *                  - "gpt-4": ~$10/1M tokens (20x more expensive!)
     *
     *                  📊 TYPICAL USAGE COSTS:
     *                  100 questions with gpt-4o-mini: ~$0.01-0.05
     *                  100 questions with gpt-3.5-turbo: ~$0.05-0.20
     *                  100 questions with gpt-4o: ~$0.25-1.00
     *                  100 questions with gpt-4: ~$1.00-5.00
     *
     * @param temperature Controls randomness/creativity (0.0 = deterministic, 1.0 = creative, 2.0 = very creative)
     *                   - 0.0: Always picks most likely words → consistent, predictable responses
     *                   - 0.3: Slightly more varied → good for factual Q&A
     *                   - 0.7: Balanced creativity → recommended for most conversations
     *                   - 1.0: More creative responses → good for writing, brainstorming
     *                   - 1.5: Highly creative → experimental, sometimes unexpected
     *                   - 2.0: Maximum creativity → very unpredictable, use with caution
     * @param maxTokens Maximum response length (1 token ≈ 4 characters in English)
     *                  💡 TIP: Use lower values while learning to save money!
     *                  - 200-500 tokens: Short answers, good for learning
     *                  - 1000 tokens: Medium answers
     *                  - 2000+ tokens: Long answers, use sparingly
     * @param timeoutSeconds Request timeout in seconds
     * @return Configured ChatLanguageModel
     *
     * 💰 COST-SAVING EXAMPLES FOR LEARNING:
     *
     * For Learning/Practice (CHEAPEST):
     *   config.createCustomChatModel("gpt-4o-mini", 0.7, 300, 30)
     *   → Great quality, very affordable
     *
     * For Code Generation (AFFORDABLE):
     *   config.createCustomChatModel("gpt-4o-mini", 0.0, 500, 30)
     *   → Consistent code suggestions without breaking the bank
     *
     * For General Chat (BUDGET-FRIENDLY):
     *   config.createCustomChatModel("gpt-3.5-turbo", 0.7, 400, 30)
     *   → Natural conversations at low cost
     *
     * For Special Projects Only (EXPENSIVE):
     *   config.createCustomChatModel("gpt-4o", 1.0, 1000, 45)
     *   → Use only when you need the absolute best quality
     */
    public ChatLanguageModel createCustomChatModel(String modelName, double temperature,
                                                  int maxTokens, int timeoutSeconds) {
        if (!hasValidApiKey()) {
            throw new IllegalStateException("Cannot create chat model: API key is not configured. " +
                    "Please set OPENAI_API_KEY environment variable.");
        }

        return OpenAiChatModel.builder()
            .apiKey(configuration.get(API_KEY))
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .logRequests(false)
            .logResponses(false)
            .build();
    }

    /**
     * Display current configuration (safely - hides API key)
     * @param showDetails Whether to show detailed configuration info
     */
    public void displayConfiguration(boolean showDetails) {
        System.out.println("🔧 LangChain4j Configuration:");
        System.out.println("- API Key: " + (hasValidApiKey() ? "✅ Configured" : "❌ Not set"));

        if (showDetails) {
            System.out.println("- Model: " + configuration.get(MODEL_NAME));
            System.out.println("- Temperature: " + configuration.get(TEMPERATURE));
            System.out.println("- Max Tokens: " + configuration.get(MAX_TOKENS));
            System.out.println("- Timeout: " + configuration.get(TIMEOUT) + " seconds");
        }

        if (!hasValidApiKey()) {
            System.out.println("\n💡 To set your API key:");
            System.out.println("   export OPENAI_API_KEY=your_api_key_here");
        }
    }

    /**
     * Display current configuration (with details)
     */
    public void displayConfiguration() {
        displayConfiguration(true);
    }

    // Getter methods for individual configuration values
    public String getModelName() {
        return configuration.get(MODEL_NAME);
    }

    public double getTemperature() {
        return Double.parseDouble(configuration.get(TEMPERATURE));
    }

    public int getMaxTokens() {
        return Integer.parseInt(configuration.get(MAX_TOKENS));
    }

    public int getTimeoutSeconds() {
        return Integer.parseInt(configuration.get(TIMEOUT));
    }

    /**
     * Get the API key (use carefully - for debugging only)
     * @return The API key (truncated for security)
     */
    public String getApiKeyInfo() {
        String apiKey = configuration.get(API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            return "Not configured";
        }
        // Show only first 8 characters for security
        return apiKey.substring(0, Math.min(8, apiKey.length())) + "...";
    }

    /**
     * Helper method to print API key setup instructions
     */
    public static void printApiKeyInstructions() {
        System.out.println("❌ OpenAI API Key not found!");
        System.out.println();
        System.out.println("📋 To set up your API key:");
        System.out.println("1. Get your API key from: https://platform.openai.com/api-keys");
        System.out.println("2. Set it as an environment variable:");
        System.out.println("   export OPENAI_API_KEY=your_api_key_here");
        System.out.println("3. Restart your IDE or terminal");
        System.out.println("4. Run this example again");
        System.out.println();
    }
}
