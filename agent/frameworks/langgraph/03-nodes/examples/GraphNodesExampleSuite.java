/**
 * LangGraph Nodes Examples - Complete Implementation
 * Demonstrates various node types and execution patterns
 */
package com.example.agent.langgraph.nodes;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Comprehensive nodes demonstration
 */
public class GraphNodesExampleSuite {

    private static ChatLanguageModel llm;

    public static void main(String[] args) {
        System.out.println("=== LangGraph Nodes Examples ===");

        // Initialize LLM
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("Please set OPENAI_API_KEY environment variable");
            return;
        }

        llm = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .temperature(0.7)
            .build();

        try {
            // Run all node examples
            runProcessingNodeExamples();
            runDecisionNodeExamples();
            runValidationNodeExamples();
            runAggregationNodeExamples();
            runParallelNodeExamples();
            runAdvancedPatternsExample();

        } catch (Exception e) {
            System.err.println("Error running node examples: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Processing node examples
     */
    private static void runProcessingNodeExamples() throws Exception {
        System.out.println("\n1. Processing Node Examples:");

        // Create LLM processing node
        PromptTemplate template = new StringPromptTemplate(
            "Analyze the following text for sentiment and key themes:\n\n{text}\n\n" +
            "Provide your analysis in the following format:\n" +
            "Sentiment: [positive/negative/neutral]\n" +
            "Key Themes: [list main themes]\n" +
            "Summary: [brief summary]",
            "Text analysis template"
        );

        LLMProcessingNode analysisNode = new LLMProcessingNode("text_analyzer", llm, template);
        analysisNode.initialize(Map.of());

        // Create initial state
        GraphState state = new BasicGraphState("workflow_001");
        state.set("text", "The new product launch exceeded our expectations! " +
                         "Customer feedback has been overwhelmingly positive, " +
                         "and sales figures are 150% higher than projected. " +
                         "The team's hard work and innovative approach really paid off.");

        // Execute processing node
        NodeContext context = new NodeContext("exec_001", "workflow_001");
        NodeResult result = analysisNode.execute(state, context);

        if (result.isSuccess()) {
            System.out.println("LLM Analysis Result:");
            System.out.println("Execution time: " + result.getExecutionTime().toMillis() + "ms");
            System.out.println("Analysis: " + result.getOutputState().get("llm_response", String.class).orElse("N/A"));

            // Show state progression
            System.out.println("State keys before: " + state.getKeys());
            System.out.println("State keys after: " + result.getOutputState().getKeys());
        } else {
            System.out.println("Analysis failed: " + result.getErrorMessage());
        }

        // Data transformation node example
        DataTransformNode transformNode = new DataTransformNode(
            "text_processor",
            inputState -> {
                GraphState output = inputState.copy();

                // Extract text and perform transformations
                String text = inputState.get("text", String.class).orElse("");

                output.set("word_count", text.split("\\s+").length);
                output.set("character_count", text.length());
                output.set("uppercase_text", text.toUpperCase());
                output.set("processed_timestamp", LocalDateTime.now());

                // Extract potential entities (simplified)
                List<String> entities = new ArrayList<>();
                if (text.contains("product")) entities.add("product");
                if (text.contains("customer")) entities.add("customer");
                if (text.contains("team")) entities.add("team");
                output.set("extracted_entities", entities);

                return output;
            },
            Set.of("text"),
            Set.of("word_count", "character_count", "uppercase_text", "processed_timestamp", "extracted_entities")
        );

        transformNode.initialize(Map.of());
        NodeResult transformResult = transformNode.execute(state, context);

        if (transformResult.isSuccess()) {
            System.out.println("\nText Transformation Result:");
            GraphState outputState = transformResult.getOutputState();
            System.out.println("Word count: " + outputState.get("word_count", Integer.class).orElse(0));
            System.out.println("Character count: " + outputState.get("character_count", Integer.class).orElse(0));
            System.out.println("Entities: " + outputState.get("extracted_entities", List.class).orElse(List.of()));
        }

        // Cleanup
        analysisNode.cleanup();
        transformNode.cleanup();
    }

    /**
     * Decision node routing examples
     */
    private static void runDecisionNodeExamples() throws Exception {
        System.out.println("\n2. Decision Node Examples:");

        // Create conditional routes
        List<ConditionalRoute> routes = Arrays.asList(
            RoutingConditions.fieldGreaterThan("confidence_score", 0.8, "high_confidence_processor"),
            RoutingConditions.fieldGreaterThan("confidence_score", 0.5, "medium_confidence_processor"),
            RoutingConditions.fieldContains("text_category", "urgent", "urgent_processor"),
            RoutingConditions.fieldEquals("language", "spanish", "spanish_processor")
        );

        DecisionNode routingNode = new DecisionNode("content_router", routes, "default_processor");
        routingNode.initialize(Map.of());

        // Test different routing scenarios
        Map<String, Object>[] testScenarios = {
            Map.of("confidence_score", 0.9, "text_category", "normal", "language", "english"),
            Map.of("confidence_score", 0.6, "text_category", "urgent", "language", "english"),
            Map.of("confidence_score", 0.3, "text_category", "normal", "language", "spanish"),
            Map.of("confidence_score", 0.2, "text_category", "normal", "language", "english")
        };

        for (int i = 0; i < testScenarios.length; i++) {
            GraphState testState = new BasicGraphState("test_workflow_" + i);
            testScenarios[i].forEach(testState::set);

            NodeContext testContext = new NodeContext("routing_test_" + i, "test_workflow_" + i);
            NodeResult routingResult = routingNode.execute(testState, testContext);

            if (routingResult.isSuccess()) {
                String nextNode = routingResult.getOutputState().get("next_node", String.class).orElse("unknown");
                String reason = routingResult.getOutputState().get("routing_reason", String.class).orElse("unknown");

                System.out.println("Scenario " + (i + 1) + ":");
                System.out.println("  Input: " + testScenarios[i]);
                System.out.println("  Routed to: " + nextNode + " (reason: " + reason + ")");
            } else {
                System.out.println("Scenario " + (i + 1) + " failed: " + routingResult.getErrorMessage());
            }
        }

        routingNode.cleanup();
    }

    /**
     * Validation node examples
     */
    private static void runValidationNodeExamples() throws Exception {
        System.out.println("\n3. Validation Node Examples:");

        // Create validators
        List<StateValidator> validators = Arrays.asList(
            CommonValidators.requiredFields("user_id", "email", "message"),
            CommonValidators.typeCheck("user_id", String.class),
            CommonValidators.typeCheck("email", String.class),
            CommonValidators.valueRange("priority", 1, 10),
            new StateValidator() {
                @Override
                public ValidationResult validate(GraphState state) {
                    Optional<String> email = state.get("email", String.class);
                    if (email.isPresent() && email.get().contains("@")) {
                        return ValidationResult.success();
                    }
                    return ValidationResult.failure(List.of("Invalid email format"));
                }

                @Override
                public String getValidatorName() {
                    return "EmailFormatValidator";
                }
            }
        );

        ValidationNode validationNode = new ValidationNode("input_validator", validators);
        validationNode.initialize(Map.of());

        // Test valid input
        GraphState validState = new BasicGraphState("validation_test_1");
        validState.set("user_id", "user123");
        validState.set("email", "user@example.com");
        validState.set("message", "Hello world!");
        validState.set("priority", 5);

        NodeContext validContext = new NodeContext("validation_1", "validation_test_1");
        NodeResult validResult = validationNode.execute(validState, validContext);

        System.out.println("Valid Input Test:");
        if (validResult.isSuccess()) {
            boolean passed = validResult.getOutputState().get("validation_passed", Boolean.class).orElse(false);
            List<String> errors = validResult.getOutputState().get("validation_errors", List.class).orElse(List.of());

            System.out.println("  Validation passed: " + passed);
            System.out.println("  Errors: " + errors);
        }

        // Test invalid input
        GraphState invalidState = new BasicGraphState("validation_test_2");
        invalidState.set("user_id", "user123");
        invalidState.set("email", "invalid-email");  // Missing @
        // Missing "message" field
        invalidState.set("priority", 15);  // Out of range

        NodeContext invalidContext = new NodeContext("validation_2", "validation_test_2");
        NodeResult invalidResult = validationNode.execute(invalidState, invalidContext);

        System.out.println("\nInvalid Input Test:");
        if (invalidResult.isSuccess()) {
            boolean passed = invalidResult.getOutputState().get("validation_passed", Boolean.class).orElse(false);
            List<String> errors = invalidResult.getOutputState().get("validation_errors", List.class).orElse(List.of());

            System.out.println("  Validation passed: " + passed);
            System.out.println("  Errors: " + errors);
        }

        validationNode.cleanup();
    }

    /**
     * Aggregation node examples
     */
    private static void runAggregationNodeExamples() throws Exception {
        System.out.println("\n4. Aggregation Node Examples:");

        // Create state with multiple numeric values
        GraphState aggregationState = new BasicGraphState("aggregation_test");
        aggregationState.set("sales_q1", 100000);
        aggregationState.set("sales_q2", 120000);
        aggregationState.set("sales_q3", 110000);
        aggregationState.set("sales_q4", 130000);
        aggregationState.set("region", "North America");

        // Sum aggregation
        AggregationNode sumNode = new AggregationNode("sales_sum",
            AggregationStrategies.sum("sales_q1", "sales_q2", "sales_q3", "sales_q4"),
            "total_sales");
        sumNode.initialize(Map.of());

        NodeContext aggContext = new NodeContext("agg_1", "aggregation_test");
        NodeResult sumResult = sumNode.execute(aggregationState, aggContext);

        if (sumResult.isSuccess()) {
            double totalSales = sumResult.getOutputState().get("total_sales", Double.class).orElse(0.0);
            System.out.println("Sum Aggregation:");
            System.out.println("  Total Sales: $" + String.format("%.0f", totalSales));
        }

        // Average aggregation
        AggregationNode avgNode = new AggregationNode("sales_average",
            AggregationStrategies.average("sales_q1", "sales_q2", "sales_q3", "sales_q4"),
            "average_sales");
        avgNode.initialize(Map.of());

        NodeResult avgResult = avgNode.execute(aggregationState, aggContext);

        if (avgResult.isSuccess()) {
            double avgSales = avgResult.getOutputState().get("average_sales", Double.class).orElse(0.0);
            System.out.println("  Average Sales: $" + String.format("%.0f", avgSales));
        }

        // Text concatenation
        aggregationState.set("product_name", "Super Widget");
        aggregationState.set("version", "v2.1");
        aggregationState.set("status", "Released");

        AggregationNode concatNode = new AggregationNode("product_info",
            AggregationStrategies.concatenate("product_name", "version", "status"),
            "product_description");
        concatNode.initialize(Map.of());

        NodeResult concatResult = concatNode.execute(aggregationState, aggContext);

        if (concatResult.isSuccess()) {
            String description = concatResult.getOutputState().get("product_description", String.class).orElse("");
            System.out.println("  Product Description: " + description);
        }

        // Cleanup
        sumNode.cleanup();
        avgNode.cleanup();
        concatNode.cleanup();
    }

    /**
     * Parallel node execution examples
     */
    private static void runParallelNodeExamples() throws Exception {
        System.out.println("\n5. Parallel Node Examples:");

        // Create parallel processing nodes
        List<GraphNode> parallelNodes = Arrays.asList(
            // Text analysis node
            new DataTransformNode("text_stats",
                state -> {
                    GraphState output = state.copy();
                    String text = state.get("content", String.class).orElse("");

                    output.set("word_count", text.split("\\s+").length);
                    output.set("sentence_count", text.split("[.!?]+").length);

                    return output;
                },
                Set.of("content"),
                Set.of("word_count", "sentence_count")),

            // Sentiment analysis simulation
            new DataTransformNode("sentiment_analysis",
                state -> {
                    GraphState output = state.copy();
                    String text = state.get("content", String.class).orElse("");

                    // Simple sentiment analysis simulation
                    String sentiment;
                    double score;
                    if (text.toLowerCase().contains("great") || text.toLowerCase().contains("excellent")) {
                        sentiment = "positive";
                        score = 0.8;
                    } else if (text.toLowerCase().contains("bad") || text.toLowerCase().contains("terrible")) {
                        sentiment = "negative";
                        score = 0.2;
                    } else {
                        sentiment = "neutral";
                        score = 0.5;
                    }

                    output.set("sentiment", sentiment);
                    output.set("sentiment_score", score);

                    return output;
                },
                Set.of("content"),
                Set.of("sentiment", "sentiment_score")),

            // Language detection simulation
            new DataTransformNode("language_detection",
                state -> {
                    GraphState output = state.copy();
                    String text = state.get("content", String.class).orElse("");

                    // Simple language detection simulation
                    String language;
                    if (text.contains("hola") || text.contains("gracias")) {
                        language = "spanish";
                    } else if (text.contains("bonjour") || text.contains("merci")) {
                        language = "french";
                    } else {
                        language = "english";
                    }

                    output.set("detected_language", language);
                    output.set("language_confidence", 0.85);

                    return output;
                },
                Set.of("content"),
                Set.of("detected_language", "language_confidence"))
        );

        // Initialize all nodes
        for (GraphNode node : parallelNodes) {
            node.initialize(Map.of());
        }

        // Test with merge strategy
        ParallelNode mergeParallelNode = new ParallelNode("content_analyzer",
            parallelNodes, ParallelStrategies.mergeStates());
        mergeParallelNode.initialize(Map.of());

        GraphState parallelState = new BasicGraphState("parallel_test");
        parallelState.set("content", "This is a great example of excellent parallel processing! " +
                                   "The system can analyze multiple aspects simultaneously.");

        NodeContext parallelContext = new NodeContext("parallel_1", "parallel_test");
        NodeResult mergeResult = mergeParallelNode.execute(parallelState, parallelContext);

        if (mergeResult.isSuccess()) {
            GraphState result = mergeResult.getOutputState();
            System.out.println("Parallel Execution (Merge Strategy):");
            System.out.println("  Word count: " + result.get("word_count", Integer.class).orElse(0));
            System.out.println("  Sentiment: " + result.get("sentiment", String.class).orElse("unknown"));
            System.out.println("  Language: " + result.get("detected_language", String.class).orElse("unknown"));
            System.out.println("  Execution time: " + mergeResult.getExecutionTime().toMillis() + "ms");
            System.out.println("  Success count: " + result.get("parallel_success_count", Integer.class).orElse(0));
        }

        // Test with collect strategy
        ParallelNode collectParallelNode = new ParallelNode("content_analyzer_collect",
            parallelNodes, ParallelStrategies.collectResults("analysis_results"));
        collectParallelNode.initialize(Map.of());

        NodeResult collectResult = collectParallelNode.execute(parallelState, parallelContext);

        if (collectResult.isSuccess()) {
            List<Map<String, Object>> results = collectResult.getOutputState()
                .get("analysis_results", List.class).orElse(List.of());

            System.out.println("\nParallel Execution (Collect Strategy):");
            System.out.println("  Individual results count: " + results.size());

            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> result = results.get(i);
                System.out.println("    Result " + (i + 1) + ": " + result.get("node_name") +
                                 " - Success: " + result.get("success") +
                                 " - Time: " + result.get("execution_time") + "ms");
            }
        }

        // Cleanup
        for (GraphNode node : parallelNodes) {
            node.cleanup();
        }
        mergeParallelNode.cleanup();
        collectParallelNode.cleanup();
    }

    /**
     * Advanced patterns and error handling
     */
    private static void runAdvancedPatternsExample() throws Exception {
        System.out.println("\n6. Advanced Patterns Example:");

        // Create a complex processing pipeline with error handling
        ComplexProcessingNode complexNode = new ComplexProcessingNode("document_processor");
        complexNode.initialize(Map.of("retry_count", 2, "timeout_ms", 5000));

        // Test successful processing
        GraphState successState = new BasicGraphState("complex_test");
        successState.set("document_type", "article");
        successState.set("document_content", "This is a well-formed document with proper content structure.");
        successState.set("processing_mode", "standard");

        NodeContext complexContext = new NodeContext("complex_1", "complex_test");
        NodeResult successResult = complexNode.execute(successState, complexContext);

        if (successResult.isSuccess()) {
            System.out.println("Complex Processing Success:");
            GraphState result = successResult.getOutputState();
            System.out.println("  Processing status: " + result.get("processing_status", String.class).orElse("unknown"));
            System.out.println("  Quality score: " + result.get("quality_score", Double.class).orElse(0.0));
            System.out.println("  Processing steps: " + result.get("processing_steps", List.class).orElse(List.of()));
        }

        // Test error handling
        GraphState errorState = new BasicGraphState("error_test");
        errorState.set("document_type", "corrupted");
        errorState.set("document_content", ""); // Empty content should trigger error
        errorState.set("processing_mode", "strict");

        try {
            NodeResult errorResult = complexNode.execute(errorState, complexContext);
            System.out.println("Error handling test result: " +
                (errorResult.isSuccess() ? "Unexpected success" : "Expected failure: " + errorResult.getErrorMessage()));
        } catch (NodeExecutionException e) {
            System.out.println("Expected error caught: " + e.getMessage() + " (Type: " + e.getErrorType() + ")");
        }

        // Demonstrate custom node with configuration
        CustomConfigurableNode configurableNode = new CustomConfigurableNode("configurable_processor");

        Map<String, Object> customConfig = Map.of(
            "processing_level", "detailed",
            "include_metadata", true,
            "max_processing_time", 3000,
            "output_format", "structured"
        );

        configurableNode.initialize(customConfig);

        GraphState configState = new BasicGraphState("config_test");
        configState.set("input_data", "Sample data for configurable processing");
        configState.set("priority", "high");

        NodeResult configResult = configurableNode.execute(configState, complexContext);

        if (configResult.isSuccess()) {
            System.out.println("\nConfigurable Node Result:");
            GraphState result = configResult.getOutputState();
            System.out.println("  Configuration applied: " + result.get("config_applied", Map.class).orElse(Map.of()));
            System.out.println("  Processing level: " + result.get("processing_level_used", String.class).orElse("unknown"));
            System.out.println("  Metadata included: " + result.get("metadata_included", Boolean.class).orElse(false));
        }

        // Cleanup
        complexNode.cleanup();
        configurableNode.cleanup();
    }

    /**
     * Complex processing node with error handling and retries
     */
    private static class ComplexProcessingNode extends BaseGraphNode {

        private int retryCount = 3;
        private long timeoutMs = 10000;

        public ComplexProcessingNode(String name) {
            super(name, "Complex document processing with error handling", NodeType.PROCESSING);
        }

        @Override
        protected void doInitialize(Map<String, Object> config) {
            retryCount = (Integer) config.getOrDefault("retry_count", 3);
            timeoutMs = (Integer) config.getOrDefault("timeout_ms", 10000);
        }

        @Override
        protected GraphState executeInternal(GraphState state, NodeContext context) {
            String docType = state.get("document_type", String.class).orElse("unknown");
            String content = state.get("document_content", String.class).orElse("");
            String mode = state.get("processing_mode", String.class).orElse("standard");

            context.getLogger().logNodeInfo(getName(),
                "Processing " + docType + " document with " + content.length() + " characters in " + mode + " mode");

            // Simulate processing steps
            List<String> steps = new ArrayList<>();
            steps.add("Content validation");

            // Validation
            if ("strict".equals(mode) && content.isEmpty()) {
                throw new RuntimeException("Content validation failed: empty content not allowed in strict mode");
            }

            if ("corrupted".equals(docType)) {
                throw new RuntimeException("Document type not supported: " + docType);
            }

            steps.add("Content analysis");

            // Simulate processing time
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted");
            }

            steps.add("Quality assessment");

            // Calculate quality score
            double qualityScore = Math.min(1.0, content.length() / 100.0 * 0.8 + 0.2);

            steps.add("Result generation");

            // Build output state
            GraphState output = state.copy();
            output.set("processing_status", "completed");
            output.set("quality_score", qualityScore);
            output.set("processing_steps", steps);
            output.set("processed_at", LocalDateTime.now());
            output.set("processor_version", "1.0.0");

            context.getLogger().logNodeInfo(getName(), "Processing completed with quality score: " + qualityScore);

            return output;
        }

        @Override
        public Set<String> getRequiredInputs() {
            return Set.of("document_type", "document_content");
        }

        @Override
        public Set<String> getOutputs() {
            return Set.of("processing_status", "quality_score", "processing_steps", "processed_at", "processor_version");
        }
    }

    /**
     * Custom configurable node demonstrating configuration patterns
     */
    private static class CustomConfigurableNode extends BaseGraphNode {

        private String processingLevel = "basic";
        private boolean includeMetadata = false;
        private long maxProcessingTime = 5000;
        private String outputFormat = "simple";

        public CustomConfigurableNode(String name) {
            super(name, "Configurable processing node", NodeType.PROCESSING);
        }

        @Override
        protected void doInitialize(Map<String, Object> config) {
            processingLevel = (String) config.getOrDefault("processing_level", "basic");
            includeMetadata = (Boolean) config.getOrDefault("include_metadata", false);
            maxProcessingTime = ((Number) config.getOrDefault("max_processing_time", 5000)).longValue();
            outputFormat = (String) config.getOrDefault("output_format", "simple");
        }

        @Override
        protected GraphState executeInternal(GraphState state, NodeContext context) {
            context.getLogger().logNodeInfo(getName(),
                "Using configuration: level=" + processingLevel + ", metadata=" + includeMetadata +
                ", timeout=" + maxProcessingTime + "ms, format=" + outputFormat);

            String inputData = state.get("input_data", String.class).orElse("");
            String priority = state.get("priority", String.class).orElse("normal");

            // Apply configuration-based processing
            GraphState output = state.copy();

            // Record configuration applied
            Map<String, Object> configApplied = Map.of(
                "processing_level", processingLevel,
                "include_metadata", includeMetadata,
                "max_processing_time", maxProcessingTime,
                "output_format", outputFormat
            );
            output.set("config_applied", configApplied);
            output.set("processing_level_used", processingLevel);
            output.set("metadata_included", includeMetadata);

            // Processing based on level
            switch (processingLevel) {
                case "detailed":
                    output.set("detailed_analysis", analyzeDetailed(inputData));
                    output.set("processing_complexity", "high");
                    break;
                case "standard":
                    output.set("standard_analysis", analyzeStandard(inputData));
                    output.set("processing_complexity", "medium");
                    break;
                default:
                    output.set("basic_analysis", analyzeBasic(inputData));
                    output.set("processing_complexity", "low");
            }

            // Include metadata if configured
            if (includeMetadata) {
                Map<String, Object> metadata = Map.of(
                    "input_length", inputData.length(),
                    "priority_level", priority,
                    "processing_timestamp", LocalDateTime.now(),
                    "node_version", "2.1.0"
                );
                output.set("processing_metadata", metadata);
            }

            // Format output based on configuration
            if ("structured".equals(outputFormat)) {
                Map<String, Object> structuredOutput = Map.of(
                    "status", "success",
                    "data", output.getKeys(),
                    "format_version", "1.0"
                );
                output.set("structured_output", structuredOutput);
            }

            return output;
        }

        private String analyzeBasic(String input) {
            return "Basic analysis: length=" + input.length();
        }

        private String analyzeStandard(String input) {
            return "Standard analysis: length=" + input.length() +
                   ", words=" + input.split("\\s+").length;
        }

        private Map<String, Object> analyzeDetailed(String input) {
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("character_count", input.length());
            analysis.put("word_count", input.split("\\s+").length);
            analysis.put("sentence_count", input.split("[.!?]+").length);
            analysis.put("uppercase_ratio",
                (double) input.chars().mapToObj(c -> (char) c)
                    .mapToInt(c -> Character.isUpperCase(c) ? 1 : 0).sum() / input.length());
            analysis.put("complexity_score", Math.random() * 100); // Simulated complexity

            return analysis;
        }

        @Override
        public Set<String> getRequiredInputs() {
            return Set.of("input_data");
        }

        @Override
        public Set<String> getOutputs() {
            Set<String> outputs = new HashSet<>(Set.of(
                "config_applied", "processing_level_used", "metadata_included", "processing_complexity"
            ));

            // Add conditional outputs based on configuration
            switch (processingLevel) {
                case "detailed": outputs.add("detailed_analysis"); break;
                case "standard": outputs.add("standard_analysis"); break;
                default: outputs.add("basic_analysis");
            }

            if (includeMetadata) {
                outputs.add("processing_metadata");
            }

            if ("structured".equals(outputFormat)) {
                outputs.add("structured_output");
            }

            return outputs;
        }
    }
}

/**
 * Mock implementations for the examples
 */

// Basic prompt template implementation for examples
class StringPromptTemplate implements PromptTemplate {
    private final String template;
    private final String description;

    public StringPromptTemplate(String template, String description) {
        this.template = template;
        this.description = description;
    }

    @Override
    public String format(Map<String, Object> variables) throws TemplateException {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }

    @Override
    public Set<String> getRequiredVariables() {
        Set<String> vars = new HashSet<>();
        // Simple extraction - in real implementation would be more sophisticated
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{(\\w+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars;
    }

    @Override
    public Map<String, Object> getOptionalVariables() {
        return Map.of();
    }

    @Override
    public String getDescription() {
        return description;
    }
}
