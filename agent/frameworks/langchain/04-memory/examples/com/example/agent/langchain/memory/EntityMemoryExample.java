/**
 * Simple Entity Memory Example
 * Shows how to extract and remember entities from conversations
 */
package com.example.agent.langchain.memory;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.*;
import java.util.regex.Pattern;

public class EntityMemoryExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        System.out.println("=== Entity Memory Example ===");
        System.out.println("Shows how to extract and remember entities from conversations\n");

        try {
            // Initialize ChatModel using ConfigurationUtil
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createChatModel();

            // Create regular memory for conversation
            MessageWindowChatMemory conversationMemory = MessageWindowChatMemory.withMaxMessages(10);

            // Create simple entity tracker
            SimpleEntityTracker entityTracker = new SimpleEntityTracker(chatModel);

            System.out.println("🔧 Created Entity Memory system\n");

            // Simulate a conversation with personal information
            String[] userInputs = {
                "Hi, I'm John Smith and I work as a software engineer at Google",
                "I live in San Francisco and I love Python programming",
                "My favorite programming languages are Python and Java",
                "I have 5 years of experience in backend development",
                "I'm working on a machine learning project using TensorFlow",
                "My colleague Alice Johnson is helping me with the data analysis",
                "We're building an e-commerce recommendation system",
                "The project deadline is next month"
            };

            for (int i = 0; i < userInputs.length; i++) {
                String userInput = userInputs[i];
                System.out.printf("👤 User Message %d: %s\n", i + 1, userInput);

                // Extract entities from user input
                entityTracker.processMessage(userInput);

                // Add to conversation memory
                UserMessage userMessage = UserMessage.from(userInput);
                conversationMemory.add(userMessage);

                // Generate response with entity context
                String entityContext = entityTracker.getEntityContext();
                String enhancedPrompt = String.format(
                    "Based on what I know about the user: %s\n\nUser message: %s\n\nProvide a helpful response:",
                    entityContext, userInput
                );

                AiMessage response = chatModel.generate(UserMessage.from(enhancedPrompt)).content();
                conversationMemory.add(response);

                System.out.printf("🤖 AI Response %d: %s\n", i + 1,
                    response.text().length() > 100 ?
                        response.text().substring(0, 100) + "..." :
                        response.text());

                // Show extracted entities
                if ((i + 1) % 3 == 0) { // Show entities every 3 messages
                    System.out.println("\n📊 Extracted Entities:");
                    entityTracker.displayEntities();
                    System.out.println();
                }

                System.out.println("─".repeat(60));
            }

            // Test entity retrieval
            System.out.println("\n🧪 Testing Entity Retrieval:");
            System.out.println("Query: What do we know about the user's work and preferences?");

            String fullEntityContext = entityTracker.getDetailedEntityContext();
            System.out.println("📋 Complete Entity Knowledge:");
            System.out.println(fullEntityContext);

            System.out.println("\n✅ Key Benefits of Entity Memory:");
            System.out.println("• ✅ Remembers important facts about users");
            System.out.println("• ✅ Enables personalized responses");
            System.out.println("• ✅ Doesn't grow with conversation length");
            System.out.println("• ✅ Structured information storage");

            System.out.println("\n⚠️ Considerations:");
            System.out.println("• ❌ Requires good entity extraction");
            System.out.println("• ❌ May miss context-dependent information");
            System.out.println("• ❌ Needs privacy considerations");

        } catch (Exception e) {
            System.err.println("❌ Error in entity memory example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Simple entity tracker that extracts and stores entities
 */
class SimpleEntityTracker {
    private final ChatLanguageModel chatModel;
    private final Map<String, String> entities;
    private final Map<String, List<String>> entityCategories;

    public SimpleEntityTracker(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.entities = new HashMap<>();
        this.entityCategories = new HashMap<>();
        initializeCategories();
    }

    private void initializeCategories() {
        entityCategories.put("personal", new ArrayList<>());
        entityCategories.put("work", new ArrayList<>());
        entityCategories.put("skills", new ArrayList<>());
        entityCategories.put("projects", new ArrayList<>());
        entityCategories.put("preferences", new ArrayList<>());
    }

    public void processMessage(String message) {
        // Use simple patterns and AI to extract entities
        extractBasicEntities(message);
        extractUsingAI(message);
    }

    private void extractBasicEntities(String message) {
        // Extract name pattern
        Pattern namePattern = Pattern.compile("I'm ([A-Z][a-z]+ [A-Z][a-z]+)");
        java.util.regex.Matcher nameMatcher = namePattern.matcher(message);
        if (nameMatcher.find()) {
            entities.put("name", nameMatcher.group(1));
            entityCategories.get("personal").add("name: " + nameMatcher.group(1));
        }

        // Extract company pattern
        if (message.toLowerCase().contains("work at") || message.toLowerCase().contains("work for")) {
            String[] words = message.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].toLowerCase().equals("at") || words[i].toLowerCase().equals("for")) {
                    String company = words[i + 1].replaceAll("[^A-Za-z]", "");
                    if (company.length() > 2) {
                        entities.put("company", company);
                        entityCategories.get("work").add("company: " + company);
                    }
                }
            }
        }

        // Extract location
        if (message.toLowerCase().contains("live in") || message.toLowerCase().contains("from")) {
            String[] words = message.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].toLowerCase().equals("in") && i > 0 &&
                    (words[i-1].toLowerCase().equals("live") || words[i-1].toLowerCase().equals("from"))) {
                    String location = words[i + 1].replaceAll("[^A-Za-z]", "");
                    if (location.length() > 2) {
                        entities.put("location", location);
                        entityCategories.get("personal").add("location: " + location);
                    }
                }
            }
        }
    }

    private void extractUsingAI(String message) {
        try {
            String extractionPrompt = String.format(
                "Extract key information from this message. List any:\n" +
                "- Skills/technologies mentioned\n" +
                "- Projects or work mentioned\n" +
                "- Preferences stated\n" +
                "- Professional details\n\n" +
                "Message: %s\n\n" +
                "Format as: category: item (one per line, be concise)",
                message
            );

            AiMessage extraction = chatModel.generate(UserMessage.from(extractionPrompt)).content();
            parseAIExtraction(extraction.text());

        } catch (Exception e) {
            // Silently continue if AI extraction fails
        }
    }

    private void parseAIExtraction(String extraction) {
        String[] lines = extraction.split("\n");
        for (String line : lines) {
            line = line.trim().toLowerCase();
            if (line.contains("skill") || line.contains("technology") || line.contains("language")) {
                String skill = extractValue(line);
                if (!skill.isEmpty()) {
                    entityCategories.get("skills").add(skill);
                }
            } else if (line.contains("project") || line.contains("work")) {
                String project = extractValue(line);
                if (!project.isEmpty()) {
                    entityCategories.get("projects").add(project);
                }
            } else if (line.contains("prefer") || line.contains("favorite") || line.contains("like")) {
                String preference = extractValue(line);
                if (!preference.isEmpty()) {
                    entityCategories.get("preferences").add(preference);
                }
            }
        }
    }

    private String extractValue(String line) {
        if (line.contains(":")) {
            String[] parts = line.split(":", 2);
            return parts[1].trim();
        }
        return "";
    }

    public String getEntityContext() {
        StringBuilder context = new StringBuilder();
        if (!entities.isEmpty()) {
            context.append("User facts: ");
            entities.forEach((key, value) ->
                context.append(key).append(": ").append(value).append(", "));
        }
        return context.toString();
    }

    public String getDetailedEntityContext() {
        StringBuilder context = new StringBuilder("What I know about the user:\n");

        entityCategories.forEach((category, items) -> {
            if (!items.isEmpty()) {
                context.append(String.format("• %s: %s\n",
                    category.substring(0, 1).toUpperCase() + category.substring(1),
                    String.join(", ", new HashSet<>(items)))); // Remove duplicates
            }
        });

        if (context.length() == "What I know about the user:\n".length()) {
            context.append("• No specific entities extracted yet");
        }

        return context.toString();
    }

    public void displayEntities() {
        entityCategories.forEach((category, items) -> {
            if (!items.isEmpty()) {
                System.out.printf("   %s: %s\n",
                    category.substring(0, 1).toUpperCase() + category.substring(1),
                    String.join(", ", new HashSet<>(items)));
            }
        });
    }
}
