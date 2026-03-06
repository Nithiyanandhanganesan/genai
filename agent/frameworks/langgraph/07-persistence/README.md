# Persistence in LangGraph (Java)

## 🎯 Overview
Persistence in LangGraph provides enterprise-grade state management, workflow durability, and recovery capabilities for production environments. It enables long-running workflows, supports distributed execution, and ensures data integrity across system failures, restarts, and scaling operations.

## 🧠 Core Persistence Concepts

### What is LangGraph Persistence?
LangGraph persistence systems provide:
- **Workflow Durability**: Survive system restarts and failures
- **State Checkpointing**: Save and restore workflow state at any point
- **Distributed Coordination**: Support multi-instance workflow execution
- **Transaction Management**: Ensure atomic operations and consistency
- **Recovery Mechanisms**: Automatic recovery from failures

### Persistence Types
1. **State Persistence**: Graph state storage across executions
2. **Workflow Persistence**: Complete workflow definition storage
3. **Execution Persistence**: Runtime execution tracking
4. **Checkpoint Persistence**: Incremental state snapshots
5. **Event Persistence**: Audit trails and event sourcing
6. **Configuration Persistence**: Dynamic configuration management

## 🏗️ Persistence Architecture Patterns

### 1. **Database-Backed Persistence**
```
Graph Execution → Database → State Storage
                        ↓
                   Recovery/Resume
```

### 2. **Event Sourcing Pattern**
```
Events → Event Store → State Reconstruction
  ↓
Snapshots → Quick Recovery
```

### 3. **Distributed Persistence**
```
Instance A → Shared Storage ← Instance B
              ↓
         Coordination Layer
```

### 4. **Layered Storage**
```
Hot Storage (Redis) → Warm Storage (DB) → Cold Storage (Files)
```

## 💻 Java Persistence Implementation

### Base Persistence Framework
```java
package com.example.agent.langgraph.persistence;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Base interface for LangGraph persistence
 */
public interface GraphPersistence {
    
    /**
     * Persist workflow state
     */
    void persistState(String workflowId, String executionId, GraphState state, 
                     PersistenceMetadata metadata) throws PersistenceException;
    
    /**
     * Restore workflow state
     */
    Optional<PersistedState> restoreState(String workflowId, String executionId) throws PersistenceException;
    
    /**
     * Create workflow checkpoint
     */
    String createCheckpoint(String workflowId, String executionId, GraphState state, 
                           CheckpointMetadata metadata) throws PersistenceException;
    
    /**
     * Restore from checkpoint
     */
    Optional<PersistedState> restoreCheckpoint(String checkpointId) throws PersistenceException;
    
    /**
     * List workflow checkpoints
     */
    List<CheckpointInfo> listCheckpoints(String workflowId, String executionId) throws PersistenceException;
    
    /**
     * Store workflow definition
     */
    void storeWorkflowDefinition(WorkflowDefinition definition) throws PersistenceException;
    
    /**
     * Load workflow definition
     */
    Optional<WorkflowDefinition> loadWorkflowDefinition(String workflowId, String version) 
            throws PersistenceException;
    
    /**
     * Track workflow execution
     */
    void trackExecution(ExecutionRecord record) throws PersistenceException;
    
    /**
     * Get execution history
     */
    List<ExecutionRecord> getExecutionHistory(String workflowId, ExecutionQuery query) 
            throws PersistenceException;
    
    /**
     * Store configuration
     */
    void storeConfiguration(String key, Object value, ConfigurationScope scope) throws PersistenceException;
    
    /**
     * Load configuration
     */
    <T> Optional<T> loadConfiguration(String key, Class<T> type, ConfigurationScope scope) 
            throws PersistenceException;
    
    /**
     * Cleanup old data
     */
    CleanupResult cleanup(CleanupPolicy policy) throws PersistenceException;
    
    /**
     * Get persistence statistics
     */
    PersistenceStats getStats() throws PersistenceException;
    
    /**
     * Initialize persistence system
     */
    void initialize(Map<String, Object> config) throws PersistenceException;
    
    /**
     * Close persistence system
     */
    void close() throws PersistenceException;
}

/**
 * Persistence metadata
 */
public class PersistenceMetadata {
    private final LocalDateTime timestamp;
    private final String phase;
    private final Map<String, Object> tags;
    private final long retentionDays;
    private final PersistenceLevel level;
    
    public PersistenceMetadata(String phase, Map<String, Object> tags, 
                              long retentionDays, PersistenceLevel level) {
        this.timestamp = LocalDateTime.now();
        this.phase = phase;
        this.tags = new HashMap<>(tags);
        this.retentionDays = retentionDays;
        this.level = level;
    }
    
    public static PersistenceMetadata create(String phase) {
        return new PersistenceMetadata(phase, Map.of(), 30, PersistenceLevel.NORMAL);
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPhase() { return phase; }
    public Map<String, Object> getTags() { return new HashMap<>(tags); }
    public long getRetentionDays() { return retentionDays; }
    public PersistenceLevel getLevel() { return level; }
}

/**
 * Persistence levels
 */
public enum PersistenceLevel {
    MINIMAL,    // Essential data only
    NORMAL,     // Standard persistence
    DETAILED,   // Include debug information
    COMPLETE    // Full state and context
}

/**
 * Persisted state container
 */
public class PersistedState {
    private final String workflowId;
    private final String executionId;
    private final GraphState state;
    private final PersistenceMetadata metadata;
    private final String checkpointId;
    
    public PersistedState(String workflowId, String executionId, GraphState state,
                         PersistenceMetadata metadata, String checkpointId) {
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.state = state;
        this.metadata = metadata;
        this.checkpointId = checkpointId;
    }
    
    // Getters
    public String getWorkflowId() { return workflowId; }
    public String getExecutionId() { return executionId; }
    public GraphState getState() { return state; }
    public PersistenceMetadata getMetadata() { return metadata; }
    public String getCheckpointId() { return checkpointId; }
}

/**
 * Checkpoint metadata
 */
public class CheckpointMetadata {
    private final String description;
    private final CheckpointType type;
    private final Map<String, Object> properties;
    private final boolean autoCreated;
    
    public CheckpointMetadata(String description, CheckpointType type, 
                             Map<String, Object> properties, boolean autoCreated) {
        this.description = description;
        this.type = type;
        this.properties = new HashMap<>(properties);
        this.autoCreated = autoCreated;
    }
    
    public static CheckpointMetadata manual(String description) {
        return new CheckpointMetadata(description, CheckpointType.MANUAL, Map.of(), false);
    }
    
    public static CheckpointMetadata automatic(String phase) {
        return new CheckpointMetadata("Auto checkpoint: " + phase, CheckpointType.AUTOMATIC, 
                                    Map.of("phase", phase), true);
    }
    
    // Getters
    public String getDescription() { return description; }
    public CheckpointType getType() { return type; }
    public Map<String, Object> getProperties() { return new HashMap<>(properties); }
    public boolean isAutoCreated() { return autoCreated; }
}

/**
 * Checkpoint types
 */
public enum CheckpointType {
    MANUAL,      // User-created checkpoint
    AUTOMATIC,   // System-created checkpoint
    SCHEDULED,   // Time-based checkpoint
    MILESTONE,   // Important workflow milestone
    ERROR        // Created on error for recovery
}

/**
 * Checkpoint information
 */
public class CheckpointInfo {
    private final String checkpointId;
    private final LocalDateTime createdAt;
    private final CheckpointMetadata metadata;
    private final long sizeBytes;
    
    public CheckpointInfo(String checkpointId, LocalDateTime createdAt, 
                         CheckpointMetadata metadata, long sizeBytes) {
        this.checkpointId = checkpointId;
        this.createdAt = createdAt;
        this.metadata = metadata;
        this.sizeBytes = sizeBytes;
    }
    
    // Getters
    public String getCheckpointId() { return checkpointId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public CheckpointMetadata getMetadata() { return metadata; }
    public long getSizeBytes() { return sizeBytes; }
}

/**
 * Configuration scope
 */
public enum ConfigurationScope {
    GLOBAL,      // System-wide configuration
    WORKFLOW,    // Workflow-specific configuration
    USER,        // User-specific configuration
    SESSION      // Session-specific configuration
}

/**
 * Cleanup policy
 */
public class CleanupPolicy {
    private final Duration retentionPeriod;
    private final int maxCheckpoints;
    private final boolean deleteOrphanedData;
    private final Set<PersistenceLevel> levelsToCleanup;
    
    public CleanupPolicy(Duration retentionPeriod, int maxCheckpoints, 
                        boolean deleteOrphanedData, Set<PersistenceLevel> levelsToCleanup) {
        this.retentionPeriod = retentionPeriod;
        this.maxCheckpoints = maxCheckpoints;
        this.deleteOrphanedData = deleteOrphanedData;
        this.levelsToCleanup = new HashSet<>(levelsToCleanup);
    }
    
    public static CleanupPolicy defaultPolicy() {
        return new CleanupPolicy(Duration.ofDays(30), 10, true, 
                                Set.of(PersistenceLevel.MINIMAL, PersistenceLevel.NORMAL));
    }
    
    // Getters
    public Duration getRetentionPeriod() { return retentionPeriod; }
    public int getMaxCheckpoints() { return maxCheckpoints; }
    public boolean isDeleteOrphanedData() { return deleteOrphanedData; }
    public Set<PersistenceLevel> getLevelsToCleanup() { return new HashSet<>(levelsToCleanup); }
}

/**
 * Cleanup result
 */
public class CleanupResult {
    private final int deletedStates;
    private final int deletedCheckpoints;
    private final long reclaimedBytes;
    private final Duration cleanupTime;
    
    public CleanupResult(int deletedStates, int deletedCheckpoints, 
                        long reclaimedBytes, Duration cleanupTime) {
        this.deletedStates = deletedStates;
        this.deletedCheckpoints = deletedCheckpoints;
        this.reclaimedBytes = reclaimedBytes;
        this.cleanupTime = cleanupTime;
    }
    
    // Getters
    public int getDeletedStates() { return deletedStates; }
    public int getDeletedCheckpoints() { return deletedCheckpoints; }
    public long getReclaimedBytes() { return reclaimedBytes; }
    public Duration getCleanupTime() { return cleanupTime; }
}

/**
 * Persistence statistics
 */
public class PersistenceStats {
    private final long totalStates;
    private final long totalCheckpoints;
    private final long totalSizeBytes;
    private final double compressionRatio;
    private final Map<PersistenceLevel, Long> statesByLevel;
    
    public PersistenceStats(long totalStates, long totalCheckpoints, long totalSizeBytes,
                           double compressionRatio, Map<PersistenceLevel, Long> statesByLevel) {
        this.totalStates = totalStates;
        this.totalCheckpoints = totalCheckpoints;
        this.totalSizeBytes = totalSizeBytes;
        this.compressionRatio = compressionRatio;
        this.statesByLevel = new HashMap<>(statesByLevel);
    }
    
    // Getters
    public long getTotalStates() { return totalStates; }
    public long getTotalCheckpoints() { return totalCheckpoints; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public double getCompressionRatio() { return compressionRatio; }
    public Map<PersistenceLevel, Long> getStatesByLevel() { return new HashMap<>(statesByLevel); }
}

/**
 * Persistence exception
 */
public class PersistenceException extends Exception {
    private final PersistenceErrorType errorType;
    
    public PersistenceException(String message, PersistenceErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
    
    public PersistenceException(String message, PersistenceErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public PersistenceErrorType getErrorType() { return errorType; }
}

/**
 * Persistence error types
 */
public enum PersistenceErrorType {
    STORAGE_ERROR,
    SERIALIZATION_ERROR,
    DESERIALIZATION_ERROR,
    CHECKPOINT_ERROR,
    CONFIGURATION_ERROR,
    CONNECTION_ERROR,
    CONSISTENCY_ERROR
}

/**
 * Abstract base persistence implementation
 */
public abstract class BaseGraphPersistence implements GraphPersistence {
    
    protected final String name;
    protected final Map<String, Object> config;
    protected volatile boolean initialized = false;
    
    protected BaseGraphPersistence(String name) {
        this.name = name;
        this.config = new HashMap<>();
    }
    
    @Override
    public void initialize(Map<String, Object> config) throws PersistenceException {
        this.config.putAll(config);
        try {
            doInitialize();
            this.initialized = true;
        } catch (Exception e) {
            throw new PersistenceException("Persistence initialization failed: " + e.getMessage(), 
                                         PersistenceErrorType.CONFIGURATION_ERROR, e);
        }
    }
    
    @Override
    public void close() throws PersistenceException {
        try {
            doClose();
        } finally {
            this.initialized = false;
        }
    }
    
    protected void checkInitialized() throws PersistenceException {
        if (!initialized) {
            throw new PersistenceException("Persistence system not initialized", 
                                         PersistenceErrorType.CONFIGURATION_ERROR);
        }
    }
    
    protected abstract void doInitialize() throws Exception;
    protected abstract void doClose() throws Exception;
    
    public String getName() { return name; }
    public boolean isInitialized() { return initialized; }
}

/**
 * Database-backed persistence implementation
 */
public class DatabaseGraphPersistence extends BaseGraphPersistence {
    
    private DataSource dataSource;
    private final StateSerializer stateSerializer;
    private final CompressionManager compressionManager;
    
    public DatabaseGraphPersistence(String name) {
        super(name);
        this.stateSerializer = new StateSerializer();
        this.compressionManager = new CompressionManager();
    }
    
    @Override
    protected void doInitialize() throws Exception {
        String jdbcUrl = (String) config.get("jdbc_url");
        String username = (String) config.get("username");
        String password = (String) config.get("password");
        
        this.dataSource = createDataSource(jdbcUrl, username, password);
        initializeTables();
    }
    
    @Override
    protected void doClose() throws Exception {
        if (dataSource instanceof AutoCloseable) {
            ((AutoCloseable) dataSource).close();
        }
    }
    
    @Override
    public void persistState(String workflowId, String executionId, GraphState state, 
                            PersistenceMetadata metadata) throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO workflow_states (workflow_id, execution_id, state_data, " +
                 "phase, persistence_level, tags, created_at, retention_until) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            // Serialize and compress state
            byte[] serializedState = stateSerializer.serialize(state);
            byte[] compressedState = compressionManager.compress(serializedState);
            
            stmt.setString(1, workflowId);
            stmt.setString(2, executionId);
            stmt.setBytes(3, compressedState);
            stmt.setString(4, metadata.getPhase());
            stmt.setString(5, metadata.getLevel().name());
            stmt.setString(6, serializeTags(metadata.getTags()));
            stmt.setTimestamp(7, Timestamp.valueOf(metadata.getTimestamp()));
            stmt.setTimestamp(8, Timestamp.valueOf(metadata.getTimestamp().plusDays(metadata.getRetentionDays())));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to persist state: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
    }
    
    @Override
    public Optional<PersistedState> restoreState(String workflowId, String executionId) 
            throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT state_data, phase, persistence_level, tags, created_at " +
                 "FROM workflow_states WHERE workflow_id = ? AND execution_id = ?")) {
            
            stmt.setString(1, workflowId);
            stmt.setString(2, executionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Decompress and deserialize state
                    byte[] compressedState = rs.getBytes("state_data");
                    byte[] serializedState = compressionManager.decompress(compressedState);
                    GraphState state = stateSerializer.deserialize(serializedState);
                    
                    // Create metadata
                    String phase = rs.getString("phase");
                    PersistenceLevel level = PersistenceLevel.valueOf(rs.getString("persistence_level"));
                    Map<String, Object> tags = deserializeTags(rs.getString("tags"));
                    
                    PersistenceMetadata metadata = new PersistenceMetadata(phase, tags, 30, level);
                    
                    return Optional.of(new PersistedState(workflowId, executionId, state, metadata, null));
                }
            }
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to restore state: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public String createCheckpoint(String workflowId, String executionId, GraphState state, 
                                  CheckpointMetadata metadata) throws PersistenceException {
        checkInitialized();
        
        String checkpointId = "cp_" + workflowId + "_" + executionId + "_" + System.currentTimeMillis();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO workflow_checkpoints (checkpoint_id, workflow_id, execution_id, " +
                 "state_data, description, checkpoint_type, properties, auto_created, created_at) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            // Serialize and compress state
            byte[] serializedState = stateSerializer.serialize(state);
            byte[] compressedState = compressionManager.compress(serializedState);
            
            stmt.setString(1, checkpointId);
            stmt.setString(2, workflowId);
            stmt.setString(3, executionId);
            stmt.setBytes(4, compressedState);
            stmt.setString(5, metadata.getDescription());
            stmt.setString(6, metadata.getType().name());
            stmt.setString(7, serializeTags(metadata.getProperties()));
            stmt.setBoolean(8, metadata.isAutoCreated());
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to create checkpoint: " + e.getMessage(), 
                                         PersistenceErrorType.CHECKPOINT_ERROR, e);
        }
        
        return checkpointId;
    }
    
    @Override
    public Optional<PersistedState> restoreCheckpoint(String checkpointId) throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT workflow_id, execution_id, state_data FROM workflow_checkpoints " +
                 "WHERE checkpoint_id = ?")) {
            
            stmt.setString(1, checkpointId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Decompress and deserialize state
                    byte[] compressedState = rs.getBytes("state_data");
                    byte[] serializedState = compressionManager.decompress(compressedState);
                    GraphState state = stateSerializer.deserialize(serializedState);
                    
                    String workflowId = rs.getString("workflow_id");
                    String executionId = rs.getString("execution_id");
                    
                    PersistenceMetadata metadata = PersistenceMetadata.create("checkpoint_restore");
                    
                    return Optional.of(new PersistedState(workflowId, executionId, state, metadata, checkpointId));
                }
            }
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to restore checkpoint: " + e.getMessage(), 
                                         PersistenceErrorType.CHECKPOINT_ERROR, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<CheckpointInfo> listCheckpoints(String workflowId, String executionId) 
            throws PersistenceException {
        checkInitialized();
        
        List<CheckpointInfo> checkpoints = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT checkpoint_id, description, checkpoint_type, properties, auto_created, " +
                 "created_at, LENGTH(state_data) as size_bytes FROM workflow_checkpoints " +
                 "WHERE workflow_id = ? AND execution_id = ? ORDER BY created_at DESC")) {
            
            stmt.setString(1, workflowId);
            stmt.setString(2, executionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String checkpointId = rs.getString("checkpoint_id");
                    String description = rs.getString("description");
                    CheckpointType type = CheckpointType.valueOf(rs.getString("checkpoint_type"));
                    Map<String, Object> properties = deserializeTags(rs.getString("properties"));
                    boolean autoCreated = rs.getBoolean("auto_created");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    long sizeBytes = rs.getLong("size_bytes");
                    
                    CheckpointMetadata metadata = new CheckpointMetadata(description, type, properties, autoCreated);
                    CheckpointInfo info = new CheckpointInfo(checkpointId, createdAt, metadata, sizeBytes);
                    
                    checkpoints.add(info);
                }
            }
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to list checkpoints: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
        
        return checkpoints;
    }
    
    @Override
    public void storeWorkflowDefinition(WorkflowDefinition definition) throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO workflow_definitions (workflow_id, version, definition_data, " +
                 "created_at, created_by) VALUES (?, ?, ?, ?, ?)")) {
            
            byte[] serializedDefinition = stateSerializer.serialize(definition);
            byte[] compressedDefinition = compressionManager.compress(serializedDefinition);
            
            stmt.setString(1, definition.getWorkflowId());
            stmt.setString(2, definition.getVersion());
            stmt.setBytes(3, compressedDefinition);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(5, definition.getCreatedBy());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to store workflow definition: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
    }
    
    @Override
    public Optional<WorkflowDefinition> loadWorkflowDefinition(String workflowId, String version) 
            throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT definition_data FROM workflow_definitions WHERE workflow_id = ? AND version = ?")) {
            
            stmt.setString(1, workflowId);
            stmt.setString(2, version);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] compressedDefinition = rs.getBytes("definition_data");
                    byte[] serializedDefinition = compressionManager.decompress(compressedDefinition);
                    WorkflowDefinition definition = (WorkflowDefinition) stateSerializer.deserialize(serializedDefinition);
                    
                    return Optional.of(definition);
                }
            }
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load workflow definition: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void trackExecution(ExecutionRecord record) throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO execution_tracking (execution_id, workflow_id, session_id, " +
                 "user_id, status, start_time, end_time, execution_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, record.getExecutionId());
            stmt.setString(2, record.getWorkflowId());
            stmt.setString(3, record.getSessionId());
            stmt.setString(4, record.getUserId());
            stmt.setString(5, record.getStatus());
            stmt.setTimestamp(6, Timestamp.valueOf(record.getStartTime()));
            stmt.setTimestamp(7, record.getEndTime() != null ? Timestamp.valueOf(record.getEndTime()) : null);
            stmt.setString(8, serializeTags(record.getResults()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to track execution: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
    }
    
    @Override
    public List<ExecutionRecord> getExecutionHistory(String workflowId, ExecutionQuery query) 
            throws PersistenceException {
        checkInitialized();
        
        List<ExecutionRecord> history = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT execution_id, workflow_id, session_id, user_id, status, start_time, end_time, execution_data " +
            "FROM execution_tracking WHERE workflow_id = ?");
        
        List<Object> params = new ArrayList<>();
        params.add(workflowId);
        
        if (query.getFromTime() != null) {
            sql.append(" AND start_time >= ?");
            params.add(Timestamp.valueOf(query.getFromTime()));
        }
        
        if (query.getToTime() != null) {
            sql.append(" AND start_time <= ?");
            params.add(Timestamp.valueOf(query.getToTime()));
        }
        
        if (query.getStatus() != null) {
            sql.append(" AND status = ?");
            params.add(query.getStatus());
        }
        
        sql.append(" ORDER BY start_time DESC LIMIT ?");
        params.add(query.getMaxResults());
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExecutionRecord record = new ExecutionRecord(
                        rs.getString("execution_id"),
                        rs.getString("workflow_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null,
                        rs.getString("status"),
                        deserializeTags(rs.getString("execution_data")),
                        List.of() // Checkpoints would be loaded separately
                    );
                    record.setSessionId(rs.getString("session_id"));
                    record.setUserId(rs.getString("user_id"));
                    
                    history.add(record);
                }
            }
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to get execution history: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
        
        return history;
    }
    
    @Override
    public void storeConfiguration(String key, Object value, ConfigurationScope scope) 
            throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO configurations (config_key, config_value, scope, updated_at) " +
                 "VALUES (?, ?, ?, ?)")) {
            
            stmt.setString(1, key);
            stmt.setString(2, stateSerializer.serialize(value).toString());
            stmt.setString(3, scope.name());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to store configuration: " + e.getMessage(), 
                                         PersistenceErrorType.CONFIGURATION_ERROR, e);
        }
    }
    
    @Override
    public <T> Optional<T> loadConfiguration(String key, Class<T> type, ConfigurationScope scope) 
            throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT config_value FROM configurations WHERE config_key = ? AND scope = ?")) {
            
            stmt.setString(1, key);
            stmt.setString(2, scope.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String serializedValue = rs.getString("config_value");
                    Object value = stateSerializer.deserialize(serializedValue.getBytes());
                    
                    if (type.isAssignableFrom(value.getClass())) {
                        return Optional.of(type.cast(value));
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load configuration: " + e.getMessage(), 
                                         PersistenceErrorType.CONFIGURATION_ERROR, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public CleanupResult cleanup(CleanupPolicy policy) throws PersistenceException {
        checkInitialized();
        
        long startTime = System.currentTimeMillis();
        int deletedStates = 0;
        int deletedCheckpoints = 0;
        long reclaimedBytes = 0;
        
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Clean up old states
                LocalDateTime cutoffTime = LocalDateTime.now().minus(policy.getRetentionPeriod());
                
                try (PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM workflow_states WHERE created_at < ? OR retention_until < ?")) {
                    stmt.setTimestamp(1, Timestamp.valueOf(cutoffTime));
                    stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    deletedStates = stmt.executeUpdate();
                }
                
                // Clean up old checkpoints (keep only recent ones)
                try (PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM workflow_checkpoints WHERE checkpoint_id NOT IN (" +
                     "SELECT checkpoint_id FROM workflow_checkpoints ORDER BY created_at DESC LIMIT ?)")) {
                    stmt.setInt(1, policy.getMaxCheckpoints());
                    deletedCheckpoints = stmt.executeUpdate();
                }
                
                // Clean up orphaned data if requested
                if (policy.isDeleteOrphanedData()) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM workflow_checkpoints WHERE workflow_id NOT IN " +
                         "(SELECT DISTINCT workflow_id FROM workflow_states)")) {
                        stmt.executeUpdate();
                    }
                }
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            throw new PersistenceException("Cleanup failed: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
        
        Duration cleanupTime = Duration.ofMillis(System.currentTimeMillis() - startTime);
        
        return new CleanupResult(deletedStates, deletedCheckpoints, reclaimedBytes, cleanupTime);
    }
    
    @Override
    public PersistenceStats getStats() throws PersistenceException {
        checkInitialized();
        
        try (Connection conn = dataSource.getConnection()) {
            long totalStates = 0;
            long totalCheckpoints = 0;
            long totalSizeBytes = 0;
            
            // Get state statistics
            try (PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) as count, SUM(LENGTH(state_data)) as size FROM workflow_states");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalStates = rs.getLong("count");
                    totalSizeBytes += rs.getLong("size");
                }
            }
            
            // Get checkpoint statistics
            try (PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) as count, SUM(LENGTH(state_data)) as size FROM workflow_checkpoints");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalCheckpoints = rs.getLong("count");
                    totalSizeBytes += rs.getLong("size");
                }
            }
            
            // Get states by level
            Map<PersistenceLevel, Long> statesByLevel = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                 "SELECT persistence_level, COUNT(*) as count FROM workflow_states GROUP BY persistence_level");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PersistenceLevel level = PersistenceLevel.valueOf(rs.getString("persistence_level"));
                    statesByLevel.put(level, rs.getLong("count"));
                }
            }
            
            double compressionRatio = 0.7; // Estimated compression ratio
            
            return new PersistenceStats(totalStates, totalCheckpoints, totalSizeBytes, 
                                      compressionRatio, statesByLevel);
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to get persistence stats: " + e.getMessage(), 
                                         PersistenceErrorType.STORAGE_ERROR, e);
        }
    }
    
    // Helper methods
    private void initializeTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Workflow states table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS workflow_states (
                    workflow_id TEXT NOT NULL,
                    execution_id TEXT NOT NULL,
                    state_data BLOB NOT NULL,
                    phase TEXT NOT NULL,
                    persistence_level TEXT NOT NULL,
                    tags TEXT,
                    created_at TIMESTAMP NOT NULL,
                    retention_until TIMESTAMP,
                    PRIMARY KEY (workflow_id, execution_id)
                )
                """);
            
            // Workflow checkpoints table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS workflow_checkpoints (
                    checkpoint_id TEXT PRIMARY KEY,
                    workflow_id TEXT NOT NULL,
                    execution_id TEXT NOT NULL,
                    state_data BLOB NOT NULL,
                    description TEXT,
                    checkpoint_type TEXT NOT NULL,
                    properties TEXT,
                    auto_created BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);
            
            // Workflow definitions table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS workflow_definitions (
                    workflow_id TEXT NOT NULL,
                    version TEXT NOT NULL,
                    definition_data BLOB NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    created_by TEXT,
                    PRIMARY KEY (workflow_id, version)
                )
                """);
            
            // Execution tracking table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS execution_tracking (
                    execution_id TEXT PRIMARY KEY,
                    workflow_id TEXT NOT NULL,
                    session_id TEXT,
                    user_id TEXT,
                    status TEXT NOT NULL,
                    start_time TIMESTAMP NOT NULL,
                    end_time TIMESTAMP,
                    execution_data TEXT
                )
                """);
            
            // Configurations table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS configurations (
                    config_key TEXT NOT NULL,
                    config_value TEXT NOT NULL,
                    scope TEXT NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    PRIMARY KEY (config_key, scope)
                )
                """);
            
            // Create indexes
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_workflow_states_created ON workflow_states(created_at)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_checkpoints_workflow ON workflow_checkpoints(workflow_id, execution_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_execution_tracking_workflow ON execution_tracking(workflow_id, start_time)");
        }
    }
    
    private DataSource createDataSource(String jdbcUrl, String username, String password) {
        return new MockDataSource(jdbcUrl, username, password);
    }
    
    private String serializeTags(Map<String, Object> tags) {
        // Simple JSON-like serialization
        if (tags.isEmpty()) return "{}";
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
    
    private Map<String, Object> deserializeTags(String serialized) {
        if (serialized == null || serialized.equals("{}")) return Map.of();
        
        // Simple deserialization - in production, use proper JSON library
        Map<String, Object> result = new HashMap<>();
        result.put("simplified", "true");
        return result;
    }
}

/**
 * Supporting classes
 */

class StateSerializer {
    public byte[] serialize(Object object) {
        // Simple serialization - in production, use proper serialization
        return object.toString().getBytes();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data) {
        // Simple deserialization - in production, use proper deserialization
        return (T) new String(data);
    }
}

class CompressionManager {
    public byte[] compress(byte[] data) {
        // Simple compression - in production, use proper compression like gzip
        return data;
    }
    
    public byte[] decompress(byte[] data) {
        // Simple decompression
        return data;
    }
}

class WorkflowDefinition {
    private final String workflowId;
    private final String version;
    private final String createdBy;
    
    public WorkflowDefinition(String workflowId, String version, String createdBy) {
        this.workflowId = workflowId;
        this.version = version;
        this.createdBy = createdBy;
    }
    
    // Getters
    public String getWorkflowId() { return workflowId; }
    public String getVersion() { return version; }
    public String getCreatedBy() { return createdBy; }
}

class ExecutionQuery {
    private final LocalDateTime fromTime;
    private final LocalDateTime toTime;
    private final String status;
    private final int maxResults;
    
    public ExecutionQuery(LocalDateTime fromTime, LocalDateTime toTime, String status, int maxResults) {
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.status = status;
        this.maxResults = maxResults;
    }
    
    public static ExecutionQuery recent(int maxResults) {
        return new ExecutionQuery(null, null, null, maxResults);
    }
    
    // Getters
    public LocalDateTime getFromTime() { return fromTime; }
    public LocalDateTime getToTime() { return toTime; }
    public String getStatus() { return status; }
    public int getMaxResults() { return maxResults; }
}
```

## 🚀 Best Practices

1. **Storage Design**
   - Use appropriate database schema design
   - Implement proper indexing strategies
   - Consider data compression for large states
   - Plan for data retention and archival

2. **Performance**
   - Use connection pooling for databases
   - Implement efficient serialization
   - Use caching for frequently accessed data
   - Monitor storage performance metrics

3. **Reliability**
   - Implement transactional operations
   - Use backup and recovery strategies
   - Handle partial failures gracefully
   - Test recovery scenarios regularly

4. **Scalability**
   - Design for horizontal scaling
   - Use sharding for large datasets
   - Implement proper cleanup policies
   - Monitor storage growth patterns

## 🔗 Integration with Other Components

Persistence integrates with:
- **Memory Systems**: Long-term storage backing
- **Graph Execution**: State checkpointing and recovery
- **Monitoring**: Audit trails and execution history
- **Configuration**: Dynamic configuration management

---

*Next: [Human-in-the-Loop](../08-human-loop/) - Learn about interactive workflow patterns and human intervention points.*
