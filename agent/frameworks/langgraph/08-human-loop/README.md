# Human-in-the-Loop in LangGraph (Java)

## 🎯 Overview
Human-in-the-Loop (HITL) in LangGraph enables interactive workflows where human intervention, approval, and decision-making are seamlessly integrated into automated graph executions. This approach combines AI efficiency with human judgment, ensuring quality control, handling edge cases, and maintaining oversight in critical processes.

## 🧠 Core HITL Concepts

### What is Human-in-the-Loop?
Human-in-the-Loop workflows provide:
- **Interactive Decision Points**: Pause execution for human input
- **Approval Workflows**: Require human authorization for critical actions
- **Quality Control**: Human review of AI-generated outputs
- **Exception Handling**: Human intervention for edge cases
- **Collaborative Processing**: Humans and AI working together

### HITL Patterns
1. **Approval Gates**: Require explicit human approval
2. **Review Points**: Human validation of AI decisions
3. **Input Collection**: Gather additional information from humans
4. **Quality Assessment**: Human evaluation of outputs
5. **Exception Resolution**: Human handling of error conditions
6. **Progressive Automation**: Gradual reduction of human involvement

## 🏗️ HITL Architecture Patterns

### 1. **Sequential Review Pattern**
```
AI Process → Human Review → Approval/Rejection → Continue/Retry
```

### 2. **Parallel Validation Pattern**
```
AI Process → Multiple Reviewers → Consensus → Continue
```

### 3. **Escalation Pattern**
```
AI Process → Auto-Check → [Failed] → Human Escalation → Resolution
```

### 4. **Collaborative Editing Pattern**
```
AI Draft → Human Edit → AI Refine → Human Final Review → Complete
```

## 💻 Java HITL Implementation

### Base HITL Framework
```java
package com.example.agent.langgraph.humanloop;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Base interface for Human-in-the-Loop operations
 */
public interface HumanInTheLoop {
    
    /**
     * Request human input
     */
    CompletableFuture<HumanResponse> requestHumanInput(HumanRequest request) throws HumanLoopException;
    
    /**
     * Request approval for an action
     */
    CompletableFuture<ApprovalResponse> requestApproval(ApprovalRequest request) throws HumanLoopException;
    
    /**
     * Submit for human review
     */
    CompletableFuture<ReviewResponse> submitForReview(ReviewRequest request) throws HumanLoopException;
    
    /**
     * Escalate to human for resolution
     */
    CompletableFuture<EscalationResponse> escalateToHuman(EscalationRequest request) throws HumanLoopException;
    
    /**
     * Check status of pending human interactions
     */
    List<PendingInteraction> getPendingInteractions(String workflowId) throws HumanLoopException;
    
    /**
     * Cancel pending human interaction
     */
    boolean cancelInteraction(String interactionId) throws HumanLoopException;
    
    /**
     * Set timeout for human responses
     */
    void setTimeout(String interactionId, Duration timeout) throws HumanLoopException;
    
    /**
     * Register human interaction handler
     */
    void registerHandler(String handlerType, HumanInteractionHandler handler);
    
    /**
     * Initialize HITL system
     */
    void initialize(Map<String, Object> config) throws HumanLoopException;
    
    /**
     * Close HITL system
     */
    void close() throws HumanLoopException;
}

/**
 * Human request types
 */
public class HumanRequest {
    private final String requestId;
    private final String workflowId;
    private final String executionId;
    private final RequestType type;
    private final String title;
    private final String description;
    private final Map<String, Object> context;
    private final List<String> assignedUsers;
    private final Duration timeout;
    private final Priority priority;
    
    public HumanRequest(String requestId, String workflowId, String executionId,
                       RequestType type, String title, String description,
                       Map<String, Object> context, List<String> assignedUsers,
                       Duration timeout, Priority priority) {
        this.requestId = requestId;
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.context = new HashMap<>(context);
        this.assignedUsers = new ArrayList<>(assignedUsers);
        this.timeout = timeout;
        this.priority = priority;
    }
    
    // Getters
    public String getRequestId() { return requestId; }
    public String getWorkflowId() { return workflowId; }
    public String getExecutionId() { return executionId; }
    public RequestType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Map<String, Object> getContext() { return new HashMap<>(context); }
    public List<String> getAssignedUsers() { return new ArrayList<>(assignedUsers); }
    public Duration getTimeout() { return timeout; }
    public Priority getPriority() { return priority; }
}

/**
 * Request types
 */
public enum RequestType {
    INPUT_COLLECTION,   // Collect additional information
    APPROVAL,          // Approve/reject an action
    REVIEW,           // Review and validate output
    DECISION,         // Make a decision between options
    ESCALATION,       // Handle exceptional situation
    QUALITY_CHECK     // Assess quality of output
}

/**
 * Priority levels
 */
public enum Priority {
    LOW, NORMAL, HIGH, URGENT, CRITICAL
}

/**
 * Human response
 */
public class HumanResponse {
    private final String requestId;
    private final String responderId;
    private final LocalDateTime responseTime;
    private final ResponseStatus status;
    private final Map<String, Object> responseData;
    private final String comments;
    private final Duration responseTimeTotal;
    
    public HumanResponse(String requestId, String responderId, ResponseStatus status,
                        Map<String, Object> responseData, String comments,
                        Duration responseTimeTotal) {
        this.requestId = requestId;
        this.responderId = responderId;
        this.responseTime = LocalDateTime.now();
        this.status = status;
        this.responseData = new HashMap<>(responseData);
        this.comments = comments;
        this.responseTimeTotal = responseTimeTotal;
    }
    
    public static HumanResponse approved(String requestId, String responderId, String comments) {
        return new HumanResponse(requestId, responderId, ResponseStatus.APPROVED, 
                                Map.of(), comments, Duration.ZERO);
    }
    
    public static HumanResponse rejected(String requestId, String responderId, String reason) {
        return new HumanResponse(requestId, responderId, ResponseStatus.REJECTED, 
                                Map.of("rejection_reason", reason), reason, Duration.ZERO);
    }
    
    public static HumanResponse completed(String requestId, String responderId, 
                                        Map<String, Object> data, String comments) {
        return new HumanResponse(requestId, responderId, ResponseStatus.COMPLETED, 
                                data, comments, Duration.ZERO);
    }
    
    // Getters
    public String getRequestId() { return requestId; }
    public String getResponderId() { return responderId; }
    public LocalDateTime getResponseTime() { return responseTime; }
    public ResponseStatus getStatus() { return status; }
    public Map<String, Object> getResponseData() { return new HashMap<>(responseData); }
    public String getComments() { return comments; }
    public Duration getResponseTimeTotal() { return responseTimeTotal; }
}

/**
 * Response status
 */
public enum ResponseStatus {
    APPROVED,      // Request approved
    REJECTED,      // Request rejected
    COMPLETED,     // Task completed
    MODIFIED,      // Request modified/updated
    ESCALATED,     // Escalated to higher authority
    TIMEOUT,       // Response timeout
    CANCELLED      // Request cancelled
}

/**
 * Approval request
 */
public class ApprovalRequest extends HumanRequest {
    private final Object itemToApprove;
    private final ApprovalType approvalType;
    private final List<String> requiredApprovers;
    private final boolean requiresConsensus;
    
    public ApprovalRequest(String requestId, String workflowId, String executionId,
                          String title, String description, Object itemToApprove,
                          ApprovalType approvalType, List<String> requiredApprovers,
                          boolean requiresConsensus, Duration timeout, Priority priority) {
        super(requestId, workflowId, executionId, RequestType.APPROVAL, title, description,
              Map.of("item_to_approve", itemToApprove), requiredApprovers, timeout, priority);
        this.itemToApprove = itemToApprove;
        this.approvalType = approvalType;
        this.requiredApprovers = new ArrayList<>(requiredApprovers);
        this.requiresConsensus = requiresConsensus;
    }
    
    // Getters
    public Object getItemToApprove() { return itemToApprove; }
    public ApprovalType getApprovalType() { return approvalType; }
    public List<String> getRequiredApprovers() { return new ArrayList<>(requiredApprovers); }
    public boolean isRequiresConsensus() { return requiresConsensus; }
}

/**
 * Approval types
 */
public enum ApprovalType {
    SINGLE_APPROVER,    // Single person approval
    MULTI_APPROVER,     // Multiple people approval
    CONSENSUS,          // Consensus required
    ESCALATION_CHAIN    // Approval chain with escalation
}

/**
 * Approval response
 */
public class ApprovalResponse extends HumanResponse {
    private final boolean approved;
    private final List<String> approverIds;
    private final Map<String, String> individualComments;
    
    public ApprovalResponse(String requestId, boolean approved, List<String> approverIds,
                           Map<String, String> individualComments, String overallComments) {
        super(requestId, String.join(",", approverIds), 
              approved ? ResponseStatus.APPROVED : ResponseStatus.REJECTED,
              Map.of("approved", approved), overallComments, Duration.ZERO);
        this.approved = approved;
        this.approverIds = new ArrayList<>(approverIds);
        this.individualComments = new HashMap<>(individualComments);
    }
    
    // Getters
    public boolean isApproved() { return approved; }
    public List<String> getApproverIds() { return new ArrayList<>(approverIds); }
    public Map<String, String> getIndividualComments() { return new HashMap<>(individualComments); }
}

/**
 * Review request
 */
public class ReviewRequest extends HumanRequest {
    private final Object itemToReview;
    private final ReviewType reviewType;
    private final List<ReviewCriterion> criteria;
    
    public ReviewRequest(String requestId, String workflowId, String executionId,
                        String title, String description, Object itemToReview,
                        ReviewType reviewType, List<ReviewCriterion> criteria,
                        List<String> reviewers, Duration timeout, Priority priority) {
        super(requestId, workflowId, executionId, RequestType.REVIEW, title, description,
              Map.of("item_to_review", itemToReview), reviewers, timeout, priority);
        this.itemToReview = itemToReview;
        this.reviewType = reviewType;
        this.criteria = new ArrayList<>(criteria);
    }
    
    // Getters
    public Object getItemToReview() { return itemToReview; }
    public ReviewType getReviewType() { return reviewType; }
    public List<ReviewCriterion> getCriteria() { return new ArrayList<>(criteria); }
}

/**
 * Review types
 */
public enum ReviewType {
    QUALITY_REVIEW,     // Quality assessment
    CONTENT_REVIEW,     // Content validation
    TECHNICAL_REVIEW,   // Technical validation
    COMPLIANCE_REVIEW,  // Compliance check
    PEER_REVIEW        // Peer review
}

/**
 * Review criterion
 */
public class ReviewCriterion {
    private final String name;
    private final String description;
    private final CriterionType type;
    private final boolean required;
    private final Object expectedValue;
    
    public ReviewCriterion(String name, String description, CriterionType type,
                          boolean required, Object expectedValue) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.expectedValue = expectedValue;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CriterionType getType() { return type; }
    public boolean isRequired() { return required; }
    public Object getExpectedValue() { return expectedValue; }
}

/**
 * Criterion types
 */
public enum CriterionType {
    BINARY,        // Yes/No or Pass/Fail
    SCALE,         // Numeric scale (1-10)
    CATEGORICAL,   // Multiple choice
    TEXT,          // Free text response
    CHECKLIST      // Multiple checkboxes
}

/**
 * Review response
 */
public class ReviewResponse extends HumanResponse {
    private final boolean passed;
    private final Map<String, Object> criterionResults;
    private final double overallScore;
    private final List<String> issues;
    private final List<String> recommendations;
    
    public ReviewResponse(String requestId, String reviewerId, boolean passed,
                         Map<String, Object> criterionResults, double overallScore,
                         List<String> issues, List<String> recommendations, String comments) {
        super(requestId, reviewerId, passed ? ResponseStatus.APPROVED : ResponseStatus.REJECTED,
              Map.of("criterion_results", criterionResults, "overall_score", overallScore),
              comments, Duration.ZERO);
        this.passed = passed;
        this.criterionResults = new HashMap<>(criterionResults);
        this.overallScore = overallScore;
        this.issues = new ArrayList<>(issues);
        this.recommendations = new ArrayList<>(recommendations);
    }
    
    // Getters
    public boolean isPassed() { return passed; }
    public Map<String, Object> getCriterionResults() { return new HashMap<>(criterionResults); }
    public double getOverallScore() { return overallScore; }
    public List<String> getIssues() { return new ArrayList<>(issues); }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
}

/**
 * Escalation request
 */
public class EscalationRequest extends HumanRequest {
    private final String originalRequestId;
    private final EscalationType escalationType;
    private final String issue;
    private final Map<String, Object> troubleshootingContext;
    
    public EscalationRequest(String requestId, String workflowId, String executionId,
                           String originalRequestId, EscalationType escalationType,
                           String issue, Map<String, Object> troubleshootingContext,
                           List<String> escalationTargets, Priority priority) {
        super(requestId, workflowId, executionId, RequestType.ESCALATION,
              "Escalation: " + issue, "Requires human intervention for resolution",
              troubleshootingContext, escalationTargets, Duration.ofHours(4), priority);
        this.originalRequestId = originalRequestId;
        this.escalationType = escalationType;
        this.issue = issue;
        this.troubleshootingContext = new HashMap<>(troubleshootingContext);
    }
    
    // Getters
    public String getOriginalRequestId() { return originalRequestId; }
    public EscalationType getEscalationType() { return escalationType; }
    public String getIssue() { return issue; }
    public Map<String, Object> getTroubleshootingContext() { return new HashMap<>(troubleshootingContext); }
}

/**
 * Escalation types
 */
public enum EscalationType {
    TECHNICAL_ISSUE,    // Technical problem
    BUSINESS_LOGIC,     // Business rule exception
    DATA_QUALITY,       // Data quality issue
    TIMEOUT,           // Process timeout
    APPROVAL_DENIED,   // Approval was denied
    SYSTEM_ERROR       // System error
}

/**
 * Escalation response
 */
public class EscalationResponse extends HumanResponse {
    private final String resolution;
    private final ResolutionAction action;
    private final Map<String, Object> resolutionData;
    private final boolean requiresFollowUp;
    
    public EscalationResponse(String requestId, String resolverId, String resolution,
                             ResolutionAction action, Map<String, Object> resolutionData,
                             boolean requiresFollowUp, String comments) {
        super(requestId, resolverId, ResponseStatus.COMPLETED,
              Map.of("resolution", resolution, "action", action), comments, Duration.ZERO);
        this.resolution = resolution;
        this.action = action;
        this.resolutionData = new HashMap<>(resolutionData);
        this.requiresFollowUp = requiresFollowUp;
    }
    
    // Getters
    public String getResolution() { return resolution; }
    public ResolutionAction getAction() { return action; }
    public Map<String, Object> getResolutionData() { return new HashMap<>(resolutionData); }
    public boolean isRequiresFollowUp() { return requiresFollowUp; }
}

/**
 * Resolution actions
 */
public enum ResolutionAction {
    RETRY,              // Retry the original operation
    SKIP,               // Skip the problematic step
    MODIFY_AND_CONTINUE, // Modify parameters and continue
    ABORT_WORKFLOW,     // Abort the entire workflow
    ESCALATE_FURTHER,   // Escalate to higher level
    MANUAL_OVERRIDE     // Manual override of the issue
}

/**
 * Pending interaction info
 */
public class PendingInteraction {
    private final String interactionId;
    private final String workflowId;
    private final String executionId;
    private final RequestType type;
    private final LocalDateTime createdAt;
    private final Duration timeRemaining;
    private final List<String> assignedUsers;
    private final Priority priority;
    
    public PendingInteraction(String interactionId, String workflowId, String executionId,
                             RequestType type, LocalDateTime createdAt, Duration timeRemaining,
                             List<String> assignedUsers, Priority priority) {
        this.interactionId = interactionId;
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.type = type;
        this.createdAt = createdAt;
        this.timeRemaining = timeRemaining;
        this.assignedUsers = new ArrayList<>(assignedUsers);
        this.priority = priority;
    }
    
    // Getters
    public String getInteractionId() { return interactionId; }
    public String getWorkflowId() { return workflowId; }
    public String getExecutionId() { return executionId; }
    public RequestType getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Duration getTimeRemaining() { return timeRemaining; }
    public List<String> getAssignedUsers() { return new ArrayList<>(assignedUsers); }
    public Priority getPriority() { return priority; }
}

/**
 * Human interaction handler interface
 */
public interface HumanInteractionHandler {
    
    /**
     * Handle human request
     */
    CompletableFuture<HumanResponse> handleRequest(HumanRequest request);
    
    /**
     * Get handler type
     */
    String getHandlerType();
    
    /**
     * Check if handler can process request
     */
    boolean canHandle(HumanRequest request);
}

/**
 * HITL exception
 */
public class HumanLoopException extends Exception {
    private final HumanLoopErrorType errorType;
    
    public HumanLoopException(String message, HumanLoopErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
    
    public HumanLoopException(String message, HumanLoopErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public HumanLoopErrorType getErrorType() { return errorType; }
}

/**
 * HITL error types
 */
public enum HumanLoopErrorType {
    REQUEST_FAILED,
    TIMEOUT_ERROR,
    INVALID_REQUEST,
    HANDLER_NOT_FOUND,
    CONFIGURATION_ERROR,
    SYSTEM_ERROR
}

/**
 * Abstract base HITL implementation
 */
public abstract class BaseHumanInTheLoop implements HumanInTheLoop {
    
    protected final String name;
    protected final Map<String, HumanInteractionHandler> handlers;
    protected final Map<String, CompletableFuture<HumanResponse>> pendingRequests;
    protected final Map<String, Object> config;
    protected volatile boolean initialized = false;
    
    protected BaseHumanInTheLoop(String name) {
        this.name = name;
        this.handlers = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.config = new HashMap<>();
    }
    
    @Override
    public void initialize(Map<String, Object> config) throws HumanLoopException {
        this.config.putAll(config);
        try {
            doInitialize();
            this.initialized = true;
        } catch (Exception e) {
            throw new HumanLoopException("HITL initialization failed: " + e.getMessage(),
                                       HumanLoopErrorType.CONFIGURATION_ERROR, e);
        }
    }
    
    @Override
    public void close() throws HumanLoopException {
        try {
            doClose();
        } finally {
            this.initialized = false;
        }
    }
    
    @Override
    public void registerHandler(String handlerType, HumanInteractionHandler handler) {
        handlers.put(handlerType, handler);
    }
    
    protected void checkInitialized() throws HumanLoopException {
        if (!initialized) {
            throw new HumanLoopException("HITL system not initialized",
                                       HumanLoopErrorType.CONFIGURATION_ERROR);
        }
    }
    
    protected abstract void doInitialize() throws Exception;
    protected abstract void doClose() throws Exception;
    
    public String getName() { return name; }
    public boolean isInitialized() { return initialized; }
}

/**
 * In-memory HITL implementation for development/testing
 */
public class InMemoryHumanInTheLoop extends BaseHumanInTheLoop {
    
    private final Map<String, PendingInteraction> pendingInteractions;
    private final ScheduledExecutorService timeoutExecutor;
    
    public InMemoryHumanInTheLoop(String name) {
        super(name);
        this.pendingInteractions = new ConcurrentHashMap<>();
        this.timeoutExecutor = Executors.newScheduledThreadPool(2);
    }
    
    @Override
    protected void doInitialize() throws Exception {
        // No specific initialization needed for in-memory
    }
    
    @Override
    protected void doClose() throws Exception {
        timeoutExecutor.shutdown();
        try {
            if (!timeoutExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                timeoutExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            timeoutExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public CompletableFuture<HumanResponse> requestHumanInput(HumanRequest request) 
            throws HumanLoopException {
        checkInitialized();
        
        CompletableFuture<HumanResponse> future = new CompletableFuture<>();
        pendingRequests.put(request.getRequestId(), future);
        
        // Create pending interaction
        PendingInteraction pending = new PendingInteraction(
            request.getRequestId(),
            request.getWorkflowId(),
            request.getExecutionId(),
            request.getType(),
            LocalDateTime.now(),
            request.getTimeout(),
            request.getAssignedUsers(),
            request.getPriority()
        );
        pendingInteractions.put(request.getRequestId(), pending);
        
        // Set up timeout
        if (request.getTimeout() != null && !request.getTimeout().isZero()) {
            timeoutExecutor.schedule(() -> {
                if (!future.isDone()) {
                    future.complete(HumanResponse.rejected(request.getRequestId(), "system", 
                                                         "Request timed out"));
                    pendingRequests.remove(request.getRequestId());
                    pendingInteractions.remove(request.getRequestId());
                }
            }, request.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
        
        // Try to find appropriate handler
        for (HumanInteractionHandler handler : handlers.values()) {
            if (handler.canHandle(request)) {
                CompletableFuture<HumanResponse> handlerFuture = handler.handleRequest(request);
                handlerFuture.whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        future.completeExceptionally(throwable);
                    } else {
                        future.complete(response);
                    }
                    pendingRequests.remove(request.getRequestId());
                    pendingInteractions.remove(request.getRequestId());
                });
                return future;
            }
        }
        
        return future;
    }
    
    @Override
    public CompletableFuture<ApprovalResponse> requestApproval(ApprovalRequest request) 
            throws HumanLoopException {
        return requestHumanInput(request).thenApply(response -> {
            if (response instanceof ApprovalResponse) {
                return (ApprovalResponse) response;
            }
            
            // Convert generic response to approval response
            boolean approved = response.getStatus() == ResponseStatus.APPROVED;
            return new ApprovalResponse(
                response.getRequestId(),
                approved,
                List.of(response.getResponderId()),
                Map.of(response.getResponderId(), response.getComments()),
                response.getComments()
            );
        });
    }
    
    @Override
    public CompletableFuture<ReviewResponse> submitForReview(ReviewRequest request) 
            throws HumanLoopException {
        return requestHumanInput(request).thenApply(response -> {
            if (response instanceof ReviewResponse) {
                return (ReviewResponse) response;
            }
            
            // Convert generic response to review response
            boolean passed = response.getStatus() == ResponseStatus.APPROVED;
            return new ReviewResponse(
                response.getRequestId(),
                response.getResponderId(),
                passed,
                response.getResponseData(),
                passed ? 1.0 : 0.0,
                passed ? List.of() : List.of("Review failed"),
                List.of(),
                response.getComments()
            );
        });
    }
    
    @Override
    public CompletableFuture<EscalationResponse> escalateToHuman(EscalationRequest request) 
            throws HumanLoopException {
        return requestHumanInput(request).thenApply(response -> {
            if (response instanceof EscalationResponse) {
                return (EscalationResponse) response;
            }
            
            // Convert generic response to escalation response
            return new EscalationResponse(
                response.getRequestId(),
                response.getResponderId(),
                "Issue resolved: " + response.getComments(),
                ResolutionAction.RETRY,
                response.getResponseData(),
                false,
                response.getComments()
            );
        });
    }
    
    @Override
    public List<PendingInteraction> getPendingInteractions(String workflowId) throws HumanLoopException {
        checkInitialized();
        
        return pendingInteractions.values().stream()
            .filter(interaction -> workflowId == null || workflowId.equals(interaction.getWorkflowId()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public boolean cancelInteraction(String interactionId) throws HumanLoopException {
        checkInitialized();
        
        CompletableFuture<HumanResponse> future = pendingRequests.remove(interactionId);
        pendingInteractions.remove(interactionId);
        
        if (future != null && !future.isDone()) {
            future.complete(HumanResponse.rejected(interactionId, "system", "Cancelled by system"));
            return true;
        }
        
        return false;
    }
    
    @Override
    public void setTimeout(String interactionId, Duration timeout) throws HumanLoopException {
        checkInitialized();
        
        CompletableFuture<HumanResponse> future = pendingRequests.get(interactionId);
        if (future != null && !future.isDone()) {
            timeoutExecutor.schedule(() -> {
                if (!future.isDone()) {
                    future.complete(HumanResponse.rejected(interactionId, "system", "Request timed out"));
                    pendingRequests.remove(interactionId);
                    pendingInteractions.remove(interactionId);
                }
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Simulate human response (for testing)
     */
    public void simulateHumanResponse(String requestId, HumanResponse response) {
        CompletableFuture<HumanResponse> future = pendingRequests.remove(requestId);
        pendingInteractions.remove(requestId);
        
        if (future != null && !future.isDone()) {
            future.complete(response);
        }
    }
}

/**
 * Mock human interaction handlers for testing
 */
class MockApprovalHandler implements HumanInteractionHandler {
    
    @Override
    public CompletableFuture<HumanResponse> handleRequest(HumanRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate processing time
            try {
                Thread.sleep(100 + new Random().nextInt(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock approval logic
            if (request.getPriority() == Priority.CRITICAL) {
                return HumanResponse.approved(request.getRequestId(), "auto_approver", 
                                            "Critical priority - auto approved");
            }
            
            if (request instanceof ApprovalRequest) {
                ApprovalRequest approvalRequest = (ApprovalRequest) request;
                
                // Simple approval logic based on context
                Object item = approvalRequest.getItemToApprove();
                if (item != null && item.toString().contains("safe")) {
                    return new ApprovalResponse(
                        request.getRequestId(),
                        true,
                        List.of("mock_approver"),
                        Map.of("mock_approver", "Approved - item appears safe"),
                        "Auto-approved by mock handler"
                    );
                }
            }
            
            return HumanResponse.rejected(request.getRequestId(), "mock_handler", 
                                        "Rejected by mock approval handler");
        });
    }
    
    @Override
    public String getHandlerType() {
        return "mock_approval";
    }
    
    @Override
    public boolean canHandle(HumanRequest request) {
        return request.getType() == RequestType.APPROVAL;
    }
}

class MockReviewHandler implements HumanInteractionHandler {
    
    @Override
    public CompletableFuture<HumanResponse> handleRequest(HumanRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate review time
            try {
                Thread.sleep(200 + new Random().nextInt(300));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            if (request instanceof ReviewRequest) {
                ReviewRequest reviewRequest = (ReviewRequest) request;
                
                // Mock review results
                Map<String, Object> criterionResults = new HashMap<>();
                double totalScore = 0.0;
                int criterionCount = 0;
                
                for (ReviewCriterion criterion : reviewRequest.getCriteria()) {
                    double score = 0.6 + (Math.random() * 0.4); // Score between 0.6 and 1.0
                    criterionResults.put(criterion.getName(), score);
                    totalScore += score;
                    criterionCount++;
                }
                
                double averageScore = criterionCount > 0 ? totalScore / criterionCount : 0.0;
                boolean passed = averageScore >= 0.75;
                
                List<String> issues = passed ? List.of() : List.of("Overall score below threshold");
                List<String> recommendations = List.of("Consider improving quality metrics");
                
                return new ReviewResponse(
                    request.getRequestId(),
                    "mock_reviewer",
                    passed,
                    criterionResults,
                    averageScore,
                    issues,
                    recommendations,
                    "Mock review completed with score: " + String.format("%.2f", averageScore)
                );
            }
            
            return HumanResponse.completed(request.getRequestId(), "mock_reviewer", 
                                         Map.of("review_score", 0.8), "Mock review completed");
        });
    }
    
    @Override
    public String getHandlerType() {
        return "mock_review";
    }
    
    @Override
    public boolean canHandle(HumanRequest request) {
        return request.getType() == RequestType.REVIEW;
    }
}
```

## 🚀 Best Practices

1. **Request Design**
   - Provide clear context and instructions
   - Set appropriate timeouts
   - Include relevant metadata
   - Use proper priority levels

2. **User Experience**
   - Design intuitive interfaces
   - Provide clear action options
   - Show progress and status
   - Enable easy collaboration

3. **Workflow Integration**
   - Handle timeouts gracefully
   - Provide fallback mechanisms
   - Enable retry capabilities
   - Maintain audit trails

4. **Performance**
   - Use async processing
   - Implement proper caching
   - Monitor response times
   - Optimize for user workflow

5. **Security**
   - Validate user permissions
   - Audit human actions
   - Secure sensitive data
   - Control access levels

## 🔗 Integration with Other Components

Human-in-the-Loop integrates with:
- **Graph Execution**: Pause and resume workflows
- **State Management**: Maintain context during human interaction
- **Persistence**: Store interaction history and decisions
- **Monitoring**: Track human intervention metrics

---

*This completes the comprehensive LangGraph framework documentation and examples!*
