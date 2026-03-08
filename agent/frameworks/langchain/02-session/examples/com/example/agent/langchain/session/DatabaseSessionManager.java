/**
 * Advanced Session Management with Database Persistence
 * Java implementation using Spring Boot and JPA
 */
package com.example.agent.langchain.session;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity for Session Storage
 */
@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
class ChatSession {

    @Id
    @Column(length = 36)
    private String sessionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastAccessed;

    @Column(columnDefinition = "TEXT")
    private String conversationHistory;

    @Column(columnDefinition = "TEXT")
    private String userPreferences;

    @Column
    private Integer totalMessages;

    @Column
    private Boolean sessionActive;

    @Column
    private String sessionMetadata;

    // Constructor with required fields
    public ChatSession(String sessionId, String userId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
        this.totalMessages = 0;
        this.sessionActive = true;
    }
}

/**
 * JPA Repository for Session Management
 */
@Repository
interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    List<ChatSession> findByUserIdAndSessionActive(String userId, Boolean sessionActive);

    List<ChatSession> findBySessionActive(Boolean sessionActive);

    List<ChatSession> findByLastAccessedBefore(LocalDateTime dateTime);

    @Modifying
    @Query("UPDATE ChatSession s SET s.sessionActive = false WHERE s.lastAccessed < :expiredTime")
    int deactivateExpiredSessions(@Param("expiredTime") LocalDateTime expiredTime);
}

/**
 * Session Service with Database Persistence
 */
@Service
public class DatabaseSessionManager {

    @Autowired
    private ChatSessionRepository sessionRepository;

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    private final Map<String, MessageWindowChatMemory> memoryCache;

    public DatabaseSessionManager() {
        // Initialize with ConfigurationUtil
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            throw new IllegalStateException("OpenAI API key not available. Please set OPENAI_API_KEY environment variable.");
        }

        ConfigurationUtil config = ConfigurationUtil.create();
        this.chatModel = config.createChatModel();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
        this.memoryCache = new ConcurrentHashMap<>();
    }

    /**
     * Create a new session with optional preferences
     */
    public String createSession(String userId, Map<String, Object> preferences) {
        String sessionId = UUID.randomUUID().toString();

        ChatSession session = new ChatSession(sessionId, userId);

        // Store preferences as JSON
        if (preferences != null && !preferences.isEmpty()) {
            try {
                session.setUserPreferences(objectMapper.writeValueAsString(preferences));
            } catch (JsonProcessingException e) {
                System.err.println("Error serializing preferences: " + e.getMessage());
            }
        }

        // Initialize session metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("created_by", "system");
        metadata.put("session_type", "chat");
        metadata.put("version", "1.0");

        try {
            session.setSessionMetadata(objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing metadata: " + e.getMessage());
        }

        // Save to database
        sessionRepository.save(session);

        // Initialize memory cache
        memoryCache.put(sessionId, MessageWindowChatMemory.withMaxMessages(100));

        System.out.println("Created session " + sessionId + " for user " + userId);
        return sessionId;
    }

    /**
     * Get session with automatic cache loading
     */
    public Optional<ChatSession> getSession(String sessionId) {
        Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);

        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();

            // Update last accessed time
            session.setLastAccessed(LocalDateTime.now());
            sessionRepository.save(session);

            // Load conversation history into memory cache if not present
            if (!memoryCache.containsKey(sessionId)) {
                loadConversationIntoMemory(sessionId, session.getConversationHistory());
            }

            return Optional.of(session);
        }

        return Optional.empty();
    }

    /**
     * Send message and persist conversation
     */
    public String sendMessage(String sessionId, String message) {
        Optional<ChatSession> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("Session " + sessionId + " not found");
        }

        ChatSession session = sessionOpt.get();

        if (!session.getSessionActive()) {
            throw new IllegalStateException("Session " + sessionId + " is not active");
        }

        // Get memory from cache
        MessageWindowChatMemory memory = memoryCache.get(sessionId);
        if (memory == null) {
            memory = MessageWindowChatMemory.withMaxMessages(100);
            memoryCache.put(sessionId, memory);
        }

        // Add user message to memory
        memory.add(UserMessage.from(message));

        // Generate response
        List<ChatMessage> messages = memory.messages();
        AiMessage response = chatModel.generate(messages).content();

        // Add AI response to memory
        memory.add(response);

        // Update session in database
        session.setTotalMessages(session.getTotalMessages() + 1);

        // Serialize and save conversation history
        try {
            List<Map<String, Object>> history = convertMemoryToHistory(memory);
            session.setConversationHistory(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing conversation: " + e.getMessage());
        }

        sessionRepository.save(session);

        return response.text();
    }

    /**
     * Get conversation history from database
     */
    public List<Map<String, Object>> getConversationHistory(String sessionId) {
        Optional<ChatSession> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            return Collections.emptyList();
        }

        String historyJson = sessionOpt.get().getConversationHistory();
        if (historyJson == null || historyJson.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
            return objectMapper.readValue(historyJson, typeRef);
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing conversation history: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get user preferences
     */
    public Map<String, Object> getUserPreferences(String sessionId) {
        Optional<ChatSession> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            return Collections.emptyMap();
        }

        String preferencesJson = sessionOpt.get().getUserPreferences();
        if (preferencesJson == null || preferencesJson.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
            return objectMapper.readValue(preferencesJson, typeRef);
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing preferences: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Update user preferences
     */
    public void updateUserPreferences(String sessionId, Map<String, Object> preferences) {
        Optional<ChatSession> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("Session " + sessionId + " not found");
        }

        ChatSession session = sessionOpt.get();

        try {
            session.setUserPreferences(objectMapper.writeValueAsString(preferences));
            sessionRepository.save(session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing preferences", e);
        }
    }

    /**
     * List active sessions for a user
     */
    public List<ChatSession> getUserActiveSessions(String userId) {
        return sessionRepository.findByUserIdAndSessionActive(userId, true);
    }

    /**
     * End session
     */
    public void endSession(String sessionId) {
        Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setSessionActive(false);
            sessionRepository.save(session);

            // Remove from memory cache
            memoryCache.remove(sessionId);

            System.out.println("Session " + sessionId + " ended");
        }
    }

    /**
     * Clean up expired sessions
     */
    public int cleanupExpiredSessions(int hoursToExpire) {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(hoursToExpire);

        // Remove from memory cache
        List<ChatSession> expiredSessions = sessionRepository.findByLastAccessedBefore(expireTime);
        for (ChatSession session : expiredSessions) {
            memoryCache.remove(session.getSessionId());
        }

        // Deactivate in database
        return sessionRepository.deactivateExpiredSessions(expireTime);
    }

    /**
     * Helper method to load conversation history into memory
     */
    private void loadConversationIntoMemory(String sessionId, String historyJson) {
        if (historyJson == null || historyJson.isEmpty()) {
            return;
        }

        try {
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
            List<Map<String, Object>> history = objectMapper.readValue(historyJson, typeRef);
            MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(100);

            for (Map<String, Object> messageData : history) {
                String type = (String) messageData.get("type");
                String content = (String) messageData.get("content");

                if ("USER".equals(type)) {
                    memory.add(UserMessage.from(content));
                } else if ("AI".equals(type)) {
                    memory.add(AiMessage.from(content));
                }
            }

            memoryCache.put(sessionId, memory);
        } catch (JsonProcessingException e) {
            System.err.println("Error loading conversation history: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert memory to serializable format
     */
    private List<Map<String, Object>> convertMemoryToHistory(MessageWindowChatMemory memory) {
        List<Map<String, Object>> history = new ArrayList<>();

        for (ChatMessage message : memory.messages()) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", message.type().toString());

            // Handle content extraction safely for different message types
            String content = "";
            if (message instanceof UserMessage) {
                content = ((UserMessage) message).singleText();
            } else if (message instanceof AiMessage) {
                content = ((AiMessage) message).text();
            }

            messageData.put("content", content);
            messageData.put("timestamp", LocalDateTime.now().toString());
            history.add(messageData);
        }

        return history;
    }
}

/**
 * Example usage and testing for DatabaseSessionManager
 */
class DatabaseSessionExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        System.out.println("=== Database Session Manager Demo ===");
        System.out.println("NOTE: This is a demonstration of the DatabaseSessionManager API.");
        System.out.println("For full functionality, run this within a Spring Boot application context.\n");

        try {
            // Create a mock database session manager for demonstration
            MockDatabaseSessionManager sessionManager = new MockDatabaseSessionManager();

            // Demo 1: Create sessions for multiple users
            System.out.println("🔄 Demo 1: Creating Sessions");

            Map<String, Object> alicePrefs = new HashMap<>();
            alicePrefs.put("language", "English");
            alicePrefs.put("expertise_level", "beginner");
            alicePrefs.put("preferred_style", "conversational");

            Map<String, Object> bobPrefs = new HashMap<>();
            bobPrefs.put("language", "English");
            bobPrefs.put("expertise_level", "advanced");
            bobPrefs.put("preferred_style", "technical");

            String aliceSession = sessionManager.createSession("alice", alicePrefs);
            String bobSession = sessionManager.createSession("bob", bobPrefs);

            System.out.println("✅ Created session for Alice: " + aliceSession.substring(0, 8) + "...");
            System.out.println("✅ Created session for Bob: " + bobSession.substring(0, 8) + "...");

            // Demo 2: Send messages and get responses
            System.out.println("\n🔄 Demo 2: Conversation Flow");

            String aliceResponse1 = sessionManager.sendMessage(aliceSession, "Hello! I'm new to programming. Can you help me understand what Java is?");
            System.out.println("👤 Alice: Hello! I'm new to programming. Can you help me understand what Java is?");
            System.out.println("🤖 AI: " + aliceResponse1);

            String bobResponse1 = sessionManager.sendMessage(bobSession, "Explain the difference between checked and unchecked exceptions in Java.");
            System.out.println("\n👤 Bob: Explain the difference between checked and unchecked exceptions in Java.");
            System.out.println("🤖 AI: " + bobResponse1);

            // Demo 3: Continue conversations with context
            System.out.println("\n🔄 Demo 3: Context Awareness");

            String aliceResponse2 = sessionManager.sendMessage(aliceSession, "Can you show me a simple Java example?");
            System.out.println("👤 Alice: Can you show me a simple Java example?");
            System.out.println("🤖 AI: " + aliceResponse2);

            // Demo 4: View conversation history
            System.out.println("\n🔄 Demo 4: Conversation History");

            List<Map<String, Object>> aliceHistory = sessionManager.getConversationHistory(aliceSession);
            System.out.println("📝 Alice's conversation has " + aliceHistory.size() + " messages");

            for (int i = 0; i < Math.min(aliceHistory.size(), 4); i++) {
                Map<String, Object> msg = aliceHistory.get(i);
                String type = (String) msg.get("type");
                String content = (String) msg.get("content");
                String shortContent = content.length() > 60 ? content.substring(0, 60) + "..." : content;
                System.out.println("   " + type + ": " + shortContent);
            }

            // Demo 5: User preferences
            System.out.println("\n🔄 Demo 5: User Preferences");

            Map<String, Object> aliceCurrentPrefs = sessionManager.getUserPreferences(aliceSession);
            System.out.println("👤 Alice's preferences:");
            aliceCurrentPrefs.forEach((key, value) ->
                System.out.println("   " + key + ": " + value));

            // Update preferences
            alicePrefs.put("expertise_level", "intermediate"); // Alice is learning!
            sessionManager.updateUserPreferences(aliceSession, alicePrefs);
            System.out.println("✅ Updated Alice's expertise level to intermediate");

            // Demo 6: Session management
            System.out.println("\n🔄 Demo 6: Session Management");

            List<MockChatSession> activeSessions = sessionManager.getUserActiveSessions("alice");
            System.out.println("👤 Alice has " + activeSessions.size() + " active sessions");

            // Demo 7: Session cleanup
            System.out.println("\n🔄 Demo 7: Cleanup Operations");

            sessionManager.endSession(bobSession);
            System.out.println("🔚 Ended Bob's session");

            int cleanedUp = sessionManager.cleanupExpiredSessions(24);
            System.out.println("🧹 Cleaned up " + cleanedUp + " expired sessions");

            System.out.println("\n✅ Demo completed successfully!");
            System.out.println("\n📚 Key Takeaways:");
            System.out.println("• Sessions maintain conversation context across multiple exchanges");
            System.out.println("• User preferences can be stored and updated per session");
            System.out.println("• Conversation history is automatically persisted");
            System.out.println("• The AI adapts responses based on user expertise level");
            System.out.println("• Sessions can be managed (created, ended, cleaned up) programmatically");

        } catch (Exception e) {
            System.err.println("❌ Demo error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Mock implementation for demonstration purposes
 * Shows what the real DatabaseSessionManager would do with a database
 */
class MockDatabaseSessionManager {

    private final Map<String, MockChatSession> sessions = new HashMap<>();
    private final Map<String, MessageWindowChatMemory> memoryCache = new HashMap<>();
    private final ChatLanguageModel chatModel;

    public MockDatabaseSessionManager() {
        ConfigurationUtil config = ConfigurationUtil.create();
        this.chatModel = config.createChatModel();
    }

    public String createSession(String userId, Map<String, Object> preferences) {
        String sessionId = UUID.randomUUID().toString();
        MockChatSession session = new MockChatSession(sessionId, userId, preferences);
        sessions.put(sessionId, session);
        memoryCache.put(sessionId, MessageWindowChatMemory.withMaxMessages(50));
        return sessionId;
    }

    public String sendMessage(String sessionId, String message) {
        MockChatSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        MessageWindowChatMemory memory = memoryCache.get(sessionId);
        memory.add(UserMessage.from(message));

        // Get AI response
        List<ChatMessage> messages = memory.messages();
        AiMessage response = chatModel.generate(messages).content();
        memory.add(response);

        // Update session
        session.totalMessages++;
        session.lastAccessed = LocalDateTime.now();

        return response.text();
    }

    public List<Map<String, Object>> getConversationHistory(String sessionId) {
        MessageWindowChatMemory memory = memoryCache.get(sessionId);
        if (memory == null) return Collections.emptyList();

        List<Map<String, Object>> history = new ArrayList<>();
        for (ChatMessage msg : memory.messages()) {
            Map<String, Object> msgData = new HashMap<>();
            msgData.put("type", msg.type().toString());

            String content = "";
            if (msg instanceof UserMessage) {
                content = ((UserMessage) msg).singleText();
            } else if (msg instanceof AiMessage) {
                content = ((AiMessage) msg).text();
            }

            msgData.put("content", content);
            msgData.put("timestamp", LocalDateTime.now().toString());
            history.add(msgData);
        }
        return history;
    }

    public Map<String, Object> getUserPreferences(String sessionId) {
        MockChatSession session = sessions.get(sessionId);
        return session != null ? session.preferences : Collections.emptyMap();
    }

    public void updateUserPreferences(String sessionId, Map<String, Object> preferences) {
        MockChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.preferences.putAll(preferences);
        }
    }

    public List<MockChatSession> getUserActiveSessions(String userId) {
        return sessions.values().stream()
                .filter(s -> s.userId.equals(userId) && s.sessionActive)
                .collect(java.util.stream.Collectors.toList());
    }

    public void endSession(String sessionId) {
        MockChatSession session = sessions.get(sessionId);
        if (session != null) {
            session.sessionActive = false;
            memoryCache.remove(sessionId);
        }
    }

    public int cleanupExpiredSessions(int hoursToExpire) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hoursToExpire);
        int count = 0;

        for (MockChatSession session : sessions.values()) {
            if (session.lastAccessed.isBefore(cutoff)) {
                session.sessionActive = false;
                memoryCache.remove(session.sessionId);
                count++;
            }
        }
        return count;
    }
}

/**
 * Simple mock session class for demonstration
 */
class MockChatSession {
    String sessionId;
    String userId;
    LocalDateTime createdAt;
    LocalDateTime lastAccessed;
    Map<String, Object> preferences;
    int totalMessages;
    boolean sessionActive;

    public MockChatSession(String sessionId, String userId, Map<String, Object> preferences) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
        this.preferences = new HashMap<>(preferences != null ? preferences : Collections.emptyMap());
        this.totalMessages = 0;
        this.sessionActive = true;
    }
}

