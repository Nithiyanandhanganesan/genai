# State Management in LangGraph (Java)

## 🎯 Overview
State Management in LangGraph is more sophisticated than traditional chains, providing rich, typed state that flows through graph nodes and can be modified, persisted, and shared across complex workflows. LangGraph's state system enables building stateful applications with branching logic, parallel processing, and human intervention points.

## 🧠 Core State Concepts

### What is LangGraph State?
LangGraph state is a structured data container that:
- **Flows Through Nodes**: State is passed between graph nodes
- **Accumulates Information**: Nodes can modify and enhance state
- **Supports Typing**: Strong type safety for state properties
- **Enables Persistence**: State can be saved and restored
- **Handles Concurrency**: Thread-safe state management

### State Types
1. **Typed State**: Strongly typed state with schema validation
2. **Dynamic State**: Flexible key-value state container
3. **Immutable State**: Read-only state with copy-on-write semantics
4. **Shared State**: State shared across parallel execution paths
5. **Persistent State**: State that survives application restarts

## 🏗️ State Architecture Patterns

### 1. **Linear State Flow**
```
Initial State → Node 1 → Modified State → Node 2 → Final State
```

### 2. **Branching State**
```
State → Decision Node → Branch A State
                    → Branch B State
```

### 3. **Merge State**
```
State A → Process A ↘
                    → Merge Node → Combined State
State B → Process B ↗
```

### 4. **Persistent State**
```
State → Node 1 → Checkpoint → Node 2 → Checkpoint → Final State
         ↓                    ↓
    Persistence         Persistence
```

## 💻 Java State Implementation

### Base State Framework
```java
package com.example.agent.langgraph.state;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Base interface for all LangGraph states
 */
public interface GraphState {
    
    /**
     * Get state property by key
     */
    <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Set state property
     */
    GraphState set(String key, Object value);
    
    /**
     * Update state property using a function
     */
    <T> GraphState update(String key, Class<T> type, Function<T, T> updater);
    
    /**
     * Remove property from state
     */
    GraphState remove(String key);
    
    /**
     * Get all property keys
     */
    Set<String> getKeys();
    
    /**
     * Check if property exists
     */
    boolean containsKey(String key);
    
    /**
     * Create a copy of this state
     */
    GraphState copy();
    
    /**
     * Merge with another state
     */
    GraphState merge(GraphState other);
    
    /**
     * Get state metadata
     */
    StateMetadata getMetadata();
    
    /**
     * Validate state against schema (if applicable)
     */
    StateValidationResult validate();
}

/**
 * State metadata for tracking and debugging
 */
public class StateMetadata {
    private final String stateId;
    private final LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private final String workflowId;
    private final Map<String, Object> debugInfo;
    private final List<StateTransition> transitionHistory;
    
    public StateMetadata(String workflowId) {
        this.stateId = UUID.randomUUID().toString();
        this.workflowId = workflowId;
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.debugInfo = new ConcurrentHashMap<>();
        this.transitionHistory = new ArrayList<>();
    }
    
    public void recordTransition(String fromNode, String toNode, String reason) {
        StateTransition transition = new StateTransition(fromNode, toNode, reason, LocalDateTime.now());
        transitionHistory.add(transition);
        lastModified = LocalDateTime.now();
    }
    
    public void addDebugInfo(String key, Object value) {
        debugInfo.put(key, value);
        lastModified = LocalDateTime.now();
    }
    
    // Getters
    public String getStateId() { return stateId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastModified() { return lastModified; }
    public String getWorkflowId() { return workflowId; }
    public Map<String, Object> getDebugInfo() { return new HashMap<>(debugInfo); }
    public List<StateTransition> getTransitionHistory() { return new ArrayList<>(transitionHistory); }
}

/**
 * State transition record for debugging and auditing
 */
public class StateTransition {
    private final String fromNode;
    private final String toNode;
    private final String reason;
    private final LocalDateTime timestamp;
    
    public StateTransition(String fromNode, String toNode, String reason, LocalDateTime timestamp) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.reason = reason;
        this.timestamp = timestamp;
    }
    
    // Getters
    public String getFromNode() { return fromNode; }
    public String getToNode() { return toNode; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("%s -> %s (%s) at %s", fromNode, toNode, reason, timestamp);
    }
}

/**
 * State validation result
 */
public class StateValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    
    public StateValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = new ArrayList<>(errors);
        this.warnings = new ArrayList<>(warnings);
    }
    
    public static StateValidationResult success() {
        return new StateValidationResult(true, List.of(), List.of());
    }
    
    public static StateValidationResult failure(List<String> errors) {
        return new StateValidationResult(false, errors, List.of());
    }
    
    // Getters
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
}

/**
 * Basic implementation of GraphState
 */
public class BasicGraphState implements GraphState {
    
    private final Map<String, Object> properties;
    private final StateMetadata metadata;
    
    public BasicGraphState(String workflowId) {
        this.properties = new ConcurrentHashMap<>();
        this.metadata = new StateMetadata(workflowId);
    }
    
    private BasicGraphState(Map<String, Object> properties, StateMetadata metadata) {
        this.properties = new ConcurrentHashMap<>(properties);
        this.metadata = metadata;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }
    
    @Override
    public GraphState set(String key, Object value) {
        properties.put(key, value);
        metadata.addDebugInfo("last_set", key + " = " + value);
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> GraphState update(String key, Class<T> type, Function<T, T> updater) {
        Object currentValue = properties.get(key);
        
        if (currentValue != null && type.isAssignableFrom(currentValue.getClass())) {
            T typedValue = (T) currentValue;
            T updatedValue = updater.apply(typedValue);
            properties.put(key, updatedValue);
            metadata.addDebugInfo("last_update", key);
        } else {
            // Apply updater to null if no current value
            T updatedValue = updater.apply(null);
            if (updatedValue != null) {
                properties.put(key, updatedValue);
                metadata.addDebugInfo("last_update", key + " (from null)");
            }
        }
        
        return this;
    }
    
    @Override
    public GraphState remove(String key) {
        Object removed = properties.remove(key);
        if (removed != null) {
            metadata.addDebugInfo("last_remove", key);
        }
        return this;
    }
    
    @Override
    public Set<String> getKeys() {
        return new HashSet<>(properties.keySet());
    }
    
    @Override
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }
    
    @Override
    public GraphState copy() {
        return new BasicGraphState(properties, metadata);
    }
    
    @Override
    public GraphState merge(GraphState other) {
        Map<String, Object> mergedProperties = new HashMap<>(this.properties);
        
        for (String key : other.getKeys()) {
            // Get value from other state
            Object otherValue = other.get(key, Object.class).orElse(null);
            if (otherValue != null) {
                mergedProperties.put(key, otherValue);
            }
        }
        
        BasicGraphState merged = new BasicGraphState(mergedProperties, this.metadata);
        merged.metadata.addDebugInfo("merged_with", other.getMetadata().getStateId());
        
        return merged;
    }
    
    @Override
    public StateMetadata getMetadata() {
        return metadata;
    }
    
    @Override
    public StateValidationResult validate() {
        // Basic validation - check for null values in required fields
        List<String> errors = new ArrayList<>();
        
        if (properties.isEmpty()) {
            errors.add("State is empty");
        }
        
        return errors.isEmpty() ? 
            StateValidationResult.success() : 
            StateValidationResult.failure(errors);
    }
    
    @Override
    public String toString() {
        return String.format("BasicGraphState{properties=%s, stateId=%s}", 
                           properties, metadata.getStateId());
    }
}

/**
 * Typed state with schema validation
 */
public class TypedGraphState implements GraphState {
    
    private final StateSchema schema;
    private final Map<String, Object> properties;
    private final StateMetadata metadata;
    
    public TypedGraphState(StateSchema schema, String workflowId) {
        this.schema = schema;
        this.properties = new ConcurrentHashMap<>();
        this.metadata = new StateMetadata(workflowId);
        
        // Initialize with default values from schema
        initializeDefaults();
    }
    
    private TypedGraphState(StateSchema schema, Map<String, Object> properties, StateMetadata metadata) {
        this.schema = schema;
        this.properties = new ConcurrentHashMap<>(properties);
        this.metadata = metadata;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = properties.get(key);
        
        // Validate type against schema
        if (value != null && schema.isValidType(key, type)) {
            return Optional.of((T) value);
        }
        
        return Optional.empty();
    }
    
    @Override
    public GraphState set(String key, Object value) {
        // Validate against schema
        StateValidationResult validation = schema.validateProperty(key, value);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Schema validation failed for " + key + ": " + validation.getErrors());
        }
        
        properties.put(key, value);
        metadata.addDebugInfo("last_set", key);
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> GraphState update(String key, Class<T> type, Function<T, T> updater) {
        if (!schema.hasProperty(key)) {
            throw new IllegalArgumentException("Unknown property: " + key);
        }
        
        Object currentValue = properties.get(key);
        T typedValue = null;
        
        if (currentValue != null && type.isAssignableFrom(currentValue.getClass())) {
            typedValue = (T) currentValue;
        }
        
        T updatedValue = updater.apply(typedValue);
        
        if (updatedValue != null) {
            set(key, updatedValue); // This will validate against schema
        }
        
        return this;
    }
    
    @Override
    public GraphState remove(String key) {
        if (schema.isRequired(key)) {
            throw new IllegalArgumentException("Cannot remove required property: " + key);
        }
        
        properties.remove(key);
        metadata.addDebugInfo("last_remove", key);
        return this;
    }
    
    @Override
    public Set<String> getKeys() {
        return new HashSet<>(properties.keySet());
    }
    
    @Override
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }
    
    @Override
    public GraphState copy() {
        return new TypedGraphState(schema, properties, metadata);
    }
    
    @Override
    public GraphState merge(GraphState other) {
        Map<String, Object> mergedProperties = new HashMap<>(this.properties);
        
        for (String key : other.getKeys()) {
            if (schema.hasProperty(key)) {
                Object otherValue = other.get(key, Object.class).orElse(null);
                if (otherValue != null) {
                    // Validate before merging
                    StateValidationResult validation = schema.validateProperty(key, otherValue);
                    if (validation.isValid()) {
                        mergedProperties.put(key, otherValue);
                    }
                }
            }
        }
        
        TypedGraphState merged = new TypedGraphState(schema, mergedProperties, this.metadata);
        merged.metadata.addDebugInfo("merged_with", other.getMetadata().getStateId());
        
        return merged;
    }
    
    @Override
    public StateMetadata getMetadata() {
        return metadata;
    }
    
    @Override
    public StateValidationResult validate() {
        return schema.validateState(properties);
    }
    
    private void initializeDefaults() {
        for (PropertyDefinition property : schema.getProperties()) {
            if (property.getDefaultValue() != null) {
                properties.put(property.getName(), property.getDefaultValue());
            }
        }
    }
    
    public StateSchema getSchema() {
        return schema;
    }
}

/**
 * State schema definition
 */
public class StateSchema {
    private final String schemaName;
    private final Map<String, PropertyDefinition> properties;
    
    public StateSchema(String schemaName) {
        this.schemaName = schemaName;
        this.properties = new HashMap<>();
    }
    
    public StateSchema addProperty(PropertyDefinition property) {
        properties.put(property.getName(), property);
        return this;
    }
    
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }
    
    public boolean isRequired(String name) {
        PropertyDefinition prop = properties.get(name);
        return prop != null && prop.isRequired();
    }
    
    public boolean isValidType(String name, Class<?> type) {
        PropertyDefinition prop = properties.get(name);
        return prop != null && prop.getType().isAssignableFrom(type);
    }
    
    public StateValidationResult validateProperty(String name, Object value) {
        PropertyDefinition prop = properties.get(name);
        if (prop == null) {
            return StateValidationResult.failure(List.of("Unknown property: " + name));
        }
        
        return prop.validate(value);
    }
    
    public StateValidationResult validateState(Map<String, Object> stateProperties) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check required properties
        for (PropertyDefinition prop : properties.values()) {
            if (prop.isRequired() && !stateProperties.containsKey(prop.getName())) {
                errors.add("Missing required property: " + prop.getName());
            }
        }
        
        // Validate each property
        for (Map.Entry<String, Object> entry : stateProperties.entrySet()) {
            StateValidationResult propResult = validateProperty(entry.getKey(), entry.getValue());
            if (!propResult.isValid()) {
                errors.addAll(propResult.getErrors());
            }
            warnings.addAll(propResult.getWarnings());
        }
        
        return new StateValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    public Collection<PropertyDefinition> getProperties() {
        return new ArrayList<>(properties.values());
    }
    
    public String getSchemaName() {
        return schemaName;
    }
}

/**
 * Property definition for typed state
 */
public class PropertyDefinition {
    private final String name;
    private final Class<?> type;
    private final boolean required;
    private final Object defaultValue;
    private final List<PropertyValidator> validators;
    private final String description;
    
    public PropertyDefinition(String name, Class<?> type, boolean required) {
        this(name, type, required, null, List.of(), "");
    }
    
    public PropertyDefinition(String name, Class<?> type, boolean required, Object defaultValue,
                             List<PropertyValidator> validators, String description) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.validators = new ArrayList<>(validators);
        this.description = description;
    }
    
    public StateValidationResult validate(Object value) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check null value for required property
        if (value == null) {
            if (required) {
                errors.add("Property " + name + " is required");
            }
            return new StateValidationResult(errors.isEmpty(), errors, warnings);
        }
        
        // Check type compatibility
        if (!type.isAssignableFrom(value.getClass())) {
            errors.add(String.format("Property %s expected %s but got %s", 
                                   name, type.getSimpleName(), value.getClass().getSimpleName()));
            return new StateValidationResult(false, errors, warnings);
        }
        
        // Run custom validators
        for (PropertyValidator validator : validators) {
            PropertyValidationResult result = validator.validate(value);
            if (!result.isValid()) {
                errors.addAll(result.getErrors());
            }
            warnings.addAll(result.getWarnings());
        }
        
        return new StateValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    // Getters
    public String getName() { return name; }
    public Class<?> getType() { return type; }
    public boolean isRequired() { return required; }
    public Object getDefaultValue() { return defaultValue; }
    public List<PropertyValidator> getValidators() { return new ArrayList<>(validators); }
    public String getDescription() { return description; }
}

/**
 * Property validator interface
 */
public interface PropertyValidator {
    PropertyValidationResult validate(Object value);
}

/**
 * Property validation result
 */
public class PropertyValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    
    public PropertyValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = new ArrayList<>(errors);
        this.warnings = new ArrayList<>(warnings);
    }
    
    public static PropertyValidationResult success() {
        return new PropertyValidationResult(true, List.of(), List.of());
    }
    
    public static PropertyValidationResult error(String error) {
        return new PropertyValidationResult(false, List.of(error), List.of());
    }
    
    // Getters
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
}
```

### State Builders and Utilities
```java
/**
 * Fluent builder for creating state schemas
 */
public class StateSchemaBuilder {
    private final String schemaName;
    private final List<PropertyDefinition> properties;
    
    public StateSchemaBuilder(String schemaName) {
        this.schemaName = schemaName;
        this.properties = new ArrayList<>();
    }
    
    public static StateSchemaBuilder create(String schemaName) {
        return new StateSchemaBuilder(schemaName);
    }
    
    public PropertyBuilder property(String name, Class<?> type) {
        return new PropertyBuilder(this, name, type);
    }
    
    public StateSchemaBuilder addProperty(PropertyDefinition property) {
        properties.add(property);
        return this;
    }
    
    public StateSchema build() {
        StateSchema schema = new StateSchema(schemaName);
        for (PropertyDefinition property : properties) {
            schema.addProperty(property);
        }
        return schema;
    }
}

/**
 * Property builder for fluent API
 */
public class PropertyBuilder {
    private final StateSchemaBuilder parent;
    private final String name;
    private final Class<?> type;
    private boolean required = false;
    private Object defaultValue = null;
    private List<PropertyValidator> validators = new ArrayList<>();
    private String description = "";
    
    public PropertyBuilder(StateSchemaBuilder parent, String name, Class<?> type) {
        this.parent = parent;
        this.name = name;
        this.type = type;
    }
    
    public PropertyBuilder required() {
        this.required = true;
        return this;
    }
    
    public PropertyBuilder optional() {
        this.required = false;
        return this;
    }
    
    public PropertyBuilder defaultValue(Object value) {
        this.defaultValue = value;
        return this;
    }
    
    public PropertyBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public PropertyBuilder validator(PropertyValidator validator) {
        this.validators.add(validator);
        return this;
    }
    
    public PropertyBuilder minLength(int minLength) {
        validator(value -> {
            if (value instanceof String && ((String) value).length() < minLength) {
                return PropertyValidationResult.error("String too short, minimum length: " + minLength);
            }
            return PropertyValidationResult.success();
        });
        return this;
    }
    
    public PropertyBuilder maxLength(int maxLength) {
        validator(value -> {
            if (value instanceof String && ((String) value).length() > maxLength) {
                return PropertyValidationResult.error("String too long, maximum length: " + maxLength);
            }
            return PropertyValidationResult.success();
        });
        return this;
    }
    
    public PropertyBuilder range(Number min, Number max) {
        validator(value -> {
            if (value instanceof Number) {
                double num = ((Number) value).doubleValue();
                if (num < min.doubleValue() || num > max.doubleValue()) {
                    return PropertyValidationResult.error(
                        String.format("Value must be between %s and %s", min, max));
                }
            }
            return PropertyValidationResult.success();
        });
        return this;
    }
    
    public StateSchemaBuilder and() {
        PropertyDefinition property = new PropertyDefinition(name, type, required, defaultValue, validators, description);
        return parent.addProperty(property);
    }
}

/**
 * Common state schemas for different workflow types
 */
public class CommonStateSchemas {
    
    /**
     * Document processing workflow schema
     */
    public static StateSchema documentProcessingSchema() {
        return StateSchemaBuilder.create("DocumentProcessing")
            .property("documentId", String.class)
                .required()
                .description("Unique identifier for the document")
                .and()
            .property("documentContent", String.class)
                .required()
                .minLength(10)
                .description("Raw document content")
                .and()
            .property("processingStage", String.class)
                .required()
                .defaultValue("UPLOADED")
                .description("Current processing stage")
                .and()
            .property("analysisResults", Map.class)
                .optional()
                .defaultValue(new HashMap<>())
                .description("Analysis results and metadata")
                .and()
            .property("confidence", Double.class)
                .optional()
                .range(0.0, 1.0)
                .description("Processing confidence score")
                .and()
            .property("errors", List.class)
                .optional()
                .defaultValue(new ArrayList<>())
                .description("List of processing errors")
                .and()
            .property("retryCount", Integer.class)
                .optional()
                .defaultValue(0)
                .range(0, 5)
                .description("Number of retry attempts")
                .and()
            .build();
    }
    
    /**
     * Conversation workflow schema
     */
    public static StateSchema conversationSchema() {
        return StateSchemaBuilder.create("Conversation")
            .property("sessionId", String.class)
                .required()
                .description("Conversation session identifier")
                .and()
            .property("userId", String.class)
                .required()
                .description("User identifier")
                .and()
            .property("currentMessage", String.class)
                .optional()
                .description("Current user message")
                .and()
            .property("conversationHistory", List.class)
                .optional()
                .defaultValue(new ArrayList<>())
                .description("Previous conversation messages")
                .and()
            .property("userContext", Map.class)
                .optional()
                .defaultValue(new HashMap<>())
                .description("User-specific context and preferences")
                .and()
            .property("intent", String.class)
                .optional()
                .description("Detected user intent")
                .and()
            .property("entities", Map.class)
                .optional()
                .defaultValue(new HashMap<>())
                .description("Extracted entities from conversation")
                .and()
            .build();
    }
    
    /**
     * Multi-agent coordination schema
     */
    public static StateSchema multiAgentSchema() {
        return StateSchemaBuilder.create("MultiAgent")
            .property("coordinatorId", String.class)
                .required()
                .description("Coordinator agent identifier")
                .and()
            .property("task", String.class)
                .required()
                .description("Main task to be accomplished")
                .and()
            .property("agentAssignments", Map.class)
                .optional()
                .defaultValue(new HashMap<>())
                .description("Task assignments to agents")
                .and()
            .property("agentResults", Map.class)
                .optional()
                .defaultValue(new HashMap<>())
                .description("Results from individual agents")
                .and()
            .property("coordinationStrategy", String.class)
                .optional()
                .defaultValue("SEQUENTIAL")
                .description("Agent coordination strategy")
                .and()
            .property("overallProgress", Double.class)
                .optional()
                .defaultValue(0.0)
                .range(0.0, 1.0)
                .description("Overall task completion progress")
                .and()
            .build();
    }
}

/**
 * State persistence interface for saving/loading state
 */
public interface StatePersistence {
    
    /**
     * Save state to persistent storage
     */
    void saveState(String workflowId, String checkpointId, GraphState state) throws StatePersistenceException;
    
    /**
     * Load state from persistent storage
     */
    Optional<GraphState> loadState(String workflowId, String checkpointId) throws StatePersistenceException;
    
    /**
     * List available checkpoints for a workflow
     */
    List<StateCheckpoint> listCheckpoints(String workflowId) throws StatePersistenceException;
    
    /**
     * Delete checkpoint
     */
    void deleteCheckpoint(String workflowId, String checkpointId) throws StatePersistenceException;
    
    /**
     * Get latest checkpoint for workflow
     */
    Optional<StateCheckpoint> getLatestCheckpoint(String workflowId) throws StatePersistenceException;
}

/**
 * State checkpoint information
 */
public class StateCheckpoint {
    private final String workflowId;
    private final String checkpointId;
    private final LocalDateTime timestamp;
    private final String nodeName;
    private final Map<String, Object> metadata;
    
    public StateCheckpoint(String workflowId, String checkpointId, LocalDateTime timestamp, 
                          String nodeName, Map<String, Object> metadata) {
        this.workflowId = workflowId;
        this.checkpointId = checkpointId;
        this.timestamp = timestamp;
        this.nodeName = nodeName;
        this.metadata = new HashMap<>(metadata);
    }
    
    // Getters
    public String getWorkflowId() { return workflowId; }
    public String getCheckpointId() { return checkpointId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getNodeName() { return nodeName; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
}

/**
 * State persistence exception
 */
public class StatePersistenceException extends Exception {
    public StatePersistenceException(String message) {
        super(message);
    }
    
    public StatePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Database State Persistence
```java
/**
 * Database-backed state persistence using JPA
 */
@Entity
@Table(name = "workflow_state_checkpoints")
public class StateCheckpointEntity {
    
    @Id
    private String checkpointId;
    
    @Column
    private String workflowId;
    
    @Column
    private String nodeName;
    
    @Column(columnDefinition = "TEXT")
    private String stateData;
    
    @Column(columnDefinition = "TEXT")
    private String stateMetadata;
    
    @Column
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String checkpointMetadata;
    
    // Constructors, getters, and setters
    public StateCheckpointEntity() {}
    
    public StateCheckpointEntity(String checkpointId, String workflowId, String nodeName,
                               String stateData, String stateMetadata, 
                               String checkpointMetadata) {
        this.checkpointId = checkpointId;
        this.workflowId = workflowId;
        this.nodeName = nodeName;
        this.stateData = stateData;
        this.stateMetadata = stateMetadata;
        this.checkpointMetadata = checkpointMetadata;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters...
    public String getCheckpointId() { return checkpointId; }
    public void setCheckpointId(String checkpointId) { this.checkpointId = checkpointId; }
    
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    
    public String getStateData() { return stateData; }
    public void setStateData(String stateData) { this.stateData = stateData; }
    
    public String getStateMetadata() { return stateMetadata; }
    public void setStateMetadata(String stateMetadata) { this.stateMetadata = stateMetadata; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getCheckpointMetadata() { return checkpointMetadata; }
    public void setCheckpointMetadata(String checkpointMetadata) { this.checkpointMetadata = checkpointMetadata; }
}

@Repository
public interface StateCheckpointRepository extends JpaRepository<StateCheckpointEntity, String> {
    
    List<StateCheckpointEntity> findByWorkflowIdOrderByTimestampDesc(String workflowId);
    
    Optional<StateCheckpointEntity> findTopByWorkflowIdOrderByTimestampDesc(String workflowId);
    
    void deleteByWorkflowId(String workflowId);
    
    @Query("SELECT s FROM StateCheckpointEntity s WHERE s.workflowId = :workflowId AND s.timestamp > :since")
    List<StateCheckpointEntity> findRecentCheckpoints(@Param("workflowId") String workflowId, 
                                                     @Param("since") LocalDateTime since);
}

/**
 * JPA-based state persistence implementation
 */
@Service
public class DatabaseStatePersistence implements StatePersistence {
    
    @Autowired
    private StateCheckpointRepository repository;
    
    private final ObjectMapper objectMapper;
    
    public DatabaseStatePersistence() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void saveState(String workflowId, String checkpointId, GraphState state) 
            throws StatePersistenceException {
        try {
            // Serialize state data
            Map<String, Object> stateData = serializeState(state);
            String stateJson = objectMapper.writeValueAsString(stateData);
            
            // Serialize metadata
            String metadataJson = objectMapper.writeValueAsString(state.getMetadata());
            
            // Create checkpoint entity
            StateCheckpointEntity entity = new StateCheckpointEntity(
                checkpointId, workflowId, "unknown", stateJson, metadataJson, "{}"
            );
            
            repository.save(entity);
            
        } catch (Exception e) {
            throw new StatePersistenceException("Failed to save state", e);
        }
    }
    
    @Override
    public Optional<GraphState> loadState(String workflowId, String checkpointId) 
            throws StatePersistenceException {
        try {
            Optional<StateCheckpointEntity> entityOpt = repository.findById(checkpointId);
            
            if (entityOpt.isEmpty()) {
                return Optional.empty();
            }
            
            StateCheckpointEntity entity = entityOpt.get();
            
            // Deserialize state data
            Map<String, Object> stateData = objectMapper.readValue(
                entity.getStateData(), new TypeReference<Map<String, Object>>() {});
            
            // Create new state and populate it
            GraphState state = new BasicGraphState(workflowId);
            for (Map.Entry<String, Object> entry : stateData.entrySet()) {
                state.set(entry.getKey(), entry.getValue());
            }
            
            return Optional.of(state);
            
        } catch (Exception e) {
            throw new StatePersistenceException("Failed to load state", e);
        }
    }
    
    @Override
    public List<StateCheckpoint> listCheckpoints(String workflowId) throws StatePersistenceException {
        try {
            List<StateCheckpointEntity> entities = repository.findByWorkflowIdOrderByTimestampDesc(workflowId);
            
            return entities.stream()
                .map(this::entityToCheckpoint)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            throw new StatePersistenceException("Failed to list checkpoints", e);
        }
    }
    
    @Override
    public void deleteCheckpoint(String workflowId, String checkpointId) 
            throws StatePersistenceException {
        try {
            repository.deleteById(checkpointId);
        } catch (Exception e) {
            throw new StatePersistenceException("Failed to delete checkpoint", e);
        }
    }
    
    @Override
    public Optional<StateCheckpoint> getLatestCheckpoint(String workflowId) 
            throws StatePersistenceException {
        try {
            Optional<StateCheckpointEntity> entityOpt = 
                repository.findTopByWorkflowIdOrderByTimestampDesc(workflowId);
            
            return entityOpt.map(this::entityToCheckpoint);
            
        } catch (Exception e) {
            throw new StatePersistenceException("Failed to get latest checkpoint", e);
        }
    }
    
    private Map<String, Object> serializeState(GraphState state) {
        Map<String, Object> data = new HashMap<>();
        
        for (String key : state.getKeys()) {
            Object value = state.get(key, Object.class).orElse(null);
            data.put(key, value);
        }
        
        return data;
    }
    
    private StateCheckpoint entityToCheckpoint(StateCheckpointEntity entity) {
        try {
            Map<String, Object> metadata = entity.getCheckpointMetadata() != null ?
                objectMapper.readValue(entity.getCheckpointMetadata(), Map.class) : Map.of();
            
            return new StateCheckpoint(
                entity.getWorkflowId(),
                entity.getCheckpointId(),
                entity.getTimestamp(),
                entity.getNodeName(),
                metadata
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert entity to checkpoint", e);
        }
    }
}
```

## 🚀 Best Practices

1. **State Design**
   - Use typed schemas for complex workflows
   - Keep state properties focused and minimal
   - Implement proper validation rules
   - Document state transitions clearly

2. **State Management**
   - Use immutable state where possible
   - Implement proper error handling for state operations
   - Validate state at key transition points
   - Monitor state size and complexity

3. **Persistence**
   - Choose appropriate persistence strategy based on requirements
   - Implement regular checkpoint cleanup
   - Use compression for large state objects
   - Plan for state migration and versioning

4. **Performance**
   - Minimize state copying operations
   - Use lazy loading for large state properties
   - Implement state caching strategies
   - Monitor state serialization/deserialization performance

5. **Debugging**
   - Include comprehensive metadata
   - Track state transition history
   - Implement state visualization tools
   - Provide state inspection utilities

## 🔗 Integration with Other Components

LangGraph State integrates with:
- **Graph Nodes**: Nodes receive and modify state
- **Edge Logic**: Edges use state for routing decisions
- **Persistence**: State can be saved and restored
- **Human-in-Loop**: Human interactions modify state

---

*Next: [Node Implementation](../nodes/) - Learn about creating and managing graph computation nodes.*
