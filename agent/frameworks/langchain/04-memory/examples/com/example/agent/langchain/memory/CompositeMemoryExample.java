/**
 * Simple Composite Memory Example
 * Shows how to combine different memory types for optimal performance
 */
package com.example.agent.langchain.memory;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;

import java.util.*;

public class CompositeMemoryExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        System.out.println("=== Composite Memory Example ===");
        System.out.println("Shows how to combine multiple memory types for optimal performance\n");

        try {
            // Initialize ChatModel using ConfigurationUtil
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createChatModel();

            // Create composite memory system
            CompositeMemorySystem memorySystem = new CompositeMemorySystem(chatModel);

            System.out.println("🔧 Created Composite Memory System with:");
            System.out.println("   • Recent Memory: Last 5 messages");
            System.out.println("   • Session Summary: Compressed older context");
            System.out.println("   • User Profile: Persistent user information");
            System.out.println("   • Important Facts: Key information repository\n");

            // Simulate a multi-session conversation
            simulateSession1(memorySystem, chatModel);
            simulateSession2(memorySystem, chatModel);
            demonstrateCompositeRetrieval(memorySystem, chatModel);

            System.out.println("\n✅ Key Benefits of Composite Memory:");
            System.out.println("• ✅ Best of all memory types combined");
            System.out.println("• ✅ Recent context + historical knowledge");
            System.out.println("• ✅ Efficient token usage");
            System.out.println("• ✅ Persistent user understanding");

            System.out.println("\n⚠️ Considerations:");
            System.out.println("• ❌ More complex to implement");
            System.out.println("• ❌ Requires coordination between memory types");
            System.out.println("• ❌ May need fine-tuning for specific use cases");

        } catch (Exception e) {
            System.err.println("❌ Error in composite memory example: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void simulateSession1(CompositeMemorySystem memory, ChatLanguageModel chatModel) {
        System.out.println("📅 Session 1: Initial Learning Session");
        System.out.println("─".repeat(50));

        String[] session1Inputs = {
            "Hi, I'm Sarah and I'm a beginner at Java programming",
            "I want to learn about loops in Java",
            "Can you explain for loops with an example?",
            "What about while loops? How are they different?",
            "I prefer visual examples when learning new concepts"
        };

        processConversation(session1Inputs, memory, chatModel, "Session 1");

        // End session and create summary
        memory.endSession();
        System.out.println("✅ Session 1 ended - creating summary for future reference\n");
    }

    private static void simulateSession2(CompositeMemorySystem memory, ChatLanguageModel chatModel) {
        System.out.println("📅 Session 2: Follow-up Session (Next Day)");
        System.out.println("─".repeat(50));

        String[] session2Inputs = {
            "Hi again! I practiced loops yesterday",
            "Now I want to learn about arrays",
            "How do I create and use arrays in Java?",
            "Can you show me how loops work with arrays?"
        };

        processConversation(session2Inputs, memory, chatModel, "Session 2");
        memory.endSession();
        System.out.println("✅ Session 2 ended\n");
    }

    private static void demonstrateCompositeRetrieval(CompositeMemorySystem memory, ChatLanguageModel chatModel) {
        System.out.println("🧪 Testing Composite Memory Retrieval");
        System.out.println("─".repeat(50));

        String testQuery = "What programming concepts have we covered and what are my learning preferences?";
        System.out.printf("👤 Test Query: %s\n\n", testQuery);

        // Get comprehensive context from all memory types
        String compositeContext = memory.getCompositeContext(testQuery);
        System.out.println("📋 Composite Memory Context:");
        System.out.println(compositeContext);

        // Generate response using composite context
        String enhancedPrompt = String.format(
            "%s\n\nUser question: %s\n\nProvide a comprehensive response based on our conversation history:",
            compositeContext, testQuery
        );

        AiMessage response = chatModel.generate(UserMessage.from(enhancedPrompt)).content();
        System.out.printf("🤖 AI Response: %s\n", response.text());
    }

    private static void processConversation(String[] inputs, CompositeMemorySystem memory,
                                          ChatLanguageModel chatModel, String sessionName) {
        for (int i = 0; i < inputs.length; i++) {
            String userInput = inputs[i];
            System.out.printf("👤 %s Message %d: %s\n", sessionName, i + 1, userInput);

            // Add to memory and get context
            String context = memory.addMessageAndGetContext(userInput);

            // Generate response with context
            String prompt = String.format("%s\n\nUser: %s\n\nAssistant:", context, userInput);
            AiMessage response = chatModel.generate(UserMessage.from(prompt)).content();

            // Add response to memory
            memory.addResponse(response.text());

            System.out.printf("🤖 %s Response %d: %s\n", sessionName, i + 1,
                response.text().length() > 80 ?
                    response.text().substring(0, 80) + "..." :
                    response.text());
        }
    }
}

/**
 * Composite Memory System that combines multiple memory types
 */
class CompositeMemorySystem {
    private final MessageWindowChatMemory recentMemory;
    private final List<String> sessionSummaries;
    private final Map<String, String> userProfile;
    private final List<String> importantFacts;
    private final ChatLanguageModel chatModel;

    public CompositeMemorySystem(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.recentMemory = MessageWindowChatMemory.withMaxMessages(10);
        this.sessionSummaries = new ArrayList<>();
        this.userProfile = new HashMap<>();
        this.importantFacts = new ArrayList<>();
    }

    public String addMessageAndGetContext(String userMessage) {
        // Add to recent memory
        UserMessage message = UserMessage.from(userMessage);
        recentMemory.add(message);

        // Extract user profile information
        updateUserProfile(userMessage);

        // Extract important facts
        extractImportantFacts(userMessage);

        // Return composite context
        return getCompositeContext(userMessage);
    }

    public void addResponse(String responseText) {
        AiMessage response = AiMessage.from(responseText);
        recentMemory.add(response);

        // Extract important facts from response too
        extractImportantFacts(responseText);
    }

    public void endSession() {
        // Create session summary from recent memory
        String sessionSummary = createSessionSummary();
        if (!sessionSummary.isEmpty()) {
            sessionSummaries.add(sessionSummary);
        }

        // Keep only last message as bridge to next session
        List<ChatMessage> messages = recentMemory.messages();
        recentMemory.clear();
        if (!messages.isEmpty()) {
            recentMemory.add(messages.get(messages.size() - 1));
        }
    }

    private void updateUserProfile(String message) {
        String lowerMessage = message.toLowerCase();

        // Extract name
        if (lowerMessage.contains("i'm ") || lowerMessage.contains("my name is")) {
            String[] words = message.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].toLowerCase().equals("i'm") ||
                    (words[i].toLowerCase().equals("name") && i < words.length - 2)) {
                    String name = words[i + 1].replaceAll("[^A-Za-z]", "");
                    if (name.length() > 1) {
                        userProfile.put("name", name);
                    }
                }
            }
        }

        // Extract learning style preferences
        if (lowerMessage.contains("prefer") || lowerMessage.contains("like")) {
            if (lowerMessage.contains("visual") || lowerMessage.contains("example")) {
                userProfile.put("learning_style", "visual examples");
            }
        }

        // Extract skill level
        if (lowerMessage.contains("beginner") || lowerMessage.contains("new to")) {
            userProfile.put("skill_level", "beginner");
        }
    }

    private void extractImportantFacts(String text) {
        String lowerText = text.toLowerCase();

        // Look for learning topics
        String[] topics = {"loop", "array", "class", "object", "method", "variable", "function"};
        for (String topic : topics) {
            if (lowerText.contains(topic) && !alreadyKnows(topic)) {
                importantFacts.add("Learned about " + topic + "s");
            }
        }

        // Look for preferences or important statements
        if (lowerText.contains("want to learn") || lowerText.contains("interested in")) {
            String fact = text.length() > 100 ? text.substring(0, 100) + "..." : text;
            importantFacts.add("Goal: " + fact);
        }
    }

    private boolean alreadyKnows(String topic) {
        return importantFacts.stream()
            .anyMatch(fact -> fact.toLowerCase().contains(topic));
    }

    private String createSessionSummary() {
        List<ChatMessage> messages = recentMemory.messages();
        if (messages.isEmpty()) {
            return "";
        }

        try {
            // Create a simple summary of the session
            StringBuilder sessionContent = new StringBuilder();
            for (ChatMessage msg : messages) {
                String content = msg instanceof UserMessage ?
                    ((UserMessage) msg).singleText() :
                    ((AiMessage) msg).text();
                sessionContent.append(content).append(" ");
            }

            String summarizePrompt = String.format(
                "Summarize this learning session in 1-2 sentences, focusing on topics covered and user progress:\n\n%s",
                sessionContent.toString()
            );

            AiMessage summary = chatModel.generate(UserMessage.from(summarizePrompt)).content();
            return summary.text();

        } catch (Exception e) {
            return "Session covered programming concepts";
        }
    }

    public String getCompositeContext(String currentQuery) {
        StringBuilder context = new StringBuilder();

        // Add user profile
        if (!userProfile.isEmpty()) {
            context.append("User Profile: ");
            userProfile.forEach((key, value) ->
                context.append(key).append(": ").append(value).append(", "));
            context.append("\n\n");
        }

        // Add session summaries
        if (!sessionSummaries.isEmpty()) {
            context.append("Previous Sessions:\n");
            for (int i = 0; i < sessionSummaries.size(); i++) {
                context.append(String.format("Session %d: %s\n", i + 1, sessionSummaries.get(i)));
            }
            context.append("\n");
        }

        // Add important facts
        if (!importantFacts.isEmpty()) {
            context.append("Important Facts:\n");
            for (String fact : importantFacts) {
                context.append("• ").append(fact).append("\n");
            }
            context.append("\n");
        }

        // Add recent conversation
        List<ChatMessage> recentMessages = recentMemory.messages();
        if (!recentMessages.isEmpty()) {
            context.append("Recent Conversation:\n");
            for (ChatMessage msg : recentMessages) {
                String type = msg instanceof UserMessage ? "User" : "Assistant";
                String content = msg instanceof UserMessage ?
                    ((UserMessage) msg).singleText() :
                    ((AiMessage) msg).text();
                String shortContent = content.length() > 100 ?
                    content.substring(0, 100) + "..." : content;
                context.append(type).append(": ").append(shortContent).append("\n");
            }
        }

        return context.toString();
    }
}
