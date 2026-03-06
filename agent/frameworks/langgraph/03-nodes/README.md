# Graph Nodes in LangGraph (Java)

## 🎯 Overview
Nodes in LangGraph are the fundamental computational units that process and transform state as it flows through the graph. Unlike traditional function calls, LangGraph nodes are designed to work with rich state objects, support conditional execution, and can interact with external systems while maintaining full observability and control flow.

## 🧠 Core Node Concepts

### What are LangGraph Nodes?
LangGraph nodes are stateful processing units that:
- **Process Graph State**: Receive and modify rich state objects
- **Support Async Operations**: Handle long-running or I/O-bound tasks
- **Enable Conditional Logic**: Make decisions based on state content
- **Provide Observability**: Track execution, timing, and outcomes
- **Handle Errors Gracefully**: Implement robust error handling and recovery

### Node Types
1. **Processing Nodes**: Transform data and state
2. **Decision Nodes**: Route execution based on conditions
3. **I/O Nodes**: Interface with external systems
4. **Human-in-Loop Nodes**: Require human intervention
5. **Parallel Nodes**: Execute multiple operations concurrently
6. **Checkpoint Nodes**: Save state for persistence and recovery

## 🏗️ Node Architecture Patterns

### 1. **Linear Processing**
```
State → Process Node → Modified State → Next Node
```

### 2. **Conditional Routing**
```
State → Decision Node → Condition A → Node A
                     → Condition B → Node B
```

### 3. **Fan-Out/Fan-In**
```
State → Split → Node A ↘
              → Node B → Merge → Combined State
              → Node C ↗
```

### 4. **Human Intervention**
```
State → Process → Human Node → Wait → Continue → Final State
```

## 💻 Java Node Implementation

### Base Node Framework
```java
package com.example.agent.langgraph.nodes;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Base interface for all LangGraph nodes
 */
public interface GraphNode {
    
    /**
     * Execute the node with given state
     */
    NodeResult execute(GraphState state, NodeContext context) throws NodeExecutionException;
    
    /**
     * Execute asynchronously
     */
    default CompletableFuture<NodeResult> executeAsync(GraphState state, NodeContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(state, context);
            } catch (NodeExecutionException e) {
                return NodeResult.error(getName(), e.getMessage());
            }
        });
    }
    
    /**
     * Get node identifier
     */
    String getName();
    
    /**
     * Get node description
     */
    String getDescription();
    
    /**
     * Get node type for categorization
     */
    NodeType getType();
    
    /**
     * Validate that the node can process the given state
     */
    ValidationResult validateState(GraphState state);
    
    /**
     * Get expected input state schema
     */
    Set<String> getRequiredInputs();
    
    /**
     * Get output state modifications
     */
    Set<String> getOutputs();
    
    /**
     * Get node configuration
     */
    NodeConfiguration getConfiguration();
    
    /**
     * Initialize node with configuration
     */
    void initialize(Map<String, Object> config) throws NodeExecutionException;
    
    /**
     * Cleanup node resources
     */
    void cleanup();
}

/**
 * Node types for categorization
 */
public enum NodeType {
    PROCESSING,     // Data transformation nodes
    DECISION,       // Conditional routing nodes
    IO,            // Input/output operations
    HUMAN_LOOP,    // Human intervention required
    PARALLEL,      // Concurrent execution
    CHECKPOINT,    // State persistence
    AGGREGATE,     // Combine multiple inputs
    TRANSFORM,     // State transformation
    VALIDATION     // State validation
}

/**
 * Node execution result
 */
public class NodeResult {
    private final boolean success;
    private final String nodeName;
    private final GraphState outputState;
    private final String errorMessage;
    private final Map<String, Object> metadata;
    private final Duration executionTime;
    private final LocalDateTime timestamp;
    
    private NodeResult(boolean success, String nodeName, GraphState outputState,
                      String errorMessage, Map<String, Object> metadata,
                      Duration executionTime) {
        this.success = success;
        this.nodeName = nodeName;
        this.outputState = outputState;
        this.errorMessage = errorMessage;
        this.metadata = new HashMap<>(metadata != null ? metadata : Map.of());
        this.executionTime = executionTime;
        this.timestamp = LocalDateTime.now();
    }
    
    public static NodeResult success(String nodeName, GraphState outputState, Duration executionTime) {
        return new NodeResult(true, nodeName, outputState, null, Map.of(), executionTime);
    }
    
    public static NodeResult success(String nodeName, GraphState outputState, 
                                   Duration executionTime, Map<String, Object> metadata) {
        return new NodeResult(true, nodeName, outputState, null, metadata, executionTime);
    }
    
    public static NodeResult error(String nodeName, String errorMessage) {
        return new NodeResult(false, nodeName, null, errorMessage, Map.of(), Duration.ZERO);
    }
    
    public static NodeResult error(String nodeName, String errorMessage, GraphState partialState) {
        return new NodeResult(false, nodeName, partialState, errorMessage, Map.of(), Duration.ZERO);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getNodeName() { return nodeName; }
    public GraphState getOutputState() { return outputState; }
    public String getErrorMessage() { return errorMessage; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public Duration getExecutionTime() { return executionTime; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * Node execution context
 */
public class NodeContext {
    private final String executionId;
    private final String workflowId;
    private final Map<String, Object> variables;
    private final NodeLogger logger;
    private final ExecutorService executorService;
    private final Duration timeout;
    
    public NodeContext(String executionId, String workflowId) {
        this(executionId, workflowId, Map.of(), Duration.ofMinutes(5));
    }
    
    public NodeContext(String executionId, String workflowId, 
                      Map<String, Object> variables, Duration timeout) {
        this.executionId = executionId;
        this.workflowId = workflowId;
        this.variables = new HashMap<>(variables);
        this.logger = new NodeLogger(workflowId, executionId);
        this.executorService = ForkJoinPool.commonPool();
        this.timeout = timeout;
    }
    
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getVariable(String key, Class<T> type) {
        Object value = variables.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }
    
    // Getters
    public String getExecutionId() { return executionId; }
    public String getWorkflowId() { return workflowId; }
    public Map<String, Object> getVariables() { return new HashMap<>(variables); }
    public NodeLogger getLogger() { return logger; }
    public ExecutorService getExecutorService() { return executorService; }
    public Duration getTimeout() { return timeout; }
}

/**
 * Node execution exception
 */
public class NodeExecutionException extends Exception {
    private final String nodeName;
    private final NodeErrorType errorType;
    
    public NodeExecutionException(String nodeName, String message) {
        this(nodeName, message, NodeErrorType.EXECUTION_ERROR);
    }
    
    public NodeExecutionException(String nodeName, String message, NodeErrorType errorType) {
        super(message);
        this.nodeName = nodeName;
        this.errorType = errorType;
    }
    
    public NodeExecutionException(String nodeName, String message, Throwable cause) {
        super(message, cause);
        this.nodeName = nodeName;
        this.errorType = NodeErrorType.EXECUTION_ERROR;
    }
    
    public String getNodeName() { return nodeName; }
    public NodeErrorType getErrorType() { return errorType; }
}

/**
 * Node error types
 */
public enum NodeErrorType {
    VALIDATION_ERROR,
    EXECUTION_ERROR,
    TIMEOUT_ERROR,
    CONFIGURATION_ERROR,
    STATE_ERROR,
    EXTERNAL_SERVICE_ERROR
}

/**
 * Abstract base node implementation
 */
public abstract class BaseGraphNode implements GraphNode {
    
    protected final String name;
    protected final String description;
    protected final NodeType type;
    protected final NodeConfiguration configuration;
    protected boolean initialized = false;
    
    protected BaseGraphNode(String name, String description, NodeType type) {
        this(name, description, type, new NodeConfiguration());
    }
    
    protected BaseGraphNode(String name, String description, NodeType type, 
                           NodeConfiguration configuration) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.configuration = configuration;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public NodeType getType() {
        return type;
    }
    
    @Override
    public NodeConfiguration getConfiguration() {
        return configuration;
    }
    
    @Override
    public void initialize(Map<String, Object> config) throws NodeExecutionException {
        try {
            doInitialize(config);
            this.initialized = true;
        } catch (Exception e) {
            throw new NodeExecutionException(name, "Initialization failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void cleanup() {
        try {
            doCleanup();
        } finally {
            this.initialized = false;
        }
    }
    
    @Override
    public NodeResult execute(GraphState state, NodeContext context) throws NodeExecutionException {
        if (!initialized) {
            throw new NodeExecutionException(name, "Node not initialized");
        }
        
        long startTime = System.nanoTime();
        context.getLogger().logNodeStart(name, state);
        
        try {
            // Validate input state
            ValidationResult validation = validateState(state);
            if (!validation.isValid()) {
                throw new NodeExecutionException(name, "State validation failed: " + 
                    String.join(", ", validation.getErrors()), NodeErrorType.VALIDATION_ERROR);
            }
            
            // Execute with timeout
            CompletableFuture<GraphState> future = CompletableFuture.supplyAsync(
                () -> executeInternal(state, context), context.getExecutorService());
            
            GraphState result = future.get(context.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            
            Duration executionTime = Duration.ofNanos(System.nanoTime() - startTime);
            context.getLogger().logNodeSuccess(name, result, executionTime);
            
            Map<String, Object> metadata = createExecutionMetadata(state, result, executionTime, context);
            
            return NodeResult.success(name, result, executionTime, metadata);
            
        } catch (TimeoutException e) {
            Duration executionTime = Duration.ofNanos(System.nanoTime() - startTime);
            context.getLogger().logNodeError(name, "Timeout", executionTime);
            throw new NodeExecutionException(name, "Node execution timeout", NodeErrorType.TIMEOUT_ERROR);
            
        } catch (Exception e) {
            Duration executionTime = Duration.ofNanos(System.nanoTime() - startTime);
            context.getLogger().logNodeError(name, e.getMessage(), executionTime);
            
            if (e instanceof NodeExecutionException) {
                throw e;
            } else {
                throw new NodeExecutionException(name, "Node execution failed: " + e.getMessage(), e);
            }
        }
    }
    
    @Override
    public ValidationResult validateState(GraphState state) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check required inputs
        for (String requiredInput : getRequiredInputs()) {
            if (!state.containsKey(requiredInput)) {
                errors.add("Missing required input: " + requiredInput);
            }
        }
        
        // Validate state schema if available
        StateValidationResult stateValidation = state.validate();
        if (!stateValidation.isValid()) {
            errors.addAll(stateValidation.getErrors());
        }
        warnings.addAll(stateValidation.getWarnings());
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Subclasses implement the core node logic here
     */
    protected abstract GraphState executeInternal(GraphState state, NodeContext context);
    
    /**
     * Subclasses override for custom initialization
     */
    protected void doInitialize(Map<String, Object> config) throws Exception {
        // Default: no-op
    }
    
    /**
     * Subclasses override for cleanup
     */
    protected void doCleanup() {
        // Default: no-op
    }
    
    protected Map<String, Object> createExecutionMetadata(GraphState inputState, GraphState outputState,
                                                         Duration executionTime, NodeContext context) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("execution_time_ms", executionTime.toMillis());
        metadata.put("input_keys", inputState.getKeys());
        metadata.put("output_keys", outputState.getKeys());
        metadata.put("node_type", type.name());
        metadata.put("execution_id", context.getExecutionId());
        metadata.put("workflow_id", context.getWorkflowId());
        return metadata;
    }
}

/**
 * Node configuration
 */
public class NodeConfiguration {
    private final Map<String, Object> properties;
    private final Duration defaultTimeout;
    private final int maxRetries;
    private final boolean enableLogging;
    
    public NodeConfiguration() {
        this.properties = new HashMap<>();
        this.defaultTimeout = Duration.ofMinutes(5);
        this.maxRetries = 3;
        this.enableLogging = true;
    }
    
    public NodeConfiguration(Map<String, Object> properties, Duration defaultTimeout,
                           int maxRetries, boolean enableLogging) {
        this.properties = new HashMap<>(properties);
        this.defaultTimeout = defaultTimeout;
        this.maxRetries = maxRetries;
        this.enableLogging = enableLogging;
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        Object value = properties.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return defaultValue;
    }
    
    // Getters
    public Map<String, Object> getProperties() { return new HashMap<>(properties); }
    public Duration getDefaultTimeout() { return defaultTimeout; }
    public int getMaxRetries() { return maxRetries; }
    public boolean isLoggingEnabled() { return enableLogging; }
}

/**
 * Validation result
 */
public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    
    public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = new ArrayList<>(errors);
        this.warnings = new ArrayList<>(warnings);
    }
    
    public static ValidationResult success() {
        return new ValidationResult(true, List.of(), List.of());
    }
    
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors, List.of());
    }
    
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
}

/**
 * Node logging utility
 */
public class NodeLogger {
    private final String workflowId;
    private final String executionId;
    
    public NodeLogger(String workflowId, String executionId) {
        this.workflowId = workflowId;
        this.executionId = executionId;
    }
    
    public void logNodeStart(String nodeName, GraphState state) {
        log("INFO", "Node %s started with state keys: %s", nodeName, state.getKeys());
    }
    
    public void logNodeSuccess(String nodeName, GraphState state, Duration duration) {
        log("INFO", "Node %s completed successfully in %dms, output keys: %s", 
            nodeName, duration.toMillis(), state.getKeys());
    }
    
    public void logNodeError(String nodeName, String error, Duration duration) {
        log("ERROR", "Node %s failed after %dms: %s", nodeName, duration.toMillis(), error);
    }
    
    public void logNodeInfo(String nodeName, String message) {
        log("INFO", "Node %s: %s", nodeName, message);
    }
    
    public void logNodeWarning(String nodeName, String message) {
        log("WARN", "Node %s: %s", nodeName, message);
    }
    
    private void log(String level, String format, Object... args) {
        String message = String.format(format, args);
        System.out.printf("[%s] [%s] [%s] %s%n", 
                         LocalDateTime.now(), level, workflowId + "/" + executionId, message);
    }
}
```

### Processing Nodes Implementation
```java
/**
 * LLM processing node for AI-powered transformations
 */
public class LLMProcessingNode extends BaseGraphNode {
    
    private final ChatLanguageModel llm;
    private final PromptTemplate promptTemplate;
    
    public LLMProcessingNode(String name, ChatLanguageModel llm, PromptTemplate promptTemplate) {
        super(name, "LLM-powered processing node", NodeType.PROCESSING);
        this.llm = llm;
        this.promptTemplate = promptTemplate;
    }
    
    @Override
    protected GraphState executeInternal(GraphState state, NodeContext context) {
        try {
            // Extract variables for prompt template
            Map<String, Object> promptVariables = new HashMap<>();
            for (String key : state.getKeys()) {
                promptVariables.put(key, state.get(key, Object.class).orElse(null));
            }
            
            // Format prompt
            String prompt = promptTemplate.format(promptVariables);
            context.getLogger().logNodeInfo(getName(), "Generated prompt with " + prompt.length() + " characters");
            
            // Generate LLM response
            AiMessage response = llm.generate(UserMessage.from(prompt)).content();
            
            // Update state with response
            GraphState outputState = state.copy();
            outputState.set("llm_response", response.text());
            outputState.set("llm_timestamp", LocalDateTime.now());
            outputState.set("prompt_used", prompt);
            
            return outputState;
            
        } catch (Exception e) {
            throw new RuntimeException("LLM processing failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Set<String> getRequiredInputs() {
        return promptTemplate.getRequiredVariables();
    }
    
    @Override
    public Set<String> getOutputs() {
        return Set.of("llm_response", "llm_timestamp", "prompt_used");
    }
}

/**
 * Data transformation node for structured processing
 */
public class DataTransformNode extends BaseGraphNode {
    
    private final Function<GraphState, GraphState> transformer;
    private final Set<String> requiredInputs;
    private final Set<String> outputs;
    
    public DataTransformNode(String name, Function<GraphState, GraphState> transformer,
                           Set<String> requiredInputs, Set<String> outputs) {
        super(name, "Data transformation node", NodeType.TRANSFORM);
        this.transformer = transformer;
        this.requiredInputs = new HashSet<>(requiredInputs);
        this.outputs = new HashSet<>(outputs);
    }
    
    @Override
    protected GraphState executeInternal(GraphState state, NodeContext context) {
        try {
            context.getLogger().logNodeInfo(getName(), "Transforming state with " + state.getKeys().size() + " properties");
            
            GraphState result = transformer.apply(state);
            
            context.getLogger().logNodeInfo(getName(), "Transformation complete, output has " + 
                                           result.getKeys().size() + " properties");
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Data transformation failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Set<String> getRequiredInputs() {
        return new HashSet<>(requiredInputs);
    }
    
    @Override
    public Set<String> getOutputs() {
        return new HashSet<>(outputs);
    }
}

/**
 * Validation node for state verification
 */
public class ValidationNode extends BaseGraphNode {
    
    private final List<StateValidator> validators;
    
    public ValidationNode(String name, List<StateValidator> validators) {
        super(name, "State validation node", NodeType.VALIDATION);
        this.validators = new ArrayList<>(validators);
    }
    
    @Override
    protected GraphState executeInternal(GraphState state, NodeContext context) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        context.getLogger().logNodeInfo(getName(), "Running " + validators.size() + " validators");
        
        for (StateValidator validator : validators) {
            try {
                ValidationResult result = validator.validate(state);
                
                if (!result.isValid()) {
                    errors.addAll(result.getErrors());
                }
                warnings.addAll(result.getWarnings());
                
            } catch (Exception e) {
                errors.add("Validator error: " + e.getMessage());
            }
        }
        
        GraphState outputState = state.copy();
        outputState.set("validation_errors", errors);
        outputState.set("validation_warnings", warnings);
        outputState.set("validation_passed", errors.isEmpty());
        outputState.set("validation_timestamp", LocalDateTime.now());
        
        if (!errors.isEmpty()) {
            context.getLogger().logNodeWarning(getName(), "Validation failed with " + errors.size() + " errors");
        } else {
            context.getLogger().logNodeInfo(getName(), "Validation passed");
        }
        
        return outputState;
    }
    
    @Override
    public Set<String> getRequiredInputs() {
        return Set.of(); // Validation can work on any state
    }
    
    @Override
    public Set<String> getOutputs() {
        return Set.of("validation_errors", "validation_warnings", "validation_passed", "validation_timestamp");
    }
}

/**
 * State validator interface
 */
public interface StateValidator {
    ValidationResult validate(GraphState state);
    String getValidatorName();
}

/**
 * Common state validators
 */
public class CommonValidators {
    
    public static StateValidator requiredFields(String... fields) {
        return new StateValidator() {
            @Override
            public ValidationResult validate(GraphState state) {
                List<String> missing = new ArrayList<>();
                for (String field : fields) {
                    if (!state.containsKey(field)) {
                        missing.add(field);
                    }
                }
                
                if (missing.isEmpty()) {
                    return ValidationResult.success();
                } else {
                    return ValidationResult.failure(
                        List.of("Missing required fields: " + String.join(", ", missing)));
                }
            }
            
            @Override
            public String getValidatorName() {
                return "RequiredFields(" + String.join(", ", fields) + ")";
            }
        };
    }
    
    public static StateValidator typeCheck(String field, Class<?> expectedType) {
        return new StateValidator() {
            @Override
            public ValidationResult validate(GraphState state) {
                Optional<?> value = state.get(field, Object.class);
                
                if (value.isEmpty()) {
                    return ValidationResult.success(); // Field not present, that's OK for type check
                }
                
                if (!expectedType.isAssignableFrom(value.get().getClass())) {
                    return ValidationResult.failure(
                        List.of(String.format("Field %s expected %s but got %s", 
                                             field, expectedType.getSimpleName(), 
                                             value.get().getClass().getSimpleName())));
                }
                
                return ValidationResult.success();
            }
            
            @Override
            public String getValidatorName() {
                return "TypeCheck(" + field + " -> " + expectedType.getSimpleName() + ")";
            }
        };
    }
    
    public static StateValidator valueRange(String field, double min, double max) {
        return new StateValidator() {
            @Override
            public ValidationResult validate(GraphState state) {
                Optional<Number> value = state.get(field, Number.class);
                
                if (value.isEmpty()) {
                    return ValidationResult.success(); // Field not present
                }
                
                double numValue = value.get().doubleValue();
                if (numValue < min || numValue > max) {
                    return ValidationResult.failure(
                        List.of(String.format("Field %s value %.2f is outside range [%.2f, %.2f]", 
                                             field, numValue, min, max)));
                }
                
                return ValidationResult.success();
            }
            
            @Override
            public String getValidatorName() {
                return "ValueRange(" + field + " in [" + min + ", " + max + "])";
            }
        };
    }
}

/**
 * Aggregation node for combining data
 */
public class AggregationNode extends BaseGraphNode {
    
    private final AggregationStrategy strategy;
    private final String outputKey;
    
    public AggregationNode(String name, AggregationStrategy strategy, String outputKey) {
        super(name, "Data aggregation node", NodeType.AGGREGATE);
        this.strategy = strategy;
        this.outputKey = outputKey;
    }
    
    @Override
    protected GraphState executeInternal(GraphState state, NodeContext context) {
        try {
            context.getLogger().logNodeInfo(getName(), "Aggregating data using " + strategy.getStrategyName());
            
            Object aggregatedValue = strategy.aggregate(state);
            
            GraphState outputState = state.copy();
            outputState.set(outputKey, aggregatedValue);
            outputState.set("aggregation_timestamp", LocalDateTime.now());
            outputState.set("aggregation_strategy", strategy.getStrategyName());
            
            context.getLogger().logNodeInfo(getName(), "Aggregation complete, result: " + aggregatedValue);
            
            return outputState;
            
        } catch (Exception e) {
            throw new RuntimeException("Aggregation failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Set<String> getRequiredInputs() {
        return strategy.getRequiredInputs();
    }
    
    @Override
    public Set<String> getOutputs() {
        return Set.of(outputKey, "aggregation_timestamp", "aggregation_strategy");
    }
}

/**
 * Aggregation strategy interface
 */
public interface AggregationStrategy {
    Object aggregate(GraphState state);
    String getStrategyName();
    Set<String> getRequiredInputs();
}

/**
 * Common aggregation strategies
 */
public class AggregationStrategies {
    
    public static AggregationStrategy sum(String... fields) {
        return new AggregationStrategy() {
            @Override
            public Object aggregate(GraphState state) {
                double total = 0.0;
                int count = 0;
                
                for (String field : fields) {
                    Optional<Number> value = state.get(field, Number.class);
                    if (value.isPresent()) {
                        total += value.get().doubleValue();
                        count++;
                    }
                }
                
                return count > 0 ? total : 0.0;
            }
            
            @Override
            public String getStrategyName() {
                return "Sum(" + String.join(", ", fields) + ")";
            }
            
            @Override
            public Set<String> getRequiredInputs() {
                return Set.of(fields);
            }
        };
    }
    
    public static AggregationStrategy average(String... fields) {
        return new AggregationStrategy() {
            @Override
            public Object aggregate(GraphState state) {
                double total = 0.0;
                int count = 0;
                
                for (String field : fields) {
                    Optional<Number> value = state.get(field, Number.class);
                    if (value.isPresent()) {
                        total += value.get().doubleValue();
                        count++;
                    }
                }
                
                return count > 0 ? total / count : 0.0;
            }
            
            @Override
            public String getStrategyName() {
                return "Average(" + String.join(", ", fields) + ")";
            }
            
            @Override
            public Set<String> getRequiredInputs() {
                return Set.of(fields);
            }
        };
    }
    
    public static AggregationStrategy concatenate(String... fields) {
        return new AggregationStrategy() {
            @Override
            public Object aggregate(GraphState state) {
                StringBuilder result = new StringBuilder();
                
                for (String field : fields) {
                    Optional<String> value = state.get(field, String.class);
                    if (value.isPresent()) {
                        if (result.length() > 0) {
                            result.append(" ");
                        }
                        result.append(value.get());
                    }
                }
                
                return result.toString();
            }
            
            @Override
            public String getStrategyName() {
                return "Concatenate(" + String.join(", ", fields) + ")";
            }
            
            @Override
            public Set<String> getRequiredInputs() {
                return Set.of(fields);
            }
        };
    }
}
```

### Decision and Routing Nodes
```java
/**
 * Decision node for conditional routing
 */
public class DecisionNode extends BaseGraphNode {
    
    private final List<ConditionalRoute> routes;
    private final String defaultRoute;
    
    public DecisionNode(String name, List<ConditionalRoute> routes, String defaultRoute) {
        super(name, "Conditional routing decision node", NodeType.DECISION);
        this.routes = new ArrayList<>(routes);
        this.defaultRoute = defaultRoute;
    }
    
    @Override
    protected GraphState executeInternal(GraphState state, NodeContext context) {
        context.getLogger().logNodeInfo(getName(), "Evaluating " + routes.size() + " routing conditions");
        
        // Evaluate conditions in order
        for (ConditionalRoute route : routes) {
            try {
                if (route.getCondition().test(state)) {
                    context.getLogger().logNodeInfo(getName(), 
                        "Condition matched: " + route.getConditionDescription() + " -> " + route.getTargetNode());
                    
                    GraphState outputState = state.copy();
                    outputState.set("next_node", route.getTargetNode());
                    outputState.set("routing_reason", route.getConditionDescription());
                    outputState.set("routing_timestamp", LocalDateTime.now());
                    
                    return outputState;
                }
            } catch (Exception e) {
                context.getLogger().logNodeWarning(getName(), 
                    "Condition evaluation error: " + e.getMessage());
            }
        }
        
        // No conditions matched, use default route
        context.getLogger().logNodeInfo(getName(), "No conditions matched, using default route: " + defaultRoute);
        
        GraphState outputState = state.copy();
        outputState.set("next_node", defaultRoute);
        outputState.set("routing_reason", "default");
        outputState.set("routing_timestamp", LocalDateTime.now());
        
        return outputState;
    }
    
    @Override
    public Set<String> getRequiredInputs() {
        return routes.stream()
            .flatMap(route -> route.getRequiredInputs().stream())
            .collect(Collectors.toSet());
    }
    
    @Override
    public Set<String> getOutputs() {
        return Set.of("next_node", "routing_reason", "routing_timestamp");
    }
}

/**
 * Conditional route for decision nodes
 */
public class ConditionalRoute {
    private final Predicate<GraphState> condition;
    private final String targetNode;
    private final String conditionDescription;
    private final Set<String> requiredInputs;
    
    public ConditionalRoute(Predicate<GraphState> condition, String targetNode, 
                          String conditionDescription, Set<String> requiredInputs) {
        this.condition = condition;
        this.targetNode = targetNode;
        this.conditionDescription = conditionDescription;
        this.requiredInputs = new HashSet<>(requiredInputs);
    }
    
    public Predicate<GraphState> getCondition() { return condition; }
    public String getTargetNode() { return targetNode; }
    public String getConditionDescription() { return conditionDescription; }
    public Set<String> getRequiredInputs() { return new HashSet<>(requiredInputs); }
}

/**
 * Common routing conditions
 */
public class RoutingConditions {
    
    public static ConditionalRoute fieldEquals(String field, Object expectedValue, String targetNode) {
        return new ConditionalRoute(
            state -> {
                Optional<?> value = state.get(field, Object.class);
                return value.isPresent() && Objects.equals(value.get(), expectedValue);
            },
            targetNode,
            field + " equals " + expectedValue,
            Set.of(field)
        );
    }
    
    public static ConditionalRoute fieldGreaterThan(String field, double threshold, String targetNode) {
        return new ConditionalRoute(
            state -> {
                Optional<Number> value = state.get(field, Number.class);
                return value.isPresent() && value.get().doubleValue() > threshold;
            },
            targetNode,
            field + " > " + threshold,
            Set.of(field)
        );
    }
    
    public static ConditionalRoute fieldContains(String field, String substring, String targetNode) {
        return new ConditionalRoute(
            state -> {
                Optional<String> value = state.get(field, String.class);
                return value.isPresent() && value.get().contains(substring);
            },
            targetNode,
            field + " contains '" + substring + "'",
            Set.of(field)
        );
    }
    
    public static ConditionalRoute fieldPresent(String field, String targetNode) {
        return new ConditionalRoute(
            state -> state.containsKey(field),
            targetNode,
            field + " is present",
            Set.of()
        );
    }
    
    public static ConditionalRoute multiFieldCondition(String description, String targetNode,
                                                      Set<String> requiredInputs,
                                                      Predicate<GraphState> condition) {
        return new ConditionalRoute(condition, targetNode, description, requiredInputs);
    }
}

/**
 * Parallel processing node
 */
public class ParallelNode extends BaseGraphNode {
    
    private final List<GraphNode> parallelNodes;
    private final ParallelStrategy strategy;
    
    public ParallelNode(String name, List<GraphNode> parallelNodes, ParallelStrategy strategy) {
        super(name, "Parallel execution node", NodeType.PARALLEL);
        this.parallelNodes = new ArrayList<>(parallelNodes);
        this.strategy = strategy;
    }
    
    @Override
    protected GraphState executeInternal(GraphState state, NodeContext context) {
        context.getLogger().logNodeInfo(getName(), 
            "Executing " + parallelNodes.size() + " nodes in parallel using " + strategy.getStrategyName());
        
        // Create execution contexts for each parallel node
        List<CompletableFuture<NodeResult>> futures = new ArrayList<>();
        
        for (GraphNode node : parallelNodes) {
            NodeContext nodeContext = new NodeContext(
                context.getExecutionId() + "_" + node.getName(),
                context.getWorkflowId(),
                context.getVariables(),
                context.getTimeout()
            );
            
            CompletableFuture<NodeResult> future = node.executeAsync(state, nodeContext);
            futures.add(future);
        }
        
        try {
            // Wait for all to complete or timeout
            List<NodeResult> results = futures.stream()
                .map(future -> {
                    try {
                        return future.get(context.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        return NodeResult.error("parallel_node_error", "Parallel execution failed: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
            
            // Apply strategy to combine results
            GraphState combinedState = strategy.combineResults(state, results);
            
            // Add parallel execution metadata
            combinedState.set("parallel_results_count", results.size());
            combinedState.set("parallel_success_count", 
                (int) results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum());
            combinedState.set("parallel_execution_timestamp", LocalDateTime.now());
            
            context.getLogger().logNodeInfo(getName(), 
                "Parallel execution complete: " + results.size() + " results combined");
            
            return combinedState;
            
        } catch (Exception e) {
            throw new RuntimeException("Parallel execution failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Set<String> getRequiredInputs() {
        return parallelNodes.stream()
            .flatMap(node -> node.getRequiredInputs().stream())
            .collect(Collectors.toSet());
    }
    
    @Override
    public Set<String> getOutputs() {
        Set<String> allOutputs = parallelNodes.stream()
            .flatMap(node -> node.getOutputs().stream())
            .collect(Collectors.toSet());
        
        allOutputs.addAll(Set.of("parallel_results_count", "parallel_success_count", "parallel_execution_timestamp"));
        return allOutputs;
    }
    
    @Override
    protected void doInitialize(Map<String, Object> config) throws Exception {
        // Initialize all parallel nodes
        for (GraphNode node : parallelNodes) {
            node.initialize(config);
        }
    }
    
    @Override
    protected void doCleanup() {
        // Cleanup all parallel nodes
        parallelNodes.forEach(GraphNode::cleanup);
    }
}

/**
 * Strategy for combining parallel execution results
 */
public interface ParallelStrategy {
    GraphState combineResults(GraphState originalState, List<NodeResult> results);
    String getStrategyName();
}

/**
 * Common parallel execution strategies
 */
public class ParallelStrategies {
    
    public static ParallelStrategy mergeStates() {
        return new ParallelStrategy() {
            @Override
            public GraphState combineResults(GraphState originalState, List<NodeResult> results) {
                GraphState combined = originalState.copy();
                
                for (NodeResult result : results) {
                    if (result.isSuccess() && result.getOutputState() != null) {
                        combined = combined.merge(result.getOutputState());
                    }
                }
                
                return combined;
            }
            
            @Override
            public String getStrategyName() {
                return "MergeStates";
            }
        };
    }
    
    public static ParallelStrategy collectResults(String resultKey) {
        return new ParallelStrategy() {
            @Override
            public GraphState combineResults(GraphState originalState, List<NodeResult> results) {
                GraphState combined = originalState.copy();
                
                List<Map<String, Object>> resultsList = new ArrayList<>();
                for (NodeResult result : results) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("success", result.isSuccess());
                    resultMap.put("node_name", result.getNodeName());
                    resultMap.put("execution_time", result.getExecutionTime().toMillis());
                    
                    if (result.isSuccess() && result.getOutputState() != null) {
                        Map<String, Object> stateData = new HashMap<>();
                        for (String key : result.getOutputState().getKeys()) {
                            stateData.put(key, result.getOutputState().get(key, Object.class).orElse(null));
                        }
                        resultMap.put("state_data", stateData);
                    } else {
                        resultMap.put("error", result.getErrorMessage());
                    }
                    
                    resultsList.add(resultMap);
                }
                
                combined.set(resultKey, resultsList);
                return combined;
            }
            
            @Override
            public String getStrategyName() {
                return "CollectResults(" + resultKey + ")";
            }
        };
    }
    
    public static ParallelStrategy firstSuccess() {
        return new ParallelStrategy() {
            @Override
            public GraphState combineResults(GraphState originalState, List<NodeResult> results) {
                for (NodeResult result : results) {
                    if (result.isSuccess() && result.getOutputState() != null) {
                        return result.getOutputState();
                    }
                }
                
                // If no success, return original state with error info
                GraphState errorState = originalState.copy();
                errorState.set("parallel_error", "No successful executions");
                return errorState;
            }
            
            @Override
            public String getStrategyName() {
                return "FirstSuccess";
            }
        };
    }
}
```

## 🚀 Best Practices

1. **Node Design**
   - Keep nodes focused on single responsibilities
   - Use clear input/output contracts
   - Implement proper validation
   - Handle errors gracefully

2. **State Management**
   - Minimize state mutations
   - Use immutable patterns where possible
   - Validate state at node boundaries
   - Document state schema changes

3. **Error Handling**
   - Provide meaningful error messages
   - Implement timeout handling
   - Use proper exception types
   - Log execution details for debugging

4. **Performance**
   - Use async execution for I/O operations
   - Implement proper timeouts
   - Monitor execution times
   - Use parallel processing where appropriate

5. **Testing**
   - Test nodes in isolation
   - Mock external dependencies
   - Test error conditions
   - Validate state transformations

## 🔗 Integration with Other Components

Graph Nodes integrate with:
- **State Management**: Nodes process and transform state
- **Edges**: Edge conditions use node outputs for routing
- **Tools**: Nodes can use tools for external capabilities
- **Human-in-Loop**: Nodes can pause for human intervention

---

*Next: [Graph Edges](../edges/) - Learn about conditional routing and graph flow control.*
