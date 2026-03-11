/**
 * Simple Buffer Memory Example
 * Shows how MessageWindowChatMemory keeps recent messages
 */
package com.example.agent.langchain.memory;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public class BufferMemoryExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        System.out.println("=== Buffer Memory Example ===");
        System.out.println("Shows how MessageWindowChatMemory keeps recent conversation history\n");

        try {
            // Initialize ChatModel using ConfigurationUtil
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createChatModel();

            // Create buffer memory that keeps last 3 messages
            MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(3);

            System.out.println("🔧 Created MessageWindowChatMemory with max 3 messages\n");

            // Simulate a conversation
            String[] userInputs = {
                "Hi, I'm learning Java programming",
                "What are the main features of Java?",
                "Can you explain object-oriented programming?",
                "What's the difference between class and object?",
                "How do I create a simple Java program?"
            };

            for (int i = 0; i < userInputs.length; i++) {
                String userInput = userInputs[i];
                System.out.printf("👤 User Message %d: %s\n", i + 1, userInput);

                // Add user message to memory
                UserMessage userMessage = UserMessage.from(userInput);
                memory.add(userMessage);

                // Get conversation context and send to LLM
                List<ChatMessage> messages = memory.messages();
                AiMessage response = chatModel.generate(messages).content();

                // Add AI response to memory
                memory.add(response);

                System.out.printf("🤖 AI Response %d: %s\n", i + 1, response.text());

                // Show current memory state
                List<ChatMessage> currentMemory = memory.messages();
                System.out.printf("📝 Messages in memory: %d\n", currentMemory.size());

                if (i >= 2) { // After 3rd exchange, memory will start dropping old messages
                    System.out.println("⚠️  Memory is now at capacity - older messages will be dropped");
                }

                System.out.println("📋 Current memory contents:");
                for (int j = 0; j < currentMemory.size(); j++) {
                    ChatMessage msg = currentMemory.get(j);
                    String type = msg instanceof UserMessage ? "👤 User" : "🤖 AI";
                    String content = msg instanceof UserMessage ?
                        ((UserMessage) msg).singleText() :
                        ((AiMessage) msg).text();
                    String shortContent = content.length() > 50 ?
                        content.substring(0, 50) + "..." : content;
                    System.out.printf("   %d. %s: %s\n", j + 1, type, shortContent);
                }

                System.out.println("─".repeat(80));
            }

            System.out.println("\n✅ Key Benefits of Buffer Memory:");
            System.out.println("• ✅ Simple and predictable");
            System.out.println("• ✅ Perfect recall of recent messages");
            System.out.println("• ✅ Fixed memory usage - won't grow indefinitely");
            System.out.println("• ✅ Good for short to medium conversations");

            System.out.println("\n⚠️ Limitations:");
            System.out.println("• ❌ Loses older context when buffer fills up");
            System.out.println("• ❌ Hard cutoff may break conversation flow");
            System.out.println("• ❌ No semantic understanding of importance");

        } catch (Exception e) {
            System.err.println("❌ Error in buffer memory example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
