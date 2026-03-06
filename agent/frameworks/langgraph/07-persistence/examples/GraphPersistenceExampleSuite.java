/**
 * LangGraph Persistence Examples - Complete Implementation
 * Demonstrates enterprise-grade persistence patterns for graph workflows
 */
package com.example.agent.langgraph.persistence;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.sql.*;

/**
 * Comprehensive persistence system demonstration
 */
public class GraphPersistenceExampleSuite {

    private static GraphPersistence databasePersistence;
    private static WorkflowOrchestrator orchestrator;

    public static void main(String[] args) {
        System.out.println("=== LangGraph Persistence Examples ===");

        try {
            // Initialize persistence systems
            setupPersistenceSystem();

            // Run comprehensive persistence examples
            runBasicPersistenceOperations();
            runCheckpointingWorkflows();
            runWorkflowDefinitionManagement();
            runExecutionTrackingExamples();
            runConfigurationManagement();
            runProductionRecoveryScenarios();
            runAdvancedPersistencePatterns();

        } catch (Exception e) {
            System.err.println("Error running persistence examples: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * Setup persistence system
     */
    private static void setupPersistenceSystem() throws Exception {
        System.out.println("Setting up persistence system...");

        databasePersistence = new DatabaseGraphPersistence("production_persistence");
        databasePersistence.initialize(Map.of(
            "jdbc_url", "jdbc:h2:mem:persistence_db;DB_CLOSE_DELAY=-1",
            "username", "sa",
            "password", "",
            "pool_size", 20,
            "compression_enabled", true
        ));

        orchestrator = new WorkflowOrchestrator(databasePersistence);

        System.out.println("Persistence system initialized successfully");
    }

    /**
     * Basic persistence operations
     */
    private static void runBasicPersistenceOperations() throws Exception {
        System.out.println("\n1. Basic Persistence Operations:");

        String workflowId = "document_processing_workflow";
        String executionId = "exec_001_" + System.currentTimeMillis();

        // Create workflow state
        GraphState workflowState = new BasicGraphState(workflowId);
        workflowState.set("stage", "initialization");
        workflowState.set("input_documents", Arrays.asList("doc1.pdf", "doc2.pdf", "doc3.pdf"));
        workflowState.set("processing_config", Map.of(
            "batch_size", 10,
            "quality_threshold", 0.8,
            "output_format", "json"
        ));
        workflowState.set("started_at", LocalDateTime.now().toString());

        // Persist initial state
        PersistenceMetadata metadata = new PersistenceMetadata(
            "initialization",
            Map.of("workflow_type", "document_processing", "priority", "high"),
            7, // 7 days retention
            PersistenceLevel.NORMAL
        );

        databasePersistence.persistState(workflowId, executionId, workflowState, metadata);
        System.out.println("Persisted initial state for execution: " + executionId);

        // Simulate workflow progression
        String[] stages = {"validation", "preprocessing", "analysis", "output_generation"};

        for (String stage : stages) {
            // Restore current state
            Optional<PersistedState> restoredState = databasePersistence.restoreState(workflowId, executionId);

            if (restoredState.isPresent()) {
                GraphState currentState = restoredState.get().getState();

                // Update state for new stage
                currentState.set("stage", stage);
                currentState.set(stage + "_started_at", LocalDateTime.now().toString());

                // Add stage-specific data
                switch (stage) {
                    case "validation":
                        currentState.set("validation_results", Map.of("passed", 3, "failed", 0));
                        break;
                    case "preprocessing":
                        currentState.set("preprocessed_documents", Arrays.asList("doc1_clean.pdf", "doc2_clean.pdf", "doc3_clean.pdf"));
                        break;
                    case "analysis":
                        currentState.set("analysis_results", Map.of("sentiment_score", 0.75, "key_topics", Arrays.asList("finance", "technology")));
                        break;
                    case "output_generation":
                        currentState.set("output_files", Arrays.asList("summary.json", "report.pdf"));
                        currentState.set("completed_at", LocalDateTime.now().toString());
                        break;
                }

                // Persist updated state
                PersistenceMetadata stageMetadata = new PersistenceMetadata(
                    stage,
                    Map.of("stage_duration_estimate", "5min"),
                    7,
                    PersistenceLevel.NORMAL
                );

                databasePersistence.persistState(workflowId, executionId, currentState, stageMetadata);
                System.out.println("Updated state for stage: " + stage);
            }
        }

        // Verify final state
        Optional<PersistedState> finalState = databasePersistence.restoreState(workflowId, executionId);
        if (finalState.isPresent()) {
            GraphState state = finalState.get().getState();
            System.out.println("Final workflow state:");
            System.out.println("  Stage: " + state.get("stage", String.class).orElse("unknown"));
            System.out.println("  Input documents: " + state.get("input_documents", List.class).orElse(List.of()).size());
            System.out.println("  Output files: " + state.get("output_files", List.class).orElse(List.of()));
        }
    }

    /**
     * Checkpointing workflows
     */
    private static void runCheckpointingWorkflows() throws Exception {
        System.out.println("\n2. Checkpointing Workflows:");

        String workflowId = "data_pipeline_workflow";
        String executionId = "checkpoint_exec_" + System.currentTimeMillis();

        // Create initial state for data pipeline
        GraphState pipelineState = new BasicGraphState(workflowId);
        pipelineState.set("pipeline_type", "etl");
        pipelineState.set("data_sources", Arrays.asList("database", "api", "files"));
        pipelineState.set("target_tables", Arrays.asList("customers", "orders", "products"));
        pipelineState.set("batch_id", "batch_" + System.currentTimeMillis());

        // Checkpoint 1: Pipeline initialization
        String initCheckpoint = databasePersistence.createCheckpoint(
            workflowId,
            executionId,
            pipelineState,
            CheckpointMetadata.automatic("pipeline_init")
        );
        System.out.println("Created initialization checkpoint: " + initCheckpoint);

        // Simulate data extraction phase
        pipelineState.set("extraction_phase", "completed");
        pipelineState.set("extracted_records", Map.of(
            "customers", 15000,
            "orders", 45000,
            "products", 1200
        ));
        pipelineState.set("extraction_time", LocalDateTime.now().toString());

        // Checkpoint 2: Extraction complete
        String extractionCheckpoint = databasePersistence.createCheckpoint(
            workflowId,
            executionId,
            pipelineState,
            CheckpointMetadata.manual("Data extraction completed successfully")
        );
        System.out.println("Created extraction checkpoint: " + extractionCheckpoint);

        // Simulate transformation phase
        pipelineState.set("transformation_phase", "in_progress");
        pipelineState.set("transformation_rules", Arrays.asList("normalize_dates", "clean_text", "validate_emails"));

        // Checkpoint 3: Transformation start
        String transformCheckpoint = databasePersistence.createCheckpoint(
            workflowId,
            executionId,
            pipelineState,
            CheckpointMetadata.automatic("transformation_start")
        );
        System.out.println("Created transformation checkpoint: " + transformCheckpoint);

        // Simulate transformation completion
        pipelineState.set("transformation_phase", "completed");
        pipelineState.set("transformed_records", Map.of(
            "customers_clean", 14850,
            "orders_processed", 44750,
            "products_enriched", 1200
        ));
        pipelineState.set("data_quality_score", 0.96);

        // Final checkpoint
        String finalCheckpoint = databasePersistence.createCheckpoint(
            workflowId,
            executionId,
            pipelineState,
            CheckpointMetadata.milestone("Pipeline processing completed")
        );
        System.out.println("Created final checkpoint: " + finalCheckpoint);

        // List all checkpoints for this execution
        List<CheckpointInfo> checkpoints = databasePersistence.listCheckpoints(workflowId, executionId);
        System.out.println("\nCheckpoint history (" + checkpoints.size() + " checkpoints):");

        for (CheckpointInfo info : checkpoints) {
            System.out.println("  " + info.getCheckpointId() + " - " +
                             info.getMetadata().getDescription() +
                             " (" + info.getMetadata().getType() + ") - " +
                             info.getSizeBytes() + " bytes");
        }

        // Test checkpoint recovery
        System.out.println("\nTesting checkpoint recovery:");
        Optional<PersistedState> recoveredState = databasePersistence.restoreCheckpoint(extractionCheckpoint);

        if (recoveredState.isPresent()) {
            GraphState recovered = recoveredState.get().getState();
            System.out.println("Recovered from extraction checkpoint:");
            System.out.println("  Extraction phase: " + recovered.get("extraction_phase", String.class).orElse("unknown"));
            System.out.println("  Extracted records: " + recovered.get("extracted_records", Map.class).orElse(Map.of()));
        }
    }

    /**
     * Workflow definition management
     */
    private static void runWorkflowDefinitionManagement() throws Exception {
        System.out.println("\n3. Workflow Definition Management:");

        // Create workflow definitions
        WorkflowDefinition orderProcessing = new WorkflowDefinition(
            "order_processing_workflow",
            "v1.0",
            "system_admin"
        );

        WorkflowDefinition customerOnboarding = new WorkflowDefinition(
            "customer_onboarding_workflow",
            "v2.1",
            "product_team"
        );

        WorkflowDefinition reportGeneration = new WorkflowDefinition(
            "report_generation_workflow",
            "v1.5",
            "analytics_team"
        );

        // Store workflow definitions
        databasePersistence.storeWorkflowDefinition(orderProcessing);
        databasePersistence.storeWorkflowDefinition(customerOnboarding);
        databasePersistence.storeWorkflowDefinition(reportGeneration);

        System.out.println("Stored 3 workflow definitions");

        // Test workflow definition retrieval
        System.out.println("Testing workflow definition retrieval:");

        Optional<WorkflowDefinition> retrievedOrder = databasePersistence.loadWorkflowDefinition(
            "order_processing_workflow", "v1.0");
        if (retrievedOrder.isPresent()) {
            WorkflowDefinition def = retrievedOrder.get();
            System.out.println("  Retrieved: " + def.getWorkflowId() + " " + def.getVersion() +
                             " (created by: " + def.getCreatedBy() + ")");
        }

        Optional<WorkflowDefinition> retrievedOnboarding = databasePersistence.loadWorkflowDefinition(
            "customer_onboarding_workflow", "v2.1");
        if (retrievedOnboarding.isPresent()) {
            WorkflowDefinition def = retrievedOnboarding.get();
            System.out.println("  Retrieved: " + def.getWorkflowId() + " " + def.getVersion() +
                             " (created by: " + def.getCreatedBy() + ")");
        }

        // Test version management
        WorkflowDefinition orderProcessingV2 = new WorkflowDefinition(
            "order_processing_workflow",
            "v2.0",
            "system_admin"
        );

        databasePersistence.storeWorkflowDefinition(orderProcessingV2);
        System.out.println("Created new version of order processing workflow");

        // Retrieve different versions
        Optional<WorkflowDefinition> v1 = databasePersistence.loadWorkflowDefinition(
            "order_processing_workflow", "v1.0");
        Optional<WorkflowDefinition> v2 = databasePersistence.loadWorkflowDefinition(
            "order_processing_workflow", "v2.0");

        System.out.println("Version management test:");
        System.out.println("  v1.0 available: " + v1.isPresent());
        System.out.println("  v2.0 available: " + v2.isPresent());
    }

    /**
     * Execution tracking examples
     */
    private static void runExecutionTrackingExamples() throws Exception {
        System.out.println("\n4. Execution Tracking Examples:");

        // Create multiple execution records for tracking
        String[] workflowIds = {"order_processing_workflow", "customer_onboarding_workflow", "report_generation_workflow"};
        String[] userIds = {"user001", "user002", "user001", "user003"};
        String[] statuses = {"completed", "running", "completed", "failed"};

        List<String> executionIds = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String executionId = "tracking_exec_" + i + "_" + System.currentTimeMillis();
            executionIds.add(executionId);

            ExecutionRecord record = new ExecutionRecord(
                executionId,
                workflowIds[i % workflowIds.length],
                LocalDateTime.now().minusHours(4 - i), // Staggered start times
                i < 2 ? LocalDateTime.now().minusHours(3 - i) : null, // Some completed
                statuses[i],
                Map.of("processed_items", (i + 1) * 100, "processing_time_ms", (i + 1) * 5000),
                List.of()
            );

            record.setSessionId("session_" + (i % 2 + 1));
            record.setUserId(userIds[i]);

            databasePersistence.trackExecution(record);
            System.out.println("Tracked execution: " + executionId + " (" + statuses[i] + ")");
        }

        // Query execution history
        System.out.println("\nQuerying execution history:");

        // Get all executions for order processing workflow
        ExecutionQuery orderQuery = ExecutionQuery.recent(10);
        List<ExecutionRecord> orderHistory = databasePersistence.getExecutionHistory(
            "order_processing_workflow", orderQuery);

        System.out.println("Order processing workflow history (" + orderHistory.size() + " executions):");
        for (ExecutionRecord record : orderHistory) {
            System.out.println("  " + record.getExecutionId() + " - " + record.getStatus() +
                             " (user: " + record.getUserId() + ", session: " + record.getSessionId() + ")");
        }

        // Get recent executions across all workflows
        System.out.println("\nRecent executions across all workflows:");
        for (String workflowId : workflowIds) {
            ExecutionQuery recentQuery = new ExecutionQuery(
                LocalDateTime.now().minusHours(6),
                LocalDateTime.now(),
                null,
                5
            );

            List<ExecutionRecord> recentHistory = databasePersistence.getExecutionHistory(workflowId, recentQuery);
            System.out.println("  " + workflowId + ": " + recentHistory.size() + " recent executions");
        }

        // Filter by status
        ExecutionQuery failedQuery = new ExecutionQuery(
            LocalDateTime.now().minusHours(24),
            LocalDateTime.now(),
            "failed",
            100
        );

        for (String workflowId : workflowIds) {
            List<ExecutionRecord> failedExecutions = databasePersistence.getExecutionHistory(workflowId, failedQuery);
            if (!failedExecutions.isEmpty()) {
                System.out.println("  " + workflowId + " has " + failedExecutions.size() + " failed executions");
            }
        }
    }

    /**
     * Configuration management
     */
    private static void runConfigurationManagement() throws Exception {
        System.out.println("\n5. Configuration Management:");

        // Store different types of configurations

        // Global system configuration
        Map<String, Object> globalConfig = Map.of(
            "max_concurrent_workflows", 100,
            "default_timeout_minutes", 30,
            "retry_policy", Map.of("max_retries", 3, "backoff_multiplier", 2.0),
            "monitoring_enabled", true
        );

        databasePersistence.storeConfiguration("system_config", globalConfig, ConfigurationScope.GLOBAL);
        System.out.println("Stored global system configuration");

        // Workflow-specific configuration
        Map<String, Object> orderConfig = Map.of(
            "payment_timeout_seconds", 300,
            "inventory_check_enabled", true,
            "notification_channels", Arrays.asList("email", "sms"),
            "processing_priority", "high"
        );

        databasePersistence.storeConfiguration("order_processing_config", orderConfig, ConfigurationScope.WORKFLOW);
        System.out.println("Stored order processing workflow configuration");

        // User-specific configuration
        Map<String, Object> userPrefs = Map.of(
            "ui_theme", "dark",
            "notification_preferences", Map.of("email", true, "sms", false, "push", true),
            "default_language", "en",
            "timezone", "UTC-5"
        );

        databasePersistence.storeConfiguration("user_preferences", userPrefs, ConfigurationScope.USER);
        System.out.println("Stored user preferences configuration");

        // Session configuration
        Map<String, Object> sessionConfig = Map.of(
            "session_timeout_minutes", 120,
            "auto_save_interval_seconds", 30,
            "debug_mode", false
        );

        databasePersistence.storeConfiguration("session_config", sessionConfig, ConfigurationScope.SESSION);
        System.out.println("Stored session configuration");

        // Retrieve and test configurations
        System.out.println("\nRetrieving configurations:");

        Optional<Map> retrievedGlobal = databasePersistence.loadConfiguration(
            "system_config", Map.class, ConfigurationScope.GLOBAL);
        if (retrievedGlobal.isPresent()) {
            Map<String, Object> config = retrievedGlobal.get();
            System.out.println("  Global config - max_concurrent_workflows: " +
                             config.get("max_concurrent_workflows"));
        }

        Optional<Map> retrievedWorkflow = databasePersistence.loadConfiguration(
            "order_processing_config", Map.class, ConfigurationScope.WORKFLOW);
        if (retrievedWorkflow.isPresent()) {
            Map<String, Object> config = retrievedWorkflow.get();
            System.out.println("  Workflow config - payment_timeout_seconds: " +
                             config.get("payment_timeout_seconds"));
        }

        Optional<Map> retrievedUser = databasePersistence.loadConfiguration(
            "user_preferences", Map.class, ConfigurationScope.USER);
        if (retrievedUser.isPresent()) {
            Map<String, Object> config = retrievedUser.get();
            System.out.println("  User config - ui_theme: " + config.get("ui_theme"));
        }

        // Update configuration
        Map<String, Object> updatedUserPrefs = Map.of(
            "ui_theme", "light", // Changed from dark
            "notification_preferences", Map.of("email", true, "sms", true, "push", false), // Updated
            "default_language", "en",
            "timezone", "UTC-8" // Changed timezone
        );

        databasePersistence.storeConfiguration("user_preferences", updatedUserPrefs, ConfigurationScope.USER);
        System.out.println("  Updated user preferences configuration");

        // Verify update
        Optional<Map> updatedConfig = databasePersistence.loadConfiguration(
            "user_preferences", Map.class, ConfigurationScope.USER);
        if (updatedConfig.isPresent()) {
            Map<String, Object> config = updatedConfig.get();
            System.out.println("  Verified update - ui_theme: " + config.get("ui_theme") +
                             ", timezone: " + config.get("timezone"));
        }
    }

    /**
     * Production recovery scenarios
     */
    private static void runProductionRecoveryScenarios() throws Exception {
        System.out.println("\n6. Production Recovery Scenarios:");

        // Scenario 1: System crash during workflow execution
        String workflowId = "critical_batch_job";
        String executionId = "recovery_test_" + System.currentTimeMillis();

        System.out.println("Scenario 1: System crash recovery");

        // Simulate workflow state before crash
        GraphState preFailureState = new BasicGraphState(workflowId);
        preFailureState.set("batch_id", "batch_20240305_001");
        preFailureState.set("total_records", 50000);
        preFailureState.set("processed_records", 35000);
        preFailureState.set("current_chunk", 7);
        preFailureState.set("processing_start_time", LocalDateTime.now().minusHours(2).toString());
        preFailureState.set("last_checkpoint_time", LocalDateTime.now().minusMinutes(10).toString());

        // Create checkpoint before "crash"
        String preFailureCheckpoint = databasePersistence.createCheckpoint(
            workflowId,
            executionId,
            preFailureState,
            CheckpointMetadata.automatic("pre_failure_checkpoint")
        );

        System.out.println("  Created pre-failure checkpoint: " + preFailureCheckpoint);

        // Simulate crash recovery
        Optional<PersistedState> recoveredState = databasePersistence.restoreCheckpoint(preFailureCheckpoint);

        if (recoveredState.isPresent()) {
            GraphState recovered = recoveredState.get().getState();

            // Add recovery metadata
            recovered.set("recovered_from_checkpoint", preFailureCheckpoint);
            recovered.set("recovery_time", LocalDateTime.now().toString());
            recovered.set("recovery_reason", "system_crash");

            System.out.println("  Recovered successfully:");
            System.out.println("    Batch ID: " + recovered.get("batch_id", String.class).orElse("unknown"));
            System.out.println("    Progress: " + recovered.get("processed_records", Integer.class).orElse(0) +
                             "/" + recovered.get("total_records", Integer.class).orElse(0));
            System.out.println("    Current chunk: " + recovered.get("current_chunk", Integer.class).orElse(0));

            // Continue from recovery point
            recovered.set("processed_records", 36000); // Simulate continued processing
            recovered.set("current_chunk", 8);

            PersistenceMetadata recoveryMetadata = new PersistenceMetadata(
                "post_recovery",
                Map.of("recovery_checkpoint", preFailureCheckpoint),
                30,
                PersistenceLevel.DETAILED
            );

            databasePersistence.persistState(workflowId, executionId, recovered, recoveryMetadata);
            System.out.println("  Continued processing after recovery");
        }

        // Scenario 2: Data corruption recovery
        System.out.println("\nScenario 2: Data corruption recovery");

        String corruptedExecutionId = "corruption_test_" + System.currentTimeMillis();

        // Create multiple checkpoints for redundancy
        GraphState healthyState = new BasicGraphState("data_processing_workflow");
        healthyState.set("data_source", "customer_database");
        healthyState.set("processing_stage", "transformation");
        healthyState.set("data_integrity_hash", "abc123def456");
        healthyState.set("record_count", 25000);

        String checkpoint1 = databasePersistence.createCheckpoint(
            "data_processing_workflow",
            corruptedExecutionId,
            healthyState,
            CheckpointMetadata.automatic("healthy_state_1")
        );

        Thread.sleep(100); // Ensure different timestamps

        String checkpoint2 = databasePersistence.createCheckpoint(
            "data_processing_workflow",
            corruptedExecutionId,
            healthyState,
            CheckpointMetadata.automatic("healthy_state_2")
        );

        System.out.println("  Created redundant checkpoints: " + checkpoint1 + ", " + checkpoint2);

        // Simulate detection of corruption and recovery
        List<CheckpointInfo> availableCheckpoints = databasePersistence.listCheckpoints(
            "data_processing_workflow", corruptedExecutionId);

        if (!availableCheckpoints.isEmpty()) {
            CheckpointInfo latestHealthy = availableCheckpoints.get(0); // Most recent

            Optional<PersistedState> recoveredFromCorruption = databasePersistence.restoreCheckpoint(
                latestHealthy.getCheckpointId());

            if (recoveredFromCorruption.isPresent()) {
                System.out.println("  Recovered from corruption using checkpoint: " +
                                 latestHealthy.getCheckpointId());
                System.out.println("  Data integrity hash: " +
                                 recoveredFromCorruption.get().getState().get("data_integrity_hash", String.class).orElse("unknown"));
            }
        }
    }

    /**
     * Advanced persistence patterns
     */
    private static void runAdvancedPersistencePatterns() throws Exception {
        System.out.println("\n7. Advanced Persistence Patterns:");

        // Pattern 1: Cleanup and maintenance
        System.out.println("Pattern 1: Cleanup and maintenance");

        // Get current statistics
        PersistenceStats beforeCleanup = databasePersistence.getStats();
        System.out.println("  Before cleanup:");
        System.out.println("    Total states: " + beforeCleanup.getTotalStates());
        System.out.println("    Total checkpoints: " + beforeCleanup.getTotalCheckpoints());
        System.out.println("    Total size: " + beforeCleanup.getTotalSizeBytes() + " bytes");

        // Execute cleanup
        CleanupPolicy cleanupPolicy = new CleanupPolicy(
            Duration.ofDays(1), // Retain for 1 day only (for demo)
            5, // Keep max 5 checkpoints
            true, // Delete orphaned data
            Set.of(PersistenceLevel.MINIMAL, PersistenceLevel.NORMAL)
        );

        CleanupResult cleanupResult = databasePersistence.cleanup(cleanupPolicy);
        System.out.println("  Cleanup results:");
        System.out.println("    Deleted states: " + cleanupResult.getDeletedStates());
        System.out.println("    Deleted checkpoints: " + cleanupResult.getDeletedCheckpoints());
        System.out.println("    Reclaimed bytes: " + cleanupResult.getReclaimedBytes());
        System.out.println("    Cleanup time: " + cleanupResult.getCleanupTime().toMillis() + "ms");

        // Pattern 2: Distributed workflow coordination
        System.out.println("\nPattern 2: Distributed workflow coordination");

        DistributedWorkflowCoordinator coordinator = new DistributedWorkflowCoordinator(databasePersistence);

        String distributedWorkflow = "distributed_data_pipeline";

        // Register multiple instances
        coordinator.registerInstance("instance_1", "node_a");
        coordinator.registerInstance("instance_2", "node_b");
        coordinator.registerInstance("instance_3", "node_c");

        // Simulate distributed execution
        for (int i = 0; i < 3; i++) {
            String instanceId = "instance_" + (i + 1);
            String executionId = "distributed_exec_" + i + "_" + System.currentTimeMillis();

            GraphState distributedState = new BasicGraphState(distributedWorkflow);
            distributedState.set("instance_id", instanceId);
            distributedState.set("partition", "partition_" + i);
            distributedState.set("processing_start", LocalDateTime.now().toString());

            PersistenceMetadata distributedMetadata = new PersistenceMetadata(
                "distributed_processing",
                Map.of("instance", instanceId, "partition", i),
                7,
                PersistenceLevel.NORMAL
            );

            databasePersistence.persistState(distributedWorkflow, executionId, distributedState, distributedMetadata);

            // Create coordination checkpoint
            String coordCheckpoint = databasePersistence.createCheckpoint(
                distributedWorkflow,
                executionId,
                distributedState,
                CheckpointMetadata.milestone("Instance " + instanceId + " started")
            );

            coordinator.reportProgress(instanceId, executionId, 0.0, "started");

            System.out.println("    Instance " + instanceId + " started execution: " + executionId);
        }

        // Pattern 3: Versioned state evolution
        System.out.println("\nPattern 3: Versioned state evolution");

        StateVersionManager versionManager = new StateVersionManager(databasePersistence);

        String evolvingWorkflow = "customer_profile_workflow";
        String versionedExecutionId = "versioned_exec_" + System.currentTimeMillis();

        // Version 1.0 state
        GraphState v1State = new BasicGraphState(evolvingWorkflow);
        v1State.set("schema_version", "1.0");
        v1State.set("customer_id", "cust_001");
        v1State.set("basic_info", Map.of("name", "John Doe", "email", "john@example.com"));

        String v1Checkpoint = versionManager.createVersionedCheckpoint(
            evolvingWorkflow, versionedExecutionId, v1State, "1.0");
        System.out.println("    Created v1.0 state checkpoint: " + v1Checkpoint);

        // Version 2.0 state (schema evolution)
        GraphState v2State = versionManager.migrateState(v1State, "1.0", "2.0");

        String v2Checkpoint = versionManager.createVersionedCheckpoint(
            evolvingWorkflow, versionedExecutionId, v2State, "2.0");
        System.out.println("    Migrated to v2.0 state checkpoint: " + v2Checkpoint);

        // Demonstrate backward compatibility
        Optional<GraphState> restoredV1 = versionManager.restoreVersionedState(v1Checkpoint, "1.0");
        Optional<GraphState> restoredV2 = versionManager.restoreVersionedState(v2Checkpoint, "2.0");

        System.out.println("    Version 1.0 schema: " +
                         restoredV1.map(s -> s.get("schema_version", String.class).orElse("unknown")).orElse("not found"));
        System.out.println("    Version 2.0 schema: " +
                         restoredV2.map(s -> s.get("schema_version", String.class).orElse("unknown")).orElse("not found"));

        // Final statistics
        PersistenceStats finalStats = databasePersistence.getStats();
        System.out.println("\nFinal persistence statistics:");
        System.out.println("  Total states: " + finalStats.getTotalStates());
        System.out.println("  Total checkpoints: " + finalStats.getTotalCheckpoints());
        System.out.println("  Compression ratio: " + String.format("%.2f", finalStats.getCompressionRatio()));
        System.out.println("  States by level: " + finalStats.getStatesByLevel());
    }

    /**
     * Cleanup resources
     */
    private static void cleanup() {
        try {
            if (databasePersistence != null) {
                databasePersistence.close();
                System.out.println("Database persistence closed");
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}

/**
 * Supporting classes for advanced patterns
 */

class WorkflowOrchestrator {
    private final GraphPersistence persistence;

    public WorkflowOrchestrator(GraphPersistence persistence) {
        this.persistence = persistence;
    }

    public String startWorkflow(String workflowId, Map<String, Object> parameters) throws PersistenceException {
        String executionId = "orchestrated_" + workflowId + "_" + System.currentTimeMillis();

        GraphState initialState = new BasicGraphState(workflowId);
        initialState.set("status", "started");
        initialState.set("parameters", parameters);
        initialState.set("orchestrator", "WorkflowOrchestrator");

        PersistenceMetadata metadata = PersistenceMetadata.create("orchestration_start");
        persistence.persistState(workflowId, executionId, initialState, metadata);

        return executionId;
    }
}

class DistributedWorkflowCoordinator {
    private final GraphPersistence persistence;
    private final Map<String, String> instances = new ConcurrentHashMap<>();
    private final Map<String, Double> progress = new ConcurrentHashMap<>();

    public DistributedWorkflowCoordinator(GraphPersistence persistence) {
        this.persistence = persistence;
    }

    public void registerInstance(String instanceId, String nodeId) {
        instances.put(instanceId, nodeId);
        progress.put(instanceId, 0.0);
    }

    public void reportProgress(String instanceId, String executionId, double progressValue, String status) {
        progress.put(instanceId, progressValue);
        // In a real implementation, this would coordinate with other instances
    }

    public Map<String, Double> getAllProgress() {
        return new HashMap<>(progress);
    }
}

class StateVersionManager {
    private final GraphPersistence persistence;

    public StateVersionManager(GraphPersistence persistence) {
        this.persistence = persistence;
    }

    public String createVersionedCheckpoint(String workflowId, String executionId,
                                          GraphState state, String version) throws PersistenceException {
        CheckpointMetadata metadata = new CheckpointMetadata(
            "Version " + version + " checkpoint",
            CheckpointType.MILESTONE,
            Map.of("schema_version", version),
            false
        );

        return persistence.createCheckpoint(workflowId, executionId, state, metadata);
    }

    public GraphState migrateState(GraphState oldState, String fromVersion, String toVersion) {
        GraphState migratedState = oldState.copy();

        // Simulate schema migration
        if ("1.0".equals(fromVersion) && "2.0".equals(toVersion)) {
            migratedState.set("schema_version", "2.0");

            // Add new fields in v2.0
            migratedState.set("extended_profile", Map.of(
                "preferences", Map.of("marketing", true, "notifications", false),
                "metadata", Map.of("created_date", LocalDateTime.now().toString())
            ));

            // Migrate existing fields
            Map<String, Object> basicInfo = migratedState.get("basic_info", Map.class).orElse(Map.of());
            Map<String, Object> enhancedInfo = new HashMap<>(basicInfo);
            enhancedInfo.put("profile_version", "2.0");
            migratedState.set("basic_info", enhancedInfo);
        }

        return migratedState;
    }

    public Optional<GraphState> restoreVersionedState(String checkpointId, String expectedVersion)
            throws PersistenceException {
        Optional<PersistedState> restored = persistence.restoreCheckpoint(checkpointId);

        if (restored.isPresent()) {
            GraphState state = restored.get().getState();
            String stateVersion = state.get("schema_version", String.class).orElse("unknown");

            if (expectedVersion.equals(stateVersion)) {
                return Optional.of(state);
            }
        }

        return Optional.empty();
    }
}
