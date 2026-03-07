///**
// * Memory Systems Examples - Complete Implementation
// * Demonstrates various memory types and patterns for conversation management
// */
//package com.example.agent.langchain.memory;
//
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.UserMessage;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Comprehensive memory systems demonstration
// */
//public class MemorySystemsExampleSuite {
//
//    private static ChatLanguageModel llm;
//
//    public static void main(String[] args) {
//        System.out.println("=== Memory Systems Examples ===");
//
//        // Initialize LLM
//        String apiKey = System.getenv("OPENAI_API_KEY");
//        if (apiKey == null || apiKey.isEmpty()) {
//            System.out.println("Please set OPENAI_API_KEY environment variable");
//            return;
//        }
//
//        llm = OpenAiChatModel.builder()
//            .apiKey(apiKey)
//            .modelName("gpt-3.5-turbo")
//            .temperature(0.7)
//            .build();
//
//        try {
//            // Run all memory examples
//            runBufferMemoryExample();
//            runSummaryMemoryExample();
//            runEntityMemoryExample();
//            runVectorMemoryExample();
//            runCompositeMemoryExample();
//            runPersistentMemoryExample();
//            runAdvancedPatternsExample();
//
//        } catch (Exception e) {
//            System.err.println("Error running memory examples: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Conversation buffer memory demonstration
//     */
//    private static void runBufferMemoryExample() {
//        System.out.println("\n1. Conversation Buffer Memory Example:");
//
//        // Create buffer memory with limit of 5 messages
//        ConversationBufferMemory bufferMemory = new ConversationBufferMemory(5);
//
//        String[] conversation = {
//            "Hi, I'm planning a trip to Japan.",
//            "What are the best places to visit in Tokyo?",
//            "How about traditional food recommendations?",
//            "What's the best time of year to visit?",
//            "Can you help me with a basic itinerary?",
//            "What about accommodation recommendations?"
//        };
//
//        for (String userInput : conversation) {
//            try {
//                // Add user message to memory
//                bufferMemory.addMessage(UserMessage.from(userInput));
//
//                // Get conversation history
//                String history = bufferMemory.getFormattedHistory();
//
//                // Create prompt with history
//                String prompt = String.format(
//                    "Previous conversation:\n%s\n\nUser: %s\n\nAssistant:",
//                    history, userInput
//                );
//
//                // Generate response
//                AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//
//                // Add AI response to memory
//                bufferMemory.addMessage(response);
//
//                System.out.println("User: " + userInput);
//                System.out.println("AI: " + response.text());
//                System.out.println("Memory size: " + bufferMemory.getMessageCount());
//                System.out.println("---");
//
//            } catch (Exception e) {
//                System.err.println("Buffer memory error: " + e.getMessage());
//            }
//        }
//
//        System.out.println("Final conversation history:");
//        System.out.println(bufferMemory.getFormattedHistory());
//    }
//
//    /**
//     * Conversation summary memory demonstration
//     */
//    private static void runSummaryMemoryExample() {
//        System.out.println("\n2. Conversation Summary Memory Example:");
//
//        ConversationSummaryMemory summaryMemory = new ConversationSummaryMemory(llm, 3);
//
//        String[] longConversation = {
//            "I'm a software developer working on a machine learning project.",
//            "We're trying to build a recommendation system for our e-commerce platform.",
//            "The system needs to handle about 100,000 users and 50,000 products.",
//            "We're considering collaborative filtering and content-based approaches.",
//            "What are the pros and cons of each approach?",
//            "How would you handle the cold start problem?",
//            "What about scalability issues with large datasets?",
//            "Can you recommend some specific algorithms or libraries?",
//            "How do we evaluate the performance of our recommendation system?"
//        };
//
//        for (String userInput : longConversation) {
//            try {
//                // Process message with summary memory
//                String context = summaryMemory.addMessageAndGetContext(userInput);
//
//                // Generate response using context
//                String prompt = String.format(
//                    "Context: %s\n\nUser: %s\n\nAssistant:",
//                    context, userInput
//                );
//
//                AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//                summaryMemory.addMessage(response);
//
//                System.out.println("User: " + userInput);
//                System.out.println("AI: " + response.text());
//
//                // Show memory state every few messages
//                if ((longConversation.length - Arrays.asList(longConversation).indexOf(userInput)) % 3 == 0) {
//                    System.out.println("Current summary: " + summaryMemory.getSummary());
//                }
//                System.out.println("---");
//
//            } catch (Exception e) {
//                System.err.println("Summary memory error: " + e.getMessage());
//            }
//        }
//
//        System.out.println("Final conversation summary:");
//        System.out.println(summaryMemory.getSummary());
//    }
//
//    /**
//     * Entity memory demonstration
//     */
//    private static void runEntityMemoryExample() {
//        System.out.println("\n3. Entity Memory Example:");
//
//        EntityMemory entityMemory = new EntityMemory(llm);
//
//        String[] entityConversation = {
//            "Hi, I'm Alice Johnson. I work as a product manager at TechCorp.",
//            "I've been working on Project Phoenix for 6 months now.",
//            "My colleague Bob Smith is the lead developer on this project.",
//            "We're launching the product next quarter in California.",
//            "Bob mentioned that Sarah from the marketing team will handle the launch campaign.",
//            "What do you remember about my project and team?"
//        };
//
//        for (String userInput : entityConversation) {
//            try {
//                // Process message and extract entities
//                entityMemory.processMessage(userInput);
//
//                // Get relevant entity context
//                String entityContext = entityMemory.getRelevantContext(userInput);
//
//                // Generate response with entity awareness
//                String prompt = String.format(
//                    "Entity context: %s\n\nUser: %s\n\nAssistant:",
//                    entityContext, userInput
//                );
//
//                AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//
//                System.out.println("User: " + userInput);
//                System.out.println("AI: " + response.text());
//
//                // Show extracted entities
//                if (userInput.equals(entityConversation[entityConversation.length - 1])) {
//                    System.out.println("\nExtracted entities:");
//                    entityMemory.getAllEntities().forEach((entity, info) ->
//                        System.out.println("  " + entity + ": " + info));
//                }
//                System.out.println("---");
//
//            } catch (Exception e) {
//                System.err.println("Entity memory error: " + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Vector memory demonstration
//     */
//    private static void runVectorMemoryExample() {
//        System.out.println("\n4. Vector Memory Example:");
//
//        // Mock vector memory for demonstration
//        VectorMemory vectorMemory = new VectorMemory(new MockEmbeddingService());
//
//        // Add some knowledge to vector memory
//        String[] knowledgeBase = {
//            "Java is a programming language developed by Sun Microsystems in 1995.",
//            "Spring Framework is a popular Java application framework for enterprise development.",
//            "Maven is a build automation and dependency management tool for Java projects.",
//            "JUnit is a unit testing framework for Java programming language.",
//            "Hibernate is an object-relational mapping framework for Java."
//        };
//
//        for (String knowledge : knowledgeBase) {
//            vectorMemory.addMemory(new MemoryEntry(knowledge, Map.of("type", "technical_knowledge")));
//        }
//
//        String[] questions = {
//            "Tell me about Java programming language",
//            "What is Spring Framework?",
//            "How do I manage dependencies in Java projects?",
//            "What testing frameworks are available for Java?"
//        };
//
//        for (String question : questions) {
//            try {
//                // Retrieve relevant memories
//                List<RelevantMemory> relevantMemories = vectorMemory.retrieveRelevantMemories(question, 2);
//
//                // Build context from relevant memories
//                String context = relevantMemories.stream()
//                    .map(rm -> rm.getEntry().getContent())
//                    .reduce("", (a, b) -> a + "\n" + b);
//
//                // Generate response with context
//                String prompt = String.format(
//                    "Based on this knowledge:\n%s\n\nQuestion: %s\n\nAnswer:",
//                    context, question
//                );
//
//                AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//
//                System.out.println("Question: " + question);
//                System.out.println("Retrieved context: " + context.trim());
//                System.out.println("Answer: " + response.text());
//                System.out.println("---");
//
//            } catch (Exception e) {
//                System.err.println("Vector memory error: " + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Composite memory demonstration
//     */
//    private static void runCompositeMemoryExample() {
//        System.out.println("\n5. Composite Memory Example:");
//
//        // Create composite memory with multiple memory types
//        ConversationBufferMemory bufferMemory = new ConversationBufferMemory(3);
//        EntityMemory entityMemory = new EntityMemory(llm);
//        VectorMemory vectorMemory = new VectorMemory(new MockEmbeddingService());
//
//        CompositeMemory compositeMemory = new CompositeMemory(
//            Map.of(
//                "buffer", bufferMemory,
//                "entity", entityMemory,
//                "vector", vectorMemory
//            )
//        );
//
//        // Add some technical knowledge to vector memory
//        vectorMemory.addMemory(new MemoryEntry("Microservices architecture divides applications into small, independent services.",
//            Map.of("type", "architecture")));
//        vectorMemory.addMemory(new MemoryEntry("Docker containers provide consistent deployment environments across different systems.",
//            Map.of("type", "deployment")));
//
//        String[] complexConversation = {
//            "Hi, I'm David Miller, a senior architect at CloudTech Inc.",
//            "We're designing a new microservices architecture for our platform.",
//            "I'm particularly interested in containerization with Docker.",
//            "Can you explain how microservices and Docker work together?",
//            "What are the benefits of this approach for our team?"
//        };
//
//        for (String userInput : complexConversation) {
//            try {
//                // Get comprehensive context from composite memory
//                Map<String, String> contexts = compositeMemory.getMultipleContexts(userInput);
//
//                // Combine contexts for prompt
//                StringBuilder contextBuilder = new StringBuilder();
//                contexts.forEach((type, context) -> {
//                    if (!context.trim().isEmpty()) {
//                        contextBuilder.append(type.toUpperCase()).append(" Context:\n")
//                                   .append(context).append("\n\n");
//                    }
//                });
//
//                String prompt = String.format(
//                    "%sUser: %s\n\nAssistant:",
//                    contextBuilder.toString(), userInput
//                );
//
//                AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//
//                // Add to all relevant memory systems
//                compositeMemory.addMessage(UserMessage.from(userInput));
//                compositeMemory.addMessage(response);
//
//                System.out.println("User: " + userInput);
//                System.out.println("AI: " + response.text());
//                System.out.println("---");
//
//            } catch (Exception e) {
//                System.err.println("Composite memory error: " + e.getMessage());
//            }
//        }
//
//        // Show final memory states
//        System.out.println("\nFinal memory states:");
//        System.out.println("Entities: " + entityMemory.getAllEntities());
//        System.out.println("Recent messages: " + bufferMemory.getFormattedHistory());
//    }
//
//    /**
//     * Persistent memory demonstration
//     */
//    private static void runPersistentMemoryExample() {
//        System.out.println("\n6. Persistent Memory Example:");
//
//        // Simulate database persistence
//        MockMemoryPersistence persistence = new MockMemoryPersistence();
//
//        PersistentConversationMemory persistentMemory = new PersistentConversationMemory(
//            "user_123", "session_456", persistence);
//
//        // Simulate conversation across multiple sessions
//        String[] session1 = {
//            "I'm working on learning Python programming.",
//            "I'm particularly interested in data science applications."
//        };
//
//        String[] session2 = {
//            "I wanted to continue learning Python. What did we discuss last time?",
//            "Can you recommend some data science libraries for Python?"
//        };
//
//        // Session 1
//        System.out.println("Session 1:");
//        for (String message : session1) {
//            UserMessage userMsg = UserMessage.from(message);
//            persistentMemory.addMessage(userMsg);
//
//            String context = persistentMemory.getFormattedHistory();
//            String prompt = String.format("Context: %s\nUser: %s\nAssistant:", context, message);
//
//            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//            persistentMemory.addMessage(response);
//
//            System.out.println("User: " + message);
//            System.out.println("AI: " + response.text());
//        }
//
//        // Save session
//        persistentMemory.save();
//
//        // Simulate new session - create new memory instance with same IDs
//        PersistentConversationMemory newSessionMemory = new PersistentConversationMemory(
//            "user_123", "session_456", persistence);
//
//        // Load previous session data
//        newSessionMemory.load();
//
//        // Session 2
//        System.out.println("\nSession 2 (after reload):");
//        for (String message : session2) {
//            UserMessage userMsg = UserMessage.from(message);
//            newSessionMemory.addMessage(userMsg);
//
//            String context = newSessionMemory.getFormattedHistory();
//            String prompt = String.format("Context: %s\nUser: %s\nAssistant:", context, message);
//
//            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//            newSessionMemory.addMessage(response);
//
//            System.out.println("User: " + message);
//            System.out.println("AI: " + response.text());
//        }
//
//        // Show persistence statistics
//        System.out.println("\nPersistence stats: " + persistence.getStats());
//    }
//
//    /**
//     * Advanced memory patterns demonstration
//     */
//    private static void runAdvancedPatternsExample() {
//        System.out.println("\n7. Advanced Memory Patterns Example:");
//
//        // Hierarchical memory with different retention levels
//        HierarchicalMemory hierarchicalMemory = new HierarchicalMemory(llm);
//
//        // Working memory (immediate context)
//        hierarchicalMemory.addToWorkingMemory("Current task: Planning a website redesign project");
//        hierarchicalMemory.addToWorkingMemory("Focus area: User experience improvements");
//
//        // Short-term memory (recent conversation)
//        hierarchicalMemory.addToShortTermMemory("User mentioned they work in e-commerce");
//        hierarchicalMemory.addToShortTermMemory("Current website has high bounce rate");
//
//        // Long-term memory (persistent knowledge)
//        hierarchicalMemory.addToLongTermMemory("User prefers modern, minimalist design");
//        hierarchicalMemory.addToLongTermMemory("Budget constraints: $50k maximum");
//
//        String query = "What should we prioritize in the website redesign?";
//
//        try {
//            String context = hierarchicalMemory.getHierarchicalContext(query);
//            String prompt = String.format("Context:\n%s\n\nQuestion: %s\n\nAnswer:", context, query);
//
//            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
//
//            System.out.println("Query: " + query);
//            System.out.println("Hierarchical context: " + context);
//            System.out.println("Response: " + response.text());
//
//        } catch (Exception e) {
//            System.err.println("Hierarchical memory error: " + e.getMessage());
//        }
//
//        // Adaptive memory that adjusts based on importance
//        AdaptiveMemory adaptiveMemory = new AdaptiveMemory();
//
//        // Add memories with different importance levels
//        adaptiveMemory.addMemory("Meeting scheduled for tomorrow", 0.9); // High importance
//        adaptiveMemory.addMemory("User likes coffee", 0.3); // Low importance
//        adaptiveMemory.addMemory("Project deadline is next Friday", 0.95); // Critical
//        adaptiveMemory.addMemory("Weather is nice today", 0.1); // Very low importance
//
//        String adaptiveQuery = "What important things should I remember?";
//        List<String> importantMemories = adaptiveMemory.getImportantMemories(0.5); // Threshold
//
//        System.out.println("\nAdaptive Memory - Important items (threshold 0.5):");
//        importantMemories.forEach(memory -> System.out.println("  - " + memory));
//
//        // Demonstrate memory decay
//        adaptiveMemory.applyDecay(0.1); // Reduce importance by 10%
//        List<String> afterDecay = adaptiveMemory.getImportantMemories(0.5);
//
//        System.out.println("\nAfter decay (10% reduction):");
//        afterDecay.forEach(memory -> System.out.println("  - " + memory));
//    }
//}
//
///**
// * Mock implementations for demonstration
// */
//
//class MockEmbeddingService implements EmbeddingService {
//
//    @Override
//    public float[] embed(String text) {
//        // Simple mock embedding based on text hash
//        int hash = text.hashCode();
//        float[] embedding = new float[4]; // Very simple 4D embedding
//
//        embedding[0] = (hash % 1000) / 1000.0f;
//        embedding[1] = ((hash / 1000) % 1000) / 1000.0f;
//        embedding[2] = ((hash / 1000000) % 1000) / 1000.0f;
//        embedding[3] = text.length() / 100.0f;
//
//        return embedding;
//    }
//
//    @Override
//    public List<float[]> embedBatch(List<String> texts) {
//        return texts.stream().map(this::embed).collect(java.util.stream.Collectors.toList());
//    }
//
//    @Override
//    public int getDimensions() {
//        return 4;
//    }
//
//    @Override
//    public String getModelName() {
//        return "mock-embedding";
//    }
//}
//
//class MockMemoryPersistence {
//    private final Map<String, String> storage = new HashMap<>();
//    private int operationCount = 0;
//
//    public void save(String key, String data) {
//        storage.put(key, data);
//        operationCount++;
//    }
//
//    public String load(String key) {
//        operationCount++;
//        return storage.get(key);
//    }
//
//    public void delete(String key) {
//        storage.remove(key);
//        operationCount++;
//    }
//
//    public Map<String, Object> getStats() {
//        return Map.of(
//            "total_keys", storage.size(),
//            "operations", operationCount,
//            "storage_size_bytes", storage.values().stream().mapToInt(String::length).sum()
//        );
//    }
//}
//
///**
// * Advanced memory implementations
// */
//
//class HierarchicalMemory {
//    private final ChatLanguageModel llm;
//    private final List<String> workingMemory = new ArrayList<>();
//    private final List<String> shortTermMemory = new ArrayList<>();
//    private final List<String> longTermMemory = new ArrayList<>();
//
//    public HierarchicalMemory(ChatLanguageModel llm) {
//        this.llm = llm;
//    }
//
//    public void addToWorkingMemory(String item) {
//        workingMemory.add(item);
//        // Keep only last 3 items in working memory
//        if (workingMemory.size() > 3) {
//            workingMemory.remove(0);
//        }
//    }
//
//    public void addToShortTermMemory(String item) {
//        shortTermMemory.add(item);
//        // Keep only last 10 items in short-term memory
//        if (shortTermMemory.size() > 10) {
//            shortTermMemory.remove(0);
//        }
//    }
//
//    public void addToLongTermMemory(String item) {
//        longTermMemory.add(item);
//    }
//
//    public String getHierarchicalContext(String query) {
//        StringBuilder context = new StringBuilder();
//
//        if (!workingMemory.isEmpty()) {
//            context.append("Working Memory:\n");
//            workingMemory.forEach(item -> context.append("- ").append(item).append("\n"));
//            context.append("\n");
//        }
//
//        if (!shortTermMemory.isEmpty()) {
//            context.append("Short-term Memory:\n");
//            shortTermMemory.forEach(item -> context.append("- ").append(item).append("\n"));
//            context.append("\n");
//        }
//
//        if (!longTermMemory.isEmpty()) {
//            context.append("Long-term Memory:\n");
//            longTermMemory.forEach(item -> context.append("- ").append(item).append("\n"));
//        }
//
//        return context.toString();
//    }
//}
//
//class AdaptiveMemory {
//    private final List<MemoryItem> memories = new ArrayList<>();
//
//    private static class MemoryItem {
//        String content;
//        double importance;
//        long timestamp;
//
//        MemoryItem(String content, double importance) {
//            this.content = content;
//            this.importance = importance;
//            this.timestamp = System.currentTimeMillis();
//        }
//    }
//
//    public void addMemory(String content, double importance) {
//        memories.add(new MemoryItem(content, Math.max(0.0, Math.min(1.0, importance))));
//    }
//
//    public List<String> getImportantMemories(double threshold) {
//        return memories.stream()
//            .filter(item -> item.importance >= threshold)
//            .sorted((a, b) -> Double.compare(b.importance, a.importance))
//            .map(item -> String.format("%s (importance: %.2f)", item.content, item.importance))
//            .collect(java.util.stream.Collectors.toList());
//    }
//
//    public void applyDecay(double decayRate) {
//        memories.forEach(memory -> {
//            memory.importance *= (1.0 - decayRate);
//        });
//    }
//
//    public void reinforceMemory(String content, double boost) {
//        memories.stream()
//            .filter(item -> item.content.contains(content))
//            .forEach(item -> item.importance = Math.min(1.0, item.importance + boost));
//    }
//}
//
///**
// * Enhanced conversation memory with analytics
// */
//class AnalyticalConversationMemory extends ConversationBufferMemory {
//    private final Map<String, Integer> topicCounts = new HashMap<>();
//    private final Map<String, Double> sentimentHistory = new ArrayList<>();
//    private int totalInteractions = 0;
//
//    public AnalyticalConversationMemory(int maxMessages) {
//        super(maxMessages);
//    }
//
//    @Override
//    public void addMessage(UserMessage message) {
//        super.addMessage(message);
//        analyzeMessage(message.text());
//        totalInteractions++;
//    }
//
//    @Override
//    public void addMessage(AiMessage message) {
//        super.addMessage(message);
//        analyzeMessage(message.text());
//    }
//
//    private void analyzeMessage(String message) {
//        // Simple topic extraction (would use NLP in real implementation)
//        String[] words = message.toLowerCase().split("\\s+");
//        for (String word : words) {
//            if (word.length() > 4) { // Simple filter for meaningful words
//                topicCounts.merge(word, 1, Integer::sum);
//            }
//        }
//
//        // Mock sentiment analysis
//        double sentiment = 0.5; // Neutral baseline
//        if (message.toLowerCase().contains("great") || message.toLowerCase().contains("good")) {
//            sentiment = 0.8;
//        } else if (message.toLowerCase().contains("bad") || message.toLowerCase().contains("problem")) {
//            sentiment = 0.2;
//        }
//        sentimentHistory.add(sentiment);
//    }
//
//    public Map<String, Object> getAnalytics() {
//        Map<String, Object> analytics = new HashMap<>();
//        analytics.put("total_interactions", totalInteractions);
//        analytics.put("average_sentiment",
//            sentimentHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.5));
//        analytics.put("top_topics", getTopTopics(5));
//        analytics.put("conversation_length", getMessageCount());
//
//        return analytics;
//    }
//
//    private List<Map.Entry<String, Integer>> getTopTopics(int limit) {
//        return topicCounts.entrySet().stream()
//            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
//            .limit(limit)
//            .collect(java.util.stream.Collectors.toList());
//    }
//}
