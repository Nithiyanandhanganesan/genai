# State Management in LangChain (Java)

## 🎯 Overview
State management in LangChain involves maintaining application state across complex workflows, multi-step processes, and agent interactions. Unlike simple session management, state management handles complex application logic, workflow progression, and data persistence across multiple components.

## 🧠 Core Concepts

### What is Application State?
Application state represents the current condition and data of your AI application:
- **Workflow State**: Current step in multi-step processes
- **Data State**: Accumulated information from various interactions
- **Context State**: Environmental conditions and configurations
- **Business Logic State**: Domain-specific application states

### State vs Session vs Memory
- **Session**: User interaction continuity (who is talking)
- **Memory**: Conversation history and context (what was said)
- **State**: Application logic and workflow position (where we are in the process)

## 🏗️ State Management Patterns

### 1. **Finite State Machines (FSM)**
Best for: Well-defined workflows with clear transitions
```
[Collect Info] → [Validate] → [Process] → [Confirm] → [Complete]
```

### 2. **Hierarchical State Machines**
Best for: Complex workflows with sub-states
```
[Order Process]
├── [Item Selection]
│   ├── [Browse Catalog]
│   └── [Add to Cart]
├── [Checkout]
│   ├── [Payment]
│   └── [Shipping]
└── [Confirmation]
```

### 3. **Event-Driven State**
Best for: Reactive systems with external triggers
```
Events → State Transitions → Actions → New State
```

### 4. **Context-Based State**
Best for: Rich contextual information management
```
Global Context + Local Context + Temporal Context = Current State
```

## 🔑 Key Components in Java

### State Definition
```java
public enum WorkflowState {
    INITIALIZING,
    COLLECTING_REQUIREMENTS,
    ANALYZING_DATA,
    GENERATING_RESPONSE,
    AWAITING_APPROVAL,
    COMPLETED,
    FAILED
}

public class ApplicationState {
    private WorkflowState currentState;
    private Map<String, Object> stateData;
    private LocalDateTime lastTransition;
    private String userId;
    private List<StateTransition> transitionHistory;
}
```

### State Transition Logic
```java
public interface StateTransition {
    boolean canTransition(ApplicationState currentState, String event);
    ApplicationState transition(ApplicationState currentState, String event, Object data);
    void onEnterState(ApplicationState newState);
    void onExitState(ApplicationState oldState);
}
```

### State Persistence
```java
public interface StatePersistence {
    void saveState(String stateId, ApplicationState state);
    Optional<ApplicationState> loadState(String stateId);
    void deleteState(String stateId);
    List<ApplicationState> findStatesByUser(String userId);
}
```

## 💻 Implementation Examples

### Basic State Machine
```java
@Component
public class WorkflowStateMachine {
    
    private final Map<String, ApplicationState> activeStates;
    private final StatePersistence statePersistence;
    
    public WorkflowStateMachine(StatePersistence statePersistence) {
        this.activeStates = new ConcurrentHashMap<>();
        this.statePersistence = statePersistence;
    }
    
    public String createWorkflow(String userId, String workflowType) {
        String stateId = UUID.randomUUID().toString();
        
        ApplicationState initialState = new ApplicationState();
        initialState.setStateId(stateId);
        initialState.setUserId(userId);
        initialState.setCurrentState(WorkflowState.INITIALIZING);
        initialState.setWorkflowType(workflowType);
        initialState.setCreatedAt(LocalDateTime.now());
        
        activeStates.put(stateId, initialState);
        statePersistence.saveState(stateId, initialState);
        
        return stateId;
    }
    
    public ApplicationState processEvent(String stateId, String event, Object data) {
        ApplicationState currentState = getState(stateId);
        
        // Find applicable transition
        StateTransition transition = findTransition(currentState.getCurrentState(), event);
        
        if (transition != null && transition.canTransition(currentState, event)) {
            // Execute transition
            ApplicationState newState = transition.transition(currentState, event, data);
            
            // Update state
            activeStates.put(stateId, newState);
            statePersistence.saveState(stateId, newState);
            
            return newState;
        } else {
            throw new IllegalStateException("Invalid transition from " + 
                currentState.getCurrentState() + " with event " + event);
        }
    }
}
```

### Rich State Context
```java
public class StateContext {
    private Map<String, Object> globalContext;
    private Map<String, Object> localContext;
    private Map<String, Object> temporalContext;
    private UserProfile userProfile;
    private ApplicationConfiguration config;
    
    public StateContext merge(StateContext other) {
        StateContext merged = new StateContext();
        merged.globalContext = new HashMap<>(this.globalContext);
        merged.globalContext.putAll(other.globalContext);
        // ... merge other contexts
        return merged;
    }
    
    public <T> Optional<T> getContextValue(String key, Class<T> type) {
        // Search in temporal -> local -> global order
        Object value = temporalContext.get(key);
        if (value == null) value = localContext.get(key);
        if (value == null) value = globalContext.get(key);
        
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }
}
```

## 🗄️ State Storage Strategies

### 1. **In-Memory State Store**
```java
@Component
public class InMemoryStateStore implements StatePersistence {
    
    private final Map<String, ApplicationState> states = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAccessed = new ConcurrentHashMap<>();
    
    @Override
    public void saveState(String stateId, ApplicationState state) {
        states.put(stateId, state);
        lastAccessed.put(stateId, LocalDateTime.now());
    }
    
    @Override
    public Optional<ApplicationState> loadState(String stateId) {
        lastAccessed.put(stateId, LocalDateTime.now());
        return Optional.ofNullable(states.get(stateId));
    }
    
    @Scheduled(fixedRate = 300000) // Clean up every 5 minutes
    public void cleanupExpiredStates() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(1);
        
        lastAccessed.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(expireTime)) {
                states.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}
```

### 2. **Database State Store**
```java
@Entity
@Table(name = "workflow_states")
public class WorkflowStateEntity {
    
    @Id
    private String stateId;
    
    @Column
    private String userId;
    
    @Enumerated(EnumType.STRING)
    private WorkflowState currentState;
    
    @Column(columnDefinition = "TEXT")
    private String stateData;
    
    @Column(columnDefinition = "TEXT")
    private String contextData;
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastModified;
    
    @Column
    private String workflowType;
    
    // Getters and setters...
}

@Service
public class DatabaseStateStore implements StatePersistence {
    
    @Autowired
    private WorkflowStateRepository repository;
    
    private final ObjectMapper objectMapper;
    
    public DatabaseStateStore() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void saveState(String stateId, ApplicationState state) {
        WorkflowStateEntity entity = repository.findById(stateId)
            .orElse(new WorkflowStateEntity());
        
        entity.setStateId(stateId);
        entity.setUserId(state.getUserId());
        entity.setCurrentState(state.getCurrentState());
        entity.setWorkflowType(state.getWorkflowType());
        entity.setLastModified(LocalDateTime.now());
        
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        
        try {
            entity.setStateData(objectMapper.writeValueAsString(state.getStateData()));
            entity.setContextData(objectMapper.writeValueAsString(state.getContext()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing state data", e);
        }
        
        repository.save(entity);
    }
    
    @Override
    public Optional<ApplicationState> loadState(String stateId) {
        return repository.findById(stateId)
            .map(this::entityToState);
    }
    
    private ApplicationState entityToState(WorkflowStateEntity entity) {
        ApplicationState state = new ApplicationState();
        state.setStateId(entity.getStateId());
        state.setUserId(entity.getUserId());
        state.setCurrentState(entity.getCurrentState());
        state.setWorkflowType(entity.getWorkflowType());
        state.setCreatedAt(entity.getCreatedAt());
        state.setLastModified(entity.getLastModified());
        
        try {
            if (entity.getStateData() != null) {
                Map<String, Object> stateData = objectMapper.readValue(
                    entity.getStateData(), new TypeReference<Map<String, Object>>() {});
                state.setStateData(stateData);
            }
            
            if (entity.getContextData() != null) {
                StateContext context = objectMapper.readValue(
                    entity.getContextData(), StateContext.class);
                state.setContext(context);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing state data", e);
        }
        
        return state;
    }
}
```

### 3. **Redis State Store**
```java
@Service
public class RedisStateStore implements StatePersistence {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private final ObjectMapper objectMapper;
    private static final String STATE_PREFIX = "workflow:state:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    
    public RedisStateStore() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void saveState(String stateId, ApplicationState state) {
        try {
            String key = STATE_PREFIX + stateId;
            String serializedState = objectMapper.writeValueAsString(state);
            
            redisTemplate.opsForValue().set(key, serializedState, DEFAULT_TTL);
            
            // Also maintain user index
            String userKey = "workflow:user:" + state.getUserId();
            redisTemplate.opsForSet().add(userKey, stateId);
            redisTemplate.expire(userKey, DEFAULT_TTL);
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing state", e);
        }
    }
    
    @Override
    public Optional<ApplicationState> loadState(String stateId) {
        try {
            String key = STATE_PREFIX + stateId;
            String serializedState = redisTemplate.opsForValue().get(key);
            
            if (serializedState != null) {
                ApplicationState state = objectMapper.readValue(serializedState, ApplicationState.class);
                
                // Refresh TTL
                redisTemplate.expire(key, DEFAULT_TTL);
                
                return Optional.of(state);
            }
            
            return Optional.empty();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing state", e);
        }
    }
}
```

## 🔄 State Transition Patterns

### Workflow-Based Transitions
```java
@Component
public class DocumentProcessingWorkflow {
    
    public enum DocumentState {
        UPLOADED,
        ANALYZING,
        EXTRACTING_TEXT,
        PROCESSING_CONTENT,
        GENERATING_SUMMARY,
        AWAITING_REVIEW,
        APPROVED,
        REJECTED,
        COMPLETED
    }
    
    @EventListener
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        ApplicationState state = stateManager.getState(event.getWorkflowId());
        
        if (state.getCurrentState() == DocumentState.UPLOADED) {
            // Update state with document information
            state.addStateData("documentId", event.getDocumentId());
            state.addStateData("fileName", event.getFileName());
            state.addStateData("fileSize", event.getFileSize());
            
            // Transition to analysis
            stateManager.transitionTo(state.getStateId(), DocumentState.ANALYZING);
            
            // Trigger analysis
            analysisService.analyzeDocument(event.getDocumentId(), state.getStateId());
        }
    }
    
    @EventListener
    public void handleAnalysisComplete(AnalysisCompleteEvent event) {
        ApplicationState state = stateManager.getState(event.getWorkflowId());
        
        if (state.getCurrentState() == DocumentState.ANALYZING) {
            // Store analysis results
            state.addStateData("analysisResults", event.getResults());
            state.addStateData("confidence", event.getConfidence());
            
            if (event.getConfidence() > 0.8) {
                stateManager.transitionTo(state.getStateId(), DocumentState.EXTRACTING_TEXT);
            } else {
                stateManager.transitionTo(state.getStateId(), DocumentState.AWAITING_REVIEW);
            }
        }
    }
}
```

## 🔐 State Security and Validation

### State Validation
```java
@Component
public class StateValidator {
    
    public boolean validateStateTransition(ApplicationState currentState, 
                                         WorkflowState newState, 
                                         String userId) {
        // Check user permissions
        if (!hasPermission(userId, currentState, newState)) {
            return false;
        }
        
        // Validate business rules
        if (!isValidTransition(currentState.getCurrentState(), newState)) {
            return false;
        }
        
        // Check required data
        if (!hasRequiredData(currentState, newState)) {
            return false;
        }
        
        return true;
    }
    
    private boolean hasRequiredData(ApplicationState state, WorkflowState newState) {
        switch (newState) {
            case PROCESSING:
                return state.getStateData().containsKey("inputData");
            case AWAITING_APPROVAL:
                return state.getStateData().containsKey("results");
            default:
                return true;
        }
    }
}
```

## 🚀 Best Practices

1. **State Design**
   - Keep states simple and focused
   - Use enum types for defined state values
   - Implement proper state validation

2. **Data Management**
   - Serialize complex objects properly
   - Handle null and missing data gracefully
   - Implement proper error handling

3. **Performance**
   - Use appropriate storage based on access patterns
   - Implement state caching for frequently accessed states
   - Clean up expired states regularly

4. **Monitoring**
   - Log state transitions for debugging
   - Track state execution times
   - Monitor state distribution and patterns

5. **Testing**
   - Test all state transitions
   - Validate error conditions
   - Test state persistence and recovery

## 🔗 Integration with Other Concepts

State management works closely with:
- **Session Management**: States persist across sessions
- **Memory Systems**: States influence what should be remembered
- **Tool Integration**: States determine which tools are available
- **Agent Behavior**: States control agent decision-making

---

*Next: [Memory Systems](../memory/) - Learn about different memory implementations for conversation context.*
