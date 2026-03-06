/**
 * LangGraph StateGraph Examples - Complete Implementation
 * Demonstrates graph architecture and execution patterns
 */
package com.example.agent.langgraph.graph;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Comprehensive StateGraph demonstration
 */
public class StateGraphExampleSuite {

    public static void main(String[] args) {
        System.out.println("=== LangGraph StateGraph Examples ===");

        try {
            // Run comprehensive graph examples
            runBasicStateGraphExample();
            runConditionalRoutingExample();
            runParallelExecutionExample();
            runLoopWorkflowExample();
            runComplexDocumentWorkflow();
            runErrorHandlingExample();
            runProductionWorkflowExample();

        } catch (Exception e) {
            System.err.println("Error running StateGraph examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Basic StateGraph creation and execution
     */
    private static void runBasicStateGraphExample() throws Exception {
        System.out.println("\n1. Basic StateGraph Example:");

        // Create a simple StateGraph
        StateGraph<BasicGraphState> simpleGraph = StateGraph.<BasicGraphState>builder()
            .withStateType(BasicGraphState.class)
            .withName("basic_example")
            .build();

        // Add simple nodes
        simpleGraph.addNode("start", new StartNode());
        simpleGraph.addNode("process", new ProcessingNode());
        simpleGraph.addNode("end", new EndNode());

        // Add simple edges
        simpleGraph.addEdge("start", "process");
        simpleGraph.addEdge("process", "end");

        // Set entry and exit points
        simpleGraph.setEntryPoint("start");
        simpleGraph.setFinishPoint("end");

        // Compile the graph
        CompiledGraph<BasicGraphState> compiled = simpleGraph.compile();

        // Create initial state
        BasicGraphState initialState = new BasicGraphState("basic_workflow");
        initialState.set("input_message", "Hello, LangGraph!");
        initialState.set("timestamp", LocalDateTime.now().toString());

        System.out.println("Executing basic graph with state: " + initialState.getKeys());

        // Execute the graph
        GraphExecutionResult<BasicGraphState> result = compiled.invoke(initialState);

        System.out.println("Execution completed:");
        System.out.println("  Status: " + result.getStatus());
        System.out.println("  Final state keys: " + result.getFinalState().getKeys());
        System.out.println("  Processing result: " + result.getFinalState().get("processing_result", String.class).orElse("none"));
        System.out.println("  Execution time: " + result.getExecutionTime().toMillis() + "ms");
    }

    /**
     * Conditional routing based on state
     */
    private static void runConditionalRoutingExample() throws Exception {
        System.out.println("\n2. Conditional Routing Example:");

        StateGraph<BasicGraphState> conditionalGraph = StateGraph.<BasicGraphState>builder()
            .withStateType(BasicGraphState.class)
            .withName("conditional_routing")
            .build();

        // Add nodes for different paths
        conditionalGraph.addNode("input", new InputValidationNode());
        conditionalGraph.addNode("process_valid", new ValidDataProcessingNode());
        conditionalGraph.addNode("process_invalid", new InvalidDataHandlingNode());
        conditionalGraph.addNode("output", new OutputNode());
        conditionalGraph.addNode("error_output", new ErrorOutputNode());

        // Add conditional edges based on validation result
        conditionalGraph.addConditionalEdges("input",
            Arrays.asList(
                EdgeConditions.fieldEquals("validation_passed", true, "process_valid"),
                EdgeConditions.fieldEquals("validation_passed", false, "process_invalid")
            ),
            "error_output" // Default if no condition matches
        );

        // Continue from processing to outputs
        conditionalGraph.addEdge("process_valid", "output");
        conditionalGraph.addEdge("process_invalid", "error_output");

        conditionalGraph.setEntryPoint("input");
        conditionalGraph.addFinishPoint("output");
        conditionalGraph.addFinishPoint("error_output");

        CompiledGraph<BasicGraphState> compiled = conditionalGraph.compile();

        // Test with valid data
        System.out.println("Testing with valid data:");
        BasicGraphState validState = new BasicGraphState("conditional_test");
        validState.set("input_data", "valid@email.com");
        validState.set("data_type", "email");

        GraphExecutionResult<BasicGraphState> validResult = compiled.invoke(validState);
        System.out.println("  Result: " + validResult.getStatus());
        System.out.println("  Validation passed: " + validResult.getFinalState().get("validation_passed", Boolean.class).orElse(false));
        System.out.println("  Output: " + validResult.getFinalState().get("output_message", String.class).orElse("none"));

        // Test with invalid data
        System.out.println("\nTesting with invalid data:");
        BasicGraphState invalidState = new BasicGraphState("conditional_test");
        invalidState.set("input_data", "invalid_email");
        invalidState.set("data_type", "email");

        GraphExecutionResult<BasicGraphState> invalidResult = compiled.invoke(invalidState);
        System.out.println("  Result: " + invalidResult.getStatus());
        System.out.println("  Validation passed: " + invalidResult.getFinalState().get("validation_passed", Boolean.class).orElse(false));
        System.out.println("  Error message: " + invalidResult.getFinalState().get("error_message", String.class).orElse("none"));
    }

    /**
     * Parallel execution with merge
     */
    private static void runParallelExecutionExample() throws Exception {
        System.out.println("\n3. Parallel Execution Example:");

        StateGraph<BasicGraphState> parallelGraph = StateGraph.<BasicGraphState>builder()
            .withStateType(BasicGraphState.class)
            .withName("parallel_processing")
            .build();

        // Add nodes for parallel processing
        parallelGraph.addNode("input", new InputSplittingNode());
        parallelGraph.addNode("analyze_text", new TextAnalysisNode());
        parallelGraph.addNode("extract_entities", new EntityExtractionNode());
        parallelGraph.addNode("sentiment_analysis", new SentimentAnalysisNode());
        parallelGraph.addNode("merge_results", new ResultMergingNode());
        parallelGraph.addNode("output", new FinalOutputNode());

        // Split input to parallel processing
        parallelGraph.addParallelEdges("input",
            Arrays.asList("analyze_text", "extract_entities", "sentiment_analysis"));

        // Merge parallel results
        parallelGraph.addMergeEdges(
            Arrays.asList("analyze_text", "extract_entities", "sentiment_analysis"),
            "merge_results"
        );

        parallelGraph.addEdge("merge_results", "output");

        parallelGraph.setEntryPoint("input");
        parallelGraph.setFinishPoint("output");

        CompiledGraph<BasicGraphState> compiled = parallelGraph.compile();

        // Create state for parallel processing
        BasicGraphState parallelState = new BasicGraphState("parallel_workflow");
        parallelState.set("text_content", "LangGraph is an amazing framework for building complex AI workflows. " +
                                         "It provides powerful graph-based execution with state management. " +
                                         "The parallel processing capabilities are particularly impressive.");

        System.out.println("Executing parallel processing workflow...");
        long startTime = System.currentTimeMillis();

        GraphExecutionResult<BasicGraphState> result = compiled.invoke(parallelState);

        long endTime = System.currentTimeMillis();

        System.out.println("Parallel execution completed:");
        System.out.println("  Status: " + result.getStatus());
        System.out.println("  Execution time: " + (endTime - startTime) + "ms");
        System.out.println("  Text analysis: " + result.getFinalState().get("text_analysis", Map.class).orElse(Map.of()));
        System.out.println("  Entities: " + result.getFinalState().get("entities", List.class).orElse(List.of()));
        System.out.println("  Sentiment: " + result.getFinalState().get("sentiment_score", Double.class).orElse(0.0));
        System.out.println("  Merged results: " + result.getFinalState().get("merged_insights", String.class).orElse("none"));
    }

    /**
     * Loop workflow with conditional exit
     */
    private static void runLoopWorkflowExample() throws Exception {
        System.out.println("\n4. Loop Workflow Example:");

        StateGraph<BasicGraphState> loopGraph = StateGraph.<BasicGraphState>builder()
            .withStateType(BasicGraphState.class)
            .withName("iterative_processing")
            .build();

        // Add nodes for iterative processing
        loopGraph.addNode("initialize", new InitializationNode());
        loopGraph.addNode("process_iteration", new IterationProcessingNode());
        loopGraph.addNode("check_convergence", new ConvergenceCheckNode());
        loopGraph.addNode("finalize", new FinalizationNode());

        // Initial flow
        loopGraph.addEdge("initialize", "process_iteration");

        // Loop or exit based on convergence
        loopGraph.addConditionalEdges("process_iteration",
            Arrays.asList(
                EdgeConditions.custom((state) -> {
                    Integer iteration = state.get("iteration", Integer.class).orElse(0);
                    Double error = state.get("current_error", Double.class).orElse(1.0);
                    Boolean converged = state.get("converged", Boolean.class).orElse(false);

                    // Continue if not converged and under max iterations
                    if (!converged && iteration < 10 && error > 0.01) {
                        return "check_convergence";
                    } else {
                        return "finalize";
                    }
                })
            ),
            "finalize" // Default exit
        );

        // Loop back for another iteration
        loopGraph.addConditionalEdges("check_convergence",
            Arrays.asList(
                EdgeConditions.fieldEquals("converged", false, "process_iteration"),
                EdgeConditions.fieldEquals("converged", true, "finalize")
            ),
            "finalize"
        );

        loopGraph.setEntryPoint("initialize");
        loopGraph.setFinishPoint("finalize");

        CompiledGraph<BasicGraphState> compiled = loopGraph.compile();

        // Create initial state for iterative processing
        BasicGraphState iterativeState = new BasicGraphState("iterative_workflow");
        iterativeState.set("target_value", 42.0);
        iterativeState.set("initial_guess", 1.0);
        iterativeState.set("tolerance", 0.01);

        System.out.println("Starting iterative processing...");
        System.out.println("Target: " + iterativeState.get("target_value", Double.class).orElse(0.0));

        GraphExecutionResult<BasicGraphState> result = compiled.invoke(iterativeState);

        System.out.println("Iterative processing completed:");
        System.out.println("  Status: " + result.getStatus());
        System.out.println("  Final iteration: " + result.getFinalState().get("iteration", Integer.class).orElse(0));
        System.out.println("  Final value: " + result.getFinalState().get("current_value", Double.class).orElse(0.0));
        System.out.println("  Final error: " + result.getFinalState().get("current_error", Double.class).orElse(0.0));
        System.out.println("  Converged: " + result.getFinalState().get("converged", Boolean.class).orElse(false));
    }

    /**
     * Complex document processing workflow
     */
    private static void runComplexDocumentWorkflow() throws Exception {
        System.out.println("\n5. Complex Document Processing Workflow:");

        StateGraph<BasicGraphState> docGraph = StateGraph.<BasicGraphState>builder()
            .withStateType(BasicGraphState.class)
            .withName("document_processing")
            .build();

        // Build complex document processing workflow
        docGraph.addNode("validate_input", new DocumentValidationNode());
        docGraph.addNode("parse_document", new DocumentParsingNode());
        docGraph.addNode("extract_metadata", new MetadataExtractionNode());
        docGraph.addNode("content_analysis", new ContentAnalysisNode());
        docGraph.addNode("quality_check", new QualityCheckNode());
        docGraph.addNode("enhance_content", new ContentEnhancementNode());
        docGraph.addNode("generate_summary", new SummaryGenerationNode());
        docGraph.addNode("create_output", new OutputCreationNode());
        docGraph.addNode("handle_error", new ErrorHandlingNode());

        // Validation flow
        docGraph.addConditionalEdges("validate_input",
            Arrays.asList(
                EdgeConditions.fieldEquals("validation_passed", true, "parse_document"),
                EdgeConditions.fieldEquals("validation_passed", false, "handle_error")
            ),
            "handle_error"
        );

        // Parsing flow
        docGraph.addConditionalEdges("parse_document",
            Arrays.asList(
                EdgeConditions.fieldEquals("parsing_successful", true, "extract_metadata"),
                EdgeConditions.fieldEquals("parsing_successful", false, "handle_error")
            ),
            "handle_error"
        );

        // Parallel metadata and content analysis
        docGraph.addParallelEdges("extract_metadata",
            Arrays.asList("content_analysis"));

        // Wait for both parallel tasks
        docGraph.addMergeEdges(
            Arrays.asList("extract_metadata", "content_analysis"),
            "quality_check"
        );

        // Quality-based routing
        docGraph.addConditionalEdges("quality_check",
            Arrays.asList(
                EdgeConditions.custom((state) -> {
                    Double quality = state.get("content_quality", Double.class).orElse(0.0);
                    if (quality >= 0.8) {
                        return "generate_summary";
                    } else if (quality >= 0.5) {
                        return "enhance_content";
                    } else {
                        return "handle_error";
                    }
                })
            ),
            "handle_error"
        );

        // Enhancement flow
        docGraph.addEdge("enhance_content", "generate_summary");
        docGraph.addEdge("generate_summary", "create_output");

        docGraph.setEntryPoint("validate_input");
        docGraph.addFinishPoint("create_output");
        docGraph.addFinishPoint("handle_error");

        CompiledGraph<BasicGraphState> compiled = docGraph.compile();

        // Create document processing state
        BasicGraphState documentState = new BasicGraphState("document_workflow");
        documentState.set("document_path", "/path/to/document.pdf");
        documentState.set("document_type", "pdf");
        documentState.set("processing_options", Map.of(
            "extract_images", true,
            "preserve_formatting", true,
            "quality_threshold", 0.7
        ));

        System.out.println("Processing document workflow...");

        GraphExecutionResult<BasicGraphState> result = compiled.invoke(documentState);

        System.out.println("Document processing completed:");
        System.out.println("  Status: " + result.getStatus());
        System.out.println("  Processing stages: " + result.getFinalState().get("completed_stages", List.class).orElse(List.of()));
        System.out.println("  Content quality: " + result.getFinalState().get("content_quality", Double.class).orElse(0.0));
        System.out.println("  Summary generated: " + (result.getFinalState().get("summary", String.class).isPresent() ? "Yes" : "No"));
        System.out.println("  Output location: " + result.getFinalState().get("output_path", String.class).orElse("none"));
    }

    /**
     * Error handling and recovery patterns
     */
    private static void runErrorHandlingExample() throws Exception {
        System.out.println("\n6. Error Handling and Recovery:");

        StateGraph<BasicGraphState> errorGraph = StateGraph.<BasicGraphState>builder()
            .withStateType(BasicGraphState.class)
            .withName("error_handling")
            .withErrorHandler(new GraphErrorHandler())
            .build();

        // Add nodes with potential failures
        errorGraph.addNode("risky_operation", new RiskyOperationNode());
        errorGraph.addNode("retry_operation", new RetryOperationNode());
        errorGraph.addNode("fallback_operation", new FallbackOperationNode());
        errorGraph.addNode("success_path", new SuccessProcessingNode());
        errorGraph.addNode("error_recovery", new ErrorRecoveryNode());
        errorGraph.addNode("final_cleanup", new CleanupNode());

        // Main operation with error handling
        errorGraph.addConditionalEdges("risky_operation",
            Arrays.asList(
                EdgeConditions.fieldEquals("operation_successful", true, "success_path"),
                EdgeConditions.fieldEquals("operation_successful", false, "retry_operation")
            ),
            "error_recovery"
        );

        // Retry logic
        errorGraph.addConditionalEdges("retry_operation",
            Arrays.asList(
                EdgeConditions.custom((state) -> {
                    Integer retryCount = state.get("retry_count", Integer.class).orElse(0);
                    Boolean lastRetrySuccess = state.get("retry_successful", Boolean.class).orElse(false);

                    if (lastRetrySuccess) {
                        return "success_path";
                    } else if (retryCount < 3) {
                        return "risky_operation"; // Try again
                    } else {
                        return "fallback_operation"; // Give up, use fallback
                    }
                })
            ),
            "error_recovery"
        );

        // Continue to cleanup
        errorGraph.addEdge("success_path", "final_cleanup");
        errorGraph.addEdge("fallback_operation", "final_cleanup");
        errorGraph.addEdge("error_recovery", "final_cleanup");

        errorGraph.setEntryPoint("risky_operation");
        errorGraph.setFinishPoint("final_cleanup");

        CompiledGraph<BasicGraphState> compiled = errorGraph.compile();

        // Test error scenarios
        for (int scenario = 1; scenario <= 3; scenario++) {
            System.out.println("\n  Scenario " + scenario + ":");

            BasicGraphState errorState = new BasicGraphState("error_test_" + scenario);
            errorState.set("failure_rate", scenario == 1 ? 0.0 : scenario == 2 ? 0.7 : 1.0); // Different failure rates
            errorState.set("operation_type", "network_call");
            errorState.set("max_retries", 3);

            GraphExecutionResult<BasicGraphState> result = compiled.invoke(errorState);

            System.out.println("    Status: " + result.getStatus());
            System.out.println("    Path taken: " + result.getFinalState().get("execution_path", List.class).orElse(List.of()));
            System.out.println("    Retry count: " + result.getFinalState().get("retry_count", Integer.class).orElse(0));
            System.out.println("    Final result: " + result.getFinalState().get("final_result", String.class).orElse("unknown"));
        }
    }

    /**
     * Production workflow with monitoring and metrics
     */
    private static void runProductionWorkflowExample() throws Exception {
        System.out.println("\n7. Production Workflow with Monitoring:");

        StateGraph<BasicGraphState> prodGraph = StateGraph.<BasicGraphState>builder()
            .withStateType(BasicGraphState.class)
            .withName("production_workflow")
            .withMetrics(new WorkflowMetrics())
            .withLogging(true)
            .build();

        // Production-ready nodes
        prodGraph.addNode("health_check", new HealthCheckNode());
        prodGraph.addNode("load_balancer", new LoadBalancerNode());
        prodGraph.addNode("data_processor", new DataProcessorNode());
        prodGraph.addNode("cache_manager", new CacheManagerNode());
        prodGraph.addNode("database_writer", new DatabaseWriterNode());
        prodGraph.addNode("notification_sender", new NotificationSenderNode());
        prodGraph.addNode("metrics_collector", new MetricsCollectorNode());

        // Health check first
        prodGraph.addConditionalEdges("health_check",
            Arrays.asList(
                EdgeConditions.fieldEquals("system_healthy", true, "load_balancer"),
                EdgeConditions.fieldEquals("system_healthy", false, "metrics_collector")
            ),
            "metrics_collector"
        );

        // Load balancing to processing
        prodGraph.addEdge("load_balancer", "data_processor");

        // Parallel cache and database operations
        prodGraph.addParallelEdges("data_processor",
            Arrays.asList("cache_manager", "database_writer"));

        // Wait for both operations
        prodGraph.addMergeEdges(
            Arrays.asList("cache_manager", "database_writer"),
            "notification_sender"
        );

        prodGraph.addEdge("notification_sender", "metrics_collector");

        prodGraph.setEntryPoint("health_check");
        prodGraph.setFinishPoint("metrics_collector");

        CompiledGraph<BasicGraphState> compiled = prodGraph.compile();

        // Production request simulation
        BasicGraphState productionState = new BasicGraphState("production_request");
        productionState.set("request_id", "req_" + System.currentTimeMillis());
        productionState.set("user_id", "user_12345");
        productionState.set("request_data", Map.of(
            "action", "process_document",
            "document_id", "doc_789",
            "priority", "high"
        ));
        productionState.set("request_timestamp", LocalDateTime.now().toString());

        System.out.println("Processing production request: " + productionState.get("request_id", String.class).orElse("unknown"));

        long startTime = System.currentTimeMillis();
        GraphExecutionResult<BasicGraphState> result = compiled.invoke(productionState);
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("Production workflow completed:");
        System.out.println("  Status: " + result.getStatus());
        System.out.println("  Total time: " + totalTime + "ms");
        System.out.println("  System health: " + result.getFinalState().get("system_healthy", Boolean.class).orElse(false));
        System.out.println("  Load balancer node: " + result.getFinalState().get("assigned_node", String.class).orElse("unknown"));
        System.out.println("  Processing result: " + result.getFinalState().get("processing_status", String.class).orElse("unknown"));
        System.out.println("  Cache hit: " + result.getFinalState().get("cache_hit", Boolean.class).orElse(false));
        System.out.println("  Database written: " + result.getFinalState().get("database_success", Boolean.class).orElse(false));
        System.out.println("  Notifications sent: " + result.getFinalState().get("notifications_sent", Integer.class).orElse(0));

        // Display metrics
        Map<String, Object> metrics = result.getFinalState().get("workflow_metrics", Map.class).orElse(Map.of());
        if (!metrics.isEmpty()) {
            System.out.println("  Workflow metrics:");
            metrics.forEach((key, value) -> System.out.println("    " + key + ": " + value));
        }
    }
}

/**
 * Basic nodes for examples
 */
class StartNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("execution_started", LocalDateTime.now().toString());
        state.set("node_path", Arrays.asList("start"));

        System.out.println("    [START] Workflow initiated");
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "start"; }
}

class ProcessingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String inputMessage = state.get("input_message", String.class).orElse("");

        // Simulate processing
        Thread.sleep(100);

        String result = "Processed: " + inputMessage.toUpperCase();
        state.set("processing_result", result);
        state.set("processing_timestamp", LocalDateTime.now().toString());

        List<String> path = state.get("node_path", List.class).orElse(new ArrayList<>());
        path.add("process");
        state.set("node_path", path);

        System.out.println("    [PROCESS] Message processed: " + result);
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "process"; }
}

class EndNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("execution_completed", LocalDateTime.now().toString());

        List<String> path = state.get("node_path", List.class).orElse(new ArrayList<>());
        path.add("end");
        state.set("node_path", path);

        System.out.println("    [END] Workflow completed");
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "end"; }
}

// Input validation node
class InputValidationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String inputData = state.get("input_data", String.class).orElse("");
        String dataType = state.get("data_type", String.class).orElse("");

        boolean isValid = false;
        String validationError = null;

        switch (dataType) {
            case "email":
                isValid = inputData.contains("@") && inputData.contains(".");
                validationError = isValid ? null : "Invalid email format";
                break;
            case "phone":
                isValid = inputData.matches("\\d{10,}");
                validationError = isValid ? null : "Invalid phone number format";
                break;
            default:
                isValid = !inputData.trim().isEmpty();
                validationError = isValid ? null : "Input data is empty";
        }

        state.set("validation_passed", isValid);
        if (!isValid) {
            state.set("validation_error", validationError);
        }

        System.out.println("    [VALIDATION] Input '" + inputData + "' validation: " +
                         (isValid ? "PASSED" : "FAILED - " + validationError));

        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "input_validation"; }
}

class ValidDataProcessingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String inputData = state.get("input_data", String.class).orElse("");

        // Simulate processing valid data
        Thread.sleep(80);

        String processedData = "PROCESSED_VALID: " + inputData.toUpperCase();
        state.set("processed_data", processedData);
        state.set("output_message", "Data processed successfully");

        System.out.println("    [VALID_PROCESS] Processed valid data: " + processedData);
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "valid_data_processing"; }
}

class InvalidDataHandlingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String validationError = state.get("validation_error", String.class).orElse("Unknown validation error");

        state.set("error_handled", true);
        state.set("error_message", "Data validation failed: " + validationError);
        state.set("suggested_action", "Please check input format and try again");

        System.out.println("    [INVALID_HANDLE] Handled invalid data: " + validationError);
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "invalid_data_handling"; }
}

class OutputNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String outputMessage = state.get("output_message", String.class).orElse("Processing completed");
        state.set("final_output", outputMessage);
        state.set("output_timestamp", LocalDateTime.now().toString());

        System.out.println("    [OUTPUT] " + outputMessage);
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "output"; }
}

class ErrorOutputNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String errorMessage = state.get("error_message", String.class).orElse("Unknown error occurred");
        state.set("final_error", errorMessage);
        state.set("error_timestamp", LocalDateTime.now().toString());

        System.out.println("    [ERROR_OUTPUT] " + errorMessage);
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "error_output"; }
}

// Parallel processing nodes
class InputSplittingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String textContent = state.get("text_content", String.class).orElse("");

        // Split content for parallel processing
        String[] sentences = textContent.split("\\. ");
        state.set("sentences", Arrays.asList(sentences));
        state.set("word_count", textContent.split("\\s+").length);
        state.set("char_count", textContent.length());

        System.out.println("    [INPUT_SPLIT] Split text into " + sentences.length + " sentences");
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "input_splitting"; }
}

class TextAnalysisNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        // Simulate text analysis processing
        Thread.sleep(150);

        Integer wordCount = state.get("word_count", Integer.class).orElse(0);
        Integer charCount = state.get("char_count", Integer.class).orElse(0);

        Map<String, Object> analysis = Map.of(
            "word_count", wordCount,
            "char_count", charCount,
            "avg_word_length", charCount > 0 ? (double) charCount / wordCount : 0.0,
            "readability_score", 7.5,
            "complexity_level", "moderate"
        );

        state.set("text_analysis", analysis);
        System.out.println("    [TEXT_ANALYSIS] Completed - " + wordCount + " words, readability: 7.5");

        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "text_analysis"; }
}

class EntityExtractionNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        // Simulate entity extraction
        Thread.sleep(120);

        String textContent = state.get("text_content", String.class).orElse("");

        // Simple entity extraction simulation
        List<Map<String, String>> entities = Arrays.asList(
            Map.of("entity", "LangGraph", "type", "TECHNOLOGY", "confidence", "0.95"),
            Map.of("entity", "AI workflows", "type", "CONCEPT", "confidence", "0.87"),
            Map.of("entity", "framework", "type", "TOOL", "confidence", "0.92")
        );

        state.set("entities", entities);
        System.out.println("    [ENTITY_EXTRACTION] Found " + entities.size() + " entities");

        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "entity_extraction"; }
}

class SentimentAnalysisNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        // Simulate sentiment analysis
        Thread.sleep(100);

        // Mock positive sentiment for the example text
        double sentimentScore = 0.78; // Positive sentiment
        String sentimentLabel = sentimentScore > 0.6 ? "POSITIVE" :
                               sentimentScore > 0.4 ? "NEUTRAL" : "NEGATIVE";

        state.set("sentiment_score", sentimentScore);
        state.set("sentiment_label", sentimentLabel);
        state.set("sentiment_confidence", 0.83);

        System.out.println("    [SENTIMENT_ANALYSIS] " + sentimentLabel + " (" +
                         String.format("%.2f", sentimentScore) + ")");

        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "sentiment_analysis"; }
}

class ResultMergingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        // Merge results from parallel processing
        Map<String, Object> textAnalysis = state.get("text_analysis", Map.class).orElse(Map.of());
        List<Map> entities = state.get("entities", List.class).orElse(List.of());
        String sentimentLabel = state.get("sentiment_label", String.class).orElse("NEUTRAL");

        String mergedInsights = String.format(
            "Text contains %s words with %s sentiment. Found %d entities. Readability score: %s",
            textAnalysis.get("word_count"),
            sentimentLabel.toLowerCase(),
            entities.size(),
            textAnalysis.get("readability_score")
        );

        state.set("merged_insights", mergedInsights);
        state.set("analysis_complete", true);

        System.out.println("    [RESULT_MERGE] Merged parallel analysis results");
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "result_merging"; }
}

class FinalOutputNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String insights = state.get("merged_insights", String.class).orElse("No insights available");

        state.set("final_output", insights);
        state.set("processing_completed", LocalDateTime.now().toString());

        System.out.println("    [FINAL_OUTPUT] Processing pipeline completed");
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "final_output"; }
}

// Additional nodes for other examples would be implemented similarly...
// For brevity, I'm including placeholders for the remaining node types

class InitializationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("iteration", 0);
        state.set("current_value", state.get("initial_guess", Double.class).orElse(1.0));
        state.set("converged", false);

        System.out.println("    [INIT] Iterative process initialized");
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "initialization"; }
}

class IterationProcessingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Integer iteration = state.get("iteration", Integer.class).orElse(0);
        Double currentValue = state.get("current_value", Double.class).orElse(1.0);
        Double targetValue = state.get("target_value", Double.class).orElse(42.0);

        // Simple iteration: Newton's method approximation
        double newValue = (currentValue + targetValue / currentValue) / 2;
        double error = Math.abs(newValue - targetValue);

        state.set("iteration", iteration + 1);
        state.set("current_value", newValue);
        state.set("current_error", error);

        System.out.println("    [ITERATION " + (iteration + 1) + "] Value: " +
                         String.format("%.4f", newValue) + ", Error: " + String.format("%.6f", error));

        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "iteration_processing"; }
}

class ConvergenceCheckNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Double currentError = state.get("current_error", Double.class).orElse(1.0);
        Double tolerance = state.get("tolerance", Double.class).orElse(0.01);

        boolean converged = currentError <= tolerance;
        state.set("converged", converged);

        System.out.println("    [CONVERGENCE_CHECK] " + (converged ? "CONVERGED" : "CONTINUE"));
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "convergence_check"; }
}

class FinalizationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Integer finalIteration = state.get("iteration", Integer.class).orElse(0);
        Double finalValue = state.get("current_value", Double.class).orElse(0.0);
        Boolean converged = state.get("converged", Boolean.class).orElse(false);

        state.set("finalization_complete", true);
        state.set("final_summary", String.format(
            "Completed in %d iterations. Final value: %.4f. Converged: %s",
            finalIteration, finalValue, converged
        ));

        System.out.println("    [FINALIZE] " + state.get("final_summary", String.class).orElse(""));
        return NodeResult.success(state);
    }

    @Override
    public String getName() { return "finalization"; }
}

// Placeholder implementations for remaining nodes
// (In a real implementation, these would have full business logic)

class DocumentValidationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("validation_passed", true);
        state.set("completed_stages", Arrays.asList("validation"));
        System.out.println("    [DOC_VALIDATION] Document validation passed");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "document_validation"; }
}

class DocumentParsingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("parsing_successful", true);
        state.set("extracted_text", "Sample document content...");
        List<String> stages = state.get("completed_stages", List.class).orElse(new ArrayList<>());
        stages.add("parsing");
        state.set("completed_stages", stages);
        System.out.println("    [DOC_PARSING] Document parsed successfully");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "document_parsing"; }
}

class MetadataExtractionNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Thread.sleep(100); // Simulate processing time
        state.set("metadata", Map.of("title", "Sample Document", "author", "AI System", "pages", 5));
        System.out.println("    [METADATA] Metadata extracted");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "metadata_extraction"; }
}

class ContentAnalysisNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Thread.sleep(120); // Simulate processing time
        state.set("content_quality", 0.85);
        state.set("topic_analysis", Arrays.asList("technology", "automation", "efficiency"));
        System.out.println("    [CONTENT_ANALYSIS] Content analysis completed");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "content_analysis"; }
}

class QualityCheckNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Double quality = state.get("content_quality", Double.class).orElse(0.5);
        state.set("quality_check_passed", quality >= 0.7);
        System.out.println("    [QUALITY_CHECK] Quality score: " + String.format("%.2f", quality));
        return NodeResult.success(state);
    }
    @Override public String getName() { return "quality_check"; }
}

class ContentEnhancementNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("content_enhanced", true);
        state.set("content_quality", 0.9); // Improved quality
        System.out.println("    [ENHANCEMENT] Content enhanced");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "content_enhancement"; }
}

class SummaryGenerationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("summary", "This is an auto-generated summary of the processed document content.");
        System.out.println("    [SUMMARY] Summary generated");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "summary_generation"; }
}

class OutputCreationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("output_path", "/output/processed_document.json");
        state.set("output_created", true);
        System.out.println("    [OUTPUT_CREATION] Output created successfully");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "output_creation"; }
}

class ErrorHandlingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("error_handled", true);
        state.set("error_message", "Error in document processing workflow");
        System.out.println("    [ERROR_HANDLING] Error handled gracefully");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "error_handling"; }
}

// Error handling and recovery nodes
class RiskyOperationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Double failureRate = state.get("failure_rate", Double.class).orElse(0.3);
        boolean success = Math.random() > failureRate;

        state.set("operation_successful", success);
        List<String> path = state.get("execution_path", List.class).orElse(new ArrayList<>());
        path.add("risky_operation");
        state.set("execution_path", path);

        System.out.println("    [RISKY_OP] Operation " + (success ? "succeeded" : "failed"));
        return NodeResult.success(state);
    }
    @Override public String getName() { return "risky_operation"; }
}

class RetryOperationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Integer retryCount = state.get("retry_count", Integer.class).orElse(0);
        state.set("retry_count", retryCount + 1);

        // Improved success rate on retry
        boolean success = Math.random() > 0.4;
        state.set("retry_successful", success);

        List<String> path = state.get("execution_path", List.class).orElse(new ArrayList<>());
        path.add("retry_" + (retryCount + 1));
        state.set("execution_path", path);

        System.out.println("    [RETRY " + (retryCount + 1) + "] " + (success ? "succeeded" : "failed"));
        return NodeResult.success(state);
    }
    @Override public String getName() { return "retry_operation"; }
}

class FallbackOperationNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("fallback_used", true);
        state.set("final_result", "fallback_result");

        List<String> path = state.get("execution_path", List.class).orElse(new ArrayList<>());
        path.add("fallback");
        state.set("execution_path", path);

        System.out.println("    [FALLBACK] Fallback operation executed");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "fallback_operation"; }
}

class SuccessProcessingNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("final_result", "success_result");

        List<String> path = state.get("execution_path", List.class).orElse(new ArrayList<>());
        path.add("success");
        state.set("execution_path", path);

        System.out.println("    [SUCCESS] Success path executed");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "success_processing"; }
}

class ErrorRecoveryNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("error_recovered", true);
        state.set("final_result", "error_recovery_result");

        List<String> path = state.get("execution_path", List.class).orElse(new ArrayList<>());
        path.add("error_recovery");
        state.set("execution_path", path);

        System.out.println("    [ERROR_RECOVERY] Error recovery executed");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "error_recovery"; }
}

class CleanupNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        state.set("cleanup_completed", true);
        state.set("workflow_end", LocalDateTime.now().toString());

        List<String> path = state.get("execution_path", List.class).orElse(new ArrayList<>());
        path.add("cleanup");
        state.set("execution_path", path);

        System.out.println("    [CLEANUP] Cleanup completed");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "cleanup"; }
}

// Production nodes
class HealthCheckNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        // Simulate system health check
        boolean healthy = Math.random() > 0.1; // 90% healthy
        state.set("system_healthy", healthy);
        state.set("health_check_timestamp", LocalDateTime.now().toString());

        System.out.println("    [HEALTH_CHECK] System status: " + (healthy ? "HEALTHY" : "UNHEALTHY"));
        return NodeResult.success(state);
    }
    @Override public String getName() { return "health_check"; }
}

class LoadBalancerNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        String[] nodes = {"node-1", "node-2", "node-3"};
        String assignedNode = nodes[new Random().nextInt(nodes.length)];

        state.set("assigned_node", assignedNode);
        state.set("load_balancing_completed", true);

        System.out.println("    [LOAD_BALANCER] Assigned to " + assignedNode);
        return NodeResult.success(state);
    }
    @Override public String getName() { return "load_balancer"; }
}

class DataProcessorNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        // Simulate data processing
        Thread.sleep(150);

        state.set("processing_status", "completed");
        state.set("records_processed", 1247);
        state.set("processing_time_ms", 150);

        System.out.println("    [DATA_PROCESSOR] Processed 1247 records");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "data_processor"; }
}

class CacheManagerNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Thread.sleep(50);

        boolean cacheHit = Math.random() > 0.3; // 70% cache hit rate
        state.set("cache_hit", cacheHit);
        state.set("cache_operation_completed", true);

        System.out.println("    [CACHE_MANAGER] " + (cacheHit ? "Cache hit" : "Cache miss"));
        return NodeResult.success(state);
    }
    @Override public String getName() { return "cache_manager"; }
}

class DatabaseWriterNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Thread.sleep(80);

        boolean success = Math.random() > 0.05; // 95% success rate
        state.set("database_success", success);
        state.set("database_write_completed", true);

        System.out.println("    [DATABASE_WRITER] Write " + (success ? "successful" : "failed"));
        return NodeResult.success(state);
    }
    @Override public String getName() { return "database_writer"; }
}

class NotificationSenderNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Thread.sleep(30);

        int notificationsSent = new Random().nextInt(3) + 1;
        state.set("notifications_sent", notificationsSent);
        state.set("notification_sending_completed", true);

        System.out.println("    [NOTIFICATION_SENDER] Sent " + notificationsSent + " notifications");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "notification_sender"; }
}

class MetricsCollectorNode implements GraphNode<BasicGraphState> {
    @Override
    public NodeResult<BasicGraphState> execute(BasicGraphState state, NodeContext context) throws Exception {
        Map<String, Object> metrics = Map.of(
            "execution_time_ms", System.currentTimeMillis() % 1000,
            "memory_usage_mb", 256 + new Random().nextInt(512),
            "cpu_usage_percent", 15 + new Random().nextInt(70),
            "requests_processed", 1,
            "cache_hit_rate", state.get("cache_hit", Boolean.class).orElse(false) ? 1.0 : 0.0
        );

        state.set("workflow_metrics", metrics);
        state.set("metrics_collection_completed", true);

        System.out.println("    [METRICS_COLLECTOR] Collected performance metrics");
        return NodeResult.success(state);
    }
    @Override public String getName() { return "metrics_collector"; }
}

/**
 * Supporting classes for the examples
 */

class GraphErrorHandler {
    public void handleError(Exception error, BasicGraphState state, String nodeName) {
        System.err.println("Error in node " + nodeName + ": " + error.getMessage());
        state.set("error_occurred", true);
        state.set("error_node", nodeName);
        state.set("error_message", error.getMessage());
    }
}

class WorkflowMetrics {
    public void recordNodeExecution(String nodeName, long executionTime) {
        System.out.println("  [METRICS] Node '" + nodeName + "' executed in " + executionTime + "ms");
    }

    public void recordGraphExecution(String graphName, long totalTime) {
        System.out.println("  [METRICS] Graph '" + graphName + "' completed in " + totalTime + "ms");
    }
}
