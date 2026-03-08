# Database Session Persistence

## 🎯 Understanding Database Session Management

This demo shows how ChatSession entities automatically persist conversation data to databases using JPA/Hibernate.

## 🏗️ Core Concepts

### Entity to Table Mapping
The `@Entity` annotation on `ChatSession` class automatically creates a database table:

```sql
CREATE TABLE chat_sessions (
    session_id           VARCHAR(36) PRIMARY KEY,
    user_id             VARCHAR(255) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    last_accessed       TIMESTAMP NOT NULL,
    conversation_history TEXT,
    user_preferences     TEXT,
    total_messages       INTEGER,
    session_active       BOOLEAN,
    session_metadata     VARCHAR(500)
);
```

### Automatic Persistence Flow

#### 1. Message Storage Process
When `sendMessage()` is called:
- User message → Memory object
- AI generates response → Memory object  
- Memory → JSON serialization
- JSON → Database storage (conversation_history column)
- Session metadata updated automatically

#### 2. Data Retrieval Process
When session is accessed again:
- Database query retrieves session record
- JSON conversation_history → Memory objects
- Memory cache updated for performance
- Conversation context fully restored

### JSON Storage Format
Conversations are stored as JSON in the database:
```json
[
  {
    "type": "USER",
    "content": "Hello! What is Java?",
    "timestamp": "2026-03-07T10:30:00"
  },
  {
    "type": "AI",
    "content": "Java is a popular programming language...",
    "timestamp": "2026-03-07T10:30:05"
  }
]
```

## 🚀 Demo Files

### `SimpleDatabaseDemo.java` ⭐ **Standalone Demo**
- No Spring Boot required
- Shows table creation and data storage
- Demonstrates SQL queries and JSON storage
- **Run**: `java SimpleDatabaseDemo`

### `DatabaseSessionDemoApp.java` - **Spring Boot Integration**
- Full Spring Boot application
- Real AI integration with ConfigurationUtil
- H2 console for database inspection
- **Run**: `mvn spring-boot:run` then visit `http://localhost:8080/h2-console`

## 🔑 Key Architecture Points

### Memory Cache + Database Strategy
- **Performance**: Active sessions cached in memory
- **Persistence**: All data automatically saved to database
- **Scalability**: Sessions survive application restarts
- **Consistency**: Cache and database stay synchronized

### Automatic Data Management
- **No Manual Database Operations**: All persistence happens automatically
- **JSON Serialization**: Complex conversation objects → Database-friendly JSON
- **Session Lifecycle**: Creation, updates, and cleanup handled transparently
- **Spring Integration**: JPA repositories handle all SQL operations

### Configuration Requirements
```properties
# Database setup (application.properties)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

## 🔍 Database Inspection

### SQL Queries for Analysis
```sql
-- View all sessions
SELECT * FROM chat_sessions;

-- Check conversation history
SELECT session_id, conversation_history FROM chat_sessions 
WHERE conversation_history IS NOT NULL;

-- Count messages per user
SELECT user_id, SUM(total_messages) as total 
FROM chat_sessions GROUP BY user_id;
```

### H2 Console Access
1. Start Spring Boot application
2. Navigate to `http://localhost:8080/h2-console`
3. Use connection: `jdbc:h2:mem:testdb`, username: `sa`, password: (empty)

## 💡 Best Practices

### Session Management
- Use UUIDs for session identifiers
- Implement session cleanup for expired records
- Cache frequently accessed sessions
- Store user preferences as JSON for flexibility

### Performance Optimization
- Limit conversation history size (use MessageWindowChatMemory)
- Implement proper indexing on user_id and session_active columns
- Use database connection pooling
- Consider Redis for high-traffic memory caching

### Production Considerations
- Use PostgreSQL or MySQL instead of H2 for production
- Implement proper backup strategies for conversation data
- Add encryption for sensitive conversation content
- Monitor database performance and query optimization
