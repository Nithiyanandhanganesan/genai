/**
 * Database Session Demo - Shows how ChatSession entity creates tables and stores data
 * This example demonstrates the complete flow from entity creation to data persistence
 */
package com.example.agent.langchain.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Spring Boot Application to demonstrate database session management
 */
@SpringBootApplication
public class DatabaseSessionDemoApp {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "demo");
        ConfigurableApplicationContext context = SpringApplication.run(DatabaseSessionDemoApp.class, args);

        // This will automatically run the demo
        context.getBean(SessionDemoRunner.class).runDemo();

        // Keep application running to inspect database
        System.out.println("\n🔍 Application is running. You can now:");
        System.out.println("1. Connect to H2 console: http://localhost:8080/h2-console");
        System.out.println("2. JDBC URL: jdbc:h2:mem:testdb");
        System.out.println("3. Username: sa, Password: (empty)");
        System.out.println("4. Press Ctrl+C to exit");
    }
}

/**
 * Demo runner that shows the complete flow
 */
@Component
class SessionDemoRunner implements CommandLineRunner {

    @Autowired
    private DatabaseSessionManager sessionManager;

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Don't run automatically - let user call runDemo() manually
    }

    public void runDemo() {
        System.out.println("=== DATABASE SESSION DEMO ===");
        System.out.println("This demo shows how ChatSession entity creates and populates database tables\n");

        try {
            // Step 1: Show empty database
            System.out.println("📊 STEP 1: Initial Database State");
            showDatabaseState();

            // Step 2: Create a session (this creates the table and first record)
            System.out.println("\n📊 STEP 2: Creating First Session");
            Map<String, Object> userPrefs = new HashMap<>();
            userPrefs.put("language", "English");
            userPrefs.put("expertise", "beginner");

            String sessionId = sessionManager.createSession("john_doe", userPrefs);
            System.out.println("✅ Created session: " + sessionId);

            // Show database after session creation
            showDatabaseState();

            // Step 3: Send first message
            System.out.println("\n📊 STEP 3: Sending First Message");
            String response1 = sessionManager.sendMessage(sessionId, "Hello! What is Java?");
            System.out.println("👤 User: Hello! What is Java?");
            System.out.println("🤖 AI: " + response1.substring(0, Math.min(100, response1.length())) + "...");

            // Show database after first message
            showDatabaseState();

            // Step 4: Send second message (show conversation history building)
            System.out.println("\n📊 STEP 4: Sending Second Message");
            String response2 = sessionManager.sendMessage(sessionId, "Can you show me a simple example?");
            System.out.println("👤 User: Can you show me a simple example?");
            System.out.println("🤖 AI: " + response2.substring(0, Math.min(100, response2.length())) + "...");

            // Show database after second message
            showDatabaseState();

            // Step 5: Show conversation history retrieval
            System.out.println("\n📊 STEP 5: Retrieving Conversation History");
            List<Map<String, Object>> history = sessionManager.getConversationHistory(sessionId);
            System.out.println("📝 Retrieved " + history.size() + " messages from database:");

            for (int i = 0; i < history.size(); i++) {
                Map<String, Object> msg = history.get(i);
                String type = (String) msg.get("type");
                String content = (String) msg.get("content");
                String shortContent = content.length() > 80 ? content.substring(0, 80) + "..." : content;
                System.out.println("   " + (i+1) + ". " + type + ": " + shortContent);
            }

            // Step 6: Show raw database query results
            System.out.println("\n📊 STEP 6: Raw Database Queries");
            showRawDatabaseQueries();

        } catch (Exception e) {
            System.err.println("❌ Demo error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDatabaseState() {
        List<ChatSession> allSessions = sessionRepository.findAll();
        System.out.println("📊 Database State:");
        System.out.println("   Total sessions in database: " + allSessions.size());

        if (allSessions.isEmpty()) {
            System.out.println("   ℹ️  Database is empty (chat_sessions table will be created on first insert)");
        } else {
            for (ChatSession session : allSessions) {
                System.out.println("   📄 Session ID: " + session.getSessionId().substring(0, 8) + "...");
                System.out.println("      User: " + session.getUserId());
                System.out.println("      Messages: " + session.getTotalMessages());
                System.out.println("      Active: " + session.getSessionActive());
                System.out.println("      Created: " + session.getCreatedAt());
                System.out.println("      Last Accessed: " + session.getLastAccessed());

                // Show conversation history size
                String history = session.getConversationHistory();
                if (history != null) {
                    System.out.println("      Conversation History Size: " + history.length() + " characters");
                    System.out.println("      History Preview: " +
                        (history.length() > 100 ? history.substring(0, 100) + "..." : history));
                } else {
                    System.out.println("      Conversation History: null");
                }

                // Show user preferences
                String prefs = session.getUserPreferences();
                System.out.println("      User Preferences: " +
                    (prefs != null ? prefs : "null"));

                System.out.println();
            }
        }
    }

    private void showRawDatabaseQueries() {
        // These would be actual SQL queries you can run in H2 console
        System.out.println("🔍 SQL Queries you can run in H2 Console:");
        System.out.println();

        System.out.println("1. See all sessions:");
        System.out.println("   SELECT * FROM chat_sessions;");
        System.out.println();

        System.out.println("2. See session details:");
        System.out.println("   SELECT session_id, user_id, total_messages, session_active FROM chat_sessions;");
        System.out.println();

        System.out.println("3. See conversation history (JSON format):");
        System.out.println("   SELECT session_id, conversation_history FROM chat_sessions WHERE conversation_history IS NOT NULL;");
        System.out.println();

        System.out.println("4. See user preferences:");
        System.out.println("   SELECT session_id, user_preferences FROM chat_sessions WHERE user_preferences IS NOT NULL;");
        System.out.println();

        System.out.println("5. Count messages per user:");
        System.out.println("   SELECT user_id, SUM(total_messages) as total FROM chat_sessions GROUP BY user_id;");
        System.out.println();

        // Show actual data from current session
        List<ChatSession> sessions = sessionRepository.findAll();
        if (!sessions.isEmpty()) {
            ChatSession firstSession = sessions.get(0);
            System.out.println("📋 Sample Data from Current Session:");
            System.out.println("Session ID: " + firstSession.getSessionId());
            System.out.println("User ID: " + firstSession.getUserId());
            System.out.println("Total Messages: " + firstSession.getTotalMessages());
            System.out.println("Session Active: " + firstSession.getSessionActive());
            System.out.println();

            if (firstSession.getConversationHistory() != null) {
                System.out.println("Raw Conversation History JSON:");
                System.out.println(firstSession.getConversationHistory());
                System.out.println();
            }

            if (firstSession.getUserPreferences() != null) {
                System.out.println("Raw User Preferences JSON:");
                System.out.println(firstSession.getUserPreferences());
                System.out.println();
            }
        }
    }
}
