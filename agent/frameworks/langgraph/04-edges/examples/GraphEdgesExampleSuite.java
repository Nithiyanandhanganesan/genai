/**
 * LangGraph Edges Examples - Complete Implementation
 * Demonstrates various edge types and routing patterns
 */
package com.example.agent.langgraph.edges;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Comprehensive edges demonstration
 */
public class GraphEdgesExampleSuite {

    public static void main(String[] args) {
        System.out.println("=== LangGraph Edges Examples ===");

        try {
            // Run all edge examples
            runStaticEdgeExamples();
            runConditionalEdgeExamples();
            runDynamicEdgeExamples();
            runParallelEdgeExamples();
            runLoopEdgeExamples();
            runTerminalEdgeExamples();
            runAdvancedPatternsExample();

        } catch (Exception e) {
            System.err.println("Error running edge examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Static edge routing examples
     */
    private static void runStaticEdgeExamples() throws Exception {
        System.out.println("\n1. Static Edge Examples:");

        // Create simple static edges
        StaticEdge simpleEdge = new StaticEdge("process_to_validate", "processor_node", "validation_node");
        simpleEdge.initialize(Map.of());

        // Create test state
        GraphState state = new BasicGraphState("workflow_001");
        state.set("document_id", "DOC123");
        state.set("processing_complete", true);

        EdgeContext context = new EdgeContext("exec_001", "workflow_001", "processor_node");
        EdgeResult result = simpleEdge.evaluate(state, context);

        if (result.isSuccess()) {
            System.out.println("Static Edge Result:");
            System.out.println("  Source: " + simpleEdge.getSourceNode());
            System.out.println("  Target: " + result.getTargetNodes());
            System.out.println("  Reason: " + result.getRoutingReason());
            System.out.println("  Evaluation time: " + result.getEvaluationTime().toMillis() + "ms");
        }

        // Chain of static edges example
        List<StaticEdge> processingChain = Arrays.asList(
            new StaticEdge("input_to_parser", "input_node", "parser_node"),
            new StaticEdge("parser_to_analyzer", "parser_node", "analyzer_node"),
            new StaticEdge("analyzer_to_formatter", "analyzer_node", "formatter_node"),
            new StaticEdge("formatter_to_output", "formatter_node", "output_node")
        );

        System.out.println("\nProcessing Chain:");
        for (StaticEdge edge : processingChain) {
            edge.initialize(Map.of());
            System.out.println("  " + edge.getSourceNode() + " -> " + edge.getPossibleTargetNodes());
        }

        // Cleanup
        simpleEdge.cleanup();
        processingChain.forEach(StaticEdge::cleanup);
    }

    /**
     * Conditional edge routing examples
     */
    private static void runConditionalEdgeExamples() throws Exception {
        System.out.println("\n2. Conditional Edge Examples:");

        // Create conditions for document processing workflow
        List<EdgeCondition> documentConditions = Arrays.asList(
            EdgeConditions.fieldGreaterThan("confidence_score", 0.9, "high_confidence_processor"),
            EdgeConditions.fieldContains("document_type", "urgent", "priority_processor"),
            EdgeConditions.fieldEquals("language", "spanish", "spanish_processor"),
            EdgeConditions.fieldPresent("requires_review", "manual_review"),
            EdgeConditions.stateValidationPassed("standard_processor")
        );

        ConditionalEdge documentRouter = new ConditionalEdge(
            "document_router", "intake_node", documentConditions, "default_processor");
        documentRouter.initialize(Map.of());

        // Test different document scenarios
        Map<String, Object>[] testScenarios = {
            // High confidence scenario
            Map.of("document_type", "contract", "confidence_score", 0.95, "language", "english"),

            // Urgent document scenario
            Map.of("document_type", "urgent_memo", "confidence_score", 0.7, "language", "english"),

            // Spanish document scenario
            Map.of("document_type", "invoice", "confidence_score", 0.8, "language", "spanish"),

            // Review required scenario
            Map.of("document_type", "legal", "confidence_score", 0.6, "requires_review", true, "language", "english"),

            // Default scenario
            Map.of("document_type", "standard", "confidence_score", 0.5, "language", "french")
        };

        for (int i = 0; i < testScenarios.length; i++) {
            GraphState testState = new BasicGraphState("test_workflow_" + i);
            testScenarios[i].forEach(testState::set);

            EdgeContext testContext = new EdgeContext("routing_test_" + i, "test_workflow_" + i, "intake_node");
            EdgeResult routingResult = documentRouter.evaluate(testState, testContext);

            if (routingResult.isSuccess()) {
                System.out.println("Scenario " + (i + 1) + ":");
                System.out.println("  Input: " + testScenarios[i]);
                System.out.println("  Routed to: " + routingResult.getTargetNodes());
                System.out.println("  Reason: " + routingResult.getRoutingReason());
            }
        }

        // Advanced conditional edge with custom conditions
        List<EdgeCondition> advancedConditions = Arrays.asList(
            EdgeConditions.multiFieldCondition(
                "high priority and valid email",
                "priority_email_processor",
                Map.of(
                    "priority", value -> ((Number) value).intValue() > 8,
                    "email", value -> value.toString().contains("@")
                )
            ),

            EdgeConditions.customCondition(
                "complex business rule",
                "business_rule_processor",
                Set.of("account_type", "transaction_amount", "risk_score"),
                state -> {
                    String accountType = state.get("account_type", String.class).orElse("");
                    double amount = state.get("transaction_amount", Double.class).orElse(0.0);
                    double riskScore = state.get("risk_score", Double.class).orElse(0.0);

                    return "premium".equals(accountType) && amount > 10000 && riskScore < 0.3;
                }
            )
        );

        ConditionalEdge advancedRouter = new ConditionalEdge(
            "advanced_router", "business_node", advancedConditions, "standard_processor");
        advancedRouter.initialize(Map.of());

        // Test advanced conditions
        GraphState businessState = new BasicGraphState("business_test");
        businessState.set("priority", 9);
        businessState.set("email", "premium@customer.com");
        businessState.set("account_type", "premium");
        businessState.set("transaction_amount", 25000.0);
        businessState.set("risk_score", 0.2);

        EdgeContext businessContext = new EdgeContext("business_routing", "business_test", "business_node");
        EdgeResult businessResult = advancedRouter.evaluate(businessState, businessContext);

        System.out.println("\nAdvanced Conditions Test:");
        System.out.println("  Business state: " + businessState.getKeys());
        System.out.println("  Routed to: " + businessResult.getTargetNodes());
        System.out.println("  Reason: " + businessResult.getRoutingReason());

        // Cleanup
        documentRouter.cleanup();
        advancedRouter.cleanup();
    }

    /**
     * Dynamic edge routing examples
     */
    private static void runDynamicEdgeExamples() throws Exception {
        System.out.println("\n3. Dynamic Edge Examples:");

        // Create dynamic edge that routes based on calculated score
        Function<GraphState, String> scoreBasedResolver = state -> {
            double score = calculateOverallScore(state);

            if (score >= 90) return "excellent_handler";
            if (score >= 75) return "good_handler";
            if (score >= 50) return "average_handler";
            return "poor_handler";
        };

        Set<String> scoreTargets = Set.of("excellent_handler", "good_handler", "average_handler", "poor_handler");

        DynamicEdge scoreRouter = new DynamicEdge(
            "score_router", "assessment_node", scoreBasedResolver, scoreTargets, "default_handler");
        scoreRouter.initialize(Map.of());

        // Test with different score scenarios
        Map<String, Object>[] scoreScenarios = {
            Map.of("quality", 95, "speed", 90, "accuracy", 98),      // Excellent
            Map.of("quality", 80, "speed", 75, "accuracy", 85),      // Good
            Map.of("quality", 60, "speed", 55, "accuracy", 65),      // Average
            Map.of("quality", 40, "speed", 30, "accuracy", 45)       // Poor
        };

        for (int i = 0; i < scoreScenarios.length; i++) {
            GraphState scoreState = new BasicGraphState("score_test_" + i);
            scoreScenarios[i].forEach(scoreState::set);

            EdgeContext scoreContext = new EdgeContext("score_routing_" + i, "score_test_" + i, "assessment_node");
            EdgeResult scoreResult = scoreRouter.evaluate(scoreState, scoreContext);

            if (scoreResult.isSuccess()) {
                double calculatedScore = calculateOverallScore(scoreState);
                System.out.println("Score Routing " + (i + 1) + ":");
                System.out.println("  Metrics: " + scoreScenarios[i]);
                System.out.println("  Calculated score: " + String.format("%.1f", calculatedScore));
                System.out.println("  Routed to: " + scoreResult.getTargetNodes());
            }
        }

        // Dynamic edge with context-aware routing
        Function<GraphState, String> contextAwareResolver = state -> {
            String userRole = state.get("user_role", String.class).orElse("user");
            boolean isUrgent = state.get("is_urgent", Boolean.class).orElse(false);
            int workload = state.get("current_workload", Integer.class).orElse(50);

            // Complex routing logic based on multiple factors
            if ("admin".equals(userRole)) {
                return "admin_processor";
            }

            if (isUrgent && workload < 80) {
                return "express_processor";
            }

            if (workload > 95) {
                return "queue_processor";
            }

            return "standard_processor";
        };

        Set<String> contextTargets = Set.of("admin_processor", "express_processor", "queue_processor", "standard_processor");

        DynamicEdge contextRouter = new DynamicEdge(
            "context_router", "request_node", contextAwareResolver, contextTargets, "fallback_processor");
        contextRouter.initialize(Map.of());

        // Test context-aware routing
        GraphState contextState = new BasicGraphState("context_test");
        contextState.set("user_role", "admin");
        contextState.set("is_urgent", true);
        contextState.set("current_workload", 85);

        EdgeContext contextContext = new EdgeContext("context_routing", "context_test", "request_node");
        EdgeResult contextResult = contextRouter.evaluate(contextState, contextContext);

        System.out.println("\nContext-Aware Routing:");
        System.out.println("  User role: " + contextState.get("user_role", String.class).orElse("unknown"));
        System.out.println("  Is urgent: " + contextState.get("is_urgent", Boolean.class).orElse(false));
        System.out.println("  Workload: " + contextState.get("current_workload", Integer.class).orElse(0) + "%");
        System.out.println("  Routed to: " + contextResult.getTargetNodes());

        // Cleanup
        scoreRouter.cleanup();
        contextRouter.cleanup();
    }

    /**
     * Parallel edge execution examples
     */
    private static void runParallelEdgeExamples() throws Exception {
        System.out.println("\n4. Parallel Edge Examples:");

        // Create parallel edge with all targets strategy
        List<String> parallelTargets = Arrays.asList("analysis_node", "validation_node", "enrichment_node");

        ParallelEdge allTargetsEdge = new ParallelEdge(
            "parallel_processor", "input_node", parallelTargets, ParallelStrategies.allTargets());
        allTargetsEdge.initialize(Map.of());

        GraphState parallelState = new BasicGraphState("parallel_test");
        parallelState.set("document_content", "Sample document for parallel processing");
        parallelState.set("document_type", "research_paper");

        EdgeContext parallelContext = new EdgeContext("parallel_routing", "parallel_test", "input_node");
        EdgeResult allTargetsResult = allTargetsEdge.evaluate(parallelState, parallelContext);

        System.out.println("All Targets Parallel Execution:");
        System.out.println("  Original targets: " + parallelTargets);
        System.out.println("  Actual targets: " + allTargetsResult.getTargetNodes());
        System.out.println("  Is parallel: " + allTargetsResult.isParallel());
        System.out.println("  Metadata: " + allTargetsResult.getMetadata());

        // Conditional parallel edge
        Map<String, Predicate<GraphState>> targetConditions = Map.of(
            "analysis_node", state -> state.get("document_type", String.class).orElse("").contains("research"),
            "validation_node", state -> state.get("requires_validation", Boolean.class).orElse(true),
            "enrichment_node", state -> state.get("document_content", String.class).orElse("").length() > 100,
            "translation_node", state -> "spanish".equals(state.get("language", String.class).orElse("english"))
        );

        List<String> conditionalTargets = Arrays.asList("analysis_node", "validation_node", "enrichment_node", "translation_node");

        ParallelEdge conditionalEdge = new ParallelEdge(
            "conditional_parallel", "input_node", conditionalTargets,
            ParallelStrategies.conditionalTargets(targetConditions));
        conditionalEdge.initialize(Map.of());

        // Test conditional parallel with different states
        GraphState conditionalState1 = new BasicGraphState("conditional_test_1");
        conditionalState1.set("document_type", "research_paper");
        conditionalState1.set("requires_validation", false);
        conditionalState1.set("document_content", "This is a very long research document with extensive content...");
        conditionalState1.set("language", "english");

        EdgeResult conditionalResult1 = conditionalEdge.evaluate(conditionalState1, parallelContext);

        System.out.println("\nConditional Parallel (Test 1):");
        System.out.println("  Available targets: " + conditionalTargets);
        System.out.println("  Selected targets: " + conditionalResult1.getTargetNodes());
        System.out.println("  Filtered: " + conditionalResult1.getMetadata().get("filtered_targets"));

        GraphState conditionalState2 = new BasicGraphState("conditional_test_2");
        conditionalState2.set("document_type", "memo");
        conditionalState2.set("requires_validation", true);
        conditionalState2.set("document_content", "Short memo");
        conditionalState2.set("language", "spanish");

        EdgeResult conditionalResult2 = conditionalEdge.evaluate(conditionalState2, parallelContext);

        System.out.println("\nConditional Parallel (Test 2):");
        System.out.println("  Available targets: " + conditionalTargets);
        System.out.println("  Selected targets: " + conditionalResult2.getTargetNodes());

        // Limited parallel edge
        ParallelEdge limitedEdge = new ParallelEdge(
            "limited_parallel", "input_node", parallelTargets, ParallelStrategies.limitedTargets(2));
        limitedEdge.initialize(Map.of());

        EdgeResult limitedResult = limitedEdge.evaluate(parallelState, parallelContext);

        System.out.println("\nLimited Parallel Execution (max 2):");
        System.out.println("  Available targets: " + parallelTargets);
        System.out.println("  Selected targets: " + limitedResult.getTargetNodes());
        System.out.println("  Limit applied: " + limitedResult.getMetadata().get("target_limit_applied"));

        // Cleanup
        allTargetsEdge.cleanup();
        conditionalEdge.cleanup();
        limitedEdge.cleanup();
    }

    /**
     * Loop edge examples
     */
    private static void runLoopEdgeExamples() throws Exception {
        System.out.println("\n5. Loop Edge Examples:");

        // Simple counter loop
        Predicate<GraphState> counterCondition = state -> {
            int counter = state.get("counter", Integer.class).orElse(0);
            return counter < 5;
        };

        Function<GraphState, GraphState> counterUpdater = state -> {
            GraphState updated = state.copy();
            int currentCounter = state.get("counter", Integer.class).orElse(0);
            updated.set("counter", currentCounter + 1);
            updated.set("last_update", LocalDateTime.now());
            return updated;
        };

        LoopEdge counterLoop = new LoopEdge(
            "counter_loop", "process_node", counterCondition,
            "process_node", "completion_node", 10, counterUpdater);
        counterLoop.initialize(Map.of());

        // Test counter loop
        GraphState loopState = new BasicGraphState("loop_test");
        loopState.set("counter", 0);
        loopState.set("data", "processing data");

        EdgeContext loopContext = new EdgeContext("loop_execution", "loop_test", "process_node");

        System.out.println("Counter Loop Execution:");
        for (int iteration = 0; iteration < 7; iteration++) {
            EdgeResult loopResult = counterLoop.evaluate(loopState, loopContext);

            if (loopResult.isSuccess()) {
                int counter = loopResult.getModifiedState().get("counter", Integer.class).orElse(0);
                String target = loopResult.getTargetNodes().get(0);

                System.out.println("  Iteration " + iteration + ": counter=" + counter +
                                 " -> " + target + " (" + loopResult.getRoutingReason() + ")");

                loopState = loopResult.getModifiedState();

                if ("completion_node".equals(target)) {
                    String exitReason = loopState.get("loop_exit_reason", String.class).orElse("unknown");
                    System.out.println("  Loop exited: " + exitReason);
                    break;
                }
            }
        }

        // Data processing loop with completion condition
        Predicate<GraphState> dataCondition = state -> {
            List<String> remaining = state.get("remaining_items", List.class).orElse(List.of());
            boolean hasErrors = state.get("has_errors", Boolean.class).orElse(false);
            return !remaining.isEmpty() && !hasErrors;
        };

        Function<GraphState, GraphState> dataUpdater = state -> {
            GraphState updated = state.copy();
            List<String> remaining = new ArrayList<>(state.get("remaining_items", List.class).orElse(List.of()));
            List<String> processed = new ArrayList<>(state.get("processed_items", List.class).orElse(List.of()));

            if (!remaining.isEmpty()) {
                String item = remaining.remove(0);
                processed.add(item + "_processed");

                updated.set("remaining_items", remaining);
                updated.set("processed_items", processed);
                updated.set("current_item", item);

                // Simulate error condition
                if (item.contains("error")) {
                    updated.set("has_errors", true);
                }
            }

            return updated;
        };

        LoopEdge dataLoop = new LoopEdge(
            "data_processing_loop", "data_processor", dataCondition,
            "data_processor", "data_completed", 20, dataUpdater);
        dataLoop.initialize(Map.of());

        // Test data processing loop
        GraphState dataState = new BasicGraphState("data_loop_test");
        dataState.set("remaining_items", Arrays.asList("item1", "item2", "error_item", "item4", "item5"));
        dataState.set("processed_items", new ArrayList<>());
        dataState.set("has_errors", false);

        EdgeContext dataContext = new EdgeContext("data_loop", "data_loop_test", "data_processor");

        System.out.println("\nData Processing Loop:");
        for (int iteration = 0; iteration < 10; iteration++) {
            EdgeResult dataResult = dataLoop.evaluate(dataState, dataContext);

            if (dataResult.isSuccess()) {
                List<String> remaining = dataResult.getModifiedState().get("remaining_items", List.class).orElse(List.of());
                List<String> processed = dataResult.getModifiedState().get("processed_items", List.class).orElse(List.of());
                boolean hasErrors = dataResult.getModifiedState().get("has_errors", Boolean.class).orElse(false);
                String target = dataResult.getTargetNodes().get(0);

                System.out.println("  Iteration " + iteration + ": remaining=" + remaining.size() +
                                 ", processed=" + processed.size() + ", errors=" + hasErrors +
                                 " -> " + target);

                dataState = dataResult.getModifiedState();

                if ("data_completed".equals(target)) {
                    String exitReason = dataState.get("loop_exit_reason", String.class).orElse("unknown");
                    int completed = dataState.get("loop_iterations_completed", Integer.class).orElse(0);
                    System.out.println("  Loop completed after " + completed + " iterations: " + exitReason);
                    break;
                }
            }
        }

        // Cleanup
        counterLoop.cleanup();
        dataLoop.cleanup();
    }

    /**
     * Terminal edge examples
     */
    private static void runTerminalEdgeExamples() throws Exception {
        System.out.println("\n6. Terminal Edge Examples:");

        // Simple terminal edge (always terminates)
        TerminalEdge simpleTerminal = new TerminalEdge("simple_end", "final_node");
        simpleTerminal.initialize(Map.of());

        GraphState terminalState = new BasicGraphState("terminal_test");
        terminalState.set("processing_complete", true);
        terminalState.set("result", "success");

        EdgeContext terminalContext = new EdgeContext("terminal_test", "terminal_test", "final_node");
        EdgeResult simpleTerminalResult = simpleTerminal.evaluate(terminalState, terminalContext);

        System.out.println("Simple Terminal Edge:");
        System.out.println("  Is terminal: " + simpleTerminalResult.isTerminal());
        System.out.println("  Target nodes: " + simpleTerminalResult.getTargetNodes());
        System.out.println("  Execution completed: " +
            simpleTerminalResult.getModifiedState().get("execution_completed", Boolean.class).orElse(false));

        // Conditional terminal edge
        Predicate<GraphState> completionCondition = state -> {
            boolean allTasksComplete = state.get("all_tasks_complete", Boolean.class).orElse(false);
            boolean noErrors = !state.get("has_errors", Boolean.class).orElse(true);
            return allTasksComplete && noErrors;
        };

        Function<GraphState, GraphState> finalProcessor = state -> {
            GraphState finalState = state.copy();
            finalState.set("final_status", "SUCCESS");
            finalState.set("completion_summary", "All tasks completed successfully");

            // Calculate final metrics
            int tasksProcessed = state.get("tasks_processed", Integer.class).orElse(0);
            int errorsCount = state.get("error_count", Integer.class).orElse(0);
            double successRate = tasksProcessed > 0 ? (double) (tasksProcessed - errorsCount) / tasksProcessed * 100 : 0;

            finalState.set("success_rate", successRate);
            finalState.remove("temporary_data"); // Clean up temporary state

            return finalState;
        };

        TerminalEdge conditionalTerminal = new TerminalEdge(
            "conditional_end", "validation_node", completionCondition, finalProcessor);
        conditionalTerminal.initialize(Map.of());

        // Test conditional terminal with success condition
        GraphState successState = new BasicGraphState("success_test");
        successState.set("all_tasks_complete", true);
        successState.set("has_errors", false);
        successState.set("tasks_processed", 10);
        successState.set("error_count", 0);
        successState.set("temporary_data", "should be removed");

        EdgeContext successContext = new EdgeContext("success_test", "success_test", "validation_node");
        EdgeResult successResult = conditionalTerminal.evaluate(successState, successContext);

        System.out.println("\nConditional Terminal (Success):");
        System.out.println("  Termination allowed: " + successResult.isSuccess());
        System.out.println("  Final status: " +
            successResult.getModifiedState().get("final_status", String.class).orElse("unknown"));
        System.out.println("  Success rate: " +
            successResult.getModifiedState().get("success_rate", Double.class).orElse(0.0) + "%");
        System.out.println("  Has temporary data: " +
            successResult.getModifiedState().containsKey("temporary_data"));

        // Test conditional terminal with failure condition
        GraphState failureState = new BasicGraphState("failure_test");
        failureState.set("all_tasks_complete", false);  // Not complete
        failureState.set("has_errors", true);           // Has errors
        failureState.set("tasks_processed", 5);
        failureState.set("error_count", 2);

        try {
            EdgeResult failureResult = conditionalTerminal.evaluate(failureState, successContext);
            System.out.println("\nConditional Terminal (Failure): Unexpected success");
        } catch (EdgeEvaluationException e) {
            System.out.println("\nConditional Terminal (Failure):");
            System.out.println("  Expected termination failure: " + e.getMessage());
        }

        // Cleanup
        simpleTerminal.cleanup();
        conditionalTerminal.cleanup();
    }

    /**
     * Advanced patterns and complex routing
     */
    private static void runAdvancedPatternsExample() throws Exception {
        System.out.println("\n7. Advanced Patterns Example:");

        // Multi-stage workflow with complex routing
        WorkflowManager workflow = new WorkflowManager();

        // Stage 1: Input validation with multiple paths
        List<EdgeCondition> validationConditions = Arrays.asList(
            EdgeConditions.customCondition(
                "high priority urgent document",
                "express_pipeline",
                Set.of("priority", "is_urgent", "document_type"),
                state -> {
                    int priority = state.get("priority", Integer.class).orElse(0);
                    boolean urgent = state.get("is_urgent", Boolean.class).orElse(false);
                    String type = state.get("document_type", String.class).orElse("");
                    return priority > 8 && urgent && !type.isEmpty();
                }
            ),
            EdgeConditions.fieldContains("document_type", "legal", "legal_review"),
            EdgeConditions.fieldGreaterThan("file_size", 10000000, "large_file_handler")
        );

        ConditionalEdge validationRouter = new ConditionalEdge(
            "validation_router", "input_validation", validationConditions, "standard_pipeline");

        // Stage 2: Processing with parallel execution
        ParallelEdge processingRouter = new ParallelEdge(
            "processing_router", "standard_pipeline",
            Arrays.asList("content_analysis", "metadata_extraction", "quality_check"),
            ParallelStrategies.allTargets()
        );

        // Stage 3: Quality-based routing
        Function<GraphState, String> qualityResolver = state -> {
            double contentScore = state.get("content_quality", Double.class).orElse(0.0);
            double metadataScore = state.get("metadata_quality", Double.class).orElse(0.0);
            boolean qualityCheck = state.get("quality_check_passed", Boolean.class).orElse(false);

            double overallScore = (contentScore + metadataScore) / 2.0;

            if (!qualityCheck || overallScore < 60) {
                return "quality_improvement";
            } else if (overallScore >= 90) {
                return "premium_output";
            } else {
                return "standard_output";
            }
        };

        DynamicEdge qualityRouter = new DynamicEdge(
            "quality_router", "processing_complete", qualityResolver,
            Set.of("quality_improvement", "premium_output", "standard_output"),
            "default_output"
        );

        // Initialize all edges
        validationRouter.initialize(Map.of());
        processingRouter.initialize(Map.of());
        qualityRouter.initialize(Map.of());

        // Test complete workflow
        System.out.println("Complex Workflow Test:");

        GraphState workflowState = new BasicGraphState("complex_workflow");
        workflowState.set("priority", 9);
        workflowState.set("is_urgent", true);
        workflowState.set("document_type", "contract");
        workflowState.set("file_size", 5000000);

        EdgeContext workflowContext = new EdgeContext("workflow_exec", "complex_workflow", "input_validation");

        // Stage 1: Validation routing
        EdgeResult stage1 = validationRouter.evaluate(workflowState, workflowContext);
        System.out.println("  Stage 1 (Validation): " + stage1.getTargetNodes() + " - " + stage1.getRoutingReason());

        // Simulate processing results
        workflowState.set("content_quality", 85.0);
        workflowState.set("metadata_quality", 92.0);
        workflowState.set("quality_check_passed", true);

        // Stage 2: Processing (if went to standard pipeline)
        if (stage1.getTargetNodes().contains("standard_pipeline")) {
            EdgeContext processingContext = new EdgeContext("workflow_exec", "complex_workflow", "standard_pipeline");
            EdgeResult stage2 = processingRouter.evaluate(workflowState, processingContext);
            System.out.println("  Stage 2 (Processing): " + stage2.getTargetNodes() + " - Parallel execution");
        }

        // Stage 3: Quality routing
        EdgeContext qualityContext = new EdgeContext("workflow_exec", "complex_workflow", "processing_complete");
        EdgeResult stage3 = qualityRouter.evaluate(workflowState, qualityContext);
        System.out.println("  Stage 3 (Quality): " + stage3.getTargetNodes() + " - " + stage3.getRoutingReason());

        // Error handling edge example
        ConditionalEdge errorHandler = new ConditionalEdge(
            "error_handler", "any_node",
            Arrays.asList(
                EdgeConditions.fieldPresent("fatal_error", "emergency_stop"),
                EdgeConditions.fieldGreaterThan("error_count", 5, "error_recovery"),
                EdgeConditions.fieldEquals("retry_attempts", 3, "manual_intervention")
            ),
            "continue_processing"
        );

        errorHandler.initialize(Map.of());

        GraphState errorState = new BasicGraphState("error_test");
        errorState.set("error_count", 7);
        errorState.set("retry_attempts", 2);

        EdgeContext errorContext = new EdgeContext("error_handling", "error_test", "any_node");
        EdgeResult errorResult = errorHandler.evaluate(errorState, errorContext);

        System.out.println("\nError Handling:");
        System.out.println("  Error state: error_count=7, retry_attempts=2");
        System.out.println("  Routed to: " + errorResult.getTargetNodes());
        System.out.println("  Reason: " + errorResult.getRoutingReason());

        // Cleanup
        validationRouter.cleanup();
        processingRouter.cleanup();
        qualityRouter.cleanup();
        errorHandler.cleanup();
    }

    /**
     * Helper method to calculate overall score for dynamic routing
     */
    private static double calculateOverallScore(GraphState state) {
        double quality = state.get("quality", Number.class).map(Number::doubleValue).orElse(0.0);
        double speed = state.get("speed", Number.class).map(Number::doubleValue).orElse(0.0);
        double accuracy = state.get("accuracy", Number.class).map(Number::doubleValue).orElse(0.0);

        // Weighted average: quality 50%, speed 20%, accuracy 30%
        return (quality * 0.5) + (speed * 0.2) + (accuracy * 0.3);
    }
}

/**
 * Workflow manager for organizing complex edge relationships
 */
class WorkflowManager {
    private final Map<String, GraphEdge> edges = new HashMap<>();
    private final Map<String, Set<String>> nodeConnections = new HashMap<>();

    public void addEdge(GraphEdge edge) {
        edges.put(edge.getName(), edge);

        String sourceNode = edge.getSourceNode();
        Set<String> targets = edge.getPossibleTargetNodes();

        nodeConnections.computeIfAbsent(sourceNode, k -> new HashSet<>()).addAll(targets);
    }

    public Optional<GraphEdge> getEdge(String edgeName) {
        return Optional.ofNullable(edges.get(edgeName));
    }

    public Set<String> getConnectedNodes(String sourceNode) {
        return nodeConnections.getOrDefault(sourceNode, Set.of());
    }

    public Map<String, Set<String>> getTopology() {
        return new HashMap<>(nodeConnections);
    }

    public void validateWorkflow() {
        // Basic workflow validation
        Set<String> allNodes = new HashSet<>();
        nodeConnections.forEach((source, targets) -> {
            allNodes.add(source);
            allNodes.addAll(targets);
        });

        System.out.println("Workflow validation:");
        System.out.println("  Total edges: " + edges.size());
        System.out.println("  Total nodes: " + allNodes.size());
        System.out.println("  Node connections: " + nodeConnections.size());
    }
}
