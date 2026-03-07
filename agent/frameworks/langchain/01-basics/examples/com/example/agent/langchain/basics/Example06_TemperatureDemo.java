/**
 * Temperature Demonstration Example
 *
 * This class demonstrates how different temperature values affect AI responses.
 * Run this to see the practical difference between temperature settings!
 *
 * Learning Objectives:
 * - Understand what temperature does in practice
 * - See how different values produce different response styles
 * - Learn when to use different temperature settings
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;

/**
 * Executable class demonstrating temperature effects on AI responses
 */
public class Example06_TemperatureDemo {

    public static void main(String[] args) {
        System.out.println("=== Temperature Demonstration Example ===\n");

        // Check if we have an API key
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        try {
            System.out.println("🌡️  UNDERSTANDING TEMPERATURE IN AI MODELS");
            System.out.println();
            System.out.println("Temperature controls how 'creative' or 'random' the AI's responses are.");
            System.out.println("Let's see this in action with the same question at different temperatures!\n");

            // The question we'll ask at different temperatures
            String question = "Describe a sunny day in a park.";
            System.out.println("📝 Question: \"" + question + "\"\n");

            // Test different temperature values
            double[] temperatures = {0.0, 0.3, 0.7, 1.0, 1.5};
            String[] temperatureDescriptions = {
                "Very Predictable - Always chooses most likely words",
                "Slightly Varied - Good for factual content",
                "Balanced - Recommended for most uses",
                "Creative - Good for storytelling",
                "Highly Creative - Experimental responses"
            };

            ConfigurationUtil config = ConfigurationUtil.create();

            for (int i = 0; i < temperatures.length; i++) {
                double temp = temperatures[i];
                String description = temperatureDescriptions[i];

                System.out.println("🌡️  TEMPERATURE: " + temp + " - " + description);
                System.out.println("=".repeat(60));

                try {
                    // Create a model with this specific temperature
                    ChatLanguageModel model = config.createCustomChatModel(
                        "gpt-4o-mini",      // Using cheapest model for learning!
                        temp,               // Temperature we're testing
                        300,                // Shorter responses for comparison (saves money)
                        30                  // Timeout
                    );

                    // Ask the question
                    long startTime = System.currentTimeMillis();
                    Response<AiMessage> response = model.generate(UserMessage.from(question));
                    long duration = System.currentTimeMillis() - startTime;

                    String answer = response.content().text();

                    System.out.println("🤖 AI Response:");
                    System.out.println(answer);
                    System.out.println();
                    System.out.println("📊 Response Stats:");
                    System.out.println("   ⏱️  Time: " + duration + "ms");
                    System.out.println("   📝 Length: " + answer.length() + " characters");
                    System.out.println("   📖 Words: ~" + countWords(answer));

                } catch (Exception e) {
                    System.out.println("❌ Error at temperature " + temp + ": " + e.getMessage());
                }

                System.out.println("\n" + "=".repeat(60) + "\n");

                // Small delay between requests to be nice to the API
                Thread.sleep(1000);
            }

            // Summary and recommendations
            System.out.println("🎯 TEMPERATURE RECOMMENDATIONS:");
            System.out.println();
            System.out.println("📋 For Code Generation:");
            System.out.println("   Use temperature 0.0-0.2 for consistent, predictable code");
            System.out.println();
            System.out.println("❓ For Q&A and Facts:");
            System.out.println("   Use temperature 0.3-0.5 for accurate but slightly varied answers");
            System.out.println();
            System.out.println("💬 For General Conversation:");
            System.out.println("   Use temperature 0.7-0.9 for natural, engaging responses");
            System.out.println();
            System.out.println("✍️  For Creative Writing:");
            System.out.println("   Use temperature 1.0-1.3 for interesting and varied content");
            System.out.println();
            System.out.println("🔬 For Brainstorming:");
            System.out.println("   Use temperature 1.5-2.0 for unexpected and creative ideas");
            System.out.println();
            System.out.println("⚠️  Warning: Temperature above 1.5 can produce inconsistent results!");

        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simple word counting utility
     */
    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}

/**
 * Additional helper class for temperature analysis
 */
class TemperatureAnalyzer {

    /**
     * Analyze the creativity of a response
     * @param response The AI response to analyze
     * @return A creativity score from 1-10
     */
    public static int analyzeCreativity(String response) {
        if (response == null || response.trim().isEmpty()) {
            return 0;
        }

        int score = 5; // Base score

        // Check for creative indicators
        if (response.contains("!")) score += 1;                    // Excitement
        if (response.matches(".*[A-Z]{2,}.*")) score += 1;         // Emphasis
        if (response.contains("...")) score += 1;                 // Dramatic pauses
        if (countUniqueWords(response) > 20) score += 1;          // Vocabulary variety
        if (response.split("\\.").length > 3) score += 1;        // Multiple sentences

        // Check for very predictable patterns (reduce score)
        if (response.startsWith("The ") && response.contains(" is ")) score -= 1;
        if (response.toLowerCase().contains("beautiful day")) score -= 1; // Common phrase

        return Math.max(1, Math.min(10, score));
    }

    /**
     * Count unique words in text (rough estimate of vocabulary variety)
     */
    private static int countUniqueWords(String text) {
        if (text == null) return 0;

        String[] words = text.toLowerCase()
                            .replaceAll("[^a-zA-Z\\s]", "")
                            .split("\\s+");

        return (int) java.util.Arrays.stream(words)
                                   .distinct()
                                   .count();
    }

    /**
     * Suggest optimal temperature for a given use case
     */
    public static double suggestTemperature(String useCase) {
        switch (useCase.toLowerCase()) {
            case "code":
            case "programming":
            case "math":
                return 0.1;

            case "qa":
            case "questions":
            case "facts":
                return 0.3;

            case "chat":
            case "conversation":
            case "general":
                return 0.7;

            case "writing":
            case "creative":
            case "story":
                return 1.0;

            case "brainstorm":
            case "ideas":
            case "experimental":
                return 1.5;

            default:
                return 0.7; // Safe default
        }
    }
}
