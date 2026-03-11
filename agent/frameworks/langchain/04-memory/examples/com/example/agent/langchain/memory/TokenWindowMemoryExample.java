/**
 * Simple Summary Memory Example
 * Shows how ConversationSummaryMemory compresses old conversations
 */
package com.example.agent.langchain.memory;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public class TokenWindowMemoryExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        System.out.println("=== Summary Memory Example ===");
        System.out.println("Shows how TokenWindowChatMemory manages conversations by token limit\n");

        try {
            // Initialize ChatModel using ConfigurationUtil
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createChatModel();

            // Create tokenizer for counting tokens
            Tokenizer tokenizer = new OpenAiTokenizer("gpt-4o-mini");

            // Create token-based memory with small limit to demonstrate summarization
            TokenWindowChatMemory memory = TokenWindowChatMemory.withMaxTokens(500, tokenizer);

            System.out.println("🔧 Created TokenWindowChatMemory with max 500 tokens\n");

            // Simulate a longer conversation about programming
            String[] userInputs = {
                "I'm new to programming and want to learn Java. Where should I start?",
                "What's the difference between Java and JavaScript? I'm confused.",
                "Can you explain what object-oriented programming means?",
                "How do classes and objects work in Java? Can you give me an example?",
                "What are methods in Java? How do I create and use them?",
                "I keep hearing about inheritance. What is that?",
                "What are packages in Java and why are they important?",
                "How do I handle errors in Java programs?",
                "What's the difference between public, private, and protected?",
                "Can you help me understand arrays in Java?"
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

                System.out.printf("🤖 AI Response %d: %s\n", i + 1,
                    response.text().length() > 100 ?
                        response.text().substring(0, 100) + "..." :
                        response.text());

                // Show current memory state
                List<ChatMessage> currentMemory = memory.messages();
                int totalTokens = currentMemory.stream()
                    .mapToInt(msg -> {
                        String content = msg instanceof UserMessage ?
                            ((UserMessage) msg).singleText() :
                            ((AiMessage) msg).text();
                        return tokenizer.estimateTokenCountInText(content);
                    })
                    .sum();

                System.out.printf("📊 Memory state: %d messages, ~%d tokens\n",
                    currentMemory.size(), totalTokens);

                if (totalTokens > 400) {
                    System.out.println("⚠️  Approaching token limit - older messages may be removed");
                }

                System.out.println("📋 Memory contains:");
                for (int j = 0; j < Math.min(currentMemory.size(), 3); j++) {
                    ChatMessage msg = currentMemory.get(j);
                    String type = msg instanceof UserMessage ? "👤 User" : "🤖 AI";
                    String content = msg instanceof UserMessage ?
                        ((UserMessage) msg).singleText() :
                        ((AiMessage) msg).text();
                    String shortContent = content.length() > 60 ?
                        content.substring(0, 60) + "..." : content;
                    System.out.printf("   %s: %s\n", type, shortContent);
                }

                if (currentMemory.size() > 3) {
                    System.out.printf("   ... and %d more messages\n", currentMemory.size() - 3);
                }

                System.out.println("─".repeat(80));
            }

            // Demonstrate how memory maintains context despite token limits
            System.out.println("\n🧪 Testing Context Retention:");
            String testQuery = "Can you remind me what we discussed about inheritance?";
            System.out.printf("👤 Test Query: %s\n", testQuery);

            UserMessage testMessage = UserMessage.from(testQuery);
            memory.add(testMessage);

            List<ChatMessage> contextMessages = memory.messages();
            AiMessage contextResponse = chatModel.generate(contextMessages).content();

            System.out.printf("🤖 AI Response: %s\n", contextResponse.text());

            System.out.println("\n✅ Key Benefits of Token-Based Memory:");
            System.out.println("• ✅ Precise token control for cost management");
            System.out.println("• ✅ Maintains relevant recent context");
            System.out.println("• ✅ Automatically manages conversation length");
            System.out.println("• ✅ Scales well with long conversations");

            System.out.println("\n⚠️ Considerations:");
            System.out.println("• ❌ May lose important older context");
            System.out.println("• ❌ Token counting adds slight complexity");
            System.out.println("• ❌ Requires tokenizer knowledge");

        } catch (Exception e) {
            System.err.println("❌ Error in summary memory example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
