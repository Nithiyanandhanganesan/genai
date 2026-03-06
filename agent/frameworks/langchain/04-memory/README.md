# Memory Systems in LangChain (Java)

## 🎯 Overview
Memory systems in LangChain enable agents to remember and utilize information from previous interactions. Different types of memory serve different purposes, from simple conversation history to sophisticated semantic memory that can retrieve relevant context based on similarity.

## 🧠 Core Memory Concepts

### Types of Memory
1. **Short-term Memory**: Recent conversation context (buffer memory)
2. **Long-term Memory**: Persistent information across sessions  
3. **Semantic Memory**: Concept-based, searchable memory
4. **Episodic Memory**: Event-based memory with temporal context
5. **Working Memory**: Active processing context

### Memory Characteristics
- **Capacity**: How much information can be stored
- **Persistence**: How long information is retained
- **Retrieval**: How information is accessed and recalled
- **Update Strategy**: How new information is incorporated

## 🏗️ Memory Architecture Patterns

### 1. **Buffer Memory**
Stores recent conversation exchanges in order
```
User: "What's the weather like?"
AI: "I need your location to check the weather."
User: "I'm in San Francisco"
AI: "The weather in San Francisco is sunny, 72°F"
```

### 2. **Summary Memory**
Compresses old conversations into summaries
```
Summary: "User is in San Francisco, asked about weather. Current weather is sunny, 72°F."
Recent: [current conversation...]
```

### 3. **Vector Memory**
Stores information as embeddings for semantic retrieval
```
Query: "What did I say about my preferences?"
Retrieved: Similar conversations about user preferences
```

### 4. **Entity Memory**
Tracks specific entities and their attributes
```
Entities:
- User: {name: "John", location: "San Francisco", preferences: ["sunny weather"]}
- Topics: {weather: {last_checked: "2024-03-05", location: "San Francisco"}}
```

## 💻 Java Memory Implementations

### Basic Buffer Memory
```java
public class ConversationBufferMemory {
    private final List<ChatMessage> messages;
    private final int maxMessages;
    
    public ConversationBufferMemory(int maxMessages) {
        this.messages = new ArrayList<>();
        this.maxMessages = maxMessages;
    }
    
    public void addMessage(ChatMessage message) {
        messages.add(message);
        
        // Remove oldest messages if exceeding capacity
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }
    
    public List<ChatMessage> getRecentMessages(int count) {
        int start = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(start, messages.size()));
    }
    
    public String getFormattedHistory() {
        return messages.stream()
            .map(msg -> msg.type() + ": " + msg.text())
            .collect(Collectors.joining("\n"));
    }
}
```

### Summary Memory
```java
public class ConversationSummaryMemory {
    private final ChatLanguageModel llm;
    private String conversationSummary;
    private final List<ChatMessage> recentMessages;
    private final int maxRecentMessages;
    
    public ConversationSummaryMemory(ChatLanguageModel llm, int maxRecentMessages) {
        this.llm = llm;
        this.maxRecentMessages = maxRecentMessages;
        this.recentMessages = new ArrayList<>();
        this.conversationSummary = "";
    }
    
    public void addMessage(ChatMessage message) {
        recentMessages.add(message);
        
        if (recentMessages.size() > maxRecentMessages) {
            // Summarize oldest messages
            updateSummary();
            
            // Keep only recent messages
            List<ChatMessage> toSummarize = new ArrayList<>(
                recentMessages.subList(0, recentMessages.size() - maxRecentMessages/2));
            recentMessages.removeAll(toSummarize);
        }
    }
    
    private void updateSummary() {
        String conversationText = formatMessagesForSummary(recentMessages);
        
        String prompt = String.format(
            "Summarize this conversation, incorporating the previous summary:\n\n" +
            "Previous summary: %s\n\n" +
            "Recent conversation:\n%s\n\n" +
            "Updated summary:",
            conversationSummary.isEmpty() ? "None" : conversationSummary,
            conversationText
        );
        
        AiMessage summaryResponse = llm.generate(UserMessage.from(prompt)).content();
        conversationSummary = summaryResponse.text();
    }
    
    public String getMemoryContext() {
        StringBuilder context = new StringBuilder();
        
        if (!conversationSummary.isEmpty()) {
            context.append("Previous conversation summary:\n")
                  .append(conversationSummary)
                  .append("\n\n");
        }
        
        if (!recentMessages.isEmpty()) {
            context.append("Recent conversation:\n")
                  .append(formatMessagesForSummary(recentMessages));
        }
        
        return context.toString();
    }
}
```

### Entity Memory
```java
public class EntityMemory {
    
    private final Map<String, EntityInfo> entities;
    private final ChatLanguageModel llm;
    
    public EntityMemory(ChatLanguageModel llm) {
        this.entities = new ConcurrentHashMap<>();
        this.llm = llm;
    }
    
    public void updateFromConversation(String conversationText) {
        // Extract entities using LLM
        String extractionPrompt = String.format(
            "Extract entities and their attributes from this conversation. " +
            "Return JSON format with entity names as keys and attributes as values:\n\n%s",
            conversationText
        );
        
        AiMessage response = llm.generate(UserMessage.from(extractionPrompt)).content();
        
        try {
            // Parse JSON response and update entities
            Map<String, Object> extractedEntities = parseEntityResponse(response.text());
            updateEntities(extractedEntities);
        } catch (Exception e) {
            System.err.println("Error updating entities: " + e.getMessage());
        }
    }
    
    public Optional<EntityInfo> getEntity(String entityName) {
        return Optional.ofNullable(entities.get(entityName.toLowerCase()));
    }
    
    public String getEntityContext(Set<String> relevantEntities) {
        StringBuilder context = new StringBuilder("Known entities:\n");
        
        for (String entityName : relevantEntities) {
            EntityInfo entity = entities.get(entityName.toLowerCase());
            if (entity != null) {
                context.append("- ").append(entityName).append(": ")
                       .append(entity.getAttributesSummary()).append("\n");
            }
        }
        
        return context.toString();
    }
    
    private void updateEntities(Map<String, Object> extractedEntities) {
        for (Map.Entry<String, Object> entry : extractedEntities.entrySet()) {
            String entityName = entry.getKey().toLowerCase();
            
            EntityInfo existingEntity = entities.get(entityName);
            if (existingEntity == null) {
                existingEntity = new EntityInfo(entityName);
                entities.put(entityName, existingEntity);
            }
            
            // Update attributes
            if (entry.getValue() instanceof Map) {
                Map<String, Object> attributes = (Map<String, Object>) entry.getValue();
                existingEntity.updateAttributes(attributes);
            }
        }
    }
}

class EntityInfo {
    private final String name;
    private final Map<String, Object> attributes;
    private LocalDateTime lastUpdated;
    
    public EntityInfo(String name) {
        this.name = name;
        this.attributes = new ConcurrentHashMap<>();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void updateAttributes(Map<String, Object> newAttributes) {
        attributes.putAll(newAttributes);
        lastUpdated = LocalDateTime.now();
    }
    
    public String getAttributesSummary() {
        return attributes.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
    }
    
    // Getters
    public String getName() { return name; }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
```

### Vector Memory with Embeddings
```java
public class VectorMemory {
    
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final Map<String, MemoryEntry> memoryEntries;
    
    public VectorMemory(EmbeddingStore<TextSegment> embeddingStore, 
                       EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.memoryEntries = new ConcurrentHashMap<>();
    }
    
    public void storeMemory(String content, Map<String, Object> metadata) {
        String memoryId = UUID.randomUUID().toString();
        
        // Create text segment with metadata
        TextSegment textSegment = TextSegment.from(content);
        textSegment.metadata().put("memoryId", memoryId);
        textSegment.metadata().put("timestamp", LocalDateTime.now().toString());
        
        if (metadata != null) {
            metadata.forEach((key, value) -> 
                textSegment.metadata().put(key, value.toString()));
        }
        
        // Generate embedding and store
        Embedding embedding = embeddingModel.embed(textSegment).content();
        embeddingStore.add(embedding, textSegment);
        
        // Store memory entry for reference
        MemoryEntry entry = new MemoryEntry(memoryId, content, metadata, LocalDateTime.now());
        memoryEntries.put(memoryId, entry);
    }
    
    public List<RelevantMemory> retrieveRelevantMemories(String query, int maxResults) {
        // Generate query embedding
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        
        // Search for similar memories
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
            queryEmbedding, maxResults);
        
        return matches.stream()
            .map(match -> {
                String memoryId = match.embedded().metadata().get("memoryId");
                MemoryEntry entry = memoryEntries.get(memoryId);
                return new RelevantMemory(entry, match.score());
            })
            .collect(Collectors.toList());
    }
    
    public String getRelevantContext(String query, int maxResults) {
        List<RelevantMemory> memories = retrieveRelevantMemories(query, maxResults);
        
        if (memories.isEmpty()) {
            return "No relevant memories found.";
        }
        
        StringBuilder context = new StringBuilder("Relevant memories:\n");
        for (RelevantMemory memory : memories) {
            context.append("- ").append(memory.getEntry().getContent())
                   .append(" (relevance: ").append(String.format("%.2f", memory.getScore()))
                   .append(")\n");
        }
        
        return context.toString();
    }
}

class MemoryEntry {
    private final String id;
    private final String content;
    private final Map<String, Object> metadata;
    private final LocalDateTime createdAt;
    
    public MemoryEntry(String id, String content, Map<String, Object> metadata, 
                      LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.createdAt = createdAt;
    }
    
    // Getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

class RelevantMemory {
    private final MemoryEntry entry;
    private final double score;
    
    public RelevantMemory(MemoryEntry entry, double score) {
        this.entry = entry;
        this.score = score;
    }
    
    public MemoryEntry getEntry() { return entry; }
    public double getScore() { return score; }
}
```

## 🔄 Composite Memory Systems

### Multi-Layer Memory Manager
```java
public class CompositeMemoryManager {
    
    private final ConversationBufferMemory bufferMemory;
    private final ConversationSummaryMemory summaryMemory;
    private final EntityMemory entityMemory;
    private final VectorMemory vectorMemory;
    
    public CompositeMemoryManager(ChatLanguageModel llm, 
                                 EmbeddingStore<TextSegment> embeddingStore,
                                 EmbeddingModel embeddingModel) {
        this.bufferMemory = new ConversationBufferMemory(10);
        this.summaryMemory = new ConversationSummaryMemory(llm, 20);
        this.entityMemory = new EntityMemory(llm);
        this.vectorMemory = new VectorMemory(embeddingStore, embeddingModel);
    }
    
    public void addConversationExchange(ChatMessage userMessage, ChatMessage aiMessage) {
        // Add to all memory systems
        bufferMemory.addMessage(userMessage);
        bufferMemory.addMessage(aiMessage);
        
        summaryMemory.addMessage(userMessage);
        summaryMemory.addMessage(aiMessage);
        
        // Extract entities from the conversation
        String conversationText = userMessage.text() + "\n" + aiMessage.text();
        entityMemory.updateFromConversation(conversationText);
        
        // Store important information in vector memory
        storeImportantInformation(conversationText);
    }
    
    public String getCompleteContext(String currentQuery) {
        StringBuilder context = new StringBuilder();
        
        // Add recent conversation context
        context.append("=== Recent Conversation ===\n");
        context.append(bufferMemory.getFormattedHistory()).append("\n\n");
        
        // Add summary of older conversations
        String summary = summaryMemory.getMemoryContext();
        if (!summary.isEmpty()) {
            context.append("=== Conversation History ===\n");
            context.append(summary).append("\n\n");
        }
        
        // Add relevant entity information
        Set<String> queryEntities = extractEntitiesFromQuery(currentQuery);
        if (!queryEntities.isEmpty()) {
            context.append("=== Relevant Entities ===\n");
            context.append(entityMemory.getEntityContext(queryEntities)).append("\n\n");
        }
        
        // Add semantically relevant memories
        String vectorContext = vectorMemory.getRelevantContext(currentQuery, 3);
        context.append("=== Relevant Previous Context ===\n");
        context.append(vectorContext);
        
        return context.toString();
    }
    
    private void storeImportantInformation(String conversationText) {
        // Simple heuristic to determine if information is worth storing
        if (conversationText.toLowerCase().contains("remember") ||
            conversationText.toLowerCase().contains("important") ||
            conversationText.toLowerCase().contains("preference") ||
            conversationText.length() > 200) {
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "conversation");
            metadata.put("importance", "medium");
            
            vectorMemory.storeMemory(conversationText, metadata);
        }
    }
    
    private Set<String> extractEntitiesFromQuery(String query) {
        // Simple entity extraction - in real implementation, use NER
        Set<String> entities = new HashSet<>();
        
        // Extract potential entities (capitalized words, names, etc.)
        String[] words = query.split("\\s+");
        for (String word : words) {
            if (Character.isUpperCase(word.charAt(0)) && word.length() > 2) {
                entities.add(word.toLowerCase());
            }
        }
        
        return entities;
    }
}
```

## 🗄️ Persistent Memory Storage

### Database-Backed Memory
```java
@Entity
@Table(name = "conversation_memories")
public class ConversationMemoryEntity {
    
    @Id
    private String memoryId;
    
    @Column
    private String sessionId;
    
    @Column
    private String userId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @Enumerated(EnumType.STRING)
    private MemoryType memoryType;
    
    @Column
    private Double importance;
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastAccessed;
    
    // Getters and setters...
}

enum MemoryType {
    CONVERSATION,
    ENTITY,
    SUMMARY,
    VECTOR,
    USER_PREFERENCE
}

@Service
public class PersistentMemoryService {
    
    @Autowired
    private ConversationMemoryRepository memoryRepository;
    
    private final ObjectMapper objectMapper;
    
    public PersistentMemoryService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public void saveMemory(String sessionId, String userId, String content, 
                          MemoryType type, Map<String, Object> metadata, 
                          Double importance) {
        
        ConversationMemoryEntity entity = new ConversationMemoryEntity();
        entity.setMemoryId(UUID.randomUUID().toString());
        entity.setSessionId(sessionId);
        entity.setUserId(userId);
        entity.setContent(content);
        entity.setMemoryType(type);
        entity.setImportance(importance != null ? importance : 0.5);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLastAccessed(LocalDateTime.now());
        
        try {
            if (metadata != null) {
                entity.setMetadata(objectMapper.writeValueAsString(metadata));
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing metadata: " + e.getMessage());
        }
        
        memoryRepository.save(entity);
    }
    
    public List<ConversationMemoryEntity> getRecentMemories(String userId, 
                                                           MemoryType type, 
                                                           int limit) {
        return memoryRepository.findByUserIdAndMemoryTypeOrderByCreatedAtDesc(
            userId, type, PageRequest.of(0, limit));
    }
    
    public List<ConversationMemoryEntity> searchMemories(String userId, 
                                                        String searchTerm, 
                                                        int limit) {
        return memoryRepository.findByUserIdAndContentContainingIgnoreCase(
            userId, searchTerm, PageRequest.of(0, limit));
    }
    
    @Scheduled(fixedRate = 3600000) // Clean up every hour
    public void cleanupOldMemories() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        
        // Delete low-importance memories older than 30 days
        memoryRepository.deleteByImportanceLessThanAndLastAccessedBefore(0.3, cutoff);
        
        // Update access times for recently queried memories
        memoryRepository.updateLastAccessedForRecentlyQueried();
    }
}
```

## 🚀 Best Practices

1. **Memory Strategy Selection**
   - Use buffer memory for recent context
   - Use summary memory for long conversations
   - Use vector memory for semantic retrieval
   - Use entity memory for structured information

2. **Performance Optimization**
   - Set appropriate memory limits
   - Implement lazy loading for large memories
   - Use caching for frequently accessed memories
   - Regular cleanup of old memories

3. **Data Quality**
   - Validate memory content before storage
   - Implement importance scoring
   - Filter out low-quality information
   - Regular memory consolidation

4. **Privacy and Security**
   - Encrypt sensitive memory content
   - Implement proper access controls
   - Regular data retention reviews
   - User consent for memory storage

5. **Integration Patterns**
   - Combine multiple memory types
   - Context-aware memory retrieval
   - Progressive memory building
   - Cross-session memory sharing

## 🔗 Integration with Other Components

Memory systems integrate with:
- **Session Management**: Persistent memory across sessions
- **State Management**: Memory influences state transitions
- **Tool Integration**: Tools can add to memory
- **Agent Decision Making**: Memory provides context for decisions

---

*Next: [Tools Integration](../tools/) - Learn about extending agent capabilities with external tools.*
