/**
 * LangGraph State Management Examples - Complete Implementation
 * Demonstrates comprehensive state patterns for graph workflows
 */
package com.example.agent.langgraph.state;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Comprehensive GraphState demonstration
 */
public class GraphStateExampleSuite {

    public static void main(String[] args) {
        System.out.println("=== LangGraph State Management Examples ===");

        try {
            // Run comprehensive state examples
            runBasicStateOperations();
            runTypedStateValidation();
            runStateSchemaExamples();
            runStatePersistenceExamples();
            runStateCheckpointingExamples();
            runParallelStateHandling();
            runAdvancedStatePatterns();

        } catch (Exception e) {
            System.err.println("Error running state examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Basic state operations
     */
    private static void runBasicStateOperations() throws Exception {
        System.out.println("\n1. Basic State Operations:");

        // Create basic graph state
        BasicGraphState basicState = new BasicGraphState("basic_operations");

        // Set various data types
        basicState.set("string_value", "Hello LangGraph");
        basicState.set("integer_value", 42);
        basicState.set("double_value", 3.14159);
        basicState.set("boolean_value", true);
        basicState.set("list_value", Arrays.asList("item1", "item2", "item3"));
        basicState.set("map_value", Map.of("key1", "value1", "key2", "value2"));
        basicState.set("timestamp", LocalDateTime.now().toString());

        System.out.println("Created basic state with keys: " + basicState.getKeys());

        // Retrieve and verify values
        String stringVal = basicState.get("string_value", String.class).orElse("default");
        Integer intVal = basicState.get("integer_value", Integer.class).orElse(0);
        Double doubleVal = basicState.get("double_value", Double.class).orElse(0.0);
        Boolean boolVal = basicState.get("boolean_value", Boolean.class).orElse(false);
        List<?> listVal = basicState.get("list_value", List.class).orElse(Collections.emptyList());
        Map<?, ?> mapVal = basicState.get("map_value", Map.class).orElse(Collections.emptyMap());

        System.out.println("Retrieved values:");
        System.out.println("  String: " + stringVal);
        System.out.println("  Integer: " + intVal);
        System.out.println("  Double: " + doubleVal);
        System.out.println("  Boolean: " + boolVal);
        System.out.println("  List size: " + listVal.size());
        System.out.println("  Map size: " + mapVal.size());

        // Test state copying
        BasicGraphState copiedState = basicState.copy();
        copiedState.set("modified_in_copy", "This is only in the copy");

        System.out.println("Original state keys: " + basicState.getKeys().size());
        System.out.println("Copied state keys: " + copiedState.getKeys().size());

        // Test state clearing
        basicState.clear();
        System.out.println("State after clear: " + basicState.getKeys().size() + " keys");
    }

    /**
     * Typed state validation
     */
    private static void runTypedStateValidation() throws Exception {
        System.out.println("\n2. Typed State Validation:");

        // Create typed state with schema
        TypedGraphState<UserDataSchema> typedState = new TypedGraphState<>("typed_operations", UserDataSchema.class);

        // Set data according to schema
        UserData userData = new UserData("john_doe", "john@example.com", 30, Arrays.asList("admin", "user"));
        ProcessingMetadata metadata = new ProcessingMetadata("user_registration", 1, LocalDateTime.now());
        ValidationResult validation = new ValidationResult(true, Collections.emptyList(), 0.95);

        typedState.set("user_data", userData);
        typedState.set("processing_metadata", metadata);
        typedState.set("validation_result", validation);

        System.out.println("Created typed state with schema: " + UserDataSchema.class.getSimpleName());

        // Validate state against schema
        SchemaValidationResult schemaValidation = typedState.validateSchema();
        System.out.println("Schema validation:");
        System.out.println("  Valid: " + schemaValidation.isValid());
        System.out.println("  Required fields present: " + schemaValidation.getValidatedFields());

        if (!schemaValidation.getErrors().isEmpty()) {
            System.out.println("  Validation errors:");
            schemaValidation.getErrors().forEach(error -> System.out.println("    - " + error));
        }

        // Retrieve typed data
        UserData retrievedUser = typedState.get("user_data", UserData.class).orElse(null);
        ProcessingMetadata retrievedMetadata = typedState.get("processing_metadata", ProcessingMetadata.class).orElse(null);

        if (retrievedUser != null) {
            System.out.println("Retrieved user: " + retrievedUser.getUsername() + " (" + retrievedUser.getEmail() + ")");
        }

        if (retrievedMetadata != null) {
            System.out.println("Processing stage: " + retrievedMetadata.getStage() + " (attempt " + retrievedMetadata.getAttemptCount() + ")");
        }

        // Test schema evolution
        System.out.println("\nTesting schema evolution:");
        typedState.evolveSchema("2.0", Map.of(
            "new_field", "default_value",
            "enhanced_validation", true
        ));

        String schemaVersion = typedState.getSchemaVersion();
        System.out.println("Evolved to schema version: " + schemaVersion);
    }

    /**
     * State schema examples
     */
    private static void runStateSchemaExamples() throws Exception {
        System.out.println("\n3. State Schema Examples:");

        // Document processing schema
        DocumentProcessingSchema docSchema = new DocumentProcessingSchema();
        TypedGraphState<DocumentProcessingSchema> docState = new TypedGraphState<>("document_processing", DocumentProcessingSchema.class);

        // Set document processing data
        DocumentInfo docInfo = new DocumentInfo(
            "doc_12345",
            "important_document.pdf",
            "application/pdf",
            1024567L,
            Arrays.asList("finance", "confidential")
        );

        ProcessingConfig config = new ProcessingConfig(
            true, // extractText
            false, // extractImages
            true, // preserveFormatting
            Arrays.asList("english", "spanish")
        );

        ProcessingStatus status = new ProcessingStatus(
            "IN_PROGRESS",
            45.0, // progress percentage
            Arrays.asList("validation", "parsing"),
            Collections.emptyList()
        );

        docState.set("document_info", docInfo);
        docState.set("processing_config", config);
        docState.set("processing_status", status);

        System.out.println("Document processing state created:");
        System.out.println("  Document: " + docInfo.getFilename() + " (" + docInfo.getSize() + " bytes)");
        System.out.println("  Status: " + status.getStatus() + " (" + status.getProgressPercentage() + "%)");
        System.out.println("  Completed stages: " + status.getCompletedStages());

        // Validate document schema
        SchemaValidationResult docValidation = docState.validateSchema();
        System.out.println("Document schema validation: " + (docValidation.isValid() ? "PASSED" : "FAILED"));

        // E-commerce order schema
        OrderProcessingSchema orderSchema = new OrderProcessingSchema();
        TypedGraphState<OrderProcessingSchema> orderState = new TypedGraphState<>("order_processing", OrderProcessingSchema.class);

        Customer customer = new Customer("cust_789", "Jane Smith", "jane@example.com", "premium");
        List<OrderItem> items = Arrays.asList(
            new OrderItem("item_001", "Laptop Computer", 2, 999.99),
            new OrderItem("item_002", "Wireless Mouse", 1, 29.99)
        );

        OrderInfo orderInfo = new OrderInfo(
            "order_12345",
            customer,
            items,
            2059.97, // total
            "credit_card",
            "processing"
        );

        orderState.set("order_info", orderInfo);
        orderState.set("payment_verified", false);
        orderState.set("inventory_checked", false);
        orderState.set("shipping_calculated", false);

        System.out.println("\nOrder processing state created:");
        System.out.println("  Order ID: " + orderInfo.getOrderId());
        System.out.println("  Customer: " + orderInfo.getCustomer().getName() + " (" + orderInfo.getCustomer().getTier() + ")");
        System.out.println("  Total: $" + String.format("%.2f", orderInfo.getTotal()));
        System.out.println("  Items: " + orderInfo.getItems().size());

        // Validate order schema
        SchemaValidationResult orderValidation = orderState.validateSchema();
        System.out.println("Order schema validation: " + (orderValidation.isValid() ? "PASSED" : "FAILED"));
    }

    /**
     * State persistence examples
     */
    private static void runStatePersistenceExamples() throws Exception {
        System.out.println("\n4. State Persistence Examples:");

        // Create state persistence manager
        StatePersistenceManager persistenceManager = new StatePersistenceManager();

        // Create a complex state to persist
        BasicGraphState complexState = new BasicGraphState("persistence_test");
        complexState.set("workflow_id", "workflow_" + System.currentTimeMillis());
        complexState.set("execution_start", LocalDateTime.now().toString());
        complexState.set("user_context", Map.of(
            "user_id", "user_12345",
            "session_id", "session_789",
            "preferences", Map.of("theme", "dark", "language", "en")
        ));
        complexState.set("processing_data", Map.of(
            "input_files", Arrays.asList("file1.txt", "file2.pdf", "file3.docx"),
            "output_format", "json",
            "quality_threshold", 0.85
        ));
        complexState.set("intermediate_results", Map.of(
            "validation_score", 0.92,
            "extracted_entities", Arrays.asList("person:John", "location:NYC", "date:2024-03-05"),
            "processing_time_ms", 1250
        ));

        System.out.println("Created complex state with " + complexState.getKeys().size() + " keys");

        // Persist state to different storage backends
        String memoryStateId = persistenceManager.persistToMemory(complexState);
        String fileStateId = persistenceManager.persistToFile(complexState, "/tmp/state_" + System.currentTimeMillis() + ".json");
        String dbStateId = persistenceManager.persistToDatabase(complexState, "workflow_states");

        System.out.println("Persisted state:");
        System.out.println("  Memory ID: " + memoryStateId);
        System.out.println("  File ID: " + fileStateId);
        System.out.println("  Database ID: " + dbStateId);

        // Restore state from different backends
        Optional<GraphState> restoredFromMemory = persistenceManager.restoreFromMemory(memoryStateId);
        Optional<GraphState> restoredFromFile = persistenceManager.restoreFromFile(fileStateId);
        Optional<GraphState> restoredFromDatabase = persistenceManager.restoreFromDatabase(dbStateId);

        System.out.println("Restoration results:");
        System.out.println("  From memory: " + (restoredFromMemory.isPresent() ? "SUCCESS" : "FAILED"));
        System.out.println("  From file: " + (restoredFromFile.isPresent() ? "SUCCESS" : "FAILED"));
        System.out.println("  From database: " + (restoredFromDatabase.isPresent() ? "SUCCESS" : "FAILED"));

        // Verify restored state integrity
        if (restoredFromMemory.isPresent()) {
            GraphState restored = restoredFromMemory.get();
            boolean integrityCheck = restored.getKeys().size() == complexState.getKeys().size();
            System.out.println("  Memory integrity check: " + (integrityCheck ? "PASSED" : "FAILED"));

            String originalWorkflowId = complexState.get("workflow_id", String.class).orElse("");
            String restoredWorkflowId = restored.get("workflow_id", String.class).orElse("");
            System.out.println("  Workflow ID preserved: " + originalWorkflowId.equals(restoredWorkflowId));
        }

        // Test state compression
        StateCompressionResult compression = persistenceManager.compressState(complexState);
        System.out.println("State compression:");
        System.out.println("  Original size: " + compression.getOriginalSize() + " bytes");
        System.out.println("  Compressed size: " + compression.getCompressedSize() + " bytes");
        System.out.println("  Compression ratio: " + String.format("%.2f%%", compression.getCompressionRatio() * 100));
    }

    /**
     * State checkpointing examples
     */
    private static void runStateCheckpointingExamples() throws Exception {
        System.out.println("\n5. State Checkpointing Examples:");

        // Create checkpoint manager
        StateCheckpointManager checkpointManager = new StateCheckpointManager();

        // Create workflow state that will evolve
        BasicGraphState workflowState = new BasicGraphState("checkpointing_workflow");
        workflowState.set("workflow_name", "document_analysis");
        workflowState.set("total_documents", 100);
        workflowState.set("processed_documents", 0);
        workflowState.set("current_stage", "initialization");

        // Create initial checkpoint
        String checkpoint1 = checkpointManager.createCheckpoint(
            "workflow_start",
            workflowState,
            "Workflow initialization completed"
        );
        System.out.println("Created checkpoint 1: " + checkpoint1);

        // Simulate processing progress
        workflowState.set("processed_documents", 25);
        workflowState.set("current_stage", "document_parsing");
        workflowState.set("parsing_results", Map.of(
            "successful", 23,
            "failed", 2,
            "average_parse_time", 120
        ));

        String checkpoint2 = checkpointManager.createCheckpoint(
            "parsing_complete",
            workflowState,
            "Document parsing phase completed"
        );
        System.out.println("Created checkpoint 2: " + checkpoint2);

        // Continue processing
        workflowState.set("processed_documents", 50);
        workflowState.set("current_stage", "content_analysis");
        workflowState.set("analysis_results", Map.of(
            "entities_extracted", 1247,
            "topics_identified", 15,
            "sentiment_scores", Arrays.asList(0.7, 0.8, 0.6, 0.9)
        ));

        String checkpoint3 = checkpointManager.createCheckpoint(
            "analysis_milestone",
            workflowState,
            "Content analysis milestone reached"
        );
        System.out.println("Created checkpoint 3: " + checkpoint3);

        // Continue to completion
        workflowState.set("processed_documents", 100);
        workflowState.set("current_stage", "finalization");
        workflowState.set("final_results", Map.of(
            "total_entities", 2485,
            "unique_topics", 15,
            "average_sentiment", 0.75,
            "processing_time_total", 15000
        ));

        String checkpoint4 = checkpointManager.createCheckpoint(
            "workflow_complete",
            workflowState,
            "Workflow execution completed successfully"
        );
        System.out.println("Created checkpoint 4: " + checkpoint4);

        // List all checkpoints
        List<CheckpointInfo> allCheckpoints = checkpointManager.listCheckpoints();
        System.out.println("\nAll checkpoints created:");
        for (CheckpointInfo info : allCheckpoints) {
            System.out.println("  " + info.getCheckpointId() + ": " + info.getDescription() +
                             " (size: " + info.getSizeBytes() + " bytes)");
        }

        // Demonstrate checkpoint restoration
        System.out.println("\nCheckpoint restoration test:");

        Optional<GraphState> restoredState = checkpointManager.restoreCheckpoint(checkpoint2);
        if (restoredState.isPresent()) {
            GraphState restored = restoredState.get();
            Integer processedDocs = restored.get("processed_documents", Integer.class).orElse(0);
            String stage = restored.get("current_stage", String.class).orElse("unknown");

            System.out.println("Restored from checkpoint 2:");
            System.out.println("  Processed documents: " + processedDocs);
            System.out.println("  Current stage: " + stage);
            System.out.println("  Has parsing results: " + restored.get("parsing_results", Map.class).isPresent());
            System.out.println("  Has analysis results: " + restored.get("analysis_results", Map.class).isPresent());
        }

        // Test checkpoint cleanup
        int deletedCheckpoints = checkpointManager.cleanupOldCheckpoints(Duration.ofMinutes(5));
        System.out.println("Cleaned up " + deletedCheckpoints + " old checkpoints");
    }

    /**
     * Parallel state handling
     */
    private static void runParallelStateHandling() throws Exception {
        System.out.println("\n6. Parallel State Handling:");

        // Create parallel state manager
        ParallelStateManager parallelManager = new ParallelStateManager();

        // Create base state
        BasicGraphState baseState = new BasicGraphState("parallel_workflow");
        baseState.set("base_data", "shared across all parallel branches");
        baseState.set("execution_id", "exec_" + System.currentTimeMillis());

        // Create parallel branches
        int numBranches = 3;
        String[] branchNames = {"branch_analysis", "branch_validation", "branch_enhancement"};

        System.out.println("Creating " + numBranches + " parallel state branches...");

        List<CompletableFuture<GraphState>> futures = new ArrayList<>();

        for (int i = 0; i < numBranches; i++) {
            String branchName = branchNames[i];

            CompletableFuture<GraphState> branchFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    // Create branch-specific state
                    BasicGraphState branchState = baseState.copy();
                    branchState.set("branch_name", branchName);
                    branchState.set("branch_start_time", LocalDateTime.now().toString());

                    // Simulate branch-specific processing
                    Thread.sleep(100 + new Random().nextInt(200));

                    switch (branchName) {
                        case "branch_analysis":
                            branchState.set("analysis_complete", true);
                            branchState.set("entities_found", 45);
                            branchState.set("confidence_score", 0.87);
                            break;
                        case "branch_validation":
                            branchState.set("validation_complete", true);
                            branchState.set("validation_passed", true);
                            branchState.set("validation_score", 0.92);
                            break;
                        case "branch_enhancement":
                            branchState.set("enhancement_complete", true);
                            branchState.set("improvements_applied", 12);
                            branchState.set("quality_improvement", 0.15);
                            break;
                    }

                    branchState.set("branch_end_time", LocalDateTime.now().toString());
                    System.out.println("  " + branchName + " completed");

                    return branchState;

                } catch (Exception e) {
                    throw new RuntimeException("Branch processing failed", e);
                }
            });

            futures.add(branchFuture);
        }

        // Wait for all branches to complete
        CompletableFuture<Void> allComplete = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );

        allComplete.get(5, TimeUnit.SECONDS);

        // Merge results from parallel branches
        List<GraphState> branchResults = new ArrayList<>();
        for (CompletableFuture<GraphState> future : futures) {
            branchResults.add(future.get());
        }

        GraphState mergedState = parallelManager.mergeParallelStates(baseState, branchResults);

        System.out.println("Parallel processing completed:");
        System.out.println("  Base execution ID: " + mergedState.get("execution_id", String.class).orElse("unknown"));
        System.out.println("  Analysis entities: " + mergedState.get("entities_found", Integer.class).orElse(0));
        System.out.println("  Validation passed: " + mergedState.get("validation_passed", Boolean.class).orElse(false));
        System.out.println("  Enhancements applied: " + mergedState.get("improvements_applied", Integer.class).orElse(0));
        System.out.println("  Overall processing successful: " + mergedState.get("parallel_processing_complete", Boolean.class).orElse(false));

        // Test state conflict resolution
        System.out.println("\nTesting state conflict resolution:");

        BasicGraphState conflictState1 = new BasicGraphState("conflict_test");
        conflictState1.set("shared_field", "value_from_branch_1");
        conflictState1.set("priority", 1);

        BasicGraphState conflictState2 = new BasicGraphState("conflict_test");
        conflictState2.set("shared_field", "value_from_branch_2");
        conflictState2.set("priority", 2);

        GraphState resolvedState = parallelManager.resolveStateConflicts(
            Arrays.asList(conflictState1, conflictState2),
            ConflictResolutionStrategy.HIGHEST_PRIORITY
        );

        String resolvedValue = resolvedState.get("shared_field", String.class).orElse("unresolved");
        System.out.println("Conflict resolution result: " + resolvedValue);
    }

    /**
     * Advanced state patterns
     */
    private static void runAdvancedStatePatterns() throws Exception {
        System.out.println("\n7. Advanced State Patterns:");

        // Pattern 1: State versioning and evolution
        System.out.println("Pattern 1: State Versioning");

        VersionedStateManager versionManager = new VersionedStateManager();

        // Create initial state version
        BasicGraphState initialState = new BasicGraphState("versioned_workflow");
        initialState.set("schema_version", "1.0");
        initialState.set("user_name", "John Doe");
        initialState.set("email", "john@example.com");

        String version1Id = versionManager.createVersion(initialState, "Initial user data");
        System.out.println("  Created version 1: " + version1Id);

        // Evolve state to version 2
        BasicGraphState evolvedState = initialState.copy();
        evolvedState.set("schema_version", "2.0");
        evolvedState.set("user_profile", Map.of(
            "name", evolvedState.get("user_name", String.class).orElse(""),
            "email", evolvedState.get("email", String.class).orElse(""),
            "preferences", Map.of("theme", "dark", "notifications", true),
            "metadata", Map.of("created_date", LocalDateTime.now().toString())
        ));

        String version2Id = versionManager.createVersion(evolvedState, "Enhanced user profile schema");
        System.out.println("  Created version 2: " + version2Id);

        // Pattern 2: State transactions
        System.out.println("\nPattern 2: State Transactions");

        StateTransactionManager transactionManager = new StateTransactionManager();

        BasicGraphState transactionalState = new BasicGraphState("transaction_test");
        transactionalState.set("account_balance", 1000.0);
        transactionalState.set("transaction_count", 0);

        // Begin transaction
        String transactionId = transactionManager.beginTransaction(transactionalState);
        System.out.println("  Started transaction: " + transactionId);

        try {
            // Make changes within transaction
            transactionalState.set("account_balance", 850.0);
            transactionalState.set("last_transaction", Map.of(
                "type", "withdrawal",
                "amount", 150.0,
                "timestamp", LocalDateTime.now().toString()
            ));
            transactionalState.set("transaction_count",
                transactionalState.get("transaction_count", Integer.class).orElse(0) + 1);

            // Commit transaction
            transactionManager.commitTransaction(transactionId, transactionalState);
            System.out.println("  Transaction committed successfully");
            System.out.println("  New balance: $" + transactionalState.get("account_balance", Double.class).orElse(0.0));

        } catch (Exception e) {
            transactionManager.rollbackTransaction(transactionId);
            System.out.println("  Transaction rolled back due to error: " + e.getMessage());
        }

        // Pattern 3: State event sourcing
        System.out.println("\nPattern 3: State Event Sourcing");

        StateEventSourcing eventSourcing = new StateEventSourcing();

        String streamId = "user_activity_stream";

        // Record events
        eventSourcing.recordEvent(streamId, "user_created", Map.of("user_id", "user123", "name", "Alice"));
        eventSourcing.recordEvent(streamId, "email_updated", Map.of("user_id", "user123", "new_email", "alice@example.com"));
        eventSourcing.recordEvent(streamId, "preferences_set", Map.of("user_id", "user123", "theme", "light"));
        eventSourcing.recordEvent(streamId, "login_recorded", Map.of("user_id", "user123", "timestamp", LocalDateTime.now().toString()));

        System.out.println("  Recorded 4 events to stream: " + streamId);

        // Replay events to rebuild state
        GraphState reconstructedState = eventSourcing.replayEvents(streamId);
        System.out.println("  Reconstructed state from events:");
        System.out.println("    User ID: " + reconstructedState.get("user_id", String.class).orElse("unknown"));
        System.out.println("    Name: " + reconstructedState.get("name", String.class).orElse("unknown"));
        System.out.println("    Email: " + reconstructedState.get("email", String.class).orElse("unknown"));
        System.out.println("    Theme: " + reconstructedState.get("theme", String.class).orElse("unknown"));

        // Get event history
        List<StateEvent> eventHistory = eventSourcing.getEventHistory(streamId);
        System.out.println("  Event history (" + eventHistory.size() + " events):");
        for (StateEvent event : eventHistory) {
            System.out.println("    " + event.getEventType() + " at " + event.getTimestamp());
        }

        // Pattern 4: State caching and optimization
        System.out.println("\nPattern 4: State Caching and Optimization");

        StateCacheManager cacheManager = new StateCacheManager();

        // Create expensive-to-compute state
        BasicGraphState expensiveState = new BasicGraphState("expensive_computation");
        expensiveState.set("computation_type", "machine_learning_inference");
        expensiveState.set("model_parameters", Map.of("layers", 50, "neurons", 1000));
        expensiveState.set("input_data", "large_dataset_reference");

        // Simulate expensive computation
        long startTime = System.currentTimeMillis();
        Thread.sleep(100); // Simulate computation time
        expensiveState.set("ml_results", Map.of(
            "prediction", 0.87,
            "confidence", 0.93,
            "processing_time", System.currentTimeMillis() - startTime
        ));

        // Cache the computed state
        String cacheKey = cacheManager.cacheState(expensiveState, Duration.ofMinutes(10));
        System.out.println("  Cached expensive computation with key: " + cacheKey);

        // Retrieve from cache
        Optional<GraphState> cachedState = cacheManager.getCachedState(cacheKey);
        if (cachedState.isPresent()) {
            System.out.println("  Cache hit! Retrieved cached results:");
            Map<?, ?> results = cachedState.get().get("ml_results", Map.class).orElse(Map.of());
            System.out.println("    Prediction: " + results.get("prediction"));
            System.out.println("    Confidence: " + results.get("confidence"));
        }

        // Cache statistics
        CacheStats cacheStats = cacheManager.getCacheStats();
        System.out.println("  Cache statistics:");
        System.out.println("    Entries: " + cacheStats.getEntryCount());
        System.out.println("    Hit rate: " + String.format("%.2f%%", cacheStats.getHitRate() * 100));
        System.out.println("    Memory usage: " + cacheStats.getMemoryUsageBytes() + " bytes");
    }
}

/**
 * Supporting classes and schemas for the examples
 */

// Schema definitions
class UserDataSchema {
    // Schema definition for user data validation
}

class DocumentProcessingSchema {
    // Schema definition for document processing
}

class OrderProcessingSchema {
    // Schema definition for order processing
}

// Data model classes
class UserData {
    private final String username;
    private final String email;
    private final int age;
    private final List<String> roles;

    public UserData(String username, String email, int age, List<String> roles) {
        this.username = username;
        this.email = email;
        this.age = age;
        this.roles = new ArrayList<>(roles);
    }

    // Getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public List<String> getRoles() { return new ArrayList<>(roles); }
}

class ProcessingMetadata {
    private final String stage;
    private final int attemptCount;
    private final LocalDateTime timestamp;

    public ProcessingMetadata(String stage, int attemptCount, LocalDateTime timestamp) {
        this.stage = stage;
        this.attemptCount = attemptCount;
        this.timestamp = timestamp;
    }

    public String getStage() { return stage; }
    public int getAttemptCount() { return attemptCount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final double confidence;

    public ValidationResult(boolean valid, List<String> errors, double confidence) {
        this.valid = valid;
        this.errors = new ArrayList<>(errors);
        this.confidence = confidence;
    }

    public boolean isValid() { return valid; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public double getConfidence() { return confidence; }
}

// Additional model classes for document processing
class DocumentInfo {
    private final String documentId;
    private final String filename;
    private final String mimeType;
    private final long size;
    private final List<String> tags;

    public DocumentInfo(String documentId, String filename, String mimeType, long size, List<String> tags) {
        this.documentId = documentId;
        this.filename = filename;
        this.mimeType = mimeType;
        this.size = size;
        this.tags = new ArrayList<>(tags);
    }

    public String getDocumentId() { return documentId; }
    public String getFilename() { return filename; }
    public String getMimeType() { return mimeType; }
    public long getSize() { return size; }
    public List<String> getTags() { return new ArrayList<>(tags); }
}

class ProcessingConfig {
    private final boolean extractText;
    private final boolean extractImages;
    private final boolean preserveFormatting;
    private final List<String> targetLanguages;

    public ProcessingConfig(boolean extractText, boolean extractImages, boolean preserveFormatting, List<String> targetLanguages) {
        this.extractText = extractText;
        this.extractImages = extractImages;
        this.preserveFormatting = preserveFormatting;
        this.targetLanguages = new ArrayList<>(targetLanguages);
    }

    public boolean isExtractText() { return extractText; }
    public boolean isExtractImages() { return extractImages; }
    public boolean isPreserveFormatting() { return preserveFormatting; }
    public List<String> getTargetLanguages() { return new ArrayList<>(targetLanguages); }
}

class ProcessingStatus {
    private final String status;
    private final double progressPercentage;
    private final List<String> completedStages;
    private final List<String> errors;

    public ProcessingStatus(String status, double progressPercentage, List<String> completedStages, List<String> errors) {
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.completedStages = new ArrayList<>(completedStages);
        this.errors = new ArrayList<>(errors);
    }

    public String getStatus() { return status; }
    public double getProgressPercentage() { return progressPercentage; }
    public List<String> getCompletedStages() { return new ArrayList<>(completedStages); }
    public List<String> getErrors() { return new ArrayList<>(errors); }
}

// Order processing model classes
class Customer {
    private final String customerId;
    private final String name;
    private final String email;
    private final String tier;

    public Customer(String customerId, String name, String email, String tier) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.tier = tier;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTier() { return tier; }
}

class OrderItem {
    private final String itemId;
    private final String name;
    private final int quantity;
    private final double price;

    public OrderItem(String itemId, String name, int quantity, double price) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}

class OrderInfo {
    private final String orderId;
    private final Customer customer;
    private final List<OrderItem> items;
    private final double total;
    private final String paymentMethod;
    private final String status;

    public OrderInfo(String orderId, Customer customer, List<OrderItem> items, double total, String paymentMethod, String status) {
        this.orderId = orderId;
        this.customer = customer;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public double getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
}

// Supporting manager classes
class StatePersistenceManager {
    private final Map<String, GraphState> memoryStorage = new ConcurrentHashMap<>();

    public String persistToMemory(GraphState state) {
        String id = "mem_" + System.currentTimeMillis();
        memoryStorage.put(id, state.copy());
        return id;
    }

    public String persistToFile(GraphState state, String filePath) {
        // Simulate file persistence
        return "file_" + System.currentTimeMillis();
    }

    public String persistToDatabase(GraphState state, String tableName) {
        // Simulate database persistence
        return "db_" + System.currentTimeMillis();
    }

    public Optional<GraphState> restoreFromMemory(String id) {
        return Optional.ofNullable(memoryStorage.get(id));
    }

    public Optional<GraphState> restoreFromFile(String id) {
        // Simulate file restoration
        return Optional.of(new BasicGraphState("restored_from_file"));
    }

    public Optional<GraphState> restoreFromDatabase(String id) {
        // Simulate database restoration
        return Optional.of(new BasicGraphState("restored_from_database"));
    }

    public StateCompressionResult compressState(GraphState state) {
        // Simulate state compression
        long originalSize = state.getKeys().size() * 100; // Estimate
        long compressedSize = originalSize / 3; // Simulate 3:1 compression
        return new StateCompressionResult(originalSize, compressedSize);
    }
}

class StateCompressionResult {
    private final long originalSize;
    private final long compressedSize;

    public StateCompressionResult(long originalSize, long compressedSize) {
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
    }

    public long getOriginalSize() { return originalSize; }
    public long getCompressedSize() { return compressedSize; }
    public double getCompressionRatio() { return (double) compressedSize / originalSize; }
}

class StateCheckpointManager {
    private final List<CheckpointInfo> checkpoints = new ArrayList<>();
    private final Map<String, GraphState> checkpointData = new ConcurrentHashMap<>();

    public String createCheckpoint(String checkpointId, GraphState state, String description) {
        String fullId = checkpointId + "_" + System.currentTimeMillis();

        CheckpointInfo info = new CheckpointInfo(
            fullId,
            description,
            LocalDateTime.now(),
            state.getKeys().size() * 50 // Estimated size
        );

        checkpoints.add(info);
        checkpointData.put(fullId, state.copy());

        return fullId;
    }

    public Optional<GraphState> restoreCheckpoint(String checkpointId) {
        return Optional.ofNullable(checkpointData.get(checkpointId));
    }

    public List<CheckpointInfo> listCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public int cleanupOldCheckpoints(Duration maxAge) {
        LocalDateTime cutoff = LocalDateTime.now().minus(maxAge);
        int originalSize = checkpoints.size();

        checkpoints.removeIf(checkpoint -> checkpoint.getCreatedAt().isBefore(cutoff));

        return originalSize - checkpoints.size();
    }
}

class CheckpointInfo {
    private final String checkpointId;
    private final String description;
    private final LocalDateTime createdAt;
    private final long sizeBytes;

    public CheckpointInfo(String checkpointId, String description, LocalDateTime createdAt, long sizeBytes) {
        this.checkpointId = checkpointId;
        this.description = description;
        this.createdAt = createdAt;
        this.sizeBytes = sizeBytes;
    }

    public String getCheckpointId() { return checkpointId; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getSizeBytes() { return sizeBytes; }
}

class ParallelStateManager {
    public GraphState mergeParallelStates(GraphState baseState, List<GraphState> branchStates) {
        BasicGraphState merged = (BasicGraphState) baseState.copy();

        // Merge data from all branches
        for (GraphState branchState : branchStates) {
            for (String key : branchState.getKeys()) {
                if (!key.equals("branch_name") && !key.equals("branch_start_time") && !key.equals("branch_end_time")) {
                    merged.set(key, branchState.get(key, Object.class).orElse(null));
                }
            }
        }

        merged.set("parallel_processing_complete", true);
        merged.set("parallel_branches_count", branchStates.size());
        merged.set("parallel_merge_time", LocalDateTime.now().toString());

        return merged;
    }

    public GraphState resolveStateConflicts(List<GraphState> conflictingStates, ConflictResolutionStrategy strategy) {
        BasicGraphState resolved = new BasicGraphState("conflict_resolved");

        switch (strategy) {
            case HIGHEST_PRIORITY:
                // Find state with highest priority
                GraphState highest = conflictingStates.stream()
                    .max(Comparator.comparingInt(state ->
                        state.get("priority", Integer.class).orElse(0)))
                    .orElse(conflictingStates.get(0));

                // Copy all data from highest priority state
                for (String key : highest.getKeys()) {
                    resolved.set(key, highest.get(key, Object.class).orElse(null));
                }
                break;

            case MERGE_ALL:
                // Merge all non-conflicting data
                for (GraphState state : conflictingStates) {
                    for (String key : state.getKeys()) {
                        if (!resolved.get(key, Object.class).isPresent()) {
                            resolved.set(key, state.get(key, Object.class).orElse(null));
                        }
                    }
                }
                break;
        }

        return resolved;
    }
}

enum ConflictResolutionStrategy {
    HIGHEST_PRIORITY,
    MERGE_ALL,
    LATEST_TIMESTAMP
}

// Additional supporting classes for advanced patterns
class VersionedStateManager {
    private final Map<String, StateVersion> versions = new ConcurrentHashMap<>();

    public String createVersion(GraphState state, String description) {
        String versionId = "v" + System.currentTimeMillis();
        StateVersion version = new StateVersion(versionId, state.copy(), description, LocalDateTime.now());
        versions.put(versionId, version);
        return versionId;
    }

    public Optional<StateVersion> getVersion(String versionId) {
        return Optional.ofNullable(versions.get(versionId));
    }
}

class StateVersion {
    private final String versionId;
    private final GraphState state;
    private final String description;
    private final LocalDateTime createdAt;

    public StateVersion(String versionId, GraphState state, String description, LocalDateTime createdAt) {
        this.versionId = versionId;
        this.state = state;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getVersionId() { return versionId; }
    public GraphState getState() { return state; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

class StateTransactionManager {
    private final Map<String, GraphState> transactions = new ConcurrentHashMap<>();

    public String beginTransaction(GraphState state) {
        String transactionId = "tx_" + System.currentTimeMillis();
        transactions.put(transactionId, state.copy());
        return transactionId;
    }

    public void commitTransaction(String transactionId, GraphState finalState) {
        transactions.remove(transactionId);
        // In real implementation, would persist the final state
    }

    public void rollbackTransaction(String transactionId) {
        GraphState originalState = transactions.remove(transactionId);
        // In real implementation, would restore the original state
    }
}

class StateEventSourcing {
    private final Map<String, List<StateEvent>> eventStreams = new ConcurrentHashMap<>();

    public void recordEvent(String streamId, String eventType, Map<String, Object> eventData) {
        StateEvent event = new StateEvent(eventType, eventData, LocalDateTime.now());
        eventStreams.computeIfAbsent(streamId, k -> new ArrayList<>()).add(event);
    }

    public GraphState replayEvents(String streamId) {
        List<StateEvent> events = eventStreams.getOrDefault(streamId, List.of());
        BasicGraphState state = new BasicGraphState("replayed_from_events");

        for (StateEvent event : events) {
            applyEvent(state, event);
        }

        return state;
    }

    public List<StateEvent> getEventHistory(String streamId) {
        return new ArrayList<>(eventStreams.getOrDefault(streamId, List.of()));
    }

    private void applyEvent(BasicGraphState state, StateEvent event) {
        switch (event.getEventType()) {
            case "user_created":
                state.set("user_id", event.getEventData().get("user_id"));
                state.set("name", event.getEventData().get("name"));
                break;
            case "email_updated":
                state.set("email", event.getEventData().get("new_email"));
                break;
            case "preferences_set":
                state.set("theme", event.getEventData().get("theme"));
                break;
            case "login_recorded":
                state.set("last_login", event.getEventData().get("timestamp"));
                break;
        }
    }
}

class StateEvent {
    private final String eventType;
    private final Map<String, Object> eventData;
    private final LocalDateTime timestamp;

    public StateEvent(String eventType, Map<String, Object> eventData, LocalDateTime timestamp) {
        this.eventType = eventType;
        this.eventData = new HashMap<>(eventData);
        this.timestamp = timestamp;
    }

    public String getEventType() { return eventType; }
    public Map<String, Object> getEventData() { return new HashMap<>(eventData); }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class StateCacheManager {
    private final Map<String, CachedState> cache = new ConcurrentHashMap<>();
    private long hitCount = 0;
    private long totalAccess = 0;

    public String cacheState(GraphState state, Duration ttl) {
        String cacheKey = "cache_" + System.currentTimeMillis();
        LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);
        CachedState cached = new CachedState(state.copy(), expiresAt);
        cache.put(cacheKey, cached);
        return cacheKey;
    }

    public Optional<GraphState> getCachedState(String cacheKey) {
        totalAccess++;
        CachedState cached = cache.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            hitCount++;
            return Optional.of(cached.getState());
        } else {
            cache.remove(cacheKey); // Remove expired entry
            return Optional.empty();
        }
    }

    public CacheStats getCacheStats() {
        double hitRate = totalAccess > 0 ? (double) hitCount / totalAccess : 0.0;
        long memoryUsage = cache.size() * 1000; // Estimate
        return new CacheStats(cache.size(), hitRate, memoryUsage);
    }
}

class CachedState {
    private final GraphState state;
    private final LocalDateTime expiresAt;

    public CachedState(GraphState state, LocalDateTime expiresAt) {
        this.state = state;
        this.expiresAt = expiresAt;
    }

    public GraphState getState() { return state; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}

class CacheStats {
    private final long entryCount;
    private final double hitRate;
    private final long memoryUsageBytes;

    public CacheStats(long entryCount, double hitRate, long memoryUsageBytes) {
        this.entryCount = entryCount;
        this.hitRate = hitRate;
        this.memoryUsageBytes = memoryUsageBytes;
    }

    public long getEntryCount() { return entryCount; }
    public double getHitRate() { return hitRate; }
    public long getMemoryUsageBytes() { return memoryUsageBytes; }
}
