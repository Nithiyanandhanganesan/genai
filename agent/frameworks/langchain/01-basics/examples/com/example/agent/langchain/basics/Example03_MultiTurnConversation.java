/**
 * Example 3: Multi-Turn Conversation
 *
 * This class demonstrates how to have a back-and-forth conversation with an AI
 * where the AI remembers what was said earlier. This is like having a real
 * conversation where context matters!
 *
 * Learning Objectives:
 * - How to maintain conversation history
 * - Understanding the difference between single messages and conversations
 * - How to build a conversation step by step
 * - How the AI uses previous context to give better responses
 */
package com.example.agent.langchain.basics;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.output.Response;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Executable class demonstrating multi-turn conversations
 */
public class Example03_MultiTurnConversation {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j Multi-Turn Conversation Example ===\n");

        // Step 1: Check if we have an API key using utility class
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        try {
            // Step 2: Create a chat model optimized for conversations
            System.out.println("Step 1: Creating AI Chat Model...");
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createCustomChatModel(
                "gpt-4o-mini",         // Cheapest model with excellent quality
                0.8,                   // Slightly more creative for teaching
                800,                   // Longer responses for explanations
                45                     // A bit more time for thoughtful responses
            );
            System.out.println("✅ Chat model created!\n");

            // Step 3: Create a conversation manager
            System.out.println("Step 2: Starting a new conversation...");
            ConversationManager conversation = new ConversationManager(chatModel);

            // Optional: Set a system message to guide the AI's behavior
            conversation.setSystemMessage("You are a helpful assistant teaching someone about Java programming. " +
                                        "Be encouraging, clear, and give practical examples.");

            System.out.println("✅ Conversation started!\n");

            // Step 4: Have a multi-turn conversation about learning Java
            System.out.println("🗣️  Starting conversation about Java learning...\n");

            // Turn 1: Ask about getting started
            conversation.addUserMessage("Hi! I'm completely new to programming. Should I start with Java?");
            conversation.getAiResponse();

            // Turn 2: Ask a follow-up question based on the response
            conversation.addUserMessage("That sounds good! What's the very first thing I should learn?");
            conversation.getAiResponse();

            // Turn 3: Ask for specific guidance
            conversation.addUserMessage("Can you give me a simple example of Java code that I can try?");
            conversation.getAiResponse();

            // Turn 4: Ask about the example
            conversation.addUserMessage("What does 'public static void main' mean in that example?");
            conversation.getAiResponse();

            // Step 5: Show conversation summary
            System.out.println("\n" + "=".repeat(60));
            conversation.showConversationSummary();

        } catch (Exception e) {
            System.err.println("❌ Error during conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * ConversationManager handles the back-and-forth conversation with the AI
 */
class ConversationManager {

    private final ChatLanguageModel chatModel;
    private final List<ChatMessage> conversationHistory;
    private int turnCounter;

    /**
     * Create a new conversation manager
     * @param chatModel The AI model to chat with
     */
    public ConversationManager(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.conversationHistory = new ArrayList<>();
        this.turnCounter = 0;
    }

    /**
     * Set a system message to guide how the AI behaves
     * Think of this as giving the AI instructions about its personality or role
     *
     * @param systemPrompt Instructions for the AI
     */
    public void setSystemMessage(String systemPrompt) {
        // Add system message at the beginning of conversation
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        conversationHistory.add(0, systemMessage);

        System.out.println("📋 System instruction set: " + systemPrompt + "\n");
    }

    /**
     * Add a message from the user to the conversation
     * @param message What the user wants to say
     */
    public void addUserMessage(String message) {
        turnCounter++;

        // Create a user message and add it to our conversation history
        UserMessage userMessage = UserMessage.from(message);
        conversationHistory.add(userMessage);

        // Display what the user said
        System.out.println("🔄 Turn " + turnCounter);
        System.out.println("👤 Human: " + message);
    }

    /**
     * Get a response from the AI based on the entire conversation so far
     */
    public void getAiResponse() {
        try {
            long startTime = System.currentTimeMillis();

            System.out.println("🤔 AI is thinking about the conversation...");

            // Send the ENTIRE conversation history to the AI
            // This is what makes it a "multi-turn" conversation - the AI sees everything!
            Response<AiMessage> response = chatModel.generate(conversationHistory);

            long duration = System.currentTimeMillis() - startTime;

            // Get the AI's response
            AiMessage aiMessage = response.content();
            String aiResponse = aiMessage.text();

            // Add the AI's response to our conversation history
            conversationHistory.add(aiMessage);

            // Display the AI's response
            System.out.println("🤖 AI: " + aiResponse);

            // Show some stats
            System.out.println("📊 Response time: " + duration + "ms, " +
                             "Response length: " + aiResponse.length() + " characters\n");

        } catch (Exception e) {
            System.err.println("❌ Failed to get AI response: " + e.getMessage());

            // Still increment the conversation, but add an error marker
            conversationHistory.add(AiMessage.from("Error: Could not get response"));
        }
    }

    /**
     * Show a summary of the entire conversation
     */
    public void showConversationSummary() {
        System.out.println("📋 CONVERSATION SUMMARY");
        System.out.println("Total turns: " + turnCounter);
        System.out.println("Total messages in history: " + conversationHistory.size());

        // Calculate some statistics
        int userMessages = 0;
        int aiMessages = 0;
        int totalCharacters = 0;

        for (ChatMessage message : conversationHistory) {
            if (message instanceof UserMessage) {
                userMessages++;
            } else if (message instanceof AiMessage) {
                aiMessages++;
            }
            // Note: We don't count SystemMessage in user/AI counts

            totalCharacters += message.text().length();
        }

        System.out.println("User messages: " + userMessages);
        System.out.println("AI messages: " + aiMessages);
        System.out.println("Total characters exchanged: " + totalCharacters);

        // Show the conversation flow
        System.out.println("\n📝 Conversation Flow:");
        int messageNumber = 1;
        for (ChatMessage message : conversationHistory) {
            String speaker;
            if (message instanceof SystemMessage) {
                speaker = "🔧 System";
            } else if (message instanceof UserMessage) {
                speaker = "👤 Human";
            } else {
                speaker = "🤖 AI";
            }

            // Show first 100 characters of each message
            String preview = message.text();
            if (preview.length() > 100) {
                preview = preview.substring(0, 100) + "...";
            }

            System.out.println(messageNumber + ". " + speaker + ": " + preview);
            messageNumber++;
        }
    }

    /**
     * Get the current conversation history (useful for debugging or analysis)
     * @return A copy of the conversation history
     */
    public List<ChatMessage> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    /**
     * Clear the conversation and start fresh
     */
    public void clearConversation() {
        conversationHistory.clear();
        turnCounter = 0;
        System.out.println("🧹 Conversation cleared - starting fresh!");
    }
}

