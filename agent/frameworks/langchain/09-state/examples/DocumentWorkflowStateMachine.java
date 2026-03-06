/**
 * State Management Example - Document Processing Workflow
 * Demonstrates complex state management with transitions and validations
 */
package com.example.agent.state;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Workflow state definitions
 */
enum DocumentWorkflowState {
    CREATED,
    UPLOADED,
    ANALYZING,
    EXTRACTING_TEXT,
    PROCESSING_CONTENT,
    GENERATING_SUMMARY,
    AWAITING_REVIEW,
    APPROVED,
    REJECTED,
    COMPLETED,
    FAILED
}

/**
 * Application state container
 */
class ApplicationState {
    private String stateId;
    private String userId;
    private DocumentWorkflowState currentState;
    private Map<String, Object> stateData;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private List<StateTransition> transitionHistory;
    private String workflowType;

    public ApplicationState() {
        this.stateData = new HashMap<>();
        this.transitionHistory = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    public ApplicationState(String stateId, String userId, DocumentWorkflowState initialState) {
        this();
        this.stateId = stateId;
        this.userId = userId;
        this.currentState = initialState;
    }

    public void addStateData(String key, Object value) {
        this.stateData.put(key, value);
        this.lastModified = LocalDateTime.now();
    }

    public <T> Optional<T> getStateData(String key, Class<T> type) {
        Object value = stateData.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public void addTransition(StateTransition transition) {
        this.transitionHistory.add(transition);
        this.lastModified = LocalDateTime.now();
    }

    // Getters and setters
    public String getStateId() { return stateId; }
    public void setStateId(String stateId) { this.stateId = stateId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public DocumentWorkflowState getCurrentState() { return currentState; }
    public void setCurrentState(DocumentWorkflowState currentState) {
        this.currentState = currentState;
        this.lastModified = LocalDateTime.now();
    }

    public Map<String, Object> getStateData() { return stateData; }
    public void setStateData(Map<String, Object> stateData) { this.stateData = stateData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastModified() { return lastModified; }

    public List<StateTransition> getTransitionHistory() { return transitionHistory; }

    public String getWorkflowType() { return workflowType; }
    public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }
}

/**
 * State transition record
 */
class StateTransition {
    private final DocumentWorkflowState fromState;
    private final DocumentWorkflowState toState;
    private final String event;
    private final LocalDateTime timestamp;
    private final String userId;
    private final Map<String, Object> transitionData;

    public StateTransition(DocumentWorkflowState fromState, DocumentWorkflowState toState,
                          String event, String userId) {
        this.fromState = fromState;
        this.toState = toState;
        this.event = event;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
        this.transitionData = new HashMap<>();
    }

    public void addTransitionData(String key, Object value) {
        this.transitionData.put(key, value);
    }

    // Getters
    public DocumentWorkflowState getFromState() { return fromState; }
    public DocumentWorkflowState getToState() { return toState; }
    public String getEvent() { return event; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
    public Map<String, Object> getTransitionData() { return transitionData; }
}

/**
 * State machine for document processing workflow
 */
public class DocumentWorkflowStateMachine {

    private final Map<String, ApplicationState> activeStates;
    private final Map<DocumentWorkflowState, Set<DocumentWorkflowState>> allowedTransitions;

    public DocumentWorkflowStateMachine() {
        this.activeStates = new ConcurrentHashMap<>();
        this.allowedTransitions = initializeTransitions();
    }

    /**
     * Initialize allowed state transitions
     */
    private Map<DocumentWorkflowState, Set<DocumentWorkflowState>> initializeTransitions() {
        Map<DocumentWorkflowState, Set<DocumentWorkflowState>> transitions = new HashMap<>();

        transitions.put(DocumentWorkflowState.CREATED,
            Set.of(DocumentWorkflowState.UPLOADED, DocumentWorkflowState.FAILED));

        transitions.put(DocumentWorkflowState.UPLOADED,
            Set.of(DocumentWorkflowState.ANALYZING, DocumentWorkflowState.FAILED));

        transitions.put(DocumentWorkflowState.ANALYZING,
            Set.of(DocumentWorkflowState.EXTRACTING_TEXT, DocumentWorkflowState.AWAITING_REVIEW,
                   DocumentWorkflowState.FAILED));

        transitions.put(DocumentWorkflowState.EXTRACTING_TEXT,
            Set.of(DocumentWorkflowState.PROCESSING_CONTENT, DocumentWorkflowState.FAILED));

        transitions.put(DocumentWorkflowState.PROCESSING_CONTENT,
            Set.of(DocumentWorkflowState.GENERATING_SUMMARY, DocumentWorkflowState.FAILED));

        transitions.put(DocumentWorkflowState.GENERATING_SUMMARY,
            Set.of(DocumentWorkflowState.AWAITING_REVIEW, DocumentWorkflowState.COMPLETED,
                   DocumentWorkflowState.FAILED));

        transitions.put(DocumentWorkflowState.AWAITING_REVIEW,
            Set.of(DocumentWorkflowState.APPROVED, DocumentWorkflowState.REJECTED,
                   DocumentWorkflowState.PROCESSING_CONTENT));

        transitions.put(DocumentWorkflowState.APPROVED,
            Set.of(DocumentWorkflowState.COMPLETED));

        transitions.put(DocumentWorkflowState.REJECTED,
            Set.of(DocumentWorkflowState.PROCESSING_CONTENT, DocumentWorkflowState.FAILED));

        return transitions;
    }

    /**
     * Create a new workflow instance
     */
    public String createWorkflow(String userId, String documentName) {
        String stateId = UUID.randomUUID().toString();

        ApplicationState state = new ApplicationState(stateId, userId, DocumentWorkflowState.CREATED);
        state.setWorkflowType("document_processing");
        state.addStateData("documentName", documentName);
        state.addStateData("createdBy", userId);

        activeStates.put(stateId, state);

        System.out.println("Created workflow " + stateId + " for document: " + documentName);
        return stateId;
    }

    /**
     * Process an event and potentially transition state
     */
    public ApplicationState processEvent(String stateId, String event, Map<String, Object> eventData) {
        ApplicationState currentState = getState(stateId);
        if (currentState == null) {
            throw new IllegalArgumentException("Workflow " + stateId + " not found");
        }

        DocumentWorkflowState newState = determineNewState(currentState.getCurrentState(), event);

        if (newState != null && isValidTransition(currentState.getCurrentState(), newState)) {
            return executeTransition(currentState, newState, event, eventData);
        } else {
            throw new IllegalStateException("Invalid transition from " +
                currentState.getCurrentState() + " with event " + event);
        }
    }

    /**
     * Get current state
     */
    public ApplicationState getState(String stateId) {
        return activeStates.get(stateId);
    }

    /**
     * Manual state transition with validation
     */
    public ApplicationState transitionTo(String stateId, DocumentWorkflowState newState, String userId) {
        ApplicationState currentState = getState(stateId);
        if (currentState == null) {
            throw new IllegalArgumentException("Workflow " + stateId + " not found");
        }

        if (!isValidTransition(currentState.getCurrentState(), newState)) {
            throw new IllegalStateException("Invalid transition from " +
                currentState.getCurrentState() + " to " + newState);
        }

        return executeTransition(currentState, newState, "MANUAL_TRANSITION",
            Map.of("transitionBy", userId));
    }

    /**
     * Determine new state based on current state and event
     */
    private DocumentWorkflowState determineNewState(DocumentWorkflowState currentState, String event) {
        switch (event) {
            case "DOCUMENT_UPLOADED":
                return currentState == DocumentWorkflowState.CREATED ?
                    DocumentWorkflowState.UPLOADED : null;

            case "ANALYSIS_STARTED":
                return currentState == DocumentWorkflowState.UPLOADED ?
                    DocumentWorkflowState.ANALYZING : null;

            case "ANALYSIS_COMPLETED":
                return currentState == DocumentWorkflowState.ANALYZING ?
                    DocumentWorkflowState.EXTRACTING_TEXT : null;

            case "TEXT_EXTRACTED":
                return currentState == DocumentWorkflowState.EXTRACTING_TEXT ?
                    DocumentWorkflowState.PROCESSING_CONTENT : null;

            case "CONTENT_PROCESSED":
                return currentState == DocumentWorkflowState.PROCESSING_CONTENT ?
                    DocumentWorkflowState.GENERATING_SUMMARY : null;

            case "SUMMARY_GENERATED":
                return currentState == DocumentWorkflowState.GENERATING_SUMMARY ?
                    DocumentWorkflowState.AWAITING_REVIEW : null;

            case "APPROVED":
                return currentState == DocumentWorkflowState.AWAITING_REVIEW ?
                    DocumentWorkflowState.APPROVED : null;

            case "REJECTED":
                return currentState == DocumentWorkflowState.AWAITING_REVIEW ?
                    DocumentWorkflowState.REJECTED : null;

            case "COMPLETED":
                return (currentState == DocumentWorkflowState.APPROVED ||
                       currentState == DocumentWorkflowState.GENERATING_SUMMARY) ?
                    DocumentWorkflowState.COMPLETED : null;

            case "FAILED":
                return DocumentWorkflowState.FAILED; // Can fail from any state

            default:
                return null;
        }
    }

    /**
     * Check if transition is allowed
     */
    private boolean isValidTransition(DocumentWorkflowState fromState, DocumentWorkflowState toState) {
        Set<DocumentWorkflowState> allowed = allowedTransitions.get(fromState);
        return allowed != null && allowed.contains(toState);
    }

    /**
     * Execute state transition
     */
    private ApplicationState executeTransition(ApplicationState currentState,
                                             DocumentWorkflowState newState,
                                             String event,
                                             Map<String, Object> eventData) {

        DocumentWorkflowState previousState = currentState.getCurrentState();

        // Create transition record
        StateTransition transition = new StateTransition(
            previousState, newState, event, currentState.getUserId());

        if (eventData != null) {
            eventData.forEach(transition::addTransitionData);
        }

        // Execute pre-transition actions
        executePreTransitionActions(currentState, newState, eventData);

        // Update state
        currentState.setCurrentState(newState);
        currentState.addTransition(transition);

        // Add event data to state
        if (eventData != null) {
            eventData.forEach(currentState::addStateData);
        }

        // Execute post-transition actions
        executePostTransitionActions(currentState, previousState, eventData);

        System.out.println("Transitioned workflow " + currentState.getStateId() +
                          " from " + previousState + " to " + newState +
                          " via event " + event);

        return currentState;
    }

    /**
     * Execute actions before state transition
     */
    private void executePreTransitionActions(ApplicationState state,
                                           DocumentWorkflowState newState,
                                           Map<String, Object> eventData) {
        switch (newState) {
            case ANALYZING:
                System.out.println("Starting analysis for workflow " + state.getStateId());
                state.addStateData("analysisStartTime", LocalDateTime.now());
                break;

            case PROCESSING_CONTENT:
                System.out.println("Beginning content processing for workflow " + state.getStateId());
                state.addStateData("processingStartTime", LocalDateTime.now());
                break;

            case AWAITING_REVIEW:
                System.out.println("Workflow " + state.getStateId() + " ready for review");
                state.addStateData("reviewRequestTime", LocalDateTime.now());
                // In real implementation, notify reviewers
                break;
        }
    }

    /**
     * Execute actions after state transition
     */
    private void executePostTransitionActions(ApplicationState state,
                                            DocumentWorkflowState previousState,
                                            Map<String, Object> eventData) {
        switch (state.getCurrentState()) {
            case COMPLETED:
                System.out.println("Workflow " + state.getStateId() + " completed successfully");
                state.addStateData("completedAt", LocalDateTime.now());
                calculateWorkflowMetrics(state);
                break;

            case FAILED:
                System.out.println("Workflow " + state.getStateId() + " failed");
                state.addStateData("failedAt", LocalDateTime.now());
                state.addStateData("failureReason", eventData.get("reason"));
                break;

            case APPROVED:
                System.out.println("Workflow " + state.getStateId() + " approved");
                state.addStateData("approvedAt", LocalDateTime.now());
                state.addStateData("approvedBy", eventData.get("approvedBy"));
                break;
        }
    }

    /**
     * Calculate workflow execution metrics
     */
    private void calculateWorkflowMetrics(ApplicationState state) {
        LocalDateTime startTime = state.getCreatedAt();
        LocalDateTime endTime = LocalDateTime.now();

        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();

        state.addStateData("totalExecutionTimeMinutes", totalMinutes);
        state.addStateData("totalTransitions", state.getTransitionHistory().size());

        // Calculate time in each state
        Map<DocumentWorkflowState, Long> stateTimings = new HashMap<>();

        LocalDateTime currentTime = startTime;
        for (StateTransition transition : state.getTransitionHistory()) {
            long timeInState = java.time.Duration.between(currentTime, transition.getTimestamp()).toMinutes();
            stateTimings.merge(transition.getFromState(), timeInState, Long::sum);
            currentTime = transition.getTimestamp();
        }

        state.addStateData("stateExecutionTimes", stateTimings);
    }

    /**
     * Get workflow statistics
     */
    public Map<String, Object> getWorkflowStatistics(String stateId) {
        ApplicationState state = getState(stateId);
        if (state == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("workflowId", stateId);
        stats.put("currentState", state.getCurrentState());
        stats.put("createdAt", state.getCreatedAt());
        stats.put("lastModified", state.getLastModified());
        stats.put("totalTransitions", state.getTransitionHistory().size());

        if (state.getCurrentState() == DocumentWorkflowState.COMPLETED ||
            state.getCurrentState() == DocumentWorkflowState.FAILED) {

            stats.put("totalExecutionTime",
                state.getStateData("totalExecutionTimeMinutes", Long.class).orElse(0L));
            stats.put("stateTimings",
                state.getStateData("stateExecutionTimes", Map.class).orElse(Collections.emptyMap()));
        }

        return stats;
    }

    /**
     * List all active workflows for a user
     */
    public List<ApplicationState> getUserWorkflows(String userId) {
        return activeStates.values().stream()
            .filter(state -> userId.equals(state.getUserId()))
            .filter(state -> state.getCurrentState() != DocumentWorkflowState.COMPLETED &&
                           state.getCurrentState() != DocumentWorkflowState.FAILED)
            .toList();
    }
}

/**
 * Example usage and testing
 */
class StateManagementExample {

    public static void main(String[] args) {
        DocumentWorkflowStateMachine stateMachine = new DocumentWorkflowStateMachine();

        // Create a new workflow
        String workflowId = stateMachine.createWorkflow("user123", "contract_analysis.pdf");

        System.out.println("\n=== Simulating Document Processing Workflow ===");

        try {
            // Simulate workflow progression
            stateMachine.processEvent(workflowId, "DOCUMENT_UPLOADED",
                Map.of("fileSize", 1024000, "mimeType", "application/pdf"));

            Thread.sleep(1000);

            stateMachine.processEvent(workflowId, "ANALYSIS_STARTED",
                Map.of("analysisType", "contract_analysis"));

            Thread.sleep(2000);

            stateMachine.processEvent(workflowId, "ANALYSIS_COMPLETED",
                Map.of("confidence", 0.92, "extractedEntities", 15));

            Thread.sleep(1000);

            stateMachine.processEvent(workflowId, "TEXT_EXTRACTED",
                Map.of("textLength", 5000, "pages", 12));

            Thread.sleep(1500);

            stateMachine.processEvent(workflowId, "CONTENT_PROCESSED",
                Map.of("processedSections", 8, "keyPoints", 25));

            Thread.sleep(1000);

            stateMachine.processEvent(workflowId, "SUMMARY_GENERATED",
                Map.of("summaryLength", 500, "keyHighlights", 5));

            System.out.println("\n=== Workflow ready for review ===");

            // Simulate human review and approval
            Thread.sleep(2000);

            stateMachine.processEvent(workflowId, "APPROVED",
                Map.of("approvedBy", "reviewer123", "reviewNotes", "Looks good"));

            stateMachine.processEvent(workflowId, "COMPLETED",
                Map.of("completedBy", "system"));

            // Show final statistics
            System.out.println("\n=== Workflow Statistics ===");
            Map<String, Object> stats = stateMachine.getWorkflowStatistics(workflowId);
            stats.forEach((key, value) -> System.out.println(key + ": " + value));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error in workflow: " + e.getMessage());

            // Handle failure
            stateMachine.processEvent(workflowId, "FAILED",
                Map.of("reason", e.getMessage(), "failureStage", "processing"));
        }
    }
}
