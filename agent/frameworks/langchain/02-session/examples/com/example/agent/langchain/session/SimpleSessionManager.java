/**
 * Session Management Examples - Simple In-Memory Implementation
 * Java implementation using LangChain4j framework
 */
package com.example.agent.langchain.session;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;

/**
 * Session data container
 */
class SessionData {
    private final String sessionId;
    private final String userId;
    private final LocalDateTime createdAt;
    private LocalDateTime lastAccessed;
    private final MessageWindowChatMemory memory;
    private final ChatLanguageModel model;
    private int totalMessages;
    private boolean sessionActive;

    public SessionData(String sessionId, String userId, ChatLanguageModel model) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
        this.memory = MessageWindowChatMemory.withMaxMessages(100);
        this.model = model;
        this.totalMessages = 0;
        this.sessionActive = true;
    }

    public void incrementTotalMessages() {
        this.totalMessages++;
    }

    // Explicit getters/setters to ensure compatibility if Lombok has issues
    public String getSessionId() { return sessionId; }
    public String getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
    public MessageWindowChatMemory getMemory() { return memory; }
    public ChatLanguageModel getModel() { return model; }
    public int getTotalMessages() { return totalMessages; }
    public boolean isSessionActive() { return sessionActive; }
    public void setSessionActive(boolean sessionActive) { this.sessionActive = sessionActive; }
}

/**
 * Basic in-memory session manager for development and testing
 */
public class SimpleSessionManager {

    private final ChatLanguageModel chatModel;
    private final Map<String, SessionData> sessions;

    public SimpleSessionManager(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
        this.sessions = new ConcurrentHashMap<>();
    }

    /**
     * Create a new session for a user
     */
    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();

        // Create session data with memory and model
        SessionData sessionData = new SessionData(sessionId, userId, chatModel);

        // Store session
        sessions.put(sessionId, sessionData);

        System.out.println("Created session " + sessionId + " for user " + userId);
        return sessionId;
    }

    /**
     * Retrieve session data
     */
    public Optional<SessionData> getSession(String sessionId) {
        SessionData session = sessions.get(sessionId);
        if (session != null) {
            // Update last accessed time
            session.setLastAccessed(LocalDateTime.now());
            return Optional.of(session);
        }
        return Optional.empty();
    }

    /**
     * Send a message in a session and get response
     */
    public String sendMessage(String sessionId, String message) {
        Optional<SessionData> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("Session " + sessionId + " not found");
        }

        SessionData session = sessionOpt.get();

        // Add user message to memory
        session.getMemory().add(UserMessage.from(message));

        // Generate response using the model
        List<ChatMessage> messages = session.getMemory().messages();
        AiMessage response = session.getModel().generate(messages).content();

        // Add AI response to memory
        session.getMemory().add(response);

        // Update metadata
        session.incrementTotalMessages();

        return response.text();
    }

    /**
     * Get conversation history for a session
     */
    public List<Map<String, Object>> getConversationHistory(String sessionId) {
        Optional<SessionData> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            return Collections.emptyList();
        }

        SessionData session = sessionOpt.get();
        List<Map<String, Object>> history = new ArrayList<>();

        // Extract messages from memory
        List<ChatMessage> messages = session.getMemory().messages();
        for (ChatMessage msg : messages) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", msg.type().toString());

            // Handle different message types and extract content safely
            String content;
            if (msg instanceof UserMessage) {
                content = ((UserMessage) msg).singleText();
            } else if (msg instanceof AiMessage) {
                content = ((AiMessage) msg).text();
            } else {
                // Fallback for other message types
                content = msg.toString();
            }

            messageData.put("content", content);
            messageData.put("timestamp", LocalDateTime.now()); // In real implementation, store actual timestamp
            history.add(messageData);
        }

        return history;
    }


    /**
     * List all active sessions
     */
    public List<Map<String, Object>> listActiveSessions() {
        List<Map<String, Object>> activeSessions = new ArrayList<>();

        for (SessionData session : sessions.values()) {
            if (session.isSessionActive()) {
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("sessionId", session.getSessionId());
                sessionInfo.put("userId", session.getUserId());
                sessionInfo.put("createdAt", session.getCreatedAt());
                sessionInfo.put("totalMessages", session.getTotalMessages());
                activeSessions.add(sessionInfo);
            }
        }

        return activeSessions;
    }

}

/**
 * Example usage and testing
 */
class SessionExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        try {
            // Initialize ChatModel using ConfigurationUtil
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createChatModel();

            // Create session manager
            SimpleSessionManager sessionManager = new SimpleSessionManager(chatModel);

        // Create sessions for different users
        String session1 = sessionManager.createSession("user_123");
        String session2 = sessionManager.createSession("user_456");

        System.out.println("\n=== Session 1 Conversation ===");
        // Have a conversation in session 1
        String response1 = sessionManager.sendMessage(session1, "Hello, my name is Alice");
        System.out.println("AI: " + response1);

        String response2 = sessionManager.sendMessage(session1, "What's my name?");
        System.out.println("AI: " + response2);

        System.out.println("\n=== Session 2 Conversation ===");
        // Have a different conversation in session 2
        String response3 = sessionManager.sendMessage(session2, "Hello, my name is Bob");
        System.out.println("AI: " + response3);

        String response4 = sessionManager.sendMessage(session2, "What's my name?");
        System.out.println("AI: " + response4);

        System.out.println("\n=== Session History ===");
        // Check conversation history
        List<Map<String, Object>> history1 = sessionManager.getConversationHistory(session1);
        System.out.println("Session 1 has " + history1.size() + " messages");

        List<Map<String, Object>> history2 = sessionManager.getConversationHistory(session2);
        System.out.println("Session 2 has " + history2.size() + " messages");

        System.out.println("\n=== Active Sessions ===");
        // List active sessions
        List<Map<String, Object>> active = sessionManager.listActiveSessions();
        for (Map<String, Object> sessionInfo : active) {
            String sessionId = (String) sessionInfo.get("sessionId");
            String displayId = sessionId.substring(0, 8) + "...";
            System.out.println("Session: " + displayId + " " +
                    "User: " + sessionInfo.get("userId") + " " +
                    "Messages: " + sessionInfo.get("totalMessages"));
        }

        } catch (Exception e) {
            System.err.println("❌ Error during session management demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
