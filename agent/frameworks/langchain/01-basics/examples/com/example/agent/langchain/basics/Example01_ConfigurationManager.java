/**
 * Example 1: Configuration Manager
 *
 * This class demonstrates how to use the ConfigurationUtil class to manage
 * LangChain4j configuration. This shows best practices for organizing utility code.
 *
 * Learning Objectives:
 * - How to use utility classes for configuration management
 * - How to manage API keys securely
 * - How to load configuration from environment variables
 * - How to create ChatLanguageModel with proper settings
 * - How to organize code with separate utility classes
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * Simple executable class to demonstrate configuration management using utility class
 */
public class Example01_ConfigurationManager {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j Configuration Manager Example ===\n");

        try {
            // Method 1: Quick API key check using static method
            System.out.println("🔍 Step 1: Quick API Key Check...");
            if (ConfigurationUtil.isApiKeyAvailable()) {
                System.out.println("✅ API Key is available!");
            } else {
                System.out.println("❌ API Key not found!");
                ConfigurationUtil.printApiKeyInstructions();
                return; // Exit early if no API key
            }

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Method 2: Create configuration utility instance for detailed control
            System.out.println("⚙️  Step 2: Creating Configuration Utility...");
            ConfigurationUtil config = ConfigurationUtil.create();
            System.out.println("✅ Configuration utility created successfully!");

            // Step 3: Display current configuration
            System.out.println("\n📋 Step 3: Current Configuration:");
            config.displayConfiguration();

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Step 4: Create a basic chat model using default settings
            System.out.println("🤖 Step 4: Creating Chat Model (Default Settings)...");
            ChatLanguageModel defaultModel = ConfigurationUtil.createDefaultChatModel();
            System.out.println("✅ Default chat model created successfully!");

            // Step 5: Create a custom chat model with specific settings
            System.out.println("\n🎛️  Step 5: Creating Custom Chat Model...");
            ChatLanguageModel customModel = config.createCustomChatModel(
                "gpt-3.5-turbo",    // Model name
                0.9,                // Higher temperature for more creativity
                1500,               // More tokens for longer responses
                45                  // Longer timeout
            );
            System.out.println("✅ Custom chat model created successfully!");

            // Step 6: Display model configuration details
            System.out.println("\n📊 Step 6: Configuration Details:");
            System.out.println("Default Model Settings:");
            System.out.println("  - Model: " + config.getModelName());
            System.out.println("  - Temperature: " + config.getTemperature());
            System.out.println("  - Max Tokens: " + config.getMaxTokens());
            System.out.println("  - Timeout: " + config.getTimeoutSeconds() + " seconds");
            System.out.println("  - API Key: " + config.getApiKeyInfo());

            System.out.println("\nCustom Model Settings:");
            System.out.println("  - Model: gpt-3.5-turbo");
            System.out.println("  - Temperature: 0.9 (more creative)");
            System.out.println("  - Max Tokens: 1500 (longer responses)");
            System.out.println("  - Timeout: 45 seconds");

            System.out.println("\n🎉 Configuration example completed successfully!");
            System.out.println("\n💡 Key Takeaways:");
            System.out.println("   ✓ Use utility classes to organize configuration logic");
            System.out.println("   ✓ Environment variables keep API keys secure");
            System.out.println("   ✓ Static methods provide quick access to common operations");
            System.out.println("   ✓ Instance methods allow detailed configuration control");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

