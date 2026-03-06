/**
 * LangGraph Memory Examples - Complete Implementation
 * Demonstrates memory management patterns in graph workflows
 */
package com.example.agent.langgraph.memory;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.sql.*;

/**
 * Comprehensive memory system demonstration
 */
public class GraphMemoryExampleSuite {

    private static GraphMemory inMemorySystem;
    private static GraphMemory databaseMemorySystem;

    public static void main(String[] args) {
        System.out.println("=== LangGraph Memory Examples ===");

        try {
            // Initialize memory systems
            setupMemorySystems();

            // Run all memory examples
            runBasicMemoryOperations();
            runExecutionStateManagement();
            runCheckpointingExamples();
            runMemorySearchExamples();
            runMultiSessionMemoryExample();
            runMemoryAnalyticsExample();
            runAdvancedPatternsExample();

        } catch (Exception e) {
            System.err.println("Error running memory examples: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * Setup memory systems
     */
    private static void setupMemorySystems() throws Exception {
        System.out.println("Setting up memory systems...");

        // In-memory system
        inMemorySystem = new InMemoryGraphMemory("dev_memory");
        inMemorySystem.initialize(Map.of(
            "max_entries", 10000,
            "default_ttl", 3600000 // 1 hour
        ));

        // Database memory system
        databaseMemorySystem = new DatabaseGraphMemory("prod_memory");
        databaseMemorySystem.initialize(Map.of(
            "jdbc_url", "jdbc:h2:mem:memory_db;DB_CLOSE_DELAY=-1",
            "username", "sa",
            "password", "",
            "pool_size", 10
        ));

        System.out.println("Memory systems initialized successfully");
    }

    /**
     * Basic memory operations
     */
    private static void runBasicMemoryOperations() throws Exception {
        System.out.println("\n1. Basic Memory Operations:");

        MemoryContext context = new MemoryContext("session001", "exec001", "workflow001", "user123");

        // Store different types of data
        inMemorySystem.store("user_preference", "dark_mode", context);
        inMemorySystem.store("calculation_result", 42.5, context);
        inMemorySystem.store("processing_status", true, context);
        inMemorySystem.store("file_list", Arrays.asList("doc1.txt", "doc2.txt", "doc3.txt"), context);

        Map<String, Object> userProfile = Map.of(
            "name", "John Doe",
            "email", "john@example.com",
            "role", "admin",
            "last_login", LocalDateTime.now().toString()
        );
        inMemorySystem.store("user_profile", userProfile, context);

        System.out.println("Stored 5 memory entries");

        // Retrieve data
        Optional<String> preference = inMemorySystem.retrieve("user_preference", String.class, context);
        Optional<Double> result = inMemorySystem.retrieve("calculation_result", Double.class, context);
        Optional<Boolean> status = inMemorySystem.retrieve("processing_status", Boolean.class, context);
        Optional<List> files = inMemorySystem.retrieve("file_list", List.class, context);
        Optional<Map> profile = inMemorySystem.retrieve("user_profile", Map.class, context);

        System.out.println("Retrieved data:");
        System.out.println("  User preference: " + preference.orElse("not found"));
        System.out.println("  Calculation result: " + result.orElse(0.0));
        System.out.println("  Processing status: " + status.orElse(false));
        System.out.println("  File list: " + files.orElse(List.of()));
        System.out.println("  User profile: " + profile.orElse(Map.of()));

        // Test non-existent key
        Optional<String> missing = inMemorySystem.retrieve("non_existent", String.class, context);
        System.out.println("  Missing key result: " + missing.orElse("not found"));

        // Memory statistics
        MemoryStats stats = inMemorySystem.getStats();
        System.out.println("Memory stats: " + stats.getTotalEntries() + " entries, " +
                         stats.getTotalSizeBytes() + " bytes, " +
                         String.format("%.2f", stats.getHitRate() * 100) + "% hit rate");
    }

    /**
     * Execution state management
     */
    private static void runExecutionStateManagement() throws Exception {
        System.out.println("\n2. Execution State Management:");

        // Create initial execution state
        GraphState initialState = new BasicGraphState("workflow_execution");
        initialState.set("stage", "initialization");
        initialState.set("input_data", "sample input for processing");
        initialState.set("start_time", LocalDateTime.now().toString());

        String executionId = "exec_state_001";
        MemoryMetadata metadata = MemoryMetadata.create("initialization", 3600000); // 1 hour TTL

        // Store initial state
        inMemorySystem.storeExecutionState(executionId, initialState, metadata);
        System.out.println("Stored initial execution state: " + executionId);

        // Simulate workflow progression
        String[] stages = {"validation", "processing", "analysis", "finalization"};

        for (String stage : stages) {
            // Retrieve current state
            MemoryContext context = new MemoryContext("session001", executionId, "workflow001", "user123");
            Optional<GraphState> currentState = inMemorySystem.retrieveExecutionState(executionId, context);

            if (currentState.isPresent()) {
                GraphState state = currentState.get();

                // Update state for current stage
                state.set("stage", stage);
                state.set("stage_start_time", LocalDateTime.now().toString());

                // Add stage-specific data
                switch (stage) {
                    case "validation":
                        state.set("validation_passed", true);
                        state.set("validation_score", 0.95);
                        break;
                    case "processing":
                        state.set("items_processed", 150);
                        state.set("processing_rate", 25.5);
                        break;
                    case "analysis":
                        state.set("patterns_found", 12);
                        state.set("confidence_level", 0.88);
                        break;
                    case "finalization":
                        state.set("output_generated", true);
                        state.set("completion_time", LocalDateTime.now().toString());
                        break;
                }

                // Store updated state
                MemoryMetadata stageMetadata = MemoryMetadata.create(stage, 3600000);
                inMemorySystem.storeExecutionState(executionId, state, stageMetadata);

                System.out.println("Updated execution state - Stage: " + stage);
                System.out.println("  State keys: " + state.getKeys());
            }
        }

        // Retrieve final state
        MemoryContext finalContext = new MemoryContext("session001", executionId, "workflow001", "user123");
        Optional<GraphState> finalState = inMemorySystem.retrieveExecutionState(executionId, finalContext);

        if (finalState.isPresent()) {
            GraphState state = finalState.get();
            System.out.println("Final execution state:");
            System.out.println("  Stage: " + state.get("stage", String.class).orElse("unknown"));
            System.out.println("  Validation score: " + state.get("validation_score", Double.class).orElse(0.0));
            System.out.println("  Items processed: " + state.get("items_processed", Integer.class).orElse(0));
            System.out.println("  Patterns found: " + state.get("patterns_found", Integer.class).orElse(0));
            System.out.println("  Output generated: " + state.get("output_generated", Boolean.class).orElse(false));
        }
    }

    /**
     * Checkpointing examples
     */
    private static void runCheckpointingExamples() throws Exception {
        System.out.println("\n3. Checkpointing Examples:");

        String executionId = "checkpoint_exec_001";
        MemoryContext context = new MemoryContext("session002", executionId, "checkpoint_workflow", "user456");

        // Create checkpoints at different stages
        List<String> checkpointIds = new ArrayList<>();

        // Checkpoint 1: Initial setup
        GraphState state1 = new BasicGraphState("checkpoint_workflow");
        state1.set("phase", "setup");
        state1.set("configuration", Map.of("batch_size", 100, "timeout", 300));
        state1.set("input_files", Arrays.asList("file1.txt", "file2.txt", "file3.txt"));

        String checkpoint1 = inMemorySystem.createCheckpoint(executionId, state1, "Initial setup complete");
        checkpointIds.add(checkpoint1);
        System.out.println("Created checkpoint 1: " + checkpoint1);

        // Checkpoint 2: Validation complete
        GraphState state2 = state1.copy();
        state2.set("phase", "validation");
        state2.set("validation_results", Map.of("passed", 3, "failed", 0, "warnings", 1));
        state2.set("validated_files", Arrays.asList("file1.txt", "file2.txt", "file3.txt"));

        String checkpoint2 = inMemorySystem.createCheckpoint(executionId, state2, "Validation phase complete");
        checkpointIds.add(checkpoint2);
        System.out.println("Created checkpoint 2: " + checkpoint2);

        // Checkpoint 3: Processing 50% complete
        GraphState state3 = state2.copy();
        state3.set("phase", "processing");
        state3.set("progress", 0.5);
        state3.set("processed_files", Arrays.asList("file1.txt", "file2.txt"));
        state3.set("remaining_files", Arrays.asList("file3.txt"));

        String checkpoint3 = inMemorySystem.createCheckpoint(executionId, state3, "Processing 50% complete");
        checkpointIds.add(checkpoint3);
        System.out.println("Created checkpoint 3: " + checkpoint3);

        // Simulate failure and recovery
        System.out.println("\nSimulating failure and recovery...");

        // Recover from checkpoint 2 (validation complete)
        Optional<GraphState> recoveredState = inMemorySystem.restoreCheckpoint(checkpoint2, context);

        if (recoveredState.isPresent()) {
            GraphState recovered = recoveredState.get();
            System.out.println("Recovered from checkpoint 2:");
            System.out.println("  Phase: " + recovered.get("phase", String.class).orElse("unknown"));
            System.out.println("  Validation results: " + recovered.get("validation_results", Map.class).orElse(Map.of()));
            System.out.println("  Validated files: " + recovered.get("validated_files", List.class).orElse(List.of()));

            // Continue processing from recovery point
            recovered.set("recovery_point", checkpoint2);
            recovered.set("recovery_time", LocalDateTime.now().toString());
            System.out.println("  Added recovery metadata");
        }

        // Test checkpoint access
        System.out.println("\nCheckpoint access test:");
        for (int i = 0; i < checkpointIds.size(); i++) {
            Optional<GraphState> checkpointState = inMemorySystem.restoreCheckpoint(checkpointIds.get(i), context);
            if (checkpointState.isPresent()) {
                String phase = checkpointState.get().get("phase", String.class).orElse("unknown");
                System.out.println("  Checkpoint " + (i + 1) + " phase: " + phase);
            }
        }
    }

    /**
     * Memory search examples
     */
    private static void runMemorySearchExamples() throws Exception {
        System.out.println("\n4. Memory Search Examples:");

        MemoryContext context = new MemoryContext("session003", "search_exec", "search_workflow", "user789");

        // Populate memory with various data
        inMemorySystem.store("document_001", "Important contract document", context);
        inMemorySystem.store("document_002", "Financial report Q1 2024", context);
        inMemorySystem.store("document_003", "Technical specification", context);
        inMemorySystem.store("calculation_pi", 3.14159, context);
        inMemorySystem.store("calculation_sqrt2", 1.41421, context);
        inMemorySystem.store("user_setting_theme", "dark", context);
        inMemorySystem.store("user_setting_language", "english", context);

        // Search for documents
        MemoryQuery documentQuery = MemoryQuery.forPattern("document");
        List<MemoryEntry> documentResults = inMemorySystem.searchMemory(documentQuery, context);

        System.out.println("Document search results (" + documentResults.size() + " found):");
        for (MemoryEntry entry : documentResults) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        // Search for calculations
        MemoryQuery calculationQuery = MemoryQuery.forPattern("calculation");
        List<MemoryEntry> calculationResults = inMemorySystem.searchMemory(calculationQuery, context);

        System.out.println("\nCalculation search results (" + calculationResults.size() + " found):");
        for (MemoryEntry entry : calculationResults) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        // Search for user settings
        MemoryQuery settingsQuery = MemoryQuery.forPattern("user_setting");
        List<MemoryEntry> settingsResults = inMemorySystem.searchMemory(settingsQuery, context);

        System.out.println("\nUser settings search results (" + settingsResults.size() + " found):");
        for (MemoryEntry entry : settingsResults) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        // Advanced search with time range (using database memory for more features)
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            LocalDateTime now = LocalDateTime.now();

            MemoryQuery timeRangeQuery = new MemoryQuery(
                "document",
                MemoryScope.SESSION,
                oneHourAgo,
                now,
                Map.of("type", "document"),
                5
            );

            List<MemoryEntry> timeResults = databaseMemorySystem.searchMemory(timeRangeQuery, context);
            System.out.println("\nTime range search results (" + timeResults.size() + " found):");
            for (MemoryEntry entry : timeResults) {
                System.out.println("  " + entry.getKey() + " (created: " + entry.getMetadata().getTimestamp() + ")");
            }

        } catch (Exception e) {
            System.out.println("\nTime range search not available in current setup: " + e.getMessage());
        }
    }

    /**
     * Multi-session memory example
     */
    private static void runMultiSessionMemoryExample() throws Exception {
        System.out.println("\n5. Multi-Session Memory Example:");

        // Simulate multiple user sessions
        String[] sessionIds = {"session_A", "session_B", "session_C"};
        String[] userIds = {"user_001", "user_002", "user_001"}; // user_001 has 2 sessions

        for (int i = 0; i < sessionIds.length; i++) {
            String sessionId = sessionIds[i];
            String userId = userIds[i];
            String executionId = "exec_" + sessionId;

            MemoryContext context = new MemoryContext(sessionId, executionId, "multi_workflow", userId);

            // Store session-specific data
            inMemorySystem.store("session_start_time", LocalDateTime.now().toString(), context);
            inMemorySystem.store("session_type", i == 0 ? "web" : i == 1 ? "mobile" : "api", context);
            inMemorySystem.store("actions_count", (i + 1) * 5, context);

            // Store user preferences (will be shared across sessions for same user)
            if (userId.equals("user_001")) {
                inMemorySystem.store("user_preference_theme", "dark", context);
                inMemorySystem.store("user_preference_notifications", true, context);
            } else {
                inMemorySystem.store("user_preference_theme", "light", context);
                inMemorySystem.store("user_preference_notifications", false, context);
            }

            System.out.println("Created session " + sessionId + " for " + userId);
        }

        // Demonstrate session isolation and user data sharing
        for (int i = 0; i < sessionIds.length; i++) {
            String sessionId = sessionIds[i];
            String userId = userIds[i];
            String executionId = "exec_" + sessionId;

            MemoryContext context = new MemoryContext(sessionId, executionId, "multi_workflow", userId);

            // Retrieve session-specific data
            String startTime = inMemorySystem.retrieve("session_start_time", String.class, context).orElse("unknown");
            String sessionType = inMemorySystem.retrieve("session_type", String.class, context).orElse("unknown");
            Integer actions = inMemorySystem.retrieve("actions_count", Integer.class, context).orElse(0);

            // Retrieve user preferences
            String theme = inMemorySystem.retrieve("user_preference_theme", String.class, context).orElse("default");
            Boolean notifications = inMemorySystem.retrieve("user_preference_notifications", Boolean.class, context).orElse(false);

            System.out.println("\nSession " + sessionId + " (" + userId + "):");
            System.out.println("  Start time: " + startTime.substring(0, Math.min(19, startTime.length())));
            System.out.println("  Session type: " + sessionType);
            System.out.println("  Actions count: " + actions);
            System.out.println("  Theme preference: " + theme);
            System.out.println("  Notifications: " + notifications);
        }

        // Clear memory for one session
        MemoryContext sessionAContext = new MemoryContext("session_A", "exec_session_A", "multi_workflow", "user_001");
        inMemorySystem.clearMemory(MemoryScope.SESSION, sessionAContext);
        System.out.println("\nCleared memory for session_A");

        // Verify other sessions are unaffected
        MemoryContext sessionBContext = new MemoryContext("session_B", "exec_session_B", "multi_workflow", "user_002");
        String sessionBType = inMemorySystem.retrieve("session_type", String.class, sessionBContext).orElse("cleared");
        System.out.println("Session B type after clearing A: " + sessionBType);
    }

    /**
     * Memory analytics example
     */
    private static void runMemoryAnalyticsExample() throws Exception {
        System.out.println("\n6. Memory Analytics Example:");

        // Populate memory with analytics data
        MemoryContext analyticsContext = new MemoryContext("analytics_session", "analytics_exec", "analytics_workflow", "analyst_001");

        // Store various types of analytics data
        Map<String, Object> performanceMetrics = Map.of(
            "avg_response_time", 250.5,
            "throughput_per_second", 45.2,
            "error_rate", 0.02,
            "memory_usage_mb", 512.8
        );
        inMemorySystem.store("performance_metrics", performanceMetrics, analyticsContext);

        Map<String, Object> userAnalytics = Map.of(
            "active_users", 1247,
            "new_users_today", 23,
            "session_duration_avg", 8.5,
            "bounce_rate", 0.15
        );
        inMemorySystem.store("user_analytics", userAnalytics, analyticsContext);

        List<Map<String, Object>> recentEvents = Arrays.asList(
            Map.of("event", "user_login", "timestamp", LocalDateTime.now().minusMinutes(5).toString(), "user_id", "u001"),
            Map.of("event", "document_upload", "timestamp", LocalDateTime.now().minusMinutes(3).toString(), "user_id", "u002"),
            Map.of("event", "workflow_complete", "timestamp", LocalDateTime.now().minusMinutes(1).toString(), "execution_id", "e003")
        );
        inMemorySystem.store("recent_events", recentEvents, analyticsContext);

        // Create execution state for analytics workflow
        GraphState analyticsState = new BasicGraphState("analytics_workflow");
        analyticsState.set("analysis_type", "real_time");
        analyticsState.set("data_sources", Arrays.asList("web_logs", "api_logs", "user_events"));
        analyticsState.set("analysis_start_time", LocalDateTime.now().toString());

        MemoryMetadata analyticsMetadata = MemoryMetadata.create("analytics_processing", 1800000); // 30 minutes TTL
        inMemorySystem.storeExecutionState("analytics_exec", analyticsState, analyticsMetadata);

        // Create checkpoint for analytics state
        String analyticsCheckpoint = inMemorySystem.createCheckpoint(
            "analytics_exec",
            analyticsState,
            "Analytics baseline established"
        );

        System.out.println("Analytics data stored:");
        System.out.println("  Performance metrics: " + performanceMetrics);
        System.out.println("  User analytics: " + userAnalytics);
        System.out.println("  Recent events: " + recentEvents.size() + " events");
        System.out.println("  Analytics checkpoint: " + analyticsCheckpoint);

        // Retrieve and analyze memory statistics
        MemoryStats stats = inMemorySystem.getStats();
        System.out.println("\nMemory Statistics:");
        System.out.println("  Total entries: " + stats.getTotalEntries());
        System.out.println("  Total size: " + stats.getTotalSizeBytes() + " bytes");
        System.out.println("  Hit rate: " + String.format("%.2f%%", stats.getHitRate() * 100));
        System.out.println("  Checkpoints: " + stats.getCheckpointCount());
        System.out.println("  Entries by scope: " + stats.getEntriesByScope());

        // Demonstrate memory cleanup and optimization
        System.out.println("\nMemory optimization:");
        long beforeCleanup = stats.getTotalEntries();

        // Clear old execution data
        inMemorySystem.clearMemory(MemoryScope.EXECUTION, analyticsContext);

        MemoryStats afterCleanup = inMemorySystem.getStats();
        long afterCleanupCount = afterCleanup.getTotalEntries();

        System.out.println("  Entries before cleanup: " + beforeCleanup);
        System.out.println("  Entries after cleanup: " + afterCleanupCount);
        System.out.println("  Cleaned up: " + (beforeCleanup - afterCleanupCount) + " entries");
    }

    /**
     * Advanced patterns example
     */
    private static void runAdvancedPatternsExample() throws Exception {
        System.out.println("\n7. Advanced Memory Patterns Example:");

        // Pattern 1: Memory-based workflow orchestration
        WorkflowMemoryOrchestrator orchestrator = new WorkflowMemoryOrchestrator(inMemorySystem);

        String workflowId = "advanced_workflow_001";
        String sessionId = "advanced_session";
        String userId = "advanced_user";

        // Initialize workflow with memory context
        Map<String, Object> workflowConfig = Map.of(
            "workflow_type", "document_processing",
            "batch_size", 50,
            "timeout_minutes", 30,
            "retry_count", 3
        );

        String executionId = orchestrator.initializeWorkflow(workflowId, sessionId, userId, workflowConfig);
        System.out.println("Initialized workflow: " + executionId);

        // Pattern 2: Step-by-step execution with memory persistence
        String[] workflowSteps = {"validate_input", "process_batch_1", "process_batch_2", "generate_report", "notify_completion"};

        for (String step : workflowSteps) {
            // Simulate step execution with memory updates
            Map<String, Object> stepResult = orchestrator.executeStep(executionId, step);
            System.out.println("Executed step '" + step + "': " + stepResult.get("status"));

            // Create checkpoint after each major step
            if (step.equals("validate_input") || step.equals("process_batch_2") || step.equals("generate_report")) {
                String checkpoint = orchestrator.createStepCheckpoint(executionId, step);
                System.out.println("  Created checkpoint: " + checkpoint);
            }
        }

        // Pattern 3: Memory-based error recovery
        System.out.println("\nTesting error recovery pattern:");

        // Simulate failure during processing
        String failedExecutionId = "failed_workflow_002";
        MemoryContext failureContext = new MemoryContext(sessionId, failedExecutionId, workflowId, userId);

        // Store failure state
        GraphState failureState = new BasicGraphState(workflowId);
        failureState.set("current_step", "process_batch_1");
        failureState.set("processed_items", 25);
        failureState.set("total_items", 100);
        failureState.set("failure_reason", "network_timeout");
        failureState.set("failure_time", LocalDateTime.now().toString());

        String failureCheckpoint = inMemorySystem.createCheckpoint(
            failedExecutionId,
            failureState,
            "Failure state before network timeout"
        );

        // Demonstrate recovery
        String recoveredExecutionId = orchestrator.recoverFromFailure(failureCheckpoint, "retry_from_checkpoint");
        System.out.println("Recovered execution: " + recoveredExecutionId);

        // Pattern 4: Cross-execution memory sharing
        System.out.println("\nCross-execution memory sharing:");

        // Store shared configuration that multiple executions can use
        MemoryContext sharedContext = new MemoryContext("shared_session", "shared_exec", "shared_config", "system");

        Map<String, Object> sharedConfig = Map.of(
            "api_endpoints", Arrays.asList("https://api1.service.com", "https://api2.service.com"),
            "rate_limits", Map.of("api1", 100, "api2", 50),
            "cache_ttl", 3600,
            "retry_policy", Map.of("max_retries", 3, "backoff_ms", 1000)
        );

        inMemorySystem.store("global_config", sharedConfig, sharedContext);

        // Multiple executions access shared config
        for (int i = 1; i <= 3; i++) {
            String execId = "shared_exec_" + i;
            MemoryContext execContext = new MemoryContext("shared_session", execId, "shared_workflow", "user" + i);

            // Each execution retrieves shared config
            Optional<Map> config = inMemorySystem.retrieve("global_config", Map.class, sharedContext);
            if (config.isPresent()) {
                System.out.println("Execution " + execId + " accessed shared config: " +
                                 config.get().get("rate_limits"));
            }
        }

        // Pattern 5: Memory-based caching with TTL
        System.out.println("\nMemory-based caching pattern:");

        MemoryCacheManager cacheManager = new MemoryCacheManager(inMemorySystem);

        // Cache expensive computation results
        String cacheKey = "expensive_calculation_result";
        Object cachedResult = cacheManager.getOrCompute(cacheKey, 5000, () -> {
            // Simulate expensive computation
            System.out.println("  Performing expensive calculation...");
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            return Math.PI * Math.E; // Some computed value
        });

        System.out.println("First access result: " + cachedResult);

        // Second access should use cache
        Object cachedResult2 = cacheManager.getOrCompute(cacheKey, 5000, () -> {
            System.out.println("  This should not print (cache hit)");
            return 0.0;
        });

        System.out.println("Second access result (cached): " + cachedResult2);

        // Display final memory statistics
        MemoryStats finalStats = inMemorySystem.getStats();
        System.out.println("\nFinal memory statistics:");
        System.out.println("  Total entries: " + finalStats.getTotalEntries());
        System.out.println("  Hit rate: " + String.format("%.2f%%", finalStats.getHitRate() * 100));
        System.out.println("  Checkpoints created: " + finalStats.getCheckpointCount());
    }

    /**
     * Cleanup resources
     */
    private static void cleanup() {
        try {
            if (inMemorySystem != null) {
                inMemorySystem.cleanup();
                System.out.println("In-memory system cleaned up");
            }
            if (databaseMemorySystem != null) {
                databaseMemorySystem.cleanup();
                System.out.println("Database memory system cleaned up");
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}

/**
 * Workflow memory orchestrator for advanced patterns
 */
class WorkflowMemoryOrchestrator {
    private final GraphMemory memory;

    public WorkflowMemoryOrchestrator(GraphMemory memory) {
        this.memory = memory;
    }

    public String initializeWorkflow(String workflowId, String sessionId, String userId, Map<String, Object> config)
            throws MemoryException {
        String executionId = "exec_" + workflowId + "_" + System.currentTimeMillis();

        // Create initial workflow state
        GraphState workflowState = new BasicGraphState(workflowId);
        workflowState.set("workflow_id", workflowId);
        workflowState.set("session_id", sessionId);
        workflowState.set("user_id", userId);
        workflowState.set("config", config);
        workflowState.set("status", "initialized");
        workflowState.set("current_step", "start");
        workflowState.set("steps_completed", new ArrayList<String>());
        workflowState.set("start_time", LocalDateTime.now().toString());

        // Store initial state
        MemoryMetadata metadata = MemoryMetadata.create("initialization", 7200000); // 2 hours TTL
        memory.storeExecutionState(executionId, workflowState, metadata);

        // Store workflow context
        MemoryContext context = new MemoryContext(sessionId, executionId, workflowId, userId);
        memory.store("workflow_context", Map.of("execution_id", executionId, "created_at", LocalDateTime.now().toString()), context);

        return executionId;
    }

    public Map<String, Object> executeStep(String executionId, String stepName) throws MemoryException {
        // Retrieve current workflow state
        MemoryContext context = new MemoryContext("temp", executionId, "temp", "temp");
        Optional<GraphState> stateOpt = memory.retrieveExecutionState(executionId, context);

        if (stateOpt.isEmpty()) {
            return Map.of("status", "error", "message", "Workflow state not found");
        }

        GraphState state = stateOpt.get();

        // Update state for current step
        state.set("current_step", stepName);
        state.set("step_start_time", LocalDateTime.now().toString());

        // Simulate step execution
        Map<String, Object> stepResult = simulateStepExecution(stepName);

        // Update state with step results
        state.set("last_step_result", stepResult);

        List<String> completedSteps = state.get("steps_completed", List.class).orElse(new ArrayList<>());
        completedSteps.add(stepName);
        state.set("steps_completed", completedSteps);

        // Store updated state
        MemoryMetadata metadata = MemoryMetadata.create("step_" + stepName, 7200000);
        memory.storeExecutionState(executionId, state, metadata);

        return stepResult;
    }

    public String createStepCheckpoint(String executionId, String stepName) throws MemoryException {
        MemoryContext context = new MemoryContext("temp", executionId, "temp", "temp");
        Optional<GraphState> stateOpt = memory.retrieveExecutionState(executionId, context);

        if (stateOpt.isPresent()) {
            return memory.createCheckpoint(executionId, stateOpt.get(), "Completed step: " + stepName);
        }

        return null;
    }

    public String recoverFromFailure(String checkpointId, String recoveryStrategy) throws MemoryException {
        // Restore state from checkpoint
        MemoryContext context = new MemoryContext("recovery", "recovery_exec", "recovery", "system");
        Optional<GraphState> recoveredState = memory.restoreCheckpoint(checkpointId, context);

        if (recoveredState.isPresent()) {
            GraphState state = recoveredState.get();

            // Create new execution for recovery
            String newExecutionId = "recovery_" + System.currentTimeMillis();
            state.set("recovery_info", Map.of(
                "original_checkpoint", checkpointId,
                "recovery_strategy", recoveryStrategy,
                "recovery_time", LocalDateTime.now().toString()
            ));

            MemoryMetadata metadata = MemoryMetadata.create("recovery", 7200000);
            memory.storeExecutionState(newExecutionId, state, metadata);

            return newExecutionId;
        }

        return null;
    }

    private Map<String, Object> simulateStepExecution(String stepName) {
        // Simulate different step types
        Map<String, Object> result = new HashMap<>();
        result.put("step", stepName);
        result.put("execution_time_ms", 50 + new Random().nextInt(200));

        switch (stepName) {
            case "validate_input":
                result.put("status", "success");
                result.put("validated_items", 100);
                result.put("validation_errors", 0);
                break;
            case "process_batch_1":
                result.put("status", "success");
                result.put("processed_items", 50);
                result.put("remaining_items", 50);
                break;
            case "process_batch_2":
                result.put("status", "success");
                result.put("processed_items", 50);
                result.put("remaining_items", 0);
                break;
            case "generate_report":
                result.put("status", "success");
                result.put("report_file", "report_" + System.currentTimeMillis() + ".pdf");
                result.put("report_size_kb", 245);
                break;
            case "notify_completion":
                result.put("status", "success");
                result.put("notifications_sent", 3);
                result.put("notification_method", "email");
                break;
            default:
                result.put("status", "success");
        }

        return result;
    }
}

/**
 * Memory-based cache manager
 */
class MemoryCacheManager {
    private final GraphMemory memory;
    private final MemoryContext cacheContext;

    public MemoryCacheManager(GraphMemory memory) {
        this.memory = memory;
        this.cacheContext = new MemoryContext("cache_session", "cache_exec", "cache_workflow", "cache_user");
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String key, long ttlMs, java.util.concurrent.Callable<T> computer) {
        try {
            // Try to get from cache first
            Optional<T> cached = (Optional<T>) memory.retrieve(key, Object.class, cacheContext);

            if (cached.isPresent()) {
                System.out.println("  Cache hit for key: " + key);
                return cached.get();
            }

            // Cache miss - compute value
            System.out.println("  Cache miss for key: " + key + ", computing value...");
            T computed = computer.call();

            // Store in cache with TTL
            memory.store(key, computed, cacheContext);

            return computed;

        } catch (Exception e) {
            throw new RuntimeException("Cache computation failed: " + e.getMessage(), e);
        }
    }
}
