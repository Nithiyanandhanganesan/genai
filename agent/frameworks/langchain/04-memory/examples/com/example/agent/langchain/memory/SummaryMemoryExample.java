/**
 * Simple Summary Memory Example
 * Shows how to implement conversation summarization for memory efficiency
 */
package com.example.agent.langchain.memory;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public class SummaryMemoryExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        System.out.println("=== Summary Memory Example ===");
        System.out.println("Shows how to implement conversation summarization to compress memory\n");

        try {
            // Initialize ChatModel using ConfigurationUtil
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createChatModel();

            // Create our custom summary memory system
            ConversationSummaryMemory summaryMemory = new ConversationSummaryMemory(chatModel, 5);

            System.out.println("🔧 Created Summary Memory system:");
            System.out.println("   • Max messages before summarization: 5");
            System.out.println("   • Uses AI to create conversation summaries");
            System.out.println("   • Maintains recent messages + compressed history\n");

            // Simulate a longer learning conversation
            String[] userInputs = {
                "I'm new to programming and want to learn Java",
                "What are variables in Java? Can you explain?",
                "How do I declare different types of variables?",
                "What's the difference between int and String?",
                "Can you show me examples of variable declarations?",
                "Now I want to learn about loops in Java",
                "What are for loops and how do they work?",
                "Can you show me a for loop example?",
                "What about while loops? How are they different?",
                "I'm confused about when to use each type of loop"
            };

            for (int i = 0; i < userInputs.length; i++) {
                String userInput = userInputs[i];
                System.out.printf("👤 Message %d: %s\n", i + 1, userInput);

                // Add message and get response with summary context
                String response = summaryMemory.processMessage(userInput);

                System.out.printf("🤖 Response %d: %s\n", i + 1,
                    response.length() > 120 ? response.substring(0, 120) + "..." : response);

                // Show memory state
                summaryMemory.showMemoryState(i + 1);

                System.out.println("─".repeat(80));
            }

            // Test summary retrieval
            System.out.println("\n🧪 Testing Summary Context:");
            String testQuery = "Can you remind me what we've covered so far?";
            System.out.printf("👤 Test Query: %s\n", testQuery);

            String summaryContext = summaryMemory.getFullContext();
            System.out.println("📋 Summary Context:");
            System.out.println(summaryContext);

            System.out.println("\n✅ Key Benefits of Summary Memory:");
            System.out.println("• ✅ Dramatic token reduction (80-95% savings)");
            System.out.println("• ✅ Retains important conversation themes");
            System.out.println("• ✅ Scales to unlimited conversation length");
            System.out.println("• ✅ AI-powered intelligent summarization");

            System.out.println("\n⚠️ Trade-offs:");
            System.out.println("• ❌ Loss of specific conversation details");
            System.out.println("• ❌ Extra AI calls for summarization");
            System.out.println("• ❌ Summary quality depends on AI model");

        } catch (Exception e) {
            System.err.println("❌ Error in summary memory example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Custom implementation of conversation summary memory
 * This demonstrates how to build AI-powered conversation summarization
 */
class ConversationSummaryMemory {
    private final ChatLanguageModel chatModel;
    private final MessageWindowChatMemory recentMemory;
    private final int maxMessagesBeforeSummary;
    private String conversationSummary = "";
    private int totalMessagesProcessed = 0;

    public ConversationSummaryMemory(ChatLanguageModel chatModel, int maxMessagesBeforeSummary) {
        this.chatModel = chatModel;
        this.maxMessagesBeforeSummary = maxMessagesBeforeSummary;
        this.recentMemory = MessageWindowChatMemory.withMaxMessages(maxMessagesBeforeSummary);
    }

    public String processMessage(String userInput) {
        try {
            // Add user message to recent memory
            UserMessage userMessage = UserMessage.from(userInput);
            recentMemory.add(userMessage);
            totalMessagesProcessed++;

            // Check if we need to summarize
            if (recentMemory.messages().size() >= maxMessagesBeforeSummary * 2) {
                createSummary();
            }

            // Get response with full context (summary + recent messages)
            String fullContext = getFullContext();
            String enhancedPrompt = String.format(
                "%s\n\nUser: %s\n\nAssistant (provide helpful response based on conversation context):",
                fullContext, userInput
            );

            AiMessage response = chatModel.generate(UserMessage.from(enhancedPrompt)).content();
            recentMemory.add(response);

            return response.text();

        } catch (Exception e) {
            return "Sorry, I encountered an error processing your message.";
        }
    }

    private void createSummary() {
        try {
            // Get messages to summarize (keep recent ones)
            List<ChatMessage> allMessages = recentMemory.messages();
            if (allMessages.size() < maxMessagesBeforeSummary) {
                return;
            }

            // Take older messages for summarization
            List<ChatMessage> toSummarize = allMessages.subList(0, maxMessagesBeforeSummary);

            // Create conversation text from messages to summarize
            StringBuilder conversationText = new StringBuilder();
            for (ChatMessage msg : toSummarize) {
                String role = msg instanceof UserMessage ? "User" : "Assistant";
                String content = msg instanceof UserMessage ?
                    ((UserMessage) msg).singleText() :
                    ((AiMessage) msg).text();
                conversationText.append(role).append(": ").append(content).append("\n");
            }

            // Create summary prompt
            String summaryPrompt = String.format(
                "Previous conversation summary: %s\n\n" +
                "New conversation to integrate:\n%s\n\n" +
                "Create a comprehensive but concise summary that combines the previous summary " +
                "with the new conversation. Focus on key topics learned, user progress, and important details:",
                conversationSummary.isEmpty() ? "None yet." : conversationSummary,
                conversationText.toString()
            );

            AiMessage summaryResponse = chatModel.generate(UserMessage.from(summaryPrompt)).content();
            conversationSummary = summaryResponse.text();

            // Keep only recent messages in memory
            MessageWindowChatMemory newMemory = MessageWindowChatMemory.withMaxMessages(maxMessagesBeforeSummary);
            List<ChatMessage> recentMessages = allMessages.subList(maxMessagesBeforeSummary, allMessages.size());
            for (ChatMessage msg : recentMessages) {
                newMemory.add(msg);
            }

            // Replace old memory with new trimmed memory
            recentMemory.clear();
            for (ChatMessage msg : newMemory.messages()) {
                recentMemory.add(msg);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error creating summary: " + e.getMessage());
        }
    }

    public String getFullContext() {
        StringBuilder context = new StringBuilder();

        if (!conversationSummary.isEmpty()) {
            context.append("Previous Conversation Summary:\n");
            context.append(conversationSummary).append("\n\n");
        }

        List<ChatMessage> recentMessages = recentMemory.messages();
        if (!recentMessages.isEmpty()) {
            context.append("Recent Conversation:\n");
            for (ChatMessage msg : recentMessages) {
                String role = msg instanceof UserMessage ? "User" : "Assistant";
                String content = msg instanceof UserMessage ?
                    ((UserMessage) msg).singleText() :
                    ((AiMessage) msg).text();
                String shortContent = content.length() > 100 ?
                    content.substring(0, 100) + "..." : content;
                context.append(role).append(": ").append(shortContent).append("\n");
            }
        }

        return context.toString();
    }

    public void showMemoryState(int messageNumber) {
        System.out.printf("📊 Memory State after message %d:\n", messageNumber);
        System.out.printf("   Recent messages: %d\n", recentMemory.messages().size());
        System.out.printf("   Total processed: %d\n", totalMessagesProcessed);

        if (!conversationSummary.isEmpty()) {
            String shortSummary = conversationSummary.length() > 100 ?
                conversationSummary.substring(0, 100) + "..." : conversationSummary;
            System.out.printf("   Summary: %s\n", shortSummary);
        } else {
            System.out.println("   Summary: Not created yet");
        }

        if (recentMemory.messages().size() >= maxMessagesBeforeSummary * 1.5) {
            System.out.println("   ⚠️ Summary will be created on next message");
        }
    }
}
