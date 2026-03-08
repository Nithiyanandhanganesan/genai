/**
 * Standalone Database Demo - No Spring Boot Required
 * This shows you exactly how the ChatSession entity works with a real database
 */
package com.example.agent.langchain.session;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SimpleDatabaseDemo {

    public static void main(String[] args) {
        if (!com.example.agent.langchain.basics.ConfigurationUtil.isApiKeyAvailable()) {
            System.out.println("⚠️  OpenAI API key not found. Setting up mock data for database demo.");
        }

        System.out.println("=== SIMPLE DATABASE DEMO ===");
        System.out.println("This demo shows exactly how ChatSession data is stored in the database\n");

        try {
            // Create in-memory H2 database
            Connection conn = DriverManager.getConnection(
                "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1", "sa", "");

            // Create the chat_sessions table (same as JPA would create)
            createTable(conn);

            // Demo 1: Create a session and show database
            System.out.println("📊 DEMO 1: Creating Session Record");
            String sessionId = UUID.randomUUID().toString();
            insertSession(conn, sessionId, "john_doe");
            showTableData(conn);

            // Demo 2: Add conversation history and show database
            System.out.println("\n📊 DEMO 2: Adding Conversation History");
            String conversationJson = createConversationJson();
            updateConversationHistory(conn, sessionId, conversationJson, 2);
            showTableData(conn);

            // Demo 3: Add more messages and show growth
            System.out.println("\n📊 DEMO 3: Adding More Messages");
            String longerConversationJson = createLongerConversationJson();
            updateConversationHistory(conn, sessionId, longerConversationJson, 4);
            showTableData(conn);

            // Demo 4: Show specific queries
            System.out.println("\n📊 DEMO 4: Querying Specific Data");
            showConversationHistory(conn, sessionId);
            showUserPreferences(conn, sessionId);

            System.out.println("\n✅ Demo completed! This shows exactly what happens in the database.");
            System.out.println("🔍 You can see how:")
            System.out.println("   • ChatSession entity becomes a database table");
            System.out.println("   • Conversation history is stored as JSON");
            System.out.println("   • Each message exchange updates the database");
            System.out.println("   • Data persists and can be queried later");

            conn.close();

        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTable(Connection conn) throws SQLException {
        String createTableSQL = """
            CREATE TABLE chat_sessions (
                session_id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                last_accessed TIMESTAMP NOT NULL,
                conversation_history TEXT,
                user_preferences TEXT,
                total_messages INTEGER,
                session_active BOOLEAN,
                session_metadata VARCHAR(500)
            )
            """;

        Statement stmt = conn.createStatement();
        stmt.execute(createTableSQL);
        System.out.println("✅ Created chat_sessions table");
    }

    private static void insertSession(Connection conn, String sessionId, String userId) throws SQLException {
        String insertSQL = """
            INSERT INTO chat_sessions 
            (session_id, user_id, created_at, last_accessed, total_messages, session_active, user_preferences) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        PreparedStatement pstmt = conn.prepareStatement(insertSQL);
        pstmt.setString(1, sessionId);
        pstmt.setString(2, userId);
        pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        pstmt.setInt(5, 0);
        pstmt.setBoolean(6, true);
        pstmt.setString(7, "{\"language\":\"English\",\"expertise\":\"beginner\"}");

        pstmt.executeUpdate();
        System.out.println("✅ Inserted session for user: " + userId);
    }

    private static void updateConversationHistory(Connection conn, String sessionId,
                                                String conversationJson, int messageCount) throws SQLException {
        String updateSQL = """
            UPDATE chat_sessions 
            SET conversation_history = ?, total_messages = ?, last_accessed = ? 
            WHERE session_id = ?
            """;

        PreparedStatement pstmt = conn.prepareStatement(updateSQL);
        pstmt.setString(1, conversationJson);
        pstmt.setInt(2, messageCount);
        pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        pstmt.setString(4, sessionId);

        pstmt.executeUpdate();
        System.out.println("✅ Updated conversation history with " + messageCount + " messages");
    }

    private static void showTableData(Connection conn) throws SQLException {
        String selectSQL = "SELECT * FROM chat_sessions";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL);

        System.out.println("📋 Current Database State:");
        System.out.println("   TABLE: chat_sessions");

        while (rs.next()) {
            System.out.println("   ┌─────────────────────────────────────────────────");
            System.out.println("   │ Session ID: " + rs.getString("session_id").substring(0, 8) + "...");
            System.out.println("   │ User ID: " + rs.getString("user_id"));
            System.out.println("   │ Created: " + rs.getTimestamp("created_at"));
            System.out.println("   │ Last Accessed: " + rs.getTimestamp("last_accessed"));
            System.out.println("   │ Total Messages: " + rs.getInt("total_messages"));
            System.out.println("   │ Active: " + rs.getBoolean("session_active"));

            String history = rs.getString("conversation_history");
            if (history != null) {
                System.out.println("   │ Conversation History Length: " + history.length() + " chars");
                System.out.println("   │ History Preview: " +
                    (history.length() > 100 ? history.substring(0, 100) + "..." : history));
            } else {
                System.out.println("   │ Conversation History: NULL");
            }

            String prefs = rs.getString("user_preferences");
            System.out.println("   │ User Preferences: " + prefs);
            System.out.println("   └─────────────────────────────────────────────────");
        }
    }

    private static void showConversationHistory(Connection conn, String sessionId) throws SQLException {
        String selectSQL = "SELECT conversation_history FROM chat_sessions WHERE session_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(selectSQL);
        pstmt.setString(1, sessionId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("🔍 CONVERSATION HISTORY DETAILS:");
        if (rs.next()) {
            String history = rs.getString("conversation_history");
            if (history != null) {
                System.out.println("Raw JSON stored in database:");
                System.out.println(history);
                System.out.println();
                System.out.println("This JSON contains:");
                System.out.println("• Each user message and AI response");
                System.out.println("• Message types (USER/AI)");
                System.out.println("• Timestamps");
                System.out.println("• Full conversation context");
            }
        }
    }

    private static void showUserPreferences(Connection conn, String sessionId) throws SQLException {
        String selectSQL = "SELECT user_preferences FROM chat_sessions WHERE session_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(selectSQL);
        pstmt.setString(1, sessionId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("\n🔍 USER PREFERENCES:");
        if (rs.next()) {
            String prefs = rs.getString("user_preferences");
            System.out.println("Stored preferences JSON: " + prefs);
        }
    }

    private static String createConversationJson() {
        return """
            [
              {
                "type": "USER",
                "content": "Hello! What is Java?",
                "timestamp": "2026-03-07T10:30:00"
              },
              {
                "type": "AI",
                "content": "Java is a popular object-oriented programming language developed by Sun Microsystems.",
                "timestamp": "2026-03-07T10:30:05"
              }
            ]
            """;
    }

    private static String createLongerConversationJson() {
        return """
            [
              {
                "type": "USER",
                "content": "Hello! What is Java?",
                "timestamp": "2026-03-07T10:30:00"
              },
              {
                "type": "AI",
                "content": "Java is a popular object-oriented programming language developed by Sun Microsystems.",
                "timestamp": "2026-03-07T10:30:05"
              },
              {
                "type": "USER", 
                "content": "Can you show me a simple Java example?",
                "timestamp": "2026-03-07T10:31:00"
              },
              {
                "type": "AI",
                "content": "Here's a simple Java example: public class Hello { public static void main(String[] args) { System.out.println(\\"Hello World!\\"); } }",
                "timestamp": "2026-03-07T10:31:10"
              }
            ]
            """;
    }
}
