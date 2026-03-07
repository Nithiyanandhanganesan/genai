///**
// * Advanced Session Management with Database Persistence
// * Java implementation using Spring Boot and JPA
// */
//package com.example.agent.session;
//
//import jakarta.persistence.Table;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.stereotype.Repository;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import javax.persistence.*;
//import java.time.LocalDateTime;
//import java.util.*;
//
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.data.message.ChatMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.data.message.AiMessage;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
///**
// * JPA Entity for Session Storage
// */
//@Entity
//@Table(name = "chat_sessions")
//public class ChatSession {
//
//    @Id
//    @Column(length = 36)
//    private String sessionId;
//
//    @Column(nullable = false)
//    private String userId;
//
//    @Column(nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(nullable = false)
//    private LocalDateTime lastAccessed;
//
//    @Column(columnDefinition = "TEXT")
//    private String conversationHistory;
//
//    @Column(columnDefinition = "TEXT")
//    private String userPreferences;
//
//    @Column
//    private Integer totalMessages;
//
//    @Column
//    private Boolean sessionActive;
//
//    @Column
//    private String sessionMetadata;
//
//    // Default constructor
//    public ChatSession() {
//        this.createdAt = LocalDateTime.now();
//        this.lastAccessed = LocalDateTime.now();
//        this.totalMessages = 0;
//        this.sessionActive = true;
//    }
//
//    // Constructor with required fields
//    public ChatSession(String sessionId, String userId) {
//        this();
//        this.sessionId = sessionId;
//        this.userId = userId;
//    }
//
//    // Getters and Setters
//    public String getSessionId() { return sessionId; }
//    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
//
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getLastAccessed() { return lastAccessed; }
//    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
//
//    public String getConversationHistory() { return conversationHistory; }
//    public void setConversationHistory(String conversationHistory) { this.conversationHistory = conversationHistory; }
//
//    public String getUserPreferences() { return userPreferences; }
//    public void setUserPreferences(String userPreferences) { this.userPreferences = userPreferences; }
//
//    public Integer getTotalMessages() { return totalMessages; }
//    public void setTotalMessages(Integer totalMessages) { this.totalMessages = totalMessages; }
//
//    public Boolean getSessionActive() { return sessionActive; }
//    public void setSessionActive(Boolean sessionActive) { this.sessionActive = sessionActive; }
//
//    public String getSessionMetadata() { return sessionMetadata; }
//    public void setSessionMetadata(String sessionMetadata) { this.sessionMetadata = sessionMetadata; }
//}
//
///**
// * JPA Repository for Session Management
// */
//@Repository
//public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
//
//    List<ChatSession> findByUserIdAndSessionActive(String userId, Boolean sessionActive);
//
//    List<ChatSession> findBySessionActive(Boolean sessionActive);
//
//    List<ChatSession> findByLastAccessedBefore(LocalDateTime dateTime);
//
//    @Modifying
//    @Query("UPDATE ChatSession s SET s.sessionActive = false WHERE s.lastAccessed < :expiredTime")
//    int deactivateExpiredSessions(@Param("expiredTime") LocalDateTime expiredTime);
//}
//
///**
// * Session Service with Database Persistence
// */
//@Service
//public class DatabaseSessionManager {
//
//    @Autowired
//    private ChatSessionRepository sessionRepository;
//
//    private final ChatLanguageModel chatModel;
//    private final ObjectMapper objectMapper;
//    private final Map<String, MessageWindowChatMemory> memoryCache;
//
//    public DatabaseSessionManager(ChatLanguageModel chatModel) {
//        this.chatModel = chatModel;
//        this.objectMapper = new ObjectMapper();
//        this.memoryCache = new ConcurrentHashMap<>();
//    }
//
//    /**
//     * Create a new session with optional preferences
//     */
//    public String createSession(String userId, Map<String, Object> preferences) {
//        String sessionId = UUID.randomUUID().toString();
//
//        ChatSession session = new ChatSession(sessionId, userId);
//
//        // Store preferences as JSON
//        if (preferences != null && !preferences.isEmpty()) {
//            try {
//                session.setUserPreferences(objectMapper.writeValueAsString(preferences));
//            } catch (JsonProcessingException e) {
//                System.err.println("Error serializing preferences: " + e.getMessage());
//            }
//        }
//
//        // Initialize session metadata
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("created_by", "system");
//        metadata.put("session_type", "chat");
//        metadata.put("version", "1.0");
//
//        try {
//            session.setSessionMetadata(objectMapper.writeValueAsString(metadata));
//        } catch (JsonProcessingException e) {
//            System.err.println("Error serializing metadata: " + e.getMessage());
//        }
//
//        // Save to database
//        sessionRepository.save(session);
//
//        // Initialize memory cache
//        memoryCache.put(sessionId, MessageWindowChatMemory.withMaxMessages(100));
//
//        System.out.println("Created session " + sessionId + " for user " + userId);
//        return sessionId;
//    }
//
//    /**
//     * Get session with automatic cache loading
//     */
//    public Optional<ChatSession> getSession(String sessionId) {
//        Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
//
//        if (sessionOpt.isPresent()) {
//            ChatSession session = sessionOpt.get();
//
//            // Update last accessed time
//            session.setLastAccessed(LocalDateTime.now());
//            sessionRepository.save(session);
//
//            // Load conversation history into memory cache if not present
//            if (!memoryCache.containsKey(sessionId)) {
//                loadConversationIntoMemory(sessionId, session.getConversationHistory());
//            }
//
//            return Optional.of(session);
//        }
//
//        return Optional.empty();
//    }
//
//    /**
//     * Send message and persist conversation
//     */
//    public String sendMessage(String sessionId, String message) {
//        Optional<ChatSession> sessionOpt = getSession(sessionId);
//        if (sessionOpt.isEmpty()) {
//            throw new IllegalArgumentException("Session " + sessionId + " not found");
//        }
//
//        ChatSession session = sessionOpt.get();
//
//        if (!session.getSessionActive()) {
//            throw new IllegalStateException("Session " + sessionId + " is not active");
//        }
//
//        // Get memory from cache
//        MessageWindowChatMemory memory = memoryCache.get(sessionId);
//        if (memory == null) {
//            memory = MessageWindowChatMemory.withMaxMessages(100);
//            memoryCache.put(sessionId, memory);
//        }
//
//        // Add user message to memory
//        memory.add(UserMessage.from(message));
//
//        // Generate response
//        List<ChatMessage> messages = memory.messages();
//        AiMessage response = chatModel.generate(messages).content();
//
//        // Add AI response to memory
//        memory.add(response);
//
//        // Update session in database
//        session.setTotalMessages(session.getTotalMessages() + 1);
//
//        // Serialize and save conversation history
//        try {
//            List<Map<String, Object>> history = convertMemoryToHistory(memory);
//            session.setConversationHistory(objectMapper.writeValueAsString(history));
//        } catch (JsonProcessingException e) {
//            System.err.println("Error serializing conversation: " + e.getMessage());
//        }
//
//        sessionRepository.save(session);
//
//        return response.text();
//    }
//
//    /**
//     * Get conversation history from database
//     */
//    public List<Map<String, Object>> getConversationHistory(String sessionId) {
//        Optional<ChatSession> sessionOpt = getSession(sessionId);
//        if (sessionOpt.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        String historyJson = sessionOpt.get().getConversationHistory();
//        if (historyJson == null || historyJson.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        try {
//            return objectMapper.readValue(historyJson, List.class);
//        } catch (JsonProcessingException e) {
//            System.err.println("Error deserializing conversation history: " + e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * Get user preferences
//     */
//    public Map<String, Object> getUserPreferences(String sessionId) {
//        Optional<ChatSession> sessionOpt = getSession(sessionId);
//        if (sessionOpt.isEmpty()) {
//            return Collections.emptyMap();
//        }
//
//        String preferencesJson = sessionOpt.get().getUserPreferences();
//        if (preferencesJson == null || preferencesJson.isEmpty()) {
//            return Collections.emptyMap();
//        }
//
//        try {
//            return objectMapper.readValue(preferencesJson, Map.class);
//        } catch (JsonProcessingException e) {
//            System.err.println("Error deserializing preferences: " + e.getMessage());
//            return Collections.emptyMap();
//        }
//    }
//
//    /**
//     * Update user preferences
//     */
//    public void updateUserPreferences(String sessionId, Map<String, Object> preferences) {
//        Optional<ChatSession> sessionOpt = getSession(sessionId);
//        if (sessionOpt.isEmpty()) {
//            throw new IllegalArgumentException("Session " + sessionId + " not found");
//        }
//
//        ChatSession session = sessionOpt.get();
//
//        try {
//            session.setUserPreferences(objectMapper.writeValueAsString(preferences));
//            sessionRepository.save(session);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Error serializing preferences", e);
//        }
//    }
//
//    /**
//     * List active sessions for a user
//     */
//    public List<ChatSession> getUserActiveSessions(String userId) {
//        return sessionRepository.findByUserIdAndSessionActive(userId, true);
//    }
//
//    /**
//     * End session
//     */
//    public void endSession(String sessionId) {
//        Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
//        if (sessionOpt.isPresent()) {
//            ChatSession session = sessionOpt.get();
//            session.setSessionActive(false);
//            sessionRepository.save(session);
//
//            // Remove from memory cache
//            memoryCache.remove(sessionId);
//
//            System.out.println("Session " + sessionId + " ended");
//        }
//    }
//
//    /**
//     * Clean up expired sessions
//     */
//    public int cleanupExpiredSessions(int hoursToExpire) {
//        LocalDateTime expireTime = LocalDateTime.now().minusHours(hoursToExpire);
//
//        // Remove from memory cache
//        List<ChatSession> expiredSessions = sessionRepository.findByLastAccessedBefore(expireTime);
//        for (ChatSession session : expiredSessions) {
//            memoryCache.remove(session.getSessionId());
//        }
//
//        // Deactivate in database
//        return sessionRepository.deactivateExpiredSessions(expireTime);
//    }
//
//    /**
//     * Helper method to load conversation history into memory
//     */
//    private void loadConversationIntoMemory(String sessionId, String historyJson) {
//        if (historyJson == null || historyJson.isEmpty()) {
//            return;
//        }
//
//        try {
//            List<Map<String, Object>> history = objectMapper.readValue(historyJson, List.class);
//            MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(100);
//
//            for (Map<String, Object> messageData : history) {
//                String type = (String) messageData.get("type");
//                String content = (String) messageData.get("content");
//
//                if ("USER".equals(type)) {
//                    memory.add(UserMessage.from(content));
//                } else if ("AI".equals(type)) {
//                    memory.add(AiMessage.from(content));
//                }
//            }
//
//            memoryCache.put(sessionId, memory);
//        } catch (JsonProcessingException e) {
//            System.err.println("Error loading conversation history: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Helper method to convert memory to serializable format
//     */
//    private List<Map<String, Object>> convertMemoryToHistory(MessageWindowChatMemory memory) {
//        List<Map<String, Object>> history = new ArrayList<>();
//
//        for (ChatMessage message : memory.messages()) {
//            Map<String, Object> messageData = new HashMap<>();
//            messageData.put("type", message.type().toString());
//            messageData.put("content", message.text());
//            messageData.put("timestamp", LocalDateTime.now().toString());
//            history.add(messageData);
//        }
//
//        return history;
//    }
//}
