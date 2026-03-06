# Memory Management in LangGraph (Java)

## 🎯 Overview
Memory in LangGraph provides sophisticated state persistence, caching, and historical context management across graph executions. Unlike traditional memory systems, LangGraph memory integrates deeply with graph topology, supporting distributed state, checkpointing, and multi-execution memory patterns that enable complex, long-running workflows.

## 🧠 Core Memory Concepts

### What is LangGraph Memory?
LangGraph memory systems provide:
- **Graph State Persistence**: Maintain state across graph execution cycles
- **Execution History**: Track and replay execution paths and decisions
- **Checkpoint Management**: Save and restore graph state at key points
- **Distributed Memory**: Share memory across multiple graph instances
- **Context Propagation**: Pass memory context between nodes and edges

### Memory Types
1. **Execution Memory**: Short-term memory for current graph execution
2. **Session Memory**: Medium-term memory across user sessions
3. **Persistent Memory**: Long-term memory stored in databases
4. **Checkpoint Memory**: Snapshot-based recovery and rollback
5. **Shared Memory**: Cross-instance memory sharing
6. **Context Memory**: Rich contextual information storage

## 🏗️ Memory Architecture Patterns

### 1. **Linear Memory Flow**
```
Execution 1 → Memory Store → Execution 2 → Memory Store → Execution 3
```

### 2. **Branching Memory**
```
Main Execution → Branch A → Memory A
               → Branch B → Memory B
               → Merge → Combined Memory
```

### 3. **Checkpoint Pattern**
```
Start → Checkpoint 1 → Process → Checkpoint 2 → Process → End
  ↓         ↓                     ↓
Memory    Memory                Memory
```

### 4. **Distributed Memory**
```
Instance A ↘           ↗ Memory Result A
Instance B → Shared Memory → Memory Result B
Instance C ↗           ↘ Memory Result C
```

## 💻 Java Memory Implementation

### Base Memory Framework
```java
package com.example.agent.langgraph.memory;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Base interface for LangGraph memory systems
 */
public interface GraphMemory {
    
    /**
     * Store memory entry
     */
    void store(String key, Object value, MemoryContext context) throws MemoryException;
    
    /**
     * Retrieve memory entry
     */
    <T> Optional<T> retrieve(String key, Class<T> type, MemoryContext context) throws MemoryException;
    
    /**
     * Store execution state
     */
    void storeExecutionState(String executionId, GraphState state, MemoryMetadata metadata) throws MemoryException;
    
    /**
     * Retrieve execution state
     */
    Optional<GraphState> retrieveExecutionState(String executionId, MemoryContext context) throws MemoryException;
    
    /**
     * Create checkpoint
     */
    String createCheckpoint(String executionId, GraphState state, String description) throws MemoryException;
    
    /**
     * Restore from checkpoint
     */
    Optional<GraphState> restoreCheckpoint(String checkpointId, MemoryContext context) throws MemoryException;
    
    /**
     * Get execution history
     */
    List<ExecutionRecord> getExecutionHistory(String sessionId, MemoryContext context) throws MemoryException;
    
    /**
     * Search memory entries
     */
    List<MemoryEntry> searchMemory(MemoryQuery query, MemoryContext context) throws MemoryException;
    
    /**
     * Clear memory
     */
    void clearMemory(MemoryScope scope, MemoryContext context) throws MemoryException;
    
    /**
     * Get memory statistics
     */
    MemoryStats getStats() throws MemoryException;
    
    /**
     * Initialize memory system
     */
    void initialize(Map<String, Object> config) throws MemoryException;
    
    /**
     * Cleanup memory resources
     */
    void cleanup() throws MemoryException;
}

/**
 * Memory context for operations
 */
public class MemoryContext {
    private final String sessionId;
    private final String executionId;
    private final String workflowId;
    private final String userId;
    private final Map<String, Object> attributes;
    private final Duration timeout;
    
    public MemoryContext(String sessionId, String executionId, String workflowId, String userId) {
        this(sessionId, executionId, workflowId, userId, Map.of(), Duration.ofMinutes(5));
    }
    
    public MemoryContext(String sessionId, String executionId, String workflowId, String userId,
                        Map<String, Object> attributes, Duration timeout) {
        this.sessionId = sessionId;
        this.executionId = executionId;
        this.workflowId = workflowId;
        this.userId = userId;
        this.attributes = new HashMap<>(attributes);
        this.timeout = timeout;
    }
    
    // Getters
    public String getSessionId() { return sessionId; }
    public String getExecutionId() { return executionId; }
    public String getWorkflowId() { return workflowId; }
    public String getUserId() { return userId; }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
    public Duration getTimeout() { return timeout; }
}

/**
 * Memory metadata
 */
public class MemoryMetadata {
    private final LocalDateTime timestamp;
    private final String executionPhase;
    private final Map<String, Object> properties;
    private final long ttl; // Time to live in milliseconds
    
    public MemoryMetadata(String executionPhase, Map<String, Object> properties, long ttl) {
        this.timestamp = LocalDateTime.now();
        this.executionPhase = executionPhase;
        this.properties = new HashMap<>(properties);
        this.ttl = ttl;
    }
    
    public static MemoryMetadata create(String phase) {
        return new MemoryMetadata(phase, Map.of(), -1); // No expiration
    }
    
    public static MemoryMetadata create(String phase, long ttlMs) {
        return new MemoryMetadata(phase, Map.of(), ttlMs);
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getExecutionPhase() { return executionPhase; }
    public Map<String, Object> getProperties() { return new HashMap<>(properties); }
    public long getTtl() { return ttl; }
    public boolean isExpired() {
        if (ttl < 0) return false;
        return System.currentTimeMillis() - timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() > ttl;
    }
}

/**
 * Memory entry representation
 */
public class MemoryEntry {
    private final String key;
    private final Object value;
    private final MemoryMetadata metadata;
    private final MemoryContext context;
    
    public MemoryEntry(String key, Object value, MemoryMetadata metadata, MemoryContext context) {
        this.key = key;
        this.value = value;
        this.metadata = metadata;
        this.context = context;
    }
    
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getValue(Class<T> type) {
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }
    
    // Getters
    public String getKey() { return key; }
    public Object getValue() { return value; }
    public MemoryMetadata getMetadata() { return metadata; }
    public MemoryContext getContext() { return context; }
}

/**
 * Execution record for history tracking
 */
public class ExecutionRecord {
    private final String executionId;
    private final String workflowId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String status;
    private final Map<String, Object> results;
    private final List<String> checkpoints;
    
    public ExecutionRecord(String executionId, String workflowId, LocalDateTime startTime,
                          LocalDateTime endTime, String status, Map<String, Object> results,
                          List<String> checkpoints) {
        this.executionId = executionId;
        this.workflowId = workflowId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.results = new HashMap<>(results);
        this.checkpoints = new ArrayList<>(checkpoints);
    }
    
    // Getters
    public String getExecutionId() { return executionId; }
    public String getWorkflowId() { return workflowId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public Map<String, Object> getResults() { return new HashMap<>(results); }
    public List<String> getCheckpoints() { return new ArrayList<>(checkpoints); }
}

/**
 * Memory query for searching
 */
public class MemoryQuery {
    private final String pattern;
    private final MemoryScope scope;
    private final LocalDateTime fromTime;
    private final LocalDateTime toTime;
    private final Map<String, Object> filters;
    private final int maxResults;
    
    public MemoryQuery(String pattern, MemoryScope scope, LocalDateTime fromTime,
                      LocalDateTime toTime, Map<String, Object> filters, int maxResults) {
        this.pattern = pattern;
        this.scope = scope;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.filters = new HashMap<>(filters);
        this.maxResults = maxResults;
    }
    
    public static MemoryQuery forPattern(String pattern) {
        return new MemoryQuery(pattern, MemoryScope.SESSION, null, null, Map.of(), 100);
    }
    
    // Getters
    public String getPattern() { return pattern; }
    public MemoryScope getScope() { return scope; }
    public LocalDateTime getFromTime() { return fromTime; }
    public LocalDateTime getToTime() { return toTime; }
    public Map<String, Object> getFilters() { return new HashMap<>(filters); }
    public int getMaxResults() { return maxResults; }
}

/**
 * Memory scope enumeration
 */
public enum MemoryScope {
    EXECUTION,    // Current execution only
    SESSION,      // Current session
    USER,         // All user sessions
    WORKFLOW,     // Specific workflow
    GLOBAL        // System-wide
}

/**
 * Memory statistics
 */
public class MemoryStats {
    private final long totalEntries;
    private final long totalSizeBytes;
    private final Map<MemoryScope, Long> entriesByScope;
    private final double hitRate;
    private final long checkpointCount;
    
    public MemoryStats(long totalEntries, long totalSizeBytes, Map<MemoryScope, Long> entriesByScope,
                      double hitRate, long checkpointCount) {
        this.totalEntries = totalEntries;
        this.totalSizeBytes = totalSizeBytes;
        this.entriesByScope = new HashMap<>(entriesByScope);
        this.hitRate = hitRate;
        this.checkpointCount = checkpointCount;
    }
    
    // Getters
    public long getTotalEntries() { return totalEntries; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public Map<MemoryScope, Long> getEntriesByScope() { return new HashMap<>(entriesByScope); }
    public double getHitRate() { return hitRate; }
    public long getCheckpointCount() { return checkpointCount; }
}

/**
 * Memory exception
 */
public class MemoryException extends Exception {
    private final MemoryErrorType errorType;
    
    public MemoryException(String message, MemoryErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
    
    public MemoryException(String message, MemoryErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public MemoryErrorType getErrorType() { return errorType; }
}

/**
 * Memory error types
 */
public enum MemoryErrorType {
    STORAGE_ERROR,
    RETRIEVAL_ERROR,
    CHECKPOINT_ERROR,
    TIMEOUT_ERROR,
    CAPACITY_ERROR,
    SERIALIZATION_ERROR
}

/**
 * Abstract base memory implementation
 */
public abstract class BaseGraphMemory implements GraphMemory {
    
    protected final String name;
    protected final Map<String, Object> config;
    protected volatile boolean initialized = false;
    
    protected BaseGraphMemory(String name) {
        this.name = name;
        this.config = new HashMap<>();
    }
    
    @Override
    public void initialize(Map<String, Object> config) throws MemoryException {
        this.config.putAll(config);
        try {
            doInitialize();
            this.initialized = true;
        } catch (Exception e) {
            throw new MemoryException("Memory initialization failed: " + e.getMessage(), 
                                    MemoryErrorType.STORAGE_ERROR, e);
        }
    }
    
    @Override
    public void cleanup() throws MemoryException {
        try {
            doCleanup();
        } finally {
            this.initialized = false;
        }
    }
    
    protected void checkInitialized() throws MemoryException {
        if (!initialized) {
            throw new MemoryException("Memory system not initialized", MemoryErrorType.STORAGE_ERROR);
        }
    }
    
    protected abstract void doInitialize() throws Exception;
    protected abstract void doCleanup() throws Exception;
    
    public String getName() { return name; }
    public boolean isInitialized() { return initialized; }
}

/**
 * In-memory implementation for development
 */
public class InMemoryGraphMemory extends BaseGraphMemory {
    
    private final ConcurrentHashMap<String, MemoryEntry> entries;
    private final ConcurrentHashMap<String, GraphState> executionStates;
    private final ConcurrentHashMap<String, GraphState> checkpoints;
    private final ConcurrentHashMap<String, List<ExecutionRecord>> executionHistory;
    private final Object statisticsLock = new Object();
    private long accessCount = 0;
    private long hitCount = 0;
    
    public InMemoryGraphMemory(String name) {
        super(name);
        this.entries = new ConcurrentHashMap<>();
        this.executionStates = new ConcurrentHashMap<>();
        this.checkpoints = new ConcurrentHashMap<>();
        this.executionHistory = new ConcurrentHashMap<>();
    }
    
    @Override
    protected void doInitialize() throws Exception {
        // No specific initialization needed for in-memory
    }
    
    @Override
    protected void doCleanup() throws Exception {
        entries.clear();
        executionStates.clear();
        checkpoints.clear();
        executionHistory.clear();
    }
    
    @Override
    public void store(String key, Object value, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        MemoryMetadata metadata = MemoryMetadata.create("store");
        MemoryEntry entry = new MemoryEntry(key, value, metadata, context);
        
        String fullKey = buildKey(key, context);
        entries.put(fullKey, entry);
    }
    
    @Override
    public <T> Optional<T> retrieve(String key, Class<T> type, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        synchronized (statisticsLock) {
            accessCount++;
        }
        
        String fullKey = buildKey(key, context);
        MemoryEntry entry = entries.get(fullKey);
        
        if (entry != null) {
            synchronized (statisticsLock) {
                hitCount++;
            }
            
            // Check if entry is expired
            if (entry.getMetadata().isExpired()) {
                entries.remove(fullKey);
                return Optional.empty();
            }
            
            return entry.getValue(type);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void storeExecutionState(String executionId, GraphState state, MemoryMetadata metadata) 
            throws MemoryException {
        checkInitialized();
        executionStates.put(executionId, state.copy());
    }
    
    @Override
    public Optional<GraphState> retrieveExecutionState(String executionId, MemoryContext context) 
            throws MemoryException {
        checkInitialized();
        
        synchronized (statisticsLock) {
            accessCount++;
        }
        
        GraphState state = executionStates.get(executionId);
        if (state != null) {
            synchronized (statisticsLock) {
                hitCount++;
            }
            return Optional.of(state.copy());
        }
        
        return Optional.empty();
    }
    
    @Override
    public String createCheckpoint(String executionId, GraphState state, String description) 
            throws MemoryException {
        checkInitialized();
        
        String checkpointId = "cp_" + executionId + "_" + System.currentTimeMillis();
        checkpoints.put(checkpointId, state.copy());
        
        return checkpointId;
    }
    
    @Override
    public Optional<GraphState> restoreCheckpoint(String checkpointId, MemoryContext context) 
            throws MemoryException {
        checkInitialized();
        
        synchronized (statisticsLock) {
            accessCount++;
        }
        
        GraphState state = checkpoints.get(checkpointId);
        if (state != null) {
            synchronized (statisticsLock) {
                hitCount++;
            }
            return Optional.of(state.copy());
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<ExecutionRecord> getExecutionHistory(String sessionId, MemoryContext context) 
            throws MemoryException {
        checkInitialized();
        
        return new ArrayList<>(executionHistory.getOrDefault(sessionId, List.of()));
    }
    
    @Override
    public List<MemoryEntry> searchMemory(MemoryQuery query, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        return entries.values().stream()
            .filter(entry -> matchesQuery(entry, query))
            .limit(query.getMaxResults())
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public void clearMemory(MemoryScope scope, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        switch (scope) {
            case EXECUTION:
                entries.entrySet().removeIf(entry -> 
                    entry.getValue().getContext().getExecutionId().equals(context.getExecutionId()));
                break;
            case SESSION:
                entries.entrySet().removeIf(entry -> 
                    entry.getValue().getContext().getSessionId().equals(context.getSessionId()));
                break;
            case USER:
                entries.entrySet().removeIf(entry -> 
                    entry.getValue().getContext().getUserId().equals(context.getUserId()));
                break;
            case WORKFLOW:
                entries.entrySet().removeIf(entry -> 
                    entry.getValue().getContext().getWorkflowId().equals(context.getWorkflowId()));
                break;
            case GLOBAL:
                entries.clear();
                executionStates.clear();
                checkpoints.clear();
                executionHistory.clear();
                break;
        }
    }
    
    @Override
    public MemoryStats getStats() throws MemoryException {
        checkInitialized();
        
        Map<MemoryScope, Long> entriesByScope = new HashMap<>();
        entriesByScope.put(MemoryScope.EXECUTION, (long) entries.size());
        entriesByScope.put(MemoryScope.SESSION, (long) executionStates.size());
        entriesByScope.put(MemoryScope.GLOBAL, (long) checkpoints.size());
        
        double hitRate;
        synchronized (statisticsLock) {
            hitRate = accessCount > 0 ? (double) hitCount / accessCount : 0.0;
        }
        
        long totalSize = entries.values().stream()
            .mapToLong(this::estimateSize)
            .sum();
        
        return new MemoryStats(entries.size(), totalSize, entriesByScope, hitRate, checkpoints.size());
    }
    
    private String buildKey(String key, MemoryContext context) {
        return context.getSessionId() + ":" + context.getExecutionId() + ":" + key;
    }
    
    private boolean matchesQuery(MemoryEntry entry, MemoryQuery query) {
        // Simple pattern matching - in production, use more sophisticated matching
        if (query.getPattern() != null && !entry.getKey().contains(query.getPattern())) {
            return false;
        }
        
        // Time range filtering
        LocalDateTime entryTime = entry.getMetadata().getTimestamp();
        if (query.getFromTime() != null && entryTime.isBefore(query.getFromTime())) {
            return false;
        }
        if (query.getToTime() != null && entryTime.isAfter(query.getToTime())) {
            return false;
        }
        
        return true;
    }
    
    private long estimateSize(MemoryEntry entry) {
        // Rough estimation - in production, use more accurate serialization
        String str = entry.getValue().toString();
        return str.length() * 2; // Assuming UTF-16 encoding
    }
}

/**
 * Database-backed memory implementation
 */
public class DatabaseGraphMemory extends BaseGraphMemory {
    
    private DataSource dataSource;
    private final Object connectionLock = new Object();
    
    public DatabaseGraphMemory(String name) {
        super(name);
    }
    
    @Override
    protected void doInitialize() throws Exception {
        String jdbcUrl = (String) config.get("jdbc_url");
        String username = (String) config.get("username");
        String password = (String) config.get("password");
        
        // Initialize database connection
        this.dataSource = createDataSource(jdbcUrl, username, password);
        
        // Create tables if they don't exist
        initializeTables();
    }
    
    @Override
    protected void doCleanup() throws Exception {
        // Close database connections
        if (dataSource instanceof AutoCloseable) {
            ((AutoCloseable) dataSource).close();
        }
    }
    
    @Override
    public void store(String key, Object value, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO graph_memory (key, value, session_id, execution_id, " +
                 "workflow_id, user_id, created_at, ttl) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, key);
            stmt.setString(2, serializeValue(value));
            stmt.setString(3, context.getSessionId());
            stmt.setString(4, context.getExecutionId());
            stmt.setString(5, context.getWorkflowId());
            stmt.setString(6, context.getUserId());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(8, -1); // No expiration
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to store memory entry: " + e.getMessage(), 
                                    MemoryErrorType.STORAGE_ERROR, e);
        }
    }
    
    @Override
    public <T> Optional<T> retrieve(String key, Class<T> type, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT value FROM graph_memory WHERE key = ? AND session_id = ? AND execution_id = ?")) {
            
            stmt.setString(1, key);
            stmt.setString(2, context.getSessionId());
            stmt.setString(3, context.getExecutionId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String serializedValue = rs.getString("value");
                    Object value = deserializeValue(serializedValue);
                    
                    if (type.isAssignableFrom(value.getClass())) {
                        return Optional.of(type.cast(value));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to retrieve memory entry: " + e.getMessage(), 
                                    MemoryErrorType.RETRIEVAL_ERROR, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void storeExecutionState(String executionId, GraphState state, MemoryMetadata metadata) 
            throws MemoryException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO execution_states (execution_id, state_data, phase, created_at) " +
                 "VALUES (?, ?, ?, ?)")) {
            
            stmt.setString(1, executionId);
            stmt.setString(2, serializeState(state));
            stmt.setString(3, metadata.getExecutionPhase());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to store execution state: " + e.getMessage(), 
                                    MemoryErrorType.STORAGE_ERROR, e);
        }
    }
    
    @Override
    public Optional<GraphState> retrieveExecutionState(String executionId, MemoryContext context) 
            throws MemoryException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT state_data FROM execution_states WHERE execution_id = ?")) {
            
            stmt.setString(1, executionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stateData = rs.getString("state_data");
                    return Optional.of(deserializeState(stateData));
                }
            }
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to retrieve execution state: " + e.getMessage(), 
                                    MemoryErrorType.RETRIEVAL_ERROR, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public String createCheckpoint(String executionId, GraphState state, String description) 
            throws MemoryException {
        checkInitialized();
        
        String checkpointId = "cp_" + executionId + "_" + System.currentTimeMillis();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO checkpoints (checkpoint_id, execution_id, state_data, description, created_at) " +
                 "VALUES (?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, checkpointId);
            stmt.setString(2, executionId);
            stmt.setString(3, serializeState(state));
            stmt.setString(4, description);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to create checkpoint: " + e.getMessage(), 
                                    MemoryErrorType.CHECKPOINT_ERROR, e);
        }
        
        return checkpointId;
    }
    
    @Override
    public Optional<GraphState> restoreCheckpoint(String checkpointId, MemoryContext context) 
            throws MemoryException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT state_data FROM checkpoints WHERE checkpoint_id = ?")) {
            
            stmt.setString(1, checkpointId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stateData = rs.getString("state_data");
                    return Optional.of(deserializeState(stateData));
                }
            }
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to restore checkpoint: " + e.getMessage(), 
                                    MemoryErrorType.CHECKPOINT_ERROR, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<ExecutionRecord> getExecutionHistory(String sessionId, MemoryContext context) 
            throws MemoryException {
        checkInitialized();
        
        List<ExecutionRecord> history = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT execution_id, workflow_id, start_time, end_time, status, results " +
                 "FROM execution_history WHERE session_id = ? ORDER BY start_time DESC")) {
            
            stmt.setString(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExecutionRecord record = new ExecutionRecord(
                        rs.getString("execution_id"),
                        rs.getString("workflow_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null,
                        rs.getString("status"),
                        deserializeResults(rs.getString("results")),
                        List.of() // Checkpoints would be loaded separately
                    );
                    history.add(record);
                }
            }
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to get execution history: " + e.getMessage(), 
                                    MemoryErrorType.RETRIEVAL_ERROR, e);
        }
        
        return history;
    }
    
    @Override
    public List<MemoryEntry> searchMemory(MemoryQuery query, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        List<MemoryEntry> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT key, value, session_id, execution_id, workflow_id, user_id, created_at FROM graph_memory WHERE 1=1");
        
        if (query.getPattern() != null) {
            sql.append(" AND key LIKE ?");
        }
        
        sql.append(" ORDER BY created_at DESC LIMIT ?");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (query.getPattern() != null) {
                stmt.setString(paramIndex++, "%" + query.getPattern() + "%");
            }
            stmt.setInt(paramIndex, query.getMaxResults());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemoryContext entryContext = new MemoryContext(
                        rs.getString("session_id"),
                        rs.getString("execution_id"),
                        rs.getString("workflow_id"),
                        rs.getString("user_id")
                    );
                    
                    MemoryMetadata metadata = MemoryMetadata.create("stored");
                    Object value = deserializeValue(rs.getString("value"));
                    
                    MemoryEntry entry = new MemoryEntry(
                        rs.getString("key"),
                        value,
                        metadata,
                        entryContext
                    );
                    
                    results.add(entry);
                }
            }
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to search memory: " + e.getMessage(), 
                                    MemoryErrorType.RETRIEVAL_ERROR, e);
        }
        
        return results;
    }
    
    @Override
    public void clearMemory(MemoryScope scope, MemoryContext context) throws MemoryException {
        checkInitialized();
        
        String sql = switch (scope) {
            case EXECUTION -> "DELETE FROM graph_memory WHERE execution_id = ?";
            case SESSION -> "DELETE FROM graph_memory WHERE session_id = ?";
            case USER -> "DELETE FROM graph_memory WHERE user_id = ?";
            case WORKFLOW -> "DELETE FROM graph_memory WHERE workflow_id = ?";
            case GLOBAL -> "DELETE FROM graph_memory";
        };
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            switch (scope) {
                case EXECUTION:
                    stmt.setString(1, context.getExecutionId());
                    break;
                case SESSION:
                    stmt.setString(1, context.getSessionId());
                    break;
                case USER:
                    stmt.setString(1, context.getUserId());
                    break;
                case WORKFLOW:
                    stmt.setString(1, context.getWorkflowId());
                    break;
                case GLOBAL:
                    // No parameters needed
                    break;
            }
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new MemoryException("Failed to clear memory: " + e.getMessage(), 
                                    MemoryErrorType.STORAGE_ERROR, e);
        }
    }
    
    @Override
    public MemoryStats getStats() throws MemoryException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) as total_entries, " +
                 "SUM(LENGTH(value)) as total_size, " +
                 "(SELECT COUNT(*) FROM checkpoints) as checkpoint_count " +
                 "FROM graph_memory")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MemoryStats(
                        rs.getLong("total_entries"),
                        rs.getLong("total_size"),
                        Map.of(), // Scope breakdown would require additional queries
                        0.0, // Hit rate tracking would require additional infrastructure
                        rs.getLong("checkpoint_count")
                    );
                }
            }
        } catch (SQLException e) {
            throw new MemoryException("Failed to get memory stats: " + e.getMessage(), 
                                    MemoryErrorType.RETRIEVAL_ERROR, e);
        }
        
        return new MemoryStats(0, 0, Map.of(), 0.0, 0);
    }
    
    // Helper methods for database operations
    private void initializeTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Memory entries table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS graph_memory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    key TEXT NOT NULL,
                    value TEXT NOT NULL,
                    session_id TEXT NOT NULL,
                    execution_id TEXT NOT NULL,
                    workflow_id TEXT NOT NULL,
                    user_id TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    ttl BIGINT,
                    UNIQUE(key, session_id, execution_id)
                )
                """);
            
            // Execution states table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS execution_states (
                    execution_id TEXT PRIMARY KEY,
                    state_data TEXT NOT NULL,
                    phase TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);
            
            // Checkpoints table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS checkpoints (
                    checkpoint_id TEXT PRIMARY KEY,
                    execution_id TEXT NOT NULL,
                    state_data TEXT NOT NULL,
                    description TEXT,
                    created_at TIMESTAMP NOT NULL
                )
                """);
            
            // Execution history table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS execution_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    execution_id TEXT NOT NULL,
                    session_id TEXT NOT NULL,
                    workflow_id TEXT NOT NULL,
                    start_time TIMESTAMP NOT NULL,
                    end_time TIMESTAMP,
                    status TEXT NOT NULL,
                    results TEXT
                )
                """);
        }
    }
    
    private DataSource createDataSource(String jdbcUrl, String username, String password) {
        // In production, use a proper connection pool like HikariCP
        return new MockDataSource(jdbcUrl, username, password);
    }
    
    private String serializeValue(Object value) {
        // Simple JSON serialization - in production, use proper JSON library
        if (value == null) return "null";
        if (value instanceof String) return "\"" + value + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        return "\"" + value.toString() + "\"";
    }
    
    private Object deserializeValue(String serialized) {
        // Simple deserialization - in production, use proper JSON library
        if ("null".equals(serialized)) return null;
        if (serialized.startsWith("\"") && serialized.endsWith("\"")) {
            return serialized.substring(1, serialized.length() - 1);
        }
        try {
            return Integer.parseInt(serialized);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(serialized);
            } catch (NumberFormatException e2) {
                if ("true".equals(serialized) || "false".equals(serialized)) {
                    return Boolean.parseBoolean(serialized);
                }
                return serialized;
            }
        }
    }
    
    private String serializeState(GraphState state) {
        // Serialize graph state to JSON-like format
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        for (String key : state.getKeys()) {
            if (!first) sb.append(",");
            first = false;
            
            Object value = state.get(key, Object.class).orElse(null);
            sb.append("\"").append(key).append("\":").append(serializeValue(value));
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private GraphState deserializeState(String serialized) {
        // Simple state deserialization - in production, use proper JSON library
        GraphState state = new BasicGraphState("restored");
        
        // This is a simplified implementation
        // In production, use a proper JSON parser
        
        return state;
    }
    
    private Map<String, Object> deserializeResults(String serialized) {
        // Simple results deserialization
        if (serialized == null) return Map.of();
        return Map.of("status", "completed"); // Simplified
    }
}
```

## 🚀 Best Practices

1. **Memory Design**
   - Choose appropriate memory types for use cases
   - Implement proper expiration and cleanup
   - Use efficient serialization formats
   - Consider memory capacity limits

2. **Performance**
   - Use memory hierarchies (in-memory + database)
   - Implement proper caching strategies
   - Monitor memory usage patterns
   - Optimize checkpoint frequency

3. **Reliability**
   - Implement backup and recovery
   - Use transactional operations
   - Handle memory corruption gracefully
   - Provide rollback capabilities

4. **Security**
   - Encrypt sensitive memory data
   - Implement access controls
   - Audit memory operations
   - Secure checkpoint storage

## 🔗 Integration with Other Components

Memory integrates with:
- **Graph State**: Memory persistence for state data
- **Nodes**: Memory access within node processing
- **Execution Engine**: Checkpoint and recovery management
- **Persistence**: Long-term storage integration

---

*Next: [Persistence](../07-persistence/) - Learn about production persistence and checkpointing patterns.*
