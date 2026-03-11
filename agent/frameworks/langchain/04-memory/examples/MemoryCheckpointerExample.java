/**
 * Memory Checkpointer Example
 * Shows how to create persistent conversation checkpoints for state management
 */
package com.example.agent.langchain.memory;

import com.example.agent.langchain.basics.ConfigurationUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.nio.file.*;

public class MemoryCheckpointerExample {

    public static void main(String[] args) {
        // Check if API key is available
        if (!ConfigurationUtil.isApiKeyAvailable()) {
            ConfigurationUtil.printApiKeyInstructions();
            return;
        }

        System.out.println("=== Memory Checkpointer Example ===");
        System.out.println("Shows how to create persistent conversation checkpoints\n");

        try {
            // Initialize ChatModel using ConfigurationUtil
            ConfigurationUtil config = ConfigurationUtil.create();
            ChatLanguageModel chatModel = config.createChatModel();

            // Create checkpointer system
            ConversationCheckpointer checkpointer = new ConversationCheckpointer("./checkpoints");

            System.out.println("🔧 Created Conversation Checkpointer:");
            System.out.println("   • Saves conversation state to disk");
            System.out.println("   • Restores conversations across app restarts");
            System.out.println("   • Manages multiple user sessions");
            System.out.println("   • Creates automatic checkpoints\n");

            // Demonstrate checkpointing workflow
            demonstrateCheckpointing(checkpointer, chatModel);
            demonstrateStateRestoration(checkpointer, chatModel);
            demonstrateMultiUserCheckpoints(checkpointer, chatModel);

            System.out.println("\n✅ Key Benefits of Checkpointer:");
            System.out.println("• ✅ Conversation state survives app restarts");
            System.out.println("• ✅ Multi-user session management");
            System.out.println("• ✅ Recovery from failures");
            System.out.println("• ✅ Conversation branching and rollback");

            System.out.println("\n💾 Use Cases:");
            System.out.println("• Long-running conversations (days/weeks)");
            System.out.println("• Multi-session learning platforms");
            System.out.println("• Customer service with session continuity");
            System.out.println("• Research assistants with persistent context");

        } catch (Exception e) {
            System.err.println("❌ Error in checkpointer example: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demonstrateCheckpointing(ConversationCheckpointer checkpointer,
                                                ChatLanguageModel chatModel) {
        System.out.println("🔄 Demo 1: Creating and Saving Checkpoints");
        System.out.println("─".repeat(60));

        String userId = "john_doe";
        String sessionId = "learning_session_1";

        // Start a conversation
        checkpointer.createSession(userId, sessionId, "Java learning session");

        String[] conversation = {
            "I want to learn Java programming from scratch",
            "Can you explain what variables are in Java?",
            "How do I declare different types of variables?",
            "What's the difference between int and String?"
        };

        for (int i = 0; i < conversation.length; i++) {
            String userInput = conversation[i];
            System.out.printf("👤 Message %d: %s\n", i + 1, userInput);

            // Process message and get response
            String response = checkpointer.processMessage(userId, sessionId, userInput, chatModel);

            System.out.printf("🤖 Response %d: %s\n", i + 1,
                response.length() > 100 ? response.substring(0, 100) + "..." : response);

            // Create checkpoint after each exchange
            String checkpointId = checkpointer.createCheckpoint(userId, sessionId,
                "After discussing " + getTopicFromMessage(userInput));

            System.out.printf("💾 Checkpoint created: %s\n", checkpointId);
            System.out.println();
        }

        // Show checkpoint list
        List<CheckpointInfo> checkpoints = checkpointer.listCheckpoints(userId, sessionId);
        System.out.println("📋 Available Checkpoints:");
        for (CheckpointInfo checkpoint : checkpoints) {
            System.out.printf("   • %s: %s (Messages: %d)\n",
                checkpoint.getCheckpointId(),
                checkpoint.getDescription(),
                checkpoint.getMessageCount());
        }
        System.out.println();
    }

    private static void demonstrateStateRestoration(ConversationCheckpointer checkpointer,
                                                  ChatLanguageModel chatModel) {
        System.out.println("🔄 Demo 2: Restoring from Checkpoints");
        System.out.println("─".repeat(60));

        String userId = "john_doe";
        String sessionId = "learning_session_1";

        // List available checkpoints
        List<CheckpointInfo> checkpoints = checkpointer.listCheckpoints(userId, sessionId);
        if (!checkpoints.isEmpty()) {
            // Restore to an earlier checkpoint
            CheckpointInfo earlierCheckpoint = checkpoints.get(1); // Second checkpoint
            System.out.printf("🔄 Restoring to checkpoint: %s\n", earlierCheckpoint.getCheckpointId());

            boolean restored = checkpointer.restoreFromCheckpoint(userId, sessionId,
                earlierCheckpoint.getCheckpointId());

            if (restored) {
                System.out.println("✅ Successfully restored conversation state");

                // Continue conversation from restored point
                String newMessage = "Can you give me a practical example of using variables?";
                System.out.printf("👤 New message after restore: %s\n", newMessage);

                String response = checkpointer.processMessage(userId, sessionId, newMessage, chatModel);
                System.out.printf("🤖 Response: %s\n", response.length() > 100 ?
                    response.substring(0, 100) + "..." : response);

                // Show current conversation state
                ConversationState state = checkpointer.getCurrentState(userId, sessionId);
                System.out.printf("📊 Current state: %d messages in conversation\n",
                    state.getMessageCount());
            }
        }
        System.out.println();
    }

    private static void demonstrateMultiUserCheckpoints(ConversationCheckpointer checkpointer,
                                                       ChatLanguageModel chatModel) {
        System.out.println("🔄 Demo 3: Multi-User Checkpoint Management");
        System.out.println("─".repeat(60));

        // Create sessions for multiple users
        String[] users = {"alice", "bob", "charlie"};
        String[] topics = {"Python basics", "JavaScript fundamentals", "Data structures"};

        for (int i = 0; i < users.length; i++) {
            String userId = users[i];
            String sessionId = "session_" + (i + 1);
            String topic = topics[i];

            checkpointer.createSession(userId, sessionId, "Learning " + topic);

            String message = "I want to learn about " + topic;
            String response = checkpointer.processMessage(userId, sessionId, message, chatModel);

            String checkpointId = checkpointer.createCheckpoint(userId, sessionId,
                "Started " + topic + " learning");

            System.out.printf("👤 %s: %s\n", userId, message);
            System.out.printf("💾 Checkpoint: %s\n", checkpointId);
        }

        // Show all active sessions
        System.out.println("\n📊 All Active Sessions:");
        Map<String, List<String>> allSessions = checkpointer.getAllActiveSessions();
        allSessions.forEach((user, sessions) -> {
            System.out.printf("   User %s: %s\n", user, String.join(", ", sessions));
        });
        System.out.println();
    }

    private static String getTopicFromMessage(String message) {
        if (message.toLowerCase().contains("variable")) return "variables";
        if (message.toLowerCase().contains("java")) return "Java basics";
        if (message.toLowerCase().contains("type")) return "data types";
        return "programming concept";
    }
}

/**
 * Conversation Checkpointer Implementation
 * Handles saving and restoring conversation states with persistent storage
 */
class ConversationCheckpointer {
    private final String checkpointDirectory;
    private final Map<String, ConversationState> activeSessions;
    private final DateTimeFormatter timestampFormat;

    public ConversationCheckpointer(String checkpointDirectory) {
        this.checkpointDirectory = checkpointDirectory;
        this.activeSessions = new ConcurrentHashMap<>();
        this.timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        // Create checkpoint directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(checkpointDirectory));
        } catch (IOException e) {
            System.err.println("Warning: Could not create checkpoint directory: " + e.getMessage());
        }
    }

    /**
     * Create a new conversation session
     */
    public void createSession(String userId, String sessionId, String description) {
        String sessionKey = getSessionKey(userId, sessionId);
        ConversationState state = new ConversationState(userId, sessionId, description);
        activeSessions.put(sessionKey, state);

        System.out.printf("📝 Created session: %s for user %s\n", sessionId, userId);
    }

    /**
     * Process a message and update conversation state
     */
    public String processMessage(String userId, String sessionId, String userMessage,
                                ChatLanguageModel chatModel) {
        String sessionKey = getSessionKey(userId, sessionId);
        ConversationState state = activeSessions.get(sessionKey);

        if (state == null) {
            throw new IllegalArgumentException("Session not found: " + sessionKey);
        }

        // Add user message to memory
        UserMessage userMsg = UserMessage.from(userMessage);
        state.getMemory().add(userMsg);

        // Get AI response
        List<ChatMessage> messages = state.getMemory().messages();
        AiMessage response = chatModel.generate(messages).content();

        // Add AI response to memory
        state.getMemory().add(response);

        // Update state
        state.incrementMessageCount();
        state.setLastActivity(LocalDateTime.now());

        return response.text();
    }

    /**
     * Create a checkpoint of current conversation state
     * Checkpoint: A snapshot of conversation state that can be restored later
     */
    public String createCheckpoint(String userId, String sessionId, String description) {
        String sessionKey = getSessionKey(userId, sessionId);
        ConversationState state = activeSessions.get(sessionKey);

        if (state == null) {
            throw new IllegalArgumentException("Session not found: " + sessionKey);
        }

        // Generate unique checkpoint ID with timestamp
        String timestamp = LocalDateTime.now().format(timestampFormat);
        String checkpointId = String.format("cp_%s_%s", timestamp, state.getMessageCount());

        try {
            // Save checkpoint to file
            CheckpointData checkpoint = new CheckpointData(
                checkpointId,
                userId,
                sessionId,
                description,
                LocalDateTime.now(),
                state.getMemory().messages(),
                state.getMessageCount(),
                state.getMetadata()
            );

            saveCheckpointToFile(checkpoint);

            // Add to state's checkpoint list
            state.addCheckpoint(new CheckpointInfo(checkpointId, description, state.getMessageCount()));

            return checkpointId;

        } catch (Exception e) {
            System.err.println("Error creating checkpoint: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restore conversation state from a specific checkpoint
     */
    public boolean restoreFromCheckpoint(String userId, String sessionId, String checkpointId) {
        try {
            CheckpointData checkpoint = loadCheckpointFromFile(userId, sessionId, checkpointId);
            if (checkpoint == null) {
                return false;
            }

            // Restore conversation state
            String sessionKey = getSessionKey(userId, sessionId);
            ConversationState state = activeSessions.get(sessionKey);

            if (state == null) {
                // Create new state if not exists
                state = new ConversationState(userId, sessionId, "Restored session");
                activeSessions.put(sessionKey, state);
            }

            // Clear current memory and restore from checkpoint
            state.getMemory().clear();
            for (ChatMessage message : checkpoint.getMessages()) {
                state.getMemory().add(message);
            }

            // Update state metadata
            state.setMessageCount(checkpoint.getMessageCount());
            state.setLastActivity(LocalDateTime.now());

            System.out.printf("✅ Restored session to checkpoint: %s\n", checkpointId);
            return true;

        } catch (Exception e) {
            System.err.println("Error restoring checkpoint: " + e.getMessage());
            return false;
        }
    }

    /**
     * List all available checkpoints for a session
     */
    public List<CheckpointInfo> listCheckpoints(String userId, String sessionId) {
        String sessionKey = getSessionKey(userId, sessionId);
        ConversationState state = activeSessions.get(sessionKey);

        if (state == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(state.getCheckpoints());
    }

    /**
     * Get current conversation state
     */
    public ConversationState getCurrentState(String userId, String sessionId) {
        String sessionKey = getSessionKey(userId, sessionId);
        return activeSessions.get(sessionKey);
    }

    /**
     * Get all active sessions across all users
     */
    public Map<String, List<String>> getAllActiveSessions() {
        Map<String, List<String>> userSessions = new HashMap<>();

        activeSessions.forEach((sessionKey, state) -> {
            String userId = state.getUserId();
            String sessionId = state.getSessionId();

            userSessions.computeIfAbsent(userId, k -> new ArrayList<>()).add(sessionId);
        });

        return userSessions;
    }

    // Helper methods

    private String getSessionKey(String userId, String sessionId) {
        return userId + ":" + sessionId;
    }

    private void saveCheckpointToFile(CheckpointData checkpoint) throws IOException {
        String filename = String.format("%s_%s_%s.checkpoint",
            checkpoint.getUserId(),
            checkpoint.getSessionId(),
            checkpoint.getCheckpointId());

        Path filePath = Paths.get(checkpointDirectory, filename);

        // Simple serialization (in production, use JSON or proper serialization)
        StringBuilder content = new StringBuilder();
        content.append("CHECKPOINT_ID=").append(checkpoint.getCheckpointId()).append("\n");
        content.append("USER_ID=").append(checkpoint.getUserId()).append("\n");
        content.append("SESSION_ID=").append(checkpoint.getSessionId()).append("\n");
        content.append("DESCRIPTION=").append(checkpoint.getDescription()).append("\n");
        content.append("TIMESTAMP=").append(checkpoint.getTimestamp()).append("\n");
        content.append("MESSAGE_COUNT=").append(checkpoint.getMessageCount()).append("\n");
        content.append("MESSAGES_START\n");

        for (ChatMessage message : checkpoint.getMessages()) {
            String type = message instanceof UserMessage ? "USER" : "AI";
            String text = message instanceof UserMessage ?
                ((UserMessage) message).singleText() :
                ((AiMessage) message).text();
            content.append(type).append(":").append(text.replace("\n", "\\n")).append("\n");
        }

        content.append("MESSAGES_END\n");

        Files.write(filePath, content.toString().getBytes());
    }

    private CheckpointData loadCheckpointFromFile(String userId, String sessionId, String checkpointId) {
        try {
            String filename = String.format("%s_%s_%s.checkpoint", userId, sessionId, checkpointId);
            Path filePath = Paths.get(checkpointDirectory, filename);

            if (!Files.exists(filePath)) {
                return null;
            }

            List<String> lines = Files.readAllLines(filePath);

            // Parse checkpoint data (simplified parsing)
            String description = "";
            LocalDateTime timestamp = LocalDateTime.now();
            int messageCount = 0;
            List<ChatMessage> messages = new ArrayList<>();

            boolean inMessages = false;
            for (String line : lines) {
                if (line.startsWith("DESCRIPTION=")) {
                    description = line.substring("DESCRIPTION=".length());
                } else if (line.startsWith("MESSAGE_COUNT=")) {
                    messageCount = Integer.parseInt(line.substring("MESSAGE_COUNT=".length()));
                } else if (line.equals("MESSAGES_START")) {
                    inMessages = true;
                } else if (line.equals("MESSAGES_END")) {
                    inMessages = false;
                } else if (inMessages && line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String type = parts[0];
                    String content = parts[1].replace("\\n", "\n");

                    if ("USER".equals(type)) {
                        messages.add(UserMessage.from(content));
                    } else if ("AI".equals(type)) {
                        messages.add(AiMessage.from(content));
                    }
                }
            }

            return new CheckpointData(checkpointId, userId, sessionId, description,
                timestamp, messages, messageCount, new HashMap<>());

        } catch (Exception e) {
            System.err.println("Error loading checkpoint: " + e.getMessage());
            return null;
        }
    }
}

/**
 * Represents the current state of a conversation session
 */
class ConversationState {
    private final String userId;
    private final String sessionId;
    private final String description;
    private final MessageWindowChatMemory memory;
    private final List<CheckpointInfo> checkpoints;
    private final Map<String, Object> metadata;
    private int messageCount;
    private LocalDateTime lastActivity;

    public ConversationState(String userId, String sessionId, String description) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.description = description;
        this.memory = MessageWindowChatMemory.withMaxMessages(50);
        this.checkpoints = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.messageCount = 0;
        this.lastActivity = LocalDateTime.now();
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public String getDescription() { return description; }
    public MessageWindowChatMemory getMemory() { return memory; }
    public List<CheckpointInfo> getCheckpoints() { return new ArrayList<>(checkpoints); }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public int getMessageCount() { return messageCount; }
    public LocalDateTime getLastActivity() { return lastActivity; }

    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
    public void incrementMessageCount() { this.messageCount++; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    public void addCheckpoint(CheckpointInfo checkpoint) { this.checkpoints.add(checkpoint); }
}

/**
 * Information about a specific checkpoint
 */
class CheckpointInfo {
    private final String checkpointId;
    private final String description;
    private final int messageCount;

    public CheckpointInfo(String checkpointId, String description, int messageCount) {
        this.checkpointId = checkpointId;
        this.description = description;
        this.messageCount = messageCount;
    }

    public String getCheckpointId() { return checkpointId; }
    public String getDescription() { return description; }
    public int getMessageCount() { return messageCount; }
}

/**
 * Complete checkpoint data for serialization
 */
class CheckpointData {
    private final String checkpointId;
    private final String userId;
    private final String sessionId;
    private final String description;
    private final LocalDateTime timestamp;
    private final List<ChatMessage> messages;
    private final int messageCount;
    private final Map<String, Object> metadata;

    public CheckpointData(String checkpointId, String userId, String sessionId,
                         String description, LocalDateTime timestamp,
                         List<ChatMessage> messages, int messageCount,
                         Map<String, Object> metadata) {
        this.checkpointId = checkpointId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.description = description;
        this.timestamp = timestamp;
        this.messages = new ArrayList<>(messages);
        this.messageCount = messageCount;
        this.metadata = new HashMap<>(metadata);
    }

    // Getters
    public String getCheckpointId() { return checkpointId; }
    public String getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<ChatMessage> getMessages() { return new ArrayList<>(messages); }
    public int getMessageCount() { return messageCount; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
}
