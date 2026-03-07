/**
 * Example 2: Simple Chat
 *
 * This class demonstrates the most basic LangChain4j operation: having a simple
 * conversation with an AI model. This is your "Hello World" for LangChain4j!
 *
 * Learning Objectives:
 * - How to send a message to an AI model
 * - How to receive and display the response
 * - Understanding the basic request-response pattern
 * - How to handle simple errors
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;

/**
 * Simple executable class to demonstrate basic AI chat
 */
public class Example02_SimpleChat {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j Simple Chat Example ===\n");

        // Step 1: Check if we have an API key using utility class
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        try {
            // Step 2: Create a simple chat model using the utility
            System.out.println("Step 1: Creating AI Chat Model...");
            ChatLanguageModel chatModel = ConfigurationUtil.createDefaultChatModel();
            System.out.println("✅ Chat model created successfully!\n");

            // Step 3: Try some simple conversations
            System.out.println("Step 2: Starting conversations with AI...\n");

            // Conversation 1: Simple greeting
            System.out.println("🗣️  Conversation 1: Greeting");
            askQuestion(chatModel, "Hello! Please introduce yourself in one sentence.");

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Conversation 2: Ask about AI
            System.out.println("🗣️  Conversation 2: About AI");
            askQuestion(chatModel, "What is artificial intelligence? Explain in simple terms.");

            System.out.println("\n" + "=".repeat(50) + "\n");

            // Conversation 3: Ask for help
            System.out.println("🗣️  Conversation 3: Getting Help");
            askQuestion(chatModel, "I'm learning Java programming. Give me one useful tip.");

            System.out.println("\n✅ All conversations completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error during chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ask a question to the AI and display the conversation
     *
     * @param chatModel The AI model to ask
     * @param question The question to ask
     */
    private static void askQuestion(ChatLanguageModel chatModel, String question) {
        try {
            // Record when we start (for timing)
            long startTime = System.currentTimeMillis();

            // Display what we're asking
            System.out.println("👤 Human: " + question);
            System.out.println("🤔 AI is thinking...");

            // Create a user message from our question
            UserMessage userMessage = UserMessage.from(question);

            // Send the message to the AI model and get response
            Response<AiMessage> response = chatModel.generate(userMessage);

            // Calculate how long it took
            long duration = System.currentTimeMillis() - startTime;

            // Get the AI's response text
            String aiResponse = response.content().text();

            // Display the AI's response
            System.out.println("🤖 AI: " + aiResponse);

            // Show some stats about the conversation
            System.out.println("\n📊 Stats:");
            System.out.println("   ⏱️  Response time: " + duration + " milliseconds");
            System.out.println("   📝 Question length: " + question.length() + " characters");
            System.out.println("   💬 Response length: " + aiResponse.length() + " characters");

        } catch (Exception e) {
            // If something goes wrong, show a helpful error message
            System.err.println("❌ Failed to get response: " + e.getMessage());

            // Show some common solutions
            System.err.println("💡 Common solutions:");
            System.err.println("   - Check your internet connection");
            System.err.println("   - Verify your API key is correct");
            System.err.println("   - Make sure you have API credits remaining");
        }
    }
}

